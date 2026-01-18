package com.github.dhruba;

/**
 * Represents different types of cache access patterns identified by the AI layer.
 */
public enum AccessPattern {
    /**
     * Sequential scan pattern - large sequential reads that shouldn't pollute cache
     */
    SEQUENTIAL_SCAN,
    
    /**
     * Temporal pattern - time-based patterns (hourly reports, daily batches)
     */
    TEMPORAL,
    
    /**
     * Hot spot pattern - small set of frequently accessed keys
     */
    HOT_SPOT,
    
    /**
     * Working set pattern - medium-sized set with high locality
     */
    WORKING_SET,
    
    /**
     * Random pattern - unpredictable access patterns
     */
    RANDOM,
    
    /**
     * Unknown pattern - not yet classified
     */
    UNKNOWN
}
