package com.bgsoftware.superiorskyblock.utils.key;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class Key implements com.bgsoftware.superiorskyblock.api.key.Key {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String globalKey;
    private final String subKey;
    private boolean apiKey = false;

    private Key(String key){
        String[] keySections = key.replace(";", ":").split(":");
        this.globalKey = keySections[0];
        this.subKey = keySections.length == 2 ? keySections[1] : "";
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
    public int compareTo(@NotNull com.bgsoftware.superiorskyblock.api.key.Key o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return subKey.isEmpty() ? globalKey : globalKey + ":" + subKey;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object obj) {
        return obj instanceof Key && toString().equals(obj.toString());
    }

    public static Key of(EntityType entityType){
        return of(entityType.name());
    }

    public static Key of(Entity entity){
        return of(EntityUtils.getLimitEntityType(entity), entity);
    }

    public static Key of(Block block){
        return of(block.getState());
    }

    public static Key of(BlockState blockState){
        if(blockState instanceof CreatureSpawner){
            CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
            return of(Materials.SPAWNER.toBukkitType() + ":" + creatureSpawner.getSpawnedType(), blockState.getLocation());
        }

        //noinspection deprecation
        return of(of(blockState.getType(), blockState.getRawData()).toString(), blockState.getLocation());
    }

    public static Key of(ItemStack itemStack){
        return of(Materials.SPAWNER.toBukkitType() == itemStack.getType() ? plugin.getProviders().getSpawnerKey(itemStack) :
                of(itemStack.getType(), itemStack.getDurability()), itemStack);
    }

    public static Key of(Material material, short data){
        return of(of(material + ":" + data), new ItemStack(material, 1, data));
    }

    public static Key of(String key){
        return new Key(key.replace("LEGACY_", ""));
    }

    public static Key of(Material material, short data, Location location){
        return of(material + ":" + data, location);
    }

    public static Key of(String key, Location location){
        return of(Key.of(key), location);
    }

    public static Key of(Key key, Location location){
        return plugin.getBlockValues().convertKey(key, location);
    }

    public static Key of(Key key, ItemStack itemStack){
        return plugin.getBlockValues().convertKey(key, itemStack);
    }

    public static Key of(Key key, Entity entity){
        return plugin.getBlockValues().convertKey(key, entity);
    }

}
