package com.bamboo.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig {

    /**
     * 配置 ObjectMapper - 用于 JSON 序列化
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 基本配置
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 可选：不序列化 null 值
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 支持多态类型（如果需要）
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    /**
     * 配置 RedisTemplate - Key用String，Value用JSON
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. Key序列化：String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);           // 普通键
        template.setHashKeySerializer(stringSerializer);       // Hash键

        // 2. Value序列化：JSON
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);          // 普通值
        template.setHashValueSerializer(jsonSerializer);      // Hash值

        // 3. 设置默认序列化器
        template.setDefaultSerializer(jsonSerializer);

        // 4. 必须调用！
        template.afterPropertiesSet();

        log.info("RedisTemplate 配置完成: Key->String, Value->JSON");
        return template;
    }

    /**
     * 配置 CacheManager - 与 RedisTemplate 保持一致
     */
    @Bean
    @Primary
    public CacheManager cacheManager(
            RedisConnectionFactory factory,
            ObjectMapper objectMapper) {

        // 使用与 RedisTemplate 相同的序列化器
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // 配置缓存
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))                   // 默认30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))   // Key: String
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer))               // Value: JSON
                .disableCachingNullValues()                         // 不缓存null
                .computePrefixWith(cacheName -> cacheName + "::");  // 前缀格式

        // 可以为不同的缓存区域设置不同配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // users 缓存：1小时
        cacheConfigurations.put("users", config.entryTtl(Duration.ofHours(1)));

        // products 缓存：2小时
        cacheConfigurations.put("products", config.entryTtl(Duration.ofHours(2)));

        // configs 缓存：永不过期
        cacheConfigurations.put("configs", config.entryTtl(Duration.ZERO));

        // 创建 CacheManager
        RedisCacheManager cacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .enableStatistics()  // 启用统计（可选）
                .build();

        log.info("CacheManager 配置完成: Key->String, Value->JSON");
        return cacheManager;
    }

    /**
     * 可选：StringRedisTemplate（纯字符串操作）
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}