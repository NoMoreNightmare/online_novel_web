package com.java2nb.novel.controller.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java2nb.novel.core.utils.ThreadLocalUtil;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.News;
import com.java2nb.novel.service.BookService;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.service.MyNewsService;
import com.java2nb.novel.vo.BookSettingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@Slf4j
public class NonRestIndexController {
    @Autowired
    MyBookService bookService;
    @Autowired
    MyNewsService newsService;

    @RequestMapping({"/", "/index", "index.html"})
    public String index(Model model) {
        //bookMap首页的各种推荐
//
//            //轮播图
//        List<BookDescriptionVO> carouseBooks = bookService.queryCarouseBooks(0, 5);
//            //热门推荐的10本书
//        List<BookDescriptionVO> hotRecommendedBooks = bookService.queryHotRecommendedBooks(5, 10);
//            //本周强推
//        List<BookDescriptionVO> weekRecommendedBooks = bookService.queryWeekRecommendedBooks(15, 5);
//            //热门推荐
//        List<BookDescriptionVO> hotRecBooks = bookService.queryHotRecBooks(20, 6);
//            //精品推荐
//        List<BookDescriptionVO> classicBooks = bookService.queryClassicBooks(26, 6);
//加载小说首页小说基本信息线程
        Map<Byte, List<BookSettingVO>> map= bookService.listBookSettingVO();
        //加载首页新闻线程
        model.addAttribute("bookMap", map);

        //newList新闻
        List<News> list = newsService.queryTop3NewsInThisWeek();


        model.addAttribute("newsList", list);

        return "index";

    }
}
