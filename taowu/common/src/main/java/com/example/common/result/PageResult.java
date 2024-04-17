package com.example.common.result;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    //定义返回结果
    private Long total;
    private Long pages;
    private List<T> data;

    //无参返回
    public static <T> PageResult<T> empty(Long total,Long pages){
        return new PageResult<>(total,pages, Collections.emptyList());
    }
    public static <T> PageResult<T> empty(Page<?> page){
        return new PageResult<>(page.getTotal(), page.getPages(), Collections.emptyList());
    }

    //有参返回
    public static <T> PageResult<T> of(Page<T> page){
        if (page == null){
            return new PageResult<>();
        }
        if (CollUtil.isEmpty(page.getRecords())){
            return empty(page);
        }
        return new PageResult<>(page.getTotal(), page.getPages(),page.getRecords());
    }
    //以所需的数据类型返回
    public static <T,R> PageResult<T> of(Page<R> page,Class<T> clazz){
        return new PageResult<>(page.getTotal(), page.getPages(), BeanUtil.copyToList(page.getRecords(),clazz));
    }
}
