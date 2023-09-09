package com.bgsoftware.superiorskyblock.core.persistence;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;

import java.util.function.BiConsumer;

public class EmptyPersistentDataContainer implements PersistentDataContainer {

    private static final EmptyPersistentDataContainer INSTANCE = new EmptyPersistentDataContainer();

    public static EmptyPersistentDataContainer getInstance() {
        return INSTANCE;
    }

    private EmptyPersistentDataContainer() {

    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public <T> boolean hasKeyOfType(String key, PersistentDataType<T> type) {
        return false;
    }

    @Nullable
    @Override
    public <T> T put(String key, PersistentDataType<T> type, T value) {
        return null;
    }

    @Nullable
    @Override
    public <T, R> R put(String key, PersistentDataType<T> type, T value, PersistentDataType<R> returnType) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Nullable
    @Override
    public Object remove(String key) {
        return null;
    }

    @Nullable
    @Override
    public <T> T removeKeyOfType(String key, PersistentDataType<T> type) {
        return null;
    }

    @Nullable
    @Override
    public <T> T get(String key, PersistentDataType<T> type) throws IllegalArgumentException {
        return null;
    }

    @Nullable
    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public <T> T getOrDefault(String key, PersistentDataType<T> type, T def) throws IllegalArgumentException {
        return def;
    }

    @Override
    public Object getOrDefault(String key, Object def) {
        return def;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void forEach(BiConsumer<String, Object> action) {
        // Do nothing.
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void load(byte[] data) {
        // Do nothing.
    }

}
