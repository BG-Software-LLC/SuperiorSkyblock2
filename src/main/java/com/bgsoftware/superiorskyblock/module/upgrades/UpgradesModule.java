package com.bgsoftware.superiorskyblock.module.upgrades;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgrade;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.island.upgrade.UpgradeRequirement;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminRankup;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetUpgrade;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSyncUpgrades;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdRankup;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdUpgrade;
import com.bgsoftware.superiorskyblock.module.upgrades.type.IUpgradeType;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeBlockLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeIslandEffects;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeSpawnerRates;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UpgradesModule extends BuiltinModule {

    private static final int MAX_UPGRADES_NAME_LENGTH = 255;

    private final List<IUpgradeType> enabledUpgrades = new LinkedList<>();

    public UpgradesModule() {
        super("upgrades");
    }

    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);
        convertOldUpgradesFile(plugin);
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public void loadData(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        if (!isEnabled())
            return null;

        List<Listener> listenersList = new ArrayList<>();

        for (IUpgradeType upgradeType : enabledUpgrades) {
            listenersList.addAll(upgradeType.getListeners());
        }

        return listenersList.toArray(new Listener[0]);
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return !isEnabled() ? null : new SuperiorCommand[]{new CmdRankup(), new CmdUpgrade()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        if (!isEnabled())
            return null;

        List<SuperiorCommand> adminCommands = enabledUpgrades.stream()
                .map(IUpgradeType::getCommands)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        adminCommands.add(new CmdAdminRankup());
        adminCommands.add(new CmdAdminSetUpgrade());
        adminCommands.add(new CmdAdminSyncUpgrades());

        return adminCommands.toArray(new SuperiorCommand[0]);
    }

    @Override
    public boolean isEnabled() {
        return !enabledUpgrades.isEmpty() && isInitialized();
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[]{"upgrades"};
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabledUpgrades.clear();

        if (config.getBoolean("crop-growth", true))
            enabledUpgrades.add(new UpgradeTypeCropGrowth(plugin));
        if (config.getBoolean("mob-drops", true))
            enabledUpgrades.add(new UpgradeTypeMobDrops(plugin));
        if (config.getBoolean("island-effects", true))
            enabledUpgrades.add(new UpgradeTypeIslandEffects(plugin));
        if (config.getBoolean("spawner-rates", true))
            enabledUpgrades.add(new UpgradeTypeSpawnerRates(plugin));
        if (config.getBoolean("block-limits", true))
            enabledUpgrades.add(new UpgradeTypeBlockLimits(plugin));
        if (config.getBoolean("entity-limits", true))
            enabledUpgrades.add(new UpgradeTypeEntityLimits(plugin));

        if (enabledUpgrades.isEmpty())
            return;

        ConfigurationSection upgrades = config.getConfigurationSection("upgrades");

        if (upgrades == null)
            return;

        for (String upgradeName : upgrades.getKeys(false)) {
            if (upgradeName.length() > MAX_UPGRADES_NAME_LENGTH)
                upgradeName = upgradeName.substring(0, MAX_UPGRADES_NAME_LENGTH);

            SUpgrade upgrade = new SUpgrade(upgradeName);
            for (String _level : upgrades.getConfigurationSection(upgradeName).getKeys(false)) {
                loadUpgradeLevelFromSection(plugin, upgrade, _level, upgrades.getConfigurationSection(upgradeName + "." + _level));
            }

            plugin.getUpgrades().addUpgrade(upgrade);
        }
    }

    @Nullable
    public <T extends IUpgradeType> T getEnabledUpgradeType(Class<T> clazz) {
        return enabledUpgrades.stream()
                .filter(upgradeType -> upgradeType.getClass().equals(clazz))
                .findFirst().map(clazz::cast).orElse(null);
    }

    public boolean isUpgradeTypeEnabled(Class<? extends IUpgradeType> clazz) {
        return getEnabledUpgradeType(clazz) != null;
    }

    private void loadUpgradeLevelFromSection(SuperiorSkyblockPlugin plugin, SUpgrade upgrade,
                                             String sectionName, ConfigurationSection levelSection) {
        int level = Integer.parseInt(sectionName);

        String priceType = levelSection.getString("price-type", "money");
        UpgradeCostLoader costLoader = plugin.getUpgrades().getUpgradeCostLoader(priceType);

        if (costLoader == null) {
            Log.warn("Upgrade by name ", upgrade.getName(), " (level ", level, ") has invalid price-type. Skipping...");
            return;
        }

        UpgradeCost upgradeCost;

        try {
            upgradeCost = costLoader.loadCost(levelSection);
        } catch (UpgradeCostLoadException error) {
            Log.error(error, "Upgrade by name ", upgrade.getName(), " (level ", level, ") failed to initialize:");
            return;
        }

        List<String> commands = levelSection.getStringList("commands");
        String permission = levelSection.getString("permission", "");
        Set<UpgradeRequirement> requirements = new HashSet<>();
        for (String line : levelSection.getStringList("required-checks")) {
            String[] sections = line.split(";");
            requirements.add(new UpgradeRequirement(sections[0], Formatters.COLOR_FORMATTER.format(sections[1])));
        }

        Value<Double> cropGrowth = Value.syncedFixed(levelSection.getDouble("crop-growth", -1D));
        Value<Double> spawnerRates = Value.syncedFixed(levelSection.getDouble("spawner-rates", -1D));
        Value<Double> mobDrops = Value.syncedFixed(levelSection.getDouble("mob-drops", -1D));
        Value<Integer> teamLimit = Value.syncedFixed(levelSection.getInt("team-limit", -1));
        Value<Integer> warpsLimit = Value.syncedFixed(levelSection.getInt("warps-limit", -1));
        Value<Integer> coopLimit = Value.syncedFixed(levelSection.getInt("coop-limit", -1));
        Value<Integer> borderSize = Value.syncedFixed(levelSection.getInt("border-size", -1));

        if (borderSize.get() > plugin.getSettings().getMaxIslandSize()) {
            Log.warn("Upgrade by name ", upgrade.getName(), " (level ", level, ") has illegal border-size, skipping...");
            return;
        }

        Value<BigDecimal> bankLimit = Value.syncedFixed(new BigDecimal(levelSection.getString("bank-limit", "-1")));
        KeyMap<Integer> blockLimits = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        if (levelSection.contains("block-limits")) {
            for (String block : levelSection.getConfigurationSection("block-limits").getKeys(false)) {
                Key blockKey = Keys.ofMaterialAndData(block);
                blockLimits.put(blockKey, levelSection.getInt("block-limits." + block));
                plugin.getBlockValues().addCustomBlockKey(blockKey);
            }
        }
        KeyMap<Integer> entityLimits = KeyMaps.createIdentityHashMap(KeyIndicator.ENTITY_TYPE);
        if (levelSection.contains("entity-limits")) {
            for (String entity : levelSection.getConfigurationSection("entity-limits").getKeys(false))
                entityLimits.put(Keys.ofEntityType(entity), levelSection.getInt("entity-limits." + entity));
        }
        EnumMap<World.Environment, Map<Key, Integer>> generatorRates = new EnumMap<>(World.Environment.class);
        if (levelSection.contains("generator-rates")) {
            for (String blockOrEnv : levelSection.getConfigurationSection("generator-rates").getKeys(false)) {
                try {
                    World.Environment environment = World.Environment.valueOf(blockOrEnv.toUpperCase(Locale.ENGLISH));
                    for (String block : levelSection.getConfigurationSection("generator-rates." + blockOrEnv).getKeys(false)) {
                        generatorRates.computeIfAbsent(environment, e -> KeyMaps.createHashMap(KeyIndicator.MATERIAL)).put(
                                Keys.ofMaterialAndData(block), levelSection.getInt("generator-rates." + blockOrEnv + "." + block));
                    }
                } catch (Exception ex) {
                    generatorRates.computeIfAbsent(plugin.getSettings().getWorlds().getDefaultWorld(), e -> KeyMaps.createHashMap(KeyIndicator.MATERIAL))
                            .put(Keys.ofMaterialAndData(blockOrEnv), levelSection.getInt("generator-rates." + blockOrEnv));
                }
            }
        }
        Map<PotionEffectType, Integer> islandEffects = new HashMap<>();
        if (levelSection.contains("island-effects")) {
            for (String effect : levelSection.getConfigurationSection("island-effects").getKeys(false)) {
                PotionEffectType potionEffectType = PotionEffectType.getByName(effect);
                if (potionEffectType != null)
                    islandEffects.put(potionEffectType, levelSection.getInt("island-effects." + effect) - 1);
            }
        }
        Map<Integer, Integer> rolesLimits = new HashMap<>();
        if (levelSection.contains("role-limits")) {
            for (String roleId : levelSection.getConfigurationSection("role-limits").getKeys(false)) {
                try {
                    rolesLimits.put(Integer.parseInt(roleId), levelSection.getInt("role-limits." + roleId));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        upgrade.addUpgradeLevel(level, new SUpgradeLevel(level, upgradeCost, commands, permission, requirements,
                cropGrowth, spawnerRates, mobDrops, teamLimit, warpsLimit, coopLimit, borderSize, blockLimits,
                entityLimits, generatorRates, islandEffects, bankLimit, rolesLimits));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void convertOldUpgradesFile(SuperiorSkyblockPlugin plugin) {
        File upgradesFile = new File(plugin.getDataFolder(), "upgrades.yml");

        if (upgradesFile.exists()) {
            CommentedConfiguration config = CommentedConfiguration.loadConfiguration(upgradesFile);

            super.config.set("upgrades", config.get("upgrades"));

            File moduleConfigFile = new File(getModuleFolder(), "config.yml");

            try {
                super.config.save(moduleConfigFile);
                config.save(upgradesFile);
            } catch (Exception error) {
                Log.entering("UpgradesModule", "convertOldUpgradesFile", "ENTER");
                Log.error(error, "An error occurred while saving config file:");
            }

            upgradesFile.delete();
        }
    }

}
