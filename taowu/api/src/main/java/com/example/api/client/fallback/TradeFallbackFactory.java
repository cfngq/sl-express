package com.example.api.client.fallback;

import com.example.api.client.ItemClient;
import com.example.api.client.TradeClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class TradeFallbackFactory implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public Result<String> markOrderPaySuccess(Long orderId) {
                log.error("远程调用修改订单支付状态接口失败：",cause);
                return Result.error("异常，无法修改订单支付状态");
            }
        };
    }
}
