package com.example.item.listener;


import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.service.IItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemNumListener {
    private final IItemService itemService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "item.num.queue",
            durable = "true",
            arguments = @Argument(name = "x-queue-mode",value = "lazy")),
            exchange = @Exchange(name = "item.topic",type = ExchangeTypes.TOPIC),
            key = "item.num"
    ))
    public void listenerItemNum(List<OrderDetailDTO> orderDetailDTOList){
        itemService.updateItemNum(orderDetailDTOList);
    }
}
