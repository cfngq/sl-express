package com.example.gateway.filters;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.utils.CacheClient;
import com.example.gateway.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.common.constant.RedisConstants.LOGIN_USER_KEY;
import static com.example.common.constant.RedisConstants.LOGIN_USER_TTL;

@Component
@Slf4j
@RequiredArgsConstructor
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final CacheClient cacheClient;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.编写过滤器逻辑
        //得到request请求体
        ServerHttpRequest request = exchange.getRequest();
        //判断是否不需要拦截
        if(isExclude(request.getPath().toString())){
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
        //得到请求头
        String token=null;
        List<String> headers = request.getHeaders().get("authorization");
        if (CollUtil.isNotEmpty(headers)){
            token=headers.get(0);
        }
        //校验解析token
        String tokenKey = LOGIN_USER_KEY + token;
        String userId;
        try {
             userId = cacheClient.get(tokenKey);
            //刷新token有效期
            cacheClient.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
        }
        catch (Exception e){
            //无用户信息，拦截
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            return response.setComplete();
        }
        //传递用户信息给微服务
        String userInfo = StrUtil.toString(userId);
        //mutate 对下游请求做更改(添加用户信息请求头供微服务使用）
        ServerWebExchange swe = exchange.mutate().request(builder -> builder.header("user-info", userInfo))
                .build();
        //放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String pathPattern: authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        //过滤器执行顺序，需小于21亿
        return 0;
    }
}
