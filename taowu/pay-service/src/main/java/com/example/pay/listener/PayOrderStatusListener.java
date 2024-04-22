package com.example.pay.listener;

import com.example.common.domain.message.MultiDelayMessage;
import com.example.pay.service.IPayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;


import static com.example.common.constant.DelayOrderConstants.*;

@Component
@RequiredArgsConstructor
public class PayOrderStatusListener {
    private final IPayOrderService payOrderService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name =DELAY_ORDER_QUEUE_NAME,
            durable = "true",
            arguments = @Argument(name = "x-queue-mode",value = "lazy")),
            exchange = @Exchange(name = DELAY_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = DELAY_ORDER_KEY
    ))
    public void listenerPayOrderStatus(MultiDelayMessage<Object> message){
        payOrderService.getByOrderId(message);
    }
}
