package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BiomeButton;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MenuBiomes extends SuperiorMenu<MenuBiomes> {

    private static RegularMenuPattern<MenuBiomes> menuPattern;

    public static boolean currentBiomeGlow = false;

    private final Island targetIsland;

    private MenuBiomes(SuperiorPlayer superiorPlayer, Island targetIsland) {
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

    @Override
    public void onButtonClickLackPermission(SuperiorMenuButton<MenuBiomes> menuButton, InventoryClickEvent clickEvent) {
        super.onButtonClickLackPermission(menuButton, clickEvent);
        if (menuButton instanceof BiomeButton) {
            List<String> commands = ((BiomeButton) menuButton).getLackPermissionCommands();
            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", inventoryViewer.getName())));
        }
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuBiomes> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "biomes.yml", MenuBiomes::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        currentBiomeGlow = cfg.getBoolean("current-biome-glow", false);

        if (cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (!itemSection.isString("biome"))
                    continue;

                String biomeName = itemSection.getString("biome");
                Biome biome;

                try {
                    biome = Biome.valueOf(biomeName.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException error) {
                    SuperiorSkyblockPlugin.log("&cBiome '" + biomeName + "' is not valid, skipping...");
                    continue;
                }

                ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + itemSectionName);
                ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + itemSectionName);

                BiomeButton.Builder buttonBuilder = new BiomeButton.Builder(biome);

                if (itemSection.contains("access")) {
                    buttonBuilder.setAccessItem(MenuParser.getItemStack("biomes.yml",
                            itemSection.getConfigurationSection("access")));
                }
                if (itemSection.contains("no-access")) {
                    buttonBuilder.setNoAccessItem(MenuParser.getItemStack("biomes.yml",
                            itemSection.getConfigurationSection("no-access")));
                }
                if (soundSection != null) {
                    if (soundSection.contains("access")) {
                        buttonBuilder.setAccessSound(MenuParser.getSound(soundSection.getConfigurationSection("access")));
                    }
                    if (soundSection.contains("no-access")) {
                        buttonBuilder.setNoAccessSound(MenuParser.getSound(soundSection.getConfigurationSection("no-access")));
                    }
                }
                if (commandSection != null) {
                    if (commandSection.contains("access")) {
                        buttonBuilder.setAccessCommands(commandSection.getStringList("access"));
                    }
                    if (commandSection.contains("no-access")) {
                        buttonBuilder.setNoAccessCommands(commandSection.getStringList("no-access"));
                    }
                }

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemSectionName), buttonBuilder);
            }
        }

        menuPattern = patternBuilder.build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland) {
        new MenuBiomes(superiorPlayer, targetIsland).open(previousMenu);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("biomes-gui.title"));

        int size = cfg.getInt("biomes-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("biomes-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("biomes-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("biomes-gui.biomes")) {
            for (String biomeName : cfg.getConfigurationSection("biomes-gui.biomes").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes." + biomeName);
                char itemChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
                section.set("biome", biomeName.toUpperCase(Locale.ENGLISH));
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
