package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.common.annotations.Nullable;
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

public interface MenusProvider {

    /**
     * Initialize the menus.
     */
    void initializeMenus();

    /**
     * Open the bank-logs menu.
     * Used to display all logs of bank transactions.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to display bank logs for.
     */
    void openBankLogs(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the bank-logs menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshBankLogs(Island island);

    /**
     * Open the biomes-menu.
     * Used to display and choose biomes for the island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to change biomes for.
     */
    void openBiomes(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Open the border-color menu.
     * Used to change the color of the world border for a player.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     */
    void openBorderColor(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu);

    /**
     * Open the confirm-ban menu.
     * Used to confirm a ban of an island member.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to ban the player from.
     * @param bannedPlayer The player that will be banned.
     */
    void openConfirmBan(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer bannedPlayer);

    /**
     * Open the confirm-disband menu.
     * Used to confirm disband of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to disband.
     */
    void openConfirmDisband(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Open the confirm-kick menu.
     * Used to confirm a kick of an island member.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to kick the player from.
     * @param kickedPlayer The player that will be kicked.
     */
    void openConfirmKick(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer kickedPlayer);

    /**
     * Open the confirm-leave menu.
     * Used to confirm leaving of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     */
    void openConfirmLeave(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu);

    /**
     * Open the control-panel menu.
     * Used when opening the control panel of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to open the control panel of.
     */
    void openControlPanel(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Open the coops menu.
     * Used when opening the coops menu of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get coop-members from.
     */
    void openCoops(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the coops-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshCoops(Island island);

    /**
     * Open the block-counts menu.
     * Used when opening the counts menu of an island (using /is counts, for example)
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get block counts from.
     */
    void openCounts(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the counts-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshCounts(Island island);

    /**
     * Open the global-warps menu.
     * Used when running the /is warp command.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     */
    void openGlobalWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu);

    /**
     * Refresh the global-warps menu.
     */
    void refreshGlobalWarps();

    /**
     * Open the island-bank menu.
     * Used when running the /is bank command.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to open the bank for.
     */
    void openIslandBank(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the island bank menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshIslandBank(Island island);

    /**
     * Open the island-banned-players menu.
     * Used when running the /is ban command.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to open the menu for.
     */
    void openIslandBannedPlayers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the banned-players menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshIslandBannedPlayers(Island island);

    /**
     * Open the island-chests menu.
     * Used to open the shared chests menu of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to open the shared-chests menu for.
     */
    void openIslandChest(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the island-chests menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshIslandChest(Island island);

    /**
     * Open the islands-creation menu.
     * Used when creating a new island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param islandName   The desired name of the new island.
     */
    void openIslandCreation(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, String islandName);

    /**
     * Open the rate-menu.
     * Used when giving a rating for an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to give a rating.
     */
    void openIslandRate(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Open the ratings-menu.
     * Used when checking given ratings of an island.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get ratings from.
     */
    void openIslandRatings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the ratings-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshIslandRatings(Island island);

    /**
     * Open the member-manage menu.
     * Used when managing an island member.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param islandMember The island member to manage.
     */
    void openMemberManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember);

    /**
     * Destroy the member-manage menus for a specific island member.
     *
     * @param islandMember The island member to close menus of.
     */
    void destroyMemberManage(SuperiorPlayer islandMember);

    /**
     * Used to open the member-role menu.
     * Used when changing a role of an island member.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param islandMember The island member to change role for.
     */
    void openMemberRole(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SuperiorPlayer islandMember);

    /**
     * Destroy the member-role menus for a specific island member.
     *
     * @param islandMember The island member to close menus of.
     */
    void destroyMemberRole(SuperiorPlayer islandMember);

    /**
     * Open the members-menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to check the members of.
     */
    void openMembers(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the members-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshMembers(Island island);

    /**
     * Open the missions-menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     */
    void openMissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu);

    /**
     * Open the missions-menu of a specific category.
     *
     * @param targetPlayer    The player to open the menu for.
     * @param previousMenu    The previous menu that was opened, if exists.
     * @param missionCategory The category to get missions from.
     */
    void openMissionsCategory(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, MissionCategory missionCategory);

    /**
     * Refresh the missions-menu for a specific category.
     *
     * @param missionCategory The category to refresh the menus for.
     */
    void refreshMissionsCategory(MissionCategory missionCategory);

    /**
     * Open the permissions-menu.
     * Used when changing island-permissions of a player on an island.
     *
     * @param targetPlayer      The player to open the menu for.
     * @param previousMenu      The previous menu that was opened, if exists.
     * @param targetIsland      The island to change permissions in.
     * @param permissiblePlayer The player to change permissions for.
     */
    void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu,
                         Island targetIsland, SuperiorPlayer permissiblePlayer);

    /**
     * Open the permissions-menu.
     * Used when changing island-permissions of an island-role on an island.
     *
     * @param targetPlayer    The player to open the menu for.
     * @param previousMenu    The previous menu that was opened, if exists.
     * @param targetIsland    The island to change permissions in.
     * @param permissibleRole The island-role to change permissions for.
     */
    void openPermissions(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu,
                         Island targetIsland, PlayerRole permissibleRole);

    /**
     * Refresh the permissions-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshPermissions(Island island);

    /**
     * Refresh the permissions-menu of a player for a specific island.
     *
     * @param island            The island to refresh the menus for.
     * @param permissiblePlayer The player to change permissions.
     */
    void refreshPermissions(Island island, SuperiorPlayer permissiblePlayer);

    /**
     * Refresh the permissions-menu of an island role for a specific island.
     *
     * @param island          The island to refresh the menus for.
     * @param permissibleRole The island role to change permissions for.
     */
    void refreshPermissions(Island island, PlayerRole permissibleRole);

    /**
     * Update the island permission in the menu.
     *
     * @param islandPrivilege The permission to update.
     */
    void updatePermission(IslandPrivilege islandPrivilege);

    /**
     * Open the player-language menu.
     * Used when a player changes his language.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     */
    void openPlayerLanguage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu);

    /**
     * Open the island-settings menu.
     * Used when changing island-settings.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to change settings for.
     */
    void openSettings(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the island-settings menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshSettings(Island island);

    /**
     * Update the island settings in the menu.
     *
     * @param islandFlag The settings to update.
     */
    void updateSettings(IslandFlag islandFlag);

    /**
     * Open the top-islands menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param sortingType  The type of sorting of islands to use.
     */
    void openTopIslands(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, SortingType sortingType);

    /**
     * Refresh the top-islands menu for a specific sorting type.
     *
     * @param sortingType The sorting type to refresh.
     */
    void refreshTopIslands(SortingType sortingType);

    /**
     * Open the unique-visitors menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get visitors from.
     */
    void openUniqueVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the unique-visitors menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshUniqueVisitors(Island island);

    /**
     * Open the upgrades-menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get upgrade levels from.
     */
    void openUpgrades(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the upgrades-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshUpgrades(Island island);

    /**
     * Open the values-menu.
     * Used when right-clicking an island in the top-islands menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get values from.
     */
    void openValues(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the values-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshValues(Island island);

    /**
     * Open the visitors-menu.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get visitors from.
     */
    void openVisitors(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the visitors-menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshVisitors(Island island);

    /**
     * Open the warp categories menu
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetIsland The island to get warp categories from.
     */
    void openWarpCategories(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, Island targetIsland);

    /**
     * Refresh the warp categories menu for a specific island.
     *
     * @param island The island to refresh the menus for.
     */
    void refreshWarpCategories(Island island);

    /**
     * Destroy the warp-categories menus for a specific island.
     *
     * @param island The island to close menus of.
     */
    void destroyWarpCategories(Island island);

    /**
     * Open the warp-category icon edit menu.
     * Used when editing an icon of a warp category.
     *
     * @param targetPlayer   The player to open the menu for.
     * @param previousMenu   The previous menu that was opened, if exists.
     * @param targetCategory The warp category to edit the icon for.
     */
    void openWarpCategoryIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory);

    /**
     * Open the warp category manage menu.
     * Used when managing a warp category.
     *
     * @param targetPlayer   The player to open the menu for.
     * @param previousMenu   The previous menu that was opened, if exists.
     * @param targetCategory The warp category to manage.
     */
    void openWarpCategoryManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory);

    /**
     * Refresh the warp category manage menu for a specific warp category.
     *
     * @param warpCategory The warp category to refresh the menus for.
     */
    void refreshWarpCategoryManage(WarpCategory warpCategory);

    /**
     * Open the warp icon edit menu.
     * Used when editing an icon of a warp.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetWarp   The warp to edit the icon for.
     */
    void openWarpIconEdit(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp);

    /**
     * Open the warp manage menu.
     * Used when managing a warp.
     *
     * @param targetPlayer The player to open the menu for.
     * @param previousMenu The previous menu that was opened, if exists.
     * @param targetWarp   The warp to manage.
     */
    void openWarpManage(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, IslandWarp targetWarp);

    /**
     * Refresh the warp manage menu for a specific warp.
     *
     * @param islandWarp The warp to refresh the menus for.
     */
    void refreshWarpManage(IslandWarp islandWarp);

    /**
     * Open the warps-menu.
     * Used to look for all warps in a category.
     *
     * @param targetPlayer   The player to open the menu for.
     * @param previousMenu   The previous menu that was opened, if exists.
     * @param targetCategory The category to get warps from.
     */
    void openWarps(SuperiorPlayer targetPlayer, @Nullable ISuperiorMenu previousMenu, WarpCategory targetCategory);

    /**
     * Refresh the warps-menu for a specific island.
     *
     * @param warpCategory The warp category to refresh the menus for.
     */
    void refreshWarps(WarpCategory warpCategory);

    /**
     * Destroy the warp-categories menus for a specific warp category.
     *
     * @param warpCategory The warp category to close menus of.
     */
    void destroyWarps(WarpCategory warpCategory);

}
