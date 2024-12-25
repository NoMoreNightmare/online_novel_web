import com.java2nb.novel.FrontNovelApplication;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookIndex;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyBookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import java.util.List;
import java.util.Optional;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

//@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FrontNovelApplication.class})
public class BookTest {

    @Autowired
    private FrontBookMapper frontBookMapper;
    @Autowired
    private BookIndexMapper bookIndexMapper;

    @Test
    public void recoverData(){

    }
}
