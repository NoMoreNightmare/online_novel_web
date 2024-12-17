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
        //TODO：myBookService的实现还没有考虑过多个线程并发访问，导致重复重建缓存的问题（当n个请求到来时，
        // 发现redis没有key，都会尝试去数据库查询并重建缓存，这样效率会下降
        // 考虑用cas锁的方式来解决问题（首先判断缓存是否存在，不存在，再加锁重建）
        return myBookService.listClickRank();
    }

    @GetMapping("listNewRank")
    public Result<?> listNewRank() {
        return myBookService.listNewRank();
    }

    @GetMapping("listUpdateRank")
    public Result<?> listUpdateRank() {
        return myBookService.listUpdateRank();
    }
}
