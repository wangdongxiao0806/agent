package com.wdx.middleware.handler;

import com.wdx.middleware.annontation.Agent;
import com.wdx.middleware.cache.AgentCache;
import com.wdx.middleware.cache.MemoryAgentCache;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class AgentHandler {

    private static Logger logger = Logger.getLogger(AgentHandler.class);

    private AgentCache agentCache = new MemoryAgentCache();


    private static long timeout = 500;

    private static ConcurrentHashMap<String,Long> timeMap = new ConcurrentHashMap<String, Long>();
    private static ConcurrentHashMap<String,String> threadHashMap = new ConcurrentHashMap<String, String>();//线程安全HashMap

    public Object proceed(ProceedingJoinPoint pjp) throws Throwable {

        Signature signature=pjp.getSignature();
        MethodSignature methodSignature=(MethodSignature)signature;
        Method method =methodSignature.getMethod();
        if(!method.isAnnotationPresent(Agent.class)){
            logger.debug("method:"+method.getName()+" has not used @Agent !");
            return pjp.proceed();
        }
        long startTime =timeMap.get(method.getName()) == null?0:timeMap.get(method.getName());
        if((System.currentTimeMillis() - startTime)>=timeout ){
            boolean del = agentCache.remove(method.getName());
            threadHashMap.remove(method.getName());
        }

        String threadName = Thread.currentThread().getName();
        String oldName = threadHashMap.putIfAbsent(method.getName(),threadName);
        Object result = null;

        //第一次请求
        if(oldName == null){
            logger.debug("method:"+method.getName()+"has used @Agent,the first request");
            //记录第一个请求时的时间戳
            timeMap.put(method.getName(),System.currentTimeMillis());
            result =pjp.proceed();
            agentCache.save(method.getName(),result);
        }else{
            logger.debug("method:"+method.getName()+"has used @Agent,but not the first request");
            //非第一次请求
            do{
                Thread.sleep(20);
                result = agentCache.get(method.getName());
            }while (result == null && (System.currentTimeMillis() - timeMap.get(method.getName()))<timeout );
            if(result == null){
                logger.debug("method:"+method.getName()+"has used @Agent,but not the first request,and time out");
                //如果等待没有获取到结果，则重试
                return proceed(pjp);
            }
        }
        return result;
    }

    public void setAgentCache(AgentCache agentCache) {
        this.agentCache = agentCache;
    }
}
