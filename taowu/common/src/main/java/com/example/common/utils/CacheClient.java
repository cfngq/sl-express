package com.example.common.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.common.domain.redis.RedisData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.example.common.constant.RedisConstants.CACHE_NULL_TTL;
import static com.example.common.constant.RedisConstants.LOCK_SHOP_KEY;


@Component
@Slf4j
@RequiredArgsConstructor
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;
    //线程池 实现互斥锁
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    //存入可过期缓存数据 key value time TimeUnit
    public void set(String key, Object value, Long time, TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,timeUnit);
    }

    //存入逻辑过期数据 key value 逻辑过期时间
    public void setLogicalExpire(String key,Object value,Long time,TimeUnit timeUnit){
        //设置逻辑过期 封装对象和过期时间
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        //写入数据
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }

    //读的缓存穿透
    //key前缀，查询条件 id, 返回的类型 Class ,函数式查询数据库 function,过期时间
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallBack,Long time,TimeUnit timeUnit){
        //组合key
        String key = keyPrefix+id;
            //从redis中查询缓存
            String json = stringRedisTemplate.opsForValue().get(key);
            //是否命中
            if(StrUtil.isNotBlank(json)){
                //存在，返回
                return JSONUtil.toBean(json,type);
            }
            //是否为空值
            if (json != null){
                //为空值，返回错误
                return null;
            }
            //不存在，根据id从数据库中查询
            R r = dbFallBack.apply(id);
            //数据库不存在，redis写入空值，返回错误
            if (r == null){
                stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
                return null;
            }
            //存在，写入redis
            this.set(key,r,time,timeUnit);
            return r;
    }

    //读写的缓存击穿(逻辑过期与互斥锁)
    public <R,ID> R queryWithLogicalExpire(
            String keyPrefix,ID id,Class<R> type,Function<ID,R> dbFallBack,Long time,TimeUnit timeUnit){
        String key = keyPrefix + id;
        //从redis中读取数据
        String json = stringRedisTemplate.opsForValue().get(key);
        //是否命中
        if (StrUtil.isBlank(json)){
            //未命中，返回错误
            return null;
        }
        //命中，将json反序列化为对象，从ReidsData中提取Data与expireTime
        RedisData data = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) data.getData(), type);
        LocalDateTime expireTime = data.getExpireTime();
        //是否过期
        if (expireTime.isAfter(LocalDateTime.now())){
            //未过期，返回结果
            return r;
        }
        //已过期，基于互斥锁缓存重建
        //获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //是否获取成功
        if (isLock){
            //获取锁成功，通过线程池开启独立线程，实现缓存重建
            //无论是否重建成功，都应该释放锁
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    //查询数据库
                    R newR = dbFallBack.apply(id);
                    //重建缓存
                    this.setLogicalExpire(key,newR,time,timeUnit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //释放锁资源
                    unlock(lockKey);
                }
            });
        }
        //失败，返回过期数据
        return r;
    }

    //获取互斥锁
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    //解锁
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
