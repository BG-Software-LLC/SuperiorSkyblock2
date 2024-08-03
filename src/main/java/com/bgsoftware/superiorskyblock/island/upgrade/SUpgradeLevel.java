package com.bgsoftware.superiorskyblock.island.upgrade;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2IntMapView;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.value.DoubleValue;
import com.bgsoftware.superiorskyblock.core.value.IntValue;
import com.bgsoftware.superiorskyblock.core.value.Value;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private final DoubleValue cropGrowth;
    private final DoubleValue spawnerRates;
    private final DoubleValue mobDrops;
    private final IntValue teamLimit;
    private final IntValue warpsLimit;
    private final IntValue coopLimit;
    private final IntValue borderSize;
    private final KeyMap<Integer> blockLimits;
    private final KeyMap<Integer> entityLimits;
    private final EnumerateMap<Dimension, Map<Key, Integer>> generatorRates;
    private final Map<PotionEffectType, Integer> islandEffects;
    private final Value<BigDecimal> bankLimit;
    private final Int2IntMapView roleLimits;

    private ItemData itemData;

    public SUpgradeLevel(int level, UpgradeCost cost, List<String> commands, String permission, Set<UpgradeRequirement> requirements,
                         DoubleValue cropGrowth, DoubleValue spawnerRates, DoubleValue mobDrops,
                         IntValue teamLimit, IntValue warpsLimit, IntValue coopLimit,
                         IntValue borderSize, KeyMap<Integer> blockLimits,
                         KeyMap<Integer> entityLimits, EnumerateMap<Dimension, Map<Key, Integer>> generatorRates,
                         Map<PotionEffectType, Integer> islandEffects, Value<BigDecimal> bankLimit,
                         Int2IntMapView roleLimits) {
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
    public int getGeneratorAmount(Key key, Dimension dimension) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        Map<Key, Integer> generatorRates = this.generatorRates.get(dimension);
        return (generatorRates == null ? 0 : generatorRates.getOrDefault(key, 0));
    }

    @Override
    @Deprecated
    public int getGeneratorAmount(Key key, World.Environment environment) {
        return getGeneratorAmount(key, Dimensions.fromEnvironment(environment));
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        Map<Key, Integer> generatorRates = this.generatorRates.get(dimension);
        return generatorRates == null ? Collections.emptyMap() : generatorRates.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue));
    }

    @Override
    @Deprecated
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        return getGeneratorAmounts(Dimensions.fromEnvironment(environment));
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
        if (this.roleLimits.isEmpty())
            return Collections.emptyMap();

        Map<PlayerRole, Integer> roleLimits = new LinkedHashMap<>();

        Iterator<Int2IntMapView.Entry> iterator = this.roleLimits.entryIterator();
        while (iterator.hasNext()) {
            Int2IntMapView.Entry entry = iterator.next();
            PlayerRole playerRole = SPlayerRole.fromId(entry.getKey());
            if (playerRole != null)
                roleLimits.put(playerRole, entry.getValue());
        }

        return roleLimits.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(roleLimits);
    }

    public DoubleValue getCropGrowthUpgradeValue() {
        return cropGrowth;
    }

    public DoubleValue getSpawnerRatesUpgradeValue() {
        return spawnerRates;
    }

    public DoubleValue getMobDropsUpgradeValue() {
        return mobDrops;
    }

    public Map<Key, IntValue> getBlockLimitsUpgradeValue() {
        return blockLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> IntValue.syncedFixed(entry.getValue()))
        );
    }

    public Map<Key, IntValue> getEntityLimitsUpgradeValue() {
        return entityLimits.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> IntValue.syncedFixed(entry.getValue()))
        );
    }

    public IntValue getTeamLimitUpgradeValue() {
        return teamLimit;
    }

    public IntValue getWarpsLimitUpgradeValue() {
        return warpsLimit;
    }

    public IntValue getCoopLimitUpgradeValue() {
        return coopLimit;
    }

    public IntValue getBorderSizeUpgradeValue() {
        return borderSize;
    }

    public EnumerateMap<Dimension, Map<Key, IntValue>> getGeneratorUpgradeValue() {
        EnumerateMap<Dimension, Map<Key, IntValue>> generatorRates = new EnumerateMap<>(Dimension.values());

        for (Dimension dimension : Dimension.values()) {
            Map<Key, Integer> worldGeneratorRates = this.generatorRates.get(dimension);
            if (worldGeneratorRates != null) {
                Map<Key, IntValue> result = worldGeneratorRates.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> IntValue.syncedFixed(entry.getValue())));
                generatorRates.put(dimension, result);
            }
        }

        return generatorRates;
    }

    public Map<PotionEffectType, IntValue> getPotionEffectsUpgradeValue() {
        return islandEffects.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> IntValue.syncedFixed(entry.getValue()))
        );
    }

    public Value<BigDecimal> getBankLimitUpgradeValue() {
        return bankLimit;
    }

    public Map<PlayerRole, IntValue> getRoleLimitsUpgradeValue() {
        if (this.roleLimits.isEmpty())
            return Collections.emptyMap();

        Map<PlayerRole, IntValue> roleLimits = new LinkedHashMap<>();

        Iterator<Int2IntMapView.Entry> iterator = this.roleLimits.entryIterator();
        while (iterator.hasNext()) {
            Int2IntMapView.Entry entry = iterator.next();
            PlayerRole playerRole = SPlayerRole.fromId(entry.getKey());
            if (playerRole != null)
                roleLimits.put(playerRole, IntValue.syncedFixed(entry.getValue()));
        }

        return roleLimits.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(roleLimits);
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
