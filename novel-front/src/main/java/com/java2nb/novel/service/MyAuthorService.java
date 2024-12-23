package com.java2nb.novel.service;

import com.java2nb.novel.core.result.Result;

public interface MyAuthorService {
    Result<?> listAuthorBook(long curr, long limit, Long userId);
}
