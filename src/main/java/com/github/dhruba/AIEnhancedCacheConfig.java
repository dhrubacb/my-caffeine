package com.github.dhruba;

/**
 * Configuration for AI-Enhanced Cache.
 */
public class AIEnhancedCacheConfig {
    
    private final int maximumSize;
    private final int windowSize;
    private final int featureHistorySize;
    private final boolean enableAI;
    private final double aiWeight;
    private final int maxTrainingExamples;
    private final double learningRate;
    
    private AIEnhancedCacheConfig(Builder builder) {
        this.maximumSize = builder.maximumSize;
        this.windowSize = builder.windowSize;
        this.featureHistorySize = builder.featureHistorySize;
        this.enableAI = builder.enableAI;
        this.aiWeight = builder.aiWeight;
        this.maxTrainingExamples = builder.maxTrainingExamples;
        this.learningRate = builder.learningRate;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getMaximumSize() {
        return maximumSize;
    }
    
    public int getWindowSize() {
        return windowSize;
    }
    
    public int getFeatureHistorySize() {
        return featureHistorySize;
    }
    
    public boolean isEnableAI() {
        return enableAI;
    }
    
    public double getAiWeight() {
        return aiWeight;
    }
    
    public int getMaxTrainingExamples() {
        return maxTrainingExamples;
    }
    
    public double getLearningRate() {
        return learningRate;
    }
    
    public static class Builder {
        private int maximumSize = 10000;
        private int windowSize = 100; // 1% of maximum
        private int featureHistorySize = 10;
        private boolean enableAI = true;
        private double aiWeight = 0.7;
        private int maxTrainingExamples = 1000;
        private double learningRate = 0.01;
        
        public Builder maximumSize(int maximumSize) {
            if (maximumSize <= 0) {
                throw new IllegalArgumentException("Maximum size must be positive");
            }
            this.maximumSize = maximumSize;
            // Auto-adjust window size to 1%
            this.windowSize = Math.max(1, maximumSize / 100);
            return this;
        }
        
        public Builder windowSize(int windowSize) {
            if (windowSize <= 0) {
                throw new IllegalArgumentException("Window size must be positive");
            }
            this.windowSize = windowSize;
            return this;
        }
        
        public Builder featureHistorySize(int featureHistorySize) {
            if (featureHistorySize <= 0) {
                throw new IllegalArgumentException("Feature history size must be positive");
            }
            this.featureHistorySize = featureHistorySize;
            return this;
        }
        
        public Builder enableAI(boolean enableAI) {
            this.enableAI = enableAI;
            return this;
        }
        
        public Builder aiWeight(double aiWeight) {
            if (aiWeight < 0.0 || aiWeight > 1.0) {
                throw new IllegalArgumentException("AI weight must be between 0.0 and 1.0");
            }
            this.aiWeight = aiWeight;
            return this;
        }
        
        public Builder maxTrainingExamples(int maxTrainingExamples) {
            if (maxTrainingExamples <= 0) {
                throw new IllegalArgumentException("Max training examples must be positive");
            }
            this.maxTrainingExamples = maxTrainingExamples;
            return this;
        }
        
        public Builder learningRate(double learningRate) {
            if (learningRate <= 0.0 || learningRate > 1.0) {
                throw new IllegalArgumentException("Learning rate must be between 0.0 and 1.0");
            }
            this.learningRate = learningRate;
            return this;
        }
        
        public AIEnhancedCacheConfig build() {
            return new AIEnhancedCacheConfig(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "AIEnhancedCacheConfig{maximumSize=%d, windowSize=%d, featureHistorySize=%d, " +
            "enableAI=%b, aiWeight=%.2f, maxTrainingExamples=%d, learningRate=%.4f}",
            maximumSize, windowSize, featureHistorySize, enableAI, aiWeight, 
            maxTrainingExamples, learningRate
        );
    }
}
