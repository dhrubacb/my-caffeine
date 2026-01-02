package com.github.dhruba;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyCaffeineCache<K, V> implements Cache<K, V> {
    ConcurrentMap<K, V> concurrentMap = new ConcurrentHashMap<>();

    @Override
    public V getIfPresent(K key) {
        if (concurrentMap.containsKey(key)) {
            return concurrentMap.get(key);
        }
        return null;
    }

    @Override
    public V get(K key) {
        if (concurrentMap.containsKey(key)) {
            return concurrentMap.get(key);
        }
        // TODO: Implement cache loading mechanism
        return null;
    }

    @Override
    public boolean put(K key, V value) {
        concurrentMap.put(key, value);
        // TODO: Implement cache eviction mechanism
        return true;
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (concurrentMap.containsKey(key)) {
            return false;
        }
        concurrentMap.put(key, value);
        // TODO: Implement cache eviction mechanism
        return true;
    }
}
