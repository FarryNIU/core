package com.bamboo.core.service;

import com.bamboo.core.mq.bean.BookRequest;
import com.bamboo.core.util.RedissonDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BookService {

    @Autowired
    private RedissonDistributedLock redissonLock;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CONTRACT_KEY_PREFIX = "contract:";
    private static final String LOCK_KEY_PREFIX = "lock:contract:";

    public boolean book(BookRequest bookRequest){
        log.info("开始预约课程",bookRequest.getContractId());
        return bookWithRedisson(bookRequest.getContractId());
    }

    public boolean bookWithRedisson(String contractId) {
        String lockKey = LOCK_KEY_PREFIX + contractId;
        String contractKey = CONTRACT_KEY_PREFIX + contractId;

        return redissonLock.executeWithLock(lockKey, 10, 30,
                TimeUnit.SECONDS, () -> {
                    // 获取当前状态
                    Object stockObj = redisTemplate.opsForValue().get(contractKey);
                    if (stockObj == null) {
                        throw new RuntimeException("课程不存在");
                    }

                    String status = stockObj.toString();
                    if ("Booked".equals(status)) {
                        log.warn("已被抢先预定",
                                contractId, status);
                        return false;
                    }
                    else if("Available".equals(status)){
                        redisTemplate.opsForValue().set(contractKey, "Available");
                    }
                    log.info("预约成功，课程ID: {}", contractId);
                    return true;
                });
    }
}
