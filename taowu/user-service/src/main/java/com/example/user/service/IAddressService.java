package com.example.user.service;

import com.example.user.domain.dto.AddressDTO;
import com.example.user.domain.po.Address;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author author
 * @since 2024-04-08
 */
public interface IAddressService extends IService<Address> {

    void updateAddress(AddressDTO addressDTO);
}
