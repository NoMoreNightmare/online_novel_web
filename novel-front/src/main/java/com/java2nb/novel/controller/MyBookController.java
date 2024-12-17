package com.java2nb.novel.controller;

import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.service.MyBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("book")
@RestController
@Slf4j
public class MyBookController {
    @Resource
    MyBookService myBookService;

    @GetMapping("listClickRank")
    public Result<?> listClickRank() {
        return myBookService.listClickRank();
    }
}
