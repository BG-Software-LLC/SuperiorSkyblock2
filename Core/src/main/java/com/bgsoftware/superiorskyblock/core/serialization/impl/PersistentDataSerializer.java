package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersistentDataSerializer implements ISerializer<Object, String> {

    private static final Pattern BYTE_VALUE = Pattern.compile("^([0-9]+)b$");
    private static final Pattern DOUBLE_VALUE = Pattern.compile("^([0-9.]+)d$");
    private static final Pattern FLOAT_VALUE = Pattern.compile("^([0-9]+)f$");
    private static final Pattern INT_VALUE = Pattern.compile("^([0-9]+)i$");
    private static final Pattern LONG_VALUE = Pattern.compile("^([0-9]+)l$");
    private static final Pattern SHORT_VALUE = Pattern.compile("^([0-9]+)s$");

    private final Locale locale;

    public PersistentDataSerializer(Locale locale) {
        this.locale = locale;
    }

    @Override
    public @NotNull String serialize(@Nullable Object serializable) {
        if (serializable == null)
            return "";

        String result;

        if (serializable instanceof Byte) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "b");
        } else if (serializable instanceof Double) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "d");
        } else if (serializable instanceof Float) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "f");
        } else if (serializable instanceof Integer) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "i");
        } else if (serializable instanceof Long) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "l");
        } else if (serializable instanceof Short) {
            result = Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "s");
        } else {
            result = "\"" + Message.PERSISTENT_DATA_SHOW_VALUE.getMessage(locale, serializable, "") + "\"";
        }

        return result == null ? "" : result;
    }

    @Nullable
    @Override
    public Object deserialize(@Nullable String deserializable) {
        if (deserializable == null)
            return null;

        Matcher matcher;

        if ((matcher = BYTE_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Byte.parseByte(matcher.group(1)), PersistentDataType.BYTE);
        } else if ((matcher = DOUBLE_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Double.parseDouble(matcher.group(1)), PersistentDataType.DOUBLE);
        } else if ((matcher = FLOAT_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Float.parseFloat(matcher.group(1)), PersistentDataType.FLOAT);
        } else if ((matcher = INT_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Integer.parseInt(matcher.group(1)), PersistentDataType.INTEGER);
        } else if ((matcher = LONG_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Long.parseLong(matcher.group(1)), PersistentDataType.LONG);
        } else if ((matcher = SHORT_VALUE.matcher(deserializable)).matches()) {
            return new Pair<>(Short.parseShort(matcher.group(1)), PersistentDataType.SHORT);
        }

        return new Pair<>(deserializable, PersistentDataType.STRING);
    }

}
