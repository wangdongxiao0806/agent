package com.wdx.middleware.aop;

import com.wdx.middleware.cache.AgentCache;
import com.wdx.middleware.cache.MemoryAgentCache;
import com.wdx.middleware.handler.AgentHandler;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * AOP拦截器
 */
public class AgentAopInterceptor {

    private AgentHandler agent = new AgentHandler();

    private String cacheModel;

    private AgentCache agentCache = new MemoryAgentCache();

    public Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        return agent.proceed(pjp,agentCache);
    }

    public void setCacheModel(String cacheModel) {
        if("memory".equals(cacheModel)){
            agentCache = new MemoryAgentCache();
        }else if("redis".equals(cacheModel)){
            agentCache = null;
        }
        this.cacheModel = cacheModel;
    }
}
