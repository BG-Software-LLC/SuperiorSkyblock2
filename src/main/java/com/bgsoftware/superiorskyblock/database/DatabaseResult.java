package com.bgsoftware.superiorskyblock.database;

import java.math.BigDecimal;
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
        Object value = getObject(key, 0L);

        if(value instanceof Long) {
            return (long) value;
        }
        else if(value instanceof Integer) {
            return (int) value;
        }
        else {
            return 0L;
        }
    }

    public int getInt(String key){
        return getObject(key, Integer.class, 0);
    }

    public double getDouble(String key){
        Object value = getObject(key, 0D);

        if(value instanceof Double){
            return (double) value;
        }
        else if(value instanceof Long) {
            return (double) (long) value;
        }
        else if(value instanceof Integer) {
            return (double) (int) value;
        }
        else if(value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        else {
            return 0L;
        }
    }

    public boolean getBoolean(String key){
        Object value = getObject(key, false);

        if(value instanceof Integer) {
            return (int) value == 1;
        }
        else if(value instanceof Boolean) {
            return (boolean) value;
        }
        else {
            return false;
        }
    }

    public BigDecimal getBigDecimal(String key){
        String value = getString(key);
        try{
            return new BigDecimal(value);
        }catch (NumberFormatException | NullPointerException ex){
            return BigDecimal.ZERO;
        }
    }

    private Object getObject(String key, Object def){
        return resultSet.getOrDefault(key, def);
    }

    private <T> T getObject(String key, Class<T> clazz, T def){
        Object value = resultSet.get(key);
        return value == null || !value.getClass().isAssignableFrom(clazz) ? def : clazz.cast(value);
    }

}
