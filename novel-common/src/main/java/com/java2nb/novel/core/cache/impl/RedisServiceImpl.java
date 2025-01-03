package com.java2nb.novel.core.cache.impl;

import com.java2nb.novel.core.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author xxy
 */
@RequiredArgsConstructor
@Service
public class RedisServiceImpl implements CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<Object, Object> redisTemplate;

    private final long retryTime = 1L;
    private final long expireReleaseTime = 10L;


    @Resource
    private RedissonClient redissonClient;


    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);

    }

    @Override
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void setObject(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void setObject(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    @Override
    public void del(String key) {
        redisTemplate.delete(key);
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean contains(String key) {
        return redisTemplate.hasKey(key) || stringRedisTemplate.hasKey(key);
    }

    @Override
    public void expire(String key, long timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    private boolean tryLock(String key) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);
        return lock.tryLock(retryTime, expireReleaseTime, TimeUnit.SECONDS);
    }

    public void lock(String key) throws InterruptedException {
        boolean result = tryLock(key);
        while(!result){
            result = tryLock(key);
        }
    }

    public void unlock(String key) throws InterruptedException {
        RLock lock = redissonClient.getLock(key);
        if(lock.isHeldByCurrentThread()){
            lock.unlock();
        }

    }

}
