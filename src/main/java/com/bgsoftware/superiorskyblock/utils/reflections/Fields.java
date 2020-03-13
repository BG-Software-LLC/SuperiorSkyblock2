package com.bgsoftware.superiorskyblock.utils.reflections;

import java.lang.reflect.Field;

public enum Fields {

    BIOME_STORAGE_BIOME_BASES,
    BIOME_GRID_BIOME_STORAGE,

    CHUNK_PROVIDER_UNLOAD_QUEUE,

    BLOCK_FLOWER_POT_CONTENT,

    CRAFT_INVENTORY_INVENTORY;

    private Field field = null;

    public void set(Object obj, Object value){
        ensureNotNull();
        try{
            field.set(obj, value);
        }catch(IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

    public Object get(Object obj){
        ensureNotNull();
        try{
            return field.get(obj);
        }catch(IllegalAccessException ex){
            ex.printStackTrace();
            return null;
        }
    }

    private void ensureNotNull(){
        if(field == null)
            field = ReflectionUtils.getFromFields(this);
    }

}
