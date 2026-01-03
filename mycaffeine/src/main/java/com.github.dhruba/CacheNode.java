package com.github.dhruba;

public class CacheNode<K, V> {
    private final K key;
    private V value;
    private long accessTime;
    private long writeTime;
    private CacheNode<K, V> next;
    private CacheNode<K, V> prev;

    public CacheNode(K key, V value) {
        this.key = key;
        this.value = value;
        this.accessTime = System.currentTimeMillis();
        this.writeTime = System.currentTimeMillis();
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
        this.writeTime = System.currentTimeMillis();
        this.accessTime = System.currentTimeMillis();
    }

    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public long getWriteTime() {
        return writeTime;
    }

    public void setWriteTime(long writeTime) {
        this.writeTime = writeTime;
    }

    public CacheNode<K, V> getNext() {
        return next;
    }

    public void setNext(CacheNode<K, V> next) {
        this.next = next;
    }

    public CacheNode<K, V> getPrev() {
        return prev;
    }

    public void setPrev(CacheNode<K, V> prev) {
        this.prev = prev;
    }

    @Override
    public String toString() {
        return "Key: " + key + ", Value: " + value
                + ", Access Time: " + accessTime
                + ", Write Time: " + writeTime;
    }

}
