package com.aaronicsubstances.cs_and_math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class TestResourceLoader {
    public static final Random RAND_GEN = new Random();

	public static void printTestHeader(String testName, Object... testArgs) {
        System.out.print(testName + "(");
        for (int i = 0; i < testArgs.length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            if (testArgs[i] != null) {
                System.out.print(testArgs[i]);
            }
        }
        System.out.println(")");
        System.out.println("---------------------");
    }

    public static <K, V> KeyValuePair<K, V> newMapEntry(K key, V value) {
        return new KeyValuePair<>(key, value);
    }

    public static <K, V> Map<K, V> newMap(List<KeyValuePair<K, V>> entries) {
        Map<K, V> map = new HashMap<>();
        for (KeyValuePair<K, V> entry : entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    public static class KeyValuePair<K, V> {
        public K key;
        public V value;

        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}