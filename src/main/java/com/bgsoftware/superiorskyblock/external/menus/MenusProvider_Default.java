package com.bgsoftware.superiorskyblock.external.menus;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.MenusProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandPrivileges;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuMissionsCategory;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuCustom;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import com.bgsoftware.superiorskyblock.core.menu.view.args.PlayerViewArgs;
import com.google.common.base.Preconditions;

import java.io.File;

public class MenusProvider_Default implements MenusProvider {

    private final SuperiorSkyblockPlugin plugin;

    public MenusProvider_Default(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    private static void handleExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            ManagerLoadException handlerError = new ManagerLoadException(ex, ManagerLoadException.ErrorLevel.CONTINUE);
            Log.error(handlerError, "An unexpected error occurred while loading menu:");
        }
    }

    @Override
    public void initializeMenus() {
        File guiFolder = new File(plugin.getDataFolder(), "guis");
        if (guiFolder.exists()) {
            File oldGuisFolder = new File(plugin.getDataFolder(), "old-guis");
            //noinspection ResultOfMethodCallIgnored
            guiFolder.renameTo(oldGuisFolder);
        }

        // We first want to unregister all menus
        plugin.getMenus().unregisterMenus();

        Menus.registerMenus();

        File customMenusFolder = new File(plugin.getDataFolder(), "menus/custom");

        if (!customMenusFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            customMenusFolder.mkdirs();
            return;
        }

        File[] customMenuFiles = customMenusFolder.listFiles();
        if (customMenuFiles != null) {
            for (File menuFile : customMenuFiles) {
                handleExceptions(() -> plugin.getMenus().registerMenu(MenuCustom.createInstance(menuFile)));
            }
        }
    }

    @Override
    public void openBankLogs(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_BANK_LOGS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshBankLogs(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_BANK_LOGS.refreshViews(island);
    }

    @Override
    public void openBiomes(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_BIOMES.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void openBorderColor(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Menus.MENU_BORDER_COLOR.createView(targetPlayer, EmptyViewArgs.INSTANCE, previousMenu);
    }

    @Override
    public void openConfirmBan(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer bannedPlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(bannedPlayer, "bannedPlayer parameter cannot be null.");
        Menus.MENU_CONFIRM_BAN.createView(targetPlayer, new MenuConfirmBan.Args(targetIsland, bannedPlayer), previousMenu);
    }

    @Override
    public void openConfirmDisband(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_CONFIRM_DISBAND.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void openConfirmKick(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer kickedPlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(kickedPlayer, "kickedPlayer parameter cannot be null.");
        Menus.MENU_CONFIRM_KICK.createView(targetPlayer, new MenuConfirmKick.Args(targetIsland, kickedPlayer), previousMenu);
    }

    @Override
    public void openConfirmLeave(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Menus.MENU_CONFIRM_LEAVE.createView(targetPlayer, EmptyViewArgs.INSTANCE, previousMenu);
    }

    @Override
    public void openControlPanel(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_CONTROL_PANEL.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void openCoops(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_COOPS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshCoops(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_COOPS.refreshViews(island);
    }

    @Override
    public void openCounts(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_COUNTS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshCounts(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_COUNTS.refreshViews(island);
    }

    @Override
    public void openGlobalWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Menus.MENU_GLOBAL_WARPS.createView(targetPlayer, EmptyViewArgs.INSTANCE, previousMenu);
    }

    @Override
    public void refreshGlobalWarps() {
        Menus.MENU_GLOBAL_WARPS.refreshViews();
    }

    @Override
    public void openIslandBank(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_BANK.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshIslandBank(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_BANK.refreshViews(island);
    }

    @Override
    public void openIslandBannedPlayers(SuperiorPlayer targetPlayer, ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_BANNED_PLAYERS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshIslandBannedPlayers(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_BANNED_PLAYERS.refreshViews(island);
    }

    @Override
    public void openIslandChest(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_CHEST.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshIslandChest(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_CHEST.refreshViews(island);
    }

    @Override
    public void openIslandCreation(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, String islandName) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        Menus.MENU_ISLAND_CREATION.createView(targetPlayer, new MenuIslandCreation.Args(islandName), previousMenu);
    }

    @Override
    public void openIslandRate(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_RATE.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void openIslandRatings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_RATINGS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshIslandRatings(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_RATINGS.refreshViews(island);
    }

    @Override
    public void openMemberManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        Menus.MENU_MEMBER_MANAGE.createView(targetPlayer, new PlayerViewArgs(islandMember), previousMenu);
    }

    @Override
    public void destroyMemberManage(SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        Menus.MENU_MEMBER_MANAGE.closeViews(islandMember);
    }

    @Override
    public void openMemberRole(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        Menus.MENU_MEMBER_ROLE.createView(targetPlayer, new PlayerViewArgs(islandMember), previousMenu);
    }

    @Override
    public void destroyMemberRole(SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        Menus.MENU_MEMBER_ROLE.closeViews(islandMember);
    }

    @Override
    public void openMembers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_MEMBERS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshMembers(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_MEMBERS.refreshViews(island);
    }

    @Override
    public void openMissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Menus.MENU_MISSIONS.createView(targetPlayer, EmptyViewArgs.INSTANCE, previousMenu);
    }

    @Override
    public void openMissionsCategory(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, MissionCategory missionCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(missionCategory, "missionCategory parameter cannot be null.");
        Menus.MENU_MISSIONS_CATEGORY.createView(targetPlayer, new MenuMissionsCategory.Args(missionCategory), previousMenu);
    }

    @Override
    public void refreshMissionsCategory(MissionCategory missionCategory) {
        Preconditions.checkNotNull(missionCategory, "missionCategory parameter cannot be null.");
        Menus.MENU_MISSIONS_CATEGORY.refreshViews(missionCategory);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer permissiblePlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(permissiblePlayer, "permissiblePlayer parameter cannot be null.");
        Menus.MENU_ISLAND_PRIVILEGES.createView(targetPlayer, new MenuIslandPrivileges.Args(targetIsland, permissiblePlayer), previousMenu);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, PlayerRole permissibleRole) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(permissibleRole, "permissibleRole parameter cannot be null.");
        Menus.MENU_ISLAND_PRIVILEGES.createView(targetPlayer, new MenuIslandPrivileges.Args(targetIsland, permissibleRole), previousMenu);
    }

    @Override
    public void refreshPermissions(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_PRIVILEGES.refreshViews(island);
    }

    @Override
    public void refreshPermissions(Island island, SuperiorPlayer permissiblePlayer) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(permissiblePlayer, "permissiblePlayer parameter cannot be null.");
        Menus.MENU_ISLAND_PRIVILEGES.refreshViews(island, permissiblePlayer);
    }

    @Override
    public void refreshPermissions(Island island, PlayerRole permissibleRole) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(permissibleRole, "permissibleRole parameter cannot be null.");
        Menus.MENU_ISLAND_PRIVILEGES.refreshViews(island, permissibleRole);
    }

    @Override
    public void updatePermission(IslandPrivilege islandPrivilege) {
        // The default implementation does not care if the island privilege is valid for showing the island
        // privileges in the menu. If the island privilege is not valid at the time of opening the menu, it
        // will show it as it was disabled. This is the responsibility of the server owners to properly
        // configure the menu.
    }

    @Override
    public void openPlayerLanguage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Menus.MENU_PLAYER_LANGUAGE.createView(targetPlayer, EmptyViewArgs.INSTANCE, previousMenu);
    }

    @Override
    public void openSettings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_FLAGS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshSettings(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_FLAGS.refreshViews(island);
    }

    @Override
    public void updateSettings(IslandFlag islandFlag) {
        // The default implementation does not care if the island flag is valid for showing the island flags
        // in the menu. If the island flag is not valid at the time of opening the menu, it will show it as
        // it was disabled. This is the responsibility of the server owners to properly configure the menu.
    }

    @Override
    public void openTopIslands(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SortingType sortingType) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        Menus.MENU_TOP_ISLANDS.createView(targetPlayer, new MenuTopIslands.Args(sortingType), previousMenu);
    }

    @Override
    public void refreshTopIslands(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        Menus.MENU_TOP_ISLANDS.refreshViews(sortingType);
    }

    @Override
    public void openUniqueVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_UNIQUE_VISITORS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshUniqueVisitors(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_UNIQUE_VISITORS.refreshViews(island);
    }

    @Override
    public void openUpgrades(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_UPGRADES.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshUpgrades(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_UPGRADES.refreshViews(island);
    }

    @Override
    public void openValues(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_VALUES.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshValues(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_VALUES.refreshViews(island);
    }

    @Override
    public void openVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_ISLAND_VISITORS.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshVisitors(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_ISLAND_VISITORS.refreshViews(island);
    }

    @Override
    public void openWarpCategories(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Menus.MENU_WARP_CATEGORIES.createView(targetPlayer, new IslandViewArgs(targetIsland), previousMenu);
    }

    @Override
    public void refreshWarpCategories(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_WARP_CATEGORIES.refreshViews(island);
    }

    @Override
    public void destroyWarpCategories(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Menus.MENU_WARP_CATEGORIES.closeViews(island);
    }

    @Override
    public void openWarpCategoryIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        Menus.MENU_WARP_CATEGORY_ICON_EDIT.createView(targetPlayer, new MenuWarpCategoryIconEdit.Args(targetCategory), previousMenu);
    }

    @Override
    public void openWarpCategoryManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        Menus.MENU_WARP_CATEGORY_MANAGE.createView(targetPlayer, new MenuWarpCategoryManage.Args(targetCategory), previousMenu);
    }

    @Override
    public void refreshWarpCategoryManage(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        Menus.MENU_WARP_CATEGORY_MANAGE.refreshViews(warpCategory);
    }

    @Override
    public void openWarpIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetWarp, "targetWarp parameter cannot be null.");
        Menus.MENU_WARP_ICON_EDIT.createView(targetPlayer, new MenuWarpIconEdit.Args(targetWarp), previousMenu);
    }

    @Override
    public void openWarpManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetWarp, "targetWarp parameter cannot be null.");
        Menus.MENU_WARP_MANAGE.createView(targetPlayer, new MenuWarpManage.Args(targetWarp), previousMenu);
    }

    @Override
    public void refreshWarpManage(IslandWarp islandWarp) {
        Preconditions.checkNotNull(islandWarp, "islandWarp parameter cannot be null.");
        Menus.MENU_WARP_MANAGE.refreshViews(islandWarp);
    }

    @Override
    public void openWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        Menus.MENU_WARPS.createView(targetPlayer, new MenuWarps.Args(targetCategory), previousMenu);
    }

    @Override
    public void refreshWarps(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        Menus.MENU_WARPS.refreshViews(warpCategory);
    }

    @Override
    public void destroyWarps(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        Menus.MENU_WARPS.closeViews(warpCategory);
    }

}
