package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.lang.reflect.Field;

public class EnumHelper {

    private EnumHelper() {

    }

    @Nullable
    public static <T> T getEnum(Class<T> enumClass, String name) {
        return enumClass.isInterface() ? getInterfaceEnumValue(enumClass, name) : getEnumValue(enumClass, name);
    }

    public static <T> T getEnum(Class<T> enumClass, String... names) {
        if(enumClass.isInterface()) {
            for (String name : names) {
                T enumValue = getInterfaceEnumValue(enumClass, name);
                if (enumValue != null)
                    return enumValue;
            }
        } else {
            for (String name : names) {
                T enumValue = getEnumValue(enumClass, name);
                if (enumValue != null)
                    return enumValue;
            }
        }

        return null;
    }

    @Nullable
    private static <T> T getInterfaceEnumValue(Class<T> enumClass, String name) {
        try {
            Field field = enumClass.getDeclaredField(name);
            return (T) field.get(null);
        } catch (Throwable error) {
            return null;
        }
    }

    @Nullable
    private static <T> T getEnumValue(Class enumClass, String name) {
        try {
            return (T) Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }



}
