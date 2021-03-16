package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.google.common.base.Preconditions;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Collections;
import java.util.Set;

public interface BlocksProvider extends SpawnersProvider {

    default String getSpawnerType(ItemStack itemStack){
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");

        if(itemStack.getItemMeta() instanceof BlockStateMeta){
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return creatureSpawner.getSpawnedType().name();
        }

        return "PIG";
    }

    default Set<Pair<Key, Integer>> getBlocks(ChunkPosition chunkPosition){
        return Collections.emptySet();
    }

}
