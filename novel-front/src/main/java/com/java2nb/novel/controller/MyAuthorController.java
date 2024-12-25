package com.java2nb.novel.controller;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.entity.Book;
import com.java2nb.novel.service.MyAuthorService;
import com.java2nb.novel.vo.BookContentVO;
import com.java2nb.novel.vo.BookVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;

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
        String token = getToken(request);

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

    @GetMapping("listIncomeDailyByPage")
    public Result<?> listIncomeDailyByPage(@RequestParam("curr") long curr, @RequestParam("limit") long limit, @RequestParam(value = "bookId", defaultValue = "-1") long bookId, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myAuthorService.listIncomeDailyByPage(curr, limit, bookId, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @GetMapping("listIncomeMonthByPage")
    public Result<?> listIncomeDailyByPage(@RequestParam("curr") long curr, @RequestParam("limit") long limit, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myAuthorService.listIncomeMonthByPage(curr, limit, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @DeleteMapping("deleteIndex/{indexId}")
    public Result<?> deleteIndex(@PathVariable("indexId") long indexId, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myAuthorService.deleteIndex(indexId, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

    }

    @GetMapping("queryIndexContent/{indexId}")
    public Result<?> queryIndexContent(@PathVariable("indexId") long indexId, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
//            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
//            Long userId = userDetails.getId();
            return myAuthorService.queryIndexContent(indexId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @PostMapping("updateBookContent")
    public Result<?> updateBookContent(BookContentVO bookContent, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            return myAuthorService.updateBookContent(bookContent);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @PostMapping("addBook")
    public Result<?> addBook(Book book, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myAuthorService.addBook(book, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @PostMapping("updateBookPic")
    public Result<?> updateBookPic(Long bookId, String bookPic, HttpServletRequest request) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            return myAuthorService.updateBookPic(bookId, bookPic);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    private static String getToken(HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }
        return token;
    }
}
