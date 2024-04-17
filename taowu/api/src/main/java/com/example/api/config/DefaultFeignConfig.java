package com.example.api.config;


import com.example.api.client.fallback.ItemFallbackFactory;
import com.example.api.client.fallback.PayFallbackFactory;
import com.example.api.client.fallback.TradeFallbackFactory;
import com.example.api.client.fallback.UserFallbackFactory;
import com.example.common.utils.UserHolder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
//feign的默认配置
public class DefaultFeignConfig {
    //日志配置
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    //fallback配置
    @Bean
    public ItemFallbackFactory itemFallbackFactory(){
        return new ItemFallbackFactory();
    }

    @Bean
    public UserFallbackFactory userFallbackFactory(){
        return new UserFallbackFactory();
    }
    @Bean
    public TradeFallbackFactory tradeFallbackFactory(){
        return new TradeFallbackFactory();
    }
    @Bean
    public PayFallbackFactory payFallbackFactory(){
        return new PayFallbackFactory();
    }

    //feign之间传递用户
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return template -> {
            // 获取登录用户
            Long userId = UserHolder.getUserId();
            if(userId == null) {
                // 如果为空则直接跳过
                return;
            }
            // 如果不为空则放入请求头中，传递给下游微服务
            template.header("user-info", userId.toString());
        };
    }
}
