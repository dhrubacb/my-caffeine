package com.github.dhruba;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics tracking for cache performance and AI metrics.
 */
public class CacheStatistics {
    
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong admissionCount = new AtomicLong(0);
    private final AtomicLong rejectionCount = new AtomicLong(0);
    private final AtomicLong totalAccessCount = new AtomicLong(0);
    
    // AI-specific metrics
    private final AtomicLong aiAdmissionCount = new AtomicLong(0);
    private final AtomicLong frequencyAdmissionCount = new AtomicLong(0);
    
    public void recordHit() {
        hitCount.incrementAndGet();
        totalAccessCount.incrementAndGet();
    }
    
    public void recordMiss() {
        missCount.incrementAndGet();
        totalAccessCount.incrementAndGet();
    }
    
    public void recordEviction() {
        evictionCount.incrementAndGet();
    }
    
    public void recordAdmission(boolean aiDecision) {
        admissionCount.incrementAndGet();
        if (aiDecision) {
            aiAdmissionCount.incrementAndGet();
        } else {
            frequencyAdmissionCount.incrementAndGet();
        }
    }
    
    public void recordRejection() {
        rejectionCount.incrementAndGet();
    }
    
    public long getHitCount() {
        return hitCount.get();
    }
    
    public long getMissCount() {
        return missCount.get();
    }
    
    public long getEvictionCount() {
        return evictionCount.get();
    }
    
    public long getAdmissionCount() {
        return admissionCount.get();
    }
    
    public long getRejectionCount() {
        return rejectionCount.get();
    }
    
    public long getTotalAccessCount() {
        return totalAccessCount.get();
    }
    
    public long getAiAdmissionCount() {
        return aiAdmissionCount.get();
    }
    
    public long getFrequencyAdmissionCount() {
        return frequencyAdmissionCount.get();
    }
    
    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total;
    }
    
    public double getMissRate() {
        return 1.0 - getHitRate();
    }
    
    public double getAdmissionRate() {
        long total = admissionCount.get() + rejectionCount.get();
        return total == 0 ? 0.0 : (double) admissionCount.get() / total;
    }
    
    public double getAiInfluenceRate() {
        long total = admissionCount.get();
        return total == 0 ? 0.0 : (double) aiAdmissionCount.get() / total;
    }
    
    public void reset() {
        hitCount.set(0);
        missCount.set(0);
        evictionCount.set(0);
        admissionCount.set(0);
        rejectionCount.set(0);
        totalAccessCount.set(0);
        aiAdmissionCount.set(0);
        frequencyAdmissionCount.set(0);
    }
    
    @Override
    public String toString() {
        return String.format(
            "CacheStatistics{hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, " +
            "admissions=%d, rejections=%d, admissionRate=%.2f%%, aiInfluence=%.2f%%}",
            hitCount.get(),
            missCount.get(),
            getHitRate() * 100,
            evictionCount.get(),
            admissionCount.get(),
            rejectionCount.get(),
            getAdmissionRate() * 100,
            getAiInfluenceRate() * 100
        );
    }
}
