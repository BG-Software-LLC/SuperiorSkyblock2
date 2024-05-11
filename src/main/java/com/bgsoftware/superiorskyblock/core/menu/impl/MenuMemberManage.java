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
import com.bgsoftware.superiorskyblock.core.menu.button.impl.MemberManageButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.PlayerViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuMemberManage extends AbstractMenu<MenuMemberManage.View, PlayerViewArgs> {

    private MenuMemberManage(MenuParseResult<MenuMemberManage.View> parseResult) {
        super(MenuIdentifiers.MENU_MEMBER_MANAGE, parseResult);
    }

    @Override
    protected MenuMemberManage.View createViewInternal(SuperiorPlayer superiorPlayer, PlayerViewArgs args,
                                                       @Nullable MenuView<?, ?> previousMenuView) {
        return new MenuMemberManage.View(superiorPlayer, previousMenuView, this, args);
    }

    public void closeViews(SuperiorPlayer islandMember) {
        closeViews(view -> view.getSuperiorPlayer().equals(islandMember));
    }

    @Nullable
    public static MenuMemberManage createInstance() {
        MenuParseResult<MenuMemberManage.View> menuParseResult = MenuParserImpl.getInstance().loadMenu("member-manage.yml",
                MenuMemberManage::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<MenuMemberManage.View> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "roles", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.SET_ROLE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "ban", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.BAN_MEMBER));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "kick", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.KICK_MEMBER));

        return new MenuMemberManage(menuParseResult);
    }

    public static class View extends AbstractMenuView<View, PlayerViewArgs> {

        private final SuperiorPlayer superiorPlayer;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             MenuMemberManage menu, PlayerViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.superiorPlayer = args.getSuperiorPlayer();
        }

        public SuperiorPlayer getSuperiorPlayer() {
            return this.superiorPlayer;
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{}", getSuperiorPlayer().getName());
        }

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

        newMenu.set("title", cfg.getString("players-panel.title"));

        int size = cfg.getInt("players-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("players-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("players-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char rolesChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char banChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char kickChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.roles"), patternChars, rolesChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.ban"), patternChars, banChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.kick"), patternChars, kickChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("roles", rolesChar + "");
        newMenu.set("ban", banChar + "");
        newMenu.set("kick", kickChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
