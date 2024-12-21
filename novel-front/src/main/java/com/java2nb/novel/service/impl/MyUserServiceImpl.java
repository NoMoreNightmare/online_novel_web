package com.java2nb.novel.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.java2nb.novel.controller.page.PageBean;
import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.exception.IdGenerateException;
import com.java2nb.novel.core.result.BookConstant;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.core.utils.MyRandomVerificationCodeUtil;
import com.java2nb.novel.core.utils.SnowflakeIdGenerator;
import com.java2nb.novel.entity.User;
import com.java2nb.novel.entity.UserBookshelf;
import com.java2nb.novel.entity.UserFeedback;
import com.java2nb.novel.mapper.*;
import com.java2nb.novel.service.MyUserService;
import com.java2nb.novel.vo.BookCommentVO;
import com.java2nb.novel.vo.BookReadHistoryVO;
import com.java2nb.novel.vo.BookShelfVO;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.java2nb.novel.mapper.UserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.*;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;


@Service
public class MyUserServiceImpl implements MyUserService {

    @Autowired
    FrontUserMapper userMapper;
    @Resource
    UserBookshelfMapper userBookshelfMapper;
    @Resource
    CacheService cacheService;

    @Resource
    SnowflakeIdGenerator idGenerator;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserBuyRecordMapper userBuyRecordMapper;
    @Autowired
    private FrontUserReadHistoryMapper userReadHistoryMapper;
    @Autowired
    private FrontBookCommentMapper bookCommentMapper;
    @Autowired
    private UserFeedbackMapper userFeedbackMapper;


    @Override
    public Result<?> register(User user, String velCode, String ip) {
        String actualVelCode = cacheService.get(MyRandomVerificationCodeUtil.VERIFICATION_CODE + ":" + ip);
        if(!velCode.equals(actualVelCode)){
            return Result.customError(LoginAndRegisterConstant.VEL_CODE_ERROR_MSG, LoginAndRegisterConstant.VEL_CODE_ERROR);
        }

        int existUsers = userMapper.selectExistUserNumber(user.getUsername());
        if(existUsers > 0){
//            throw new RuntimeException(LoginAndRegisterConstant.USERNAME_EXIST_MSG);
            return Result.customError(LoginAndRegisterConstant.USERNAME_EXIST_MSG, LoginAndRegisterConstant.USERNAME_EXIST);
        }

        user.setNickName(user.getUsername());

        try {
            user.setId(idGenerator.generateId());
        } catch (InterruptedException e) {
            throw new IdGenerateException();
        }
        Date currentTime = new Date();
        user.setCreateTime(currentTime);
        user.setUpdateTime(currentTime);
        user.setPassword(BCrypt.hashpw(user.getPassword()));

        userMapper.insertSelective(user);

        UserDetails userDetails = new UserDetails();
        userDetails.setId(user.getId());
        userDetails.setUsername(user.getUsername());
        userDetails.setNickName(user.getNickName());

        Map<String, Object> data = new HashMap<>(1);
        data.put("token", jwtTokenUtil.generateToken(userDetails));

        return Result.ok(data);

    }

    @Override
    public Result<?> login(User user) {
        SelectStatementProvider select = select(id, username, nickName, password)
                .from(UserDynamicSqlSupport.user)
                .where(username, isEqualTo(user.getUsername()))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        Optional<User> optionalUser = userMapper.selectOne(select);



        if(!optionalUser.isPresent()){
            return Result.customError(LoginAndRegisterConstant.USERNAME_PASS_ERROR_MSG, LoginAndRegisterConstant.USERNAME_PASS_ERROR);
        }

        User userInfo = optionalUser.get();

        if(!BCrypt.checkpw(user.getPassword(), userInfo.getPassword())){
            return Result.customError(LoginAndRegisterConstant.USERNAME_PASS_ERROR_MSG, LoginAndRegisterConstant.USERNAME_PASS_ERROR);
        }

        UserDetails userDetails = new UserDetails();
        userDetails.setId(userInfo.getId());
        userDetails.setUsername(userInfo.getUsername());
        userDetails.setNickName(userInfo.getNickName());

        String token = jwtTokenUtil.generateToken(userDetails);

        Map<String, Object> data = new HashMap<>(1);
        data.put("token", token);

        return Result.ok(data);
    }

    @Override
    public Result<?> refreshToken(String token) {
        if(token != null && jwtTokenUtil.canRefresh(token)){
            String refreshToken = jwtTokenUtil.refreshToken(token);
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Map<String, Object> data = new HashMap<>(2);
            data.put("token", refreshToken);
            data.put("nickName", userDetails.getNickName());
            data.put("username", userDetails.getUsername());
            return Result.ok(data);
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }


    }

    @Override
    public Result<?> queryIsInShelf(Long bookId, Long userId) {
        SelectStatementProvider checkBookInShelf = select(count(id))
                .from(UserBookshelfDynamicSqlSupport.userBookshelf)
                .where(UserBookshelfDynamicSqlSupport.bookId, isEqualTo(bookId))
                .and(UserBookshelfDynamicSqlSupport.userId, isEqualTo(userId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        long count = userBookshelfMapper.count(checkBookInShelf);

        if(count == 0){
            return Result.ok(false);
        }else{
            return Result.ok(true);
        }

    }

    @Override
    public Result<?> addToBookShelf(Long bookId, long preContentId, Long userId) {
        if(!(Boolean)queryIsInShelf(bookId, userId).getData()){
            UserBookshelf userBookshelf = new UserBookshelf();
            userBookshelf.setBookId(bookId);
            userBookshelf.setUserId(userId);
            userBookshelf.setPreContentId(preContentId);

            int success = userBookshelfMapper.insert(userBookshelf);

            if(success == 1){
                return Result.ok();
            }else{
                return Result.customError(BookConstant.ADD_TO_SHELF_MSG, BookConstant.ADD_TO_SHELF);
            }
        }

        return Result.ok();

    }

    @Override
    public long queryUserBuyRecord(long userId, long bookIndexId) {
        SelectStatementProvider select = select(count(UserBuyRecordDynamicSqlSupport.id))
                .from(UserBuyRecordDynamicSqlSupport.userBuyRecord)
                .where(UserBuyRecordDynamicSqlSupport.userId, isEqualTo(userId))
                .and(UserBuyRecordDynamicSqlSupport.bookIndexId, isEqualTo(bookIndexId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        return userBuyRecordMapper.count(select);
    }

    @Override
    public boolean queryIsBookRead(Long bookId, Long userId) {
        SelectStatementProvider select = select(count(UserReadHistoryDynamicSqlSupport.id))
                .from(UserReadHistoryDynamicSqlSupport.userReadHistory)
                .where(UserReadHistoryDynamicSqlSupport.userId, isEqualTo(userId))
                .and(UserReadHistoryDynamicSqlSupport.bookId, isEqualTo(bookId))
                .build()
                .render(RenderingStrategy.MYBATIS3);

        long count = userReadHistoryMapper.count(select);
        return count != 0;
    }

    @Override
    public void addReadHistory(Long bookId, Long userId, Long preContentId) {
        userReadHistoryMapper.insertNewHistory(userId, bookId, preContentId);
    }

    @Override
    public void updateReadHistory(Long bookId, Long userId, Long preContentId) {
        userReadHistoryMapper.updateBookHistory(userId, bookId, preContentId);
    }

    @Override
    public Result<?> listCommentByPage(Long userId, Long curr, Long limit) {
        PageBean<BookCommentVO> pageBean = new PageBean<>(curr, limit);

        List<BookCommentVO> bookCommentVOS = bookCommentMapper.listCommentByPageOnlyUseUserIdWithContent(userId, (curr - 1) * limit, limit);

        long total = bookCommentMapper.countBookCommentByUserId(userId);

        pageBean.setList(bookCommentVOS);
        pageBean.setTotal(total);

        return Result.ok(pageBean);
    }

    @Override
    public Result<?> getUserInfo(Long userId) {
        SelectStatementProvider select = select(userPhoto, nickName, username, accountBalance, userSex)
                .from(user)
                .where(id, isEqualTo(userId))
                .build()
                .render(RenderingStrategy.MYBATIS3);
        Optional<User> optionalUser = userMapper.selectOne(select);
        if(optionalUser.isPresent()){
            return Result.ok(optionalUser.get());
        }else{
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }
    }

    @Override
    public Result<?> listBookShelfByPage(Long userId, Long limit) {
        PageBean<BookShelfVO> pageBean = new PageBean<>(1, limit);
        List<BookShelfVO> list = userMapper.selectBookShelfList(userId, 0, limit);

        pageBean.setList(list);
        return Result.ok(pageBean);
    }

    @Override
    public Result<?> listReadHistoryByPage(Long userId, long curr, Long limit) {
        PageBean<BookReadHistoryVO> pageBean = new PageBean<>(curr, limit);

        long total = userReadHistoryMapper.countTotalHistory(userId);
        pageBean.setTotal(total);
        List<BookReadHistoryVO> bookReadHistoryVOS = userReadHistoryMapper.listReadHistory(userId, (curr - 1) * limit, limit);
        pageBean.setList(bookReadHistoryVOS);
        return Result.ok(pageBean);
    }

    @Override
    public Result<?> listUserFeedbackByPage(Long userId, Long curr, Long limit) {
        PageBean<UserFeedback> pageBean = new PageBean<>(curr, limit);

        long total = userFeedbackMapper.countTotalFeedback(userId);
        pageBean.setTotal(total);
        List<UserFeedback> userFeedbackList = userFeedbackMapper.listUserFeedbackByPage(userId, (curr - 1) * limit, limit);
        pageBean.setList(userFeedbackList);
        return Result.ok(pageBean);
    }

    @Override
    public void updateUserSex(Long userId, byte userSex) {
        userMapper.updateUserSex(userId, userSex);
    }

    @Override
    public int updatePassword(Long userId, String oldPassword, String newPassword) {
        newPassword = BCrypt.hashpw(newPassword);
        return userMapper.updatePassword(userId, newPassword);
    }

    @Override
    public int updateNickName(Long userId, String nickName) {
        return userMapper.updateNickName(userId, nickName);
    }

    @Override
    public void updateUserPhoto(Long userId, String userPhoto) {
        userMapper.updateUserPhoto(userId, userPhoto);
    }

    @Override
    public String queryUserPhoto(Long userId) {
        return userMapper.queryUserPhoto(userId);
    }


}
