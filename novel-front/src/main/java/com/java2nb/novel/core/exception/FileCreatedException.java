package com.java2nb.novel.core.exception;

import java.lang.reflect.Field;

public class FileCreatedException extends RuntimeException {
    public FileCreatedException(Exception e) {
        super(e);
    }

    public FileCreatedException() {
        super();
    }
}
