package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuBankLogs;
import com.bgsoftware.superiorskyblock.menu.MenuBiomes;
import com.bgsoftware.superiorskyblock.menu.MenuBorderColor;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.menu.MenuControlPanel;
import com.bgsoftware.superiorskyblock.menu.MenuCoops;
import com.bgsoftware.superiorskyblock.menu.MenuCounts;
import com.bgsoftware.superiorskyblock.menu.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.menu.MenuIslandBank;
import com.bgsoftware.superiorskyblock.menu.MenuIslandChest;
import com.bgsoftware.superiorskyblock.menu.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.menu.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.MenuIslandRate;
import com.bgsoftware.superiorskyblock.menu.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.menu.MenuMemberManage;
import com.bgsoftware.superiorskyblock.menu.MenuMemberRole;
import com.bgsoftware.superiorskyblock.menu.MenuMembers;
import com.bgsoftware.superiorskyblock.menu.MenuMissions;
import com.bgsoftware.superiorskyblock.menu.MenuPermissions;
import com.bgsoftware.superiorskyblock.menu.MenuPlayerLanguage;
import com.bgsoftware.superiorskyblock.menu.MenuPlayerMissions;
import com.bgsoftware.superiorskyblock.menu.MenuSettings;
import com.bgsoftware.superiorskyblock.menu.MenuTopIslands;
import com.bgsoftware.superiorskyblock.menu.MenuUniqueVisitors;
import com.bgsoftware.superiorskyblock.menu.MenuUpgrades;
import com.bgsoftware.superiorskyblock.menu.MenuValues;
import com.bgsoftware.superiorskyblock.menu.MenuVisitors;
import com.bgsoftware.superiorskyblock.menu.MenuWarps;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuBlank;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuCustom;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuSettings;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;

import java.io.File;

public final class MenusHandler extends AbstractHandler implements MenusManager {

    public MenusHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData(){
        SuperiorMenuCustom.resetMenus();

        //Reload all menus
        loadMenu(SuperiorMenuBlank::init);
        loadMenu(SuperiorMenuSettings::init);

        loadMenu(MenuBankLogs::init);
        loadMenu(MenuBiomes::init);
        loadMenu(MenuBorderColor::init);
        loadMenu(MenuConfirmBan::init);
        loadMenu(MenuConfirmDisband::init);
        loadMenu(MenuConfirmKick::init);
        loadMenu(MenuControlPanel::init);
        loadMenu(MenuCoops::init);
        loadMenu(MenuCounts::init);
        loadMenu(MenuGlobalWarps::init);
        loadMenu(MenuIslandBank::init);
        loadMenu(MenuIslandChest::init);
        loadMenu(MenuIslandCreation::init);
        loadMenu(MenuIslandMissions::init);
        loadMenu(MenuIslandRate::init);
        loadMenu(MenuIslandRatings::init);
        loadMenu(MenuMemberManage::init);
        loadMenu(MenuMemberRole::init);
        loadMenu(MenuMembers::init);
        loadMenu(MenuMissions::init);
        loadMenu(MenuPermissions::init);
        loadMenu(MenuPlayerLanguage::init);
        loadMenu(MenuPlayerMissions::init);
        loadMenu(MenuSettings::init);
        loadMenu(MenuTopIslands::init);
        loadMenu(MenuUniqueVisitors::init);
        loadMenu(MenuUpgrades::init);
        loadMenu(MenuValues::init);
        loadMenu(MenuVisitors::init);
        loadMenu(MenuWarps::init);

        File guiFolder = new File(plugin.getDataFolder(), "guis");
        if(guiFolder.exists()){
            File oldGuisFolder = new File(plugin.getDataFolder(), "old-guis");
            //noinspection ResultOfMethodCallIgnored
            guiFolder.renameTo(oldGuisFolder);
        }

        File customMenusFolder = new File(plugin.getDataFolder(), "menus/custom");

        if(!customMenusFolder.exists()){
            //noinspection ResultOfMethodCallIgnored
            customMenusFolder.mkdirs();
            return;
        }

        //noinspection ConstantConditions
        for (File menuFile : customMenusFolder.listFiles()) {
            try{
                SuperiorMenuCustom.createMenu(menuFile);
            }catch(Exception ex){
                new HandlerLoadException(ex, HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
            }
        }
    }

    @Override
    public void openBorderColorMenu(SuperiorPlayer superiorPlayer) {
        MenuBorderColor.openInventory(superiorPlayer, null);
    }

    @Override
    public void openConfirmDisbandMenu(SuperiorPlayer superiorPlayer) {
        MenuConfirmDisband.openInventory(superiorPlayer, null);
    }

    @Override
    public void openGlobalWarpsMenu(SuperiorPlayer superiorPlayer) {
        MenuGlobalWarps.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandBiomesMenu(SuperiorPlayer superiorPlayer) {
        MenuBiomes.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandCreationMenu(SuperiorPlayer superiorPlayer, String islandName) {
        MenuIslandCreation.openInventory(superiorPlayer, null, islandName);
    }

    @Override
    public void openIslandCountsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuCounts.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandMainMissionsMenu(SuperiorPlayer superiorPlayer) {
        MenuMissions.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandMembersMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuMembers.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandMissionsMenu(SuperiorPlayer superiorPlayer, boolean islandMissions) {
        if(islandMissions)
            MenuIslandMissions.openInventory(superiorPlayer, null);
        else
            MenuPlayerMissions.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandPanelMenu(SuperiorPlayer superiorPlayer) {
        MenuControlPanel.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole) {
        MenuPermissions.openInventory(superiorPlayer, null, island, playerRole);
    }

    @Override
    public void updatePermission(IslandPrivilege islandPrivilege) {
        MenuPermissions.updatePermission(islandPrivilege);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        MenuPermissions.openInventory(superiorPlayer, null, island, targetPlayer);
    }

    @Override
    public void openPlayerLanguageMenu(SuperiorPlayer superiorPlayer) {
        MenuPlayerLanguage.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandRateMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuIslandRate.openInventory(superiorPlayer, island, null);
    }

    @Override
    public void openIslandRatingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuIslandRatings.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandSettingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuSettings.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void updateSettings(IslandFlag islandFlag) {
        MenuSettings.updateSettings(islandFlag);
    }

    @Override
    public void openIslandsTopMenu(SuperiorPlayer superiorPlayer, SortingType sortingType) {
        MenuTopIslands.openInventory(superiorPlayer, null, sortingType);
    }

    @Override
    public void openUniqueVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuUniqueVisitors.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandUpgradeMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuUpgrades.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandValuesMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuValues.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuVisitors.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandWarpsMenu(SuperiorPlayer superiorPlayer, Island island) {
        MenuWarps.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openMemberManageMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        MenuMemberManage.openInventory(superiorPlayer, null, targetPlayer);
    }

    @Override
    public void openMemberRoleMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        MenuMemberRole.openInventory(superiorPlayer, null, targetPlayer);
    }

    private void loadMenu(Runnable runnable){
        try{
            runnable.run();
        }catch(Exception ex){
            new HandlerLoadException(ex, HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
        }
    }

}
