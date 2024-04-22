package com.example.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.common.constant.AddressConstants;
import com.example.common.enums.AddressStatus;
import com.example.common.result.Result;
import com.example.common.utils.CacheClient;
import com.example.common.utils.UserHolder;
import com.example.user.domain.dto.AddressDTO;
import com.example.user.domain.po.Address;
import com.example.user.mapper.AddressMapper;
import com.example.user.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.common.constant.AddressConstants.ADDRESS_KEY_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author author
 * @since 2024-04-08
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {
    private final CacheClient cacheClient;
    //更新地址信息
    @Override
    public Result<String> updateAddress(AddressDTO addressDTO) {
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
        return Result.success("地址修改成功");
    }

    //查询当前用户所有地址
    @Override
    public Result<List<AddressDTO>> queryAll() {
        //缓存redis
        String address;
        String addressKey = AddressConstants.ADDRESS_KEY + UserHolder.getUserId();
        address = cacheClient.get(addressKey);
        if (StrUtil.isNotBlank(address)){
            return Result.success(JSONUtil.toList(address, AddressDTO.class));
        }
        List<Address> addressList = lambdaQuery()
                .eq(Address::getUserId, UserHolder.getUserId())
                .list();
        //若查询为空，则返回错误
        if (CollectionUtil.isEmpty(addressList)){
            return Result.error("该用户无地址信息");
        }
        List<AddressDTO> addressDTOList = BeanUtil.copyToList(addressList, AddressDTO.class);
        address = JSONUtil.toJsonStr(addressDTOList);
        cacheClient.set(addressKey,address,ADDRESS_KEY_TTL, TimeUnit.MINUTES);
        return Result.success(addressDTOList);
    }
}
