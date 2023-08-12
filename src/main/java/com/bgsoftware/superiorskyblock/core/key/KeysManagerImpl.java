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
        return ((BaseKey<? extends Key>) Keys.of(entityType)).markAPIKey();
    }

    @Override
    public Key getKey(Entity entity) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(entity)).markAPIKey();
    }

    @Override
    public Key getKey(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(block)).markAPIKey();
    }

    @Override
    public Key getKey(BlockState blockState) {
        Preconditions.checkNotNull(blockState, "blockState parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(blockState)).markAPIKey();
    }

    @Override
    public Key getKey(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "material parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(itemStack)).markAPIKey();
    }

    @Override
    public Key getKey(Material material, short data) {
        Preconditions.checkNotNull(material, "material parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(material, data)).markAPIKey();
    }

    @Override
    public Key getKey(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        String[] keySections = key.split(":");
        return ((BaseKey<? extends Key>) Keys.of(keySections[0], keySections.length >= 2 ? keySections[1] : null)).markAPIKey();
    }

    @Override
    public Key getKey(String globalKey, String subKey) {
        Preconditions.checkNotNull(globalKey, "globalKey parameter cannot be null.");
        Preconditions.checkNotNull(subKey, "subKey parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(globalKey, subKey)).markAPIKey();
    }

    @Override
    public KeySet createKeySet(Supplier<Set<String>> setCreator) {
        return KeySets.createSet(KeyIndicator.CUSTOM, setCreator::get);
    }

    @Override
    public KeySet createKeySet(Supplier<Set<String>> setCreator, Collection<Key> collection) {
        if (collection instanceof KeySet) return (KeySet) collection;
        KeySet keySet = KeySets.createSet(KeyIndicator.CUSTOM, setCreator::get);
        keySet.addAll(collection);
        return keySet;
    }

    @Override
    public <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator) {
        return KeyMaps.createMap(KeyIndicator.CUSTOM, mapCreator::get);
    }

    @Override
    public <V> KeyMap<V> createKeyMap(Supplier<Map<String, V>> mapCreator, Map<Key, V> map) {
        if (map instanceof KeyMap) return (KeyMap<V>) map;
        KeyMap<V> keyMap = KeyMaps.createMap(KeyIndicator.CUSTOM, mapCreator::get);
        keyMap.putAll(map);
        return keyMap;
    }

}
