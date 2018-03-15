package com.wdx.middleware.timer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 计时器，用来判断请求是否超时
 */
public class Timer {

    private static Long TIME_OUT = 500L;
    private static ConcurrentHashMap<String , Long> timeCache = new ConcurrentHashMap<String, Long>();

    public static void setTime(String key){
        timeCache.put(key,System.currentTimeMillis());
    }
    public static boolean isTimeOut(String key){
        long t = timeCache.get(key)==null?0:timeCache.get(key);
        long n = System.currentTimeMillis();
        if((n-t) >= TIME_OUT){
            return true;
        }
        return false;
    }
    public static void clearTime(String key){
        timeCache.remove(key);
    }
}
