package com.java2nb.novel.core.utils;

import com.java2nb.novel.core.cache.CacheKey;
import com.java2nb.novel.core.cache.impl.RedisServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 10253
 */
@Component
public class SnowflakeIdGenerator {
    @Value("${machine-id}")
    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // 起始的时间戳
    // 2021-01-01 00:00:00
    private final static long START_TIMESTAMP = 1609459200000L;


    private final static long WORKER_ID_BIT = 5L;
    private final static long TIMESTAMP_BIT = 41L;
    private final static long SEQUENCE_BIT = 12L;

    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    @Resource
    private RedisServiceImpl redisService;

    /**
     * 使用雪花算法来生成唯一的用户id（time+机器id+序列号）
     * @throws InterruptedException
     */
    public long generateId() throws InterruptedException {
        redisService.lock(CacheKey.REDISSON_ID_GENERATOR);
        //时间戳
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            redisService.unlock(CacheKey.REDISSON_ID_GENERATOR);
            throw new RuntimeException("出现时钟回拨的情况，无法生成id");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
        } else {
            sequence = 0L;
        }
        //机器id
        //sequence
        lastTimestamp = timestamp;

        long id = ((timestamp - START_TIMESTAMP) << (SEQUENCE_BIT + WORKER_ID_BIT))
                | (workerId << WORKER_ID_BIT)
                | sequence;

        redisService.unlock(CacheKey.REDISSON_ID_GENERATOR);

        return id;

    }
}
