package com.bgsoftware.superiorskyblock.data;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import org.bukkit.World;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class IslandsDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandsDatabaseBridge(){
    }

    public static void saveMembers(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("members",
                IslandSerializer.serializePlayers(island.getIslandMembers(false))));
    }

    public static void saveBannedPlayers(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("banned",
                IslandSerializer.serializePlayers(island.getBannedPlayers())));
    }

    public static void saveCoopLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("coopLimit", island.getCoopLimit()));
    }

    public static void saveTeleportLocation(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("teleportLocation",
                IslandSerializer.serializeLocations(island.getTeleportLocations())));
    }

    public static void saveVisitorLocation(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("visitorsLocation",
                LocationUtils.getLocation(island.getVisitorsLocation())));
    }

    public static void saveUnlockedWorlds(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("unlockedWorlds",
                island.getUnlockedWorldsFlag() + ""));
    }

    public static void savePermissions(Island island){
        island.getDatabaseBridge().updateObject("islands", new Pair<>("permissionNodes",
                IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions())));
    }

    public static void saveName(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("name", island.getName()));
    }

    public static void saveDescription(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("description", island.getDescription()));
    }

    public static void saveSize(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("islandSize", island.getIslandSize()));
    }

    public static void saveDiscord(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("discord", island.getDiscord()));
    }

    public static void savePaypal(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("paypal", island.getPaypal()));
    }

    public static void saveLockedStatus(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("locked", island.isLocked()));
    }

    public static void saveIgnoredStatus(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("ignored", island.isIgnored()));
    }

    public static void saveLastTimeUpdate(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("lastTimeUpdate", island.getLastTimeUpdate()));
    }

    public static void saveBankLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("bankLimit", island.getBankLimit() + ""));
    }

    public static void saveBonusWorth(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("bonusWorth", island.getBonusWorth() + ""));
    }

    public static void saveBonusLevel(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("bonusLevel", island.getBonusLevel() + ""));
    }

    public static void saveUpgrades(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("upgrades",
                IslandSerializer.serializeUpgrades(island.getUpgrades())));
    }

    public static void saveCropGrowth(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("cropGrowth", island.getCropGrowthMultiplier()));
    }

    public static void saveSpawnerRates(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("spawnerRates", island.getSpawnerRatesMultiplier()));
    }

    public static void saveMobDrops(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("mobDrops", island.getMobDropsMultiplier()));
    }

    public static void saveBlockLimits(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("blockLimits",
                IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits())));
    }

    public static void saveEntityLimits(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("entityLimits",
                IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits())));
    }

    public static void saveTeamLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("teamLimit", island.getTeamLimit()));
    }

    public static void saveWarpsLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("warpsLimit", island.getWarpsLimit()));
    }

    public static void saveIslandEffects(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("islandEffects",
                IslandSerializer.serializeEffects(island.getPotionEffects())));
    }

    public static void saveRolesLimits(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("roleLimits",
                IslandSerializer.serializeRoleLimits(island.getRoleLimits())));
    }

    public static void saveWarps(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("warps",
                IslandSerializer.serializeWarps(island.getIslandWarps())));
    }

    public static void saveRatings(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("ratings",
                IslandSerializer.serializeRatings(island.getRatings())));
    }

    public static void saveMissions(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("missions",
                IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts())));
    }

    public static void saveSettings(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("settings",
                IslandSerializer.serializeIslandFlags(island.getAllSettings())));
    }

    public static void saveGenerators(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("generator",
                IslandSerializer.serializeGenerator(getIslandGenerators(island))));
    }

    public static void saveGeneratedSchematics(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("generatedSchematics",
                island.getGeneratedSchematicsFlag() + ""));
    }

    public static void saveDirtyChunks(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("dirtyChunks", ChunksTracker.serialize(island)));
    }

    public static void saveBlockCounts(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("blockCounts",
                IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger())));
    }

    public static void saveIslandChest(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("islandChest",
                IslandSerializer.serializeIslandChest(island.getChest())));
    }

    public static void saveLastInterestTime(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("lastInterest", island.getLastInterestTime()));
    }

    public static void saveUniqueVisitors(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("uniqueVisitors",
                IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes())));
    }

    public static void saveWarpCategories(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("warpCategories",
                IslandSerializer.serializeWarpCategories(island.getWarpCategories())));
    }

    public static void saveUniqueId(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("uuid", island.getUniqueId().toString()));
    }

    public static void saveIslandBank(Island island) {
        island.getDatabaseBridge().updateObject("islands", new Pair<>("islandBank",
                island.getIslandBank().getBalance() + ""));
    }

    public static void saveBankTransaction(Island island, BankTransaction bankTransaction) {
        island.getDatabaseBridge().insertObject("bankTransactions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", bankTransaction.getPlayer() == null ? "" : bankTransaction.getPlayer().toString()),
                new Pair<>("bankAction", bankTransaction.getAction().name()),
                new Pair<>("position", bankTransaction.getPosition()),
                new Pair<>("time", bankTransaction.getTime() + ""),
                new Pair<>("failureReason", bankTransaction.getFailureReason()),
                new Pair<>("amount", bankTransaction.getAmount() + "")
        );
    }

    public static void insertIsland(Island island){
        island.getDatabaseBridge().insertObject("islands",
                new Pair<>("owner", island.getOwner().getUniqueId().toString()),
                new Pair<>("center", LocationUtils.getLocation(island.getCenter(plugin.getSettings().defaultWorldEnvironment))),
                new Pair<>("teleportLocation", IslandSerializer.serializeLocations(island.getTeleportLocations())),
                new Pair<>("members", IslandSerializer.serializePlayers(island.getIslandMembers(false))),
                new Pair<>("banned", IslandSerializer.serializePlayers(island.getBannedPlayers())),
                new Pair<>("permissionNodes", IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions())),
                new Pair<>("upgrades", IslandSerializer.serializeUpgrades(island.getUpgrades())),
                new Pair<>("warps", IslandSerializer.serializeWarps(island.getIslandWarps())),
                new Pair<>("islandBank", island.getIslandBank().getBalance() + ""),
                new Pair<>("islandSize", island.getIslandSizeRaw()),
                new Pair<>("blockLimits", IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits())),
                new Pair<>("teamLimit", island.getTeamLimitRaw()),
                new Pair<>("cropGrowth", (float) island.getCropGrowthRaw()),
                new Pair<>("spawnerRates", (float) island.getSpawnerRatesRaw()),
                new Pair<>("mobDrops", (float) island.getMobDropsRaw()),
                new Pair<>("discord", island.getDiscord()),
                new Pair<>("paypal", island.getPaypal()),
                new Pair<>("warpsLimit", island.getWarpsLimitRaw()),
                new Pair<>("bonusWorth", island.getBonusWorth() + ""),
                new Pair<>("locked", island.isLocked()),
                new Pair<>("blockCounts", IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger())),
                new Pair<>("name", island.getName()),
                new Pair<>("visitorsLocation", LocationUtils.getLocation(island.getVisitorsLocation())),
                new Pair<>("description", island.getDescription()),
                new Pair<>("ratings", IslandSerializer.serializeRatings(island.getRatings())),
                new Pair<>("missions", IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts())),
                new Pair<>("settings", IslandSerializer.serializeIslandFlags(island.getAllSettings())),
                new Pair<>("ignored", island.isIgnored()),
                new Pair<>("generator", IslandSerializer.serializeGenerator(getIslandGenerators(island))),
                new Pair<>("generatedSchematics", island.getGeneratedSchematicsFlag() + ""),
                new Pair<>("schemName", island.getSchematicName()),
                new Pair<>("uniqueVisitors", IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes())),
                new Pair<>("unlockedWorlds", island.getUnlockedWorldsFlag() + ""),
                new Pair<>("lastTimeUpdate", island.getLastTimeUpdate()),
                new Pair<>("dirtyChunks", ChunksTracker.serialize(island)),
                new Pair<>("entityLimits", IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits())),
                new Pair<>("bonusLevel", island.getBonusLevel() + ""),
                new Pair<>("creationTime", island.getCreationTime()),
                new Pair<>("coopLimit", island.getCoopLimitRaw()),
                new Pair<>("islandEffects", IslandSerializer.serializeEffects(island.getPotionEffects())),
                new Pair<>("islandChest", IslandSerializer.serializeIslandChest(island.getChest())),
                new Pair<>("uuid", island.getUniqueId().toString()),
                new Pair<>("bankLimit", island.getBankLimitRaw() + ""),
                new Pair<>("lastInterest", island.getLastInterestTime()),
                new Pair<>("roleLimits", IslandSerializer.serializeRoleLimits(island.getRoleLimits())),
                new Pair<>("warpCategories", IslandSerializer.serializeWarpCategories(island.getWarpCategories()))
        );
    }

    public static void updateIsland(Island island){
        island.getDatabaseBridge().updateObject("islands",
                new Pair<>("center", LocationUtils.getLocation(island.getCenter(plugin.getSettings().defaultWorldEnvironment))),
                new Pair<>("teleportLocation", IslandSerializer.serializeLocations(island.getTeleportLocations())),
                new Pair<>("members", IslandSerializer.serializePlayers(island.getIslandMembers(false))),
                new Pair<>("banned", IslandSerializer.serializePlayers(island.getBannedPlayers())),
                new Pair<>("permissionNodes", IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions())),
                new Pair<>("upgrades", IslandSerializer.serializeUpgrades(island.getUpgrades())),
                new Pair<>("warps", IslandSerializer.serializeWarps(island.getIslandWarps())),
                new Pair<>("islandBank", island.getIslandBank().getBalance() + ""),
                new Pair<>("islandSize", island.getIslandSizeRaw()),
                new Pair<>("blockLimits", IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits())),
                new Pair<>("teamLimit", island.getTeamLimitRaw()),
                new Pair<>("cropGrowth", (float) island.getCropGrowthRaw()),
                new Pair<>("spawnerRates", (float) island.getSpawnerRatesRaw()),
                new Pair<>("mobDrops", (float) island.getMobDropsRaw()),
                new Pair<>("discord", island.getDiscord()),
                new Pair<>("paypal", island.getPaypal()),
                new Pair<>("warpsLimit", island.getWarpsLimitRaw()),
                new Pair<>("bonusWorth", island.getBonusWorth() + ""),
                new Pair<>("locked", island.isLocked()),
                new Pair<>("blockCounts", IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger())),
                new Pair<>("name", island.getName()),
                new Pair<>("visitorsLocation", LocationUtils.getLocation(island.getVisitorsLocation())),
                new Pair<>("description", island.getDescription()),
                new Pair<>("ratings", IslandSerializer.serializeRatings(island.getRatings())),
                new Pair<>("missions", IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts())),
                new Pair<>("settings", IslandSerializer.serializeIslandFlags(island.getAllSettings())),
                new Pair<>("ignored", island.isIgnored()),
                new Pair<>("generator", IslandSerializer.serializeGenerator(getIslandGenerators(island))),
                new Pair<>("generatedSchematics", island.getGeneratedSchematicsFlag() + ""),
                new Pair<>("schemName", island.getSchematicName()),
                new Pair<>("uniqueVisitors", IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes())),
                new Pair<>("unlockedWorlds", island.getUnlockedWorldsFlag() + ""),
                new Pair<>("lastTimeUpdate", island.getLastTimeUpdate()),
                new Pair<>("dirtyChunks", ChunksTracker.serialize(island)),
                new Pair<>("entityLimits", IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits())),
                new Pair<>("bonusLevel", island.getBonusLevel() + ""),
                new Pair<>("creationTime", island.getCreationTime()),
                new Pair<>("coopLimit", island.getCoopLimitRaw()),
                new Pair<>("islandEffects", IslandSerializer.serializeEffects(island.getPotionEffects())),
                new Pair<>("islandChest", IslandSerializer.serializeIslandChest(island.getChest())),
                new Pair<>("uuid", island.getUniqueId().toString()),
                new Pair<>("bankLimit", island.getBankLimitRaw() + ""),
                new Pair<>("lastInterest", island.getLastInterestTime()),
                new Pair<>("roleLimits", IslandSerializer.serializeRoleLimits(island.getRoleLimits())),
                new Pair<>("warpCategories", IslandSerializer.serializeWarpCategories(island.getWarpCategories()))
        );
    }

    public static void deleteIsland(Island island){
        island.getDatabaseBridge().deleteObject("islands");
    }

    private static Map<Key, Integer>[] getIslandGenerators(Island island){
        Map<Key, Integer>[] customGeneratorAmounts = new Map[World.Environment.values().length];
        for(World.Environment environment : World.Environment.values())
            customGeneratorAmounts[environment.ordinal()] = island.getCustomGeneratorAmounts(environment);
        return customGeneratorAmounts;
    }

}
