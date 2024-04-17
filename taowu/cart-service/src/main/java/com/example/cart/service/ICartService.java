package com.example.cart.service;

import com.example.cart.domain.dto.CartFormDTO;
import com.example.cart.domain.po.Cart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.cart.domain.vo.CartVO;
import com.example.common.result.Result;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务类
 * </p>
 *
 * @author author
 * @since 2024-04-11
 */
public interface ICartService extends IService<Cart> {

    List<CartVO> queryAll();

    Result<String> saveCart(CartFormDTO cartFormDTO);
}
