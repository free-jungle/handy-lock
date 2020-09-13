package com.github.tousy.lock.examples.service;

public interface IRedisOpService {

    void lockUseAnnotation();

    void lockUserAnnotationWithTryCount();

    void lockUserAnnotationWithSpel(String id);

    void lockUserAnnotationWithEmptyKey(String id);

    void lockUseLockManager(String id);

    void lockWithScheduleUseLockManager(String id);

}
