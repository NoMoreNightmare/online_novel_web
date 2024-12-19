package com.java2nb.novel.controller.page;

import com.java2nb.novel.core.utils.ThreadLocalUtil;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookComment;
import com.java2nb.novel.service.BookService;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.vo.BookCommentVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("book")
public class NonRestBookController {
    @Resource
    private MyBookService bookService;

    @SneakyThrows
    @RequestMapping("{id}.html")
    public String bookDetails(@PathVariable("id") long id, Model model) {
        //查询book的完整信息
        Book book = bookService.queryBook(id);

        //查询这个book的评论信息bookCommentPageBean
        PageBean<BookCommentVO> bookComment = bookService.queryBookComment(id, 1, 10);

        //查询这个book的同类书籍信息recBooks
        List<Book> recBooks = bookService.queryRecommendedBooks(id);

        //查询这个book的首章节信息
        Long chapterId = bookService.queryBookFirstChapter(id);

        model.addAttribute("book", book);
        model.addAttribute("bookCommentPageBean", bookComment);
        model.addAttribute("recBooks", recBooks);
        model.addAttribute("firstBookIndexId", chapterId);

        return "book/book_detail";

    }
}
