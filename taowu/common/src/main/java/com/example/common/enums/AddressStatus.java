package com.example.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum AddressStatus {
    FROZEN("0","非默认地址"),
    NORMAL("1","正常"),
    ;
    @EnumValue
    String value;

    String desc;

    AddressStatus(String value,String desc){
        this.value=value;
        this.desc=desc;
    }
}
