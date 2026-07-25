package com.bgsoftware.superiorskyblock.core.database;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.EnumHelper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DatabaseResult {

    private final Map<String, Object> resultSet;

    public DatabaseResult(Map<String, Object> resultSet) {
        this.resultSet = resultSet;
    }

    public Optional<String> getString(String key) {
        return getObject(key, String.class);
    }

    public Optional<Integer> getInt(String key) {
        return getObject(key, Integer.class);
    }

    public Optional<Long> getLong(String key) {
        Object value = getObject(key);

        if (value != null) {
            if (value instanceof Long) {
                return Optional.of((long) value);
            } else if (value instanceof Integer) {
                return Optional.of((long) (int) value);
            }
        }

        return Optional.empty();
    }

    public Optional<Double> getDouble(String key) {
        Object value = getObject(key);

        if (value != null) {
            if (value instanceof Double) {
                return Optional.of((double) value);
            } else if (value instanceof Long) {
                return Optional.of((double) (long) value);
            } else if (value instanceof Integer) {
                return Optional.of((double) (int) value);
            } else if (value instanceof BigDecimal) {
                return Optional.of(((BigDecimal) value).doubleValue());
            }
        }

        return Optional.empty();
    }

    public Optional<Boolean> getBoolean(String key) {
        Object value = getObject(key);

        if (value != null) {
            if (value instanceof Integer) {
                return Optional.of((int) value == 1);
            } else if (value instanceof Boolean) {
                return Optional.of((boolean) value);
            } else if (value instanceof Byte) {
                return Optional.of((byte) value == 1);
            }
        }

        return Optional.empty();
    }

    public Optional<Byte> getByte(String key) {
        Object value = getObject(key);

        if (value != null) {
            if (value instanceof Byte) {
                return Optional.of((byte) value);
            } else if (value instanceof Boolean) {
                return Optional.of((Boolean) value ? (byte) 1 : 0);
            } else if (value instanceof Integer) {
                return Optional.of((byte) (int) value);
            }
        }

        return Optional.empty();
    }

    public Optional<BigDecimal> getBigDecimal(String key) {
        Optional<String> value = getString(key);
        try {
            return value.map(BigDecimal::new);
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }

    public Optional<UUID> getUUID(String key) {
        Optional<String> value = getString(key);

        if (!value.isPresent())
            return Optional.empty();

        try {
            return Optional.of(UUID.fromString(value.get()));
        } catch (IllegalArgumentException error) {
            return Optional.empty();
        }
    }

    public <T extends Enum<T>> Optional<T> getEnum(String key, Class<T> enumType) {
        return getString(key).map(s -> EnumHelper.getEnum(enumType, s));

    }

    public Optional<byte[]> getBlob(String key) {
        Object obj = getObject(key);

        if (obj instanceof String) {
            return Optional.of(((String) obj).getBytes());
        } else if (obj instanceof byte[]) {
            return Optional.of((byte[]) obj);
        }

        return Optional.empty();
    }

    private <T> Optional<T> getObject(String key, Class<T> clazz) {
        Object value = getObject(key);
        return Optional.ofNullable(value == null || !value.getClass().isAssignableFrom(clazz) ? null : clazz.cast(value));
    }

    @Nullable
    private Object getObject(String key) {
        return resultSet.get(key);
    }
}
