package com.questionproassignment.config;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public Cache<String, Object> cache() {
        CacheBuilderSpec cacheBuilderSpec = CacheBuilderSpec.parse("maximumSize=1000, expireAfterWrite=15m");
        Cache<String, Object> cache = CacheBuilder.from(cacheBuilderSpec).build();
        return cache;
    }
}
