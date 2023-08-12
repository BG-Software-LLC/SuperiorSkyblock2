package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SUpgradeLevel implements UpgradeLevel {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private final int level;
    private final UpgradeCost cost;
    private final List<String> commands;
    private final String permission;
    private final Set<UpgradeRequirement> requirements;
    private final Value<Double> cropGrowth;
    private final Value<Double> spawnerRates;
    private final Value<Double> mobDrops;
    private final Value<Integer> teamLimit;
    private final Value<Integer> warpsLimit;
    private final Value<Integer> coopLimit;
    private final Value<Integer> borderSize;
    private final KeyMap<Integer> blockLimits;
    private final KeyMap<Integer> entityLimits;
    private final Map<World.Environment, Map<Key, Integer>> generatorRates;
    private final Map<PotionEffectType, Integer> islandEffects;
    private final Value<BigDecimal> bankLimit;
    private final Map<Integer, Integer> roleLimits;

    private ItemData itemData;

    public SUpgradeLevel(int level, UpgradeCost cost, List<String> commands, String permission, Set<UpgradeRequirement> requirements,
                         Value<Double> cropGrowth, Value<Double> spawnerRates, Value<Double> mobDrops,
                         Value<Integer> teamLimit, Value<Integer> warpsLimit, Value<Integer> coopLimit,
                         Value<Integer> borderSize, KeyMap<Integer> blockLimits,
                         KeyMap<Integer> entityLimits, Map<World.Environment, Map<Key, Integer>> generatorRates,
                         Map<PotionEffectType, Integer> islandEffects, Value<BigDecimal> bankLimit,
                         Map<Integer, Integer> roleLimits) {
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

        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();

        if (offlinePlayer != null) {
            for (UpgradeRequirement requirement : requirements) {
                String check = placeholdersService.get().parsePlaceholders(offlinePlayer, requirement.getPlaceholder());
                try {
                    if (!Boolean.parseBoolean(plugin.getScriptEngine().eval(check) + ""))
                        return requirement.getErrorMessage();
                } catch (ScriptException error) {
                    Log.entering("ENTER", level, superiorPlayer.getName(), requirement.getPlaceholder());
                    Log.error(error, "An unexpected error occurred while checking for upgrade requirement:");
                }
            }
        }

        return "";
    }

    @Override
    public double getCropGrowth() {
        return cropGrowth.get();
    }

    @Override
    public double getSpawnerRates() {
        return spawnerRates.get();
    }

    @Override
    public double getMobDrops() {
        return mobDrops.get();
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

    @Override
    public int getEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return getEntityLimit(Keys.of(entityType));
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

    @Override
    public int getTeamLimit() {
        return teamLimit.get();
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit.get();
    }

    @Override
    public int getCoopLimit() {
        return coopLimit.get();
    }

    @Override
    public int getBorderSize() {
        return borderSize.get();
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        Map<Key, Integer> generatorRates = this.generatorRates.get(environment);
        return (generatorRates == null ? 0 : generatorRates.getOrDefault(key, 0));
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        Map<Key, Integer> generatorRates = this.generatorRates.get(environment);
        return generatorRates == null ? Collections.emptyMap() : generatorRates.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue));
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

    @Override
    public BigDecimal getBankLimit() {
        return bankLimit.get();
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

    public Value<Double> getCropGrowthUpgradeValue() {
        return cropGrowth;
    }

    public Value<Double> getSpawnerRatesUpgradeValue() {
        return spawnerRates;
    }

    public Value<Double> getMobDropsUpgradeValue() {
        return mobDrops;
    }

    public Map<Key, Value<Integer>> getBlockLimitsUpgradeValue() {
        return blockLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Value.syncedFixed(entry.getValue()))
        );
    }

    public Map<Key, Value<Integer>> getEntityLimitsUpgradeValue() {
        return entityLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Value.syncedFixed(entry.getValue()))
        );
    }

    public Value<Integer> getTeamLimitUpgradeValue() {
        return teamLimit;
    }

    public Value<Integer> getWarpsLimitUpgradeValue() {
        return warpsLimit;
    }

    public Value<Integer> getCoopLimitUpgradeValue() {
        return coopLimit;
    }

    public Value<Integer> getBorderSizeUpgradeValue() {
        return borderSize;
    }

    public Map<World.Environment, Map<Key, Value<Integer>>> getGeneratorUpgradeValue() {
        EnumMap<World.Environment, Map<Key, Value<Integer>>> generatorRates = new EnumMap<>(World.Environment.class);

        this.generatorRates.forEach(((environment, worldGeneratorRates) -> {
            Map<Key, Value<Integer>> result = worldGeneratorRates.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> Value.syncedFixed(entry.getValue())));
            generatorRates.put(environment, result);
        }));

        return Collections.unmodifiableMap(generatorRates);
    }

    public Map<PotionEffectType, Value<Integer>> getPotionEffectsUpgradeValue() {
        return islandEffects.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> Value.syncedFixed(entry.getValue()))
        );
    }

    public Value<BigDecimal> getBankLimitUpgradeValue() {
        return bankLimit;
    }

    public Map<PlayerRole, Value<Integer>> getRoleLimitsUpgradeValue() {
        return roleLimits.entrySet().stream()
                .filter(entry -> SPlayerRole.fromId(entry.getKey()) != null)
                .collect(Collectors.toMap(
                        entry -> SPlayerRole.fromId(entry.getKey()),
                        entry -> Value.syncedFixed(entry.getValue()))
                );
    }

    public void setItemData(TemplateItem hasNextLevel, TemplateItem noNextLevel,
                            GameSound hasNextLevelSound, GameSound noNextLevelSound,
                            List<String> hasNextLevelCommands, List<String> noNextLevelCommands) {
        this.itemData = new ItemData(hasNextLevel, noNextLevel, hasNextLevelSound, noNextLevelSound, hasNextLevelCommands, noNextLevelCommands);
    }

    public ItemData getItemData() {
        return itemData;
    }

    public static class ItemData {

        public TemplateItem hasNextLevel;
        public TemplateItem noNextLevel;
        public GameSound hasNextLevelSound;
        public GameSound noNextLevelSound;
        public List<String> hasNextLevelCommands;
        public List<String> noNextLevelCommands;

        public ItemData(TemplateItem hasNextLevel, TemplateItem noNextLevel,
                        GameSound hasNextLevelSound, GameSound noNextLevelSound,
                        List<String> hasNextLevelCommands, List<String> noNextLevelCommands) {
            this.hasNextLevel = hasNextLevel;
            this.noNextLevel = noNextLevel;
            this.hasNextLevelSound = hasNextLevelSound;
            this.noNextLevelSound = noNextLevelSound;
            this.hasNextLevelCommands = hasNextLevelCommands;
            this.noNextLevelCommands = noNextLevelCommands;
        }

    }

}
