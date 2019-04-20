package com.thorn.bbsmain.confugurations;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class WebLogAspect {


    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.thorn.bbsmain.controller.*.*(..))")
    public void webLog() {
    }

    @Order(1000)
    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());

        // 省略日志记录内容
    }

    @Order(1)
    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        log.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
    }


}