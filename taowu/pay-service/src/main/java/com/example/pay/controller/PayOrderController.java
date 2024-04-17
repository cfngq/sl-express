package com.example.pay.controller;


import cn.hutool.core.bean.BeanUtil;
import com.example.common.enums.PayType;
import com.example.common.result.Result;
import com.example.pay.domain.dto.PayApplyDTO;
import com.example.pay.domain.dto.PayOrderFormDTO;
import com.example.pay.domain.vo.PayOrderVO;
import com.example.pay.service.IPayOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 支付订单 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-15
 */
@RestController
@RequestMapping("/pay-order")
@Api(tags = "支付相关接口")
@RequiredArgsConstructor
public class PayOrderController {
    private final IPayOrderService payOrderService;
    //生成支付单,尝试基于用户余额支付,查询支付单

    @PostMapping
    @ApiOperation("生成支付单")
    public Result<String> applyPayOrder(@RequestBody PayApplyDTO payApplyDTO){
        if (!PayType.BALANCE.equalsValue(payApplyDTO.getPayType())){
            return Result.error("当前只支持余额支付");
        }
        return payOrderService.applyPayOrder(payApplyDTO);
    }

    @PostMapping("/pay")
    @ApiOperation("基于余额支付")
    public Result<String> tryPayOrderByBalance(@RequestBody PayOrderFormDTO payOrderFormDTO){
        return payOrderService.tryPayOrderByBalance(payOrderFormDTO);
    }

    @GetMapping
    @ApiOperation("查询支付单")
    public Result<List<PayOrderVO>> queryList(){
        return  Result.success(BeanUtil.copyToList(payOrderService.list(), PayOrderVO.class));
    }
}
