package com.bgsoftware.superiorskyblock.utils.reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum Methods {

    PLAYER_CHUNK_MAP_IS_OUTSIDE_OF_RANGE;

    private Method method = null;

    public Object invoke(Object obj, Object... args){
        ensureNotNull();
        try{
            return method.invoke(obj, args);
        }catch(IllegalAccessException | InvocationTargetException ex){
            ex.printStackTrace();
            return null;
        }
    }

    private void ensureNotNull(){
        if(method == null)
            method = ReflectionUtils.getFromMethod(this);
    }

}
