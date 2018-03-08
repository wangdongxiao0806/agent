package com.wdx.middleware.aop;

import com.wdx.middleware.annontation.Agent;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态代理类
 */
public class ProxyAopHandler implements InvocationHandler {

    private static Logger logger = Logger.getLogger(ProxyAopHandler.class);

    private Object target;
    private static long timeout = 500;

    private static ConcurrentHashMap<String,Long> timeMap = new ConcurrentHashMap<String, Long>();
    private static ConcurrentHashMap<String,String> threadHashMap = new ConcurrentHashMap<String, String>();//线程安全HashMap

    private static ConcurrentHashMap<String ,Object> resultMap = new ConcurrentHashMap<String, Object>();//请求结果本地缓存

    public ProxyAopHandler(Object target){
        this.target = target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.error("log error");
        logger.info("log info");
        logger.debug("log debug");
        //校验接口上是否使用@Agent注解
        if(!method.isAnnotationPresent(Agent.class)){
            System.out.println("方法："+method.getName()+"没有使用@Agent注解");
            return method.invoke(target,args);
        }
        long startTime =timeMap.get(method.getName()) == null?0:timeMap.get(method.getName());
        if((System.currentTimeMillis() - startTime)>=timeout ){
            resultMap.remove(method.getName());
            threadHashMap.remove(method.getName());
        }

        String threadName = Thread.currentThread().getName();
        String oldName = threadHashMap.putIfAbsent(method.getName(),threadName);
        Object result = null;

        //第一次请求
        if(oldName == null){
            System.out.println("方法："+method.getName()+"使用@Agent注解，第一次发起请求....");
            //记录第一个请求时的时间戳
            timeMap.put(method.getName(),System.currentTimeMillis());
            result = method.invoke(target,args);
            resultMap.put(method.getName(),result);
        }else{
            System.out.println("方法："+method.getName()+"使用@Agent注解，非第一次发起请求....");
            //非第一次请求
            do{
                Thread.sleep(20);
                result = resultMap.get(method.getName());
            }while (result == null && (System.currentTimeMillis() - timeMap.get(method.getName()))<timeout );
            if(result == null){
                System.out.println("方法："+method.getName()+"使用@Agent注解，非第一次发起请求....但是请求超时");
                //如果等待没有获取到结果，则重试
                return this.invoke(proxy,method,args);
            }
        }
        return result;
    }
}
