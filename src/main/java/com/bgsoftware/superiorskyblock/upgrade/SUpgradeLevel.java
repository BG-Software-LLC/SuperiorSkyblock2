package com.bgsoftware.superiorskyblock.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SUpgradeLevel implements UpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final int level;
    private final UpgradeCost cost;
    private final List<String> commands;
    private final String permission;
    private final Set<Pair<String, String>> requirements;
    private final UpgradeValue<Double> cropGrowth, spawnerRates, mobDrops;
    private final UpgradeValue<Integer> teamLimit, warpsLimit, coopLimit, borderSize;
    private final KeyMap<Integer> blockLimits, entityLimits;
    private final KeyMap<Integer>[] generatorRates;
    private final Map<PotionEffectType, Integer> islandEffects;
    private final UpgradeValue<BigDecimal> bankLimit;
    private final Map<Integer, Integer> roleLimits;

    private ItemData itemData;

    public SUpgradeLevel(int level, UpgradeCost cost, List<String> commands, String permission, Set<Pair<String, String>> requirements,
                         UpgradeValue<Double> cropGrowth, UpgradeValue<Double> spawnerRates, UpgradeValue<Double> mobDrops,
                         UpgradeValue<Integer> teamLimit, UpgradeValue<Integer> warpsLimit, UpgradeValue<Integer> coopLimit,
                         UpgradeValue<Integer> borderSize, KeyMap<Integer> blockLimits,
                         KeyMap<Integer> entityLimits, KeyMap<Integer>[] generatorRates,
                         Map<PotionEffectType, Integer> islandEffects, UpgradeValue<BigDecimal> bankLimit,
                         Map<Integer, Integer> roleLimits){
        this.level = level;
        this.cost = cost;
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
        this.bankLimit = bankLimit;
        this.roleLimits = roleLimits;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public double getPrice() {
        return cost.getCost().doubleValue();
    }

    public UpgradeCost getCost() {
        return cost;
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
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        for (Pair<String, String> requirement : requirements) {
            String check = PlaceholderHook.parse(superiorPlayer, requirement.getKey());
            try {
                if (!Boolean.parseBoolean(plugin.getScriptEngine().eval(check) + ""))
                    return requirement.getValue();
            } catch (ScriptException ignored) {}
        }

        return "";
    }

    @Override
    public double getCropGrowth() {
        return cropGrowth.get();
    }

    public UpgradeValue<Double> getCropGrowthUpgradeValue(){
        return cropGrowth;
    }

    @Override
    public double getSpawnerRates() {
        return spawnerRates.get();
    }

    public UpgradeValue<Double> getSpawnerRatesUpgradeValue(){
        return spawnerRates;
    }

    @Override
    public double getMobDrops() {
        return mobDrops.get();
    }

    public UpgradeValue<Double> getMobDropsUpgradeValue(){
        return mobDrops;
    }

    @Override
    public int getBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getOrDefault(key, -1);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getRaw(key, -1);
    }

    @Override
    public Map<Key, Integer> getBlockLimits() {
        return Collections.unmodifiableMap(blockLimits);
    }

    public Map<Key, UpgradeValue<Integer>> getBlockLimitsUpgradeValue(){
        return blockLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new UpgradeValue<>(entry.getValue(), true))
        );
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return getEntityLimit(Key.of(entityType));
    }

    @Override
    public int getEntityLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return entityLimits.getOrDefault(key, -1);
    }

    @Override
    public Map<Key, Integer> getEntityLimitsAsKeys() {
        return Collections.unmodifiableMap(entityLimits);
    }

    public Map<Key, UpgradeValue<Integer>> getEntityLimitsUpgradeValue(){
        return entityLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new UpgradeValue<>(entry.getValue(), true))
        );
    }

    @Override
    public int getTeamLimit() {
        return teamLimit.get();
    }

    public UpgradeValue<Integer> getTeamLimitUpgradeValue(){
        return teamLimit;
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit.get();
    }

    public UpgradeValue<Integer> getWarpsLimitUpgradeValue(){
        return warpsLimit;
    }

    @Override
    public int getCoopLimit() {
        return coopLimit.get();
    }

    public UpgradeValue<Integer> getCoopLimitUpgradeValue(){
        return coopLimit;
    }

    @Override
    public int getBorderSize() {
        return borderSize.get();
    }

    public UpgradeValue<Integer> getBorderSizeUpgradeValue(){
        return borderSize;
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        KeyMap<Integer> generatorRates = this.generatorRates[environment.ordinal()];
        return (generatorRates == null ? 0 : generatorRates.getOrDefault(key, 0));
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        KeyMap<Integer> generatorRates = this.generatorRates[environment.ordinal()];
        return generatorRates == null ? new HashMap<>() : generatorRates.asKeyMap().entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue));
    }

    public Map<Key, UpgradeValue<Integer>>[] getGeneratorUpgradeValue(){
        Map<Key, UpgradeValue<Integer>>[] generatorRates = new Map[this.generatorRates.length];

        for(int i = 0; i < generatorRates.length; ++i) {
            if(this.generatorRates[i] != null) {
                generatorRates[i] = this.generatorRates[i].entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new UpgradeValue<>(entry.getValue(), true))
                );
            }
        }

        return generatorRates;
    }

    @Override
    public int getPotionEffect(PotionEffectType potionEffectType) {
        Preconditions.checkNotNull(potionEffectType, "potionEffectType parameter cannot be null.");
        return islandEffects.getOrDefault(potionEffectType, 0);
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        return Collections.unmodifiableMap(islandEffects);
    }

    public Map<PotionEffectType, UpgradeValue<Integer>> getPotionEffectsUpgradeValue(){
        return islandEffects.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new UpgradeValue<>(entry.getValue(), true))
        );
    }

    @Override
    public BigDecimal getBankLimit() {
        return bankLimit.get();
    }

    public UpgradeValue<BigDecimal> getBankLimitUpgradeValue(){
        return bankLimit;
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        return roleLimits.getOrDefault(playerRole.getId(), 0);
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return roleLimits.entrySet().stream()
                .filter(entry -> SPlayerRole.fromId(entry.getKey()) != null)
                .collect(Collectors.toMap(
                        entry -> SPlayerRole.fromId(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    public Map<PlayerRole, UpgradeValue<Integer>> getRoleLimitsUpgradeValue(){
        return roleLimits.entrySet().stream()
                .filter(entry -> SPlayerRole.fromId(entry.getKey()) != null)
                .collect(Collectors.toMap(
                        entry -> SPlayerRole.fromId(entry.getKey()),
                        entry -> new UpgradeValue<>(entry.getValue(), true)
                ));
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
