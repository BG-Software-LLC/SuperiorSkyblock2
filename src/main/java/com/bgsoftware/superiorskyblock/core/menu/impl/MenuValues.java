package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeySetImpl;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.ValuesButton;
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
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MenuValues extends SuperiorMenu<MenuValues> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static RegularMenuPattern<MenuValues> menuPattern;

    private final Island island;

    private MenuValues(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
        if (island != null)
            updateTargetPlayer(island.getOwner());
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", island.getOwner().getName())
                .replace("{1}", Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                .replace("{2}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), inventoryViewer.getUserLocale()));
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuValues> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "values.yml",
                MenuValues::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        KeySet keysToUpdate = KeySetImpl.createHashSet();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                String block = itemsSection.getString("block");

                if (block == null)
                    continue;

                Key blockKey = KeyImpl.of(block);
                keysToUpdate.add(blockKey);

                patternBuilder.mapButton(menuPatternSlots.getSlot(itemsSectionName), new ValuesButton.Builder(blockKey));
            }
        }

        menuPattern = patternBuilder.build();

        plugin.getBlockValues().registerMenuValueBlocks(keysToUpdate);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuValues(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuValues.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/values-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("values-gui.title"));

        int size = cfg.getInt("values-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("values-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("values-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        ConfigurationSection blockItemSection = cfg.getConfigurationSection("values-gui.block-item");

        for (String material : cfg.getStringList("values-gui.materials")) {
            char itemChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
            ConfigurationSection section = itemsSection.createSection(itemChar + "");
            String[] materialSections = material.split(":");
            String block = materialSections.length == 2 ? materialSections[0] : materialSections[0] + ":" + materialSections[1];
            int slot = Integer.parseInt(materialSections.length == 2 ? materialSections[1] : materialSections[2]);
            copySection(blockItemSection, section, str ->
                    str.replace("{0}", Formatters.CAPITALIZED_FORMATTER.format(block)).replace("{1}", "{0}"));
            section.set("block", block);
            convertType(section, block);
            patternChars[slot] = itemChar;
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    private static void copySection(ConfigurationSection source, ConfigurationSection dest, Function<String, String> stringReplacer) {
        for (String key : source.getKeys(false)) {
            if (source.isConfigurationSection(key)) {
                copySection(source.getConfigurationSection(key), dest.createSection(key), stringReplacer);
            } else if (source.isList(key)) {
                dest.set(key, source.getStringList(key).stream().map(stringReplacer).collect(Collectors.toList()));
            } else if (source.isString(key)) {
                dest.set(key, stringReplacer.apply(source.getString(key)));
            } else {
                dest.set(key, source.getString(key));
            }
        }
    }

    private static void convertType(ConfigurationSection section, String block) {
        String[] materialSections = block.split(":");
        String spawnerType = materialSections[0],
                entityType = (materialSections.length >= 2 ? materialSections[1] : "PIG").toUpperCase(Locale.ENGLISH);
        if (spawnerType.equals(Materials.SPAWNER.toBukkitType() + "")) {
            String texture = ItemSkulls.getTexture(entityType);
            if (!texture.isEmpty()) {
                section.set("type", Materials.PLAYER_HEAD.toBukkitType().name());
                if (section.getString("type").equalsIgnoreCase("SKULL_ITEM"))
                    section.set("data", 3);
                section.set("skull", texture);
                return;
            }
        }

        section.set("type", spawnerType.equals(Materials.SPAWNER.toBukkitType() + "") ? spawnerType : block);
    }

}
