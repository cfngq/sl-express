package com.example.api.client;

import com.example.api.client.fallback.ItemFallbackFactory;
import com.example.api.client.fallback.TradeFallbackFactory;
import com.example.api.domain.dto.ItemDTO;
import com.example.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "trade-service",fallbackFactory = TradeFallbackFactory.class)
public interface TradeClient {
    @PostMapping("/order/status")
    Result<String> markOrderPaySuccess(@RequestParam("orderId") Long orderId);
}
