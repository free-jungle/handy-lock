package com.github.free.jungle.handy.lock.starter;

import com.github.free.jungle.handy.lock.core.IRedisClient;
import com.github.free.jungle.handy.lock.core.impl.RedisTemplateClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisClientAutoConfiguration {

    @Configuration
    @ConditionalOnClass({RedisTemplate.class})
    static class RedisTemplateClientAutoConfiguration {

        @Bean
        @ConditionalOnBean({RedisTemplate.class})
        @ConditionalOnMissingBean
        public IRedisClient redisClient(
                RedisTemplate<String, ?> redisTemplate) {
            return new RedisTemplateClientImpl(redisTemplate);
        }
    }

}
