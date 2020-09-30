package com.github.free.monkey.handy.lock.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 非线程安全，不要多线程共享RedisLock实例
 */
public class RedisLock implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

    private final IRedisClient redisClient;

    private final String lockKey;

    private final long expireInMilliseconds;

    private boolean acquired = false;

    private ScheduledFuture scheduledFuture;

    private String value;

    public String getValue() {
        return this.value;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    /**
     * RedisLock构造
     *
     * @param redisClient          redis操作类
     * @param lockKey              锁key
     * @param expireInMilliseconds 有效期
     */
    public RedisLock(IRedisClient redisClient, String lockKey, long expireInMilliseconds) {
        this.redisClient = redisClient;
        this.lockKey = lockKey;
        this.expireInMilliseconds = expireInMilliseconds;
        this.acquired = false;
        this.value = UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * RedisLock构造，可以自己指定value
     *
     * @param redisClient          redis操作类
     * @param lockKey              锁key
     * @param expireInMilliseconds 有效期
     * @param value                指定存储的value，不能为空值
     */
    public RedisLock(IRedisClient redisClient, String lockKey, long expireInMilliseconds, String value) {
        if (Objects.isNull(value) || "".equals(value)) {
            throw new RuntimeException("value could not be null or empty");
        }
        this.redisClient = redisClient;
        this.lockKey = lockKey;
        this.expireInMilliseconds = expireInMilliseconds;
        this.acquired = false;
        this.value = value;
    }

    /**
     * 尝试获取分布式锁
     *
     * @return 获取结果
     */
    public boolean tryLock() {
        try {
            String result = redisClient.setnx(lockKey, value, expireInMilliseconds);
            acquired = "OK".equalsIgnoreCase(result);
            return acquired;
        } catch (Exception ex) {
            LOGGER.error("error when tryLock, key: " + this.lockKey, ex);
            return false;
        }
    }

    /**
     * 获取分布式锁
     *
     * @param waitInMilliseconds 尝试获取失败后等待时间
     * @param tryCount           尝试获取锁次数
     * @return 获取结果
     */
    public boolean tryLock(long waitInMilliseconds, int tryCount) {
        while (tryCount > 0) {
            acquired = tryLock();
            if (acquired) {
                break;
            }
            try {
                if (waitInMilliseconds > 0) {
                    TimeUnit.MILLISECONDS.sleep(waitInMilliseconds);
                }
            } catch (InterruptedException ex) {
                LOGGER.error("error when sleep in tryLock", ex);
            }
            tryCount--;
        }
        return acquired;
    }

    /**
     * 释放锁
     */
    public void releaseLock() {
        if (acquired) {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
            deleteWithRetry(3);
            acquired = false;
        }
    }

    public void extend(int seconds) {
        if (acquired) {
            try {
                this.redisClient.expire(this.lockKey, seconds);
            } catch (Exception ex) {
                LOGGER.error("error when extend lockKey: " + this.lockKey, ex);
            }
        }
    }

    /**
     * 释放锁
     *
     * @throws IOException IOException
     */
    @Override
    public void close() throws IOException {
        releaseLock();
    }

    public boolean ifAcquired() {
        return this.acquired;
    }

    private void deleteWithRetry(int tryCount) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        while (tryCount > 0) {
            try {
                redisClient.eval(luaScript, Collections.singletonList(this.lockKey), Collections.singletonList(this.value));
                break;
            } catch (Exception ex) {
                LOGGER.error("error when delete key: " + this.lockKey);
            }
            tryCount--;
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                LOGGER.warn("error when sleep", ex);
            }
        }
    }
}
