package com.example.trade.service;

import com.example.common.result.Result;
import com.example.trade.domain.dto.OrderFormDTO;
import com.example.trade.domain.po.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.trade.domain.vo.OrderVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
public interface IOrderService extends IService<Order> {

    Result<OrderVO> addOrder(OrderFormDTO orderFormDTO);

    Result<String> markOrderPaySuccess(Long orderId);

    Result<String> cancelOrder(Long orderId);
}
