package com.java2nb.novel.controller;

import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.service.MyAuthorService;
import com.java2nb.novel.service.MyBookService;
import com.java2nb.novel.vo.BookContentVO;
import com.java2nb.novel.vo.BookDoc;
import com.java2nb.novel.vo.BookVO;
import com.java2nb.novel.vo.SearchDataVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;

@RequestMapping("book")
@RestController
@Slf4j
public class MyBookController {
    @Resource
    MyBookService myBookService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${elasticsearch.enable}")
    private boolean elasticsearchEnable;
    @Autowired
    private MyAuthorService myAuthorService;

    @GetMapping("listClickRank")
    public Result<?> listClickRank() {
        //TODO：myBookService的实现还没有考虑过多个线程并发访问，导致重复重建缓存的问题（当n个请求到来时，
        // 发现redis没有key，都会尝试去数据库查询并重建缓存，这样效率会下降
        // 考虑用cas锁的方式来解决问题（首先判断缓存是否存在，不存在，再加锁重建）
        return myBookService.listClickRank();
    }

    @GetMapping("listNewRank")
    public Result<?> listNewRank() {
        return myBookService.listNewRank();
    }

    @GetMapping("listUpdateRank")
    public Result<?> listUpdateRank() {
        return myBookService.listUpdateRank();
    }

    @GetMapping("queryBookIndexAbout")
    public Result<?> queryBookIndexAbout(Long bookId, Long lastBookIndexId) {
        return myBookService.queryBookIndexAbout(bookId, lastBookIndexId);
    }

    @PostMapping("addVisitCount")
    public Result<?> addVisitCount(Long bookId) {
        return myBookService.addVisitCount(bookId);
    }

    @PostMapping("addBookComment")
    public Result<?> addBookComment(Long bookId, String commentContent, HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }

        //TODO 当前book_comment表为（book_id, create_user_id）这个字段创建了唯一键unique key，因此一个用户不能在同一本书上
        //评论多次，这个特性看看日后能不能改动
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            return myBookService.addBookComment(bookId, commentContent, userDetails.getId());
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

    }

    @GetMapping("listCommentByPage")
    public Result<?> listCommentByPage(Long bookId, @RequestParam(defaultValue = "1") Long curr, @RequestParam(defaultValue = "5") Long limit) {
        return myBookService.listCommentByPage(bookId, curr, limit);
    }

    @GetMapping("searchByPage")
    @SneakyThrows
    public Result<?> searchByPage(SearchDataVO searchData) {
        if(elasticsearchEnable && searchData.getKeyword() != null && !searchData.getKeyword().isEmpty()) {
            PageBean<BookDoc> pageBean = new PageBean<>(searchData.getCurr(), searchData.getLimit());
            myBookService.queryUsingElasticSearch(pageBean, searchData);
            return Result.ok(pageBean);
        }else{
            PageBean<BookVO> pageBean = new PageBean<>(searchData.getCurr(), searchData.getLimit());

            List<BookVO> list = CompletableFuture.supplyAsync(() -> myBookService.queryWithCondition(searchData)).get();

//            List<BookVO> list = myBookService.queryWithCondition(searchData);

            int total = CompletableFuture.supplyAsync(() -> myBookService.queryWithConditionTotal(searchData)).get();

//            int total = myBookService.queryWithConditionTotal(searchData);

            pageBean.setTotal(total);
            pageBean.setList(list);
            return Result.ok(pageBean);
        }


    }

    @GetMapping("listBookCategory")
    public Result<?> listBookCategory() {
        return myBookService.queryAllCategory();
    }

    @GetMapping("listRank")
    public Result<?> listRank(int type, int limit) {
        return myBookService.listRank(type, limit);
    }

    @GetMapping("queryIndexList")
    public Result<?> queryIndexList(Long bookId, long curr, long limit, String orderBy, HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }

        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            return myBookService.listAuthorBookChapter(curr, limit, bookId, orderBy, userId);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @GetMapping("generateCSV")
    public Result<?> generateCSV() {
        return myBookService.generateCSV();
    }

    @GetMapping("transferData")
    public Result<?> transferData() {
        myBookService.transferData();
        return Result.ok();
    }

    @GetMapping("insertTest")
    public Result<?> insertTest() {
        BookContentVO bookContentVO = new BookContentVO();
        bookContentVO.setIndexId(0L);
        bookContentVO.setIndexName("test");
        bookContentVO.setContent("hello world");
        myAuthorService.addBookContentByMe(bookContentVO, 0L, (byte)0);
        return Result.ok();
    }


//    @GetMapping("recover")
//    public Result<?> recoverFromES() {
//        try {
//            return myBookService.recoverFromES();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @GetMapping("recover")
//    public Result<?> recover() {
//        myBookService.recover();
//        return Result.ok();
//    }
}
