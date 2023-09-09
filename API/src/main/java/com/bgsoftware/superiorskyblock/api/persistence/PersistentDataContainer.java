package com.bgsoftware.superiorskyblock.api.persistence;

import com.bgsoftware.common.annotations.Nullable;

import java.util.function.BiConsumer;

public interface PersistentDataContainer {

    /**
     * Check if the provided key has a matching metadata value.
     *
     * @param key The key to check.
     */
    boolean has(String key);

    /**
     * Check if the provided key has a matching metadata value of the provided type.
     *
     * @param key  The key to check.
     * @param type The type to check.
     */
    <T> boolean hasKeyOfType(String key, PersistentDataType<T> type);

    /**
     * Store a metadata value matching the provided key and type.
     *
     * @param key   The key to store.
     * @param type  The type of the metadata value.
     * @param value The metadata value to store.
     * @return The old metadata value that was stored matching the key, if exists.
     * @throws IllegalArgumentException If the old metadata value is not of type {@param type}.
     * @throws IllegalStateException    If {@param type} doesn't have a valid serializer available.
     */
    @Nullable
    <T> T put(String key, PersistentDataType<T> type, T value) throws IllegalArgumentException, IllegalStateException;

    /**
     * Store a metadata value matching the provided key and type.
     *
     * @param key        The key to store.
     * @param type       The type of the metadata.
     * @param value      The metadata value to store.
     * @param returnType The type of the old metadata value.
     * @return The old metadata value that was stored matching the key, if exists.
     * @throws IllegalArgumentException If the old metadata value is not of type {@param returnType}.
     * @throws IllegalStateException    If {@param type} doesn't have a valid serializer available.
     */
    @Nullable
    <T, R> R put(String key, PersistentDataType<T> type, T value, PersistentDataType<R> returnType) throws IllegalArgumentException, IllegalStateException;

    /**
     * Remove a metadata value matching the provided key.
     *
     * @param key The key to remove.
     * @return The old metadata value that was stored matching the key, if exists.
     */
    @Nullable
    Object remove(String key);

    /**
     * Remove a metadata value matching the provided key and type.
     * If the metadata value doesn't match the {@param type}, it will not get removed.
     *
     * @param key  The key to remove.
     * @param type The type of the metadata value to remove.
     * @return The old metadata value that was stored matching the key, if exists.
     * If the metadata value does not match the current type, null will be returned.
     */
    @Nullable
    <T> T removeKeyOfType(String key, PersistentDataType<T> type);

    /**
     * Get a metadata value matching the provided key and type.
     *
     * @param key  The key to fetch.
     * @param type The type of the metadata value to fetch.
     * @return The metadata value that is stored matching the key, if exists.
     * @throws IllegalArgumentException If the metadata value is not of type {@param type}.
     */
    @Nullable
    <T> T get(String key, PersistentDataType<T> type) throws IllegalArgumentException;

    /**
     * Get a metadata value matching the provided key.
     *
     * @param key The key to fetch.
     * @return The metadata value that is stored matching the key, if exists.
     */
    @Nullable
    Object get(String key);

    /**
     * Get a metadata value matching the provided key and type.
     *
     * @param key  The key to fetch.
     * @param type The type of the metadata value to fetch.
     * @param def  Value to return in case there is no metadata value matching the provided key.
     * @return The metadata value that is stored matching the key, or {@param def} otherwise.
     * @throws IllegalArgumentException If the metadata value is not of type {@param type}.
     */
    <T> T getOrDefault(String key, PersistentDataType<T> type, T def) throws IllegalArgumentException;

    /**
     * Get a metadata value matching the provided key.
     *
     * @param key The key to fetch.
     * @param def Value to return in case there is no metadata value matching the provided key.
     * @return The metadata value that is stored matching the key, or {@param def} otherwise.
     */
    Object getOrDefault(String key, Object def);

    /**
     * Check whether the container is empty.
     */
    boolean isEmpty();

    /**
     * Get the size of the container.
     */
    int size();

    /**
     * Iterate through all the data of the container.
     *
     * @param action The action to perform for each key and value pair.
     */
    void forEach(BiConsumer<String, Object> action);

    /**
     * Get the serialized contents of the container as a bytes array.
     * The format of the serialized data may be different depending on the implementation of the container.
     * The serialized data must be loaded without any errors using {@link #load(byte[])}.
     */
    byte[] serialize();

    /**
     * Load contents from the serialized data into the container.
     * The format of the serialized data may be different depending on the implementation of the container.
     *
     * @param data The serialized data.
     * @throws IllegalArgumentException If the given data cannot be serialized correctly.
     */
    void load(byte[] data) throws IllegalArgumentException;

}
