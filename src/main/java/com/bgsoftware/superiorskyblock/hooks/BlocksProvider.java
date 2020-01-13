package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public interface BlocksProvider extends SpawnersProvider {

    default Key getSpawnerKey(ItemStack itemStack){
        if(itemStack.getItemMeta() instanceof BlockStateMeta){
            CreatureSpawner creatureSpawner = (CreatureSpawner) ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
            return Key.of(Materials.SPAWNER.toBukkitType() + ":" + creatureSpawner.getSpawnedType());
        }

        return Key.of(itemStack);
    }

    default Pair<Integer, Material> getBlock(Location location){
        return null;
    }

}
