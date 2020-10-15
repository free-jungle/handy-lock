package com.github.free.jungle.handy.lock.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisDistributedLockable {

    // 原始格式key
    String key() default "";

    // SpEL格式key，解析优先级高于key
    String keySpel() default "";

    long expireInMilliseconds();

    long waitInMilliseconds() default 0;

    int tryCount() default 1;
}
