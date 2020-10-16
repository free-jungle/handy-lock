## 简介

handy-lock是一个简易的redis分布式锁实现类库。它并不像redission实现的分布式锁那样严谨，但是简单易用，而且可以用于codis。

## 主要特性

* 兼容codis
* setnx原子加锁，lua脚本原子释放锁
* 每次加锁value用guid，不会存在释放别人的锁的问题
* 加锁和释放锁自带重试机制
* 提供了自动延期的锁，避免过期时间长度选择的困境

## 引用

```xml
<dependency>
    <groupId>com.github.free-jungle</groupId>
    <artifactId>handy-lock-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 配置

redis操作使用springboot的RedisTemplate实现，所以配置和spring.redis配置是一样的

```yml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

## 使用举例

### 例1: 使用RedisLockManager加锁[推荐]

```java

@Resource
private RedisLockManager redisLockManager;

public void lockUseLockManager(String id) {
    String lockKey = String.format("%s.%s#%s", this.getClass().getCanonicalName(), "lockUseLockManager", id);
    try (RedisLock redisLock = redisLockManager.fetchAndTryLock(lockKey, 5000, 1000, 2)) {
        TimeUnit.SECONDS.sleep(2L);
    } catch (LockFailException | IOException | InterruptedException ex) {
        LOGGER.error("error when lockUseLockManager", ex);
    }
}
```

### 例2: 注解方式加锁-静态key

```java
@RedisDistributedLockable(key = "com.github.free.jungle.lock.examples.service.impl.lockUseAnnotation",
        expireInMilliseconds = 10000, waitInMilliseconds = 10, tryCount = 1)
public void lockUseAnnotation() {
    LOGGER.info("start lockUseAnnotation");
    try {
        TimeUnit.MILLISECONDS.sleep(100L);
    } catch (Exception ex) {
        LOGGER.error("error when lockUseAnnotation", ex);
    }
    LOGGER.info("end lockUseAnnotation");
}
```

虽然提供了注解的使用方式，还是推荐直接用例1的方式，代码易读，易懂，用起来也很简单，没有比
注解麻烦。

### 例3: 注解方式加锁-动态key

```java
@RedisDistributedLockable(keySpel = "'com.github.free.jungle.lock.examples.service.impl.lockUserAnnotationWithSpel#'+#id",
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
```

适用于key需要根据入参动态拼装的情况，其中keySpel是spel表达式

### 例4: 自动延长过期时间的分布式锁[推荐]

上面的用法有一个困难的问题，就是过期时间参数(expireInMilliseconds)的配置可能很难，因为：

* 锁有效时间太短，任务还没执行完，redis就过期了，这样分布式执行就会产生并发的问题
* 锁有效期太长，极端情况当程序异常退出，没有正确释放锁，锁长时间无法获取，导致任务无法进行的问题

所以实现了一个能够自动延长锁有效时间的加锁方法，使用方法如下:

```java
@Resource
private RedisLockManager redisLockManager;

public void lockWithScheduleUseLockManager(String id) {
    String lockKey = String.format("%s.%s#%s", this.getClass().getCanonicalName(), "lockUseLockManager", id);
    try (RedisLock redisLock = redisLockManager.fetchAndTryLockWithSchedule(lockKey)) {
        TimeUnit.SECONDS.sleep(30L);
    } catch (LockFailException | IOException | InterruptedException ex) {
        LOGGER.error("error when lockUseLockManager", ex);
    }
}
```

## 详细方法说明

类[RedisLockManager](./handy-lock-core/src/main/java/com/github/free/jungle/handy/lock/core/RedisLockManager.java)
的方法有较为详尽的注释，请直接查看源码。

## 样例项目

handy-lock-examples是专门的使用样例项目，作为参考使用

## 已知问题

只是实现了客户端层面的简易分布式锁，所以无法处理由于redis服务端故障造成数据不一致的问题，需要
更严谨的分布式锁的情况建议用Redssion

## 希望对你有所帮助