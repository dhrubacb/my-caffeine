package com.github.dhruba;

import org.junit.jupiter.api.Test;

public class MyCaffeineCacheTest {

    @Test
    public void testPut() {
        Cache<String, String> cache = new MyCaffeineCache<>();
        cache.put("key", "value");
        assert cache.get("key").equals("value");
    }
    @Test
    public void testPutIfAbsent() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
        cache.putIfAbsent("key", "value");
        assert cache.get("key").equals("value");
    }
    @Test
    public void testEviction() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
        cache.put("key1", "value1");
        // TODO: test eviction mechanism
    }

}
