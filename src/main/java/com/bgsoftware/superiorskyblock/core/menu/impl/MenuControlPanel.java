package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.ControlPanelButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuControlPanel extends SuperiorMenu<MenuControlPanel> {

    private static RegularMenuPattern<MenuControlPanel> menuPattern;

    private final Island targetIsland;

    private MenuControlPanel(SuperiorPlayer superiorPlayer, Island targetIsland) {
        super(menuPattern, superiorPlayer);
        this.targetIsland = targetIsland;
    }

    public Island getTargetIsland() {
        return targetIsland;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, targetIsland);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuControlPanel> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "control-panel.yml",
                MenuControlPanel::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "members", menuPatternSlots),
                        new ControlPanelButton.Builder().setAction(ControlPanelButton.ControlPanelAction.OPEN_MEMBERS))
                .mapButtons(getSlots(cfg, "settings", menuPatternSlots),
                        new ControlPanelButton.Builder().setAction(ControlPanelButton.ControlPanelAction.OPEN_SETTINGS))
                .mapButtons(getSlots(cfg, "visitors", menuPatternSlots),
                        new ControlPanelButton.Builder().setAction(ControlPanelButton.ControlPanelAction.OPEN_VISITORS))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland) {
        new MenuControlPanel(superiorPlayer, targetIsland).open(previousMenu);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("main-panel.title"));

        int size = cfg.getInt("main-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("main-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("main-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char membersChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char settingsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char visitorsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.members"), patternChars, membersChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.settings"), patternChars, settingsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.visitors"), patternChars, visitorsChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("members", membersChar + "");
        newMenu.set("settings", settingsChar + "");
        newMenu.set("visitors", visitorsChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
