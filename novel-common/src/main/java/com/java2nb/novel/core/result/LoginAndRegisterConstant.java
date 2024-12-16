package com.java2nb.novel.core.result;

public class LoginAndRegisterConstant {
    public static final int NO_LOGIN = 1001;
    public static final int VEL_CODE_ERROR = 1002;
    public static final int USERNAME_EXIST = 1003;
    public static final int USERNAME_PASS_ERROR = 1004;
    public static final int TWO_PASSWORD_DIFF = 1005;
    public static final int OLD_PASSWORD_ERROR = 1006;

    public static final String NO_LOGIN_MSG = "未登录或登录失效";
    public static final String VEL_CODE_ERROR_MSG = "验证码错误";
    public static final String USERNAME_EXIST_MSG = "手机号已注册";
    public static final String USERNAME_PASS_ERROR_MSG = "手机号或密码错误";
    public static final String TWO_PASS_DIFF_MSG = "两次输入的密码不匹配";
    public static final String OLD_PASS_ERROR_MSG = "旧密码不匹配";

}
