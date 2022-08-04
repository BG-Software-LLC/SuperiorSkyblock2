package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.UpgradeButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MenuUpgrades extends SuperiorMenu<MenuUpgrades> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static RegularMenuPattern<MenuUpgrades> menuPattern;

    private final Island island;

    private MenuUpgrades(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuUpgrades> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "upgrades.yml",
                MenuUpgrades::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        BukkitExecutor.sync(() -> {
            if (cfg.contains("upgrades")) {
                ConfigurationSection upgradesSection = cfg.getConfigurationSection("upgrades");
                for (Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
                    ConfigurationSection upgradeSection = upgradesSection.getConfigurationSection(upgrade.getName());

                    if (upgradeSection == null) {
                        continue;
                    }

                    int slot = getSlots(upgradeSection, "item", menuPatternSlots).get(0);
                    upgrade.setSlot(slot);

                    patternBuilder.mapButton(slot, new UpgradeButton.Builder(upgrade));

                    for (String levelSectionKey : upgradeSection.getKeys(false)) {
                        int level;

                        try {
                            level = Integer.parseInt(levelSectionKey);
                        } catch (NumberFormatException error) {
                            // Not a number, skipping.
                            continue;
                        }

                        if (slot == -1) {
                            SuperiorSkyblockPlugin.log("&cThe item of the upgrade " + upgrade.getName() + " (level " + level + ") is not inside the pattern, skipping...");
                            continue;
                        }

                        SUpgradeLevel upgradeLevel = (SUpgradeLevel) upgrade.getUpgradeLevel(level);

                        if (upgradeLevel != null) {
                            TemplateItem hasNextLevel = MenuParser.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".has-next-level"));
                            if (hasNextLevel == null) {
                                SuperiorSkyblockPlugin.log("&cThe upgrade " + upgrade.getName() + " (level " + level + ") is missing has-next-level item.");
                            }

                            TemplateItem noNextLevel = MenuParser.getItemStack("upgrades.yml", upgradeSection.getConfigurationSection(level + ".no-next-level"));
                            if (noNextLevel == null) {
                                SuperiorSkyblockPlugin.log("&cThe upgrade " + upgrade.getName() + " (level " + level + ") is missing no-next-level item.");
                            }

                            GameSound hasNextLevelSound = MenuParser.getSound(upgradeSection.getConfigurationSection(level + ".has-next-level.sound"));
                            GameSound noNextLevelSound = MenuParser.getSound(upgradeSection.getConfigurationSection(level + ".no-next-level.sound"));
                            List<String> hasNextLevelCommands = upgradeSection.getStringList(level + ".has-next-level.commands");
                            List<String> noNextLevelCommands = upgradeSection.getStringList(level + ".no-next-level.commands");
                            upgradeLevel.setItemData(hasNextLevel, noNextLevel, hasNextLevelSound, noNextLevelSound, hasNextLevelCommands, noNextLevelCommands);
                        }
                    }
                }
            }

            menuPattern = patternBuilder.build();
        }, 5L);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuUpgrades(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuUpgrades.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/upgrades-gui.yml");

        if (!oldFile.exists())
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

        if (cfg.contains("upgrades-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("upgrades-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("upgrades-gui.upgrades")) {
            for (String upgradeName : cfg.getConfigurationSection("upgrades-gui.upgrades").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("upgrades-gui.upgrades." + upgradeName);
                char itemChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
                section.set("item", itemChar + "");
                patternChars[section.getInt("1.slot")] = itemChar;
                for (String upgradeLevel : section.getKeys(false)) {
                    section.set(upgradeLevel + ".slot", null);
                }
            }
        }

        newMenu.set("upgrades", cfg.getConfigurationSection("upgrades-gui.upgrades"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
