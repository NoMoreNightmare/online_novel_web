package com.java2nb.novel.core.result;

public class RedisConstant {
    public static final Long MIN_TTL = 600L;

    public static final Long INDEX_MAX_TTL = 1800L;

    public static final Long BOOK_MAX_TTL = 1200L;

    public static final Long BOOK_CONTENT_MAX_TTL = 900L;
    public static final long FIRST_RANK = 0;
    public static final long LAST_RANK = 49;
    public static final String BOOK_INDEX_KEY = "novel:bookIndex:";
    public static final String BOOK_KEY = "novel:book:";
}
