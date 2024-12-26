package com.java2nb.novel.controller;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.IpUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.core.utils.PictureUtil;
import com.java2nb.novel.entity.User;
import com.java2nb.novel.mapper.UserFeedbackMapper;
import com.java2nb.novel.service.MyUserService;
import com.java2nb.novel.service.UserService;
import com.java2nb.novel.service.impl.MyUserServiceImpl;
import io.github.xxyopen.web.valid.AddGroup;
import io.github.xxyopen.web.valid.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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
    @Autowired
    private UserFeedbackMapper userFeedbackMapper;

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

        String token = getToken(request);
        return userService.refreshToken(token);
    }

    @GetMapping("queryIsInShelf")
    public Result<?> queryIsInShelf(Long bookId, HttpServletRequest request) {
        String token = getToken(request);

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();
        return userService.queryIsInShelf(bookId, userId);
    }

    @PostMapping("addToBookShelf")
    public Result<?> addToBookShelf(Long bookId, long preContentId, HttpServletRequest request) {
        String token = getToken(request);

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();
        return userService.addToBookShelf(bookId, preContentId, userId);
    }

    @PostMapping("addReadHistory")
    public Result<?> addReadHistory(Long bookId, Long preContentId, HttpServletRequest request) {
        String token = getToken(request);

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        //查询是否存在这个书的阅读记录
        boolean isRead = userService.queryIsBookRead(bookId, userId);

        if(!isRead){
            userService.addReadHistory(bookId, userId, preContentId);
        }else{
            userService.updateReadHistory(bookId, userId, preContentId);
        }

        return Result.ok();


    }

    @GetMapping("listCommentByPage")
    public Result<?> listCommentByPage(Long curr, Long limit, HttpServletRequest request) {
        String token = getToken(request);

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        return userService.listCommentByPage(userId, curr, limit);
    }

    @GetMapping("userInfo")
    public Result<?> userInfo(HttpServletRequest request) {
        String token = getToken(request);

        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        return userService.getUserInfo(userId);
    }

    private static String getToken(HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }
        return token;
    }

    @GetMapping("listBookShelfByPage")
    public Result<?> listBookShelfByPage(Long limit, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();
        return userService.listBookShelfByPage(userId, limit);
    }

    @GetMapping("listReadHistoryByPage")
    public Result<?> listReadHistoryByPage(Long curr, Long limit, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        return userService.listReadHistoryByPage(userId, curr, limit);
    }

    @GetMapping("listUserFeedBackByPage")
    public Result<?> listUserFeedBackByPage(Long curr, Long limit, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        return userService.listUserFeedbackByPage(userId, curr, limit);
    }

    @PostMapping("updatePassword")
    public Result<?> updatePassword(String oldPassword, String newPassword1, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        int affectedLine = userService.updatePassword(userId, oldPassword, newPassword1);
        if(affectedLine == 0){
            return Result.customError("无法修改密码，请稍后重试", 2023);
        }else{
            return Result.ok();
        }
    }

    @PostMapping("addFeedBack")
    public Result<?> addFeedBack(String content, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        int insertSuccess = userFeedbackMapper.addFeedback(userId, content);

        if(insertSuccess == 0){
            return Result.customError("反馈失败，可能是网络或服务器问题，请稍后重试", 2024);
        }
        return Result.ok();
    }

    @PostMapping("updateUserInfo")
    public Result<?> updateUserInfo(User user, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
        Long userId = userDetails.getId();

        if(user.getNickName() != null && user.getNickName().length() > 0){
            String nickName = user.getNickName();
            int updateSuccess = userService.updateNickName(userId, nickName);
            if(updateSuccess == 0){
                return Result.customError("更改失败，请稍后重试", 2025);
            }
            userDetails.setNickName(nickName);
            token = jwtTokenUtil.generateToken(userDetails);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            return Result.ok(map);
        }

        if(user.getUserSex() != null){
            Byte userSex = user.getUserSex();
            userService.updateUserSex(userId, userSex);
            return Result.ok();
        }

        if(user.getUserPhoto() != null){
            String userPhoto = user.getUserPhoto();
            //删除原有照片
            String picPath = userService.queryUserPhoto(userId);
            if(picPath != null && picPath.length() > 0){
                PictureUtil.deleteCurrPic(picPath);
            }

            //新增照片
            userService.updateUserPhoto(userId, userPhoto);
        }


        return Result.ok();


    }

}
