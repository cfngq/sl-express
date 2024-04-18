package com.example.item.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.domain.Page.PageDTO;
import com.example.common.enums.ItemStatus;
import com.example.common.result.Result;
import com.example.common.utils.CacheClient;
import com.example.common.utils.UserHolder;
import com.example.item.domain.dto.ItemDTO;
import com.example.item.domain.dto.OrderDetailDTO;
import com.example.item.domain.po.Item;
import com.example.item.domain.query.ItemQuery;
import com.example.item.mapper.ItemMapper;
import com.example.item.service.IItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.common.constant.ItemConstants.*;
import static com.example.common.constant.RedisConstants.*;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-09
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {
    private final CacheClient cacheClient;
    private final RedissonClient redissonClient;

    //根据id查询商品
    @Override
    public Result<ItemDTO> getItemById(Long id) {
        String itemKey = ITEM_ID + id;
        //缓存
        String json = cacheClient.get(itemKey);
        if (StrUtil.isNotBlank(json)) {
            ItemDTO itemDTO = BeanUtil.toBean(json, ItemDTO.class);
            return Result.success(itemDTO);
        }
        //缓存重建
        Item item = getById(id);
        ItemDTO itemDTO = BeanUtil.toBean(item, ItemDTO.class);
        cacheClient.set(itemKey,StrUtil.toString(itemDTO),ITEM_ID_TTL,TimeUnit.MINUTES);
        return Result.success(itemDTO);
    }

    //修改商品状态
    @Override
    public Result<String> updateStatus(Long id, Integer status) {
        boolean update = lambdaUpdate()
                .set(Item::getStatus, status)
                .eq(Item::getId, id)
                .update();
        if (!update){
            throw new RuntimeException("商品状态修改错误");
        }
        return Result.success("商品状态修改成功");
    }

    //更新商品
    @Override
    public Result<String> updateItem(ItemDTO itemDTO) {
        boolean b = lambdaUpdate()
                .set(Item::getBrand,itemDTO.getBrand())
                .set(Item::getCategory,itemDTO.getCategory())
                .set(Item::getCommentCount,itemDTO.getCommentCount())
                .set(Item::getImage,itemDTO.getImage())
                .set(Item::getName,itemDTO.getName())
                .set(Item::getPrice,itemDTO.getPrice())
                .set(Item::getSold,itemDTO.getSold())
                .set(Item::getSpec,itemDTO.getSpec())
                .set(Item::getStatus,itemDTO.getStatus())
                .set(Item::getStock,itemDTO.getStock())
                .set(Item::getUpdater, UserHolder.getUserId())
                .set(Item::getUpdateTime, LocalDateTime.now())
                .eq(Item::getId,itemDTO.getId())
                .update();
        if (!b){
            throw new RuntimeException("更新商品出错");
        }
        return Result.success("更新商品成功");
    }

    //用户搜索商品
    //todo 缓存穿透判断
    @Override
    public PageDTO<ItemDTO> search(ItemQuery itemQuery) {
        String searchKey = SEARCH_KEY + itemQuery.getKey();
        //从redis中查询分页数据
        String total = String.valueOf(cacheClient.getHash(searchKey, TOTAL_KEY));
        String pages = String.valueOf(cacheClient.getHash(searchKey, PAGES_KEY));
        String json = String.valueOf(cacheClient.getHash(searchKey, DATA_KEY));
        //成功查询则封装并返回
        PageDTO<ItemDTO> itemDTOPageDTO = new PageDTO<>();
        if (StrUtil.isNotBlank(json) && StrUtil.isNotBlank(total) && StrUtil.isNotBlank(pages)){
            itemDTOPageDTO.setTotal(Long.parseLong(total));
            itemDTOPageDTO.setPages(Long.parseLong(pages));
            itemDTOPageDTO.setList(JSONUtil.toList(json, ItemDTO.class));
            return itemDTOPageDTO;
        }
        //是否为空值
        if (json != null || total!= null || pages!=null){
            return itemDTOPageDTO;
        }
        //失败则从数据库中查询
        Page<Item> page = lambdaQuery()
                .like(StrUtil.isNotBlank(itemQuery.getKey()), Item::getName, itemQuery.getKey())
                .eq(StrUtil.isNotBlank(itemQuery.getCategory()), Item::getCategory, itemQuery.getCategory())
                .eq((StrUtil.isNotBlank(itemQuery.getBrand())), Item::getBrand, itemQuery.getBrand())
                .eq(Item::getStatus, ItemStatus.NORMAL.getValue())
                .between(itemQuery.getMaxPrice() != null, Item::getPrice, itemQuery.getMinPrice(), itemQuery.getMaxPrice())
                .page(itemQuery.toMpPage());
        //保存至redis并返回
        PageDTO<ItemDTO> of = PageDTO.of(page, ItemDTO.class);
        cacheClient.setHash(searchKey,TOTAL_KEY,PAGES_KEY,DATA_KEY,of.getTotal(),of.getPages(),JSONUtil.toJsonStr(of.getList()),SEARCH_KEY_TTL,TimeUnit.MINUTES);
        return of;
    }

    //扣减库存
    @Override
    @Transactional
    public Result<String> deductStock(List<OrderDetailDTO> orderDetailDTOList) {
        //基于分布式锁实现事务一致
        RLock lock = redissonClient.getLock(LOCK_DEDUCT_STOCK_KEY + UserHolder.getUserId());
        //设置锁的过期时间，实现保底处理
        cacheClient.expire(StrUtil.toString(lock),LOCK_DEDUCT_STOCK_TTL,TimeUnit.MINUTES);
        //上锁
        boolean isLock = lock.tryLock();
        //加锁失败
        if (!isLock) {
            return Result.error("请先完成上个订单在进行新的订单！");
        }
        //封装item并判断库存是否充足
        boolean b;
        try {
            b = false;
            List<Item> itemList = new ArrayList<>();
            try {
                orderDetailDTOList.forEach(orderDetailDTO -> {
                    Item item = getById(orderDetailDTO.getItemId());
                    item.setStock(item.getStock() - orderDetailDTO.getNum());
                    if (item.getStock()<=0){
                        throw new RuntimeException(item.getName() + "商品库存不足，无法购买");
                    }
                    itemList.add(item);
                });
            } catch (RuntimeException e) {
                return Result.error(String.valueOf(e));
            }
            //库存充足，进行库存扣减
            try {
                b = updateBatchById(itemList);
            } catch (Exception e) {
                throw new RuntimeException("更新库存异常", e);
            }
        } finally {
            //释放锁
            lock.unlock();
        }
        if (!b) {
            return Result.error("库存不足，无法购买");
        }
        return Result.success("库存扣减成功");
    }

    //更新商品数量
    @Override
    public void updateItemNum(List<OrderDetailDTO> orderDetailDTOS) {
        orderDetailDTOS.forEach(orderDetailDTO -> {
            lambdaUpdate()
                    .set(Item::getStock,getById(orderDetailDTO.getItemId()).getStock()+orderDetailDTO.getNum())
                    .eq(Item::getId,orderDetailDTO.getItemId())
                    .update();
        });
    }
    //基于查询更新同步的批处理  问题：executeBatch无法处理单个sql false
      /*  String sqlStatement = "com.example.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
           r = executeBatch(orderDetailDTOList, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            return Result.error("更新异常");
        }
        if (!r) {
            return Result.error("商品库存不足，扣减失败");
        }
        return Result.success("库存扣减成功");
    }*/
    }

