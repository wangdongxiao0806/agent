package com.wdx.middleware.aop;

import com.wdx.middleware.handler.AgentHandler;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * AOP拦截器
 */
public class AgentAopInterceptor {

    private AgentHandler agent = new AgentHandler();

    public Object proceed(ProceedingJoinPoint pjp) throws Throwable {
        return agent.proceed(pjp);
    }

}
