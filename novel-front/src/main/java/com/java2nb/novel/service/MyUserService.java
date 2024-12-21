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

    Result<?> listCommentByPage(Long userId, Long curr, Long limit);

    Result<?> getUserInfo(Long userId);

    Result<?> listBookShelfByPage(Long userId, Long limit);

    Result<?> listReadHistoryByPage(Long userId, long l, Long limit);

    Result<?> listUserFeedbackByPage(Long userId, Long curr, Long limit);

    void updateUserSex(Long userId, byte userSex);

    int updatePassword(Long userId, String oldPassword, String newPassword);

    int updateNickName(Long userId, String nickName);

    void updateUserPhoto(Long userId, String userPhoto);

    String queryUserPhoto(Long userId);
}
