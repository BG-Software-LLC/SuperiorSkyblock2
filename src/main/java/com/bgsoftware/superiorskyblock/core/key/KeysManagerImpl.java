package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class KeysManagerImpl extends Manager implements KeysManager {

    public KeysManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() {
        // No data to be loaded.
    }

    @Override
    public Key getKey(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(entityType)).markAPIKey();
    }

    @Override
    public Key getKey(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(entity)).markAPIKey();
    }

    @Override
    public Key getKey(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(block)).markAPIKey();
    }

    @Override
    public Key getKey(BlockState blockState) {
        Preconditions.checkNotNull(blockState, "blockState parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(blockState)).markAPIKey();
    }

    @Override
    public Key getKey(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "material parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(itemStack)).markAPIKey();
    }

    @Override
    public Key getKey(Material material, short data) {
        Preconditions.checkNotNull(material, "material parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(material, data)).markAPIKey();
    }

    @Override
    public Key getKey(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(key)).markAPIKey();
    }

    @Override
    public Key getKey(String globalKey, String subKey) {
        Preconditions.checkNotNull(globalKey, "globalKey parameter cannot be null.");
        Preconditions.checkNotNull(subKey, "subKey parameter cannot be null.");
        return ((KeyImpl) KeyImpl.of(globalKey, subKey)).markAPIKey();
    }

    @Override
    public KeySet createKeySet(Supplier<Set<String>> setCreator) {
        return KeySetImpl.create(setCreator);
    }

    @Override
    public KeySet createKeySet(Supplier<Set<String>> setCreator, Collection<Key> collection) {
        return collection instanceof KeySet ? (KeySet) collection : KeySetImpl.create(setCreator, collection);
    }

    @Override
    public <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator) {
        return KeyMapImpl.create(mapCreator);
    }

    @Override
    public <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator, Map<Key, V> map) {
        return map instanceof KeyMap ? (KeyMap<V>) map : KeyMapImpl.create(mapCreator, map);
    }

}
