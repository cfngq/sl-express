package com.example.pay.service;

import com.example.common.result.Result;
import com.example.pay.domain.dto.PayApplyDTO;
import com.example.pay.domain.dto.PayOrderFormDTO;
import com.example.pay.domain.po.PayOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 支付订单 服务类
 * </p>
 *
 * @author author
 * @since 2024-04-15
 */
public interface IPayOrderService extends IService<PayOrder> {

    Result<String> applyPayOrder(PayApplyDTO payApplyDTO);

    Result<String> tryPayOrderByBalance(PayOrderFormDTO payOrderFormDTO);
}
