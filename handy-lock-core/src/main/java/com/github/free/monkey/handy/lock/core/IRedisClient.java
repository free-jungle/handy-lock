package com.github.free.monkey.handy.lock.core;

import java.util.List;

public interface IRedisClient {

    String setnx(String key, String value, long expiresInMillis);

    void expire(String key, int seconds);

    void eval(String luaScript, List<String> keys, List<String> args);
}
