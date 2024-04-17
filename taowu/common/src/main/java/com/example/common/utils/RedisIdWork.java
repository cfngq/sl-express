package com.example.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RedisIdWork {
    //开始时间
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    //序列号位数
    private static final int COUNT_BITS =32;
    private final StringRedisTemplate stringRedisTemplate;
    //生成唯一Id
    public Long nextId(String keyPrefix){
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long epochSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeTamp = epochSecond - BEGIN_TIMESTAMP;
        //生成序列号
        //获取当天日期 yyyy:MM:dd
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        //得到的序列号自增长
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //拼接开始时间和序列号 得到唯一id
        return timeTamp << COUNT_BITS | count;
    }
}