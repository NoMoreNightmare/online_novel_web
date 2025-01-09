package com.java2nb.novel.core.result;

public class CaffieineConstant {
    public static final int LOCAL_CACHE_SIZE = 3;
    /**
     * 本地缓存首页的各种榜单的key的前缀
     */

    public static final String INDEX_KEY = "index";
    public static final long INDEX_KEY_TTL = 30;
    public static final int INDEX_KEY_SIZE = 10;

    /**
     * 本地缓存book的key
     */
    public static final String BOOK_KEY = "book";

    public static final long BOOK_KEY_TTL = 30;

    public static final int BOOK_KEY_SIZE = 1000;
    /**
     * 本地缓存最新更新的book content的key
     */
    public static final String BOOK_CONTENT = "content";

    public static final int BOOK_CONTENT_KEY_SIZE = 1000;

    public static final long BOOK_CONTENT_KEY_TTL = 15;

}
