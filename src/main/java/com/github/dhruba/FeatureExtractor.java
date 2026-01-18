package com.github.dhruba;

import java.util.Calendar;

/**
 * Extracts features from cache entries for ML-based prediction.
 */
public class FeatureExtractor {
    
    /**
     * Extract feature vector from a cache entry
     * Features:
     * 0: Access count (normalized)
     * 1: Seconds since last access
     * 2: Entry age in seconds
     * 3: Access rate (accesses per second)
     * 4: Access variance (pattern regularity)
     * 5: Time of day feature (0-1, cyclical)
     * 6: Size proxy (value string length, normalized)
     */
    public double[] extractFeatures(CacheEntry<?, ?> entry) {
        long now = System.currentTimeMillis();
        
        double[] features = new double[7];
        
        // Feature 0: Access count (log-normalized to handle wide range)
        features[0] = Math.log1p(entry.getAccessCount());
        
        // Feature 1: Recency - seconds since last access
        features[1] = (now - entry.getLastAccessTime()) / 1000.0;
        
        // Feature 2: Age - seconds since creation
        features[2] = (now - entry.getCreationTime()) / 1000.0;
        
        // Feature 3: Access rate
        features[3] = entry.getAccessRate();
        
        // Feature 4: Access variance (pattern regularity)
        features[4] = Math.log1p(entry.getAccessVariance());
        
        // Feature 5: Time of day (cyclical feature: 0 at midnight, 1 at noon, 0 at midnight)
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        features[5] = Math.sin(2 * Math.PI * hourOfDay / 24.0);
        
        // Feature 6: Size proxy (normalized by log)
        Object value = entry.getValue();
        double size = value != null ? value.toString().length() : 0;
        features[6] = Math.log1p(size);
        
        return features;
    }
    
    /**
     * Normalize features to [0, 1] range for better ML performance
     */
    public double[] normalizeFeatures(double[] features) {
        double[] normalized = new double[features.length];
        
        // Feature-specific normalization based on expected ranges
        normalized[0] = sigmoid(features[0] / 10.0);  // Access count
        normalized[1] = sigmoid(features[1] / 3600.0); // Recency (hours)
        normalized[2] = sigmoid(features[2] / 86400.0); // Age (days)
        normalized[3] = sigmoid(features[3]);          // Access rate
        normalized[4] = sigmoid(features[4] / 1000.0); // Variance
        normalized[5] = (features[5] + 1.0) / 2.0;     // Time of day (already -1 to 1)
        normalized[6] = sigmoid(features[6] / 10.0);   // Size
        
        return normalized;
    }
    
    /**
     * Sigmoid function to map values to [0, 1]
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    /**
     * Get feature names for debugging and analysis
     */
    public String[] getFeatureNames() {
        return new String[]{
            "access_count_log",
            "seconds_since_last_access",
            "entry_age_seconds",
            "access_rate",
            "access_variance_log",
            "time_of_day_sin",
            "size_log"
        };
    }
}
