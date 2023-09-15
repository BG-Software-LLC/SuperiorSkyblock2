package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BorderColorButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BorderColorToggleButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuBorderColor extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private MenuBorderColor(MenuParseResult<BaseMenuView> parseResult) {
        super(MenuIdentifiers.MENU_BORDER_COLOR, parseResult);
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    @Nullable
    public static MenuBorderColor createInstance() {
        MenuParseResult<BaseMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("border-color.yml",
                MenuBorderColor::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<BaseMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                if (!itemsSection.contains("enable-border") || !itemsSection.contains("disable-border"))
                    continue;

                patternBuilder.setButtons(menuPatternSlots.getSlots(itemsSectionName),
                        new BorderColorToggleButton.Builder()
                                .setEnabledItem(MenuParserImpl.getInstance().getItemStack("border-color.yml", itemsSection.getConfigurationSection("disable-border")))
                                .setDisabledItem(MenuParserImpl.getInstance().getItemStack("border-color.yml", itemsSection.getConfigurationSection("enable-border")))
                                .build());
            }
        }

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "green-color", menuPatternSlots),
                new BorderColorButton.Builder().setBorderColor(BorderColor.GREEN));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "red-color", menuPatternSlots),
                new BorderColorButton.Builder().setBorderColor(BorderColor.RED));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "blue-color", menuPatternSlots),
                new BorderColorButton.Builder().setBorderColor(BorderColor.BLUE));

        return new MenuBorderColor(menuParseResult);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/border-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("border-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("border-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("border-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char greenChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char blueChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char redChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.green_color"), patternChars, greenChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.blue_color"), patternChars, blueChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("border-gui.red_color"), patternChars, redChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("green-color", greenChar + "");
        newMenu.set("red-color", redChar + "");
        newMenu.set("blue-color", blueChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
