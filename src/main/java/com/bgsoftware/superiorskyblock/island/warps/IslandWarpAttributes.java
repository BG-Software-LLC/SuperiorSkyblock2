package com.bgsoftware.superiorskyblock.island.warps;

import java.util.EnumMap;

public final class IslandWarpAttributes {

    private final EnumMap<Field, Object> fieldValues = new EnumMap<>(Field.class);

    public IslandWarpAttributes(){

    }

    public IslandWarpAttributes setValue(Field field, Object value){
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

        NAME,
        CATEGORY,
        LOCATION,
        PRIVATE_STATUS,
        ICON

    }

}
