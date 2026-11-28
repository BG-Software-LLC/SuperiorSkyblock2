package com.bgsoftware.superiorskyblock.core.database.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class DatabaseCache<V> {

    private final Map<UUID, Record<V>> cache = new HashMap<>();

    public DatabaseCache() {

    }

    public Record<V> computeIfAbsentInfo(UUID uuid, Supplier<V> value) {
        Record<V> record = cache.get(uuid);
        if (record == null) {
            record = new Record<>(value.get());
            cache.put(uuid, record);
        }
        return record;
    }

    public static class Record<T> {

        private final Set<String> recordedTables = new HashSet<>();

        private final T handle;

        private Record(T handle) {
            this.handle = handle;
        }

        public T get() {
            return this.handle;
        }

        public void recordTable(String tableName) {
            this.recordedTables.add(tableName);
        }

        public Set<String> getRecordedTables() {
            return this.recordedTables;
        }

    }

}
