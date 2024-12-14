package com.java2nb.novel.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("file")
@Slf4j
public class MyFileController {
    @GetMapping("getVerify")
    @SneakyThrows
    public String verify(HttpServletRequest request, HttpServletResponse response) {
        request.getParameter()
    }
}
