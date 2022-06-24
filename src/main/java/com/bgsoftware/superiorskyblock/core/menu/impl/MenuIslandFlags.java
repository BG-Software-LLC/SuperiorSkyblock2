package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IslandFlagPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MenuIslandFlags extends PagedSuperiorMenu<MenuIslandFlags, MenuIslandFlags.IslandFlagInfo> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuIslandFlags, IslandFlagInfo> menuPattern;

    private static final List<IslandFlagInfo> islandFlags = new LinkedList<>();

    private static boolean convertedFlags = false;

    private final Island island;

    private MenuIslandFlags(SuperiorPlayer superiorPlayer, Island island) {
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

    @Override
    protected List<IslandFlagInfo> requestObjects() {
        if (!convertedFlags) {
            convertedFlags = !islandFlags.removeIf(IslandFlagInfo::tryValidate);
        }

        return Collections.unmodifiableList(islandFlags);
    }

    public static void init() {
        menuPattern = null;
        islandFlags.clear();

        PagedMenuPattern.Builder<MenuIslandFlags, IslandFlagInfo> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "settings.yml", MenuIslandFlags::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        int position = 0;

        if (cfg.isConfigurationSection("settings")) {
            for (String settingsSectionName : cfg.getConfigurationSection("settings").getKeys(false)) {
                updateSettings(settingsSectionName, cfg, position++);
            }
        }

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots),
                        new IslandFlagPagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuIslandFlags(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuIslandFlags.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void updateSettings(String islandFlagName) {
        File file = new File(plugin.getDataFolder(), "menus/settings.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for (String key : cfg.getConfigurationSection("settings").getKeys(false)) {
            if (islandFlagName.equalsIgnoreCase(key))
                break;

            position++;
        }

        updateSettings(islandFlagName, cfg, position);
    }

    public static void updateSettings(String islandFlagName, YamlConfiguration cfg, int position) {
        islandFlags.removeIf(islandFlagInfo -> islandFlagInfo.getIslandFlagName().equalsIgnoreCase(islandFlagName));

        TemplateItem enabledIslandFlagItem = null;
        TemplateItem disabledIslandFlagItem = null;
        GameSound clickSound = null;

        ConfigurationSection itemFlagSection = cfg.getConfigurationSection("settings." +
                islandFlagName.toLowerCase(Locale.ENGLISH));

        if (itemFlagSection != null) {
            enabledIslandFlagItem = MenuParser.getItemStack("settings.yml",
                    itemFlagSection.getConfigurationSection("settings-enabled"));
            disabledIslandFlagItem = MenuParser.getItemStack("settings.yml",
                    itemFlagSection.getConfigurationSection("settings-disabled"));
            clickSound = MenuParser.getSound(itemFlagSection.getConfigurationSection("sound"));
        }

        islandFlags.add(new IslandFlagInfo(islandFlagName, enabledIslandFlagItem,
                disabledIslandFlagItem, clickSound, position));
        Collections.sort(islandFlags);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/settings-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("settings-gui.title"));

        int size = cfg.getInt("settings-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("settings-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("settings-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("settings-gui"), newMenu,
                patternChars, slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++], SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("settings", cfg.getConfigurationSection("settings-gui.settings"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    public static class IslandFlagInfo implements Comparable<IslandFlagInfo> {

        private final String islandFlagName;
        private final TemplateItem enabledIslandFlagItem;
        private final TemplateItem disabledIslandFlagItem;
        private final GameSound clickSound;
        private final int position;

        private IslandFlag cachedIslandFlag = null;

        public IslandFlagInfo(String islandFlagName, TemplateItem enabledIslandFlagItem,
                              TemplateItem disabledIslandFlagItem, GameSound clickSound, int position) {
            this.islandFlagName = islandFlagName;
            this.enabledIslandFlagItem = enabledIslandFlagItem;
            this.disabledIslandFlagItem = disabledIslandFlagItem;
            this.clickSound = clickSound;
            this.position = position;
        }

        @Nullable
        public IslandFlag getIslandFlag() {
            if (cachedIslandFlag == null) {
                try {
                    cachedIslandFlag = IslandFlag.getByName(islandFlagName);
                } catch (Exception ignored) {
                    // Flag is faulty.
                }
            }

            return cachedIslandFlag;
        }

        public boolean tryValidate() {
            if (cachedIslandFlag == null) {
                cachedIslandFlag = getIslandFlag();
            }

            return cachedIslandFlag == null;
        }

        public String getIslandFlagName() {
            return islandFlagName;
        }

        public ItemBuilder getEnabledIslandFlagItem() {
            return enabledIslandFlagItem.getBuilder();
        }

        public ItemBuilder getDisabledIslandFlagItem() {
            return disabledIslandFlagItem.getBuilder();
        }

        public GameSound getClickSound() {
            return clickSound;
        }

        @Override
        public int compareTo(@NotNull MenuIslandFlags.IslandFlagInfo other) {
            return Integer.compare(position, other.position);
        }

    }

}
