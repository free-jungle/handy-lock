package com.github.free.jungle.handy.lock.examples.service.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import com.github.free.jungle.handy.lock.core.LockFailException;
import com.github.free.jungle.handy.lock.core.RedisDistributedLockable;
import com.github.free.jungle.handy.lock.core.RedisLock;
import com.github.free.jungle.handy.lock.core.RedisLockManager;
import com.github.free.jungle.handy.lock.examples.service.IRedisOpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RedisOpService implements IRedisOpService {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisOpService.class);

    @Resource
    private RedisLockManager redisLockManager;

    @Override
    @RedisDistributedLockable(key = "com.github.free.jungle.handy.lock.examples.service.impl.lockUseAnnotation",
            expireInMilliseconds = 10000, waitInMilliseconds = 1000, tryCount = 1)
    public void lockUseAnnotation() {
        LOGGER.info("start lockUseAnnotation");
        try {
            TimeUnit.MILLISECONDS.sleep(100L);
        } catch (Exception ex) {
            LOGGER.error("error when lockUseAnnotation", ex);
        }
        LOGGER.info("end lockUseAnnotation");
    }

    @Override
    @RedisDistributedLockable(key = "com.github.free.jungle.handy.lock.examples.service.impl.lockUserAnnotationWithTryCount",
            expireInMilliseconds = 10000, waitInMilliseconds = 10, tryCount = 300)
    public void lockUserAnnotationWithTryCount() {
        LOGGER.info("start lockUserAnnotationWithTryCount");
        try {
            TimeUnit.MILLISECONDS.sleep(500L);
        } catch (Exception ex) {
            LOGGER.error("error when lockUseAnnotation", ex);
        }
        LOGGER.info("end lockUserAnnotationWithTryCount");
    }

    @Override
    @RedisDistributedLockable(keySpel = "'com.github.free.jungle.handy.lock.examples.service.impl.lockUserAnnotationWithSpel#'+#id",
            expireInMilliseconds = 10000, waitInMilliseconds = 1000, tryCount = 3)
    public void lockUserAnnotationWithSpel(String id) {
        LOGGER.info("start lockUserAnnotationWithSpel:{}", id);
        try {
            TimeUnit.SECONDS.sleep(2L);
        } catch (Exception ex) {
            LOGGER.error("error when lockUseAnnotation", ex);
        }
        LOGGER.info("end lockUserAnnotationWithSpel:{}", id);
    }

    @Override
    @RedisDistributedLockable(expireInMilliseconds = 10000, waitInMilliseconds = 1000, tryCount = 3)
    public void lockUserAnnotationWithEmptyKey(String id) {
        LOGGER.info("start lockUserAnnotationWithEmptyKey:{}", id);
        try {
            TimeUnit.SECONDS.sleep(2L);
        } catch (Exception ex) {
            LOGGER.error("error when lockUseAnnotation", ex);
        }
        LOGGER.info("end lockUserAnnotationWithEmptyKey:{}", id);
    }

    @Override
    public void lockUseLockManager(String id) {
        String lockKey = String.format("%s.%s#%s", this.getClass().getCanonicalName(), "lockUseLockManager", id);
        try (RedisLock redisLock = redisLockManager.fetchAndTryLock(lockKey, 5000, 1000, 2)) {
            TimeUnit.SECONDS.sleep(2L);
        } catch (LockFailException | IOException | InterruptedException ex) {
            LOGGER.error("error when lockUseLockManager", ex);
        }
    }

    @Override
    public void lockWithScheduleUseLockManager(String id) {
        String lockKey = String.format("%s.%s#%s", this.getClass().getCanonicalName(), "lockUseLockManager", id);
        try (RedisLock redisLock = redisLockManager.fetchAndTryLockWithSchedule(lockKey)) {
            TimeUnit.SECONDS.sleep(30L);
        } catch (LockFailException | IOException | InterruptedException ex) {
            LOGGER.error("error when lockUseLockManager", ex);
        }
    }

}
