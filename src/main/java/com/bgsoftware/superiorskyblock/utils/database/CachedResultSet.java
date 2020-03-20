package com.bgsoftware.superiorskyblock.utils.database;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class CachedResultSet {

    private final Map<String, Object> cache;

    public CachedResultSet(Map<String, Object> cache) {
        this.cache = cache;
    }

    public CachedResultSet(ResultSet resultSet) throws SQLException {
        this(new HashMap<>());
        ResultSetMetaData metaData = resultSet.getMetaData();
        for(int i = 1; i <= metaData.getColumnCount(); i++) {
            cache.put(metaData.getColumnName(i), resultSet.getObject(i));
        }
    }

    public int getInt(String key){
        Object object = cache.get(key);

        if(object instanceof String)
            return Integer.parseInt((String) object);
        else if(object instanceof Integer)
            return (int) object;
        else if(object instanceof Double)
            return (int) (double) object;
        else if(object instanceof Boolean)
            return (Boolean) object ? 1 : 0;
        else if(object instanceof BigDecimal)
            return ((BigDecimal) object).intValue();

        throw new IllegalArgumentException("Invalid type " + object.getClass());
    }

    public long getLong(String key){
        Object object = cache.get(key);

        if(object instanceof String)
            return Long.parseLong((String) object);
        else if(object instanceof Integer)
            return (long) (int) object;
        else if(object instanceof Double)
            return (long) (double) object;
        else if(object instanceof Boolean)
            return (Boolean) object ? 1L : 0L;
        else if(object instanceof BigDecimal)
            return ((BigDecimal) object).longValue();

        throw new IllegalArgumentException("Invalid type " + object.getClass());
    }

    public String getString(String key){
        return (String) cache.get(key);
    }

    public double getDouble(String key){
        Object object = cache.get(key);

        if(object instanceof String)
            return Double.parseDouble((String) object);
        else if(object instanceof Integer)
            return (double) (int) object;
        else if(object instanceof Double)
            return (Double) object;
        else if(object instanceof Boolean)
            return (Boolean) object ? 1D : 0D;
        else if(object instanceof BigDecimal)
            return ((BigDecimal) object).doubleValue();

        throw new IllegalArgumentException("Invalid type " + object.getClass());
    }

    public boolean getBoolean(String key){
        return cache.get(key) instanceof Boolean ? Boolean.parseBoolean(cache.get(key).toString()) : getInt(key) != 0;
    }

    public void delete(){
        cache.clear();
    }

}
