package com.example.trade.mapper;

import com.example.trade.domain.po.OrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 订单详情表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);
}
