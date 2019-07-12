package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class Key {

    private String key;

    private Key(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        if(obj instanceof Key){
            String key = obj.toString();
            if(this.key.equals(key))
                return true;
            else if(key.contains(":") && this.key.equals(key.split(":")[0]))
                return true;
            else if(key.contains(";") && this.key.equals(key.split(";")[0]))
                return true;
        }
        return false;
    }

    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    public static Key of(Block block){
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getGrid().isSpawner(block.getType()) ?
                of(block.getType() + ":" + ((CreatureSpawner) block.getState()).getSpawnedType()) :
                of(block.getState().getData().toItemStack());
    }

    public static Key of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
    }

    public static Key of(Material material, short data){
        return of(material + ":" + data);
    }

    public static Key of(String key){
        return new Key(key.replace("LEGACY_", ""));
    }

}
