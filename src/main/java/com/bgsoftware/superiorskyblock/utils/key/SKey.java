package com.bgsoftware.superiorskyblock.utils.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class SKey implements Key {

    private String key;

    private SKey(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        if(obj instanceof SKey){
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

    public static SKey of(EntityType entityType){
        return of(entityType.name());
    }

    public static SKey of(Block block){
        return block.getType() == Materials.SPAWNER.toBukkitType() ? of(block.getType() + ":" + ((CreatureSpawner) block.getState()).getSpawnedType()) :
                of(block.getState().getData().toItemStack());
    }

    public static SKey of(ItemStack itemStack){
        return of(itemStack.getType(), itemStack.getDurability());
    }

    public static SKey of(Material material, short data){
        return of(material + ":" + data);
    }

    public static SKey of(String key){
        return new SKey(key.replace("LEGACY_", ""));
    }

}
