package com.java2nb.novel.service;

import com.java2nb.novel.core.result.Result;

public interface MyBookService {
    Result<?> listClickRank();

    Result<?> listNewRank();

    Result<?> listUpdateRank();
}
