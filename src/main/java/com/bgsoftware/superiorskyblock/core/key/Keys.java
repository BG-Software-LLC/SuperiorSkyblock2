package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
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

    private static final Pattern KEY_SPLITTER_PATTERN = Pattern.compile("[:;]");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Keys() {

    }

    /* Entity keys */

    public static Key of(Entity entity) {
        return plugin.getKeys().convertKey(Keys.of(entity.getType()), entity);
    }

    public static Key of(EntityType entityType) {
        return EntityTypeKey.of(entityType);
    }

    public static Key ofEntityType(String key) {
        String[] sections = KEY_SPLITTER_PATTERN.split(key.toUpperCase(Locale.ENGLISH), 2);

        return Keys.ofEntityType(sections[0], sections.length >= 2 ? sections[1] : null);
    }

    private static Key ofEntityType(String entityTypeName, @Nullable String data) {
        EntityType entityType = EnumHelper.getEnum(EntityType.class, entityTypeName.toUpperCase(Locale.ENGLISH));

        if (entityType == null) {
            return Keys.of(entityTypeName, data, KeyIndicator.ENTITY_TYPE);
        }

        return Keys.of(entityType);
    }

    /* Block keys */

    public static Key of(Block block) {
        Material material = block.getType();

        Key baseKey;
        if (material == Materials.SPAWNER.toBukkitType()) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            baseKey = getSpawnerKeyFromCreatureSpawner(creatureSpawner);
        } else {
            short data = plugin.getNMSAlgorithms().getBlockDataValue(block);
            baseKey = MaterialKey.of(material, data, MaterialKeySource.BLOCK);
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return plugin.getKeys().convertKey(baseKey, block.getLocation(wrapper.getHandle()));
        }
    }

    public static Key of(BlockState blockState) {
        Key baseKey;
        if (blockState instanceof CreatureSpawner) {
            baseKey = getSpawnerKeyFromCreatureSpawner((CreatureSpawner) blockState);
        } else {
            short data = plugin.getNMSAlgorithms().getBlockDataValue(blockState);
            baseKey = MaterialKey.of(blockState.getType(), data, MaterialKeySource.BLOCK);
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return plugin.getKeys().convertKey(baseKey, blockState.getLocation(wrapper.getHandle()));
        }
    }

    public static Key of(Key baseKey, Location location) {
        Preconditions.checkArgument(baseKey instanceof MaterialKey);

        return plugin.getKeys().convertKey(baseKey, location);
    }

    /* Item keys */

    public static Key of(ItemStack itemStack) {
        Material material = itemStack.getType();

        Key baseKey;
        if (material == Materials.SPAWNER.toBukkitType()) {
            baseKey = plugin.getProviders().getSpawnerKey(itemStack);
        } else {
            baseKey = MaterialKey.of(material, itemStack.getDurability(), MaterialKeySource.ITEM);
        }

        return plugin.getKeys().convertKey(baseKey, itemStack);
    }

    public static Key of(Material material) {
        return material == Materials.SPAWNER.toBukkitType() ? SpawnerKey.GLOBAL_KEY : MaterialKey.of(material);
    }

    public static Key of(Material material, short data) {
        if (ServerVersion.isLessThan(ServerVersion.v1_21) || material == Materials.SPAWNER.toBukkitType()) {
            return Keys.of(new ItemStack(material, 1, data));
        }

        // In 1.21, we cannot set invalid durability for ItemStacks.
        // Therefore, this code is duplicated from the above method, but
        // creates the baseKey directly, not from ItemStack.
        Key baseKey = MaterialKey.of(material, data, MaterialKeySource.ITEM);

        try {
            // Now we try to convert the key.
            // This may throw an exception that is handled below.
            ItemStack itemStack = new ItemStack(material, 1, data);
            return plugin.getKeys().convertKey(baseKey, itemStack);
        } catch (IllegalArgumentException error) {
            // In 1.21, you cannot create ItemStack out of Material types that are not an item
            // If this occurs, we simply return the base key.
            return baseKey;
        }
    }

    public static Key ofMaterialAndData(String key) {
        String[] sections = KEY_SPLITTER_PATTERN.split(key.toUpperCase(Locale.ENGLISH), 2);

        return Keys.ofMaterialAndData(sections[0], sections.length >= 2 ? sections[1] : null);
    }

    private static Key ofMaterialAndData(String materialName, @Nullable String data) {
        Material material = EnumHelper.getEnum(Material.class, materialName.toUpperCase(Locale.ENGLISH));

        if (material == null) {
            return Keys.of(materialName, data, KeyIndicator.MATERIAL);
        }

        if (Text.isBlank(data)) {
            return Keys.of(material);
        }

        if (material == Materials.SPAWNER.toBukkitType()) {
            return Keys.ofSpawner(data);
        }

        try {
            return Keys.of(material, Short.parseShort(data));
        } catch (NumberFormatException e) {
            return Keys.of(material);
        }
    }

    /* Spawner keys */

    public static Key ofSpawner(EntityType entityType) {
        return SpawnerKey.of(of(entityType));
    }

    public static Key ofSpawner(EntityType entityType, Location location) {
        return plugin.getKeys().convertKey(Keys.ofSpawner(entityType), location);
    }

    public static Key ofSpawner(String key) {
        return SpawnerKey.of(Keys.ofEntityType(key));
    }

    public static Key ofSpawner(String key, Location location) {
        return plugin.getKeys().convertKey(Keys.ofSpawner(key), location);
    }

    /* Custom keys */

    public static Key of(String globalKey, @Nullable String subKey, KeyIndicator keyIndicator) {
        return CustomKey.of(globalKey, subKey, keyIndicator);
    }

    public static Key ofCustom(String key) {
        String[] sections = KEY_SPLITTER_PATTERN.split(key, 2);

        return Keys.of(sections[0], sections.length >= 2 ? sections[1] : null, KeyIndicator.CUSTOM);
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
