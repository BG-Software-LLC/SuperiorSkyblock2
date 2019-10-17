package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

public interface MenusManager {

    /**
     * Open the border color menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openBorderColorMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the confirm disband menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openConfirmDisbandMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the global warps menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openGlobalWarpsMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the island biomes menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openIslandBiomesMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the island creation menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param islandName The name to give the island if the player creates a new island.
     */
    void openIslandCreationMenu(SuperiorPlayer superiorPlayer, String islandName);

    /**
     * Open the main island missions menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openIslandMainMissionsMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the island members menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the members from.
     */
    void openIslandMembersMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island missions menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param islandMissions Should island missions be displayed or player missions?
     */
    void openIslandMissionsMenu(SuperiorPlayer superiorPlayer, boolean islandMissions);

    /**
     * Open the island panel menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    void openIslandPanelMenu(SuperiorPlayer superiorPlayer);

    /**
     * Open the island permissions menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island that holds the permissions.
     * @param playerRole The target role to see their permissions.
     */
    void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole);

    /**
     * Open the island permissions menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island that holds the permissions.
     * @param targetPlayer The target player to see their permissions.
     */
    void openIslandPermissionsMenu(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer);

    /**
     * Open the island rate menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The target island to give the rating.
     */
    void openIslandRateMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island ratings menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the ratings from.
     */
    void openIslandRatingsMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island settings menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the settings from.
     */
    void openIslandSettingsMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the islands top menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param sortingType The sorting type you want to open.
     */
    void openIslandsTopMenu(SuperiorPlayer superiorPlayer, SortingType sortingType);

    /**
     * Open the island upgrade menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the upgrades from.
     */
    void openIslandUpgradeMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island values menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the values from.
     */
    void openIslandValuesMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island visitors menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the visitors from.
     */
    void openIslandVisitorsMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the island warps menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param island The island to get the warps from.
     */
    void openIslandWarpsMenu(SuperiorPlayer superiorPlayer, Island island);

    /**
     * Open the member manage menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param targetPlayer The target player to manage.
     */
    void openMemberManageMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer);

    /**
     * Open the member role menu for a player.
     * @param superiorPlayer The player to open the menu for.
     * @param targetPlayer The target player to manage their role.
     */
    void openMemberRoleMenu(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer);



}
