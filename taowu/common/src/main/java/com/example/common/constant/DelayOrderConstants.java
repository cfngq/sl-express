package com.example.common.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DelayOrderConstants {
    public static final String DELAY_EXCHANGE_NAME  = "trade.delay.direct";
    public static final String DELAY_ORDER_QUEUE_NAME  = "trade.delay.order.queue";
    public static final String DELAY_ORDER_KEY   = "delay.order.query";
    public static final List<Long> DELAY_ORDER_TIME  = Arrays.asList(30000L,60000L,180000L,300000L,600000L,900000L);

}
