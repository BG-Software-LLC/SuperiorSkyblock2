package com.bgsoftware.superiorskyblock.core.key.collections;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class KeySetStrategy {

    public static final KeySetStrategy HASH_SET = new KeySetStrategy() {
        @Override
        public <K> Set<K> create(boolean isCustomKeySet) {
            return new HashSet<>();
        }
    };

    public static <K> KeySetStrategy custom(Supplier<Set<K>> supplier) {
        return new KeySetStrategy() {
            @Override
            public <K> Set<K> create(boolean isCustomKeyMap) {
                return (Set<K>) supplier.get();
            }
        };
    }

    private KeySetStrategy() {

    }

    public abstract <K> Set<K> create(boolean isCustomKeySet);

}
