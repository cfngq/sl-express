package com.example.api.client.fallback;


import com.example.api.client.PayClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.api.domain.dto.OrderDetailDTO;
import com.example.api.domain.dto.PayApplyDTO;
import com.example.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class PayFallbackFactory implements FallbackFactory<PayClient> {
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient() {
            @Override
            public Result<String> applyPayOrder(PayApplyDTO payApplyDTO) {
                log.error("远程调用生成支付单接口失败：",cause);
                return Result.error("异常，无法生成支付单");
            }
        };
    }
}
