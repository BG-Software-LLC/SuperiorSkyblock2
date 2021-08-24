package com.bgsoftware.superiorskyblock.island.attributes;

import java.util.EnumMap;

public final class IslandChestAttributes {

    private final EnumMap<Field, Object> fieldValues = new EnumMap<>(Field.class);

    public IslandChestAttributes(){

    }

    public IslandChestAttributes setValue(Field field, Object value){
        fieldValues.put(field, value);
        return this;
    }

    public <T> T getValue(Field field){
        Object value = fieldValues.get(field);
        // noinspection all
        return (T) value;
    }

    public <T> T getValue(Field field, Class<T> type){
        Object value = fieldValues.get(field);
        // noinspection all
        return type.cast(value);
    }

    public enum Field {

        INDEX,
        CONTENTS

    }

}
