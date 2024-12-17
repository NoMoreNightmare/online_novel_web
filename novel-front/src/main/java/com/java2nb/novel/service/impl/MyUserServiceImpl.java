package com.java2nb.novel.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.log.Log;
import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.cache.impl.RedisServiceImpl;
import com.java2nb.novel.core.exception.IdGenerateException;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.IpUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.core.utils.MyRandomVerificationCodeUtil;
import com.java2nb.novel.core.utils.SnowflakeIdGenerator;
import com.java2nb.novel.entity.User;
import com.java2nb.novel.mapper.UserDynamicSqlSupport;
import com.java2nb.novel.mapper.UserMapper;
import com.java2nb.novel.service.MyUserService;
import com.java2nb.novel.service.UserService;
import io.github.xxyopen.web.exception.BusinessException;
import org.mybatis.dynamic.sql.render.RenderingStrategy;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.java2nb.novel.mapper.UserDynamicSqlSupport.*;
import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.select.SelectDSL.select;


@Service
public class MyUserServiceImpl implements MyUserService {

    @Resource
    UserMapper userMapper;
    @Resource
    CacheService cacheService;

    @Resource
    SnowflakeIdGenerator idGenerator;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;


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


}
