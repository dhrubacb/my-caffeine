package com.github.dhruba;

/**
 * Interface for AI-based prediction of cache entry value.
 */
public interface AIPredictor {
    
    /**
     * Predict the future value/utility of a cache entry.
     * Higher values indicate the entry is more likely to be accessed in the future.
     * 
     * @param entry The cache entry to predict for
     * @return Predicted value in range [0, 1]
     */
    double predictFutureValue(CacheEntry<?, ?> entry);
    
    /**
     * Record an access event for training the model.
     * 
     * @param entry The cache entry that was accessed
     * @param hit Whether it was a cache hit (true) or miss (false)
     */
    void recordAccess(CacheEntry<?, ?> entry, boolean hit);
    
    /**
     * Train or update the model based on recorded accesses.
     */
    void train();
    
    /**
     * Classify the access pattern of an entry.
     * 
     * @param entry The cache entry to classify
     * @return The identified access pattern
     */
    AccessPattern classifyPattern(CacheEntry<?, ?> entry);
    
    /**
     * Get the confidence level of the prediction.
     * 
     * @return Confidence in range [0, 1]
     */
    double getConfidence();
}
