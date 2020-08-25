package com.github.tousy.lock.core;

public class LockFailException extends Throwable {

    private String key;

    public LockFailException(String key) {
        super("lockKey:" + key);
        this.key = key;
    }
}
