package com.wdx.middleware.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理请求结果缓存实现类，内存缓存
 */
public class MemoryAgentCache implements AgentCache{

    private static ConcurrentHashMap<String ,Object> cacheMap = new ConcurrentHashMap<String, Object>();//请求结果本地缓存

    public void save(String key, Object result) {
        cacheMap.put(key,result);
    }

    public Object get(String key) {
        return  cacheMap.get(key);
    }

    public boolean remove(String key) {
        cacheMap.remove(key);
        return cacheMap.get(key) == null?true:false;
    }
}
