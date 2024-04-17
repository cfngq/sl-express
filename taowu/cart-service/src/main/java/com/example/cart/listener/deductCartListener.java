package com.example.cart.listener;

import com.example.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class deductCartListener {
    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "deduct.cart.queue",
            durable = "true",
            arguments = @Argument(name = "x-queue-mode",value = "lazy")),
            exchange = @Exchange(name = "cart.topic",type = ExchangeTypes.TOPIC),
            key = "deduct.cart"
    ))
    public void listenDeductCart(Collection<Long> itemIds){
        cartService.removeByIds(itemIds);
    }
}
