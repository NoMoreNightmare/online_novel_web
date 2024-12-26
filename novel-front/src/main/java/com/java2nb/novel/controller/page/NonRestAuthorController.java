package com.java2nb.novel.controller.page;

import com.java2nb.novel.core.bean.UserDetails;
import com.java2nb.novel.core.utils.CookieUtil;
import com.java2nb.novel.core.utils.JwtTokenUtil;
import com.java2nb.novel.entity.Author;
import com.java2nb.novel.service.MyAuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping("author")
public class NonRestAuthorController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MyAuthorService myAuthorService;

    @RequestMapping(value = "register.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String register(Author author, HttpServletRequest request, Model model) {
        String token = getToken(request);
        if(token != null && jwtTokenUtil.canRefresh(token)) {
            UserDetails userDetails = jwtTokenUtil.getUserDetailsFromToken(token);
            Long userId = userDetails.getId();
            if(author != null && author.getInviteCode() != null) {
                String labErr = myAuthorService.register(userId, author);
                if(labErr != null) {
                    model.addAttribute("LabErr", labErr);
                    model.addAttribute("author", author);
                }else{
                    return "redirect:/author/index.html";
                }


            }
            return "/author/register";
        }else{
            return "redirect:/user/login.html?originUrl=author/register.html";
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
