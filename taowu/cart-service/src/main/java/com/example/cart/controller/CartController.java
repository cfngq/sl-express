package com.example.cart.controller;


import cn.hutool.core.bean.BeanUtil;
import com.example.cart.domain.dto.CartFormDTO;
import com.example.cart.domain.po.Cart;
import com.example.cart.domain.vo.CartVO;
import com.example.cart.service.ICartService;
import com.example.common.result.Result;
import com.example.common.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 订单详情表 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-11
 */
@RestController
@RequestMapping("/cart")
@Api(tags = "购物车相关接口")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;
       /*
添加商品到购物车 更新购物车数据 删除购物车中商品 查询购物车列表 批量删除购物车中商品
     */
    @PostMapping
    @ApiOperation("添加商品到购物车")
    public Result<String> addById(@RequestBody CartFormDTO cartFormDTO) {
        //需判断商品是否存在，商品是否超出购买上限
        return cartService.saveCart(cartFormDTO);
    }

    @PostMapping("/update")
    @ApiOperation("更新购物车中的商品")
    public Result<String> update(@RequestBody Cart cart) {
        boolean b = cartService.updateById(cart);
        if (!b) {
            return Result.error("更新失败，请重新尝试");
        }
        return Result.success("更新成功");
    }

    @DeleteMapping
    @ApiOperation("批量删除购物车中的商品")
    public Result<String> delete(@RequestParam("ids") List<Long> ids){
        boolean b = cartService.removeByIds(ids);
        if (!b) {
            return Result.error("删除失败，请重新尝试");
        }
        return Result.success("删除成功");
    }

    @GetMapping
    @ApiOperation("查看购物车数据")
    public Result<List<CartVO>> list(){
        List<CartVO> cartVOList = cartService.queryAll();
        return Result.success(cartVOList);
    }
}
