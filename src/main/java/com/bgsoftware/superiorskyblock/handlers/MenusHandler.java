package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.*;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.google.common.base.Preconditions;

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
        loadMenu(MenuConfirmLeave::init);
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
        loadMenu(MenuWarpCategories::init);
        loadMenu(MenuWarpCategoryIconEdit::init);
        loadMenu(MenuWarpCategoryManage::init);
        loadMenu(MenuWarpIconEdit::init);
        loadMenu(MenuWarpManage::init);
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
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuBorderColor.openInventory(superiorPlayer, null);
    }

    @Override
    public void openConfirmDisbandMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuConfirmDisband.openInventory(superiorPlayer, null);
    }

    @Override
    public void openGlobalWarpsMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuGlobalWarps.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandBiomesMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuBiomes.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandCreationMenu(SuperiorPlayer superiorPlayer, String islandName) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        MenuIslandCreation.openInventory(superiorPlayer, null, islandName);
    }

    @Override
    public void openIslandCountsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuCounts.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandMainMissionsMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuMissions.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandMembersMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuMembers.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandMissionsMenu(SuperiorPlayer superiorPlayer, boolean islandMissions) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if(islandMissions)
            MenuIslandMissions.openInventory(superiorPlayer, null);
        else
            MenuPlayerMissions.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandPanelMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuControlPanel.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        MenuPermissions.openInventory(superiorPlayer, null, island, playerRole);
    }

    @Override
    public void updatePermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        MenuPermissions.updatePermission(islandPrivilege);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuPermissions.openInventory(superiorPlayer, null, island, targetPlayer);
    }

    @Override
    public void openPlayerLanguageMenu(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        MenuPlayerLanguage.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandRateMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuIslandRate.openInventory(superiorPlayer, island, null);
    }

    @Override
    public void openIslandRatingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuIslandRatings.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandSettingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuSettings.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void updateSettings(IslandFlag islandFlag) {
        Preconditions.checkNotNull(islandFlag, "islandFlag parameter cannot be null.");
        MenuSettings.updateSettings(islandFlag);
    }

    @Override
    public void openIslandsTopMenu(SuperiorPlayer superiorPlayer, SortingType sortingType) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        MenuTopIslands.openInventory(superiorPlayer, null, sortingType);
    }

    @Override
    public void openUniqueVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuUniqueVisitors.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandUpgradeMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuUpgrades.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandValuesMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuValues.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuVisitors.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandWarpsMenu(SuperiorPlayer superiorPlayer, Island island) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        MenuWarps.openInventory(superiorPlayer, null, island.getWarpCategories().values()
                .stream().findFirst().orElseGet(() -> island.createWarpCategory("Default Category")));
    }

    @Override
    public void openMemberManageMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
        MenuMemberManage.openInventory(superiorPlayer, null, targetPlayer);
    }

    @Override
    public void openMemberRoleMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        Preconditions.checkNotNull(targetPlayer, "targetPlayer parameter cannot be null.");
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
