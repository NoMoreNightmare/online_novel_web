package com.java2nb.novel.core.exception;

public class FileCreatedException extends RuntimeException {
    public FileCreatedException() {
        super("文件创建失败");
    }
}
