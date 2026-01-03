package com.github.dhruba;

public interface Cache<K, V> {
    V get(K key);
    boolean put(K key, V value);

    boolean putIfAbsent(K key, V value);
    void remove(K key);
}
