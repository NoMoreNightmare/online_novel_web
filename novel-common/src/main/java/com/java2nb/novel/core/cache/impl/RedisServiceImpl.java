package com.java2nb.novel.core.cache.impl;

import com.java2nb.novel.core.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author xxy
 */
@RequiredArgsConstructor
@Service
public class RedisServiceImpl implements CacheService {
    @Value("${machine-id}")
    private long machineId;

    private final long initialTimeStamp = 1577836800000L;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate redisTemplate;


    private final long retryTime = 1L;
    private final long expireReleaseTime = 10L;


    @Resource
    private RedissonClient redissonClient;

//
//    public void setBookContentQueueName(String bookContentQueueName) {
//        this.bookContentQueueName = bookContentQueueName;
//    }


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

//    @Override
//    public Object hmGet(String key, String field) {
//        return redisTemplate.opsForHash().get(key, field);
//    }

//    @Override
//    public void hmSet(String key, String field, Object value) {
//        redisTemplate.opsForHash().put(key, field, value);
//    }
//
//    @Override
//    public Long incrHmKeyFieldByOne(String key, String field){
//        return redisTemplate.opsForHash().increment(key, field, 1);
//    }

//    @Override
//    public Map<Object, Object> hmGetAll(String key) {
//        return (Map<Object, Object>) redisTemplate.opsForHash().entries(key);
//    }

    public double incrZetByOne(String key, String value){
        return zsetIncrBy(key, value, 1);
    }

    public double zsetIncrBy(String key, String value, int score){

        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    public Set<String> zsetRankBy(String key, long start, long end){
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Set<ZSetOperations.TypedTuple<String>> zetGetAll(String key){
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, 0, -1);
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

    @Override
    public long getMQUUID(String mqKey) throws InterruptedException {
        //TODO 生成UUID

        long id = 0;
        long timestamp = System.currentTimeMillis() - initialTimeStamp;
        timestamp = timestamp << 22;

        machineId = machineId << 12;

        String completeKey = mqKey + ":" + timestamp + ":" + machineId;
        lock(mqKey);
        long currentSequenceId;
        String strSequenceId = stringRedisTemplate.opsForValue().get(completeKey);
        if(strSequenceId != null) {
            currentSequenceId = Long.parseLong(strSequenceId) + 1;
            set(completeKey, String.valueOf(currentSequenceId), 10);
        }else{
            currentSequenceId = 0;
            set(completeKey, String.valueOf(currentSequenceId), 10);
        }

        long fullId = (id | timestamp | machineId | currentSequenceId);

        unlock(mqKey);

        return fullId;
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
