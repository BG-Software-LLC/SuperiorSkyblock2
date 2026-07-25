package com.bgsoftware.superiorskyblock.core.persistence;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataTypeContext;
import com.bgsoftware.superiorskyblock.tag.BigDecimalTag;
import com.bgsoftware.superiorskyblock.tag.ByteArrayTag;
import com.bgsoftware.superiorskyblock.tag.ByteTag;
import com.bgsoftware.superiorskyblock.tag.DoubleTag;
import com.bgsoftware.superiorskyblock.tag.FloatTag;
import com.bgsoftware.superiorskyblock.tag.IntArrayTag;
import com.bgsoftware.superiorskyblock.tag.IntTag;
import com.bgsoftware.superiorskyblock.tag.LongTag;
import com.bgsoftware.superiorskyblock.tag.PersistentDataTag;
import com.bgsoftware.superiorskyblock.tag.PersistentDataTagSerialized;
import com.bgsoftware.superiorskyblock.tag.ShortTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.bgsoftware.superiorskyblock.tag.UUIDTag;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PersistenceDataTypeSerializer {

    private static final Map<Class<?>, Function<Object, Tag<?>>> CLASS_TO_NBT = new ImmutableMap.Builder<Class<?>, Function<Object, Tag<?>>>()
            .put(BigDecimal.class, value -> BigDecimalTag.of((BigDecimal) value))
            .put(byte[].class, value -> ByteArrayTag.of((byte[]) value))
            .put(Byte.class, value -> ByteTag.of((byte) value))
            .put(Double.class, value -> DoubleTag.of((double) value))
            .put(Float.class, value -> FloatTag.of((float) value))
            .put(int[].class, value -> IntArrayTag.of((int[]) value))
            .put(Integer.class, value -> IntTag.of((int) value))
            .put(Long.class, value -> LongTag.of((long) value))
            .put(Short.class, value -> ShortTag.of((short) value))
            .put(String.class, value -> StringTag.of((String) value))
            .put(UUID.class, value -> UUIDTag.of((UUID) value))
            .build();

    private PersistenceDataTypeSerializer() {

    }

    @Nullable
    public static <T> T deserialize(Tag<?> tag, PersistentDataType<T> type) throws IllegalArgumentException {
        if (tag instanceof PersistentDataTagSerialized) {
            tag = ((PersistentDataTagSerialized) tag).getPersistentDataTag(type);
        }

        checkTagType(tag, type);
        return type.getType().cast(tag.getValue());
    }

    public static <T> Tag<?> serialize(T value, PersistentDataType<T> type) {
        PersistentDataTypeContext<T> serializer = type.getContext();
        if (serializer != null)
            return PersistentDataTag.of(value, serializer);

        Tag<?> serializedTag = primitiveSerialize(value);

        Preconditions.checkState(serializedTag != null, "value " + value.getClass() + " doesnt have a valid serializer.");

        return serializedTag;
    }

    public static boolean isTagOfType(Tag<?> tag, PersistentDataType<?> type) {
        return tag instanceof PersistentDataTagSerialized ? type.getContext() != null :
                tag.getValue().getClass().isAssignableFrom(type.getType());
    }

    private static void checkTagType(Tag<?> tag, PersistentDataType<?> type) {
        if (!isTagOfType(tag, type))
            throw new IllegalArgumentException("Expected: " + type.getType().getName() + ", actual: " + tag.getValue().getClass());
    }

    private static Tag<?> primitiveSerialize(Object value) {
        Function<Object, Tag<?>> tagFunction = CLASS_TO_NBT.get(value.getClass());
        return tagFunction == null ? null : tagFunction.apply(value);
    }

}
