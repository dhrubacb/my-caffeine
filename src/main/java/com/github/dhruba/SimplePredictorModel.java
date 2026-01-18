package com.github.dhruba;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Simple linear regression model for predicting cache entry value.
 * Uses online learning with gradient descent.
 */
public class SimplePredictorModel implements AIPredictor {
    
    private final FeatureExtractor featureExtractor;
    private double[] weights;
    private final Queue<TrainingExample> recentAccesses;
    private final int maxTrainingExamples;
    private final double learningRate;
    private int trainingCount;
    private double confidence;
    
    private static class TrainingExample {
        final double[] features;
        final double label;
        
        TrainingExample(double[] features, double label) {
            this.features = features;
            this.label = label;
        }
    }
    
    public SimplePredictorModel(int maxTrainingExamples, double learningRate) {
        this.featureExtractor = new FeatureExtractor();
        this.weights = new double[7]; // Number of features
        this.recentAccesses = new LinkedList<>();
        this.maxTrainingExamples = maxTrainingExamples;
        this.learningRate = learningRate;
        this.trainingCount = 0;
        this.confidence = 0.0;
        
        // Initialize weights with small random values
        for (int i = 0; i < weights.length; i++) {
            weights[i] = (Math.random() - 0.5) * 0.1;
        }
    }
    
    @Override
    public double predictFutureValue(CacheEntry<?, ?> entry) {
        double[] features = featureExtractor.extractFeatures(entry);
        double[] normalizedFeatures = featureExtractor.normalizeFeatures(features);
        
        double prediction = 0.0;
        for (int i = 0; i < weights.length; i++) {
            prediction += weights[i] * normalizedFeatures[i];
        }
        
        // Apply sigmoid to get value in [0, 1]
        return sigmoid(prediction);
    }
    
    @Override
    public void recordAccess(CacheEntry<?, ?> entry, boolean hit) {
        double[] features = featureExtractor.extractFeatures(entry);
        double[] normalizedFeatures = featureExtractor.normalizeFeatures(features);
        
        // Label: 1.0 for hit (good to keep), 0.0 for miss (could evict)
        double label = hit ? 1.0 : 0.0;
        
        recentAccesses.add(new TrainingExample(normalizedFeatures, label));
        
        // Remove old examples to maintain bounded memory
        while (recentAccesses.size() > maxTrainingExamples) {
            recentAccesses.poll();
        }
        
        // Trigger training periodically
        if (recentAccesses.size() >= Math.min(100, maxTrainingExamples / 10)) {
            train();
        }
    }
    
    @Override
    public void train() {
        if (recentAccesses.isEmpty()) {
            return;
        }
        
        // Perform one epoch of gradient descent
        double totalError = 0.0;
        int count = 0;
        
        for (TrainingExample example : recentAccesses) {
            // Forward pass
            double prediction = 0.0;
            for (int i = 0; i < weights.length; i++) {
                prediction += weights[i] * example.features[i];
            }
            prediction = sigmoid(prediction);
            
            // Calculate error
            double error = example.label - prediction;
            totalError += error * error;
            count++;
            
            // Backward pass - update weights
            double gradient = error * prediction * (1 - prediction); // Sigmoid derivative
            for (int i = 0; i < weights.length; i++) {
                weights[i] += learningRate * gradient * example.features[i];
            }
        }
        
        // Update confidence based on training error
        double mse = totalError / count;
        confidence = Math.max(0.0, 1.0 - mse);
        
        trainingCount++;
    }
    
    @Override
    public AccessPattern classifyPattern(CacheEntry<?, ?> entry) {
        // Simple heuristic-based classification
        double accessRate = entry.getAccessRate();
        double variance = entry.getAccessVariance();
        long accessCount = entry.getAccessCount();
        
        // Hot spot: high access rate, low variance
        if (accessRate > 1.0 && variance < 1000) {
            return AccessPattern.HOT_SPOT;
        }
        
        // Temporal: moderate access rate, high variance (irregular intervals)
        if (accessRate > 0.1 && variance > 10000) {
            return AccessPattern.TEMPORAL;
        }
        
        // Sequential scan: few accesses, recent creation
        long age = System.currentTimeMillis() - entry.getCreationTime();
        if (accessCount <= 2 && age < 60000) { // Less than 1 minute old
            return AccessPattern.SEQUENTIAL_SCAN;
        }
        
        // Working set: moderate access rate and variance
        if (accessRate > 0.01 && accessRate < 1.0) {
            return AccessPattern.WORKING_SET;
        }
        
        // Default to random if no clear pattern
        return AccessPattern.RANDOM;
    }
    
    @Override
    public double getConfidence() {
        // Confidence increases with training count, capped at model confidence
        double trainingConfidence = Math.min(1.0, trainingCount / 100.0);
        return Math.min(confidence, trainingConfidence);
    }
    
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    
    /**
     * Get current model weights for inspection
     */
    public double[] getWeights() {
        return weights.clone();
    }
    
    /**
     * Get number of training iterations performed
     */
    public int getTrainingCount() {
        return trainingCount;
    }
}
