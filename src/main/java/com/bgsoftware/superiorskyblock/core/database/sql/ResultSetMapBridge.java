package com.bgsoftware.superiorskyblock.core.database.sql;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ResultSetMapBridge implements Map<String, Object> {

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
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("This operation is not supported on this map.");
    }

    @Override
    public Object get(Object key) {
        return getSafe(key + "");
    }

    public <T> T get(Object key, T def) {
        try {
            return get(key + "");
        } catch (SQLException ex) {
            return def;
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

    private <T> T get(String key) throws SQLException {
        // noinspection all
        return (T) resultSet.getObject(key);
    }

    private <T> T getSafe(String key) {
        try {
            return get(key + "");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}
