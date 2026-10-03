package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v4;

import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;

import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V4 implements Runnable {

    public static final DatabaseUpgrade_V4 INSTANCE = new DatabaseUpgrade_V4();

    private DatabaseUpgrade_V4() {

    }

    @Override
    public void run() {
        updateColumn("size");
        updateColumn("coops_limit");
        updateColumn("members_limit");
        updateColumn("warps_limit");
        updateColumn("crop_growth_multiplier");
        updateColumn("spawner_rates_multiplier");
        updateColumn("mob_drops_multiplier");
    }

    private static void updateColumn(String column) {
        CustomSQLDatabaseTransaction transaction = new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_settings SET " + column + "=? WHERE " + column + "=-1"
        ).bindObject(IslandUpgradeConstants.SYNCED_VALUE);

        try {
            DBSession.execute(transaction).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

}
