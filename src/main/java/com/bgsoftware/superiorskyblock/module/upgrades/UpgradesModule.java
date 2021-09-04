package com.bgsoftware.superiorskyblock.module.upgrades;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoadException;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCostLoader;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddEffect;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddSpawnerRates;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminRankup;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetEffect;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetSpawnerRates;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetUpgrade;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSyncUpgrades;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdRankup;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdUpgrade;
import com.bgsoftware.superiorskyblock.module.upgrades.listeners.UpgradesListener;
import com.bgsoftware.superiorskyblock.upgrade.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UpgradesModule extends BuiltinModule {

    private boolean enabled = false;

    public UpgradesModule(){
        super("upgrades");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    public void onDisable(SuperiorSkyblockPlugin plugin) {
    }

    @Override
    public Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new Listener[] {new UpgradesListener(plugin)};
    }

    @Override
    public SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[] {new CmdRankup(), new CmdUpgrade()};
    }

    @Override
    public SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return !enabled ? null : new SuperiorCommand[] {
                new CmdAdminAddCropGrowth(), new CmdAdminAddEffect(), new CmdAdminAddMobDrops(),
                new CmdAdminAddSpawnerRates(), new CmdAdminRankup(), new CmdAdminSetCropGrowth(),
                new CmdAdminSetEffect(), new CmdAdminSetMobDrops(), new CmdAdminSetSpawnerRates(),
                new CmdAdminSetUpgrade(), new CmdAdminSyncUpgrades()
        };
    }

    @Override
    public boolean isEnabled() {
        return enabled && isInitialized();
    }

    @Override
    protected void updateConfig(SuperiorSkyblockPlugin plugin) {
        enabled = config.getBoolean("enabled");

        if(enabled){
            ConfigurationSection upgrades = config.getConfigurationSection("upgrades");
            for(String upgradeName : upgrades.getKeys(false)){
                SUpgrade upgrade = new SUpgrade(upgradeName);
                for(String _level : upgrades.getConfigurationSection(upgradeName).getKeys(false)){
                    ConfigurationSection levelSection = upgrades.getConfigurationSection(upgradeName + "." + _level);
                    int level = Integer.parseInt(_level);

                    String priceType = levelSection.getString("price-type", "money");
                    UpgradeCostLoader costLoader = plugin.getUpgrades().getUpgradeCostLoader(priceType);

                    if(costLoader == null){
                        SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") has invalid price-type. Skipping...");
                        continue;
                    }

                    UpgradeCost upgradeCost;

                    try{
                        upgradeCost = costLoader.loadCost(levelSection);
                    }catch (UpgradeCostLoadException ex){
                        SuperiorSkyblockPlugin.log("&cUpgrade by name " + upgrade.getName() + " (level " + level + ") failed to initialize because: " + ex.getMessage() + ". Skipping...");
                        continue;
                    }

                    List<String> commands = levelSection.getStringList("commands");
                    String permission = levelSection.getString("permission", "");
                    Set<Pair<String, String>> requirements = new HashSet<>();
                    for(String line : levelSection.getStringList("required-checks")){
                        String[] sections = line.split(";");
                        requirements.add(new Pair<>(sections[0], StringUtils.translateColors(sections[1])));
                    }
                    UpgradeValue<Double> cropGrowth = new UpgradeValue<>(levelSection.getDouble("crop-growth", -1D), true);
                    UpgradeValue<Double> spawnerRates = new UpgradeValue<>(levelSection.getDouble("spawner-rates", -1D), true);
                    UpgradeValue<Double> mobDrops = new UpgradeValue<>(levelSection.getDouble("mob-drops", -1D), true);
                    UpgradeValue<Integer> teamLimit = new UpgradeValue<>(levelSection.getInt("team-limit", -1), true);
                    UpgradeValue<Integer> warpsLimit = new UpgradeValue<>(levelSection.getInt("warps-limit", -1), true);
                    UpgradeValue<Integer> coopLimit = new UpgradeValue<>(levelSection.getInt("coop-limit", -1), true);
                    UpgradeValue<Integer> borderSize = new UpgradeValue<>(levelSection.getInt("border-size", -1), true);
                    UpgradeValue<BigDecimal> bankLimit = new UpgradeValue<>(new BigDecimal(levelSection.getString("bank-limit", "-1")), true);
                    KeyMap<Integer> blockLimits = new KeyMap<>();
                    if(levelSection.contains("block-limits")){
                        for(String block : levelSection.getConfigurationSection("block-limits").getKeys(false))
                            blockLimits.put(block, levelSection.getInt("block-limits." + block));
                    }
                    KeyMap<Integer> entityLimits = new KeyMap<>();
                    if(levelSection.contains("entity-limits")){
                        for(String entity : levelSection.getConfigurationSection("entity-limits").getKeys(false))
                            entityLimits.put(entity.toUpperCase(), levelSection.getInt("entity-limits." + entity));
                    }
                    KeyMap<Integer>[] generatorRates = new KeyMap[World.Environment.values().length];
                    if(levelSection.contains("generator-rates")){
                        for(String blockOrEnv : levelSection.getConfigurationSection("generator-rates").getKeys(false)) {
                            try{
                                int index = World.Environment.valueOf(blockOrEnv.toUpperCase()).ordinal();
                                for(String block : levelSection.getConfigurationSection("generator-rates." + blockOrEnv).getKeys(false)) {
                                    if(generatorRates[index] == null)
                                        generatorRates[index] = new KeyMap<>();
                                    generatorRates[index].put(block, levelSection.getInt("generator-rates." + blockOrEnv + "." + block));
                                }
                            }catch (Exception ex) {
                                if(generatorRates[0] == null)
                                    generatorRates[0] = new KeyMap<>();
                                generatorRates[0].put(blockOrEnv, levelSection.getInt("generator-rates." + blockOrEnv));
                            }
                        }
                    }
                    Map<PotionEffectType, Integer> islandEffects = new HashMap<>();
                    if(levelSection.contains("island-effects")){
                        for(String effect : levelSection.getConfigurationSection("island-effects").getKeys(false)) {
                            PotionEffectType potionEffectType = PotionEffectType.getByName(effect);
                            if(potionEffectType != null)
                                islandEffects.put(potionEffectType, levelSection.getInt("island-effects." + effect) - 1);
                        }
                    }
                    Map<Integer, Integer> rolesLimits = new HashMap<>();
                    if(levelSection.contains("role-limits")){
                        for(String roleId : levelSection.getConfigurationSection("role-limits").getKeys(false)) {
                            try {
                                rolesLimits.put(Integer.parseInt(roleId), levelSection.getInt("role-limits." + roleId));
                            }catch (NumberFormatException ignored){}
                        }
                    }
                    upgrade.addUpgradeLevel(level, new SUpgradeLevel(level, upgradeCost, commands, permission, requirements,
                            cropGrowth, spawnerRates, mobDrops, teamLimit, warpsLimit, coopLimit, borderSize, blockLimits,
                            entityLimits, generatorRates, islandEffects, bankLimit, rolesLimits));
                }

                plugin.getUpgrades().addUpgrade(upgrade);
            }
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onPluginInit(SuperiorSkyblockPlugin plugin) {
        super.onPluginInit(plugin);

        File upgradesFile = new File(plugin.getDataFolder(), "upgrades.yml");

        if(upgradesFile.exists()) {
            CommentedConfiguration config = CommentedConfiguration.loadConfiguration(upgradesFile);
            super.config.set("upgrades", config.get("upgrades"));

            File moduleConfigFile = new File(getDataFolder(), "config.yml");

            try{
                super.config.save(moduleConfigFile);
                config.save(upgradesFile);
            }catch (Exception ex){
                ex.printStackTrace();
            }

            upgradesFile.delete();
        }
    }

    @Override
    protected String[] getIgnoredSections() {
        return new String[] { "upgrades" };
    }

}
