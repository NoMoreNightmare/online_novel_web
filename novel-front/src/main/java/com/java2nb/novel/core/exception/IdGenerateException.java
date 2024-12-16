package com.java2nb.novel.core.exception;

/**
 * @author 10253
 */
public class IdGenerateException extends RuntimeException {
    private static final String MESSAGE = "生成id时出现异常";
    public IdGenerateException() {
        super(MESSAGE);
    }

}
