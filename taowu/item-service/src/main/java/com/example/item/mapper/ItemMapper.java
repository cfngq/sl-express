package com.example.item.mapper;

import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.domain.po.Item;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2024-04-09
 */
public interface ItemMapper extends BaseMapper<Item> {
    @Update("update item set stock = stock - #{num} where id = #{itemId} and stock >= #{num}")
    boolean updateStock(OrderDetailDTO orderDetailDTO);
    //
}
