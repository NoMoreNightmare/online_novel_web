import com.java2nb.novel.FrontNovelApplication;
import com.java2nb.novel.core.result.ElasticSearchConstant;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyBookService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

@SpringBootTest(classes = {FrontNovelApplication.class})
public class BookTest {
//
//    @Autowired
//    private FrontBookMapper frontBookMapper;
//    @Autowired
//    private BookIndexMapper bookIndexMapper;
private RestHighLevelClient client;

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
    public void deleteHotelIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("book");

        client.indices().delete(request, RequestOptions.DEFAULT);
    }
}
