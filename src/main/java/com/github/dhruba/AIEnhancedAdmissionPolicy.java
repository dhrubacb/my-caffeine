package com.github.dhruba;

/**
 * AI-enhanced admission policy that combines traditional frequency-based
 * admission with ML-based prediction of future value.
 */
public class AIEnhancedAdmissionPolicy {
    
    private final AIPredictor predictor;
    private final CountMinSketch frequencySketch;
    private final double aiWeight;
    private final double frequencyWeight;
    private final boolean enableAI;
    
    public AIEnhancedAdmissionPolicy(AIPredictor predictor, 
                                     CountMinSketch frequencySketch,
                                     double aiWeight,
                                     boolean enableAI) {
        this.predictor = predictor;
        this.frequencySketch = frequencySketch;
        this.aiWeight = aiWeight;
        this.frequencyWeight = 1.0 - aiWeight;
        this.enableAI = enableAI;
    }
    
    /**
     * Decide whether to admit a candidate entry by evicting the victim.
     * 
     * @param candidate The new entry trying to enter the cache
     * @param victim The current entry that would be evicted
     * @return true if candidate should be admitted, false otherwise
     */
    public boolean shouldAdmit(CacheEntry<?, ?> candidate, CacheEntry<?, ?> victim) {
        if (victim == null) {
            return true; // Always admit if no victim
        }
        
        // Get traditional frequency scores
        int candidateFreq = frequencySketch.estimate(candidate.getKey());
        int victimFreq = frequencySketch.estimate(victim.getKey());
        
        // Normalize frequencies to [0, 1]
        double candidateFreqNorm = normalize(candidateFreq, 15);
        double victimFreqNorm = normalize(victimFreq, 15);
        
        if (!enableAI || predictor.getConfidence() < 0.3) {
            // Fall back to traditional frequency-based decision
            return candidateFreq > victimFreq;
        }
        
        // Get AI predictions
        double candidateValue = predictor.predictFutureValue(candidate);
        double victimValue = predictor.predictFutureValue(victim);
        
        // Classify patterns
        AccessPattern candidatePattern = predictor.classifyPattern(candidate);
        AccessPattern victimPattern = predictor.classifyPattern(victim);
        
        // Update entry patterns
        candidate.setPattern(candidatePattern);
        victim.setPattern(victimPattern);
        
        // Apply pattern-specific adjustments
        candidateValue = applyPatternAdjustment(candidateValue, candidatePattern);
        victimValue = applyPatternAdjustment(victimValue, victimPattern);
        
        // Weighted decision combining AI and frequency
        double candidateScore = aiWeight * candidateValue + frequencyWeight * candidateFreqNorm;
        double victimScore = aiWeight * victimValue + frequencyWeight * victimFreqNorm;
        
        // Store predicted values
        candidate.setPredictedValue(candidateValue);
        victim.setPredictedValue(victimValue);
        
        return candidateScore > victimScore;
    }
    
    /**
     * Apply pattern-specific adjustments to predicted values
     */
    private double applyPatternAdjustment(double value, AccessPattern pattern) {
        switch (pattern) {
            case SEQUENTIAL_SCAN:
                // Penalize sequential scans - they shouldn't pollute cache
                return value * 0.5;
                
            case HOT_SPOT:
                // Boost hot spots - they're very valuable
                return Math.min(1.0, value * 1.3);
                
            case TEMPORAL:
                // Slight boost for temporal patterns
                return Math.min(1.0, value * 1.1);
                
            case WORKING_SET:
                // Keep working set entries
                return Math.min(1.0, value * 1.2);
                
            case RANDOM:
            case UNKNOWN:
            default:
                return value;
        }
    }
    
    /**
     * Normalize a value to [0, 1] range
     */
    private double normalize(int value, int max) {
        return Math.min(1.0, (double) value / max);
    }
    
    /**
     * Record an access for the AI model to learn from
     */
    public void recordAccess(CacheEntry<?, ?> entry, boolean hit) {
        if (enableAI) {
            predictor.recordAccess(entry, hit);
        }
    }
    
    /**
     * Get the AI predictor
     */
    public AIPredictor getPredictor() {
        return predictor;
    }
}
