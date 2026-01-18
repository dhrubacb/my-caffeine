package com.github.dhruba;

import java.util.Random;

/**
 * Example usage of the AI-Enhanced Cache.
 */
public class Example {
    
    public static void main(String[] args) {
        System.out.println("=== AI-Enhanced W-TinyLFU Cache Example ===\n");
        
        // Example 1: Basic usage
        basicUsageExample();
        
        // Example 2: Workload simulation
        workloadSimulationExample();
        
        // Example 3: Pattern detection
        patternDetectionExample();
        
        // Example 4: AI vs Traditional comparison
        comparisonExample();
    }
    
    private static void basicUsageExample() {
        System.out.println("--- Example 1: Basic Usage ---");
        
        // Create cache with default configuration
        AIEnhancedCacheConfig config = AIEnhancedCacheConfig.builder()
                .maximumSize(100)
                .enableAI(true)
                .aiWeight(0.7)
                .build();
        
        AIEnhancedCache<String, String> cache = new AIEnhancedCache<>(config);
        
        // Put some entries
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        cache.put("user:3", "Charlie");
        
        // Get entries
        System.out.println("Get user:1: " + cache.get("user:1"));
        System.out.println("Get user:2: " + cache.get("user:2"));
        System.out.println("Get user:999: " + cache.get("user:999")); // Miss
        
        // Show statistics
        System.out.println("\nStatistics: " + cache.getStatistics());
        System.out.println();
    }
    
    private static void workloadSimulationExample() {
        System.out.println("--- Example 2: Workload Simulation ---");
        
        AIEnhancedCacheConfig config = AIEnhancedCacheConfig.builder()
                .maximumSize(1000)
                .enableAI(true)
                .aiWeight(0.7)
                .build();
        
        AIEnhancedCache<Integer, String> cache = new AIEnhancedCache<>(config);
        Random random = new Random(42);
        
        // Simulate 10,000 accesses with Zipfian distribution (80/20 rule)
        int totalAccesses = 10000;
        int hotSetSize = 200; // 20% of cache size
        
        System.out.println("Simulating " + totalAccesses + " accesses with hot set of " + hotSetSize + " items...");
        
        for (int i = 0; i < totalAccesses; i++) {
            int key;
            
            // 80% of accesses go to 20% of keys (hot set)
            if (random.nextDouble() < 0.8) {
                key = random.nextInt(hotSetSize);
            } else {
                key = hotSetSize + random.nextInt(4000);
            }
            
            String value = cache.get(key);
            if (value == null) {
                cache.put(key, "Value-" + key);
            }
        }
        
        System.out.println("Simulation complete!");
        System.out.println("Statistics: " + cache.getStatistics());
        System.out.println();
    }
    
    private static void patternDetectionExample() {
        System.out.println("--- Example 3: Pattern Detection ---");
        
        AIEnhancedCacheConfig config = AIEnhancedCacheConfig.builder()
                .maximumSize(500)
                .enableAI(true)
                .featureHistorySize(20)
                .build();
        
        AIEnhancedCache<String, String> cache = new AIEnhancedCache<>(config);
        
        // Create different access patterns
        
        // Hot spot pattern - frequently accessed
        System.out.println("Creating hot spot pattern...");
        for (int i = 0; i < 100; i++) {
            cache.put("hot:1", "Hot Item 1");
            cache.get("hot:1");
            cache.get("hot:1");
            cache.get("hot:1");
        }
        
        // Sequential scan pattern - accessed once
        System.out.println("Creating sequential scan pattern...");
        for (int i = 0; i < 100; i++) {
            cache.put("scan:" + i, "Scan Item " + i);
        }
        
        // Temporal pattern - accessed at intervals
        System.out.println("Creating temporal pattern...");
        for (int round = 0; round < 10; round++) {
            for (int i = 0; i < 5; i++) {
                cache.put("temporal:" + i, "Temporal Item " + i);
                cache.get("temporal:" + i);
            }
            try {
                Thread.sleep(10); // Small delay to create temporal pattern
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Pattern detection complete!");
        System.out.println("Statistics: " + cache.getStatistics());
        System.out.println();
    }
    
    private static void comparisonExample() {
        System.out.println("--- Example 4: AI vs Traditional Comparison ---");
        
        // Cache with AI enabled
        AIEnhancedCacheConfig aiConfig = AIEnhancedCacheConfig.builder()
                .maximumSize(500)
                .enableAI(true)
                .aiWeight(0.7)
                .build();
        
        AIEnhancedCache<Integer, String> aiCache = new AIEnhancedCache<>(aiConfig);
        
        // Cache with AI disabled (traditional frequency-based)
        AIEnhancedCacheConfig traditionalConfig = AIEnhancedCacheConfig.builder()
                .maximumSize(500)
                .enableAI(false)
                .build();
        
        AIEnhancedCache<Integer, String> traditionalCache = new AIEnhancedCache<>(traditionalConfig);
        
        // Run same workload on both caches
        Random random = new Random(42);
        int totalAccesses = 5000;
        
        System.out.println("Running workload on both caches...");
        
        for (int i = 0; i < totalAccesses; i++) {
            int key = generateZipfianKey(random, 100, 2000);
            
            // AI cache
            String aiValue = aiCache.get(key);
            if (aiValue == null) {
                aiCache.put(key, "Value-" + key);
            }
            
            // Traditional cache
            String tradValue = traditionalCache.get(key);
            if (tradValue == null) {
                traditionalCache.put(key, "Value-" + key);
            }
        }
        
        System.out.println("\nResults:");
        System.out.println("AI-Enhanced Cache:  " + aiCache.getStatistics());
        System.out.println("Traditional Cache:  " + traditionalCache.getStatistics());
        
        double aiHitRate = aiCache.getStatistics().getHitRate();
        double tradHitRate = traditionalCache.getStatistics().getHitRate();
        double improvement = ((aiHitRate - tradHitRate) / tradHitRate) * 100;
        
        System.out.printf("\nHit Rate Improvement: %.2f%%\n", improvement);
        System.out.println();
    }
    
    /**
     * Generate key following Zipfian distribution (power law)
     */
    private static int generateZipfianKey(Random random, int hotSetSize, int totalKeys) {
        double alpha = 1.5; // Zipfian parameter
        double c = 0.0;
        
        // Approximate Zipfian by using power law on uniform random
        double u = random.nextDouble();
        double rank = Math.pow(u, -1.0 / alpha);
        
        int key = (int) (rank * hotSetSize);
        if (key >= totalKeys) {
            key = random.nextInt(totalKeys);
        }
        
        return key;
    }
}
