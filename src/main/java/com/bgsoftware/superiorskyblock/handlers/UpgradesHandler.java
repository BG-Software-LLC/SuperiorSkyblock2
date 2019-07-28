package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;

import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UpgradesHandler {

    private SuperiorSkyblockPlugin plugin;
    private Map<String, UpgradeData> upgrades = new HashMap<>();

    public UpgradesHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadUpgrades();
    }

    public Map<String, UpgradeData> getUpgrades() {
        return upgrades;
    }

    public double getUpgradePrice(String upgradeName, int level){
        if(isUpgrade(upgradeName)){
            UpgradeData upgradeData = upgrades.get(upgradeName);

            level = Math.min(level, getMaxUpgradeLevel(upgradeName));

           return upgradeData.prices.get(level);
        }

        return 1;
    }

    public String getUpgrade(int slot){
        for(String _upgradeName : upgrades.keySet()) {
            for (ItemData itemData : upgrades.get(_upgradeName).items.values()) {
                if (slot == itemData.slot){
                    return _upgradeName;
                }
            }
        }

        return "";
    }

    public List<String> getUpgradeCommands(String upgradeName, int level){
        if(isUpgrade(upgradeName)){
            return upgrades.get(upgradeName).commands.getOrDefault(level, new ArrayList<>());
        }

        return new ArrayList<>();
    }

    public int getMaxUpgradeLevel(String upgradeName){
        if(!isUpgrade(upgradeName))
            return -1;

        UpgradeData upgradeData = upgrades.get(upgradeName);
        int maxLevel = 0;

        while (upgradeData.prices.containsKey(maxLevel + 1) && upgradeData.commands.containsKey(maxLevel + 1))
            maxLevel++;

        return maxLevel;
    }

    public boolean isUpgrade(String upgradeName){
        return upgrades.containsKey(upgradeName.toLowerCase());
    }

    public List<String> getAllUpgrades(){
        return new ArrayList<>(upgrades.keySet());
    }

    public SoundWrapper getClickSound(String upgradeName, int level, boolean hasNextLevel){
        UpgradeData upgradeData = upgrades.get(upgradeName);
        SoundWrapper sound = null;

        if(upgradeData.items.containsKey(level)){
            ItemData itemData = upgradeData.items.get(level);
            sound = hasNextLevel ? itemData.hasNextLevelSound : itemData.noNextLevelSound;
        }

        return sound;
    }

    private void loadUpgrades(){
        File file = new File(plugin.getDataFolder(), "upgrades.yml");

        if(!file.exists())
            plugin.saveResource("upgrades.yml", false);

        CommentedConfiguration cfg = new CommentedConfiguration(null, file);

        ConfigurationSection upgrades = cfg.getConfigurationSection("upgrades");

        for(String upgradeName : upgrades.getKeys(false)){
            UpgradeData upgradeData = new UpgradeData();
            for(String _level : upgrades.getConfigurationSection(upgradeName).getKeys(false)){
                int level = Integer.valueOf(_level);
                upgradeData.prices.put(level, upgrades.getDouble(upgradeName + "." + level + ".price"));
                upgradeData.commands.put(level, upgrades.getStringList(upgradeName + "." + level + ".commands"));
            }
            this.upgrades.put(upgradeName, upgradeData);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public class UpgradeData{

        public Map<Integer, Double> prices = new HashMap<>();
        public Map<Integer, List<String>> commands = new HashMap<>();
        public Map<Integer, ItemData> items = new HashMap<>();

    }

    @SuppressWarnings("WeakerAccess")
    public static class ItemData{

        public ItemStack hasNextLevel, noNextLevel;
        public int slot;
        public SoundWrapper hasNextLevelSound, noNextLevelSound;
        public List<String> hasNextLevelCommands, noNextLevelCommands;

        public ItemData(ItemStack hasNextLevel, ItemStack noNextLevel, int slot, SoundWrapper hasNextLevelSound, SoundWrapper noNextLevelSound, List<String> hasNextLevelCommands, List<String> noNextLevelCommands){
            this.hasNextLevel = hasNextLevel;
            this.noNextLevel = noNextLevel;
            this.slot = slot;
            this.hasNextLevelSound = hasNextLevelSound;
            this.noNextLevelSound = noNextLevelSound;
            this.hasNextLevelCommands = hasNextLevelCommands;
            this.noNextLevelCommands = noNextLevelCommands;
        }

    }

}
