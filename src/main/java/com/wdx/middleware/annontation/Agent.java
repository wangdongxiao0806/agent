package com.wdx.middleware.annontation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代理注解
 * 当高并发下有多个相同的请求时，代理会将多个请求合并成一个，随机选择leader，进行请求，其他等待该请求的结果
 * 减少下游系统的访问压力
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Agent {
}
