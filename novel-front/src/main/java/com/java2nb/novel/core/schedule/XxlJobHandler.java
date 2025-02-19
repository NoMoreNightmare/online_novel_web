package com.java2nb.novel.core.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.RabbitMQConstant;
import com.java2nb.novel.core.result.RedisConstant;
import com.java2nb.novel.core.utils.MQManager;

import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyAuthorService;
//import com.java2nb.novel.service.impl.BookContentHtmlService;
//import com.java2nb.novel.core.utils.SFTPFileUploadUtil;
import com.java2nb.novel.vo.BookDoc;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@Component
@Slf4j
public class XxlJobHandler {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private MQManager mqManager;
    @Autowired
    private FrontBookMapper frontBookMapper;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private BookIndexMapper bookIndexMapper;
    @Autowired
    private BookContentMapper bookContentMapper;
//    @Autowired
//    private BookContentHtmlService bookContentHtmlService;
//    @Autowired
//    private SFTPFileUploadUtil sftpFileUploadUtil;

    @XxlJob("visitCountHandler")
    public ReturnT<String> visitCountHandler(String param) throws Exception {
        Set<ZSetOperations.TypedTuple<String>> set = cacheService.zetGetAll(CacheKey.BOOK_ADD_VISIT_COUNT);

        //缓存这10分钟里最热门的50本book的信息
        Set<String> hotBooks = cacheService.zsetRankBy(CacheKey.BOOK_ADD_VISIT_COUNT, RedisConstant.FIRST_RANK, RedisConstant.LAST_RANK);

        cacheService.del(CacheKey.BOOK_ADD_VISIT_COUNT);
        if(set == null || set.isEmpty()) {
            return ReturnT.SUCCESS;
        }
        for (ZSetOperations.TypedTuple<String> tuple : set) {
            long bookId = Long.parseLong(tuple.getValue());
            long currVisitCount = tuple.getScore().longValue();
            UpdateStatementProvider updateVisitCount = update(book)
                    .set(visitCount)
                    .equalToConstant(visitCount.name() + " + " + currVisitCount)
                    .where(id, isEqualTo(bookId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            bookMapper.update(updateVisitCount);
            mqManager.sendBookMessage(bookId, RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
        }

        for (String hotBook : hotBooks) {
            Long bookId = Long.parseLong(hotBook);
            //查询是否缓存了这本书
            boolean contains = cacheService.contains(String.valueOf(bookId));
            if (contains) {
                //是，更新expire时间为20分钟
                cacheService.expire(String.valueOf(bookId), RedisConstant.BOOK_MAX_TTL);
            }else{
                //否，查询数据库并设置expire为20分钟
                SelectStatementProvider select = select(id, bookName, catId, catName, picUrl, authorName, bookStatus, visitCount, wordCount, bookDesc, lastIndexId, lastIndexUpdateTime, lastIndexName)
                        .from(book)
                        .where(id, isEqualTo(bookId))
                        .build()
                        .render(RenderingStrategy.MYBATIS3);


                ObjectMapper objectMapper = new ObjectMapper();
                Book book = bookMapper.selectOne(select).get();
                try {
                    String bookJson = objectMapper.writeValueAsString(book);
                    cacheService.set(RedisConstant.BOOK_KEY + String.valueOf(bookId), bookJson, RedisConstant.BOOK_MAX_TTL);
                } catch (JsonProcessingException e) {
                    return ReturnT.FAIL;
                }


                //查询这本书的最近更新的4个章节，并缓存
                SelectStatementProvider last4BookIndex = select(BookIndexDynamicSqlSupport.id)
                        .from(BookIndexDynamicSqlSupport.bookIndex)
                        .where(id, isEqualTo(bookId))
                        .orderBy(BookIndexDynamicSqlSupport.createTime.descending())
                        .limit(4)
                        .build()
                        .render(RenderingStrategy.MYBATIS3);

                List<BookIndex> indices = bookIndexMapper.selectMany(last4BookIndex);
                for (BookIndex index : indices) {

                    SelectStatementProvider lastBookContent = select(BookContentDynamicSqlSupport.id, BookContentDynamicSqlSupport.indexId, BookContentDynamicSqlSupport.content)
                            .from(BookContentDynamicSqlSupport.bookContent)
                            .where(BookContentDynamicSqlSupport.indexId, isEqualTo(index.getId()))
                            .build()
                            .render(RenderingStrategy.MYBATIS3);
                    Optional<BookContent> bookContent = bookContentMapper.selectOne(lastBookContent);
                    BookContent content = bookContent.get();
                    try {
                        String bookContentJson = objectMapper.writeValueAsString(content);
                        cacheService.set(RedisConstant.BOOK_INDEX_KEY + String.valueOf(index.getId()), bookContentJson, RedisConstant.BOOK_CONTENT_MAX_TTL);
                    } catch (JsonProcessingException e) {
                        return ReturnT.FAIL;
                    }
                }
            }



        }

        return ReturnT.SUCCESS;
    }

    @XxlJob("fullDataSynchronizeHandler")
    public ReturnT<String> fullDataSynchronizeHandler(String param) throws Exception {
        SelectStatementProvider selectAll = select(id, catId, catName, bookName, lastIndexId, lastIndexName, authorName, wordCount, updateTime, visitCount, bookStatus, bookDesc)
                .from(book)
                .build()
                .render(RenderingStrategy.MYBATIS3);

        List<Book> books = frontBookMapper.selectMany(selectAll);
        ObjectMapper objectMapper = new ObjectMapper();
        BulkRequest request = new BulkRequest();

        for (Book book : books) {
            BookDoc bookDoc = new BookDoc(book);
            request.add(new IndexRequest("book")
                    .id(bookDoc.getId().toString())
                    .source(objectMapper.writeValueAsString(bookDoc), XContentType.JSON));
        }

        BulkResponse result = client.bulk(request, RequestOptions.DEFAULT);
        if(result.hasFailures()){
            return ReturnT.FAIL;
        }


        return ReturnT.SUCCESS;
    }

//    @XxlJob("writeToNginxHandler")
//    public ReturnT<String> writeToNginxHandler(String param) throws Exception {
//        bookContentHtmlService.createHTML();
//        sftpFileUploadUtil.uploadFile();
//        return ReturnT.SUCCESS;
//    }
}

