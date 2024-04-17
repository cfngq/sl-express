package com.example.api.client.fallback;

import com.example.api.client.UserClient;

import com.example.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;


@Slf4j
public class UserFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public Result<String> deductMoney(Integer amount) {
                log.error("远程调用扣减用户余额接口失败：",cause);
                return Result.error("异常，无法扣减用户余额");
            }
        };
    }
}
