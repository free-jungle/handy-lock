package com.github.free.monkey.handy.lock.core;

public class LockFailException extends RuntimeException {

    private String key;

    public LockFailException() {
    }

    public LockFailException(String key) {
        super("lockKey:" + key);
        this.key = key;
    }

    public LockFailException(String key, Throwable cause) {
        super("lockKey:" + key, cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
