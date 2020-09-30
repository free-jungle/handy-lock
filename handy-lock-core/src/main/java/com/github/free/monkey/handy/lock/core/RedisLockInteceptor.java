package com.github.free.monkey.handy.lock.core;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class RedisLockInteceptor {

    private final RedisLockManager redisLockManager;

    private final SimpleSpelExpressionEvaluator redisExpressionEvaluator;

    public RedisLockInteceptor(RedisLockManager redisLockManager) {
        this.redisLockManager = redisLockManager;
        this.redisExpressionEvaluator = this.createDefaultRedisExpressionEvaluator();
    }

    public RedisLockInteceptor(RedisLockManager redisLockManager,
                               SimpleSpelExpressionEvaluator redisExpressionEvaluator) {
        this.redisLockManager = redisLockManager;
        this.redisExpressionEvaluator = redisExpressionEvaluator;
    }

    private SimpleSpelExpressionEvaluator createDefaultRedisExpressionEvaluator() {
        SimpleSpelExpressionEvaluator expressionEvaluator = new SimpleSpelExpressionEvaluator();
        expressionEvaluator.setParser(new SpelExpressionParser());
        expressionEvaluator.setParameterNameDiscoverer(new DefaultParameterNameDiscoverer());
        return expressionEvaluator;
    }

    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RedisDistributedLockable redisDistributedLockable = method.getAnnotation(RedisDistributedLockable.class);
        String lockKey = this.parseLockKey(pjp, redisDistributedLockable);
        this.checkLockKey(lockKey);
        try (RedisLock redisLock = this.redisLockManager.fetchAndTryLock(lockKey, redisDistributedLockable.expireInMilliseconds(),
                redisDistributedLockable.waitInMilliseconds(), redisDistributedLockable.tryCount())) {
            return pjp.proceed();
        }
    }

    private void checkLockKey(String lockKey) {
        if (isEmpty(lockKey)) {
            throw new IllegalArgumentException("lockKey can not be empty!");
        }
    }

    private String parseLockKey(ProceedingJoinPoint pjp,
                                RedisDistributedLockable redisDistributedLockable) {
        String lockKey;
        if (!isEmpty(redisDistributedLockable.keySpel())) {
            lockKey = this.parseKeyExpression(pjp, redisDistributedLockable.keySpel());
        } else {
            lockKey = redisDistributedLockable.key();
        }
        return lockKey;
    }

    private String parseKeyExpression(ProceedingJoinPoint pjp,
                                      String key) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return this.redisExpressionEvaluator.parseMethodValue(key, pjp.getTarget(), signature.getMethod(), pjp.getArgs());
    }

    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
