package com.example.common.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    //定义返回结果
    private Integer code;
    private String msg;
    private T data;

    //无数据成功返回
    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.setCode(1);
        return result;
    }
    //有数据成功返回
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setData(data);
        return result;
    }

    //失败返回
    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }
}
