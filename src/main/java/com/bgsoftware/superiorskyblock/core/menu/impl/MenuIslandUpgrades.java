package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.UpgradeButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MenuIslandUpgrades extends AbstractMenu<IslandMenuView, IslandViewArgs> {

    private MenuIslandUpgrades(MenuParseResult<IslandMenuView> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_UPGRADES, parseResult);
    }

    @Override
    protected IslandMenuView createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                                @Nullable MenuView<?, ?> previousMenuView) {
        return new IslandMenuView(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.getIsland().equals(island));
    }

    @Nullable
    public static MenuIslandUpgrades createInstance() {
        MenuParseResult<IslandMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("upgrades.yml",
                MenuIslandUpgrades::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<IslandMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.contains("upgrades")) {
            ConfigurationSection upgradesSection = cfg.getConfigurationSection("upgrades");
            for (Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
                ConfigurationSection upgradeSection = upgradesSection.getConfigurationSection(upgrade.getName());

                if (upgradeSection == null) {
                    continue;
                }

                List<Integer> slots = MenuParserImpl.getInstance().parseButtonSlots(upgradeSection, "item", menuPatternSlots);
                upgrade.setSlots(slots);

                patternBuilder.mapButtons(slots, new UpgradeButton.Builder(upgrade));

                for (String levelSectionKey : upgradeSection.getKeys(false)) {
                    int level;

                    try {
                        level = Integer.parseInt(levelSectionKey);
                    } catch (NumberFormatException error) {
                        // Not a number, skipping.
                        continue;
                    }

                    if (slots.isEmpty()) {
                        Log.warnFromFile("upgrades.yml", "The item of the upgrade ", upgrade.getName(),
                                " (level ", level, ") is not inside the pattern, skipping...");
                        continue;
                    }

                    SUpgradeLevel upgradeLevel = (SUpgradeLevel) upgrade.getUpgradeLevel(level);

                    if (upgradeLevel != null) {
                        TemplateItem hasNextLevel = MenuParserImpl.getInstance().getItemStack("upgrades.yml",
                                upgradeSection.getConfigurationSection(level + ".has-next-level"));
                        if (hasNextLevel == null) {
                            Log.warnFromFile("upgrades.yml", "The upgrade ", upgrade.getName(),
                                    " (level ", level, ") is missing has-next-level item.");
                        }

                        TemplateItem noNextLevel = MenuParserImpl.getInstance().getItemStack("upgrades.yml",
                                upgradeSection.getConfigurationSection(level + ".no-next-level"));
                        if (noNextLevel == null) {
                            Log.warnFromFile("upgrades.yml", "&cThe upgrade ", upgrade.getName(),
                                    " (level ", level, ") is missing no-next-level item.");
                        }

                        GameSound hasNextLevelSound = MenuParserImpl.getInstance().getSound(upgradeSection.getConfigurationSection(level + ".has-next-level.sound"));
                        GameSound noNextLevelSound = MenuParserImpl.getInstance().getSound(upgradeSection.getConfigurationSection(level + ".no-next-level.sound"));
                        List<String> hasNextLevelCommands = upgradeSection.getStringList(level + ".has-next-level.commands");
                        List<String> noNextLevelCommands = upgradeSection.getStringList(level + ".no-next-level.commands");
                        upgradeLevel.setItemData(hasNextLevel, noNextLevel, hasNextLevelSound, noNextLevelSound, hasNextLevelCommands, noNextLevelCommands);
                    }
                }
            }
        }

        return new MenuIslandUpgrades(menuParseResult);
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
                char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
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
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
