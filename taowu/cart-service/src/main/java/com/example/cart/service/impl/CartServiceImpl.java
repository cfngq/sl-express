package com.example.cart.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.api.client.ItemClient;
import com.example.api.domain.dto.ItemDTO;
import com.example.cart.domain.dto.CartFormDTO;
import com.example.cart.domain.po.Cart;
import com.example.cart.domain.vo.CartVO;
import com.example.cart.mapper.CartMapper;
import com.example.cart.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.result.Result;
import com.example.common.utils.CacheClient;
import com.example.common.utils.UserHolder;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.common.constant.CartConstants.CART_KEY;
import static com.example.common.constant.CartConstants.CART_KEY_TTL;
import static com.example.common.constant.UserConstants.USER_HOLDER;

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
    private final CacheClient cacheClient;
    //查询用户购物车所有数据
    //缓存
    @Override
    @GlobalTransactional
    public List<CartVO> queryAll() {
        //获取线程用户
        Long userId = UserHolder.getUserId();
        //缓存
        String cartKey = CART_KEY+userId;
        String json = cacheClient.get(cartKey);
        List<CartVO> cartVOList = JSONUtil.toList(json, CartVO.class);
        if (!CollectionUtil.isEmpty(cartVOList)){
            return cartVOList;
        }
        //查询该用户购物车数据
        List<CartVO> cartVOS = getCartList(userId);
        cacheClient.set(cartKey,JSONUtil.toJsonStr(cartVOS),CART_KEY_TTL, TimeUnit.MINUTES);
        return cartVOS;
    }

    //查询购物车数据
    private List<CartVO> getCartList(Long userId) {
        List<Cart> list = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .list();
        if (CollectionUtil.isEmpty(list)){
            //购物车无数据，则返回空集合
            return Collections.emptyList();
        }
        //远程调用查询商品接口，封装VO
        List<CartVO> cartVOS = BeanUtil.copyToList(list, CartVO.class);
        handleCartItems(cartVOS);
        return cartVOS;
    }

    //cartVOS封装商品数据
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


    //更新购物车则更新缓存
    @Override
    @GlobalTransactional
    public Result<String> saveCart(CartFormDTO cartFormDTO) {
        //获取用户信息
        Long userId = UserHolder.getUserId();
        //该商品存在，则直接更新商品数量
        if (ItemIsExists(cartFormDTO.getItemId(),userId)){
            try {
                lambdaUpdate()
                        //.set(Cart::getNum,lambdaQuery().eq(Cart::getId,cartFormDTO.getItemId()).eq(Cart::getUserId,userId).one().getNum()+1)
                        .set(Cart::getNum,getCartNum(cartFormDTO,userId)+1)
                        .eq(Cart::getItemId,cartFormDTO.getItemId())
                        .eq(Cart::getUserId,userId)
                        .update();
                return Result.success("购物车商品数量增加成功");
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
        //更新缓存
        List<CartVO> cartVOS = getCartList(userId);
        String cartKey = CART_KEY + userId;
        cacheClient.set(cartKey,JSONUtil.toJsonStr(cartVOS),CART_KEY_TTL,TimeUnit.MINUTES);
        if (!b){
            return Result.error("添加失败");
        }
        return Result.success("新添商品至购物车成功");
    }

    //todo 获取并删除购物车商品
    @Override
    public void removeCartItem(Message message) {
        UserHolder.saveUserId(message.getMessageProperties().getHeader(USER_HOLDER));
        List<Cart> list = lambdaQuery()
                .eq(Cart::getUserId,UserHolder.getUserId())
                .in(Cart::getItemId,(JSONUtil.toList(JSONUtil.toJsonStr(message.getBody()),Long.class)))
                .list();
        Set<Long> idSet = list.stream().map(Cart::getId).collect(Collectors.toSet());
        removeByIds(idSet);
    }

    private Integer getCartNum(CartFormDTO cartFormDTO,Long userId){
         return lambdaQuery()
                .eq(Cart::getItemId, cartFormDTO.getItemId())
                .eq(Cart::getUserId, userId)
                .one()
                .getNum();

    }
    private boolean cartIsFull(Long userId) {
        Integer count = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .count();
        return count>20;
    }

    private boolean ItemIsExists(Long itemId, Long userId) {
        Integer count = lambdaQuery()
                .eq(Cart::getItemId, itemId)
                .eq(Cart::getUserId, userId)
                .count();
        return count > 0;
    }
}
