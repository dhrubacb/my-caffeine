package com.github.dhruba;

import java.util.Random;

/**
 * Count-Min Sketch for approximate frequency counting with minimal memory overhead.
 * Uses 4-bit counters as in Caffeine's implementation.
 */
public class CountMinSketch {
    private final int depth;  // Number of hash functions
    private final int width;  // Number of counters per row
    private final byte[][] table;
    private final long[] seeds;
    private long size;
    private static final int MAX_COUNT = 15; // 4-bit counter max value

    public CountMinSketch(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.table = new byte[depth][width];
        this.seeds = new long[depth];
        this.size = 0;

        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < depth; i++) {
            seeds[i] = random.nextLong();
        }
    }

    /**
     * Increment the count for a key
     */
    public void increment(Object key) {
        int hash = key.hashCode();
        size++;

        for (int i = 0; i < depth; i++) {
            int index = hash(hash, seeds[i]) % width;
            if (index < 0) index += width;
            
            byte current = table[i][index];
            if (current < MAX_COUNT) {
                table[i][index] = (byte) (current + 1);
            }
        }
    }

    /**
     * Estimate the frequency of a key
     */
    public int estimate(Object key) {
        int hash = key.hashCode();
        int min = MAX_COUNT;

        for (int i = 0; i < depth; i++) {
            int index = hash(hash, seeds[i]) % width;
            if (index < 0) index += width;
            
            int count = table[i][index] & 0xFF;
            min = Math.min(min, count);
        }

        return min;
    }

    /**
     * Reset (age) all counters by halving them
     */
    public void reset() {
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                table[i][j] = (byte) (table[i][j] >>> 1);
            }
        }
        size = size / 2;
    }

    /**
     * Check if reset is needed (when size exceeds threshold)
     */
    public boolean shouldReset(long threshold) {
        return size >= threshold;
    }

    public long getSize() {
        return size;
    }

    /**
     * Hash function combining the key hash with a seed
     */
    private int hash(int keyHash, long seed) {
        long h = keyHash ^ seed;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return (int) h;
    }

    /**
     * Get memory footprint in bytes
     */
    public long getMemoryFootprint() {
        return (long) depth * width;
    }
}
