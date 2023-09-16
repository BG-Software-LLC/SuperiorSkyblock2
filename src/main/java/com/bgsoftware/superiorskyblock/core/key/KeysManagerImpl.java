package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.KeysManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
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
    public Key getEntityTypeKey(String entityTypeName) {
        Preconditions.checkNotNull(entityTypeName, "entityTypeName parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.ofEntityType(entityTypeName)).markAPIKey();
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
    public Key getKey(Material material) {
        Preconditions.checkNotNull(material, "material parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(material)).markAPIKey();
    }

    @Override
    public Key getMaterialAndDataKey(String type) {
        Preconditions.checkNotNull(type, "type parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.ofMaterialAndData(type)).markAPIKey();
    }

    @Override
    public Key getSpawnerKey(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.ofSpawner(entityType)).markAPIKey();
    }

    @Override
    public Key getSpawnerKey(String entityTypeName) {
        Preconditions.checkNotNull(entityTypeName, "entityTypeName parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.ofSpawner(entityTypeName)).markAPIKey();
    }

    @Override
    public Key getKey(String key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        // Due to backwards compatibility, we want to try and check if this is either a MaterialKey or EntityTypeKey.
        Key materialKey = Key.ofMaterialAndData(key);
        if (materialKey instanceof MaterialKey)
            return materialKey;

        Key entityTypeKey = Key.ofEntityType(key);
        if (entityTypeKey instanceof EntityTypeKey)
            return entityTypeKey;

        // This key does not fit MaterialKey nor EntityTypeKey, therefore we'll create a CustomKey.

        String[] keySections = key.split(":");
        Key customKey = Keys.of(keySections[0], keySections.length >= 2 ? keySections[1] : null, KeyIndicator.CUSTOM);
        return ((BaseKey<? extends Key>) customKey).markAPIKey();
    }

    @Override
    public Key getKey(String globalKey, String subKey) {
        Preconditions.checkNotNull(globalKey, "globalKey parameter cannot be null.");
        Preconditions.checkNotNull(subKey, "subKey parameter cannot be null.");
        return ((BaseKey<? extends Key>) Keys.of(globalKey, subKey, KeyIndicator.CUSTOM)).markAPIKey();
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
