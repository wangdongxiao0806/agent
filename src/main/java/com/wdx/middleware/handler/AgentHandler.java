package com.wdx.middleware.handler;

import com.wdx.middleware.annontation.Agent;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class AgentHandler {

    private static Logger logger = Logger.getLogger(AgentHandler.class);


    private static long timeout = 500;

    private static ConcurrentHashMap<String,Long> timeMap = new ConcurrentHashMap<String, Long>();
    private static ConcurrentHashMap<String,String> threadHashMap = new ConcurrentHashMap<String, String>();//线程安全HashMap

    private static ConcurrentHashMap<String ,Object> resultMap = new ConcurrentHashMap<String, Object>();//请求结果本地缓存


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
            resultMap.remove(method.getName());
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
            resultMap.put(method.getName(),result);
        }else{
            logger.debug("method:"+method.getName()+"has used @Agent,but not the first request");
            //非第一次请求
            do{
                Thread.sleep(20);
                result = resultMap.get(method.getName());
            }while (result == null && (System.currentTimeMillis() - timeMap.get(method.getName()))<timeout );
            if(result == null){
                logger.debug("method:"+method.getName()+"has used @Agent,but not the first request,and time out");
                //如果等待没有获取到结果，则重试
                return proceed(pjp);
            }
        }
        return result;
    }
}
