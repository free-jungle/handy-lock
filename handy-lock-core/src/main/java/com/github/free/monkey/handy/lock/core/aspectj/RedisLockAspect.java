package com.github.free.monkey.handy.lock.core.aspectj;

import com.github.free.monkey.handy.lock.core.RedisLockInteceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class RedisLockAspect {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisLockAspect.class);

    private RedisLockInteceptor redisLockInteceptor;

    public RedisLockAspect() {
    }

    public RedisLockAspect(RedisLockInteceptor redisLockInteceptor) {
        this.redisLockInteceptor = redisLockInteceptor;
    }

    @Around("@annotation(com.github.free.monkey.handy.lock.core.RedisDistributedLockable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (redisLockInteceptor == null) {
            LOGGER.error("RedisLockInteceptor not inject, please check your config");
            return joinPoint.proceed();
        }
        return redisLockInteceptor.around(joinPoint);
    }

}
