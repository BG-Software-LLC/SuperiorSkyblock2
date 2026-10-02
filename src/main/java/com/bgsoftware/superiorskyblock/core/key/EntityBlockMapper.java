package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Set;

public class EntityBlockMapper {

    private static final KeyMap<Key> ITEM_TO_BLOCK = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
    private static final KeyMap<Key> ENTITY_TO_BLOCK = KeyMaps.createArrayMap(KeyIndicator.ENTITY_TYPE);

    static {
        registerMinecart(ConstantKeys.CHEST, EntityType.MINECART_CHEST, "CHEST_MINECART", "STORAGE_MINECART");
        registerMinecart(ConstantKeys.COMMAND_BLOCK, EntityType.MINECART_COMMAND, "COMMAND_BLOCK_MINECART", "COMMAND_MINECART");
        registerMinecart(ConstantKeys.FURNACE, EntityType.MINECART_FURNACE, "FURNACE_MINECART", "POWERED_MINECART");
        registerMinecart(ConstantKeys.HOPPER, EntityType.MINECART_HOPPER, "HOPPER_MINECART");
        registerMinecart(ConstantKeys.MOB_SPAWNER, EntityType.MINECART_MOB_SPAWNER);
        registerMinecart(ConstantKeys.TNT, EntityType.MINECART_TNT, "TNT_MINECART", "EXPLOSIVE_MINECART");
        registerChestBoats(
                new String[]{"ACACIA_CHEST_BOAT", "BAMBOO_CHEST_RAFT", "BIRCH_CHEST_BOAT", "CHERRY_CHEST_BOAT",
                        "DARK_OAK_CHEST_BOAT", "JUNGLE_CHEST_BOAT", "MANGROVE_CHEST_BOAT", "OAK_CHEST_BOAT",
                        "PALE_OAK_CHEST_BOAT", "SPRUCE_CHEST_BOAT", "CHEST_BOAT"},
                new String[]{"ACACIA_CHEST_BOAT", "BAMBOO_CHEST_RAFT", "BIRCH_CHEST_BOAT", "CHERRY_CHEST_BOAT",
                        "DARK_OAK_CHEST_BOAT", "JUNGLE_CHEST_BOAT", "MANGROVE_CHEST_BOAT", "OAK_CHEST_BOAT",
                        "PALE_OAK_CHEST_BOAT", "SPRUCE_CHEST_BOAT"}
        );
    }

    @Nullable
    public static Key getBlockFromItem(Key itemKey) {
        return ITEM_TO_BLOCK.get(itemKey);
    }

    @Nullable
    public static Key getBlockFromEntity(Key entityKey) {
        return ENTITY_TO_BLOCK.get(entityKey);
    }

    public static Set<Map.Entry<Key, Key>> getTrackerMappings() {
        return ENTITY_TO_BLOCK.entrySet();
    }

    private static void registerMinecart(Key blockKey, EntityType entityType, String... materialNames) {
        Key entityTypeKey = Keys.of(entityType);
        ENTITY_TO_BLOCK.put(entityTypeKey, blockKey);

        for (String materialName : materialNames) {
            Key materialKey = getMaterialKey(materialName);

            if (materialKey != null) {
                ITEM_TO_BLOCK.put(materialKey, blockKey);
            }
        }
    }

    private static void registerChestBoats(String[] entityTypeNames, String[] materialNames) {
        for (String entityTypeName : entityTypeNames) {
            Key entityTypeKey = getEntityTypeKey(entityTypeName);

            if (entityTypeKey != null) {
                ENTITY_TO_BLOCK.put(entityTypeKey, ConstantKeys.CHEST);
            }
        }

        for (String materialName : materialNames) {
            Key materialKey = getMaterialKey(materialName);

            if (materialKey != null) {
                ITEM_TO_BLOCK.put(materialKey, ConstantKeys.CHEST);
            }
        }
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
