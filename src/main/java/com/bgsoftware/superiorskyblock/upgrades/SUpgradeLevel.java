package com.bgsoftware.superiorskyblock.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SUpgradeLevel implements UpgradeLevel {

    private final static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    private final int level;
    private final double price;
    private final List<String> commands;
    private final String permission;
    private final Set<Pair<String, String>> requirements;
    private final double cropGrowth, spawnerRates, mobDrops;
    private final int teamLimit, warpsLimit, coopLimit, borderSize;
    private final KeyMap<Integer> blockLimits, generatorRates;
    private final Map<EntityType, Integer> entityLimits;
    private final Map<PotionEffectType, Integer> islandEffects;

    private ItemData itemData;

    public SUpgradeLevel(int level, double price, List<String> commands, String permission, Set<Pair<String, String>> requirements, double cropGrowth, double spawnerRates, double mobDrops, int teamLimit, int warpsLimit, int coopLimit, int borderSize, KeyMap<Integer> blockLimits, Map<EntityType, Integer> entityLimits, KeyMap<Integer> generatorRates, Map<PotionEffectType, Integer> islandEffects){
        this.level = level;
        this.price = price;
        this.commands = commands;
        this.permission = permission;
        this.requirements = requirements;
        this.cropGrowth = cropGrowth;
        this.spawnerRates = spawnerRates;
        this.mobDrops = mobDrops;
        this.teamLimit = teamLimit;
        this.warpsLimit = warpsLimit;
        this.coopLimit = coopLimit;
        this.borderSize = borderSize;
        this.blockLimits = blockLimits;
        this.entityLimits = entityLimits;
        this.generatorRates = generatorRates;
        this.islandEffects = islandEffects;
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
    public String checkRequirements(SuperiorPlayer superiorPlayer) {
        for(Pair<String, String> requirement : requirements){
            String check = PlaceholderHook.parse(superiorPlayer, requirement.getKey());
            try {
                if (!Boolean.parseBoolean(engine.eval(check) + ""))
                    return requirement.getValue();
            }catch (Exception ignored){}
        }

        return "";
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
        return blockLimits.getOrDefault(key, SIsland.NO_LIMIT);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return blockLimits.getRaw(key, SIsland.NO_LIMIT);
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return blockLimits;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        return entityLimits.getOrDefault(entityType, SIsland.NO_LIMIT);
    }

    @Override
    public Map<EntityType, Integer> getEntityLimits() {
        return entityLimits;
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
    public int getCoopLimit() {
        return coopLimit;
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
        return this.generatorRates.asKeyMap().entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue));
    }

    @Override
    public int getPotionEffect(PotionEffectType potionEffectType) {
        return islandEffects.getOrDefault(potionEffectType, 0);
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
