package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DisbandButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuConfirmDisband extends AbstractMenu<IslandMenuView, IslandViewArgs> {

    private MenuConfirmDisband(MenuParseResult<IslandMenuView> parseResult) {
        super(MenuIdentifiers.MENU_CONFIRM_DISBAND, parseResult);
    }

    @Override
    protected IslandMenuView createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                                @Nullable MenuView<?, ?> previousMenuView) {
        return new IslandMenuView(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuConfirmDisband createInstance() {
        MenuParseResult<IslandMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("confirm-disband.yml",
                MenuConfirmDisband::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<IslandMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "confirm", menuPatternSlots),
                new DisbandButton.Builder().setDisbandIsland(true));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "cancel", menuPatternSlots),
                new DisbandButton.Builder());

        return new MenuConfirmDisband(menuParseResult);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/confirm-disband.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("disband-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("disband-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("disband-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char confirmChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char cancelChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("disband-gui.confirm"), patternChars, confirmChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("disband-gui.cancel"), patternChars, cancelChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("confirm", confirmChar + "");
        newMenu.set("cancel", cancelChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
