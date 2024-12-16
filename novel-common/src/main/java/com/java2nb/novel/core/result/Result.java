package com.java2nb.novel.core.result;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data){
        return new Result<>(ResultConstant.OK, ResultConstant.OK_MSG, data);
    }

    public static <T> Result<Void> ok(){
        return new Result<Void>(ResultConstant.OK, ResultConstant.OK_MSG, null);
    }

    public static <T> Result<Void> error(){
        return new Result<Void>(ResultConstant.ERROR, ResultConstant.ERROR_MSG, null);
    }

    public static <T> Result<Void> noPermission(){
        return new Result<>(ResultConstant.FORBIDDEN, ResultConstant.FORBIDDEN_MSG, null);
    }

    public static <T> Result<Void> customError(String msg, int code){
        return new Result<>(code, msg, null);
    }


}
