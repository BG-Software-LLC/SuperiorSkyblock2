package com.bgsoftware.superiorskyblock.database.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class ResultSetMapBridge implements Map<String, Object> {

    private final ResultSet resultSet;

    public ResultSetMapBridge(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        try {
            get(key);
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @Override
    public Object get(Object key) {
        try {
            return resultSet.getObject(key + "");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

}
