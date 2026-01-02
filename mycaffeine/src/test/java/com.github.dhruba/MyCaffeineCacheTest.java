package com.github.dhruba;

import org.junit.jupiter.api.Test;

public class MyCaffeineCacheTest {

    @Test
    public void testPut() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
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
    public void testGetIfPresent() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
        cache.put("key", "value");
        assert cache.getIfPresent("key").equals("value");
    }
    @Test
    public void testGet() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
        cache.put("key", "value");
        assert cache.get("key").equals("value");
        assert cache.get("key2") == null;
        assert cache.getIfPresent("key2") == null;
    }
    @Test
    public void testEviction() {
        MyCaffeineCache<String, String> cache = new MyCaffeineCache<>();
        cache.put("key1", "value1");
        // TODO: test eviction mechanism
    }

}
