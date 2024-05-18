package com.bgsoftware.superiorskyblock.core.database.loader.sql;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.database.bridge.GridDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.loader.MachineStateDatabaseLoader;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;

import java.sql.ResultSet;

public class SQLDatabaseLoader extends MachineStateDatabaseLoader {

    private final SuperiorSkyblockPlugin plugin;

    public SQLDatabaseLoader(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setState(State state) throws ManagerLoadException {
        if (!plugin.getFactory().hasCustomDatabaseBridge())
            super.setState(state);
    }

    @Override
    protected void handleInitialize() throws ManagerLoadException {
        if (!SQLHelper.createConnection(plugin)) {
            throw new ManagerLoadException("Couldn't connect to the database.\nMake sure all information is correct.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        try {
            SQLHelper.select("ssb_metadata", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
                int databaseVersion = resultSet.getInt("version");

                if (databaseVersion > SQLDatabase.getCurrentDatabaseVersion())
                    throw new IllegalStateException("Database is in newer version: " + databaseVersion + " > " + SQLDatabase.getCurrentDatabaseVersion());
            }).onFail(error -> {
            }));
        } catch (IllegalStateException error) {
            throw new ManagerLoadException(error.getMessage(), ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        SQLDatabase.UpgradeResult databaseUpgradeResult = SQLDatabase.upgradeDatabase();

        SQLDatabase.initializeDatabase();

        if (databaseUpgradeResult.isUpgraded())
            GridDatabaseBridge.updateVersion(plugin.getGrid(), databaseUpgradeResult.getDatabaseVersion());

        SQLHelper.select("grid", "", new QueryResult<ResultSet>()
                .onFail(error -> GridDatabaseBridge.insertGrid(plugin.getGrid())));
    }

    @Override
    protected void handlePostInitialize() {
        SQLHelper.createIndex("islands_bans_index", "islands_bans",
                "island", "player");

        SQLHelper.createIndex("block_limits_index", "islands_block_limits",
                "island", "block");

        SQLHelper.createIndex("islands_chests_index", "islands_chests",
                "island", "`index`");

        SQLHelper.createIndex("islands_effects_index", "islands_effects",
                "island", "effect_type");

        SQLHelper.createIndex("entity_limits_index", "islands_entity_limits",
                "island", "entity");

        SQLHelper.createIndex("islands_flags_index", "islands_flags",
                "island", "name");

        SQLHelper.createIndex("islands_generators_index", "islands_generators",
                "island", "environment", "block");

        SQLHelper.createIndex("islands_homes_index", "islands_homes",
                "island", "environment");

        SQLHelper.createIndex("islands_members_index", "islands_members",
                "island", "player");

        SQLHelper.createIndex("islands_missions_index", "islands_missions",
                "island", "name");

        SQLHelper.createIndex("player_permissions_index", "islands_player_permissions",
                "island", "player", "permission");

        SQLHelper.createIndex("islands_ratings_index", "islands_ratings",
                "island", "player");

        SQLHelper.createIndex("role_limits_index", "islands_role_limits",
                "island", "role");

        SQLHelper.createIndex("role_permissions_index", "islands_role_permissions",
                "island", "permission");

        SQLHelper.createIndex("islands_upgrades_index", "islands_upgrades",
                "island", "upgrade");

        SQLHelper.createIndex("visitor_homes_index", "islands_visitor_homes",
                "island", "environment");

        SQLHelper.createIndex("islands_visitors_index", "islands_visitors",
                "island", "player");

        SQLHelper.createIndex("warp_categories_index", "islands_warp_categories",
                "island", "name");

        SQLHelper.createIndex("islands_warps_index", "islands_warps",
                "island", "name");

        SQLHelper.createIndex("players_missions_index", "players_missions",
                "player", "name");
    }

    @Override
    protected void handlePreLoadData() {
        SQLHelper.setJournalMode("MEMORY", QueryResult.EMPTY_QUERY_RESULT);
    }

    @Override
    protected void handlePostLoadData() {
        SQLHelper.setJournalMode("DELETE", QueryResult.EMPTY_QUERY_RESULT);
    }

    @Override
    protected void handleShutdown() {
        SQLHelper.close();
    }

}
