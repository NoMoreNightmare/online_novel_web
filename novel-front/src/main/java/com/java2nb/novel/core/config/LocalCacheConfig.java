package com.java2nb.novel.core.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.java2nb.novel.core.result.CaffieineConstant.*;

@Configuration
public class LocalCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<CaffeineCache>(LOCAL_CACHE_SIZE);

        //index的caffeine
        Caffeine<Object, Object> indexCaffeine = Caffeine.newBuilder().recordStats().maximumSize(INDEX_KEY_SIZE);
        indexCaffeine.expireAfterWrite(Duration.ofMinutes(INDEX_KEY_TTL));
        caches.add(new CaffeineCache(INDEX_KEY, indexCaffeine.build()));

        //book的caffeine
        Caffeine<Object, Object> bookCaffeine = Caffeine.newBuilder().recordStats().maximumSize(BOOK_KEY_SIZE);
        bookCaffeine.expireAfterWrite(Duration.ofMinutes(BOOK_KEY_TTL));
        caches.add(new CaffeineCache(INDEX_KEY, bookCaffeine.build()));

        //book content的caffeine
        Caffeine<Object, Object> contentCaffeine = Caffeine.newBuilder().recordStats().maximumSize(BOOK_CONTENT_KEY_SIZE);
        contentCaffeine.expireAfterWrite(Duration.ofMinutes(BOOK_CONTENT_KEY_TTL));
        caches.add(new CaffeineCache(BOOK_CONTENT, contentCaffeine.build()));

        cacheManager.setCaches(caches);

        return cacheManager;
    }
}
