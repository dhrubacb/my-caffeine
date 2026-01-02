package com.github.dhruba;

public interface Cache<K, V> {
    V getIfPresent(K key);
    V get(K key);
    boolean put(K key, V value);

    boolean putIfAbsent(K key, V value);
}
