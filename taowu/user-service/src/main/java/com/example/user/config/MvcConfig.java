package com.example.user.config;

import com.example.user.interceptor.LoginInterceptor;
import com.example.user.interceptor.RefreshTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

//    //添加拦截器
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //登录拦截器
//        registry.addInterceptor(new LoginInterceptor())
//                .addPathPatterns("/user/me","/user/out")
//                .order(1);
//        //刷新token
//        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
//                .addPathPatterns("/**").order(0);
//    }
}
