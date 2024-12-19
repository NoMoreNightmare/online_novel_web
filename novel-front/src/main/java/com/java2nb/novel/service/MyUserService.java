package com.java2nb.novel.service;

import com.java2nb.novel.entity.User;

import com.java2nb.novel.core.result.Result;

/**
 * @author 10253
 */
public interface MyUserService {
    public Result<?> register(User user, String velCode, String ip);

    public Result<?> login(User user);

    Result<?> refreshToken(String token);

    Result<?> queryIsInShelf(Long bookId, Long userId);

    Result<?> addToBookShelf(Long bookId, long preContentId, Long userId);

    long queryUserBuyRecord(long userId, long bookIndexId);

    boolean queryIsBookRead(Long bookId, Long userId);

    void addReadHistory(Long bookId, Long userId, Long preContentId);

    void updateReadHistory(Long bookId, Long userId, Long preContentId);
}
