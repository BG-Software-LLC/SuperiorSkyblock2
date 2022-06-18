package com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes;

import java.util.EnumMap;

public abstract class AttributesRegistry<K extends Enum<K>> {

    private final EnumMap<K, Object> fieldValues;

    protected AttributesRegistry(Class<K> classType) {
        fieldValues = new EnumMap<>(classType);
    }

    public AttributesRegistry<K> setValue(K field, Object value) {
        fieldValues.put(field, value);
        return this;
    }

    public <T> T getValue(K field) {
        Object value = fieldValues.get(field);
        // noinspection all
        return (T) value;
    }

    public <T> T getValue(K field, Class<T> type) {
        Object value = fieldValues.get(field);
        // noinspection all
        return type.cast(value);
    }

}
