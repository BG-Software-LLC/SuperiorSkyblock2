package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.MemberManageButton;
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

public class MenuMemberManage extends SuperiorMenu<MenuMemberManage> {

    private static RegularMenuPattern<MenuMemberManage> menuPattern;

    private MenuMemberManage(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        super(menuPattern, superiorPlayer);
        updateTargetPlayer(targetPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, targetPlayer);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{}", targetPlayer.getName());
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuMemberManage> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "member-manage.yml",
                MenuMemberManage::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "roles", menuPatternSlots), new MemberManageButton.Builder()
                        .setManageAction(MemberManageButton.ManageAction.SET_ROLE))
                .mapButtons(getSlots(cfg, "ban", menuPatternSlots), new MemberManageButton.Builder()
                        .setManageAction(MemberManageButton.ManageAction.BAN_MEMBER))
                .mapButtons(getSlots(cfg, "kick", menuPatternSlots), new MemberManageButton.Builder()
                        .setManageAction(MemberManageButton.ManageAction.KICK_MEMBER))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, SuperiorPlayer targetPlayer) {
        new MenuMemberManage(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer) {
        destroyMenus(MenuMemberManage.class, menuMemberManage -> menuMemberManage.targetPlayer.equals(targetPlayer));
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

        char rolesChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char banChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
        char kickChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

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
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
