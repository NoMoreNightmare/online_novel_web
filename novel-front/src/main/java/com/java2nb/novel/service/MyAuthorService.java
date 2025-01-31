package com.java2nb.novel.service;

import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.entity.Author;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.vo.BookContentVO;

public interface MyAuthorService {
    Result<?> listAuthorBook(long curr, long limit, Long userId);


    Result<?> listIncomeDailyByPage(long curr, long limit, long bookId, Long userId);

    Result<?> listIncomeMonthByPage(long curr, long limit, Long userId);


    Result<?> deleteIndex(long indexId, Long userId);

    Result<?> queryIndexContent(long indexId);

    Result<?> updateBookContent(BookContentVO bookContent);

    Result<?> addBook(Book book, Long userId);

    Result<?> updateBookPic(Long bookId, String bookPic);

    String register(Long userId, Author author);

    Result<?> checkPenName(String penName);

    Result<?> addBookContent(BookContentVO bookContent, Long bookId, Byte isVip);

    void addBookContentByMe(BookContentVO bookContentVO, long l, byte b);
}
