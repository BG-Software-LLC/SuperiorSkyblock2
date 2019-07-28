package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.handlers.UpgradesHandler;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Map;

public final class IslandUpgradesMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private Island island;
    private SuperiorPlayer superiorPlayer;

    private IslandUpgradesMenu(Island island){
        super("upgradesPage");
        this.island = island;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        String upgradeName = plugin.getUpgrades().getUpgrade(e.getRawSlot());

        if(!upgradeName.isEmpty()){
            Bukkit.dispatchCommand(e.getWhoClicked(), "is rankup " + upgradeName);
            e.getWhoClicked().closeInventory();
        }
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        this.superiorPlayer = superiorPlayer;
        super.open(superiorPlayer, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        Map<String, UpgradesHandler.UpgradeData> upgrades = plugin.getUpgrades().getUpgrades();

        for(String upgrade : upgrades.keySet()){
            int level = island.getUpgradeLevel(upgrade);
            double nextLevelPrice = plugin.getUpgrades().getUpgradePrice(upgrade, level);
            UpgradesHandler.UpgradeData upgradeData = upgrades.get(upgrade);
            if(upgradeData.items.containsKey(level)) {
                UpgradesHandler.ItemData itemData = upgradeData.items.get(level);

                inv.setItem(itemData.slot, EconomyHook.getMoneyInBank(superiorPlayer) >= nextLevelPrice ?
                        itemData.hasNextLevel : itemData.noNextLevel);
            }
        }

        return inv;
    }

    public static void init(){
        IslandUpgradesMenu islandUpgradesMenu = new IslandUpgradesMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/upgrades-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/upgrades-gui.yml");

        CommentedConfiguration cfg = new CommentedConfiguration(null, file);

        ConfigurationSection section = cfg.getConfigurationSection("upgrades-gui");

        inventory = FileUtil.loadGUI(islandUpgradesMenu, section, 4, "&lIsland Upgrades");
        title = ChatColor.translateAlternateColorCodes('&', section.getString("title"));

        if(section.contains("upgrades")){
            ConfigurationSection upgrades = section.getConfigurationSection("upgrades");
            for(String _upgrade : upgrades.getKeys(false)){
                if(!plugin.getUpgrades().isUpgrade(_upgrade))
                    continue;

                UpgradesHandler.UpgradeData upgradeData = plugin.getUpgrades().getUpgrades().get(_upgrade);

                for(String level : upgrades.getConfigurationSection(_upgrade).getKeys(false)) {
                    int slot = upgrades.getInt(_upgrade + "." + level + ".slot");
                    upgradeData.items.put(Integer.valueOf(level), new UpgradesHandler.ItemData(
                            FileUtil.getItemStack(upgrades.getConfigurationSection(_upgrade + "." + level + ".has-next-level")),
                            FileUtil.getItemStack(upgrades.getConfigurationSection(_upgrade + "." + level + ".no-next-level")),
                            slot,
                            getSound(upgrades.getConfigurationSection(_upgrade + "." + level + ".has-next-level.sound")),
                            getSound(upgrades.getConfigurationSection(_upgrade + "." + level + ".no-next-level.sound"))));
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandUpgradesMenu(island).open(superiorPlayer, previousMenu);
    }

}
