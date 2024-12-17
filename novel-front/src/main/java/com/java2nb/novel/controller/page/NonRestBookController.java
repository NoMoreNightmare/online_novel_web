package com.java2nb.novel.controller.page;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("book")
@Slf4j
public class NonRestBookController {
    @RequestMapping("{id}.html")
    public String book(@PathVariable("id") String id, Model model) {

    }
}
