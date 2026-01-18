# Architecture Documentation

## System Overview

The AI-Enhanced W-TinyLFU Cache combines traditional cache eviction algorithms with machine learning to achieve superior hit rates. This document provides detailed architectural information for developers.

## Component Hierarchy

```
AIEnhancedCache
├── ConcurrentHashMap (storage)
├── LRU Queues (window, probationary, protected)
├── CountMinSketch (frequency tracking)
├── AIEnhancedAdmissionPolicy
│   ├── SimplePredictorModel (ML model)
│   └── FeatureExtractor (feature engineering)
└── CacheStatistics (metrics)
```

## Data Flow

### Cache Get Operation

```
1. User calls cache.get(key)
2. Lookup in ConcurrentHashMap
3. If found:
   a. Record access timestamp
   b. Increment frequency in CountMinSketch
   c. Record hit in statistics
   d. Train AI model (async)
   e. Promote entry in queue hierarchy
   f. Return value
4. If not found:
   a. Record miss in statistics
   b. Return null
```

### Cache Put Operation

```
1. User calls cache.put(key, value)
2. Check if key exists
3. If exists:
   a. Update value
   b. Record access
   c. Promote entry
4. If new:
   a. Create CacheEntry with metadata
   b. Increment frequency
   c. Check if cache is full
   d. If full:
      - Evict from window
      - AI decides admission to main
      - Evict from probationary if needed
   e. Add to window queue
```

### Admission Decision Flow

```
1. Window is full, need to evict
2. Get window victim (LRU from window)
3. Get probationary victim (LRU from probationary)
4. Extract features from both entries
5. Get frequency estimates from CountMinSketch
6. If AI enabled and confident:
   a. Predict future value for both
   b. Classify access patterns
   c. Apply pattern adjustments
   d. Calculate weighted scores
   e. Admit entry with higher score
7. Else:
   a. Use frequency-based decision
8. Update statistics
```

## Thread Safety

### Concurrency Strategy

The cache uses a hybrid approach:

1. **ConcurrentHashMap**: Lock-free reads, fine-grained locking for writes
2. **ReentrantLock**: Exclusive lock for queue operations (eviction)
3. **Atomic counters**: Lock-free statistics updates
4. **Lock amortization**: Batch operations under single lock acquisition

### Critical Sections

```java
// Only queue operations are synchronized
evictionLock.lock();
try {
    // Modify queue structure
    // Perform eviction
    // Update queue pointers
} finally {
    evictionLock.unlock();
}
```

### Lock-Free Operations

- Hash table lookups
- Frequency sketch updates (uses atomic operations internally)
- Statistics recording (AtomicLong)
- Feature extraction (read-only)

## Memory Layout

### CacheEntry Structure

```
CacheEntry<K, V>
├── key: K                           (8 bytes reference)
├── value: V                         (8 bytes reference)
├── accessCount: long                (8 bytes)
├── lastAccessTime: long             (8 bytes)
├── creationTime: long               (8 bytes)
├── writeTime: long                  (8 bytes)
├── accessTimestamps: List<Long>     (8 bytes × history size)
├── predictedValue: double           (8 bytes)
├── pattern: AccessPattern           (4 bytes enum)
├── next: CacheEntry                 (8 bytes reference)
├── prev: CacheEntry                 (8 bytes reference)
└── Object header                    (12-16 bytes)

Total: ~100 bytes + (8 × history size)
```

### CountMinSketch Structure

```
CountMinSketch
├── depth: int                       (4 bytes)
├── width: int                       (4 bytes)
├── table: byte[][]                  (depth × width bytes)
├── seeds: long[]                    (8 bytes × depth)
└── size: long                       (8 bytes)

For 10,000 entries with depth=4, width=40,000:
Total: ~160 KB
```

### SimplePredictorModel Structure

```
SimplePredictorModel
├── weights: double[]                (8 bytes × 7 features = 56 bytes)
├── recentAccesses: Queue            (variable, capped at maxTrainingExamples)
├── featureExtractor: FeatureExtractor (singleton)
└── metadata                         (32 bytes)

Training buffer for 1000 examples:
Total: ~100 KB
```

## Algorithm Details

### Count-Min Sketch

**Purpose**: Approximate frequency counting with bounded error

**Parameters**:
- Width (w): 4 × cache size
- Depth (d): 4 hash functions
- Counter size: 4 bits (0-15)

**Operations**:
- Increment: O(d) = O(1)
- Estimate: O(d) = O(1)
- Space: O(w × d) bits

**Error Bounds**:
- ε (error): 1/w
- δ (confidence): 1 - (1/2)^d
- With w=4n, d=4: ε=0.00025, δ=0.9375

### Feature Engineering

**Feature Normalization**:

```
normalized = sigmoid(raw / scale)
where sigmoid(x) = 1 / (1 + e^(-x))
```

**Feature Scales**:
- Access count: scale = 10 (log domain)
- Recency: scale = 3600 seconds (1 hour)
- Age: scale = 86400 seconds (1 day)
- Variance: scale = 1000 (log domain)

### Linear Regression Model

**Model**: y = w₀×f₀ + w₁×f₁ + ... + w₆×f₆

**Training**: Online gradient descent

```
For each training example (x, y):
  prediction = sigmoid(w · x)
  error = y - prediction
  gradient = error × prediction × (1 - prediction)
  w[i] += learningRate × gradient × x[i]
```

**Learning Rate**: 0.01 (default)

**Convergence**: Typically 100-500 examples

### Pattern Classification

**Heuristics**:

```
HOT_SPOT:
  accessRate > 1.0 AND variance < 1000

TEMPORAL:
  accessRate > 0.1 AND variance > 10000

SEQUENTIAL_SCAN:
  accessCount <= 2 AND age < 60000ms

WORKING_SET:
  0.01 < accessRate < 1.0

RANDOM:
  default fallback
```

## Performance Optimization

### Fast Path

When cache is below 50% capacity:
- Skip AI prediction (not enough data)
- Skip frequency aging
- Reduce lock contention
- Direct admission without eviction

### Batching

Operations are batched to amortize lock overhead:
- Multiple accesses recorded before training
- Frequency sketch reset is periodic, not per-access
- Statistics updates use atomic operations

### Memory Efficiency

- 4-bit counters in CountMinSketch (vs 32-bit integers)
- Bounded access history per entry
- Lazy initialization of AI components
- Reuse of feature arrays

## Configuration Guidelines

### Small Caches (< 1,000 entries)

```java
AIEnhancedCacheConfig.builder()
    .maximumSize(500)
    .windowSize(5)              // 1%
    .featureHistorySize(5)      // Minimal history
    .aiWeight(0.5)              // More weight on frequency
    .maxTrainingExamples(100)   // Small training set
    .build();
```

### Medium Caches (1,000 - 100,000 entries)

```java
AIEnhancedCacheConfig.builder()
    .maximumSize(10_000)
    .windowSize(100)            // 1%
    .featureHistorySize(10)     // Default
    .aiWeight(0.7)              // Balanced
    .maxTrainingExamples(1000)  // Default
    .build();
```

### Large Caches (> 100,000 entries)

```java
AIEnhancedCacheConfig.builder()
    .maximumSize(1_000_000)
    .windowSize(10_000)         // 1%
    .featureHistorySize(20)     // More history
    .aiWeight(0.8)              // Trust AI more
    .maxTrainingExamples(5000)  // Larger training set
    .build();
```

## Monitoring and Debugging

### Key Metrics

```java
CacheStatistics stats = cache.getStatistics();

// Performance metrics
double hitRate = stats.getHitRate();
long evictions = stats.getEvictionCount();

// AI metrics
double aiInfluence = stats.getAiInfluenceRate();
long aiAdmissions = stats.getAiAdmissionCount();

// Admission metrics
double admissionRate = stats.getAdmissionRate();
long rejections = stats.getRejectionCount();
```

### Debugging Tips

1. **Low hit rate**: Increase cache size or AI weight
2. **High eviction rate**: Cache too small for workload
3. **Low AI influence**: Model not confident, needs more training
4. **High rejection rate**: Admission policy too strict, reduce AI weight

## Extension Points

### Custom Predictor

Implement the `AIPredictor` interface:

```java
public class CustomPredictor implements AIPredictor {
    @Override
    public double predictFutureValue(CacheEntry<?, ?> entry) {
        // Your prediction logic
    }
    
    @Override
    public void recordAccess(CacheEntry<?, ?> entry, boolean hit) {
        // Your training logic
    }
    
    // ... other methods
}
```

### Custom Features

Extend `FeatureExtractor`:

```java
public class CustomFeatureExtractor extends FeatureExtractor {
    @Override
    public double[] extractFeatures(CacheEntry<?, ?> entry) {
        double[] baseFeatures = super.extractFeatures(entry);
        // Add custom features
        return enhancedFeatures;
    }
}
```

## Testing Strategy

### Unit Tests

- Component isolation
- Edge cases (empty cache, full cache)
- Concurrent access
- Configuration validation

### Integration Tests

- Full cache lifecycle
- Workload simulation
- Pattern detection
- AI training convergence

### Performance Tests

- Throughput benchmarks
- Latency percentiles
- Memory profiling
- Scalability testing

## Known Issues and Limitations

1. **Training overhead**: Model training adds ~1ms latency every 100 accesses
2. **Cold start**: AI needs 100-500 accesses to become effective
3. **Pattern detection**: Heuristics may misclassify edge cases
4. **Memory overhead**: ~100 bytes per entry vs ~40 bytes for basic LRU

## Future Work

1. **Adaptive window sizing**: ML-based segment ratio optimization
2. **Multi-model ensemble**: Combine multiple predictors
3. **Distributed learning**: Share model weights across cache nodes
4. **GPU acceleration**: Batch predictions on GPU for large caches
5. **Explainability**: Visualize why entries were admitted/evicted
