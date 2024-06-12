package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class KeyMapStrategy {

    public static final KeyMapStrategy ARRAY_MAP = new KeyMapStrategy() {
        @Override
        public <K, V> Map<K, V> create(boolean isCustomKeyMap) {
            return new ArrayMap<>();
        }
    };

    public static final KeyMapStrategy CONCURRENT_HASH_MAP = new KeyMapStrategy() {
        @Override
        public <K, V> Map<K, V> create(boolean isCustomKeyMap) {
            return new ConcurrentHashMap<>();
        }
    };

    public static final KeyMapStrategy HASH_MAP = new KeyMapStrategy() {
        @Override
        public <K, V> Map<K, V> create(boolean isCustomKeyMap) {
            return new HashMap<>();
        }
    };

    public static <K, V> KeyMapStrategy custom(Supplier<Map<K, V>> supplier) {
        return new KeyMapStrategy() {
            @Override
            public <K, V> Map<K, V> create(boolean isCustomKeyMap) {
                return (Map<K, V>) supplier.get();
            }
        };
    }

    private KeyMapStrategy() {

    }

    public abstract <K, V> Map<K, V> create(boolean isCustomKeyMap);

}
