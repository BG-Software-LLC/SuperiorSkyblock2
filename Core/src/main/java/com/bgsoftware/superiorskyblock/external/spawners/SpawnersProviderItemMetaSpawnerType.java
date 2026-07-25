package com.bgsoftware.superiorskyblock.external.spawners;

import com.google.common.base.Preconditions;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Optional;

public interface SpawnersProviderItemMetaSpawnerType extends SpawnersProvider_AutoDetect {

    default String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");

        if (itemStack.getItemMeta() instanceof BlockStateMeta) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return Optional.ofNullable(creatureSpawner.getSpawnedType()).map(EntityType::name).orElse(null);
        }

        return "PIG";
    }

}
