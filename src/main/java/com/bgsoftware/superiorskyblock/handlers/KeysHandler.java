package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.inventory.ItemStack;

public final class KeysHandler implements KeysManager {

    private final SuperiorSkyblockPlugin plugin;

    public KeysHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean isSpawner(Material type) {
        return Materials.SPAWNER.toBukkitType() == type;
    }

    @Override
    public Key getSpawnerKey(ItemStack itemStack) {
        return plugin.getProviders().getSpawnerKey(itemStack);
    }

    @Override
    public Key getSpawnerKey(Block block) {
        return getSpawnerKey(block.getState());
    }

    @Override
    public Key getSpawnerKey(BlockState blockState) {
        if(blockState instanceof CreatureSpawner){
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
            return Key.of(Materials.SPAWNER.toBukkitType() + ":" + creatureSpawner.getSpawnedType());
        }

        return Key.of(blockState.getData().toItemStack());
    }
}
