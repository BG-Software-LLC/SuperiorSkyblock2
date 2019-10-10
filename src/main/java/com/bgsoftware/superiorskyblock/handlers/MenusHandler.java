package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.BorderColorMenu;
import com.bgsoftware.superiorskyblock.menu.ConfirmDisbandMenu;
import com.bgsoftware.superiorskyblock.menu.GlobalWarpsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandBiomesMenu;
import com.bgsoftware.superiorskyblock.menu.IslandCreationMenu;
import com.bgsoftware.superiorskyblock.menu.IslandMainMissionsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandMembersMenu;
import com.bgsoftware.superiorskyblock.menu.IslandMissionsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandPanelMenu;
import com.bgsoftware.superiorskyblock.menu.IslandPermissionsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandRateMenu;
import com.bgsoftware.superiorskyblock.menu.IslandRatingsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandUpgradesMenu;
import com.bgsoftware.superiorskyblock.menu.IslandValuesMenu;
import com.bgsoftware.superiorskyblock.menu.IslandVisitorsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandWarpsMenu;
import com.bgsoftware.superiorskyblock.menu.IslandsTopMenu;
import com.bgsoftware.superiorskyblock.menu.MemberManageMenu;
import com.bgsoftware.superiorskyblock.menu.MemberRoleMenu;

public final class MenusHandler implements MenusManager {

    public MenusHandler(){
        //Reload all menus
        BorderColorMenu.init();
        ConfirmDisbandMenu.init();
        GlobalWarpsMenu.init();
        IslandBiomesMenu.init();
        IslandCreationMenu.init();
        IslandMainMissionsMenu.init();
        IslandMembersMenu.init();
        IslandMissionsMenu.init();
        IslandPanelMenu.init();
        IslandPermissionsMenu.init();
        IslandRateMenu.init();
        IslandRatingsMenu.init();
        IslandsTopMenu.init();
        IslandUpgradesMenu.init();
        IslandValuesMenu.init();
        IslandVisitorsMenu.init();
        IslandWarpsMenu.init();
        MemberManageMenu.init();
        MemberRoleMenu.init();
    }

    @Override
    public void openBorderColorMenu(SuperiorPlayer superiorPlayer) {
        BorderColorMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openConfirmDisbandMenu(SuperiorPlayer superiorPlayer) {
        ConfirmDisbandMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openGlobalWarpsMenu(SuperiorPlayer superiorPlayer) {
        GlobalWarpsMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandBiomesMenu(SuperiorPlayer superiorPlayer) {
        IslandBiomesMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandCreationMenu(SuperiorPlayer superiorPlayer, String islandName) {
        IslandCreationMenu.openInventory(superiorPlayer, null, islandName);
    }

    @Override
    public void openIslandMainMissionsMenu(SuperiorPlayer superiorPlayer) {
        IslandMainMissionsMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandMembersMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandMembersMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandMissionsMenu(SuperiorPlayer superiorPlayer, boolean islandMissions) {
        IslandMissionsMenu.openInventory(superiorPlayer, null, islandMissions);
    }

    @Override
    public void openIslandPanelMenu(SuperiorPlayer superiorPlayer) {
        IslandPanelMenu.openInventory(superiorPlayer, null);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole) {
        IslandPermissionsMenu.openInventory(superiorPlayer, null, island, playerRole);
    }

    @Override
    public void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        IslandPermissionsMenu.openInventory(superiorPlayer, null, island, targetPlayer);
    }

    @Override
    public void openIslandRateMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandRateMenu.openInventory(superiorPlayer, island, null);
    }

    @Override
    public void openIslandRatingsMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandRatingsMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandsTopMenu(SuperiorPlayer superiorPlayer, SortingType sortingType) {
        IslandsTopMenu.openInventory(superiorPlayer, null, sortingType);
    }

    @Override
    public void openIslandUpgradeMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandUpgradesMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandValuesMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandValuesMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandVisitorsMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandVisitorsMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openIslandWarpsMenu(SuperiorPlayer superiorPlayer, Island island) {
        IslandWarpsMenu.openInventory(superiorPlayer, null, island);
    }

    @Override
    public void openMemberManageMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        MemberManageMenu.openInventory(superiorPlayer, null, targetPlayer);
    }

    @Override
    public void openMemberRoleMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer) {
        MemberRoleMenu.openInventory(superiorPlayer, null, targetPlayer);
    }

}
