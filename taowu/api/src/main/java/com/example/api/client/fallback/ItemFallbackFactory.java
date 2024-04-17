package com.example.api.client.fallback;

import com.example.api.client.ItemClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.api.domain.dto.OrderDetailDTO;
import com.example.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemFallbackFactory implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public Result<List<ItemDTO>> getItemByIds(Collection<Long> ids) {
                log.error("远程调用查询商品接口失败：",cause);
                return Result.error("异常，无法查找到商品数据");
            }

            @Override
            public Result<String> deductStock(List<OrderDetailDTO> orderDetailDTOList) {
                log.error("远程调用扣减商品库存接口失败：",cause);
                return Result.error("异常，无法扣减商品库存");
            }
        };
    }
}
