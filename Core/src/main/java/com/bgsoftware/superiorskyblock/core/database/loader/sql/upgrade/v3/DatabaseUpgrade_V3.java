package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v3;

import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V3 implements Runnable {

    public static final DatabaseUpgrade_V3 INSTANCE = new DatabaseUpgrade_V3();

    private DatabaseUpgrade_V3() {

    }

    @Override
    public void run() {
        List<DatabaseItem> itemsToUpdate = new LinkedList<>();

        MutableBoolean isFailed = new MutableBoolean(false);

        DBSession.select("islands", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));

                String uuid = databaseResult.getString("uuid").orElse(null);
                if (uuid == null)
                    continue;

                Integer generatedSchematics = databaseResult.getInt("generated_schematics").orElse(null);
                if (generatedSchematics == null)
                    continue;

                Integer unlockedWorlds = databaseResult.getInt("unlocked_worlds").orElse(null);
                if (unlockedWorlds == null)
                    continue;

                itemsToUpdate.add(new DatabaseItem(uuid, convertGeneratedSchematicsMask(generatedSchematics),
                        convertUnlockedWorldsMask(unlockedWorlds)));
            }
        }).onFail(error -> isFailed.set(true)));

        if (isFailed.get())
            return;

        DBSession.modifyColumnType("islands", "generated_schematics", "TEXT");
        DBSession.modifyColumnType("islands", "unlocked_worlds", "TEXT");

        CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands SET generated_schematics=?,unlocked_worlds=? WHERE uuid=?");
        updateManyItems(itemsToUpdate, updateTransaction);
    }

    private static String convertGeneratedSchematicsMask(int generatedSchematicsMask) {
        StringBuilder generatedSchematics = new StringBuilder();

        if ((generatedSchematicsMask & 8) == 8)
            generatedSchematics.append(",NORMAL");

        if ((generatedSchematicsMask & 4) == 4)
            generatedSchematics.append(",NETHER");

        if ((generatedSchematicsMask & 3) == 3)
            generatedSchematics.append(",THE_END");

        return generatedSchematics.length() == 0 ? generatedSchematics.toString() : generatedSchematics.substring(1);
    }

    private static String convertUnlockedWorldsMask(int unlockedWorldsMask) {
        StringBuilder unlockedWorlds = new StringBuilder();

        if ((unlockedWorldsMask & 4) == 4)
            unlockedWorlds.append(",NORMAL");

        if ((unlockedWorldsMask & 1) == 1)
            unlockedWorlds.append(",NETHER");

        if ((unlockedWorldsMask & 2) == 2)
            unlockedWorlds.append(",THE_END");

        return unlockedWorlds.length() == 0 ? unlockedWorlds.toString() : unlockedWorlds.substring(1);
    }

    private static void updateManyItems(List<DatabaseItem> items, CustomSQLDatabaseTransaction updateTransaction) {
        for (DatabaseItem item : items) {
            updateTransaction
                    .bindObject(item.generatedWorlds)
                    .bindObject(item.unlockedWorlds)
                    .bindObject(item.uuid)
                    .newBatch();
        }

        try {
            DBSession.execute(updateTransaction).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static class DatabaseItem {

        private final String uuid;
        private final String generatedWorlds;
        private final String unlockedWorlds;

        DatabaseItem(String uuid, String generatedWorlds, String unlockedWorlds) {
            this.uuid = uuid;
            this.generatedWorlds = generatedWorlds;
            this.unlockedWorlds = unlockedWorlds;
        }

    }

}
