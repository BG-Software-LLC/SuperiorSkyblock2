package com.bgsoftware.superiorskyblock.core.database;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedPlayerInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.database.loader.DatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.DatabaseLoader_V1;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsDeserializer;
import com.bgsoftware.superiorskyblock.core.database.serialization.PlayersDeserializer;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLDatabaseInitializer;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
public class DataManager extends Manager {

    private final List<DatabaseLoader> databaseLoaders = new LinkedList<>();

    public DataManager(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() throws ManagerLoadException {
        loadDatabaseLoaders();

        for (DatabaseLoader databaseLoader : databaseLoaders) {
            try {
                databaseLoader.loadData();
            } catch (Exception error) {
                throw new ManagerLoadException(error, "&cUnexpected error occurred while converting data:", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }

        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().init(plugin);
        }

        for (DatabaseLoader databaseLoader : databaseLoaders) {
            try {
                databaseLoader.saveData();
            } catch (Exception error) {
                throw new ManagerLoadException(error, "&cUnexpected error occurred while saving data:", ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
            }
        }

        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().createIndexes();
            SQLHelper.setJournalMode("MEMORY", QueryResult.EMPTY_QUERY_RESULT);
        }

        loadPlayers();
        loadIslands();
        loadGrid();

        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLHelper.setJournalMode("DELETE", QueryResult.EMPTY_QUERY_RESULT);
        }

        /*
         *  Because of a bug caused leaders to be guests, I am looping through all the players and trying to fix it here.
         */

        for (SuperiorPlayer superiorPlayer : plugin.getPlayers().getAllPlayers()) {
            if (superiorPlayer.getIslandLeader().getUniqueId().equals(superiorPlayer.getUniqueId()) &&
                    superiorPlayer.hasIsland() && !superiorPlayer.getPlayerRole().isLastRole()) {
                SuperiorSkyblockPlugin.log("[WARN] Seems like " + superiorPlayer.getName() + " is an island leader, but have a guest role - fixing it...");
                superiorPlayer.setPlayerRole(SPlayerRole.lastRole());
            }
        }
    }

    public void addDatabaseLoader(DatabaseLoader databaseLoader) {
        this.databaseLoaders.add(databaseLoader);
    }

    public void saveDatabase(boolean async) {
        if (async && Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> saveDatabase(false));
            return;
        }

        try {
            //Saving grid
            GridDatabaseBridge.deleteGrid(plugin.getGrid());
            GridDatabaseBridge.insertGrid(plugin.getGrid());
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    public void closeConnection() {
        if (!plugin.getFactory().hasCustomDatabaseBridge()) {
            SQLDatabaseInitializer.getInstance().close();
        }
    }

    private void loadDatabaseLoaders() {
        DatabaseLoader_V1.register(this);
    }

    private void loadPlayers() {
        SuperiorSkyblockPlugin.log("Starting to load players...");

        DatabaseBridge playersLoader = plugin.getFactory().createDatabaseBridge((SuperiorPlayer) null);

        DatabaseCache<CachedPlayerInfo> databaseCache = new DatabaseCache<>();
        AtomicInteger playersCount = new AtomicInteger();
        long startTime = System.currentTimeMillis();

        PlayersDeserializer.deserializeMissions(playersLoader, databaseCache);
        PlayersDeserializer.deserializePlayerSettings(playersLoader, databaseCache);
        PlayersDeserializer.deserializePersistentDataContainer(playersLoader, databaseCache);

        playersLoader.loadAllObjects("players", resultSet -> {
            plugin.getPlayers().loadPlayer(databaseCache, new DatabaseResult(resultSet));
            playersCount.incrementAndGet();
        });

        long endTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("Finished loading " + playersCount.get() + " players (Took " + (endTime - startTime) + "ms)");
    }

    private void loadIslands() {
        SuperiorSkyblockPlugin.log("Starting to load islands...");

        DatabaseBridge islandsLoader = plugin.getFactory().createDatabaseBridge((Island) null);

        DatabaseCache<CachedIslandInfo> databaseCache = new DatabaseCache<>();
        long startTime = System.currentTimeMillis();

        List<CachedIslandInfo> islands = new ArrayList<>();

        IslandsDeserializer.deserializeIslandHomes(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeMembers(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBanned(islandsLoader, databaseCache);
        IslandsDeserializer.deserializePlayerPermissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRolePermissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeUpgrades(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeWarps(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBlockLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRatings(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeMissions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandFlags(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeGenerators(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeVisitors(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeEntityLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeEffects(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandChest(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeRoleLimits(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeWarpCategories(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandBank(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeVisitorHomes(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeIslandSettings(islandsLoader, databaseCache);
        IslandsDeserializer.deserializeBankTransactions(islandsLoader, databaseCache);
        IslandsDeserializer.deserializePersistentDataContainer(islandsLoader, databaseCache);

        islandsLoader.loadAllObjects("islands", resultSet -> {
            DatabaseResult databaseResult = new DatabaseResult(resultSet);

            Optional<UUID> uuid = databaseResult.getUUID("uuid");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island with invalid uuid, skipping...");
                return;
            }

            Optional<UUID> owner = databaseResult.getUUID("owner");
            if (!owner.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island with invalid owner uuid, skipping...");
                return;
            }

            Optional<Location> center = databaseResult.getString("center").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!center.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island with invalid center, skipping...");
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);

            long currentTime = System.currentTimeMillis() / 1000L;

            cachedIslandInfo.owner = owner.get();
            cachedIslandInfo.center = center.get();
            cachedIslandInfo.name = databaseResult.getString("name").orElse("");
            cachedIslandInfo.islandType = databaseResult.getString("island_type").orElse(null);
            cachedIslandInfo.creationTime = databaseResult.getLong("creation_time").orElse(currentTime);
            cachedIslandInfo.discord = databaseResult.getString("discord").orElse("None");
            cachedIslandInfo.paypal = databaseResult.getString("paypal").orElse("None");
            cachedIslandInfo.bonusWorth = databaseResult.getBigDecimal("worth_bonus").orElse(BigDecimal.ZERO);
            cachedIslandInfo.bonusLevel = databaseResult.getBigDecimal("levels_bonus").orElse(BigDecimal.ZERO);
            cachedIslandInfo.isLocked = databaseResult.getBoolean("locked").orElse(false);
            cachedIslandInfo.isTopIslandsIgnored = databaseResult.getBoolean("ignored").orElse(false);
            cachedIslandInfo.description = databaseResult.getString("description").orElse("");
            cachedIslandInfo.generatedSchematics = databaseResult.getInt("generated_schematics").orElse(0);
            cachedIslandInfo.unlockedWorlds = databaseResult.getInt("unlocked_worlds").orElse(0);
            cachedIslandInfo.lastTimeUpdated = databaseResult.getLong("last_time_updated").orElse(currentTime);
            cachedIslandInfo.dirtyChunks = ChunksTracker.deserialize(databaseResult.getString("dirty_chunks").orElse(null));
            cachedIslandInfo.blockCounts = IslandsDeserializer.deserializeBlockCounts(databaseResult.getString("block_counts").orElse(null));

            islands.add(cachedIslandInfo);
        });

        System.out.println(System.currentTimeMillis() - startTime);

        plugin.getGrid().loadCache(islands);

        long endTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("Finished loading " + islands.size() + " islands (Took " + (endTime - startTime) + "ms)");
    }


    private void loadGrid() {
        SuperiorSkyblockPlugin.log("Starting to load grid...");

        DatabaseBridge gridLoader = plugin.getFactory().createDatabaseBridge((GridManager) null);

        gridLoader.loadAllObjects("grid",
                resultSet -> plugin.getGrid().loadGrid(new DatabaseResult(resultSet)));

        SuperiorSkyblockPlugin.log("Finished grid!");
    }

}
