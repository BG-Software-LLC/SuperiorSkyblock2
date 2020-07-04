package com.bgsoftware.superiorskyblock.utils.reflections;

import java.lang.reflect.Field;

public enum Fields {

    BIOME_STORAGE_BIOME_BASES,
    BIOME_GRID_BIOME_STORAGE,

    BLOCK_FLOWER_POT_CONTENT,
    BLOCK_RANDOM_TICK,

    CHUNK_SECTION_BLOCK_IDS,
    CHUNK_SECTION_EMITTED_LIGHT,
    CHUNK_SECTION_NON_EMPTY_BLOCK_COUNT,
    CHUNK_SECTION_SKY_LIGHT,
    CHUNK_SECTION_TICKING_BLOCK_COUNT,

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

    public boolean isNull(){
        ensureNotNull();
        return field == null;
    }

}
