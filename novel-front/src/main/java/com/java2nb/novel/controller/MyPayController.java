package com.java2nb.novel.controller;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 10253
 */
@RestController
@RequestMapping("pay")
@Slf4j
public class MyPayController {
    @PostMapping("alipay")
    public String pay(@RequestParam("payAmount") int payAmount, HttpServletRequest request) {
        //TODO 实现支付功能
        return "404";
    }
}
