package com.java2nb.novel.core.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.result.RabbitMQConstant;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookContent;
import com.java2nb.novel.mapper.BookContentDynamicSqlSupport;
import com.java2nb.novel.mapper.BookContentMapper;
import com.java2nb.novel.mapper.BookDynamicSqlSupport;
import com.java2nb.novel.mapper.FrontBookMapper;
import com.java2nb.novel.service.impl.BookCacheServiceImpl;
import com.java2nb.novel.vo.BookDoc;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;

@Component
@Slf4j
public class RabbitMQListener {

    @Resource(name = "bookCaffeineQueueName")
    private String bookCaffeineQueueName;

    @Resource(name = "bookContentCaffeineQueueName")
    private String bookContentCaffeineQueueName;

    @Resource(name = "bookContentDeleteCaffeineQueueName")
    private String bookContentDeleteCaffeineQueueName;

    @Autowired
    private FrontBookMapper frontBookMapper;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private BookCacheServiceImpl bookCacheService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private BookContentMapper bookContentMapper;

    @RabbitListener(queues = "#{bookCaffeineQueueName}")
    public void receiveBookCaffeine(Long bookId) throws JsonProcessingException {
        //先查询caffeine里存在不存在（不存在说明不是热点数据，不需要占用缓存空间）
        if(bookCacheService.getBookByKey(String.valueOf(bookId)) != null) {
            SelectStatementProvider select = select(id, bookName, catId, catName, picUrl, authorName, bookStatus, visitCount, wordCount, bookDesc, lastIndexId, lastIndexUpdateTime, lastIndexName)
                    .from(book)
                    .where(id, isEqualTo(bookId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            ObjectMapper objectMapper = new ObjectMapper();
            Optional<Book> optional = frontBookMapper.selectOne(select);
            if (optional.isPresent()) {
                Book book = optional.get();
                bookCacheService.putBookByKey(String.valueOf(bookId), objectMapper.writeValueAsString(book));
            }
        }

    }

    @RabbitListener(queues = RabbitMQConstant.RABBITMQ_BOOK_ES_UPDATE_QUEUE)
    public void receiveBookEsQueue(Long bookId) throws IOException {
        //book信息的修改：包括visitcount，wordcount，last index相关等
        SelectStatementProvider selectOne = select(lastIndexId, lastIndexName, wordCount, updateTime, visitCount)
                .from(book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<Book> bookOptional = frontBookMapper.selectOne(selectOne);
        if(!bookOptional.isPresent()) {
            return;
        }
        Book book = bookOptional.get();
        UpdateRequest updateRequest = new UpdateRequest("book", bookId.toString());
        updateRequest.doc(
                lastIndexId.name(), book.getLastIndexId(),
                lastIndexName.name(), book.getLastIndexName(),
                wordCount.name(), book.getWordCount(),
                updateTime.name(), book.getUpdateTime(),
                visitCount.name(), book.getVisitCount()
        );
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    @RabbitListener(queues = RabbitMQConstant.RABBITMQ_BOOK_ES_ADD_QUEUE)
    public void receiveBookEsAdd(Long bookId) throws IOException {
        //新增了一本book
        SelectStatementProvider selectOne = select(id, bookName, authorName, catId, catName, lastIndexId, lastIndexName, wordCount, updateTime, visitCount, bookStatus, bookDesc)
                .from(book)
                .where(id, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<Book> bookOptional = frontBookMapper.selectOne(selectOne);
        if(!bookOptional.isPresent()) {
            return;
        }
        Book book = bookOptional.get();
        BookDoc bookDoc = new BookDoc(book);
        ObjectMapper objectMapper = new ObjectMapper();

        IndexRequest request = new IndexRequest("book").id(bookId.toString());
        request.source(objectMapper.writeValueAsString(bookDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @RabbitListener(queues = RabbitMQConstant.RABBITMQ_BOOK_REDIS_QUEUE)
    public void receiveBookRedisQueue(Long bookId) throws JsonProcessingException {
        //先查询redis里存在不存在（不存在说明不是热点数据，不需要占用缓存空间）
        if(cacheService.get(String.valueOf(bookId)) != null) {
            SelectStatementProvider select = select(id, bookName, catId, catName, picUrl, authorName, bookStatus, visitCount, wordCount, bookDesc, lastIndexId, lastIndexUpdateTime, lastIndexName)
                    .from(book)
                    .where(id, isEqualTo(bookId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            ObjectMapper objectMapper = new ObjectMapper();
            Optional<Book> optional = frontBookMapper.selectOne(select);
            if (optional.isPresent()) {
                Book book = optional.get();
                cacheService.set(String.valueOf(bookId), objectMapper.writeValueAsString(book));
            }
        }

    }

    @RabbitListener(queues = "#{bookContentCaffeineQueueName}")
    public void receiveBookContentCaffeine(Long bookIndexId) throws JsonProcessingException {
        if(bookCacheService.getBookContentByKey(String.valueOf(bookIndexId)) != null) {
            SelectStatementProvider select = select(BookContentDynamicSqlSupport.id, BookContentDynamicSqlSupport.indexId, BookContentDynamicSqlSupport.content)
                    .from(BookContentDynamicSqlSupport.bookContent)
                    .where(BookContentDynamicSqlSupport.indexId, isEqualTo(bookIndexId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            ObjectMapper objectMapper = new ObjectMapper();
            Optional<BookContent> optional = bookContentMapper.selectOne(select);
            if (optional.isPresent()) {
                BookContent bookContent = optional.get();
                bookCacheService.putBookContentByKey(String.valueOf(bookIndexId), objectMapper.writeValueAsString(bookContent));
            }
        }
    }

    @RabbitListener(queues = RabbitMQConstant.RABBITMQ_BOOK_CONTENT_REDIS_QUEUE)
    public void receiveBookContentRedisQueue(Long bookIndexId) throws JsonProcessingException {
        if(cacheService.get(String.valueOf(bookIndexId)) != null) {
            SelectStatementProvider select = select(BookContentDynamicSqlSupport.id, BookContentDynamicSqlSupport.indexId, BookContentDynamicSqlSupport.content)
                    .from(BookContentDynamicSqlSupport.bookContent)
                    .where(BookContentDynamicSqlSupport.indexId, isEqualTo(bookIndexId))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            ObjectMapper objectMapper = new ObjectMapper();
            Optional<BookContent> optional = bookContentMapper.selectOne(select);
            if (optional.isPresent()) {
                BookContent bookContent = optional.get();
                cacheService.set(String.valueOf(bookIndexId), objectMapper.writeValueAsString(bookContent));
            }
        }
    }

    @RabbitListener(queues = RabbitMQConstant.RABBITMQ_BOOK_CONTENT_REDIS_DELETE_QUEUE)
    public void receiveBookContentRedisDelete(Long bookIndexId) throws JsonProcessingException {
        cacheService.del(String.valueOf(bookIndexId));
    }

    @RabbitListener(queues = "#{bookCaffeineQueueName}")
    public void receiveBookContentCaffeineDelete(Long bookIndexId) throws JsonProcessingException {
        bookCacheService.delBookContentByKey(String.valueOf(bookIndexId));
    }

}
