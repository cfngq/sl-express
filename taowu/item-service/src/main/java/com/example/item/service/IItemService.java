package com.example.item.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.domain.Page.PageDTO;
import com.example.common.result.Result;
import com.example.item.domain.dto.ItemDTO;
import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.domain.po.Item;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.item.domain.query.ItemQuery;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author author
 * @since 2024-04-09
 */
public interface IItemService extends IService<Item> {

    Result<ItemDTO> getItemById(Long id);
    Result<String> updateStatus(Long id, Integer status);

    Result<String> updateItem(ItemDTO itemDTO);

    PageDTO<ItemDTO> search(ItemQuery itemQuery);

    Result<String> deductStock(List<OrderDetailDTO> orderDetailDTOList);
    void updateItemNum(List<OrderDetailDTO> orderDetailDTOS);

    Result<String> removeItem(Long id);
}
