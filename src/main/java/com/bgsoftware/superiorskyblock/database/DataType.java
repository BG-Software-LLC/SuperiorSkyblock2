package com.bgsoftware.superiorskyblock.database;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum DataType {

    STRING,
    INT,
    SHORT,
    LONG,
    FLOAT,
    DOUBLE,
    BIG_DECIMAL,
    BOOLEAN;

    public void set(PreparedStatement statement, int index, Object object) throws SQLException {
        switch (this) {
            case STRING:
                statement.setString(index, (String) object);
                break;
            case INT:
                statement.setInt(index, (int) object);
                break;
            case SHORT:
                statement.setShort(index, (short) object);
                break;
            case LONG:
                statement.setLong(index, (long) object);
                break;
            case FLOAT:
                statement.setFloat(index, (float) object);
                break;
            case DOUBLE:
                statement.setDouble(index, (double) object);
                break;
            case BIG_DECIMAL:
                statement.setBigDecimal(index, (BigDecimal) object);
                break;
            case BOOLEAN:
                statement.setBoolean(index, (boolean) object);
                break;
            default:
                statement.setObject(index, object);
                break;
        }
    }

}
