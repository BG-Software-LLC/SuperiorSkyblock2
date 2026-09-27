package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EntityBlockMapper {

    private static final KeyMap<Key> ITEM_TO_BLOCK = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
    private static final KeyMap<Key> ENTITY_TO_BLOCK = KeyMaps.createArrayMap(KeyIndicator.ENTITY_TYPE);
    private static final List<Pair<Key, Key>> TRACKER_TO_BLOCK = new LinkedList<>();

    static {
        // Minecarts
        register(getMaterialKey("CHEST_MINECART", "STORAGE_MINECART"), Keys.of(EntityType.MINECART_CHEST), ConstantKeys.CHEST);
        register(getMaterialKey("COMMAND_BLOCK_MINECART", "COMMAND_MINECART"), Keys.of(EntityType.MINECART_COMMAND), ConstantKeys.COMMAND_BLOCK);
        register(getMaterialKey("FURNACE_MINECART", "POWERED_MINECART"), Keys.of(EntityType.MINECART_FURNACE), ConstantKeys.FURNACE);
        register(getMaterialKey("HOPPER_MINECART"), Keys.of(EntityType.MINECART_HOPPER), ConstantKeys.HOPPER);
        register(null, Keys.of(EntityType.MINECART_MOB_SPAWNER), ConstantKeys.MOB_SPAWNER);
        register(getMaterialKey("TNT_MINECART", "EXPLOSIVE_MINECART"), Keys.of(EntityType.MINECART_TNT), ConstantKeys.TNT);

        // Boats
        register(getMaterialKey("ACACIA_CHEST_BOAT"), getEntityTypeKey("ACACIA_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("BAMBOO_CHEST_RAFT"), getEntityTypeKey("BAMBOO_CHEST_RAFT"), ConstantKeys.CHEST);
        register(getMaterialKey("BIRCH_CHEST_BOAT"), getEntityTypeKey("BIRCH_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("CHERRY_CHEST_BOAT"), getEntityTypeKey("CHERRY_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("DARK_OAK_CHEST_BOAT"), getEntityTypeKey("DARK_OAK_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("JUNGLE_CHEST_BOAT"), getEntityTypeKey("JUNGLE_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("MANGROVE_CHEST_BOAT"), getEntityTypeKey("MANGROVE_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("OAK_CHEST_BOAT"), getEntityTypeKey("OAK_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("PALE_OAK_CHEST_BOAT"), getEntityTypeKey("PALE_OAK_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("POPLAR_CHEST_BOAT"), getEntityTypeKey("POPLAR_CHEST_BOAT"), ConstantKeys.CHEST);
        register(getMaterialKey("SPRUCE_CHEST_BOAT"), getEntityTypeKey("SPRUCE_CHEST_BOAT"), ConstantKeys.CHEST);
    }

    private static void register(@Nullable Key itemKey, @Nullable Key entityKey, @Nullable Key blockKey) {
        if (entityKey == null || blockKey == null) {
            return;
        }

        if (itemKey != null) {
            ITEM_TO_BLOCK.put(itemKey, blockKey);
        }

        ENTITY_TO_BLOCK.put(entityKey, blockKey);
        TRACKER_TO_BLOCK.add(new Pair<>(entityKey, blockKey));
    }

    @Nullable
    public static Key getBlockFromItem(Key itemKey) {
        return ITEM_TO_BLOCK.get(itemKey);
    }

    @Nullable
    public static Key getBlockFromEntity(Key entityKey) {
        return ENTITY_TO_BLOCK.get(entityKey);
    }

    public static List<Pair<Key, Key>> getTrackerMappings() {
        return Collections.unmodifiableList(TRACKER_TO_BLOCK);
    }

    @Nullable
    private static Key getEntityTypeKey(String... names) {
        EntityType entityType = EnumHelper.getEnum(EntityType.class, names);

        if (entityType != null) {
            return Keys.of(entityType);
        }

        return null;
    }

    @Nullable
    private static Key getMaterialKey(String... names) {
        Material material = EnumHelper.getEnum(Material.class, names);

        if (material != null) {
            return Keys.of(material);
        }

        return null;
    }

}
