package com.example.api.client;

import com.example.api.client.fallback.ItemFallbackFactory;
import com.example.api.domain.dto.ItemDTO;
import com.example.api.domain.dto.OrderDetailDTO;
import com.example.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service",fallbackFactory = ItemFallbackFactory.class)
public interface ItemClient {
    @GetMapping("/item")
    Result<List<ItemDTO>> getItemByIds(@RequestParam("ids") Collection<Long> ids);
    @PostMapping("/item/deduct")
    Result<String> deductStock(@RequestBody List<OrderDetailDTO> orderDetailDTOList);
}
