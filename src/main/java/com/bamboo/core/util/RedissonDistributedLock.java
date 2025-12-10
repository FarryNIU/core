package com.bamboo.core.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class RedissonDistributedLock {
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 加锁并执行业务逻辑
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 持有锁时间
     * @param unit 时间单位
     * @param supplier 业务逻辑
     * @return 执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, 
                                 long leaseTime, TimeUnit unit,
                                 Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试加锁，最多等待waitTime，上锁以后leaseTime自动解锁
            boolean locked = lock.tryLock(waitTime, leaseTime, unit);
            if (!locked) {
                throw new RuntimeException("获取锁失败");
            }
            // 执行业务逻辑
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁时被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    /**
     * 可重入锁执行业务
     */
    public void executeWithReentrantLock(String lockKey, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            task.run();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}