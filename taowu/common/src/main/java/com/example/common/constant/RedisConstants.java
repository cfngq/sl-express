package com.example.common.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 360L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final String LOCK_SHOP_KEY = "lock:shop:";

    public static final String USER_KEY = "user:code:";
    public static final Long USER_TTL = 2L;

    public static final String PAY_ORDER_KEY = "pay:order:";
    public static final Long PAY_ORDER_TTL = 15L;


}
