package com.github.tousy.lock.examples.service.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;

import com.github.tousy.lock.core.LockFailException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisOpServiceTest {

    private static final Logger log = LoggerFactory.getLogger(RedisOpServiceTest.class);

    @Resource
    private RedisOpService redisOpService;

    @Test
    public void lockUserAnnotationWithTryCount() throws Exception {
        int size = 20;
        CountDownLatch latch = new CountDownLatch(size);
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (int i = 0; i < size; i++) {
            int index = i;
            service.execute(() -> {
                try {
                    redisOpService.lockUserAnnotationWithTryCount();
                    log.info("lockSuccess: i:{}", index);
                } catch (LockFailException e) {
                    log.error("lockFailed: i:{}", index);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30L, TimeUnit.SECONDS);
        System.exit(0);
    }
}
