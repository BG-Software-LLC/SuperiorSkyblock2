package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class MenuControlPanel extends SuperiorMenu {

    private static List<Integer> membersSlot, settingsSlot, visitorsSlot;

    private final Island targetIsland;

    private MenuControlPanel(SuperiorPlayer superiorPlayer, Island targetIsland) {
        super("menuControlPanel", superiorPlayer);
        this.targetIsland = targetIsland;
    }

    public static void init() {
        MenuControlPanel menuControlPanel = new MenuControlPanel(null, null);

        File file = new File(plugin.getDataFolder(), "menus/control-panel.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/control-panel.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if (convertOldGUI(cfg)) {
            try {
                cfg.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
        }

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuControlPanel, "control-panel.yml", cfg);

        membersSlot = getSlots(cfg, "members", menuPatternSlots);
        settingsSlot = getSlots(cfg, "settings", menuPatternSlots);
        visitorsSlot = getSlots(cfg, "visitors", menuPatternSlots);

        menuControlPanel.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland) {
        new MenuControlPanel(superiorPlayer, targetIsland).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu) {
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

        char membersChar = itemChars[charCounter++], settingsChar = itemChars[charCounter++], visitorsChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.members"), patternChars, membersChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.settings"), patternChars, settingsChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("main-panel.visitors"), patternChars, visitorsChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("members", membersChar + "");
        newMenu.set("settings", settingsChar + "");
        newMenu.set("visitors", visitorsChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if (membersSlot.contains(e.getRawSlot())) {
            plugin.getMenus().openMembers(superiorPlayer, this, targetIsland);
        } else if (settingsSlot.contains(e.getRawSlot())) {
            if (superiorPlayer.hasPermission("superior.island.settings")) {
                if (!superiorPlayer.hasPermission(IslandPrivileges.SET_SETTINGS)) {
                    Message.NO_SET_SETTINGS_PERMISSION.send(superiorPlayer, targetIsland.getRequiredPlayerRole(IslandPrivileges.SET_SETTINGS));
                    return;
                }

                plugin.getMenus().openSettings(superiorPlayer, this, targetIsland);
            }
        } else if (visitorsSlot.contains(e.getRawSlot())) {
            plugin.getMenus().openVisitors(superiorPlayer, this, targetIsland);
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetIsland);
    }

}
