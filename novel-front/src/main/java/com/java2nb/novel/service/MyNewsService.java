package com.java2nb.novel.service;

import com.java2nb.novel.entity.News;

import java.util.List;

public interface MyNewsService {
    public List<News> queryTop3NewsInThisWeek();
}
