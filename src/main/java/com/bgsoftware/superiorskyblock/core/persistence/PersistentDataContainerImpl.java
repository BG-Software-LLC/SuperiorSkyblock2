package com.bgsoftware.superiorskyblock.core.persistence;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.PersistentDataTagSerialized;
import com.bgsoftware.superiorskyblock.tag.Tag;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PersistentDataContainerImpl<E> implements PersistentDataContainer {

    private final E holder;
    private final Consumer<E> saveFunction;

    private final CompoundTag innerTag = new CompoundTag();

    public PersistentDataContainerImpl(E holder, Consumer<E> saveFunction) {
        this.holder = holder;
        this.saveFunction = saveFunction;
    }

    @Override
    public boolean has(String key) {
        return innerTag.containsKey(key);
    }

    @Override
    public <T> boolean hasKeyOfType(String key, PersistentDataType<T> type) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");
        Tag<?> tag = innerTag.getTag(key);
        return tag != null && PersistenceDataTypeSerializer.isTagOfType(tag, type);
    }

    @Nullable
    @Override
    public <T> T put(String key, PersistentDataType<T> type, T value) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");
        Preconditions.checkNotNull(value, "value parameter cannot be null");
        return put(key, type, value, type);
    }

    @Nullable
    @Override
    public <T, R> R put(String key, PersistentDataType<T> type, T value, PersistentDataType<R> returnType) throws IllegalArgumentException, IllegalStateException {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");
        Preconditions.checkNotNull(value, "value parameter cannot be null");
        Preconditions.checkNotNull(returnType, "returnType parameter cannot be null");
        Tag<?> oldValue = innerTag.setTag(key, PersistenceDataTypeSerializer.serialize(value, type));

        this.saveFunction.accept(holder);

        return oldValue == null ? null : PersistenceDataTypeSerializer.deserialize(oldValue, returnType);
    }

    @Nullable
    @Override
    public Object remove(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Tag<?> oldValue = innerTag.remove(key);

        this.saveFunction.accept(holder);

        return oldValue == null ? null : oldValue.getValue();
    }

    @Nullable
    @Override
    public <T> T removeKeyOfType(String key, PersistentDataType<T> type) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");

        if (!hasKeyOfType(key, type))
            return null;

        Tag<?> oldValue = innerTag.remove(key);

        this.saveFunction.accept(holder);

        return PersistenceDataTypeSerializer.deserialize(oldValue, type);
    }

    @Nullable
    @Override
    public <T> T get(String key, PersistentDataType<T> type) throws IllegalArgumentException {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");
        return _getOfType(key, type, null);
    }

    @Nullable
    @Override
    public Object get(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        return _get(key, null);
    }

    @Override
    public <T> T getOrDefault(String key, PersistentDataType<T> type, T def) throws IllegalArgumentException {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        Preconditions.checkNotNull(type, "type parameter cannot be null");
        return _getOfType(key, type, def);
    }

    @Override
    public Object getOrDefault(String key, Object def) {
        Preconditions.checkNotNull(key, "key parameter cannot be null");
        return _get(key, def);
    }

    @Override
    public boolean isEmpty() {
        return innerTag.isEmpty();
    }

    @Override
    public int size() {
        return innerTag.size();
    }

    @Override
    public void forEach(BiConsumer<String, Object> action) {
        innerTag.getValue().forEach((key, value) -> {
            action.accept(key, value.getValue());
        });
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream byteArrayDataOutput = new ByteArrayOutputStream();

        try {
            innerTag.write(new DataOutputStream(byteArrayDataOutput));
        } catch (IOException ignored) {
        }

        return byteArrayDataOutput.toByteArray();
    }

    @Override
    public void load(byte[] data) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            this.innerTag.putAll((CompoundTag) Tag.fromStream(new DataInputStream(byteArrayInputStream), 0));
        } catch (Exception error) {
            throw new IllegalArgumentException(error);
        }
    }

    @Nullable
    private <T> T _getOfType(String key, PersistentDataType<T> type, @Nullable T def) {
        Tag<?> tag = innerTag.getTag(key);

        if (tag == null) {
            return def;
        }

        if (tag instanceof PersistentDataTagSerialized) {
            tag = ((PersistentDataTagSerialized) tag).getPersistentDataTag(type);
            innerTag.setTag(key, tag);
        }

        return PersistenceDataTypeSerializer.deserialize(tag, type);
    }

    @Nullable
    private Object _get(String key, @Nullable Object def) {
        Tag<?> tag = innerTag.getTag(key);
        return tag == null ? def : tag.getValue();
    }


}
