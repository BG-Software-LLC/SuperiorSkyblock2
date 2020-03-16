package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class UpgradesHandler implements UpgradesManager {

    private SuperiorSkyblockPlugin plugin;
    private Registry<String, SUpgrade> upgrades = Registry.createRegistry();

    public UpgradesHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadUpgrades();
    }

    @Override
    public SUpgrade getUpgrade(String upgradeName){
        return upgrades.get(upgradeName);
    }

    @Override
    public SUpgrade getUpgrade(int slot){
        return upgrades.values().stream().filter(upgrade -> upgrade.getMenuSlot() == slot).findFirst().orElse(null);
    }

    @Override
    public boolean isUpgrade(String upgradeName){
        return upgrades.containsKey(upgradeName.toLowerCase());
    }

    @Override
    public Collection<Upgrade> getUpgrades() {
        return Collections.unmodifiableCollection(upgrades.values());
    }

    private void loadUpgrades(){
        File file = new File(plugin.getDataFolder(), "upgrades.yml");

        if(!file.exists())
            plugin.saveResource("upgrades.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection upgrades = cfg.getConfigurationSection("upgrades");

        for(String upgradeName : upgrades.getKeys(false)){
            SUpgrade upgrade = new SUpgrade(upgradeName);
            for(String _level : upgrades.getConfigurationSection(upgradeName).getKeys(false)){
                ConfigurationSection levelSection = upgrades.getConfigurationSection(upgradeName + "." + _level);
                int level = Integer.parseInt(_level);
                double price = levelSection.getDouble("price");
                List<String> commands = levelSection.getStringList("commands");
                String permission = levelSection.getString("permission", "");
                double cropGrowth = levelSection.getDouble("crop-growth", -1D);
                double spawnerRates = levelSection.getDouble("spawner-rates", -1D);
                double mobDrops = levelSection.getDouble("mob-drops", -1D);
                int teamLimit = levelSection.getInt("team-limit", -1);
                int warpsLimit = levelSection.getInt("warps-limit", -1);
                int borderSize = levelSection.getInt("border-size", -1);
                KeyMap<Integer> blockLimits = new KeyMap<>();
                if(levelSection.contains("block-limits")){
                    for(String block : levelSection.getConfigurationSection("block-limits").getKeys(false))
                        blockLimits.put(block, levelSection.getInt("block-limits." + block));
                }
                KeyMap<Integer> generatorRates = new KeyMap<>();
                if(levelSection.contains("generator-rates")){
                    for(String block : levelSection.getConfigurationSection("generator-rates").getKeys(false))
                        generatorRates.put(block, levelSection.getInt("generator-rates." + block));
                }
                upgrade.addUpgradeLevel(level, new SUpgradeLevel(level, price, commands, permission, cropGrowth,
                        spawnerRates, mobDrops, teamLimit, warpsLimit, borderSize, blockLimits, generatorRates));
            }
            this.upgrades.add(upgradeName, upgrade);
        }
    }

}
