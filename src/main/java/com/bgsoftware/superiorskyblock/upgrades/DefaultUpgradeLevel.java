package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultUpgradeLevel extends SUpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final DefaultUpgradeLevel INSTANCE = new DefaultUpgradeLevel();

    private DefaultUpgradeLevel(){
        super(-1, 0, new ArrayList<>(), "", new HashSet<>(), 0D, 0D, 0D,
                0, 0, 0, 0, new KeyMap<>(), new HashMap<>(), new KeyMap<>(),
                new HashMap<>());
    }

    @Override
    public double getCropGrowth() {
        return plugin.getSettings().defaultCropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return plugin.getSettings().defaultSpawnerRates;
    }

    @Override
    public double getMobDrops() {
        return plugin.getSettings().defaultMobDrops;
    }

    @Override
    public int getBlockLimit(Key key) {
        return plugin.getSettings().defaultBlockLimits.getOrDefault(key, SIsland.NO_LIMIT);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return plugin.getSettings().defaultBlockLimits.getRaw(key, SIsland.NO_LIMIT);
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return plugin.getSettings().defaultBlockLimits;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        return plugin.getSettings().defaultEntityLimits.getOrDefault(entityType, SIsland.NO_LIMIT);
    }

    @Override
    public Map<EntityType, Integer> getEntityLimits() {
        return plugin.getSettings().defaultEntityLimits;
    }

    @Override
    public int getTeamLimit() {
        return plugin.getSettings().defaultTeamLimit;
    }

    @Override
    public int getWarpsLimit() {
        return plugin.getSettings().defaultWarpsLimit;
    }

    @Override
    public int getCoopLimit() {
        return plugin.getSettings().defaultCoopLimit;
    }

    @Override
    public int getBorderSize() {
        return plugin.getSettings().defaultIslandSize;
    }

    @Override
    public int getGeneratorAmount(Key key) {
        return plugin.getSettings().defaultGenerator.getOrDefault(key, 0);
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts() {
        return plugin.getSettings().defaultGenerator.asKeyMap().entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue));
    }

    public static DefaultUpgradeLevel getInstance(){
        return INSTANCE;
    }

}
