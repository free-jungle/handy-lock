package com.github.free.jungle.handy.lock.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.Beta;

public class RedisLockManager implements Closeable {

    private final IRedisClient redisClient;

    public RedisLockManager(IRedisClient redisClient) {
        this.redisClient = redisClient;
        this.schedule = Executors.newScheduledThreadPool(4);
    }

    private ScheduledExecutorService schedule;

    public RedisLock fetch(String lockKey, long expireInMilliseconds) {
        return new RedisLock(this.redisClient, lockKey, expireInMilliseconds);
    }

    public RedisLock fetch(String lockKey, long expireInMilliseconds, String value) {
        if (Objects.isNull(value) || "".equals(value)) {
            return fetch(lockKey, expireInMilliseconds);
        }
        return new RedisLock(this.redisClient, lockKey, expireInMilliseconds, value);
    }

    /**
     * 获取RedisLock对象并尝试取得锁
     *
     * @param lockKey              key
     * @param expireInMilliseconds 有效期，单位毫秒
     * @return 返回RedisLock对象
     * @throws LockFailException 锁获取失败，抛出异常
     */
    public RedisLock fetchAndTryLock(String lockKey, long expireInMilliseconds) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, expireInMilliseconds);
        boolean success = redisLock.tryLock();
        if (success) {
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试取得锁
     *
     * @param lockKey              key
     * @param expireInMilliseconds 有效期，单位毫秒
     * @param value                锁的value
     * @return 返回RedisLock对象
     * @throws LockFailException 锁获取失败，抛出异常
     */
    public RedisLock fetchAndTryLock(String lockKey,
                                     long expireInMilliseconds,
                                     String value) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, expireInMilliseconds, value);
        boolean success = redisLock.tryLock();
        if (success) {
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试取得锁
     *
     * @param lockKey              key
     * @param expireInMilliseconds 有效时长, 单位毫秒
     * @param waitInMilliseconds   两次获取之间的等待时长，单位毫秒
     * @param tryCount             尝试获取次数
     * @return RedisLock对象
     * @throws LockFailException 锁获取失败时，抛出异常
     */
    public RedisLock fetchAndTryLock(String lockKey, long expireInMilliseconds, long waitInMilliseconds, int tryCount) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, expireInMilliseconds);
        boolean success = redisLock.tryLock(waitInMilliseconds, tryCount);
        if (success) {
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试取得锁
     *
     * @param lockKey              key
     * @param expireInMilliseconds 有效时长, 单位毫秒
     * @param value                存入的值
     * @param waitInMilliseconds   两次获取之间的等待时长，单位毫秒
     * @param tryCount             尝试获取次数
     * @return RedisLock对象
     * @throws LockFailException 当获取锁失败时
     */
    public RedisLock fetchAndTryLock(String lockKey,
                                     long expireInMilliseconds,
                                     String value,
                                     long waitInMilliseconds,
                                     int tryCount) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, expireInMilliseconds, value);
        boolean success = redisLock.tryLock(waitInMilliseconds, tryCount);
        if (success) {
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试获得锁，如果成功获得锁，起一个定期延长锁有效期的任务,所以不需要显示设置key的有效期，有默认配置
     * 这个任务的作用是两个: 1. 解决锁有效时间太短，任务还没执行完，redis就过期了，这样分布式执行就会产生并发的问题
     * 2. 解决锁有效期太长，当程序异常退出，没有正确释放锁，锁长时间无法获取，导致任务无法进行的问题
     *
     * @param lockKey key
     * @return RedisLock对象
     * @throws LockFailException 锁获取失败时，抛出异常
     */
    @Beta
    public RedisLock fetchAndTryLockWithSchedule(String lockKey) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, 30 * 1000);
        boolean success = redisLock.tryLock();
        if (success) {
            scheduleExtendTimer(redisLock);
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试获得锁，如果成功获得锁，起一个定期延长锁有效期的任务,所以不需要显示设置key的有效期，有默认配置
     * 这个任务的作用是两个: 1. 解决锁有效时间太短，任务还没执行完，redis就过期了，这样分布式执行就会产生并发的问题
     * 2. 解决锁有效期太长，当程序异常退出，没有正确释放锁，锁长时间无法获取，导致任务无法进行的问题
     *
     * @param lockKey key
     * @param value   value
     * @return RedisLock对象
     * @throws LockFailException 锁获取失败时，抛出异常
     */
    @Beta
    public RedisLock fetchAndTryLockWithSchedule(String lockKey, String value) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, 30 * 1000, value);
        boolean success = redisLock.tryLock();
        if (success) {
            scheduleExtendTimer(redisLock);
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    /**
     * 获取RedisLock对象并尝试获得锁，如果成功获得锁，起一个定期延长锁有效期的任务,所以不需要显示设置key的有效期，有默认配置
     * 这个任务的作用是两个: 1. 解决锁有效时间太短，任务还没执行完，redis就过期了，这样分布式执行就会产生并发的问题
     * 2. 解决锁有效期太长，当程序异常退出，没有正确释放锁，锁长时间无法获取，导致任务无法进行的问题
     *
     * @param lockKey            key
     * @param waitInMilliseconds 两次获取之间的等待时长，单位毫秒
     * @param tryCount           尝试获取次数
     * @return RedisLock对象
     * @throws LockFailException 锁获取失败时，抛出异常
     */
    @Beta
    public RedisLock fetchAndTryLockWithSchedule(String lockKey, long waitInMilliseconds, int tryCount) throws LockFailException {
        RedisLock redisLock = fetch(lockKey, 30 * 1000);
        boolean success = redisLock.tryLock(waitInMilliseconds, tryCount);
        if (success) {
            scheduleExtendTimer(redisLock);
            return redisLock;
        }
        throw new LockFailException(lockKey);
    }

    private void scheduleExtendTimer(RedisLock redisLock) {
        ScheduledFuture scheduledFuture = schedule.scheduleAtFixedRate(() -> {
            if (redisLock.ifAcquired()) {
                redisLock.extend(30);
            }
        }, 10, 10, TimeUnit.SECONDS);
        redisLock.setScheduledFuture(scheduledFuture);
    }

    @Override
    public void close() throws IOException {
        if (this.schedule != null) {
            this.schedule.shutdown();
        }
    }
}
