package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public final class KeysHandler extends AbstractHandler implements KeysManager {

    public KeysHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData() {

    }

    @Override
    public boolean isSpawner(Material type) {
        return Materials.SPAWNER.toBukkitType() == type;
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getSpawnerKey(ItemStack itemStack) {
        return Key.of(itemStack).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getSpawnerKey(Block block) {
        return Key.of(block).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getSpawnerKey(BlockState blockState) {
        return Key.of(blockState).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(EntityType entityType) {
        return Key.of(entityType).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Entity entity) {
        return Key.of(entity).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Block block) {
        return Key.of(block).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(BlockState blockState) {
        return Key.of(blockState).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(ItemStack itemStack) {
        return Key.of(itemStack).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Material material, short data) {
        return Key.of(material, data).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(String key) {
        return Key.of(key).markAPIKey();
    }

}
