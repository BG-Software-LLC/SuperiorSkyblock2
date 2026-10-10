package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v4;

import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V4 implements Runnable {

    public static final DatabaseUpgrade_V4 INSTANCE = new DatabaseUpgrade_V4();

    private DatabaseUpgrade_V4() {

    }

    @Override
    public void run() {
        updateIslandsSettingsSyncedValue();
        deleteSyncedRecords("islands_block_limits");
        deleteSyncedRecords("islands_entity_limits");
    }

    private static void updateIslandsSettingsSyncedValue() {
        List<IDatabaseTransaction> databaseTransactions = new ArrayList<>();

        databaseTransactions.add(updateColumn("size"));
        databaseTransactions.add(updateColumn("coops_limit"));
        databaseTransactions.add(updateColumn("members_limit"));
        databaseTransactions.add(updateColumn("warps_limit"));
        databaseTransactions.add(updateColumn("crop_growth_multiplier"));
        databaseTransactions.add(updateColumn("spawner_rates_multiplier"));
        databaseTransactions.add(updateColumn("mob_drops_multiplier"));

        try {
            DBSession.execute(databaseTransactions).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static IDatabaseTransaction updateColumn(String column) {
        return new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_settings SET " + column + "=? WHERE " + column + "=-1"
        ).bindObject(IslandUpgradeConstants.SYNCED_VALUE);
    }

    private static void deleteSyncedRecords(String table) {
        try {
            DBSession.execute(new CustomSQLDatabaseTransaction(
                    "DELETE FROM {prefix}" + table + " WHERE limit<=-1")).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

}
