package com.example.trade.service.impl;

import com.example.trade.domain.po.OrderDetail;
import com.example.trade.mapper.OrderDetailMapper;
import com.example.trade.service.IOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService {

    private final OrderDetailMapper orderDetailMapper;
    @Override
    public List<OrderDetail> getByOrderId(Long orderId) {
        return orderDetailMapper.getByOrderId(orderId);
    }
}
