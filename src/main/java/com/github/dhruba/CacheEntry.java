package com.github.dhruba;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced cache entry with AI-related metadata for predictive caching.
 */
public class CacheEntry<K, V> {
    private final K key;
    private V value;
    private long accessCount;
    private long lastAccessTime;
    private long creationTime;
    private long writeTime;
    private final List<Long> accessTimestamps;
    private double predictedValue;
    private AccessPattern pattern;
    private CacheEntry<K, V> next;
    private CacheEntry<K, V> prev;
    private final int maxHistorySize;

    public CacheEntry(K key, V value, int maxHistorySize) {
        this.key = key;
        this.value = value;
        this.accessCount = 0;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = this.creationTime;
        this.writeTime = this.creationTime;
        this.accessTimestamps = new ArrayList<>();
        this.predictedValue = 0.0;
        this.pattern = AccessPattern.UNKNOWN;
        this.maxHistorySize = maxHistorySize;
        recordAccess();
    }

    public void recordAccess() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
        
        // Keep only recent access history
        accessTimestamps.add(lastAccessTime);
        if (accessTimestamps.size() > maxHistorySize) {
            accessTimestamps.remove(0);
        }
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
        this.writeTime = System.currentTimeMillis();
        this.lastAccessTime = this.writeTime;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getWriteTime() {
        return writeTime;
    }

    public List<Long> getAccessTimestamps() {
        return new ArrayList<>(accessTimestamps);
    }

    public double getPredictedValue() {
        return predictedValue;
    }

    public void setPredictedValue(double predictedValue) {
        this.predictedValue = predictedValue;
    }

    public AccessPattern getPattern() {
        return pattern;
    }

    public void setPattern(AccessPattern pattern) {
        this.pattern = pattern;
    }

    public CacheEntry<K, V> getNext() {
        return next;
    }

    public void setNext(CacheEntry<K, V> next) {
        this.next = next;
    }

    public CacheEntry<K, V> getPrev() {
        return prev;
    }

    public void setPrev(CacheEntry<K, V> prev) {
        this.prev = prev;
    }

    /**
     * Calculate the access rate (accesses per second)
     */
    public double getAccessRate() {
        long ageInSeconds = Math.max(1, (System.currentTimeMillis() - creationTime) / 1000);
        return (double) accessCount / ageInSeconds;
    }

    /**
     * Calculate variance in access intervals to detect pattern regularity
     */
    public double getAccessVariance() {
        if (accessTimestamps.size() < 2) {
            return 0.0;
        }

        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < accessTimestamps.size(); i++) {
            intervals.add(accessTimestamps.get(i) - accessTimestamps.get(i - 1));
        }

        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double variance = intervals.stream()
                .mapToDouble(interval -> Math.pow(interval - mean, 2))
                .average()
                .orElse(0.0);

        return variance;
    }

    @Override
    public String toString() {
        return String.format("CacheEntry{key=%s, accessCount=%d, predictedValue=%.3f, pattern=%s}",
                key, accessCount, predictedValue, pattern);
    }
}
