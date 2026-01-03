package com.github.dhruba;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyCaffeineCache<K, V> implements Cache<K, V> {
    private final ConcurrentMap<K, CacheNode<K, V>> concurrentMap = new ConcurrentHashMap<>();

    @Override
    public V get(K key) {
        if (concurrentMap.containsKey(key)) {
            CacheNode<K, V> kvCache = concurrentMap.get(key);
            if (kvCache != null)
                return kvCache.getValue();
        }
        return null;
    }

    @Override
    public boolean put(K key, V value) {
        CacheNode<K, V> kvCache = new CacheNode<>(key, value);
        concurrentMap.put(key, kvCache);
        // TODO: Implement cache eviction mechanism
        return true;
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        if (concurrentMap.containsKey(key)) {
            return false;
        }
        return put(key, value);
    }

    @Override
    public void remove(K key) {
        concurrentMap.remove(key);
    }

}
