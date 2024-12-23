package com.java2nb.novel.controller.page;

import com.java2nb.novel.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class NonRestIndexController {
    @Autowired
    BookService bookService;

//    @RequestMapping({"/", "/index", "index.html"})
//    public String index() {
//
//    }
}
