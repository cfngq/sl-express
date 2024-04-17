package com.example.item.controller;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.domain.Page.PageDTO;
import com.example.common.enums.ItemStatus;
import com.example.common.result.Result;
import com.example.item.domain.dto.ItemDTO;
import com.example.item.domain.po.Item;
import com.example.item.domain.query.ItemQuery;
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
@RequestMapping("/search")
@Api(tags = "搜索商品相关接口")
@RequiredArgsConstructor
public class SearchItemController {
//搜索商品(分页)
    private final IItemService itemService;

    @GetMapping
    @ApiOperation("搜索商品")
    public Result<PageDTO<ItemDTO>> search(ItemQuery itemQuery){
        return Result.success(itemService.search(itemQuery));
    }
}
