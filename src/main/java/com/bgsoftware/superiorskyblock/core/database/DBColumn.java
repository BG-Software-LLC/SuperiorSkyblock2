package com.bgsoftware.superiorskyblock.core.database;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

public class DBColumn extends Pair<String, Object> {

    public DBColumn(String columnName, Object value) {
        super(columnName, value);
    }

    public DBColumn withNameAndValue(String columnName, Object value) {
        setKey(columnName);
        setValue(value);
        return this;
    }

}
