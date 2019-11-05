package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public interface BlocksProvider {

    Pair<Integer, EntityType> getSpawner(Location location);

    default Key getSpawnerKey(ItemStack itemStack){
        if(itemStack.getItemMeta() instanceof BlockStateMeta){
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return Key.of(Materials.SPAWNER.toBukkitType() + ":" + creatureSpawner.getSpawnedType());
        }

        return Key.of(Materials.SPAWNER.toBukkitType() + "");
    }

    default Pair<Integer, Material> getBlock(Location location){
        return null;
    }

}
