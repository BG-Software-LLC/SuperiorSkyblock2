package com.bgsoftware.superiorskyblock.utils.reflections;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;

public final class ReflectMethod<T> {

    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private final Method method;

    public ReflectMethod(String classPath, String methodName, Class<?>... parameterTypes){
        this(getClass(classPath), methodName, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, String methodName){
        this(clazz, methodName, new Class[0]);
    }

    public ReflectMethod(Class<?> clazz, String methodName, String... parameterTypeNames){
        this(clazz, methodName, getClasses(parameterTypeNames));
    }

    public ReflectMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes){
        this(clazz, null, methodName, parameterTypes);
    }

    public ReflectMethod(Class<?> clazz, Class<?> returnType, String methodName, Class<?>... parameterTypes){
        this.method = getMethod(clazz, methodName, returnType, parameterTypes);
    }

    public T invoke(Object instance, Object... args){
        return invokeWithDef(instance, null, args);
    }

    public T invokeWithDef(Object instance, T def, Object... args){
        Object result = null;

        try{
            if(method != null)
                result = method.invoke(instance, args);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        //noinspection unchecked
        return result == null ? def : (T) result;
    }

    public boolean isValid(){
        return method != null;
    }

    private static Method getMethod(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes){
        Method method = null;

        if(clazz != null) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                if(returnType != null && !method.getReturnType().equals(returnType)){
                    method = null;
                }
                else {
                    method.setAccessible(true);
                }
            } catch (Exception ignored) {}
        }

        return method;
    }

    private static Class<?> getClass(String classPath){
        return getClasses(classPath)[0];
    }

    private static Class<?>[] getClasses(String... classPaths){
        Class<?>[] classes = new Class[classPaths.length];

        try{
            for(int i = 0; i < classPaths.length; i++)
                classes[i] = Class.forName(classPaths[i].replace("VERSION", version));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return classes;
    }

}
