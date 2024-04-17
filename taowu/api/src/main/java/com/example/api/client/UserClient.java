package com.example.api.client;

import com.example.api.client.fallback.ItemFallbackFactory;
import com.example.api.client.fallback.UserFallbackFactory;
import com.example.api.domain.dto.ItemDTO;
import com.example.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "user-service",fallbackFactory = UserFallbackFactory.class)
public interface UserClient {
    @PostMapping("/user/deduct")
    Result<String> deductMoney(@RequestParam("amount") Integer amount);
}
