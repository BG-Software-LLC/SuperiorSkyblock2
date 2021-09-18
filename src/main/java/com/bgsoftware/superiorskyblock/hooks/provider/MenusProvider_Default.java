package com.bgsoftware.superiorskyblock.hooks.provider;

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
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuBlank;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuCustom;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuSettings;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBankLogs;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBiomes;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBorderColor;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmLeave;
import com.bgsoftware.superiorskyblock.menu.impl.MenuControlPanel;
import com.bgsoftware.superiorskyblock.menu.impl.MenuCoops;
import com.bgsoftware.superiorskyblock.menu.impl.MenuCounts;
import com.bgsoftware.superiorskyblock.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandBank;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandChest;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandRate;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMemberManage;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMemberRole;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMembers;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMissions;
import com.bgsoftware.superiorskyblock.menu.impl.MenuPermissions;
import com.bgsoftware.superiorskyblock.menu.impl.MenuPlayerLanguage;
import com.bgsoftware.superiorskyblock.menu.impl.MenuPlayerMissions;
import com.bgsoftware.superiorskyblock.menu.impl.MenuSettings;
import com.bgsoftware.superiorskyblock.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.menu.impl.MenuUniqueVisitors;
import com.bgsoftware.superiorskyblock.menu.impl.MenuUpgrades;
import com.bgsoftware.superiorskyblock.menu.impl.MenuValues;
import com.bgsoftware.superiorskyblock.menu.impl.MenuVisitors;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpIconEdit;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class MenusProvider_Default implements MenusProvider {

    private final SuperiorSkyblockPlugin plugin;

    public MenusProvider_Default(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initializeMenus() {
        SuperiorMenuCustom.resetMenus();

        //Reload all menus
        handleExceptions(SuperiorMenuBlank::init);
        handleExceptions(SuperiorMenuSettings::init);

        handleExceptions(MenuBankLogs::init);
        handleExceptions(MenuBiomes::init);
        handleExceptions(MenuBorderColor::init);
        handleExceptions(MenuConfirmBan::init);
        handleExceptions(MenuConfirmDisband::init);
        handleExceptions(MenuConfirmKick::init);
        handleExceptions(MenuConfirmLeave::init);
        handleExceptions(MenuControlPanel::init);
        handleExceptions(MenuCoops::init);
        handleExceptions(MenuCounts::init);
        handleExceptions(MenuGlobalWarps::init);
        handleExceptions(MenuIslandBank::init);
        handleExceptions(MenuIslandChest::init);
        handleExceptions(MenuIslandCreation::init);
        handleExceptions(MenuIslandMissions::init);
        handleExceptions(MenuIslandRate::init);
        handleExceptions(MenuIslandRatings::init);
        handleExceptions(MenuMemberManage::init);
        handleExceptions(MenuMemberRole::init);
        handleExceptions(MenuMembers::init);
        handleExceptions(MenuMissions::init);
        handleExceptions(MenuPermissions::init);
        handleExceptions(MenuPlayerLanguage::init);
        handleExceptions(MenuPlayerMissions::init);
        handleExceptions(MenuSettings::init);
        handleExceptions(MenuTopIslands::init);
        handleExceptions(MenuUniqueVisitors::init);
        handleExceptions(MenuUpgrades::init);
        handleExceptions(MenuValues::init);
        handleExceptions(MenuVisitors::init);
        handleExceptions(MenuWarpCategories::init);
        handleExceptions(MenuWarpCategoryIconEdit::init);
        handleExceptions(MenuWarpCategoryManage::init);
        handleExceptions(MenuWarpIconEdit::init);
        handleExceptions(MenuWarpManage::init);
        handleExceptions(MenuWarps::init);

        File guiFolder = new File(plugin.getDataFolder(), "guis");
        if (guiFolder.exists()) {
            File oldGuisFolder = new File(plugin.getDataFolder(), "old-guis");
            //noinspection ResultOfMethodCallIgnored
            guiFolder.renameTo(oldGuisFolder);
        }

        File customMenusFolder = new File(plugin.getDataFolder(), "menus/custom");

        if (!customMenusFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            customMenusFolder.mkdirs();
            return;
        }

        File[] customMenuFiles = customMenusFolder.listFiles();
        if (customMenuFiles != null) {
            for (File menuFile : customMenuFiles) {
                handleExceptions(() -> SuperiorMenuCustom.createMenu(menuFile));
            }
        }
    }

    @Override
    public void openBankLogs(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuBankLogs.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshBankLogs(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuBankLogs.refreshMenus(island);
    }

    @Override
    public void openBiomes(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuBiomes.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openBorderColor(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuBorderColor.openInventory(targetPlayer, previousMenu);
    }

    @Override
    public void openConfirmBan(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer bannedPlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(bannedPlayer, "bannedPlayer parameter cannot be null.");
        MenuConfirmBan.openInventory(targetPlayer, previousMenu, targetIsland, bannedPlayer);
    }

    @Override
    public void openConfirmDisband(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuConfirmDisband.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openConfirmKick(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer kickedPlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(kickedPlayer, "kickedPlayer parameter cannot be null.");
        MenuConfirmKick.openInventory(targetPlayer, previousMenu, targetIsland, kickedPlayer);
    }

    @Override
    public void openConfirmLeave(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        MenuConfirmLeave.openInventory(targetPlayer, previousMenu);
    }

    @Override
    public void openControlPanel(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuControlPanel.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openCoops(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuCoops.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshCoops(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuCoops.refreshMenus(island);
    }

    @Override
    public void openCounts(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuCounts.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshCounts(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuCounts.refreshMenus(island);
    }

    @Override
    public void openGlobalWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuGlobalWarps.openInventory(targetPlayer, previousMenu);
    }

    @Override
    public void refreshGlobalWarps() {
        MenuGlobalWarps.refreshMenus();
    }

    @Override
    public void openIslandBank(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuIslandBank.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandBank(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuIslandBank.refreshMenus(island);
    }

    @Override
    public void openIslandChest(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuIslandChest.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandChest(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuIslandChest.refreshMenus(island);
    }

    @Override
    public void openIslandCreation(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, String islandName) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        MenuIslandCreation.openInventory(targetPlayer, previousMenu, islandName);
    }

    @Override
    public void openIslandRate(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuIslandRate.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void openIslandRatings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuIslandRatings.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshIslandRatings(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuIslandRatings.refreshMenus(island);
    }

    @Override
    public void openMemberManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        MenuMemberManage.openInventory(targetPlayer, previousMenu, islandMember);
    }

    @Override
    public void destroyMemberManage(SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        MenuMemberManage.destroyMenus(islandMember);
    }

    @Override
    public void openMemberRole(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        MenuMemberRole.openInventory(targetPlayer, previousMenu, islandMember);
    }

    @Override
    public void destroyMemberRole(SuperiorPlayer islandMember) {
        Preconditions.checkNotNull(islandMember, "islandMember parameter cannot be null.");
        MenuMemberRole.destroyMenus(islandMember);
    }

    @Override
    public void openMembers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuMembers.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshMembers(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuMembers.refreshMenus(island);
    }

    @Override
    public void openMissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuMissions.openInventory(targetPlayer, previousMenu);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer permissiblePlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(permissiblePlayer, "permissiblePlayer parameter cannot be null.");
        MenuPermissions.openInventory(targetPlayer, previousMenu, targetIsland, permissiblePlayer);
    }

    @Override
    public void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, PlayerRole permissibleRole) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        Preconditions.checkNotNull(permissibleRole, "permissibleRole parameter cannot be null.");
        MenuPermissions.openInventory(targetPlayer, previousMenu, targetIsland, permissibleRole);
    }

    @Override
    public void refreshPermissions(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuPermissions.refreshMenus(island);
    }

    @Override
    public void refreshPermissions(Island island, SuperiorPlayer permissiblePlayer) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(permissiblePlayer, "permissiblePlayer parameter cannot be null.");
        MenuPermissions.refreshMenus(island, permissiblePlayer);
    }

    @Override
    public void refreshPermissions(Island island, PlayerRole permissibleRole) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(permissibleRole, "permissibleRole parameter cannot be null.");
        MenuPermissions.refreshMenus(island, permissibleRole);
    }

    @Override
    public void updatePermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        MenuPermissions.updatePermission(islandPrivilege);
    }

    @Override
    public void openPlayerLanguage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuPlayerLanguage.openInventory(targetPlayer, previousMenu);
    }

    @Override
    public void openSettings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuSettings.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshSettings(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuSettings.refreshMenus(island);
    }

    @Override
    public void updateSettings(IslandFlag islandFlag) {
        Preconditions.checkNotNull(islandFlag, "islandFlag parameter cannot be null.");
        MenuSettings.updateSettings(islandFlag);
    }

    @Override
    public void openTopIslands(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SortingType sortingType) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        MenuTopIslands.openInventory(targetPlayer, previousMenu, sortingType);
    }

    @Override
    public void refreshTopIslands(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        MenuTopIslands.refreshMenus(sortingType);
    }

    @Override
    public void openUniqueVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuUniqueVisitors.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshUniqueVisitors(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuUniqueVisitors.refreshMenus(island);
    }

    @Override
    public void openUpgrades(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuUpgrades.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshUpgrades(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuUpgrades.refreshMenus(island);
    }

    @Override
    public void openValues(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuValues.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshValues(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuValues.refreshMenus(island);
    }

    @Override
    public void openVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuVisitors.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshVisitors(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuVisitors.refreshMenus(island);
    }

    @Override
    public void openWarpCategories(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetIsland, "targetIsland parameter cannot be null.");
        MenuWarpCategories.openInventory(targetPlayer, previousMenu, targetIsland);
    }

    @Override
    public void refreshWarpCategories(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuWarpCategories.refreshMenus(island);
    }

    @Override
    public void destroyWarpCategories(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuWarpCategories.destroyMenus(island);
    }

    @Override
    public void openWarpCategoryIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        MenuWarpCategoryIconEdit.openInventory(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void openWarpCategoryManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        MenuWarpCategoryManage.openInventory(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void refreshWarpCategoryManage(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        MenuWarpCategoryManage.refreshMenus(warpCategory);
    }

    @Override
    public void openWarpIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetWarp, "targetWarp parameter cannot be null.");
        MenuWarpIconEdit.openInventory(targetPlayer, previousMenu, targetWarp);
    }

    @Override
    public void openWarpManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetWarp, "targetWarp parameter cannot be null.");
        MenuWarpManage.openInventory(targetPlayer, previousMenu, targetWarp);
    }

    @Override
    public void refreshWarpManage(IslandWarp islandWarp) {
        Preconditions.checkNotNull(islandWarp, "islandWarp parameter cannot be null.");
        MenuWarpManage.refreshMenus(islandWarp);
    }

    @Override
    public void openWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetCategory, "targetCategory parameter cannot be null.");
        MenuWarps.openInventory(targetPlayer, previousMenu, targetCategory);
    }

    @Override
    public void refreshWarps(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        MenuWarps.refreshMenus(warpCategory);
    }

    @Override
    public void destroyWarps(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        MenuWarps.destroyMenus(warpCategory);
    }

    private static void handleExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            new HandlerLoadException(ex, HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
        }
    }

}
