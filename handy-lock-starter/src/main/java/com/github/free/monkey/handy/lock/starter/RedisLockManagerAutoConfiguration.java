package com.github.free.monkey.handy.lock.starter;

import com.github.free.monkey.handy.lock.core.IRedisClient;
import com.github.free.monkey.handy.lock.core.RedisLockInteceptor;
import com.github.free.monkey.handy.lock.core.RedisLockManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisLockManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisLockManager redisLockManager(IRedisClient redisClient) {
        return new RedisLockManager(redisClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisLockInteceptor redisLockInteceptor(RedisLockManager redisLockManager) {
        return new RedisLockInteceptor(redisLockManager);
    }

}
