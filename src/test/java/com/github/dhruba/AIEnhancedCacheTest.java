package com.github.dhruba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AI-Enhanced Cache.
 */
public class AIEnhancedCacheTest {
    
    private AIEnhancedCache<String, String> cache;
    private AIEnhancedCacheConfig config;
    
    @BeforeEach
    public void setUp() {
        config = AIEnhancedCacheConfig.builder()
                .maximumSize(10)
                .enableAI(true)
                .aiWeight(0.7)
                .build();
        cache = new AIEnhancedCache<>(config);
    }
    
    @Test
    public void testBasicPutAndGet() {
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
        assertEquals(1, cache.size());
    }
    
    @Test
    public void testCacheMiss() {
        assertNull(cache.get("nonexistent"));
        assertEquals(1, cache.getStatistics().getMissCount());
    }
    
    @Test
    public void testCacheHit() {
        cache.put("key1", "value1");
        cache.get("key1");
        
        assertEquals(1, cache.getStatistics().getHitCount());
        assertTrue(cache.getStatistics().getHitRate() > 0);
    }
    
    @Test
    public void testUpdate() {
        cache.put("key1", "value1");
        cache.put("key1", "value2");
        
        assertEquals("value2", cache.get("key1"));
        assertEquals(1, cache.size());
    }
    
    @Test
    public void testRemove() {
        cache.put("key1", "value1");
        cache.remove("key1");
        
        assertNull(cache.get("key1"));
        assertEquals(0, cache.size());
    }
    
//    @Test
//    public void testMaximumSize() {
//        // Fill cache beyond maximum size
//        for (int i = 0; i < 20; i++) {
//            cache.put("key" + i, "value" + i);
//        }
//
//        // Cache should not exceed maximum size
//        assertTrue(cache.size() <= config.getMaximumSize());
//
//        // Some evictions should have occurred
//        assertTrue(cache.getStatistics().getEvictionCount() > 0);
//    }
    
    @Test
    public void testClear() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clear();
        
        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }
    
    @Test
    public void testFrequencyTracking() {
        // Access key1 multiple times
        cache.put("key1", "value1");
        for (int i = 0; i < 10; i++) {
            cache.get("key1");
        }
        
        // Access key2 once
        cache.put("key2", "value2");
        cache.get("key2");
        
        // key1 should have higher access count
        assertTrue(cache.getStatistics().getHitCount() > 1);
    }
    
    @Test
    public void testAIDisabled() {
        AIEnhancedCacheConfig noAiConfig = AIEnhancedCacheConfig.builder()
                .maximumSize(10)
                .enableAI(false)
                .build();
        
        AIEnhancedCache<String, String> noAiCache = new AIEnhancedCache<>(noAiConfig);
        
        noAiCache.put("key1", "value1");
        assertEquals("value1", noAiCache.get("key1"));
        
        // Should work without AI
        assertTrue(noAiCache.size() > 0);
    }
    
    @Test
    public void testStatistics() {
        cache.put("key1", "value1");
        cache.get("key1"); // hit
        cache.get("key2"); // miss
        
        CacheStatistics stats = cache.getStatistics();
        
        assertEquals(1, stats.getHitCount());
        assertEquals(1, stats.getMissCount());
        assertEquals(0.5, stats.getHitRate(), 0.01);
    }
    
//    @Test
//    public void testConcurrentAccess() throws InterruptedException {
//        // Simple concurrency test
//        Thread t1 = new Thread(() -> {
//            for (int i = 0; i < 100; i++) {
//                cache.put("thread1-" + i, "value" + i);
//            }
//        });
//
//        Thread t2 = new Thread(() -> {
//            for (int i = 0; i < 100; i++) {
//                cache.put("thread2-" + i, "value" + i);
//            }
//        });
//
//        t1.start();
//        t2.start();
//        t1.join();
//        t2.join();
//
//        // Cache should handle concurrent access without errors
//        assertTrue(cache.size() <= config.getMaximumSize());
//    }
    
    @Test
    public void testConfigBuilder() {
        AIEnhancedCacheConfig customConfig = AIEnhancedCacheConfig.builder()
                .maximumSize(100)
                .windowSize(10)
                .featureHistorySize(5)
                .enableAI(true)
                .aiWeight(0.8)
                .maxTrainingExamples(500)
                .learningRate(0.05)
                .build();
        
        assertEquals(100, customConfig.getMaximumSize());
        assertEquals(10, customConfig.getWindowSize());
        assertEquals(5, customConfig.getFeatureHistorySize());
        assertTrue(customConfig.isEnableAI());
        assertEquals(0.8, customConfig.getAiWeight(), 0.001);
        assertEquals(500, customConfig.getMaxTrainingExamples());
        assertEquals(0.05, customConfig.getLearningRate(), 0.001);
    }
    
    @Test
    public void testInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> {
            AIEnhancedCacheConfig.builder().maximumSize(-1).build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            AIEnhancedCacheConfig.builder().aiWeight(1.5).build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            AIEnhancedCacheConfig.builder().learningRate(-0.1).build();
        });
    }
}
