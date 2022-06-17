package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.core.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Pattern;

public class KeyImpl implements Key {

    private static final Pattern LEGACY_PATTERN = Pattern.compile("LEGACY_");
    private static final Pattern KEY_SPLITTER_PATTERN = Pattern.compile("[:;]");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String globalKey;
    private final String subKey;
    private boolean apiKey = false;

    private KeyImpl(String globalKey, String subKey) {
        this.globalKey = globalKey.toUpperCase(Locale.ENGLISH);
        this.subKey = subKey.toUpperCase(Locale.ENGLISH);
    }

    public static Key of(EntityType entityType) {
        return of(entityType.name());
    }

    public static Key of(Entity entity) {
        return of(BukkitEntities.getLimitEntityType(entity), entity);
    }

    public static Key of(Block block) {
        return of(block.getState());
    }

    public static Key of(BlockState blockState) {
        if (blockState instanceof CreatureSpawner) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
            return of(Materials.SPAWNER.toBukkitType() + "", creatureSpawner.getSpawnedType() + "", blockState.getLocation());
        }

        //noinspection deprecation
        Key rawBlockKey = of(blockState.getType(), blockState.getRawData());


        return of(rawBlockKey.getGlobalKey(), rawBlockKey.getSubKey(), blockState.getLocation());
    }

    public static Key of(ItemStack itemStack) {
        return of(Materials.SPAWNER.toBukkitType() == itemStack.getType() ? plugin.getProviders().getSpawnerKey(itemStack) :
                of(itemStack.getType(), itemStack.getDurability()), itemStack);
    }

    public static Key of(Material material, short data) {
        return of(of(material + "", data + ""), new ItemStack(material, 1, data));
    }

    public static Key of(String globalKey, String subKey) {
        return new KeyImpl(LEGACY_PATTERN.matcher(globalKey).replaceAll(""), subKey);
    }

    public static Key of(String key) {
        String[] sections = KEY_SPLITTER_PATTERN.split(key);
        return new KeyImpl(sections[0], sections.length == 2 ? sections[1] : "");
    }

    public static Key of(Material material, short data, Location location) {
        return of(material + "", data + "", location);
    }

    public static Key of(String globalKey, String subKey, Location location) {
        return of(KeyImpl.of(globalKey, subKey), location);
    }

    public static Key of(Key key, Location location) {
        return plugin.getBlockValues().convertKey(key, location);
    }

    public static Key of(Key key, ItemStack itemStack) {
        return plugin.getBlockValues().convertKey(key, itemStack);
    }

    public static Key of(Key key, Entity entity) {
        return plugin.getBlockValues().convertKey(key, entity);
    }

    @Override
    public String getGlobalKey() {
        return globalKey;
    }

    @Override
    public String getSubKey() {
        return subKey;
    }

    public Key markAPIKey() {
        this.apiKey = true;
        return this;
    }

    public boolean isAPIKey() {
        return apiKey;
    }

    @Override
    public int compareTo(@NotNull Key o) {
        return toString().compareTo(o.toString());
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        return obj instanceof Key && toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return subKey.isEmpty() ? globalKey : globalKey + ":" + subKey;
    }

}
