package com.example.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.example.api.client.ItemClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.cart.domain.dto.CartFormDTO;
import com.example.cart.domain.po.Cart;
import com.example.cart.domain.vo.CartVO;
import com.example.cart.mapper.CartMapper;
import com.example.cart.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.result.Result;
import com.example.common.utils.UserHolder;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-11
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final ItemClient itemClient;
    @Override
    @GlobalTransactional
    public List<CartVO> queryAll() {
        //获取线程用户
        Long userId = UserHolder.getUserId();
        //查询该用户购物车数据
        List<Cart> list = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .list();
        if (CollectionUtil.isEmpty(list)){
            //购物车无数据，则返回空集合
            return Collections.emptyList();
        }
        //todo 远程调用查询商品接口，封装VO
        List<CartVO> cartVOS = BeanUtil.copyToList(list, CartVO.class);
        handleCartItems(cartVOS);
        return cartVOS;
    }

    private void handleCartItems(List<CartVO> cartVOS) {
        //获取商品ids集合
        List<Long> ids = cartVOS.stream().map(CartVO::getItemId).collect(Collectors.toList());
        //查询商品数据
        Result<List<ItemDTO>> result = itemClient.getItemByIds(ids);
        List<ItemDTO> itemDTOList = result.getData();
        //转为 id 到 item的map   变量返回自身：Function.identity()
        Map<Long, ItemDTO> itemDTOMap = itemDTOList.stream().collect(Collectors.toMap(ItemDTO::getId, Function.identity()));
        //item中的数据写入vo
        cartVOS.forEach(cartVO -> {
            //基于cartVO中的itemId从itemDTOMap得到对应参数
            ItemDTO itemDTO = itemDTOMap.get(cartVO.getItemId());
            cartVO.setNewPrice(itemDTO.getPrice());
            cartVO.setStatus(itemDTO.getStatus());
            cartVO.setStock(itemDTO.getStock());
        });
    }

    @Override
    public Result<String> saveCart(CartFormDTO cartFormDTO) {
        //获取用户信息
        Long userId = UserHolder.getUserId();
        //该商品存在，则直接更新商品数量
        if (ItemIsExists(cartFormDTO.getItemId(),userId)){
            try {
                lambdaUpdate()
                        //.set(Cart::getNum,lambdaQuery().eq(Cart::getId,cartFormDTO.getItemId()).eq(Cart::getUserId,userId).one().getNum()+1)
                        .set(Cart::getNum,+1)
                        .eq(Cart::getId,cartFormDTO.getItemId())
                        .eq(Cart::getUserId,userId)
                        .update();
                return Result.success("添加成功");
            } catch (Exception e) {
                log.error("添加失败，错误：",e);
                return Result.error("添加失败");
            }
        }
        //该用户购物车商品是否超出上限
        if (cartIsFull(userId)){
            return Result.error("购物车超出上限，无法在进行添加");
        }
        //新增商品
        Cart cart = BeanUtil.toBean(cartFormDTO, Cart.class);
        cart.setUserId(userId);
        boolean b = save(cart);
        if (!b){
            return Result.error("添加失败");
        }
        return Result.success("添加成功");
    }

    private boolean cartIsFull(Long userId) {
        Integer count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .count();
        return count>20;
    }

    private boolean ItemIsExists(Long itemId, Long userId) {
        Integer count = lambdaQuery()
                .eq(Cart::getId, itemId)
                .eq(Cart::getUserId, userId)
                .count();
        return count > 0;
    }
}
