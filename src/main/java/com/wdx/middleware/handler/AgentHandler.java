package com.wdx.middleware.handler;

import com.wdx.middleware.annontation.Agent;
import com.wdx.middleware.cache.AgentCache;
import com.wdx.middleware.timer.Timer;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class AgentHandler {

    private static Logger logger = Logger.getLogger(AgentHandler.class);

    private static long timeout = 500;

    private static ConcurrentHashMap<String,String> threadMap = new ConcurrentHashMap<String, String>();//线程安全HashMap,用来存放并发请求线程

    public Object proceed(ProceedingJoinPoint pjp,AgentCache agentCache) throws Throwable {

        Method method = this.getMethod(pjp);
        if(!this.isAnnotationWithAgent(method)){
            logger.debug("method:"+method.getName()+" has not used @Agent !");
            return pjp.proceed();
        }
        if(Timer.isTimeOut(method.getName())){
            boolean del = agentCache.remove(method.getName());
            threadMap.remove(method.getName());
        }

        String threadName = Thread.currentThread().getName();
        String oldName = threadMap.putIfAbsent(method.getName(),threadName);
        Object result = null;

        //第一次请求
        if(oldName == null){
            logger.debug("method:"+method.getName()+"has used @Agent,the first request");
            //记录第一个请求时的时间戳
            Timer.setTime(method.getName());
            result =pjp.proceed();
            agentCache.save(method.getName(),result);
        }else{
            logger.debug("method:"+method.getName()+"has used @Agent,but not the first request");
            //非第一次请求
            do{
                Thread.sleep(20);
                result = agentCache.get(method.getName());
            }while (result == null && !Timer.isTimeOut(method.getName()) );
            if(result == null){
                logger.debug("method:"+method.getName()+"has used @Agent,but not the first request,and time out");
                //如果等待没有获取到结果，则重试
                return proceed(pjp,agentCache);
            }
        }
        return result;
    }

    private boolean isAnnotationWithAgent(Method method){
        return method.isAnnotationPresent(Agent.class);
    }

    private Method getMethod(ProceedingJoinPoint pjp){
        Signature signature=pjp.getSignature();
        MethodSignature methodSignature=(MethodSignature)signature;
        Method method =methodSignature.getMethod();
        return method;
    }
}
