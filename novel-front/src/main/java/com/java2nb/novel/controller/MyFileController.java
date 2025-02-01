package com.java2nb.novel.controller;

import com.java2nb.novel.core.cache.CacheService;
import com.java2nb.novel.core.exception.ResponseWriteException;
import com.java2nb.novel.core.result.LoginAndRegisterConstant;
import com.java2nb.novel.core.result.Result;
import com.java2nb.novel.core.utils.*;
import com.java2nb.novel.mapper.FrontUserMapper;
import com.java2nb.novel.service.MyFileService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.java2nb.novel.core.utils.MyRandomVerificationCodeUtil.VERIFICATION_CODE;


/**
 * @author 10253
 */
@Controller
@RequestMapping("file")
@Slf4j
public class MyFileController {
    @Resource
    private CacheService cacheService;
    @Resource
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private FrontUserMapper userMapper;
    @Value("${pic.save.path}")
    private String picSavePath;
//    @Autowired
//    private SFTPFileUploadUtil sftpFileUploadUtil;
    @Resource(name = "oss")
    private MyFileService myFileService;

    @GetMapping("getVerify")
    @SneakyThrows
    public void verify(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Pragma", "No-Cache");
        response.setHeader("Cache-Control", "No-Cache");
        response.setDateHeader("Expires", 0);

        MyRandomVerificationCodeUtil randomValidateCodeUtil = new MyRandomVerificationCodeUtil();
        String code = null;
        try {
            code = randomValidateCodeUtil.genRandCodeImage(response.getOutputStream());
        } catch (IOException e) {
            throw new ResponseWriteException(e);
        }

        cacheService.set(VERIFICATION_CODE + ":" + IpUtil.getRealIp(request), code, 30);
    }

    @ResponseBody
    @PostMapping("picUpload")
    public Result<?> picUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String token = getToken(request);
        if(!jwtTokenUtil.canRefresh(token)){
            return Result.customError(LoginAndRegisterConstant.NO_LOGIN_MSG, LoginAndRegisterConstant.NO_LOGIN);
        }

        String filepath = null;
        try {
            filepath = myFileService.savePic(file, picSavePath);
        } catch (IOException e) {
            return Result.customError("文件上传失败", 2050);
        }

        return Result.ok(filepath);

    }

//    @ResponseBody
//    @GetMapping("testFileUpload")
//    public String upload(HttpServletRequest request, HttpServletResponse response) throws JSchException {
//        boolean b = sftpFileUploadUtil.uploadFile("index/index.html", "index.html");
//        return String.valueOf(b);
//    }

    private static String getToken(HttpServletRequest request) {
        String token = CookieUtil.getCookie(request, "Authorization");
        if(token == null) {
            token = request.getHeader("Authorization");
        }
        return token;
    }

}
