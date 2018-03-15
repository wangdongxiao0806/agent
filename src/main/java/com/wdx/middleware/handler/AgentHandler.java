package com.wdx.middleware.handler;

import com.alibaba.fastjson.JSONObject;
import com.wdx.middleware.annontation.Agent;
import com.wdx.middleware.cache.AgentCache;
import com.wdx.middleware.timer.Timer;
import com.wdx.middleware.utils.Base64Utils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.ConcurrentHashMap;

public class AgentHandler {

    private static Logger logger = Logger.getLogger(AgentHandler.class);

    private static long timeout = 500;

    private static ConcurrentHashMap<String,String> threadCache = new ConcurrentHashMap<String, String>();//线程安全HashMap,用来存放并发请求线程

    public Object proceed(ProceedingJoinPoint pjp,AgentCache agentCache) throws Throwable {

        Method method = this.getMethod(pjp);
        if(!this.isAnnotationWithAgent(method)){
            logger.debug("method:"+method.getName()+" has not used @Agent !");
            return pjp.proceed();
        }
        String key = this.getRequest(pjp);
        if(Timer.isTimeOut(key)){
            boolean del = agentCache.remove(key);
            threadCache.remove(key);
        }

        String threadName = Thread.currentThread().getName();
        String oldName = threadCache.putIfAbsent(key,threadName);
        Object result = null;

        //第一次请求
        if(oldName == null){
            logger.debug("method:"+key+"has used @Agent,the first request");
            //记录第一个请求时的时间戳
            Timer.setTime(key);
            result =pjp.proceed();
            agentCache.save(key,result);
        }else{
            logger.debug("method:"+key+"has used @Agent,but not the first request");
            //非第一次请求
            do{
                Thread.sleep(20);
                result = agentCache.get(key);
            }while (result == null && !Timer.isTimeOut(key) );
            if(result == null){
                logger.debug("method:"+key+"has used @Agent,but not the first request,and time out");
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
    private String getRequest(ProceedingJoinPoint pjp){
        String key = "";
        Method method = this.getMethod(pjp);
        key = method.getName();
        Parameter[] parameters =method.getParameters();
        for(Parameter parameter : parameters){
            key = key + parameter.getName();
        }
        Object[] args = pjp.getArgs();
        for(Object arg : args){
            key =key + Base64Utils.encode(JSONObject.toJSONString(arg));
        }
        return key;
    }
}
