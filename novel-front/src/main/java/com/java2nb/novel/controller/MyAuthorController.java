package com.java2nb.novel.controller;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.service.MyAuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("author")
public class MyAuthorController {
    @Autowired
    private MyAuthorService myAuthorService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @GetMapping("listBookByPage")
    public Result<?> listBookByPage(long curr, long limit, HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }

        //TODO 当前book_comment表为（book_id, create_user_id）这个字段创建了唯一键unique key，因此一个用户不能在同一本书上
        //评论多次，这个特性看看日后能不能改动
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myAuthorService.listAuthorBook(curr, limit, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

    }
}
