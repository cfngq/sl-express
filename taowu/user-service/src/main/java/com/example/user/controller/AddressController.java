package com.example.user.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.example.common.exception.BadRequestException;
import com.example.common.result.Result;
import com.example.common.utils.UserHolder;
import com.example.user.domain.dto.AddressDTO;
import com.example.user.domain.po.Address;
import com.example.user.service.IAddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author author
 * @since 2024-04-08
 */
@RestController
@RequestMapping("/address")
@Api(tags = "地址相关接口")
@RequiredArgsConstructor
public class AddressController {
//根据地址id查询该用户地址信息,查询当前用户所有地址,新增地址,update
    private final IAddressService addressService;

    @GetMapping("/{addressId}")
    @ApiOperation("根据地址id查询该用户地址信息")
    public Result<AddressDTO> getById(@PathVariable("addressId") Long id){
        Address address = addressService.getById(id);
        if (!address.getUserId().equals(UserHolder.getUserId())){
            throw new BadRequestException("该地址不属于该用户！");
        }
        return Result.success(BeanUtil.toBean(address, AddressDTO.class));
    }

    @GetMapping
    @ApiOperation("查询当前用户所有地址")
    public Result<List<AddressDTO>> getAll(){
        List<Address> addressList = addressService.lambdaQuery()
                .eq(Address::getUserId, UserHolder.getUserId())
                .list();
        //若查询为空，则返回错误
        if (CollectionUtil.isEmpty(addressList)){
            return Result.error("该用户无地址信息");
        }
        List<AddressDTO> addressDTOList = BeanUtil.copyToList(addressList, AddressDTO.class);
        return Result.success(addressDTOList);
    }

    @PostMapping
    @ApiOperation("新增地址")
    public void add(@RequestBody AddressDTO addressDTO){
        Address address = BeanUtil.copyProperties(addressDTO, Address.class);
        address.setUserId(UserHolder.getUserId());
        addressService.save(address);
    }

    @PostMapping("/update")
    @ApiOperation("更新地址信息")
    public void update(AddressDTO addressDTO){
        addressService.updateAddress(addressDTO);
    }
}
