package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v5;

public class DatabaseUpgrade_V5 implements Runnable {

    public static final DatabaseUpgrade_V5 INSTANCE = new DatabaseUpgrade_V5();

    private DatabaseUpgrade_V5() {

    }

    @Override
    public void run() {
        /*
         * Block counts are now saved as individual counts in a JSON object, instead of the aggregated counts
         * in a JSON array. This upgrade does nothing; it only bumps the database version, so older versions of
         * the plugin refuse to load the database instead of failing to read the new block counts.
         * Legacy block counts are converted when their island is loaded (SIsland, LegacyBlockCounts), as the
         * conversion depends on block values and island limits, which are not available during upgrades.
         */
    }

}
