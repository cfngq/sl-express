package com.example.trade.controller;


import cn.hutool.core.bean.BeanUtil;
import com.example.common.result.Result;
import com.example.trade.domain.dto.OrderFormDTO;
import com.example.trade.domain.vo.OrderVO;
import com.example.trade.service.IOrderDetailService;
import com.example.trade.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-12
 */
@RestController
@RequestMapping("/order")
@Api(tags = "交易相关接口")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;
    //根据id查询订单 创建订单 标记订单已支付

    @GetMapping("/id")
    @ApiOperation("根据id查询订单")
    public Result<OrderVO> getById(@RequestParam("id")Long id){
        OrderVO orderVO = BeanUtil.toBean(orderService.getById(id), OrderVO.class);
        orderVO.setOrderDetailList(orderDetailService.getByOrderId(id));
        return Result.success(orderVO);
    }

    @PostMapping
    @ApiOperation("新增交易订单")
    public Result<OrderVO> addOrder(@RequestBody OrderFormDTO orderFormDTO){
        return orderService.addOrder(orderFormDTO);
    }

    @PostMapping("/status")
    @ApiOperation("更新订单状态为已支付")
    public Result<String> markOrderPaySuccess(@RequestParam("orderId") Long orderId){
        return orderService.markOrderPaySuccess(orderId);
    }

    @PostMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancelOrder(@RequestParam("orderId") Long orderId){
        return orderService.cancelOrder(orderId);
    }
}
