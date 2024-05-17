package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0;

import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;

public class DatabaseUpgrade_V0 implements Runnable {

    public static final DatabaseUpgrade_V0 INSTANCE = new DatabaseUpgrade_V0();

    private DatabaseUpgrade_V0() {

    }

    @Override
    public void run() {
        upgradeIslandsTables();
        upgradePlayersTables();
        upgradeStackedBlocksTables();
        DatabaseConverter.tryConvertDatabase();
    }

    private void upgradeIslandsTables() {
        SQLHelper.modifyColumnType("islands", "dirty_chunks", "LONGTEXT");
        SQLHelper.modifyColumnType("islands", "block_counts", "LONGTEXT");

        SQLHelper.modifyColumnType("islands_chests", "contents", "LONGBLOB");

        SQLHelper.modifyColumnType("islands_missions", "name", "LONG_UNIQUE_TEXT");

        // Up to 1.9.0.574, decimals would not be saved correctly in MySQL
        // This occurred because the field type was DECIMAL(10,0) instead of DECIMAL(10,2)
        // Updating the column types to "DECIMAL" again should fix the issue.
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1021
        SQLHelper.modifyColumnType("islands_settings", "crop_growth_multiplier", "DECIMAL");
        SQLHelper.modifyColumnType("islands_settings", "spawner_rates_multiplier", "DECIMAL");
        SQLHelper.modifyColumnType("islands_settings", "mob_drops_multiplier", "DECIMAL");

        SQLHelper.modifyColumnType("islands_upgrades", "upgrade", "LONG_UNIQUE_TEXT");
        SQLHelper.removePrimaryKey("islands_upgrades", "island");

        SQLHelper.modifyColumnType("islands_warp_categories", "name", "LONG_UNIQUE_TEXT");

        SQLHelper.modifyColumnType("islands_warps", "name", "LONG_UNIQUE_TEXT");
    }

    private void upgradePlayersTables() {
        SQLHelper.modifyColumnType("players_missions", "name", "LONG_UNIQUE_TEXT");
    }

    private void upgradeStackedBlocksTables() {
        // Before v1.8.1.363, location column of stacked_blocks was limited to 30 chars.
        // In order to make sure all tables keep the large number, we modify the column to 255-chars long
        // each time the plugin attempts to create the table.
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/730
        SQLHelper.modifyColumnType("stacked_blocks", "location", "LONG_UNIQUE_TEXT");
    }

}
