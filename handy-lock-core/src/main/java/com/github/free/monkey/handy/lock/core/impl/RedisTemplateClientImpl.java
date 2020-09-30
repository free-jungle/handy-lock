package com.github.free.monkey.handy.lock.core.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.free.monkey.handy.lock.core.IRedisClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisTemplateClientImpl implements IRedisClient {

    private RedisTemplate<String, ?> client;

    public RedisTemplateClientImpl() {
    }

    public RedisTemplateClientImpl(RedisTemplate<String, ?> client) {
        this.client = client;
    }

    @Override
    public String setnx(String key,
                        String value,
                        long expiresInMillis) {
        RedisScript<String> redisScript = new DefaultRedisScript<>("return redis.call('set',KEYS[1],ARGV[1],ARGV[2],ARGV[3],ARGV[4])", String.class);
        return client.execute(redisScript, Collections.singletonList(key), value, "PX", String.valueOf(expiresInMillis), "NX");
    }

    @Override
    public void expire(String key,
                       int seconds) {
        client.expire(key, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void eval(String luaScript,
                     List<String> keys,
                     List<String> args) {
        client.execute(new DefaultRedisScript<>(luaScript, String.class), keys, args.toArray());
    }

}
