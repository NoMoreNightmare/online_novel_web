package com.java2nb.novel.controller.page;

import com.java2nb.novel.core.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("user")
@Slf4j
public class NonRestUserController {


    @RequestMapping("login.html")
    public String login() {
        return ThreadLocalUtil.getTemplateDir() + "user/login";
    }


    @RequestMapping("register.html")
    public String register() {
        return ThreadLocalUtil.getTemplateDir() + "user/register";
    }


    @RequestMapping("userinfo.html")
    public String userinfo() {
        return ThreadLocalUtil.getTemplateDir() + "user/userinfo";
    }



    @RequestMapping("favorites.html")
    public String favorites() {
        return ThreadLocalUtil.getTemplateDir() + "user/favorites";
    }


    @RequestMapping("read_history.html")
    public String readHistory() {
        return ThreadLocalUtil.getTemplateDir() + "user/read_history";
    }
}
