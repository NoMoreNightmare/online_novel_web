package com.java2nb.novel.core.exception;

public class ResponseWriteException extends RuntimeException {
    public ResponseWriteException(Exception e) {
        super(e);
    }
}
