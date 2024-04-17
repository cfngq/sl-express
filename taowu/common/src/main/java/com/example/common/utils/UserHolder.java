package com.example.common.utils;


public class UserHolder {
    //线程
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();
    //保存
    public static void saveUserId(Long userId){tl.set(userId);}
    //获取
    public static Long getUserId(){return tl.get();}
    //删除
    public static void removeUserId(){tl.remove();}
}
