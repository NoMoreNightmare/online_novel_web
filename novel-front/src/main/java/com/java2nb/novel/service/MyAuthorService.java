package com.java2nb.novel.service;

import com.java2nb.novel.core.result.Result;

public interface MyAuthorService {
    Result<?> listAuthorBook(long curr, long limit, Long userId);


    Result<?> listIncomeDailyByPage(long curr, long limit, long bookId, Long userId);

    Result<?> listIncomeMonthByPage(long curr, long limit, Long userId);


    Result<?> deleteIndex(long indexId, Long userId);
}
