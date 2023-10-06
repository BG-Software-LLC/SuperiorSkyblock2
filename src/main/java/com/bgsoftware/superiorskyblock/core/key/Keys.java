package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class Keys {

    public static final Key EMPTY = CustomKey.of("", null, KeyIndicator.CUSTOM);

    private static final Pattern KEY_SPLITTER_PATTERN = Pattern.compile("[:;]");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Keys() {

    }

    /* Entity keys */

    public static Key of(EntityType entityType) {
        return EntityTypeKey.of(entityType);
    }

    public static Key ofEntityType(String customType) {
        try {
            return EntityTypeKey.of(EntityType.valueOf(customType.toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException error) {
            return CustomKey.of(customType, null, KeyIndicator.ENTITY_TYPE);
        }
    }

    public static Key of(Entity entity) {
        Key baseKey = BukkitEntities.getLimitEntityType(entity);
        return plugin.getBlockValues().convertKey(baseKey, entity);
    }

    /* Block keys */

    public static Key of(Block block) {
        Material blockType = block.getType();

        Key baseKey;
        if (blockType == Materials.SPAWNER.toBukkitType()) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            baseKey = getSpawnerKeyFromCreatureSpawner(creatureSpawner);
        } else {
            short durability = ServerVersion.isLegacy() ? block.getData() : 0;
            baseKey = MaterialKey.of(blockType, durability);
        }

        return plugin.getBlockValues().convertKey(baseKey, block.getLocation());
    }

    public static Key of(BlockState blockState) {
        Key baseKey;
        if (blockState instanceof CreatureSpawner) {
            baseKey = getSpawnerKeyFromCreatureSpawner((CreatureSpawner) blockState);
        } else {
            baseKey = MaterialKey.of(blockState.getType(), blockState.getRawData());
        }

        return plugin.getBlockValues().convertKey(baseKey, blockState.getLocation());
    }

    public static Key of(Key baseKey, Location location) {
        Preconditions.checkArgument(baseKey instanceof MaterialKey);
        return plugin.getBlockValues().convertKey(baseKey, location);
    }

    /* Item keys */

    public static Key of(ItemStack itemStack) {
        Material itemType = itemStack.getType();
        Key baseKey = (itemType == Materials.SPAWNER.toBukkitType()) ?
                plugin.getProviders().getSpawnerKey(itemStack) : MaterialKey.of(itemType, itemStack.getDurability());
        return plugin.getBlockValues().convertKey(baseKey, itemStack);
    }

    public static Key of(Material type, short data) {
        return of(new ItemStack(type, 1, data));
    }

    public static Key of(Material type) {
        return type == Materials.SPAWNER.toBukkitType() ? SpawnerKey.GLOBAL_KEY : MaterialKey.of(type);
    }

    public static Key ofMaterialAndData(String material, @Nullable String data) {
        try {
            Material blockType = Material.valueOf(material);
            if (Text.isBlank(data)) {
                return Keys.of(blockType);
            }
            if (blockType == Materials.SPAWNER.toBukkitType()) {
                return ofSpawner(data);
            }
            short blockData = Short.parseShort(data);
            return Keys.of(blockType, blockData);
        } catch (Exception error) {
            return Keys.of(material, data, KeyIndicator.MATERIAL);
        }
    }

    public static Key ofMaterialAndData(String key) {
        String[] keySections = KEY_SPLITTER_PATTERN.split(key.toUpperCase(Locale.ENGLISH));
        return ofMaterialAndData(keySections[0], keySections.length >= 2 ? keySections[1] : null);
    }

    /* Spawner keys */

    public static Key ofSpawner(EntityType entityType) {
        return SpawnerKey.of(of(entityType));
    }

    public static Key ofSpawner(EntityType entityType, Location location) {
        return plugin.getBlockValues().convertKey(ofSpawner(entityType), location);
    }

    public static Key ofSpawner(String customType) {
        return SpawnerKey.of(ofEntityType(customType));
    }

    public static Key ofSpawner(String customType, Location location) {
        return plugin.getBlockValues().convertKey(ofSpawner(customType), location);
    }

    /* Custom keys */

    public static Key of(String globalKey, @Nullable String subKey, KeyIndicator keyType) {
        return CustomKey.of(globalKey, subKey, keyType);
    }

    public static Key ofCustom(String key) {
        String[] sections = KEY_SPLITTER_PATTERN.split(key);
        return of(sections[0], sections.length > 2 ? sections[1] : null, KeyIndicator.CUSTOM);
    }

    public static <T extends Key> Key of(Class<T> baseKeyClass, LazyReference<T> keyLoader) {
        return new LazyKey<>(baseKeyClass, keyLoader);
    }

    private static SpawnerKey getSpawnerKeyFromCreatureSpawner(CreatureSpawner creatureSpawner) {
        EntityTypeKey entityTypeKey = Optional.ofNullable(creatureSpawner.getSpawnedType())
                .map(EntityTypeKey::of).orElse(null);
        return SpawnerKey.of(entityTypeKey);
    }

}
