package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.MemberRoleButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.PlayerMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.PlayerViewArgs;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public class MenuMemberRole extends AbstractMenu<MenuMemberRole.View, PlayerViewArgs> {

    private MenuMemberRole(MenuParseResult<MenuMemberRole.View> parseResult) {
        super(MenuIdentifiers.MENU_MEMBER_ROLE, parseResult);
    }

    @Override
    protected MenuMemberRole.View createViewInternal(SuperiorPlayer superiorPlayer, PlayerViewArgs args,
                                                     @Nullable MenuView<?, ?> previousMenuView) {
        return new MenuMemberRole.View(superiorPlayer, previousMenuView, this, args);
    }

    public void closeViews(SuperiorPlayer islandMember) {
        closeViews(view -> view.getSuperiorPlayer().equals(islandMember));
    }

    @Nullable
    public static MenuMemberRole createInstance() {
        MenuParseResult<MenuMemberRole.View> menuParseResult = MenuParserImpl.getInstance().loadMenu("member-role.yml",
                MenuMemberRole::convertOldGUI);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<MenuMemberRole.View> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                Object roleObject = itemsSection.get("role");

                PlayerRole playerRole = null;

                if (roleObject instanceof String) {
                    try {
                        playerRole = SPlayerRole.of((String) roleObject);
                    } catch (IllegalArgumentException error) {
                        Log.warnFromFile("member-role.yml", "Invalid role name: ", roleObject);
                        continue;
                    }
                } else if (roleObject instanceof Integer) {
                    playerRole = SPlayerRole.of((Integer) roleObject);
                    if (playerRole == null) {
                        Log.warnFromFile("member-role.yml", "&cInvalid role id: ", roleObject);
                        continue;
                    }
                }

                if (playerRole == null)
                    continue;

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemsSectionName),
                        new MemberRoleButton.Builder().setPlayerRole(playerRole));
            }
        }

        return new MenuMemberRole(menuParseResult);
    }

    public static class View extends AbstractMenuView<MenuMemberRole.View, PlayerViewArgs> {

        private final SuperiorPlayer superiorPlayer;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             MenuMemberRole menu, PlayerViewArgs args) {
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

        newMenu.set("title", cfg.getString("roles-panel.title"));

        int size = cfg.getInt("roles-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("roles-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("roles-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("roles-panel.roles")) {
            for (String roleName : cfg.getConfigurationSection("roles-panel.roles").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("roles-panel.roles." + roleName);
                char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
                section.set("role", Formatters.CAPITALIZED_FORMATTER.format(roleName));
                MenuConverter.convertItem(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
