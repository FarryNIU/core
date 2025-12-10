package com.bamboo.core.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@Slf4j
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

    public <T> T executeWithLockAndPersistence(
            String lockKey,
            long waitTime,
            long leaseTime,
            TimeUnit unit,
            Supplier<T> redisOperation,
            Supplier<T> dbOperation) {

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            // 尝试获取锁
            locked = lock.tryLock(waitTime, leaseTime, unit);
            if (!locked) {
                throw new RuntimeException("获取分布式锁失败");
            }

            log.info("成功获取分布式锁: {}", lockKey);

            // 第一步：执行Redis操作（如扣减库存）
            T redisResult = redisOperation.get();

            // 第二步：执行数据库持久化
            T dbResult = dbOperation.get();

            log.info("Redis和数据库操作均成功完成");
            return dbResult;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁时被中断", e);
        } catch (Exception e) {
            log.error("执行业务逻辑时发生异常", e);
            throw e;
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放分布式锁: {}", lockKey);
            }
        }
    }
}