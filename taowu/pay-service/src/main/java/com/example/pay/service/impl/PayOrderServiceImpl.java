package com.example.pay.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.api.client.UserClient;
import com.example.common.enums.PayStatus;
import com.example.common.exception.BadApplyPayOrderException;
import com.example.common.result.Result;
import com.example.common.utils.RedisIdWork;
import com.example.common.utils.UserHolder;
import com.example.pay.domain.dto.PayApplyDTO;
import com.example.pay.domain.dto.PayOrderFormDTO;
import com.example.pay.domain.po.PayOrder;
import com.example.pay.mapper.PayOrderMapper;
import com.example.pay.service.IPayOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 支付订单 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {
    private final RedisIdWork redisIdWork;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    //生成支付单
    @Override
    public Result<String> applyPayOrder(PayApplyDTO payApplyDTO) {
        //幂等性校验
        PayOrder payOrder = checkIdempotent(payApplyDTO);
        //返回
        return Result.success("支付单号："+payOrder.getId());
    }

    //支付
    @Override
    @GlobalTransactional
    public Result<String> tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO) {
        //查询支付单
        PayOrder payOrder = getById(payOrderFormDTO.getId());
        if (payOrder == null){
            return Result.error("该支付单不存在");
        }
        //判断是否支付
        if (!PayStatus.WAIT_BUYER_PAY.equalsValue(payOrder.getStatus())){
            //订单不为待支付，状态异常
            throw new RuntimeException("订单状态异常，无法支付");
        }
        //远程调用，尝试扣减余额
        userClient.deductMoney(payOrder.getAmount());
        //修改支付单状态
        boolean success = markPayOrderSuccess(payOrderFormDTO.getId(), LocalDateTime.now());
        if (!success){
            //修改支付单状态失败，交易已完成或支付
            throw new RuntimeException("修改支付单状态失败，交易已完成或支付");
        }
//        //远程调用，修改订单状态
//        tradeClient.markOrderPaySuccess(payOrder.getBizOrderNo());
        //todo mq发送消息，异步，失败也不会回滚,延迟消息查询是否成功
        try {
            rabbitTemplate.convertAndSend("pay.topic","pay.success",payOrder.getBizOrderNo());
        } catch (AmqpException e) {
            log.error("修改订单状态为支付成功的消息发送失败，支付单：{},交易单：{}",payOrder.getId(),payOrder.getBizOrderNo(),e);
        }
        return Result.success("支付成功");
    }

    private boolean markPayOrderSuccess(Long id, LocalDateTime now) {
        return lambdaUpdate()
                .set(PayOrder::getStatus,PayStatus.TRADE_SUCCESS.getValue())
                .set(PayOrder::getPaySuccessTime,now)
                .eq(PayOrder::getId,id)
                //幂等判断
                .in(PayOrder::getStatus,PayStatus.NOT_COMMIT.getValue(),PayStatus.WAIT_BUYER_PAY.getValue())
                .update();
    }

    private PayOrder checkIdempotent(PayApplyDTO payApplyDTO) {
        //查询支付单
        PayOrder olderOrder = lambdaQuery()
                .eq(PayOrder::getBizOrderNo, payApplyDTO.getBizOrderNo())
                .one();
        //是否存在
        if (olderOrder == null){
            //不存在，写入并返回支付单
            PayOrder payOrder = buildPayOrder(payApplyDTO);
            payOrder.setPayOrderNo(redisIdWork.nextId("payOrderId"));
            save(payOrder);
            return payOrder;
        }
        //存在旧单，判断其是否支付
        if (PayStatus.TRADE_SUCCESS.equalsValue(olderOrder.getStatus())){
            //已支付 抛出异常
            throw new BadApplyPayOrderException("该订单已支付");
        }
        //是否关闭
        if (PayStatus.TRADE_CLOSED.equalsValue(olderOrder.getStatus())){
            //已关闭，抛异常
            throw new BadApplyPayOrderException("该订单已关闭");
        }
        //支付渠道是否一致
        if (!StrUtil.equals(olderOrder.getPayChannelCode(),payApplyDTO.getPayChannelCode())){
            //支付渠道不同，重置订单，重新提交
            PayOrder payOrder = buildPayOrder(payApplyDTO);
            payOrder.setId(olderOrder.getId());
            payOrder.setQrCodeUrl("");
            payOrder.setPayOrderNo(olderOrder.getPayOrderNo());
            updateById(payOrder);
        }
        //以上均非，则旧单未支付或未提交，且支付渠道一样，返回旧数据
        return olderOrder;
    }

    private PayOrder buildPayOrder(PayApplyDTO payApplyDTO) {
        //得到支付数据
        PayOrder payOrder = BeanUtil.toBean(payApplyDTO, PayOrder.class);
        //封装其他属性
        payOrder.setPayOverTime(LocalDateTime.now());
        payOrder.setStatus(PayStatus.WAIT_BUYER_PAY.getValue());
        payOrder.setBizUserId(UserHolder.getUserId());
        return payOrder;
    }
}
