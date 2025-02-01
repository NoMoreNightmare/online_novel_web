package com.java2nb.novel.core.exception;

public class SerializeException extends RuntimeException {
    public SerializeException(Exception e) {
        super(e);
    }
}
