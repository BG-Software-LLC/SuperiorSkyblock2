package com.bgsoftware.superiorskyblock.island.data;

import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.utils.database.StatementHolder;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import org.bukkit.World;

import java.util.Map;

public final class SIslandDataHandler extends DatabaseObject implements IslandDataHandler {

    private final Island island;

    public SIslandDataHandler(Island island){
        this.island = island;
    }

    @Override
    public void saveMembers() {
        Query.ISLAND_SET_MEMBERS.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(island.getIslandMembers(false)))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveBannedPlayers() {
        Query.ISLAND_SET_BANNED.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(island.getBannedPlayers()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveCoopLimit() {
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder(this)
                .setInt(island.getCoopLimit())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveTeleportLocation() {
        Query.ISLAND_SET_TELEPORT_LOCATION.getStatementHolder(this)
                .setString(IslandSerializer.serializeLocations(island.getTeleportLocations()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveVisitorLocation() {
        Query.ISLAND_SET_VISITORS_LOCATION.getStatementHolder(this)
                .setString(LocationUtils.getLocation(island.getVisitorsLocation()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveUnlockedWorlds() {
        Query.ISLAND_SET_UNLOCK_WORLDS.getStatementHolder(this)
                .setString(island.getUnlockedWorldsFlag() + "")
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void savePermissions(){
        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder(this)
                .setString(IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveName() {
        Query.ISLAND_SET_NAME.getStatementHolder(this)
                .setString(island.getName())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveDescription() {
        Query.ISLAND_SET_DESCRIPTION.getStatementHolder(this)
                .setString(island.getDescription())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveSize() {
        Query.ISLAND_SET_SIZE.getStatementHolder(this)
                .setInt(island.getIslandSize())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveDiscord() {
        Query.ISLAND_SET_DISCORD.getStatementHolder(this)
                .setString(island.getDiscord())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void savePaypal() {
        Query.ISLAND_SET_PAYPAL.getStatementHolder(this)
                .setString(island.getPaypal())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveLockedStatus() {
        Query.ISLAND_SET_LOCKED.getStatementHolder(this)
                .setBoolean(island.isLocked())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveIgnoredStatus() {
        Query.ISLAND_SET_IGNORED.getStatementHolder(this)
                .setBoolean(island.isIgnored())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveLastTimeUpdate() {
        Query.ISLAND_SET_LAST_TIME_UPDATE.getStatementHolder(this)
                .setLong(island.getLastTimeUpdate())
                .setString(island.getOwner().getUniqueId() + "")
                .execute(true);
    }

    @Override
    public void saveBankLimit() {
        Query.ISLAND_SET_BANK_LIMIT.getStatementHolder(this)
                .setString(island.getBankLimit() + "")
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveBonusWorth() {
        Query.ISLAND_SET_BONUS_WORTH.getStatementHolder(this)
                .setString(island.getBonusWorth() + "")
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveBonusLevel() {
        Query.ISLAND_SET_BONUS_LEVEL.getStatementHolder(this)
                .setString(island.getBonusLevel() + "")
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveUpgrades() {
        Query.ISLAND_SET_UPGRADES.getStatementHolder(this)
                .setString(IslandSerializer.serializeUpgrades(island.getUpgrades()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveCropGrowth() {
        Query.ISLAND_SET_CROP_GROWTH.getStatementHolder(this)
                .setDouble(island.getCropGrowthMultiplier())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveSpawnerRates() {
        Query.ISLAND_SET_SPAWNER_RATES.getStatementHolder(this)
                .setDouble(island.getSpawnerRatesMultiplier())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveMobDrops() {
        Query.ISLAND_SET_MOB_DROPS.getStatementHolder(this)
                .setDouble(island.getMobDropsMultiplier())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveBlockLimits() {
        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveEntityLimits() {
        Query.ISLAND_SET_ENTITY_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveTeamLimit() {
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder(this)
                .setInt(island.getTeamLimit())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveWarpsLimit() {
        Query.ISLAND_SET_WARPS_LIMIT.getStatementHolder(this)
                .setInt(island.getWarpsLimit())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveIslandEffects() {
        Query.ISLAND_SET_ISLAND_EFFECTS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEffects(island.getPotionEffects()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveWarps() {
        Query.ISLAND_SET_WARPS.getStatementHolder(this)
                .setString(IslandSerializer.serializeWarps(island.getIslandWarps()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveRatings() {
        Query.ISLAND_SET_RATINGS.getStatementHolder(this)
                .setString(IslandSerializer.serializeRatings(island.getRatings()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveMissions() {
        Query.ISLAND_SET_MISSIONS.getStatementHolder(this)
                .setString(IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveSettings() {
        Query.ISLAND_SET_SETTINGS.getStatementHolder(this)
                .setString(IslandSerializer.serializeSettings(island.getAllSettings()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveGenerators() {
        Query.ISLAND_SET_GENERATOR.getStatementHolder(this)
                .setString(IslandSerializer.serializeGenerator(getIslandGenerators()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveGeneratedSchematics() {
        Query.ISLAND_SET_GENERATED_SCHEMATICS.getStatementHolder(this)
                .setString(island.getGeneratedSchematicsFlag() + "")
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveDirtyChunks() {
        Query.ISLAND_SET_DIRTY_CHUNKS.getStatementHolder(this)
                .setString(ChunksTracker.serialize(island))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveBlockCounts() {
        //Making sure there's no active modified flags.
        setFullUpdated(Query.ISLAND_SET_BLOCK_COUNTS);
        //Saving blocks.
        Query.ISLAND_SET_BLOCK_COUNTS.getStatementHolder(this)
                .setString(IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveIslandChest() {
        //Making sure there's no active modified flags.
        setFullUpdated(Query.ISLAND_SET_ISLAND_CHEST);
        //Saving chest.
        Query.ISLAND_SET_ISLAND_CHEST.getStatementHolder(this)
                .setString(IslandSerializer.serializeIslandChest(island.getChest()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveLastInterestTime() {
        Query.ISLAND_SET_LAST_INTEREST.getStatementHolder(this)
                .setLong(island.getLastInterestTime())
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void saveUniqueVisitors() {
        Query.ISLAND_SET_VISITORS.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes()))
                .setString(island.getOwner().getUniqueId().toString())
                .execute(true);
    }

    @Override
    public StatementHolder setUpdateStatement(StatementHolder statementHolder) {
        return statementHolder.setString(IslandSerializer.serializeLocations(island.getTeleportLocations()))
                .setString(LocationUtils.getLocation(island.getVisitorsLocation()))
                .setString(IslandSerializer.serializePlayers(island.getIslandMembers(false)))
                .setString(IslandSerializer.serializePlayers(island.getBannedPlayers()))
                .setString(IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions()))
                .setString(IslandSerializer.serializeUpgrades(island.getUpgrades()))
                .setString(IslandSerializer.serializeWarps(island.getIslandWarps()))
                .setString(island.getIslandBank().getBalance() + "")
                .setInt(island.getIslandSizeRaw())
                .setString(IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits()))
                .setInt(island.getTeamLimitRaw())
                .setFloat((float) island.getCropGrowthRaw())
                .setFloat((float) island.getSpawnerRatesRaw())
                .setFloat((float) island.getMobDropsRaw())
                .setString(island.getDiscord())
                .setString(island.getPaypal())
                .setInt(island.getWarpsLimitRaw())
                .setString(island.getBonusWorth() + "")
                .setBoolean(island.isLocked())
                .setString(IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
                .setString(island.getName())
                .setString(island.getDescription())
                .setString(IslandSerializer.serializeRatings(island.getRatings()))
                .setString(IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts()))
                .setString(IslandSerializer.serializeSettings(island.getAllSettings()))
                .setBoolean(island.isIgnored())
                .setString(IslandSerializer.serializeGenerator(getIslandGenerators()))
                .setString(island.getGeneratedSchematicsFlag() + "")
                .setString(island.getSchematicName())
                .setString(IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes()))
                .setString(island.getUnlockedWorldsFlag() + "")
                .setLong(island.getLastTimeUpdate())
                .setString(ChunksTracker.serialize(island))
                .setString(IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits()))
                .setString(island.getBonusLevel() + "")
                .setLong(island.getCreationTime())
                .setInt(island.getCoopLimitRaw())
                .setString(IslandSerializer.serializeEffects(island.getPotionEffects()))
                .setString(IslandSerializer.serializeIslandChest(island.getChest()))
                .setString(island.getUniqueId().toString())
                .setString(island.getBankLimitRaw() + "")
                .setLong(island.getLastInterestTime())
                .setString(island.getOwner().getUniqueId().toString());
    }

    @Override
    public void executeUpdateStatement(boolean async) {
        setUpdateStatement(Query.ISLAND_UPDATE.getStatementHolder(this)).execute(async);
    }

    @Override
    public void executeDeleteStatement(boolean async){
        Query.ISLAND_DELETE.getStatementHolder(this)
                .setString(island.getOwner().getUniqueId().toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async){
        Query.ISLAND_INSERT.getStatementHolder(this)
                .setString(island.getOwner().getUniqueId().toString())
                .setString(LocationUtils.getLocation(island.getCenter(World.Environment.NORMAL)))
                .setString(IslandSerializer.serializeLocations(island.getTeleportLocations()))
                .setString(IslandSerializer.serializePlayers(island.getIslandMembers(false)))
                .setString(IslandSerializer.serializePlayers(island.getBannedPlayers()))
                .setString(IslandSerializer.serializePermissions(island.getPlayerPermissions(), island.getRolePermissions()))
                .setString(IslandSerializer.serializeUpgrades(island.getUpgrades()))
                .setString(IslandSerializer.serializeWarps(island.getIslandWarps()))
                .setString(island.getIslandBank().getBalance() + "")
                .setInt(island.getIslandSizeRaw())
                .setString(IslandSerializer.serializeBlockLimits(island.getCustomBlocksLimits()))
                .setInt(island.getTeamLimitRaw())
                .setFloat((float) island.getCropGrowthRaw())
                .setFloat((float) island.getSpawnerRatesRaw())
                .setFloat((float) island.getMobDropsRaw())
                .setString(island.getDiscord())
                .setString(island.getPaypal())
                .setInt(island.getWarpsLimitRaw())
                .setString(island.getBonusWorth() + "")
                .setBoolean(island.isLocked())
                .setString(IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
                .setString(island.getName())
                .setString(LocationUtils.getLocation(island.getVisitorsLocation()))
                .setString(island.getDescription())
                .setString(IslandSerializer.serializeRatings(island.getRatings()))
                .setString(IslandSerializer.serializeMissions(island.getCompletedMissionsWithAmounts()))
                .setString(IslandSerializer.serializeSettings(island.getAllSettings()))
                .setBoolean(island.isIgnored())
                .setString(IslandSerializer.serializeGenerator(getIslandGenerators()))
                .setString(island.getGeneratedSchematicsFlag() + "")
                .setString(island.getSchematicName())
                .setString(IslandSerializer.serializePlayersWithTimes(island.getUniqueVisitorsWithTimes()))
                .setString(island.getUnlockedWorldsFlag() + "")
                .setLong(island.getLastTimeUpdate())
                .setString(ChunksTracker.serialize(island))
                .setString(IslandSerializer.serializeEntityLimits(island.getCustomEntitiesLimits()))
                .setString(island.getBonusLevel() + "")
                .setLong(island.getCreationTime())
                .setInt(island.getCoopLimitRaw())
                .setString(IslandSerializer.serializeEffects(island.getPotionEffects()))
                .setString(IslandSerializer.serializeIslandChest(island.getChest()))
                .setString(island.getUniqueId().toString())
                .setString(island.getBankLimitRaw() + "")
                .setLong(island.getLastInterestTime())
                .execute(async);
    }

    private Map<Key, Integer>[] getIslandGenerators(){
        Map<Key, Integer>[] customGeneratorAmounts = new Map[3];
        for(World.Environment environment : World.Environment.values())
            customGeneratorAmounts[environment.ordinal()] = island.getCustomGeneratorAmounts(environment);
        return customGeneratorAmounts;
    }

}
