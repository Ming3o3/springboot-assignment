package com.gzist.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Spring Cache配置 - Redis缓存管理
 *
 * @author GZIST
 * @since 2025-12-26
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置Redis缓存管理器
     * 使用Redis作为缓存存储，提高数据查询性能
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置自定义ObjectMapper处理Java 8时间类型和多态类型
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 启用默认类型信息，解决接口/抽象类反序列化问题
        // 这样序列化时会保存类型信息，反序列化时能正确还原为具体类型（如IPage -> Page）
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 配置序列化器
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = 
                new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 配置缓存策略
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存过期时间为30分钟
                .entryTtl(Duration.ofMinutes(30))
                // 禁用缓存空值
                .disableCachingNullValues()
                // 设置key序列化器
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // 设置value序列化器
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonRedisSerializer)
                )
                // 设置缓存key前缀（避免不同应用key冲突）
                .computePrefixWith(cacheName -> "product-system:" + cacheName + ":");

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
