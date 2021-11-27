package com.pawan.choure.jedisexamples.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.time.Duration;

import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;

@Configuration
@EnableCaching
@ComponentScan("com.choure.pawan.jedisexamples")
@PropertySource("classpath:redis.properties")
public class AppConfig {

    private @Value("${redis.host}") String redisHost;
    private @Value("${redis.port}") int redisPort;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @PostConstruct
    private void onPostConstruct() {
        if (springCacheRedisKeyPrefix != null) {
            springCacheRedisKeyPrefix = springCacheRedisKeyPrefix.trim();
        }
        if (springCacheRedisUseKeyPrefix && springCacheRedisKeyPrefix != null
                && !springCacheRedisKeyPrefix.isEmpty()) {
            cacheKeyPrefix = cacheName -> springCacheRedisKeyPrefix + "::" + cacheName + "::";
        } else {
            cacheKeyPrefix = CacheKeyPrefix.simple();
        }
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(poolConfig);
        connectionFactory.setUsePool(true);
        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(6379);


        return connectionFactory;
    }

    @Bean
    RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<Object, Object>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());

        return redisTemplate;
    }

    @Bean
    CacheManager cacheManager() {
        final RedisCacheManager cacheManager = RedisCacheManager.builder(jedisConnectionFactory())
                .cacheDefaults(defaultCacheConfig()
                        .computePrefixWith(cacheKeyPrefix)
                        .entryTtl(Duration.ofMillis(springCacheRedisTimeToLive))
                )
                .build();
        return cacheManager;
    }

    @Value(value = "${spring.cache.redis.key-prefix:}")
    private String springCacheRedisKeyPrefix;

    @Value("${spring.cache.redis.use-key-prefix:false}")
    private boolean springCacheRedisUseKeyPrefix;

    @Value("${spring.cache.redis.time-to-live:1200000}")
    private long springCacheRedisTimeToLive;

    private transient CacheKeyPrefix cacheKeyPrefix;
}

