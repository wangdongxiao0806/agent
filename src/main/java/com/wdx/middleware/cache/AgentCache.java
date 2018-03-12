package com.wdx.middleware.cache;

/**
 * 代理请求结果缓存接口
 */
public interface AgentCache {

    /**
     * 将请求结果存入缓存
     * @param result
     */
    public void save(String key,Object result);

    /**
     * 获取缓存的结果
     * @param key
     */
    public Object get(String key);

    /**
     * 根据key 删除缓存信息
     * @param key
     */
    public boolean remove(String key);
}
