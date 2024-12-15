package com.java2nb.novel.controller;

import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.utils.MyRandomVerificationCodeUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

import static com.java2nb.novel.core.utils.MyRandomVerificationCodeUtil.VERIFICATION_CODE;


@Controller
@RequestMapping("file")
@Slf4j
public class MyFileController {
    @Autowired
    private CacheService cacheService;


    @GetMapping("getVerify")
    @SneakyThrows
    public void verify(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Pragma", "No-Cache");
        response.setHeader("Cache-Control", "No-Cache");
        response.setDateHeader("Expires", 0);

        MyRandomVerificationCodeUtil randomValidateCodeUtil = new MyRandomVerificationCodeUtil();
        String code = randomValidateCodeUtil.genRandCodeImage(response.getOutputStream());

        UUID uuid = UUID.randomUUID();
        cacheService.set(VERIFICATION_CODE + ":" + uuid, code);
    }
}
