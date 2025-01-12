package com.java2nb.novel.core.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.RabbitMQConstant;
import com.java2nb.novel.core.utils.MQManager;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.mapper.BookMapper;
import com.java2nb.novel.mapper.FrontBookMapper;
import com.java2nb.novel.service.MyAuthorService;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

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

    @XxlJob("visitCountHandler")
    public ReturnT<String> visitCountHandler(String param) throws Exception {
        Map<Object, Object> map = cacheService.hmGetAll(CacheKey.BOOK_ADD_VISIT_COUNT);
        cacheService.del(CacheKey.BOOK_ADD_VISIT_COUNT);
        if(map == null || map.isEmpty()) {
            return ReturnT.SUCCESS;
        }
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            long bookId = (Long) entry.getKey();
            long currVisitCount = (Long) entry.getValue();
            UpdateStatementProvider updateVisitCount = update(book)
                    .set(visitCount)
                    .equalToConstant(visitCount.name() + " + " + currVisitCount)
                    .where(id, isEqualTo(bookId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            bookMapper.update(updateVisitCount);
            mqManager.sendBookMessage(bookId, RabbitMQConstant.RABBITMQ_BOOK_UPDATE_BINDING_KEY);
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
}

