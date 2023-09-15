package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeySets;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.ValuesButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MenuIslandValues extends AbstractMenu<MenuIslandValues.View, IslandViewArgs> {

    private MenuIslandValues(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_VALUES, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    @Nullable
    public static MenuIslandValues createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("values.yml",
                MenuIslandValues::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        KeySet keysToUpdate = KeySets.createHashSet(KeyIndicator.MATERIAL);

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                String block = itemsSection.getString("block");

                if (block == null)
                    continue;

                Key blockKey = Keys.ofMaterialAndData(block);
                keysToUpdate.add(blockKey);

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemsSectionName), new ValuesButton.Builder(blockKey));
            }
        }

        plugin.getBlockValues().registerMenuValueBlocks(keysToUpdate);

        return new MenuIslandValues(menuParseResult);
    }

    public static class View extends AbstractMenuView<View, IslandViewArgs> {

        private final Island island;
        private SuperiorPlayer targetPlayer;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.targetPlayer = args.getIsland().getOwner();
        }

        public Island getIsland() {
            return island;
        }

        public void setTargetPlayer(SuperiorPlayer targetPlayer) {
            this.targetPlayer = targetPlayer;
        }

        public SuperiorPlayer getTargetPlayer() {
            return targetPlayer;
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", island.getOwner().getName())
                    .replace("{1}", Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                    .replace("{2}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), getInventoryViewer().getUserLocale()));
        }

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
            char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
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
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

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
