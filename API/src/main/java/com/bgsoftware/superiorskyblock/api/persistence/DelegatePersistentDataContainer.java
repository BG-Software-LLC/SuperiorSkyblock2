package com.bgsoftware.superiorskyblock.api.persistence;

import com.bgsoftware.common.annotations.Nullable;

import java.util.function.BiConsumer;

public class DelegatePersistentDataContainer implements PersistentDataContainer {

    protected final PersistentDataContainer handle;

    protected DelegatePersistentDataContainer(PersistentDataContainer handle) {
        this.handle = handle;
    }

    @Override
    public boolean has(String key) {
        return this.handle.has(key);
    }

    @Override
    public <T> boolean hasKeyOfType(String key, PersistentDataType<T> type) {
        return this.handle.hasKeyOfType(key, type);
    }

    @Nullable
    @Override
    public <T> T put(String key, PersistentDataType<T> type, T value) throws IllegalArgumentException, IllegalStateException {
        return this.handle.put(key, type, value);
    }

    @Nullable
    @Override
    public <T, R> R put(String key, PersistentDataType<T> type, T value, PersistentDataType<R> returnType) throws IllegalArgumentException, IllegalStateException {
        return this.handle.put(key, type, value, returnType);
    }

    @Nullable
    @Override
    public Object remove(String key) {
        return this.handle.remove(key);
    }

    @Nullable
    @Override
    public <T> T removeKeyOfType(String key, PersistentDataType<T> type) {
        return this.handle.removeKeyOfType(key, type);
    }

    @Nullable
    @Override
    public <T> T get(String key, PersistentDataType<T> type) throws IllegalArgumentException {
        return this.handle.get(key, type);
    }

    @Nullable
    @Override
    public Object get(String key) {
        return this.handle.get(key);
    }

    @Override
    public <T> T getOrDefault(String key, PersistentDataType<T> type, T def) throws IllegalArgumentException {
        return this.handle.getOrDefault(key, type, def);
    }

    @Override
    public Object getOrDefault(String key, Object def) {
        return this.handle.getOrDefault(key, def);
    }

    @Override
    public boolean isEmpty() {
        return this.handle.isEmpty();
    }

    @Override
    public int size() {
        return this.handle.size();
    }

    @Override
    public void forEach(BiConsumer<String, Object> action) {
        this.handle.forEach(action);
    }

    @Override
    public byte[] serialize() {
        return this.handle.serialize();
    }

    @Override
    public void load(byte[] data) throws IllegalArgumentException {
        this.handle.load(data);
    }

}
