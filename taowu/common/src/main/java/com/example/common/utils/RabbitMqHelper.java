package com.example.common.utils;

import cn.hutool.core.lang.UUID;
import com.example.common.domain.message.MultiDelayMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static com.example.common.constant.DelayOrderConstants.DELAY_ORDER_TIME;

@Slf4j
@RequiredArgsConstructor
public class RabbitMqHelper {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange, String routingKey, Object msg){
        log.debug("准备发送消息，exchange:{}, routingKey:{}, msg:{}", exchange, routingKey, msg);
        rabbitTemplate.convertAndSend(exchange, routingKey, msg);
    }

    //延迟时间确定的消息发送
    public void sendDelayMessage(String exchange, String routingKey, Object msg){
        MultiDelayMessage<Object> message = MultiDelayMessage.of(msg, DELAY_ORDER_TIME);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
    //发送者确认
    public void sendMessageWithConfirm(String exchange, String routingKey, Object msg, int maxRetries){
        log.debug("准备发送消息，exchange:{}, routingKey:{}, msg:{}", exchange, routingKey, msg);
        // 消息唯一id
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString(true));
        //添加回调函数
        cd.getFuture().addCallback(new ListenableFutureCallback<>() {
            //重试次数
            int retryCount;
            //失败返回
            @Override
            public void onFailure(Throwable ex) {
                log.error("处理ack回执失败", ex);
            }
            @Override
            //成功返回
            public void onSuccess(CorrelationData.Confirm result) {
                if (result != null && !result.isAck()) {
                    log.debug("消息发送失败，收到nack，已重试次数：{}", retryCount);
                    if(retryCount >= maxRetries){
                        log.error("消息发送重试次数耗尽，发送失败");
                        return;
                    }
                    CorrelationData cd = new CorrelationData(UUID.randomUUID().toString(true));
                    cd.getFuture().addCallback(this);
                    rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
                    retryCount++;
                }
            }
        });
        rabbitTemplate.convertAndSend(exchange, routingKey, msg, cd);
    }
}