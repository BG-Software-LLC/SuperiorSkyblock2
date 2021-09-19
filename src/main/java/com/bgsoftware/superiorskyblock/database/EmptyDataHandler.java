package com.bgsoftware.superiorskyblock.database;

import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.data.PlayerDataHandler;

public final class EmptyDataHandler implements IslandDataHandler, PlayerDataHandler {

    private static final EmptyDataHandler instance = new EmptyDataHandler();

    public static EmptyDataHandler getInstance() {
        return instance;
    }

    private EmptyDataHandler(){

    }

    @Override
    public void saveMembers() {

    }

    @Override
    public void saveBannedPlayers() {

    }

    @Override
    public void saveCoopLimit() {

    }

    @Override
    public void saveTeleportLocation() {

    }

    @Override
    public void saveVisitorLocation() {

    }

    @Override
    public void saveUnlockedWorlds() {

    }

    @Override
    public void savePermissions() {

    }

    @Override
    public void saveName() {

    }

    @Override
    public void saveDescription() {

    }

    @Override
    public void saveSize() {

    }

    @Override
    public void saveDiscord() {

    }

    @Override
    public void savePaypal() {

    }

    @Override
    public void saveLockedStatus() {

    }

    @Override
    public void saveIgnoredStatus() {

    }

    @Override
    public void saveLastTimeUpdate() {

    }

    @Override
    public void saveBankLimit() {

    }

    @Override
    public void saveBonusWorth() {

    }

    @Override
    public void saveBonusLevel() {

    }

    @Override
    public void saveUpgrades() {

    }

    @Override
    public void saveCropGrowth() {

    }

    @Override
    public void saveSpawnerRates() {

    }

    @Override
    public void saveMobDrops() {

    }

    @Override
    public void saveBlockLimits() {

    }

    @Override
    public void saveEntityLimits() {

    }

    @Override
    public void saveTeamLimit() {

    }

    @Override
    public void saveWarpsLimit() {

    }

    @Override
    public void saveIslandEffects() {

    }

    @Override
    public void saveRolesLimits() {

    }

    @Override
    public void saveWarps() {

    }

    @Override
    public void saveRatings() {

    }

    @Override
    public void saveMissions() {

    }

    @Override
    public void saveSettings() {

    }

    @Override
    public void saveGenerators() {

    }

    @Override
    public void saveGeneratedSchematics() {

    }

    @Override
    public void saveDirtyChunks() {

    }

    @Override
    public void saveBlockCounts() {

    }

    @Override
    public void saveIslandChest() {

    }

    @Override
    public void saveLastInterestTime() {

    }

    @Override
    public void saveUniqueVisitors() {

    }

    @Override
    public void saveWarpCategories() {

    }

    @Override
    public void saveTextureValue() {

    }

    @Override
    public void savePlayerName() {

    }

    @Override
    public void saveUserLocale() {

    }

    @Override
    public void saveIslandLeader() {

    }

    @Override
    public void savePlayerRole() {

    }

    @Override
    public void saveToggledBorder() {

    }

    @Override
    public void saveDisbands() {

    }

    @Override
    public void saveToggledPanel() {

    }

    @Override
    public void saveIslandFly() {

    }

    @Override
    public void saveBorderColor() {

    }

    @Override
    public void saveLastTimeStatus() {

    }
}
