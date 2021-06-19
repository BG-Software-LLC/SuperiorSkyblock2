package com.bgsoftware.superiorskyblock.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class DatabaseResult {

    private final Map<String, Object> resultSet;

    public DatabaseResult(Map<String, Object> resultSet){
        this.resultSet = resultSet;
    }

    public String getString(String key){
        return getObject(key, String.class, null);
    }

    public long getLong(String key){
        return getObject(key, Long.class, 0L);
    }

    public int getInt(String key){
        return getObject(key, Integer.class, 0);
    }

    public double getDouble(String key){
        return getObject(key, Double.class, 0D);
    }

    public boolean getBoolean(String key){
        return getObject(key, Boolean.class, false);
    }

    @NotNull
    private <T> T getObject(String key, Class<T> clazz, T def){
        Object value = resultSet.get(key);
        return value == null || !value.getClass().isAssignableFrom(clazz) ? def : clazz.cast(value);
    }

}
