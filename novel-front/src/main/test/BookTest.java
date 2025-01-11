import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.FrontNovelApplication;
import com.java2nb.novel.core.result.ElasticSearchConstant;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.vo.BookDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.java2nb.novel.mapper.BookDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;

@SpringBootTest(classes = {FrontNovelApplication.class})
public class BookTest {

    @Autowired
    private FrontBookMapper frontBookMapper;
//    @Autowired
//    private BookIndexMapper bookIndexMapper;
    private RestHighLevelClient client;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup(){
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.200.142:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

//    @Test
//    public void recoverData(){
//
//    }

    /**
     * 创建book索引库
     * @throws IOException
     */
    @Test
    public void createBookIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("book");

        String a = ElasticSearchConstant.INDEX_TEMPLATE;

        request.source(ElasticSearchConstant.INDEX_TEMPLATE, XContentType.JSON);

        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    public void deleteBookIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("book");

        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    public void initializeBookIndex() throws IOException {
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

        client.bulk(request, RequestOptions.DEFAULT);
    }

    @Test
    public void testSendToFanoutExchange(){
        String exchange = "novel.book.fanout";
        String message = "test";

        rabbitTemplate.convertAndSend(exchange,"", message);
    }

    @Test
    public void testRecoverDataFromES() throws IOException {
        SearchRequest request = new SearchRequest("book");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        ObjectMapper objectMapper = new ObjectMapper();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            String sourceAsString = hit.getSourceAsString();
            BookDoc bookDoc = objectMapper.readValue(sourceAsString, new TypeReference<BookDoc>() {});
            UpdateStatementProvider update = update(book)
                    .set(lastIndexId)
                    .equalTo(bookDoc.getLastIndexId())
                    .set(lastIndexName)
                    .equalTo(bookDoc.getLastIndexName())
                    .set(lastIndexUpdateTime)
                    .equalTo(bookDoc.getUpdateTime())
                    .where(id, isEqualTo(bookDoc.getId()))
                    .build()
                    .render(RenderingStrategy.MYBATIS3);

            frontBookMapper.update(update);


        }
    }
}
