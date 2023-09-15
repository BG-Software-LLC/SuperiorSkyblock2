package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BiomeButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

public class MenuBiomes extends AbstractMenu<IslandMenuView, IslandViewArgs> {

    private final boolean currentBiomeGlow;

    private MenuBiomes(MenuParseResult<IslandMenuView> parseResult, boolean currentBiomeGlow) {
        super(MenuIdentifiers.MENU_BIOMES, parseResult);
        this.currentBiomeGlow = currentBiomeGlow;
    }

    public boolean isCurrentBiomeGlow() {
        return currentBiomeGlow;
    }

    @Override
    protected IslandMenuView createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                                @Nullable MenuView<?, ?> previousMenuView) {
        return new IslandMenuView(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuBiomes createInstance() {
        MenuParseResult<IslandMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("biomes.yml",
                MenuBiomes::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<IslandMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        boolean shouldCurrentBiomeGlow = cfg.getBoolean("current-biome-glow", false);

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
                    Log.warnFromFile("biomes.yml", "Biome '", biomeName, "' is not valid, skipping...");
                    continue;
                }

                ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + itemSectionName);
                ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + itemSectionName);

                BiomeButton.Builder buttonBuilder = new BiomeButton.Builder(biome);

                if (itemSection.contains("access")) {
                    buttonBuilder.setAccessItem(MenuParserImpl.getInstance().getItemStack("biomes.yml",
                            itemSection.getConfigurationSection("access")));
                }
                if (itemSection.contains("no-access")) {
                    buttonBuilder.setNoAccessItem(MenuParserImpl.getInstance().getItemStack("biomes.yml",
                            itemSection.getConfigurationSection("no-access")));
                }
                if (soundSection != null) {
                    if (soundSection.contains("access")) {
                        buttonBuilder.setAccessSound(MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("access")));
                    }
                    if (soundSection.contains("no-access")) {
                        buttonBuilder.setNoAccessSound(MenuParserImpl.getInstance().getSound(soundSection.getConfigurationSection("no-access")));
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

        return new MenuBiomes(menuParseResult, shouldCurrentBiomeGlow);
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
                char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
                section.set("biome", biomeName.toUpperCase(Locale.ENGLISH));
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
