package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.MemberRoleButton;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;

public final class MenuMemberRole extends SuperiorMenu<MenuMemberRole> {

    private static RegularMenuPattern<MenuMemberRole> menuPattern;

    private MenuMemberRole(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        super(menuPattern, superiorPlayer);
        updateTargetPlayer(targetPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetPlayer);
    }

    @Override
    protected String replaceTitle(String title) {
        return PlaceholderHook.parse(targetPlayer, title.replace("{}", targetPlayer.getName()));
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuMemberRole> patternBuilder = new RegularMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "member-role.yml", MenuMemberRole::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                Object roleObject = itemsSection.get("role");

                PlayerRole playerRole = null;

                if (roleObject instanceof String) {
                    try {
                        playerRole = SPlayerRole.of((String) roleObject);
                    } catch (IllegalArgumentException error) {
                        SuperiorSkyblockPlugin.log("&cInvalid role name in members-role menu: " + roleObject);
                        continue;
                    }
                } else if (roleObject instanceof Integer) {
                    playerRole = SPlayerRole.of((Integer) roleObject);
                    if (playerRole == null) {
                        SuperiorSkyblockPlugin.log("&cInvalid role id in members-role menu: " + roleObject);
                        continue;
                    }
                }

                if (playerRole == null)
                    continue;

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemsSectionName), new MemberRoleButton.Builder()
                        .setPlayerRole(playerRole));
            }
        }

        menuPattern = patternBuilder.build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, SuperiorPlayer targetPlayer) {
        new MenuMemberRole(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer) {
        destroyMenus(MenuMemberRole.class, menuMemberRole -> menuMemberRole.targetPlayer.equals(targetPlayer));
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
                char itemChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
                section.set("role", StringUtils.format(roleName));
                MenuConverter.convertItem(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
