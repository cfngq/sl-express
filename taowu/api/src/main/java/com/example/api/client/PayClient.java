package com.example.api.client;

import com.example.api.client.fallback.PayFallbackFactory;
import com.example.api.domain.dto.PayApplyDTO;
import com.example.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "pay-service",fallbackFactory = PayFallbackFactory.class)
public interface PayClient {
    @PostMapping("/pay-order")
    Result<String> applyPayOrder(@RequestBody PayApplyDTO payApplyDTO);
}
