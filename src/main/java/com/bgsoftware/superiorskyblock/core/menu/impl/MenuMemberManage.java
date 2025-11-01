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
import com.bgsoftware.superiorskyblock.core.menu.view.args.PlayerViewArgs;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.PlayerMenuView;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuMemberManage extends AbstractMenu<PlayerMenuView, PlayerViewArgs> {

    private MenuMemberManage(MenuParseResult<PlayerMenuView> parseResult) {
        super(MenuIdentifiers.MENU_MEMBER_MANAGE, parseResult);
    }

    @Override
    protected PlayerMenuView createViewInternal(SuperiorPlayer superiorPlayer, PlayerViewArgs args,
                                                @Nullable MenuView<?, ?> previousMenuView) {
        return new PlayerMenuView(superiorPlayer, previousMenuView, this, args);
    }

    public void closeViews(SuperiorPlayer islandMember) {
        closeViews(view -> view.getSuperiorPlayer().equals(islandMember));
    }

    @Nullable
    public static MenuMemberManage createInstance() {
        MenuParseResult<PlayerMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("member-manage.yml",
                MenuMemberManage::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<PlayerMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "roles", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.SET_ROLE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "ban", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.BAN_MEMBER));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "kick", menuPatternSlots),
                new MemberManageButton.Builder().setManageAction(MemberManageButton.ManageAction.KICK_MEMBER));

        return new MenuMemberManage(menuParseResult);
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

        if (cfg.isConfigurationSection("players-panel.fill-items")) {
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
