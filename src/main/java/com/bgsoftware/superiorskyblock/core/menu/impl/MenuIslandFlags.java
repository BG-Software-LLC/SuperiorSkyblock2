package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IslandFlagPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
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

public class MenuIslandFlags extends AbstractPagedMenu<MenuIslandFlags.View, IslandViewArgs, MenuIslandFlags.IslandFlagInfo> {

    private final List<MenuIslandFlags.IslandFlagInfo> islandFlags;

    private MenuIslandFlags(MenuParseResult<View> parseResult, List<MenuIslandFlags.IslandFlagInfo> islandFlags) {
        super(MenuIdentifiers.MENU_ISLAND_FLAGS, parseResult, false);
        this.islandFlags = islandFlags;
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
    public static MenuIslandFlags createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("settings.yml",
                MenuIslandFlags::convertOldGUI, new IslandFlagPagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        YamlConfiguration cfg = menuParseResult.getConfig();

        List<MenuIslandFlags.IslandFlagInfo> islandFlags = new LinkedList<>();

        int position = 0;

        if (cfg.isConfigurationSection("settings")) {
            for (String islandFlag : cfg.getConfigurationSection("settings").getKeys(false)) {
                try {
                    updateSettingsInternal(islandFlags, IslandFlag.getByName(islandFlag), cfg, position++);
                } catch (NullPointerException error) {
                    Log.warnFromFile("settings.yml", "The island-flag '", islandFlag, "' is not a valid flag, skipping...");
                }
            }
        }

        return new MenuIslandFlags(menuParseResult, islandFlags);
    }

    public void updateSettings(IslandFlag islandFlag) {
        File file = new File(plugin.getDataFolder(), "menus/settings.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for (String key : cfg.getConfigurationSection("settings").getKeys(false)) {
            if (islandFlag.getName().equalsIgnoreCase(key))
                break;

            position++;
        }

        updateSettingsInternal(islandFlags, islandFlag, cfg, position);
    }

    private static void updateSettingsInternal(List<IslandFlagInfo> islandFlags, IslandFlag islandFlag,
                                               YamlConfiguration cfg, int position) {
        islandFlags.removeIf(islandFlagInfo -> islandFlagInfo.getIslandFlag() == islandFlag);

        TemplateItem enabledIslandFlagItem = null;
        TemplateItem disabledIslandFlagItem = null;
        GameSound clickSound = null;

        ConfigurationSection itemFlagSection = cfg.getConfigurationSection("settings." +
                islandFlag.getName().toLowerCase(Locale.ENGLISH));

        if (itemFlagSection != null) {
            enabledIslandFlagItem = MenuParserImpl.getInstance().getItemStack("settings.yml",
                    itemFlagSection.getConfigurationSection("settings-enabled"));
            disabledIslandFlagItem = MenuParserImpl.getInstance().getItemStack("settings.yml",
                    itemFlagSection.getConfigurationSection("settings-disabled"));
            clickSound = MenuParserImpl.getInstance().getSound(itemFlagSection.getConfigurationSection("sound"));
        }

        islandFlags.add(new MenuIslandFlags.IslandFlagInfo(islandFlag, enabledIslandFlagItem,
                disabledIslandFlagItem, clickSound, position));

        Collections.sort(islandFlags);
    }

    public class View extends AbstractPagedMenuView<MenuIslandFlags.View, IslandViewArgs, IslandFlagInfo> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        public Island getIsland() {
            return island;
        }

        @Override
        protected List<IslandFlagInfo> requestObjects() {
            return Collections.unmodifiableList(islandFlags);
        }

    }

    public static class IslandFlagInfo implements Comparable<MenuIslandFlags.IslandFlagInfo> {

        private final IslandFlag islandFlag;
        private final TemplateItem enabledIslandFlagItem;
        private final TemplateItem disabledIslandFlagItem;
        private final GameSound clickSound;
        private final int position;


        public IslandFlagInfo(IslandFlag islandFlag, TemplateItem enabledIslandFlagItem,
                              TemplateItem disabledIslandFlagItem, GameSound clickSound, int position) {
            this.islandFlag = islandFlag;
            this.enabledIslandFlagItem = enabledIslandFlagItem;
            this.disabledIslandFlagItem = disabledIslandFlagItem;
            this.clickSound = clickSound;
            this.position = position;
        }

        @Nullable
        public IslandFlag getIslandFlag() {
            return this.islandFlag;
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

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("settings-gui"), newMenu,
                patternChars, slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++], AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("settings", cfg.getConfigurationSection("settings-gui.settings"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }


}
