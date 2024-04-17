package com.example.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ItemStatus {
    NORMAL("1","已上架"),
    FROZEN("2","未上架"),
    DELETE("3","已删除"),
    ;
    @EnumValue
    String value;

    String desc;

    ItemStatus(String value, String desc){
        this.value=value;
        this.desc=desc;
    }
}
