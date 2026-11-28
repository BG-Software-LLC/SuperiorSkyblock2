package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnumHelper {

    // The constants of every looked-up class by their names, collected once per class.
    // Names that do not exist are common (constants of newer versions, custom keys), and looking them up
    // in a map is much cheaper than catching the exception Enum#valueOf and Class#getDeclaredField throw.
    private static final ClassValue<Map<String, Object>> CONSTANTS = new ClassValue<Map<String, Object>>() {
        @Override
        protected Map<String, Object> computeValue(Class<?> type) {
            return type.isInterface() ? collectInterfaceConstants(type) : collectEnumConstants(type);
        }
    };

    private EnumHelper() {

    }

    @Nullable
    public static <T> T getEnum(Class<T> enumClass, String name) {
        return getConstant(enumClass, name);
    }

    @Nullable
    public static <T> T getEnum(Class<T> enumClass, String... names) {
        Map<String, Object> constants = CONSTANTS.get(enumClass);

        for (String name : names) {
            Object constant = constants.get(name);
            if (constant != null)
                return (T) constant;
        }

        return null;
    }

    @Nullable
    private static <T> T getConstant(Class<T> enumClass, @Nullable String name) {
        return name == null ? null : (T) CONSTANTS.get(enumClass).get(name);
    }

    private static Map<String, Object> collectEnumConstants(Class<?> enumClass) {
        // Null for classes that are not enums, which do not have any constants to look up.
        Object[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null)
            return Collections.emptyMap();

        Map<String, Object> constants = new HashMap<>();
        for (Object enumConstant : enumConstants)
            constants.put(((Enum<?>) enumConstant).name(), enumConstant);

        return constants;
    }

    private static Map<String, Object> collectInterfaceConstants(Class<?> interfaceClass) {
        Map<String, Object> constants = new HashMap<>();

        for (Field field : interfaceClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !interfaceClass.isAssignableFrom(field.getType()))
                continue;

            try {
                Object constant = field.get(null);
                if (constant != null)
                    constants.put(field.getName(), constant);
            } catch (Throwable ignored) {
                // The constant cannot be read, therefore it cannot be returned either.
            }
        }

        return constants;
    }

}
