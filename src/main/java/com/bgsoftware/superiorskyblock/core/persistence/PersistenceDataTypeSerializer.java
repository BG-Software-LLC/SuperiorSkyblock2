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
            .put(BigDecimal.class, value -> new BigDecimalTag((BigDecimal) value))
            .put(byte[].class, value -> new ByteArrayTag((byte[]) value))
            .put(Byte.class, value -> new ByteTag((byte) value))
            .put(Double.class, value -> new DoubleTag((double) value))
            .put(Float.class, value -> new FloatTag((float) value))
            .put(int[].class, value -> new IntArrayTag((int[]) value))
            .put(Integer.class, value -> new IntTag((int) value))
            .put(Long.class, value -> new LongTag((long) value))
            .put(Short.class, value -> new ShortTag((short) value))
            .put(String.class, value -> new StringTag((String) value))
            .put(UUID.class, value -> new UUIDTag((UUID) value))
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
            return new PersistentDataTag<>(value, serializer);

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
