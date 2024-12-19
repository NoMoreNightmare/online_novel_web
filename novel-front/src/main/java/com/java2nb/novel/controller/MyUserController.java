package com.java2nb.novel.controller;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.IpUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.entity.User;
import com.java2nb.novel.service.MyUserService;
import com.java2nb.novel.service.UserService;
import com.java2nb.novel.service.impl.MyUserServiceImpl;
import io.github.xxyopen.web.valid.AddGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author 10253
 */
@RestController
@RequestMapping("user")
@Slf4j
public class MyUserController {

    @Resource
    private MyUserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("register")
    public Result<?> register(@Validated({AddGroup.class}) User user,
                           @RequestParam(value = "velCode", defaultValue = "") String velCode, HttpServletRequest request) {
         return userService.register(user, velCode, IpUtil.getRealIp(request));
    }

    @PostMapping("login")
    public Result<?> login(User user) {
        return userService.login(user);
    }

    @PostMapping("refreshToken")
    public Result<?> refreshToken(HttpServletRequest request) {

        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }
        return userService.refreshToken(token);
    }

    @GetMapping("queryIsInShelf")
    public Result<?> queryIsInShelf(Long bookId, HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();
        return userService.queryIsInShelf(bookId, userId);
    }

    @PostMapping("addToBookShelf")
    public Result<?> addToBookShelf(Long bookId, long preContentId, HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();
        return userService.addToBookShelf(bookId, preContentId, userId);
    }

}
