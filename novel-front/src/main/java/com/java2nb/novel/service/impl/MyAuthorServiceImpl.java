package com.java2nb.novel.service.impl;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.mapper.FrontBookMapper;
import com.java2nb.novel.service.MyAuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MyAuthorServiceImpl implements MyAuthorService {
    @Autowired
    private FrontBookMapper frontBookMapper;
    @Override
    public Result<?> listAuthorBook(long curr, long limit, Long userId) {
        PageBean<Book> pageBean = new PageBean<>(curr, limit);
        long total = frontBookMapper.countTotalAuthorBookNumber(userId);
        List<Book> books = frontBookMapper.listAuthorBook((curr - 1) * limit, limit, userId);

        pageBean.setTotal(total);
        pageBean.setList(books);
        return Result.ok(pageBean);
    }
}
