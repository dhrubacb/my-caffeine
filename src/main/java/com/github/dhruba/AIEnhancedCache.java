package com.github.dhruba;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AI-Enhanced W-TinyLFU Cache implementation.
 * 
 * Architecture:
 * - Window Cache (1%): LRU admission window for new entries
 * - Probationary Segment (20%): Entries promoted from window
 * - Protected Segment (80%): Hot entries with proven value
 * - AI-Enhanced Admission Policy: ML-based decision making
 */
public class AIEnhancedCache<K, V> {
    
    private final ConcurrentMap<K, CacheEntry<K, V>> map;
    private final int maximumSize;
    private final int windowSize;
    private final int probationarySize;
    private final int protectedSize;
    private final int featureHistorySize;
    
    // Cache segments
    private final LRUQueue<K, V> windowQueue;
    private final LRUQueue<K, V> probationaryQueue;
    private final LRUQueue<K, V> protectedQueue;
    
    // AI components
    private final CountMinSketch frequencySketch;
    private final AIEnhancedAdmissionPolicy admissionPolicy;
    private final CacheStatistics statistics;
    
    // Concurrency control
    private final ReentrantLock evictionLock;
    
    public AIEnhancedCache(AIEnhancedCacheConfig config) {
        this.maximumSize = config.getMaximumSize();
        this.windowSize = config.getWindowSize();
        this.probationarySize = (int) ((maximumSize - windowSize) * 0.2);
        this.protectedSize = maximumSize - windowSize - probationarySize;
        this.featureHistorySize = config.getFeatureHistorySize();
        
        this.map = new ConcurrentHashMap<>();
        this.windowQueue = new LRUQueue<>();
        this.probationaryQueue = new LRUQueue<>();
        this.protectedQueue = new LRUQueue<>();
        
        // Initialize AI components
        this.frequencySketch = new CountMinSketch(maximumSize * 4, 4);
        AIPredictor predictor = new SimplePredictorModel(
            config.getMaxTrainingExamples(),
            config.getLearningRate()
        );
        this.admissionPolicy = new AIEnhancedAdmissionPolicy(
            predictor,
            frequencySketch,
            config.getAiWeight(),
            config.isEnableAI()
        );
        
        this.statistics = new CacheStatistics();
        this.evictionLock = new ReentrantLock();
    }
    
    /**
     * Get a value from the cache
     */
    public V get(K key) {
        CacheEntry<K, V> entry = map.get(key);
        
        if (entry != null) {
            // Cache hit
            entry.recordAccess();
            frequencySketch.increment(key);
            statistics.recordHit();
            admissionPolicy.recordAccess(entry, true);
            
            // Promote entry if needed
            promoteEntry(entry);
            
            return entry.getValue();
        } else {
            // Cache miss
            statistics.recordMiss();
            return null;
        }
    }
    
    /**
     * Put a key-value pair into the cache
     */
    public void put(K key, V value) {
        CacheEntry<K, V> existingEntry = map.get(key);
        
        if (existingEntry != null) {
            // Update existing entry
            existingEntry.setValue(value);
            existingEntry.recordAccess();
            frequencySketch.increment(key);
            promoteEntry(existingEntry);
            return;
        }
        
        // Create new entry
        CacheEntry<K, V> newEntry = new CacheEntry<>(key, value, featureHistorySize);
        frequencySketch.increment(key);
        
        evictionLock.lock();
        try {
            // Check if cache is full
            if (map.size() >= maximumSize) {
                evictAndAdmit(newEntry);
            } else {
                // Cache not full, add to window
                map.put(key, newEntry);
                windowQueue.add(newEntry);
            }
            
            // Periodic frequency sketch reset
            if (frequencySketch.shouldReset(maximumSize * 10L)) {
                frequencySketch.reset();
            }
        } finally {
            evictionLock.unlock();
        }
    }
    
    /**
     * Remove a key from the cache
     */
    public void remove(K key) {
        evictionLock.lock();
        try {
            CacheEntry<K, V> entry = map.remove(key);
            if (entry != null) {
                removeFromQueues(entry);
            }
        } finally {
            evictionLock.unlock();
        }
    }
    
    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * Get current cache size
     */
    public int size() {
        return map.size();
    }
    
    /**
     * Clear the cache
     */
    public void clear() {
        evictionLock.lock();
        try {
            map.clear();
            windowQueue.clear();
            probationaryQueue.clear();
            protectedQueue.clear();
        } finally {
            evictionLock.unlock();
        }
    }
    
    /**
     * Promote an entry to a higher segment if accessed
     */
    private void promoteEntry(CacheEntry<K, V> entry) {
        evictionLock.lock();
        try {
            if (windowQueue.contains(entry)) {
                // Entry in window - move to end of window (MRU)
                windowQueue.moveToEnd(entry);
            } else if (probationaryQueue.contains(entry)) {
                // Promote from probationary to protected
                probationaryQueue.remove(entry);
                
                // If protected is full, demote LRU to probationary
                if (protectedQueue.size() >= protectedSize) {
                    CacheEntry<K, V> demoted = protectedQueue.removeFirst();
                    if (demoted != null) {
                        probationaryQueue.add(demoted);
                    }
                }
                
                protectedQueue.add(entry);
            } else if (protectedQueue.contains(entry)) {
                // Already in protected - move to end (MRU)
                protectedQueue.moveToEnd(entry);
            }
        } finally {
            evictionLock.unlock();
        }
    }
    
    /**
     * Evict an entry and decide whether to admit the new entry
     */
    private void evictAndAdmit(CacheEntry<K, V> newEntry) {
        // Try to evict from window first
        CacheEntry<K, V> windowVictim = windowQueue.getFirst();
        
        if (windowVictim != null) {
            // Decide if window victim should be promoted to main cache
            CacheEntry<K, V> probationaryVictim = probationaryQueue.getFirst();
            
            boolean shouldAdmitToMain = admissionPolicy.shouldAdmit(windowVictim, probationaryVictim);
            
            if (shouldAdmitToMain) {
                // Admit window victim to probationary
                windowQueue.removeFirst();
                
                // Evict from probationary if full
                if (probationaryQueue.size() >= probationarySize) {
                    CacheEntry<K, V> evicted = probationaryQueue.removeFirst();
                    if (evicted != null) {
                        map.remove(evicted.getKey());
                        statistics.recordEviction();
                    }
                }
                
                probationaryQueue.add(windowVictim);
                statistics.recordAdmission(admissionPolicy.getPredictor().getConfidence() > 0.5);
            } else {
                // Reject window victim
                windowQueue.removeFirst();
                map.remove(windowVictim.getKey());
                statistics.recordRejection();
                statistics.recordEviction();
            }
        }
        
        // Add new entry to window
        map.put(newEntry.getKey(), newEntry);
        windowQueue.add(newEntry);
    }
    
    /**
     * Remove entry from all queues
     */
    private void removeFromQueues(CacheEntry<K, V> entry) {
        windowQueue.remove(entry);
        probationaryQueue.remove(entry);
        protectedQueue.remove(entry);
    }
    
    /**
     * Simple LRU queue implementation using doubly-linked list
     */
    private static class LRUQueue<K, V> {
        private CacheEntry<K, V> head;
        private CacheEntry<K, V> tail;
        private int size;
        
        public void add(CacheEntry<K, V> entry) {
            if (tail == null) {
                head = tail = entry;
                entry.setPrev(null);
                entry.setNext(null);
            } else {
                tail.setNext(entry);
                entry.setPrev(tail);
                entry.setNext(null);
                tail = entry;
            }
            size++;
        }
        
        public CacheEntry<K, V> removeFirst() {
            if (head == null) {
                return null;
            }
            
            CacheEntry<K, V> removed = head;
            head = head.getNext();
            
            if (head == null) {
                tail = null;
            } else {
                head.setPrev(null);
            }
            
            removed.setNext(null);
            removed.setPrev(null);
            size--;
            return removed;
        }
        
        public void remove(CacheEntry<K, V> entry) {
            if (entry.getPrev() != null) {
                entry.getPrev().setNext(entry.getNext());
            } else {
                head = entry.getNext();
            }
            
            if (entry.getNext() != null) {
                entry.getNext().setPrev(entry.getPrev());
            } else {
                tail = entry.getPrev();
            }
            
            entry.setNext(null);
            entry.setPrev(null);
            size--;
        }
        
        public void moveToEnd(CacheEntry<K, V> entry) {
            if (entry == tail) {
                return;
            }
            
            remove(entry);
            add(entry);
        }
        
        public CacheEntry<K, V> getFirst() {
            return head;
        }
        
        public boolean contains(CacheEntry<K, V> entry) {
            CacheEntry<K, V> current = head;
            while (current != null) {
                if (current == entry) {
                    return true;
                }
                current = current.getNext();
            }
            return false;
        }
        
        public int size() {
            return size;
        }
        
        public void clear() {
            head = null;
            tail = null;
            size = 0;
        }
    }
}
