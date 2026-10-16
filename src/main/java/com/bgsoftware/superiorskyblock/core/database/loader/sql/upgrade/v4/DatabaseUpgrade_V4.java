package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v4;

import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V4 implements Runnable {

    public static final DatabaseUpgrade_V4 INSTANCE = new DatabaseUpgrade_V4();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private DatabaseUpgrade_V4() {

    }

    @Override
    public void run() {
        List<IDatabaseTransaction> databaseTransactions = new ArrayList<>();

        updateIslandsSettingsSyncedValue(databaseTransactions);
        deleteSyncedRecords(databaseTransactions, "islands_block_limits");
        deleteSyncedRecords(databaseTransactions, "islands_entity_limits");
        convertDefaultValuesToSynced(databaseTransactions);

        try {
            DBSession.execute(databaseTransactions).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static void updateIslandsSettingsSyncedValue(List<IDatabaseTransaction> databaseTransactions) {
        databaseTransactions.add(updateColumnToSynced("size", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("coops_limit", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("members_limit", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("warps_limit", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("crop_growth_multiplier", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("spawner_rates_multiplier", IslandUpgradeConstants.NO_LIMIT_VALUE));
        databaseTransactions.add(updateColumnToSynced("mob_drops_multiplier", IslandUpgradeConstants.NO_LIMIT_VALUE));
    }

    private static void deleteSyncedRecords(List<IDatabaseTransaction> databaseTransactions, String table) {
        databaseTransactions.add(new CustomSQLDatabaseTransaction(
                "DELETE FROM {prefix}" + table + " WHERE `limit`<=?"
        ).bindObject(IslandUpgradeConstants.NO_LIMIT_VALUE));
    }

    /*
     * Old versions saved the default values of the config instead of a synced value.
     * They used to be converted in memory on every island load (SIsland#updateOldUpgradeValues), which also turned
     * custom values that are equal to the default values into synced values after every restart.
     * The conversion is now done once, and saved to the database.
     */
    private static void convertDefaultValuesToSynced(List<IDatabaseTransaction> databaseTransactions) {
        SettingsManager.DefaultValues defaultValues = plugin.getSettings().getDefaultValues();

        databaseTransactions.add(updateColumnToSynced("size", defaultValues.getIslandSize()));
        databaseTransactions.add(updateColumnToSynced("coops_limit", defaultValues.getCoopLimit()));
        databaseTransactions.add(updateColumnToSynced("members_limit", defaultValues.getTeamLimit()));
        databaseTransactions.add(updateColumnToSynced("warps_limit", defaultValues.getWarpsLimit()));
        databaseTransactions.add(updateColumnToSynced("crop_growth_multiplier", defaultValues.getCropGrowth()));
        databaseTransactions.add(updateColumnToSynced("spawner_rates_multiplier", defaultValues.getSpawnerRates()));
        databaseTransactions.add(updateColumnToSynced("mob_drops_multiplier", defaultValues.getMobDrops()));

        deleteDefaultLimits(databaseTransactions, "islands_block_limits", "block", defaultValues.getBlockLimits());
        deleteDefaultLimits(databaseTransactions, "islands_entity_limits", "entity", defaultValues.getEntityLimits());

        for (Map.Entry<Dimension, Map<Key, Integer>> entry : defaultValues.getGeneratorsMap().entrySet()) {
            Map<Key, Integer> defaultGeneratorRates = entry.getValue();

            if (defaultGeneratorRates == null || defaultGeneratorRates.isEmpty())
                continue;

            CustomSQLDatabaseTransaction deleteTransaction = new CustomSQLDatabaseTransaction(
                    "DELETE FROM {prefix}islands_generators WHERE environment=? AND block=? AND rate=?");

            defaultGeneratorRates.forEach((block, rate) -> deleteTransaction
                    .bindObject(entry.getKey().getName())
                    .bindObject(block.toString())
                    .bindObject(rate)
                    .newBatch());

            databaseTransactions.add(deleteTransaction);
        }
    }

    private static void deleteDefaultLimits(List<IDatabaseTransaction> databaseTransactions, String table,
                                            String keyColumn, Map<Key, Integer> defaultLimits) {
        if (defaultLimits.isEmpty())
            return;

        CustomSQLDatabaseTransaction deleteTransaction = new CustomSQLDatabaseTransaction(
                "DELETE FROM {prefix}" + table + " WHERE " + keyColumn + "=? AND `limit`=?");

        defaultLimits.forEach((key, limit) -> deleteTransaction
                .bindObject(key.toString())
                .bindObject(limit)
                .newBatch());

        databaseTransactions.add(deleteTransaction);
    }

    private static IDatabaseTransaction updateColumnToSynced(String column, Object value) {
        return new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_settings SET " + column + "=? WHERE " + column + "=?"
        ).bindObject(IslandUpgradeConstants.SYNCED_VALUE).bindObject(value);
    }

}
