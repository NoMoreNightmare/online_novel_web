package com.java2nb.novel.service;

import com.java2nb.novel.entity.User;

import java.util.Map;
import com.java2nb.novel.core.result.Result;

/**
 * @author 10253
 */
public interface MyUserService {
    public Result<?> register(User user, String velCode, String ip);

    public Result<?> login(User user);

    Result<?> refreshToken(String token);
}
