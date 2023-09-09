package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

public class EnumHelper {

    private EnumHelper() {

    }

    @Nullable
    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String... names) {
        for (String name : names) {
            T enumValue = getEnum(enumClass, name);
            if (enumValue != null)
                return enumValue;
        }

        return null;
    }

}
