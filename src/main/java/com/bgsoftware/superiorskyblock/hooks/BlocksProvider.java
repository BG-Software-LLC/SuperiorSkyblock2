package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public interface BlocksProvider extends SpawnersProvider {

    default EntityType getSpawnerType(ItemStack itemStack){
        if(itemStack.getItemMeta() instanceof BlockStateMeta){
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return creatureSpawner.getSpawnedType();
        }

        return EntityType.PIG;
    }

    default Pair<Integer, ItemStack> getBlock(Location location){
        return null;
    }

}
