package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBiomes;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBorderColor;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmLeave;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuControlPanel;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuCoops;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuCounts;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBank;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBannedPlayers;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandChest;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandFlags;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandMembers;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandPrivileges;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandRate;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandUniqueVisitors;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandUpgrades;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandValues;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandVisitors;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMemberManage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMemberRole;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMissions;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMissionsCategory;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuPlayerLanguage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuBlank;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuConfigEditor;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;

public class Menus {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static final MenuBlank MENU_BLANK = MenuBlank.createInstance();
    public static final MenuConfigEditor MENU_CONFIG_EDITOR = MenuConfigEditor.createInstance();

    public static MenuBankLogs MENU_BANK_LOGS;
    public static MenuBiomes MENU_BIOMES;
    public static MenuBorderColor MENU_BORDER_COLOR;
    public static MenuConfirmBan MENU_CONFIRM_BAN;
    public static MenuConfirmDisband MENU_CONFIRM_DISBAND;
    public static MenuConfirmKick MENU_CONFIRM_KICK;
    public static MenuConfirmLeave MENU_CONFIRM_LEAVE;
    public static MenuControlPanel MENU_CONTROL_PANEL;
    public static MenuCoops MENU_COOPS;
    public static MenuCounts MENU_COUNTS;
    public static MenuGlobalWarps MENU_GLOBAL_WARPS;
    public static MenuIslandBank MENU_ISLAND_BANK;
    public static MenuIslandBannedPlayers MENU_ISLAND_BANNED_PLAYERS;
    public static MenuIslandChest MENU_ISLAND_CHEST;
    public static MenuIslandCreation MENU_ISLAND_CREATION;
    public static MenuIslandFlags MENU_ISLAND_FLAGS;
    public static MenuIslandMembers MENU_ISLAND_MEMBERS;
    public static MenuIslandPrivileges MENU_ISLAND_PRIVILEGES;
    public static MenuIslandRate MENU_ISLAND_RATE;
    public static MenuIslandRatings MENU_ISLAND_RATINGS;
    public static MenuIslandUniqueVisitors MENU_ISLAND_UNIQUE_VISITORS;
    public static MenuIslandUpgrades MENU_ISLAND_UPGRADES;
    public static MenuIslandValues MENU_ISLAND_VALUES;
    public static MenuIslandVisitors MENU_ISLAND_VISITORS;
    public static MenuMemberManage MENU_MEMBER_MANAGE;
    public static MenuMemberRole MENU_MEMBER_ROLE;
    public static MenuMissions MENU_MISSIONS;
    public static MenuMissionsCategory MENU_MISSIONS_CATEGORY;
    public static MenuPlayerLanguage MENU_PLAYER_LANGUAGE;
    public static MenuTopIslands MENU_TOP_ISLANDS;
    public static MenuWarpCategories MENU_WARP_CATEGORIES;
    public static MenuWarpCategoryIconEdit MENU_WARP_CATEGORY_ICON_EDIT;
    public static MenuWarpCategoryManage MENU_WARP_CATEGORY_MANAGE;
    public static MenuWarpIconEdit MENU_WARP_ICON_EDIT;
    public static MenuWarpManage MENU_WARP_MANAGE;
    public static MenuWarps MENU_WARPS;

    private Menus() {

    }

    public static void registerMenus() {
        // We register the internal menus
        createMenu(MENU_BLANK);
        createMenu(MENU_CONFIG_EDITOR);
        // Load menus from files
        MENU_BANK_LOGS = createMenu(MenuBankLogs.createInstance());
        MENU_BIOMES = createMenu(MenuBiomes.createInstance());
        MENU_BORDER_COLOR = createMenu(MenuBorderColor.createInstance());
        MENU_CONFIRM_BAN = createMenu(MenuConfirmBan.createInstance());
        MENU_CONFIRM_DISBAND = createMenu(MenuConfirmDisband.createInstance());
        MENU_CONFIRM_KICK = createMenu(MenuConfirmKick.createInstance());
        MENU_CONFIRM_LEAVE = createMenu(MenuConfirmLeave.createInstance());
        MENU_CONTROL_PANEL = createMenu(MenuControlPanel.createInstance());
        MENU_COOPS = createMenu(MenuCoops.createInstance());
        MENU_COUNTS = createMenu(MenuCounts.createInstance());
        MENU_GLOBAL_WARPS = createMenu(MenuGlobalWarps.createInstance());
        MENU_ISLAND_BANK = createMenu(MenuIslandBank.createInstance());
        MENU_ISLAND_BANNED_PLAYERS = createMenu(MenuIslandBannedPlayers.createInstance());
        MENU_ISLAND_CHEST = createMenu(MenuIslandChest.createInstance());
        MENU_ISLAND_CREATION = createMenu(MenuIslandCreation.createInstance());
        MENU_ISLAND_FLAGS = createMenu(MenuIslandFlags.createInstance());
        MENU_ISLAND_MEMBERS = createMenu(MenuIslandMembers.createInstance());
        MENU_ISLAND_PRIVILEGES = createMenu(MenuIslandPrivileges.createInstance());
        MENU_ISLAND_RATE = createMenu(MenuIslandRate.createInstance());
        MENU_ISLAND_RATINGS = createMenu(MenuIslandRatings.createInstance());
        MENU_ISLAND_UNIQUE_VISITORS = createMenu(MenuIslandUniqueVisitors.createInstance());
        MENU_ISLAND_UPGRADES = createMenu(MenuIslandUpgrades.createInstance());
        MENU_ISLAND_VALUES = createMenu(MenuIslandValues.createInstance());
        MENU_ISLAND_VISITORS = createMenu(MenuIslandVisitors.createInstance());
        MENU_MEMBER_MANAGE = createMenu(MenuMemberManage.createInstance());
        MENU_MEMBER_ROLE = createMenu(MenuMemberRole.createInstance());
        MENU_MISSIONS = createMenu(MenuMissions.createInstance());
        MENU_MISSIONS_CATEGORY = createMenu(MenuMissionsCategory.createInstance());
        MENU_PLAYER_LANGUAGE = createMenu(MenuPlayerLanguage.createInstance());
        MENU_TOP_ISLANDS = createMenu(MenuTopIslands.createInstance());
        MENU_WARP_CATEGORIES = createMenu(MenuWarpCategories.createInstance());
        MENU_WARP_CATEGORY_ICON_EDIT = createMenu(MenuWarpCategoryIconEdit.createInstance());
        MENU_WARP_CATEGORY_MANAGE = createMenu(MenuWarpCategoryManage.createInstance());
        MENU_WARP_ICON_EDIT = createMenu(MenuWarpIconEdit.createInstance());
        MENU_WARP_MANAGE = createMenu(MenuWarpManage.createInstance());
        MENU_WARPS = createMenu(MenuWarps.createInstance());
    }

    private static <M extends AbstractMenu<V, A>, V extends AbstractMenuView<V, A>, A extends ViewArgs> M createMenu(M menu) {
        if (menu == null)
            throw new IllegalStateException("Menu could not be initialized.");

        plugin.getMenus().registerMenu(menu);
        return menu;
    }

}
