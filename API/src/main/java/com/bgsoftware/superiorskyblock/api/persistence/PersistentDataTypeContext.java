package com.bgsoftware.superiorskyblock.api.persistence;

/**
 * The context class is used to serialize and deserialize a custom data type.
 *
 * @param <T> The type of the data value.
 */
public interface PersistentDataTypeContext<T> {

    /**
     * Serialize the provided value into a bytes buffer.
     *
     * @param value The value to serialize.
     * @return The serialized data.
     */
    byte[] serialize(T value);

    /**
     * Deserialize the provided bytes buffer into a valid value.
     *
     * @param data The serialized data.
     * @return The deserialized value.
     */
    T deserialize(byte[] data);

}
