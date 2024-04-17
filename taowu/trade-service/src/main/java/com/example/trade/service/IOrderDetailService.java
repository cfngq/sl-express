package com.example.trade.service;

import com.example.trade.domain.po.OrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务类
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
public interface IOrderDetailService extends IService<OrderDetail> {

    List<OrderDetail> getByOrderId(Long orderId);
}
