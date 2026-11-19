package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

public class Text {

    // Returned for strings that are not numbers. It is outside the range of all the parsed types.
    private static final long INVALID_NUMBER = Long.MIN_VALUE;

    private Text() {

    }

    public static boolean isBlank(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    public static StringBuilder appendWithLine(StringBuilder stringBuilder, Object value) {
        if (stringBuilder.length() != 0)
            stringBuilder.append("\n");

        return stringBuilder.append(value);
    }

    public static boolean isInt(@Nullable String string) {
        return parseNumberInRange(string, Integer.MIN_VALUE, Integer.MAX_VALUE) != INVALID_NUMBER;
    }

    public static int parseInt(@Nullable String string, int def) {
        long value = parseNumberInRange(string, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return value == INVALID_NUMBER ? def : (int) value;
    }

    public static short parseShort(@Nullable String string, short def) {
        long value = parseNumberInRange(string, Short.MIN_VALUE, Short.MAX_VALUE);
        return value == INVALID_NUMBER ? def : (short) value;
    }

    private static long parseNumberInRange(@Nullable String string, long minValue, long maxValue) {
        if (isBlank(string))
            return INVALID_NUMBER;

        int index = 0;
        char firstChar = string.charAt(0);
        boolean negative = firstChar == '-';

        if (negative || firstChar == '+') {
            if (string.length() == 1)
                return INVALID_NUMBER;
            index = 1;
        }

        long limit = negative ? -minValue : maxValue;
        long value = 0;

        for (; index < string.length(); ++index) {
            // Same digits the parse methods accept.
            int digit = Character.digit(string.charAt(index), 10);
            if (digit < 0)
                return INVALID_NUMBER;

            value = value * 10 + digit;
            if (value > limit)
                return INVALID_NUMBER;
        }

        return negative ? -value : value;
    }

}
