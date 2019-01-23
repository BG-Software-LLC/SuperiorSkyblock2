package com.ome_r.superiorskyblock.utils.key;

import com.ome_r.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Key {

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
        return block.getType() == Materials.SPAWNER.toBukkitType() ? of(block.getType() + ":" + ((CreatureSpawner) block.getState()).getSpawnedType()) :
                of(block.getState().getData().toItemStack());
    }

    public static Key of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
    }

    public static Key of(Material material, short data){
        return of(material + ":" + data);
    }

    public static Key of(String key){
        return new Key(key);
    }

}
