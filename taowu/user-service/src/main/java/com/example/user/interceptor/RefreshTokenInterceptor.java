package com.example.user.interceptor;

import cn.hutool.core.util.StrUtil;
import com.example.common.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.concurrent.TimeUnit;

import static com.example.common.constant.RedisConstants.LOGIN_USER_KEY;
import static com.example.common.constant.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的token
        String token = request.getHeader("authorization");
        //token为空直接放行
        if (StrUtil.isBlank(token)){
            return true;
        }
        //基于token获取redis中的用户
        String tokenKey = LOGIN_USER_KEY + token;
        String s = stringRedisTemplate.opsForValue().get(tokenKey);
        //用户是否存在
        if (s == null){
            return true;
        }
        //保存至ThreadLocal
        UserHolder.saveUserId(Long.valueOf(s));
        //刷新token有效期
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
        //放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUserId();
    }
}
