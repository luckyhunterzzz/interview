package com.interview.hashmap;

import java.util.Objects;

public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Node<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.buckets = new Node[DEFAULT_CAPACITY];
    }

    public V put(K key, V value) {
        resizeIfNeeded();
        return putNode(key, value);
    }

    public V get(K key) {
        int index = getIndex(key);

        Node<K, V> current = buckets[index];

        while (current != null) {
            if (Objects.equals(current.key, key)) {
                return current.value;
            }

            current = current.next;
        }

        return null;
    }

    public V remove(K key) {
        int index = getIndex(key);

        Node<K, V> current = buckets[index];
        Node<K, V> previous = null;

        while (current != null) {
            if (Objects.equals(current.key, key)) {

                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }

                size--;

                return current.value;
            }

            previous = current;
            current = current.next;
        }

        return null;
    }

    public int size() {
        return size;
    }

    private int getIndex(K key) {
        if (key == null) {
            return 0;
        }

        return Math.floorMod(key.hashCode(), buckets.length);
    }

    private void resizeIfNeeded() {
        if ((float) size / buckets.length >= LOAD_FACTOR) {
            Node<K, V>[] oldBuckets = buckets;

            buckets = new Node[oldBuckets.length * 2];
            size = 0;

            for (Node<K, V> bucket : oldBuckets) {
                Node<K, V> current = bucket;

                while (current != null) {
                    putNode(current.key, current.value);
                    current = current.next;
                }
            }
        }
    }

    private V putNode(K key, V value) {
        int index = getIndex(key);

        Node<K, V> current = buckets[index];

        if (current == null) {
            buckets[index] = new Node<>(key, value);
            size++;
            return null;
        }

        while (true) {
            if (Objects.equals(current.key, key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }

            if (current.next == null) {
                current.next = new Node<>(key, value);
                size++;
                return null;
            }

            current = current.next;
        }
    }

    private static class Node<K, V> {

        private final K key;
        private V value;
        private Node<K, V> next;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
