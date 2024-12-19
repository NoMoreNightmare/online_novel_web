import cn.hutool.core.util.SerializeUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.entity.Book;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class JSONTest {
    @Test
    public void JSONtest() throws JsonProcessingException {
        Book book = new Book();
        book.setBookDesc("This is a book");

        List<Book> list = new ArrayList<Book>();
        list.add(book);

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(list);

        System.out.println(s);

        List<Book> books = objectMapper.readValue(s, new TypeReference<List<Book>>() {
        });

        System.out.println(books);
    }
}
