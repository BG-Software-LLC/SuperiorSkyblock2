package com.bgsoftware.superiorskyblock.api.persistence;

import com.bgsoftware.common.annotations.Nullable;
import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a data type that can be stored inside a {@link PersistentDataContainer}.
 *
 * @param <T> The type of the value to store.
 */
public class PersistentDataType<T> {

    public static final PersistentDataType<BigDecimal> BIG_DECIMAL = new PersistentDataType<>(BigDecimal.class);
    public static final PersistentDataType<byte[]> BYTE_ARRAY = new PersistentDataType<>(byte[].class);
    public static final PersistentDataType<Byte> BYTE = new PersistentDataType<>(Byte.class);
    public static final PersistentDataType<Double> DOUBLE = new PersistentDataType<>(Double.class);
    public static final PersistentDataType<Float> FLOAT = new PersistentDataType<>(Float.class);
    public static final PersistentDataType<int[]> INT_ARRAY = new PersistentDataType<>(int[].class);
    public static final PersistentDataType<Integer> INTEGER = new PersistentDataType<>(Integer.class);
    public static final PersistentDataType<Long> LONG = new PersistentDataType<>(Long.class);
    public static final PersistentDataType<Short> SHORT = new PersistentDataType<>(Short.class);
    public static final PersistentDataType<String> STRING = new PersistentDataType<>(String.class);
    public static final PersistentDataType<UUID> UUID = new PersistentDataType<>(UUID.class);

    private final Class<T> type;
    private final PersistentDataTypeContext<T> context;

    /**
     * Custom type constructor.
     *
     * @param type    The type.
     * @param context The context class used to serialize and deserialize this data type.
     */
    public PersistentDataType(Class<T> type, PersistentDataTypeContext<T> context) {
        this.type = Preconditions.checkNotNull(type, "type parameter cannot be null");
        this.context = Preconditions.checkNotNull(context, "context parameter cannot be null");
    }

    /**
     * Built-in type constructor.
     *
     * @param type The type.
     */
    private PersistentDataType(Class<T> type) {
        this.type = type;
        this.context = null;
    }

    public Class<T> getType() {
        return this.type;
    }

    @Nullable
    public PersistentDataTypeContext<T> getContext() {
        return this.context;
    }

}
