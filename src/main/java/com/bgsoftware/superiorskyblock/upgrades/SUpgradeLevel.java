package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SUpgradeLevel implements UpgradeLevel {

    private final int level;
    private final double price;
    private final List<String> commands;
    private final String permission;
    private final double cropGrowth, spawnerRates, mobDrops;
    private final int teamLimit, warpsLimit, borderSize;
    private final KeyMap<Integer> blockLimits, generatorRates;

    private ItemData itemData;

    public SUpgradeLevel(int level, double price, List<String> commands, String permission, double cropGrowth, double spawnerRates, double mobDrops, int teamLimit, int warpsLimit, int borderSize, KeyMap<Integer> blockLimits, KeyMap<Integer> generatorRates){
        this.level = level;
        this.price = price;
        this.commands = commands;
        this.permission = permission;
        this.cropGrowth = cropGrowth;
        this.spawnerRates = spawnerRates;
        this.mobDrops = mobDrops;
        this.teamLimit = teamLimit;
        this.warpsLimit = warpsLimit;
        this.borderSize = borderSize;
        this.blockLimits = blockLimits;
        this.generatorRates = generatorRates;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public List<String> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public double getCropGrowth() {
        return cropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return spawnerRates;
    }

    @Override
    public double getMobDrops() {
        return mobDrops;
    }

    @Override
    public int getBlockLimit(Key key) {
        return blockLimits.getOrDefault(key, SIsland.NO_BLOCK_LIMIT);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return blockLimits.getRaw(key, SIsland.NO_BLOCK_LIMIT);
    }

    @Override
    public int getTeamLimit() {
        return teamLimit;
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit;
    }

    @Override
    public int getBorderSize() {
        return borderSize;
    }

    @Override
    public int getGeneratorAmount(Key key) {
        return generatorRates.getOrDefault(key, 0);
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts() {
        Map<String, Integer> generatorRates = new HashMap<>();
        this.generatorRates.forEach((key, value) -> generatorRates.put(key.toString(), value));
        return generatorRates;
    }

    public void setItemData(ItemBuilder hasNextLevel, ItemBuilder noNextLevel, SoundWrapper hasNextLevelSound, SoundWrapper noNextLevelSound, List<String> hasNextLevelCommands, List<String> noNextLevelCommands){
        this.itemData = new ItemData(hasNextLevel, noNextLevel, hasNextLevelSound, noNextLevelSound, hasNextLevelCommands, noNextLevelCommands);
    }

    public ItemData getItemData() {
        return itemData;
    }

    public static class ItemData{

        public ItemBuilder hasNextLevel, noNextLevel;
        public SoundWrapper hasNextLevelSound, noNextLevelSound;
        public List<String> hasNextLevelCommands, noNextLevelCommands;

        public ItemData(ItemBuilder hasNextLevel, ItemBuilder noNextLevel, SoundWrapper hasNextLevelSound, SoundWrapper noNextLevelSound, List<String> hasNextLevelCommands, List<String> noNextLevelCommands){
            this.hasNextLevel = hasNextLevel;
            this.noNextLevel = noNextLevel;
            this.hasNextLevelSound = hasNextLevelSound;
            this.noNextLevelSound = noNextLevelSound;
            this.hasNextLevelCommands = hasNextLevelCommands;
            this.noNextLevelCommands = noNextLevelCommands;
        }

    }

}
