package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.google.common.base.Preconditions;
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
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return Key.of(entityType).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        return Key.of(entity).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return Key.of(block).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(BlockState blockState) {
        Preconditions.checkNotNull(blockState, "blockState parameter cannot be null.");
        return Key.of(blockState).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "material parameter cannot be null.");
        return Key.of(itemStack).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(Material material, short data) {
        Preconditions.checkNotNull(material, "material parameter cannot be null.");
        return Key.of(material, data).markAPIKey();
    }

    @Override
    public com.bgsoftware.superiorskyblock.api.key.Key getKey(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return Key.of(key).markAPIKey();
    }

}
