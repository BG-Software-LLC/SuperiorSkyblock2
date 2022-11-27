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

import javax.annotation.Nullable;

public class Menus {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    public static final MenuBankLogs MENU_BANK_LOGS = createMenu(MenuBankLogs.createInstance());
    public static final MenuBiomes MENU_BIOMES = createMenu(MenuBiomes.createInstance());
    public static final MenuBlank MENU_BLANK = createMenu(MenuBlank.createInstance());
    public static final MenuBorderColor MENU_BORDER_COLOR = createMenu(MenuBorderColor.createInstance());
    public static final MenuConfirmBan MENU_CONFIRM_BAN = createMenu(MenuConfirmBan.createInstance());
    public static final MenuConfigEditor MENU_CONFIG_EDITOR = createMenu(MenuConfigEditor.createInstance());
    public static final MenuConfirmDisband MENU_CONFIRM_DISBAND = createMenu(MenuConfirmDisband.createInstance());
    public static final MenuConfirmKick MENU_CONFIRM_KICK = createMenu(MenuConfirmKick.createInstance());
    public static final MenuConfirmLeave MENU_CONFIRM_LEAVE = createMenu(MenuConfirmLeave.createInstance());
    public static final MenuControlPanel MENU_CONTROL_PANEL = createMenu(MenuControlPanel.createInstance());
    public static final MenuCoops MENU_COOPS = createMenu(MenuCoops.createInstance());
    public static final MenuCounts MENU_COUNTS = createMenu(MenuCounts.createInstance());
    public static final MenuGlobalWarps MENU_GLOBAL_WARPS = createMenu(MenuGlobalWarps.createInstance());
    public static final MenuIslandBank MENU_ISLAND_BANK = createMenu(MenuIslandBank.createInstance());
    public static final MenuIslandChest MENU_ISLAND_CHEST = createMenu(MenuIslandChest.createInstance());
    public static final MenuIslandCreation MENU_ISLAND_CREATION = createMenu(MenuIslandCreation.createInstance());
    public static final MenuIslandFlags MENU_ISLAND_FLAGS = createMenu(MenuIslandFlags.createInstance());
    public static final MenuIslandMembers MENU_ISLAND_MEMBERS = createMenu(MenuIslandMembers.createInstance());
    public static final MenuIslandPrivileges MENU_ISLAND_PRIVILEGES = createMenu(MenuIslandPrivileges.createInstance());
    public static final MenuIslandRate MENU_ISLAND_RATE = createMenu(MenuIslandRate.createInstance());
    public static final MenuIslandRatings MENU_ISLAND_RATINGS = createMenu(MenuIslandRatings.createInstance());
    public static final MenuIslandUniqueVisitors MENU_ISLAND_UNIQUE_VISITORS = createMenu(MenuIslandUniqueVisitors.createInstance());
    public static final MenuIslandUpgrades MENU_ISLAND_UPGRADES = createMenu(MenuIslandUpgrades.createInstance());
    public static final MenuIslandValues MENU_ISLAND_VALUES = createMenu(MenuIslandValues.createInstance());
    public static final MenuIslandVisitors MENU_ISLAND_VISITORS = createMenu(MenuIslandVisitors.createInstance());
    public static final MenuMemberManage MENU_MEMBER_MANAGE = createMenu(MenuMemberManage.createInstance());
    public static final MenuMemberRole MENU_MEMBER_ROLE = createMenu(MenuMemberRole.createInstance());
    public static final MenuMissions MENU_MISSIONS = createMenu(MenuMissions.createInstance());
    public static final MenuMissionsCategory MENU_MISSIONS_CATEGORY = createMenu(MenuMissionsCategory.createInstance());
    public static final MenuPlayerLanguage MENU_PLAYER_LANGUAGE = createMenu(MenuPlayerLanguage.createInstance());
    public static final MenuTopIslands MENU_TOP_ISLANDS = createMenu(MenuTopIslands.createInstance());
    public static final MenuWarpCategories MENU_WARP_CATEGORIES = createMenu(MenuWarpCategories.createInstance());
    public static final MenuWarpCategoryIconEdit MENU_WARP_CATEGORY_ICON_EDIT = createMenu(MenuWarpCategoryIconEdit.createInstance());
    public static final MenuWarpCategoryManage MENU_WARP_CATEGORY_MANAGE = createMenu(MenuWarpCategoryManage.createInstance());
    public static final MenuWarpIconEdit MENU_WARP_ICON_EDIT = createMenu(MenuWarpIconEdit.createInstance());
    public static final MenuWarpManage MENU_WARP_MANAGE = createMenu(MenuWarpManage.createInstance());
    public static final MenuWarps MENU_WARPS = createMenu(MenuWarps.createInstance());

    private Menus() {

    }

    private static <M extends AbstractMenu<V, A>, V extends AbstractMenuView<V, A>, A extends ViewArgs> M createMenu(@Nullable M menu) {
        if (menu == null)
            throw new IllegalStateException("Menu could not be initialized.");

        plugin.getMenus().registerMenu(menu);
        return menu;
    }

}
