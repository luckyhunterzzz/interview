package com.interview.hashmap;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyHashMapTest {

    @Test
    void shouldPutAndGetValue() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put("one", 1);

        assertEquals(1, map.get("one"));
    }

    @Test
    void shouldReturnNullForMissingKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        assertNull(map.get("missing"));
    }

    @Test
    void shouldUpdateValueByExistingKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put("one", 1);
        Integer oldValue = map.put("one", 100);

        assertEquals(1, oldValue);
        assertEquals(100, map.get("one"));
        assertEquals(1, map.size());
    }

    @Test
    void shouldRemoveValueByKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put("one", 1);
        Integer removedValue = map.remove("one");

        assertEquals(1, removedValue);
        assertNull(map.get("one"));
        assertEquals(0, map.size());
    }

    @Test
    void shouldReturnNullWhenRemoveMissingKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        assertNull(map.remove("missing"));
        assertEquals(0, map.size());
    }

    @Test
    void shouldWorkWithNullKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put(null, 10);

        assertEquals(10, map.get(null));
        assertEquals(1, map.size());
    }

    @Test
    void shouldUpdateNullKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put(null, 10);
        Integer oldValue = map.put(null, 20);

        assertEquals(10, oldValue);
        assertEquals(20, map.get(null));
        assertEquals(1, map.size());
    }

    @Test
    void shouldRemoveNullKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        map.put(null, 10);
        Integer removedValue = map.remove(null);

        assertEquals(10, removedValue);
        assertNull(map.get(null));
        assertEquals(0, map.size());
    }

    @Test
    void shouldHandleCollisions() {
        MyHashMap<KeyWithSameHash, String> map = new MyHashMap<>();

        KeyWithSameHash first = new KeyWithSameHash("first");
        KeyWithSameHash second = new KeyWithSameHash("second");

        map.put(first, "value1");
        map.put(second, "value2");

        assertEquals("value1", map.get(first));
        assertEquals("value2", map.get(second));
        assertEquals(2, map.size());
    }

    @Test
    void shouldRemoveFromCollisionChain() {
        MyHashMap<KeyWithSameHash, String> map = new MyHashMap<>();

        KeyWithSameHash first = new KeyWithSameHash("first");
        KeyWithSameHash second = new KeyWithSameHash("second");
        KeyWithSameHash third = new KeyWithSameHash("third");

        map.put(first, "value1");
        map.put(second, "value2");
        map.put(third, "value3");

        assertEquals("value2", map.remove(second));

        assertEquals("value1", map.get(first));
        assertNull(map.get(second));
        assertEquals("value3", map.get(third));
        assertEquals(2, map.size());
    }

    @Test
    void shouldResizeAndKeepValues() {
        MyHashMap<Integer, String> map = new MyHashMap<>();

        for (int i = 0; i < 100; i++) {
            map.put(i, "value" + i);
        }

        for (int i = 0; i < 100; i++) {
            assertEquals("value" + i, map.get(i));
        }

        assertEquals(100, map.size());
    }

    @Test
    void shouldHandleMinIntegerHashCode() {
        MyHashMap<Integer, String> map = new MyHashMap<>();

        map.put(Integer.MIN_VALUE, "min");

        assertEquals("min", map.get(Integer.MIN_VALUE));
        assertEquals(1, map.size());
    }

    private static class KeyWithSameHash {

        private final String value;

        private KeyWithSameHash(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof KeyWithSameHash other)) {
                return false;
            }

            return value.equals(other.value);
        }
    }
}
