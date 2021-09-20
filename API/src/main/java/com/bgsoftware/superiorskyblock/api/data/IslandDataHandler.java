package com.bgsoftware.superiorskyblock.api.data;

@Deprecated
public interface IslandDataHandler {

    /**
     * Save the members of the island.
     */
    void saveMembers();

    /**
     * Save the banned-players of the island.
     */
    void saveBannedPlayers();

    /**
     * Save the coop-limit of the island.
     */
    void saveCoopLimit();

    /**
     * Save the teleport-locations of the island.
     */
    void saveTeleportLocation();

    /**
     * Save the visitors location of the island.
     */
    void saveVisitorLocation();

    /**
     * Save the unlocked worlds of the island.
     */
    void saveUnlockedWorlds();

    /**
     * Save the permissions of the island.
     */
    void savePermissions();

    /**
     * Save the name of the island.
     */
    void saveName();

    /**
     * Save the description of the island.
     */
    void saveDescription();

    /**
     * Save the size of the island.
     */
    void saveSize();

    /**
     * Save the discord of the island.
     */
    void saveDiscord();

    /**
     * Save the paypal of the island.
     */
    void savePaypal();

    /**
     * Save the locked-status of the island.
     */
    void saveLockedStatus();

    /**
     * Save the ignored-status of the island.
     */
    void saveIgnoredStatus();

    /**
     * Save the last time updated of the island.
     */
    void saveLastTimeUpdate();

    /**
     * Save the bank limit of the island.
     */
    void saveBankLimit();

    /**
     * Save the bonus worth of the island.
     */
    void saveBonusWorth();

    /**
     * Save the bonus level of the island.
     */
    void saveBonusLevel();

    /**
     * Save the upgrades of the island.
     */
    void saveUpgrades();

    /**
     * Save the crop growth of the island.
     */
    void saveCropGrowth();

    /**
     * Save the spawner rates of the island.
     */
    void saveSpawnerRates();

    /**
     * Save the mob drops of the island.
     */
    void saveMobDrops();

    /**
     * Save the block limits of the island.
     */
    void saveBlockLimits();

    /**
     * Save the entity limits of the island.
     */
    void saveEntityLimits();

    /**
     * Save the team limit of the island.
     */
    void saveTeamLimit();

    /**
     * Save the warps limit of the island.
     */
    void saveWarpsLimit();

    /**
     * Save the island effects of the island.
     */
    void saveIslandEffects();

    /**
     * Save the island roles limits of the island.
     */
    void saveRolesLimits();

    /**
     * Save the warps of the island.
     */
    void saveWarps();

    /**
     * Save the ratings of the island.
     */
    void saveRatings();

    /**
     * Save the missions of the island.
     */
    void saveMissions();

    /**
     * Save the settings of the island.
     */
    void saveSettings();

    /**
     * Save the generator rates of the island in all the worlds.
     */
    void saveGenerators();

    /**
     * Save the generated schematics of the island.
     */
    void saveGeneratedSchematics();

    /**
     * Save the dirty chunks of the island.
     */
    void saveDirtyChunks();

    /**
     * Save the block counts of the island.
     */
    void saveBlockCounts();

    /**
     * Save the island chest of the island.
     */
    void saveIslandChest();

    /**
     * Save the last interest time of the island.
     */
    void saveLastInterestTime();

    /**
     * Save the unique visitors of the island.
     */
    void saveUniqueVisitors();

    /**
     * Save the warp categories of the island.
     */
    void saveWarpCategories();

}
