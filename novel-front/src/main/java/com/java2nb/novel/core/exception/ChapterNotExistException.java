package com.java2nb.novel.core.exception;

public class ChapterNotExistException extends RuntimeException{
    public ChapterNotExistException(){
        super("当前章节不存在或已被删除");
    }
}
