package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.UpgradesHandler;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuUpgrades extends SuperiorMenu {

    private Island island;

    private MenuUpgrades(SuperiorPlayer superiorPlayer, Island island){
        super("menuUpgrades", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        String upgradeName = plugin.getUpgrades().getUpgrade(e.getRawSlot());

        if(!upgradeName.isEmpty()){
            Bukkit.dispatchCommand(e.getWhoClicked(), "is rankup " + upgradeName);
            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = super.getInventory();

        Map<String, UpgradesHandler.UpgradeData> upgrades = plugin.getUpgrades().getUpgrades();

        for(String upgrade : upgrades.keySet()){
            int level = island.getUpgradeLevel(upgrade);
            double nextLevelPrice = plugin.getUpgrades().getUpgradePrice(upgrade, level);
            UpgradesHandler.UpgradeData upgradeData = upgrades.get(upgrade);
            if(upgradeData.items.containsKey(level)) {
                UpgradesHandler.ItemData itemData = upgradeData.items.get(level);

                inv.setItem(itemData.slot, (EconomyHook.getMoneyInBank(superiorPlayer) >= nextLevelPrice ?
                        itemData.hasNextLevel : itemData.noNextLevel).clone().build(superiorPlayer));
            }
        }

        return inv;
    }

    public static void init(){
        MenuUpgrades menuUpgrades = new MenuUpgrades(null, null);

        File file = new File(plugin.getDataFolder(), "menus/upgrades.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/upgrades.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuUpgrades, "upgrades.yml", cfg);

        if(cfg.contains("upgrades")){
            ConfigurationSection upgradesSection = cfg.getConfigurationSection("upgrades");
            for(String upgradeName : upgradesSection.getKeys(false)){
                if(!plugin.getUpgrades().isUpgrade(upgradeName))
                    continue;

                UpgradesHandler.UpgradeData upgradeData = plugin.getUpgrades().getUpgrades().get(upgradeName);
                ConfigurationSection upgradeSection = upgradesSection.getConfigurationSection(upgradeName);

                for(String level : upgradeSection.getKeys(false)) {
                    if(NumberUtils.isNumber(level)) {
                        int slot = charSlots.getOrDefault(upgradeSection.getString("item", "@").charAt(0), Collections.singletonList(-1)).get(0);
                        upgradeData.items.put(Integer.parseInt(level), new UpgradesHandler.ItemData(
                                FileUtils.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".has-next-level")),
                                FileUtils.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".no-next-level")),
                                slot,
                                FileUtils.getSound(upgradeSection.getConfigurationSection(level + ".has-next-level.sound")),
                                FileUtils.getSound(upgradeSection.getConfigurationSection(level + ".no-next-level.sound")),
                                upgradeSection.getStringList(level + ".has-next-level.commands"),
                                upgradeSection.getStringList(level + ".no-next-level.commands"))
                        );
                    }
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuUpgrades(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuUpgrades.class);
    }

}
