package com.github.free.monkey.handy.lock.starter;


import com.github.free.monkey.handy.lock.core.RedisLockInteceptor;
import com.github.free.monkey.handy.lock.core.aspectj.RedisLockAspect;
import org.aspectj.lang.Aspects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
@ConditionalOnClass({Aspects.class})
public class RedisLockAopAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisLockAspect redisLockAspect(RedisLockInteceptor redisLockInteceptor) {
        return new RedisLockAspect(redisLockInteceptor);
    }

}
