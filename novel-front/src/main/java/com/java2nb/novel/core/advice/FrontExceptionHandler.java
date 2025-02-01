package com.java2nb.novel.core.advice;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSException;
import com.java2nb.novel.core.exception.*;
import com.java2nb.novel.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

@RestControllerAdvice
@Slf4j
public class FrontExceptionHandler {
    @ExceptionHandler(ExecutionException.class)
    public Result<?> handleExecutionException(ExecutionException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        return Result.customError("CompletableFuture异步查询数据时出错", 3020);
    }

    @ExceptionHandler(ResponseWriteException.class)
    public Result<?> handleResponseWriteException(ResponseWriteException e, HttpServletRequest request) {
        log.error(e.getCause().getMessage(), e.getCause());
        return Result.customError("Response写入时出现异常", 3021);
    }

    @ExceptionHandler(DeSerializeException.class)
    public Result<?> handleDeSerializeException(DeSerializeException e, HttpServletRequest request) {
        log.error(e.getCause().getMessage(), e.getCause());
        return Result.customError("反序列化错误", 2021);
    }

    @ExceptionHandler(SerializeException.class)
    public Result<?> handleSerializeException(SerializeException e, HttpServletRequest request) {
        log.error(e.getCause().getMessage(), e.getCause());
        return Result.customError("序列化错误", 2020);
    }

    @ExceptionHandler(ResponseHandleException.class)
    public Result<?> handleResponseHandleException(ResponseHandleException e, HttpServletRequest request) {
        log.error(e.getCause().getMessage(), e.getCause());
        return Result.customError("response处理时异常", 3022);
    }

    @ExceptionHandler({OSSException.class, ClientException.class})
    public Result<?> handleOSSException(Exception e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        return Result.customError("oss操作出现异常", 5050);
    }

    @ExceptionHandler(IdGenerateException.class)
    public Result<?> handleIdGenerateException(IdGenerateException e, HttpServletRequest request) {
        log.error(e.getCause().getMessage(), e.getCause());
        return Result.customError("用户id生成异常", 1010);
    }


}
