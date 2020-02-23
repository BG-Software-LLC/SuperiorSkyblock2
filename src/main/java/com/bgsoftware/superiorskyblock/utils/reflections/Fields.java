package com.bgsoftware.superiorskyblock.utils.reflections;

import java.lang.reflect.Field;

public enum Fields {

    CHUNK_SECTIONS,
    CHUNK_PENDING_BLOCK_ENTITIES,
    CHUNK_HEIGHT_MAP,
    CHUNK_TILE_ENTITIES,
    CHUNK_STRUCTURE_STARTS,
    CHUNK_STRUCTURE_REFENCES,
    CHUNK_POST_PROCESSING,
    CHUNK_ENTITY_SLICES,

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
