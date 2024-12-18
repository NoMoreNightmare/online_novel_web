package com.java2nb.novel.service;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.entity.BookComment;

import java.util.List;

public interface MyBookService {
    Result<?> listClickRank();

    Result<?> listNewRank();

    Result<?> listUpdateRank();

    Book queryBook(long id);

    PageBean<BookComment> queryBookComment(long id, long page, long pageSize);

    List<Book> queryRecommendedBooks(long id);

    Long queryBookFirstChapter(long id);
}
