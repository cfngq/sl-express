package com.example.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.common.enums.AddressStatus;
import com.example.common.utils.UserHolder;
import com.example.user.domain.dto.AddressDTO;
import com.example.user.domain.po.Address;
import com.example.user.mapper.AddressMapper;
import com.example.user.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-08
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    @Override
    public void updateAddress(AddressDTO addressDTO) {
        //是否将地址修改为默认地址
        String status = String.valueOf(addressDTO.getIsDefault());
        if (status.equals(AddressStatus.NORMAL.getValue())){
            //其余地址默认均修改为0
            lambdaUpdate()
                    .eq(Address::getUserId, UserHolder.getUserId())
                    .set(Address::getIsDefault,AddressStatus.FROZEN.getValue())
                    .update();
        }
        //修改该地址
        updateById(BeanUtil.toBean(addressDTO, Address.class));
    }
}
