package com.bgsoftware.superiorskyblock.database;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class CachedResultSet {

    private final Map<String, Object> cache = new HashMap<>();

    public CachedResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        for(int i = 1; i <= metaData.getColumnCount(); i++) {
            cache.put(metaData.getColumnName(i), resultSet.getObject(i));
        }
    }

    public int getInt(String key){
        return (int) cache.get(key);
    }

    public long getLong(String key){
        if(cache.get(key) instanceof String)
            return Long.parseLong((String) cache.get(key));
        else
            return (long) cache.get(key);
    }

    public String getString(String key){
        return (String) cache.get(key);
    }

    public double getDouble(String key){
        return cache.get(key) instanceof Integer ? getInt(key) : cache.get(key) instanceof Long ? getLong(key) : (double) cache.get(key);
    }

    public boolean getBoolean(String key){
        return getInt(key) != 0;
    }

}
