package com.example.item.controller;


import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.common.domain.Page.PageDTO;
import com.example.common.domain.Page.PageQuery;
import com.example.common.result.Result;
import com.example.item.domain.dto.ItemDTO;
import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.domain.po.Item;
import com.example.item.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-09
 */
@RestController
@RequestMapping("/item")
@Api(tags = "商品相关接口")
@RequiredArgsConstructor
public class ItemController {
//搜索商品(分页)  根据商品id/ids查询商品    删除商品
    private final IItemService itemService;
    @GetMapping("/id")
    @ApiOperation("根据商品id查询商品")
    public Result<ItemDTO> getById(@RequestParam("id")Long id){
        return itemService.getItemById(id);
    }

    @GetMapping
    @ApiOperation("根据商品id列表查询商品")
    public Result<List<ItemDTO>> getByIds(@RequestParam("ids")List<Long> ids){
        return Result.success(BeanUtil.copyToList(itemService.listByIds(ids), ItemDTO.class));
    }

    @DeleteMapping
    @ApiOperation("根据商品id删除商品")
    public Result<String> deleteById(@RequestParam("id") Long id){
        return itemService.removeItem(id);

    }

    @GetMapping("/page")
    @ApiOperation("搜索商品")
    public Result<PageDTO<ItemDTO>> page(PageQuery pageQuery){
        Page<Item> page = itemService.page(pageQuery.toMpPage());
        return Result.success(PageDTO.of(page, ItemDTO.class));
    }
    //更新商品状态  更新商品 扣减库存

    @PostMapping("/status")
    @ApiOperation("更新商品状态")
    public Result<String> status(@RequestParam("id") Long id,
                       @RequestParam("status")Integer status){
        return itemService.updateStatus(id,status);
    }

    @PostMapping("/update")
    @ApiOperation("更新商品")
    public Result<String> update(@RequestBody ItemDTO itemDTO){
        // 不允许修改商品状态，所以强制设置为null，更新时，就会忽略该字段
        itemDTO.setStatus(null);
        return itemService.updateItem(itemDTO);
    }

    @PostMapping("/deduct")
    @ApiOperation("扣减库存")
    public Result<String> deductStock(@RequestBody List<OrderDetailDTO> orderDetailDTOList){
        return itemService.deductStock(orderDetailDTOList);
    }
}
