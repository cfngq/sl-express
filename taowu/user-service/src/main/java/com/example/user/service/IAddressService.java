package com.example.user.service;

import com.example.common.result.Result;
import com.example.user.domain.dto.AddressDTO;
import com.example.user.domain.po.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-08
 */
public interface IAddressService extends IService<Address> {

    Result<String> updateAddress(AddressDTO addressDTO);

    Result<List<AddressDTO>> queryAll();
}
