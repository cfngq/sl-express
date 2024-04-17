package com.example.trade.listener;

import com.example.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {
    private final IOrderService orderService;

    @RabbitListener(bindings =@QueueBinding(
            value =@Queue(name = "make.order.pay.queue",durable = "true",
            arguments = @Argument(name = "x-queue-mode",value = "lazy")),
            exchange =@Exchange(name = "pay.topic",type = ExchangeTypes.TOPIC),
            key = "pay.success"
    ))
    public void listenPayStatus(Long orderId){orderService.markOrderPaySuccess(orderId);}
}
