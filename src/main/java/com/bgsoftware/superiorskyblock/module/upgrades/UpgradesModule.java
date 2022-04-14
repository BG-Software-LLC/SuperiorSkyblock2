package com.bgsoftware.superiorskyblock.module.upgrades;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMapImpl;
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
import com.bgsoftware.superiorskyblock.upgrade.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeRequirement;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class UpgradesModule extends BuiltinModule {

    private static final int MAX_UPGRADES_NAME_LENGTH = 255;

    private final List<IUpgradeType> enabledUpgrades = new ArrayList<>();

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
        return !isEnabled() ? null : enabledUpgrades.stream()
                .map(IUpgradeType::getListener)
                .filter(Objects::nonNull)
                .toArray(Listener[]::new);
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
            SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") has invalid price-type. Skipping...");
            return;
        }

        UpgradeCost upgradeCost;

        try {
            upgradeCost = costLoader.loadCost(levelSection);
        } catch (UpgradeCostLoadException ex) {
            SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") failed to initialize because: "
                    + ex.getMessage() + ". Skipping...");
            PluginDebugger.debug(ex);
            return;
        }

        List<String> commands = levelSection.getStringList("commands");
        String permission = levelSection.getString("permission", "");
        Set<UpgradeRequirement> requirements = new HashSet<>();
        for (String line : levelSection.getStringList("required-checks")) {
            String[] sections = line.split(";");
            requirements.add(new UpgradeRequirement(sections[0], StringUtils.translateColors(sections[1])));
        }
        UpgradeValue<Double> cropGrowth = new UpgradeValue<>(levelSection.getDouble("crop-growth", -1D), true);
        UpgradeValue<Double> spawnerRates = new UpgradeValue<>(levelSection.getDouble("spawner-rates", -1D), true);
        UpgradeValue<Double> mobDrops = new UpgradeValue<>(levelSection.getDouble("mob-drops", -1D), true);
        UpgradeValue<Integer> teamLimit = new UpgradeValue<>(levelSection.getInt("team-limit", -1), true);
        UpgradeValue<Integer> warpsLimit = new UpgradeValue<>(levelSection.getInt("warps-limit", -1), true);
        UpgradeValue<Integer> coopLimit = new UpgradeValue<>(levelSection.getInt("coop-limit", -1), true);
        UpgradeValue<Integer> borderSize = new UpgradeValue<>(levelSection.getInt("border-size", -1), true);
        UpgradeValue<BigDecimal> bankLimit = new UpgradeValue<>(new BigDecimal(levelSection.getString("bank-limit", "-1")), true);
        KeyMap<Integer> blockLimits = KeyMapImpl.createHashMap();
        if (levelSection.contains("block-limits")) {
            for (String block : levelSection.getConfigurationSection("block-limits").getKeys(false)) {
                blockLimits.put(KeyImpl.of(block.toUpperCase()), levelSection.getInt("block-limits." + block));
                plugin.getBlockValues().addCustomBlockKey(KeyImpl.of(block));
            }
        }
        KeyMap<Integer> entityLimits = KeyMapImpl.createHashMap();
        if (levelSection.contains("entity-limits")) {
            for (String entity : levelSection.getConfigurationSection("entity-limits").getKeys(false))
                entityLimits.put(KeyImpl.of(entity.toUpperCase()), levelSection.getInt("entity-limits." + entity));
        }
        KeyMap<Integer>[] generatorRates = new KeyMap[World.Environment.values().length];
        if (levelSection.contains("generator-rates")) {
            for (String blockOrEnv : levelSection.getConfigurationSection("generator-rates").getKeys(false)) {
                try {
                    int index = World.Environment.valueOf(blockOrEnv.toUpperCase()).ordinal();
                    for (String block : levelSection.getConfigurationSection("generator-rates." + blockOrEnv).getKeys(false)) {
                        if (generatorRates[index] == null)
                            generatorRates[index] = KeyMapImpl.createHashMap();
                        generatorRates[index].put(KeyImpl.of(block.toUpperCase()), levelSection.getInt("generator-rates." + blockOrEnv + "." + block));
                    }
                } catch (Exception ex) {
                    if (generatorRates[0] == null)
                        generatorRates[0] = KeyMapImpl.createHashMap();
                    generatorRates[0].put(KeyImpl.of(blockOrEnv.toUpperCase()), levelSection.getInt("generator-rates." + blockOrEnv));
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
                } catch (NumberFormatException error) {
                    PluginDebugger.debug(error);
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

            File moduleConfigFile = new File(getDataFolder(), "config.yml");

            try {
                super.config.save(moduleConfigFile);
                config.save(upgradesFile);
            } catch (Exception ex) {
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }

            upgradesFile.delete();
        }
    }

}
