package com.example.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.example.api.client.ItemClient;
import com.example.api.client.PayClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.api.domain.dto.PayApplyDTO;
import com.example.common.result.Result;
import com.example.common.utils.RabbitMqHelper;
import com.example.common.utils.UserHolder;
import com.example.api.domain.dto.OrderDetailDTO;
import com.example.trade.domain.dto.OrderFormDTO;
import com.example.trade.domain.po.Order;
import com.example.trade.domain.po.OrderDetail;
import com.example.trade.domain.vo.OrderVO;
import com.example.trade.mapper.OrderMapper;
import com.example.trade.service.IOrderDetailService;
import com.example.trade.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final ItemClient itemClient;
    private final IOrderDetailService orderDetailService;
    private final RabbitMqHelper rabbitMqHelper;
    private final PayClient payClient;
    /**
     * 新增交易单
     */
    @Override
    @GlobalTransactional
    public Result<OrderVO> addOrder(OrderFormDTO orderFormDTO) {
        //封装订单实体数据
        Order order = new Order();
        //获取下单商品列表
        List<OrderDetailDTO> details = orderFormDTO.getDetails();
        //获取商品id和商品数量
        Map<Long, Integer> itemMap = details.stream().collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> ids = itemMap.keySet();
        //远程调用查询并判断商品是否存在
        Result<List<ItemDTO>> result = itemClient.getItemByIds(ids);
        List<ItemDTO> itemDTOList = result.getData();
        //计算商品价格并封装
        int total = 0;
        for (ItemDTO itemDTO:itemDTOList){
            total = itemDTO.getPrice()*itemMap.get(itemDTO.getId());
        }
        order.setTotalFee(total);
        //封装其余属性，交易单状态，支付状态，用户id
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserHolder.getUserId());
        order.setStatus(1);
        //写入订单
        save(order);
        //保存订单详情，基于订单，商品，商品map构建订单详情
        List<OrderDetail> detailList = buildDetails(order.getId(), itemDTOList, itemMap);
        orderDetailService.saveBatch(detailList);
        //远程调用，扣减库存
        try {
            itemClient.deductStock(details);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！:",e);
        }
        //异步消息，清理购物车
        rabbitMqHelper.sendMessage("cart.topic","deduct.cart",ids);
        //封装返回数据 返回
        OrderVO orderVO = BeanUtil.toBean(order, OrderVO.class);
        orderVO.setOrderDetailList(detailList);
        return Result.success(orderVO);
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> itemDTOList, Map<Long, Integer> itemMap) {
        //封装商品信息至订单详情
        List<OrderDetail> orderDetailList = new ArrayList<>();
        itemDTOList.forEach(item -> {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(itemMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            orderDetailList.add(detail);
        });
        return orderDetailList;
    }

    @Override
    public Result<String> markOrderPaySuccess(Long orderId) {
        boolean b = lambdaUpdate()
                .set(Order::getStatus, 2)
                .set(Order::getUpdateTime, LocalDateTime.now())
                .eq(Order::getId, orderId)
                .eq(Order::getStatus, 1)
                .update();
        if (!b){
            return Result.error("更新状态失败，请重新再试");
        }
        return Result.success("更新支付状态成功");
    }

    @Override
    @GlobalTransactional
    public Result<String> cancelOrder(Long orderId) {
        boolean b = lambdaUpdate()
                .set(Order::getStatus, 5)
                .eq(Order::getId, orderId)
                .update();
        if (b){
            //todo 返回库存,mq消息通知
            //获取交易单详情
            List<OrderDetail> orderDetailList = orderDetailService.getByOrderId(orderId);
            List<OrderDetailDTO> orderDetailDTOS = BeanUtil.copyToList(orderDetailList, OrderDetailDTO.class);
            rabbitMqHelper.sendMessage("item.topic","item.num",orderDetailDTOS);
        }
        return Result.success("取消订单成功");
    }
}