package com.java2nb.novel.service.impl;

import com.java2nb.novel.entity.Book;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.java2nb.novel.core.result.CaffieineConstant.*;

/**
 * @author 10253
 */
@Service
public class BookCacheServiceImpl {
    @Cacheable(cacheNames = INDEX_KEY, key = "#key", unless = "#result == null")
    public String getIndexByKey(String key) {
        return null;
    }

    @CachePut(cacheNames = INDEX_KEY, key = "#key")
    public String putIndexByKey(String key, String value) {
        return value;
    }

    @Cacheable(cacheNames = BOOK_KEY, key = "#key", unless = "#result == null")
    public String getBookByKey(String key) {
        return null;
    }

    @CachePut(cacheNames = BOOK_KEY, key = "#key")
    public String putBookByKey(String key, String value) {
        return value;
    }

    @Cacheable(cacheNames = BOOK_CONTENT, key = "#key", unless = "#result == null")
    public String getBookContentByKey(String key) {
        return null;
    }

    @CachePut(cacheNames = BOOK_CONTENT, key = "#key")
    public String putBookContentByKey(String key, String value) {
        return value;
    }


    @CacheEvict(cacheNames = BOOK_CONTENT, key = "#key")
    public void delBookContentByKey(String key) {

    }
}
