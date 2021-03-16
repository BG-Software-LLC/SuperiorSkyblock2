package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.upgrades.SUpgrade;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MenuUpgrades extends SuperiorMenu {

    private static final ItemBuilder INVALID_ITEM = new ItemBuilder(Material.BEDROCK).withName("&c&lInvalid Item");

    private final Island island;

    private MenuUpgrades(SuperiorPlayer superiorPlayer, Island island){
        super("menuUpgrades", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(e.getRawSlot());

        if(upgrade != null){
            CommandUtils.dispatchSubCommand(e.getWhoClicked(), "rankup " + upgrade.getName());
            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inv = super.buildInventory(titleReplacer);

        for(Upgrade upgrade : plugin.getUpgrades().getUpgrades()){
            UpgradeLevel upgradeLevel = island.getUpgradeLevel(upgrade);

            if(upgradeLevel != null){
                UpgradeLevel nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

                UpgradeCost levelCost = upgradeLevel.getCost();
                String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();
                String requirements = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(superiorPlayer);

                SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) upgradeLevel).getItemData();
                if(itemData != null) {
                    boolean nextLevel = levelCost.hasEnoughBalance(superiorPlayer) &&
                            (permission.isEmpty() || superiorPlayer.hasPermission(permission)) && requirements.isEmpty();
                    inv.setItem(((SUpgrade) upgrade).getMenuSlot(), (nextLevel ? itemData.hasNextLevel : itemData.noNextLevel).clone().build(superiorPlayer));
                }
            }
        }

        return inv;
    }

    public static void init(){
        MenuUpgrades menuUpgrades = new MenuUpgrades(null, null);

        File file = new File(plugin.getDataFolder(), "menus/upgrades.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/upgrades.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuUpgrades, "upgrades.yml", cfg);

        if(cfg.contains("upgrades")){
            ConfigurationSection upgradesSection = cfg.getConfigurationSection("upgrades");
            for(Upgrade upgrade : plugin.getUpgrades().getUpgrades()){
                ConfigurationSection upgradeSection = upgradesSection.getConfigurationSection(upgrade.getName());

                if(upgradeSection == null){
                    SuperiorSkyblockPlugin.log("&cThe upgrade " + upgrade.getName() + " doesn't have an item in the menu.");
                    continue;
                }

                int slot = getSlots(upgradeSection, "item", charSlots).get(0);
                ((SUpgrade) upgrade).setMenuSlot(slot);

                for(String level : upgradeSection.getKeys(false)) {
                    if(NumberUtils.isNumber(level)) {
                        if(slot == -1){
                            SuperiorSkyblockPlugin.log("&cThe item of the upgrade " + upgrade.getName() + " (level " + level + ") is not inside the pattern, skipping...");
                            continue;
                        }

                        SUpgradeLevel upgradeLevel = (SUpgradeLevel) upgrade.getUpgradeLevel(Integer.parseInt(level));

                        if(upgradeLevel != null) {
                            ItemBuilder hasNextLevel = FileUtils.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".has-next-level"));

                            if(hasNextLevel == null){
                                SuperiorSkyblockPlugin.log("&cThe upgrade " + upgrade.getName() + " (level " + level + ") is missing has-next-level item.");
                                hasNextLevel = INVALID_ITEM.clone();
                            }

                            ItemBuilder noNextLevel = FileUtils.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".no-next-level"));

                            if(noNextLevel == null){
                                SuperiorSkyblockPlugin.log("&cThe upgrade " + upgrade.getName() + " (level " + level + ") is missing no-next-level item.");
                                noNextLevel = INVALID_ITEM.clone();
                            }

                            SoundWrapper hasNextLevelSound = FileUtils.getSound(upgradeSection.getConfigurationSection(level + ".has-next-level.sound"));
                            SoundWrapper noNextLevelSound = FileUtils.getSound(upgradeSection.getConfigurationSection(level + ".no-next-level.sound"));
                            List<String> hasNextLevelCommands = upgradeSection.getStringList(level + ".has-next-level.commands");
                            List<String> noNextLevelCommands = upgradeSection.getStringList(level + ".no-next-level.commands");
                            upgradeLevel.setItemData(hasNextLevel, noNextLevel, hasNextLevelSound, noNextLevelSound, hasNextLevelCommands, noNextLevelCommands);
                        }
                    }
                }
            }
        }

        charSlots.delete();

        menuUpgrades.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuUpgrades(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        refreshMenus(MenuUpgrades.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/upgrades-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("upgrades-gui.title"));

        int size = cfg.getInt("upgrades-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("upgrades-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("upgrades-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("upgrades-gui.upgrades")) {
            for (String upgradeName : cfg.getConfigurationSection("upgrades-gui.upgrades").getKeys(false)){
                ConfigurationSection section = cfg.getConfigurationSection("upgrades-gui.upgrades." + upgradeName);
                char itemChar = itemChars[charCounter++];
                section.set("item", itemChar + "");
                patternChars[section.getInt("1.slot")] = itemChar;
                for(String upgradeLevel : section.getKeys(false)){
                    section.set(upgradeLevel + ".slot", null);
                }
            }
        }

        newMenu.set("upgrades", cfg.getConfigurationSection("upgrades-gui.upgrades"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
