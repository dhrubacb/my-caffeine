my-caffeine ☕️
my-caffeine is a high-performance, near-optimal Java caching library. Inspired by the Caffeine project, it is designed to provide high concurrency and efficient memory management using advanced eviction policies.

🚀 Overview
my-caffeine provides an in-memory cache that uses a Google Guava-inspired API but under the hood implements the W-TinyLFU admission and eviction policy. This ensures a higher hit rate than standard LRU (Least Recently Used) caches while maintaining constant time complexity for operations.

✨ Features
Automatic Loading: Seamless integration to retrieve entries from a data source.

Size-based Eviction: Evict entries based on a maximum weight or count when the limit is exceeded.

Time-based Expiration: * Expire after access: Decay entries that haven't been read or written for a duration.

Expire after write: Decay entries that haven't been updated for a duration.

W-TinyLFU Policy: Uses a frequency-based admission policy to keep "hot" items in the cache.

Wait-free Statistics: High-performance counters for hit rate, eviction count, and average load penalty.

📦 Installation
Add the following dependency to your pom.xml (replace with your actual coordinates):

XML

<dependency>
    <groupId>com.github.dhruba</groupId>
    <artifactId>my-caffeine</artifactId>
    <version>1.0.0</version>
</dependency>
🛠 Usage
The API is designed to be fluent and easy to use via the MyCaffeine builder.

Basic Cache
Java

Cache<String, DataObject> cache = MyCaffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(5))
    .build();

// Put a value
cache.put("key1", dataObject);

// Get a value
DataObject value = cache.getIfPresent("key1");
Loading Cache
If you want the cache to automatically fetch missing values:

Java

LoadingCache<Key, Graph> graphs = MyCaffeine.newBuilder()
    .maximumSize(10_000)
    .build(key -> createExpensiveGraph(key));

Graph graph = graphs.get(key);
🏗 Architecture
my-caffeine utilizes a three-tiered approach to memory management:

Admission Window: A small LRU for newly added entries.

Frequency Filter: A Bloom Filter-based mechanism (TinyLFU) to decide if a new entry is "worth" keeping.

Main Segmented LRU: Where the majority of long-term entries reside.

📊 Benchmarks
Coming soon. Preliminary tests show my-caffeine handles concurrent read/write threads with minimal lock contention compared to standard ConcurrentHashMap wrappers.

🤝 Contributing
Contributions are welcome! If you find a bug or have a feature request, please open an issue or submit a pull request.

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📜 License
Distributed under the MIT License. See LICENSE for more information.
