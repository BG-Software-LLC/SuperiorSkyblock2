package com.bgsoftware.superiorskyblock.database;

import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.data.PlayerDataHandler;

@SuppressWarnings("deprecation")
public final class EmptyDataHandler implements IslandDataHandler, PlayerDataHandler {

    private static final EmptyDataHandler instance = new EmptyDataHandler();

    private EmptyDataHandler() {

    }

    public static EmptyDataHandler getInstance() {
        return instance;
    }

    @Override
    public void saveMembers() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBannedPlayers() {
        printUnsupportedOperation();
    }

    @Override
    public void saveCoopLimit() {
        printUnsupportedOperation();
    }

    @Override
    public void saveTeleportLocation() {
        printUnsupportedOperation();
    }

    @Override
    public void saveVisitorLocation() {
        printUnsupportedOperation();
    }

    @Override
    public void saveUnlockedWorlds() {
        printUnsupportedOperation();
    }

    @Override
    public void savePermissions() {
        printUnsupportedOperation();
    }

    @Override
    public void saveName() {
        printUnsupportedOperation();
    }

    @Override
    public void saveDescription() {
        printUnsupportedOperation();
    }

    @Override
    public void saveSize() {
        printUnsupportedOperation();
    }

    @Override
    public void saveDiscord() {
        printUnsupportedOperation();
    }

    @Override
    public void savePaypal() {
        printUnsupportedOperation();
    }

    @Override
    public void saveLockedStatus() {
        printUnsupportedOperation();
    }

    @Override
    public void saveIgnoredStatus() {
        printUnsupportedOperation();
    }

    @Override
    public void saveLastTimeUpdate() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBankLimit() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBonusWorth() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBonusLevel() {
        printUnsupportedOperation();
    }

    @Override
    public void saveUpgrades() {
        printUnsupportedOperation();
    }

    @Override
    public void saveCropGrowth() {
        printUnsupportedOperation();
    }

    @Override
    public void saveSpawnerRates() {
        printUnsupportedOperation();
    }

    @Override
    public void saveMobDrops() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBlockLimits() {
        printUnsupportedOperation();
    }

    @Override
    public void saveEntityLimits() {
        printUnsupportedOperation();
    }

    @Override
    public void saveTeamLimit() {
        printUnsupportedOperation();
    }

    @Override
    public void saveWarpsLimit() {
        printUnsupportedOperation();
    }

    @Override
    public void saveIslandEffects() {
        printUnsupportedOperation();
    }

    @Override
    public void saveRolesLimits() {
        printUnsupportedOperation();
    }

    @Override
    public void saveWarps() {
        printUnsupportedOperation();
    }

    @Override
    public void saveRatings() {
        printUnsupportedOperation();
    }

    @Override
    public void saveMissions() {
        printUnsupportedOperation();
    }

    @Override
    public void saveSettings() {
        printUnsupportedOperation();
    }

    @Override
    public void saveGenerators() {
        printUnsupportedOperation();
    }

    @Override
    public void saveGeneratedSchematics() {
        printUnsupportedOperation();
    }

    @Override
    public void saveDirtyChunks() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBlockCounts() {
        printUnsupportedOperation();
    }

    @Override
    public void saveIslandChest() {
        printUnsupportedOperation();
    }

    @Override
    public void saveLastInterestTime() {
        printUnsupportedOperation();
    }

    @Override
    public void saveUniqueVisitors() {
        printUnsupportedOperation();
    }

    @Override
    public void saveWarpCategories() {
        printUnsupportedOperation();
    }

    @Override
    public void saveTextureValue() {
        printUnsupportedOperation();
    }

    @Override
    public void savePlayerName() {
        printUnsupportedOperation();
    }

    @Override
    public void saveUserLocale() {
        printUnsupportedOperation();
    }

    @Override
    public void saveIslandLeader() {
        printUnsupportedOperation();
    }

    @Override
    public void savePlayerRole() {
        printUnsupportedOperation();
    }

    @Override
    public void saveToggledBorder() {
        printUnsupportedOperation();
    }

    @Override
    public void saveDisbands() {
        printUnsupportedOperation();
    }

    @Override
    public void saveToggledPanel() {
        printUnsupportedOperation();
    }

    @Override
    public void saveIslandFly() {
        printUnsupportedOperation();
    }

    @Override
    public void saveBorderColor() {
        printUnsupportedOperation();
    }

    @Override
    public void saveLastTimeStatus() {
        printUnsupportedOperation();
    }

    private static void printUnsupportedOperation() {
        new UnsupportedOperationException("Please use DatabaseBridge instead!").printStackTrace();
    }

}
