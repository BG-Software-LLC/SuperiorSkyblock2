package com.bgsoftware.superiorskyblock.utils.reflections;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;

public final class ReflectField<T> {

    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private final Field field;

    public ReflectField(String clazzName, Class<?> returnType, String... fieldNames){
        this(getClass(clazzName), returnType, fieldNames);
    }

    public ReflectField(Class<?> clazz, Class<?> returnType, String... fieldNames){
        this.field = getField(clazz, returnType, fieldNames);
    }

    public T get(Object instance){
        return get(instance, null);
    }

    public T get(Object instance, T def){
        Object result = null;

        try{
            if(field != null)
                result = field.get(instance);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? def : (T) result;
    }

    public void set(Object instance, T value){
        try{
            if(field != null)
                field.set(instance, value);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static Field getField(Class<?> clazz, Class<?> returnType, String... fieldNames){
        Field field = null;

        if(clazz != null) {
            for (String fieldName : fieldNames) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                    if (returnType == null || returnType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        break;
                    }
                }catch (Exception ignored){}
            }
        }

        return field;
    }

    private static Class<?> getClass(String classPath){
        try{
            return Class.forName(classPath.replace("VERSION", version));
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}
