package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v5;

import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v3.DatabaseUpgrade_V3;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V5 implements Runnable {

    public static final DatabaseUpgrade_V5 INSTANCE = new DatabaseUpgrade_V5();

    private DatabaseUpgrade_V5() {

    }

    @Override
    public void run() {
        Map<String, Long> islandIds = transformTable("islands", "uuid");
        Log.info("islandIds: ", islandIds);


        Map<String, Long> playerIds = transformTable("players", "uuid");
        Log.info("playerIds: ", playerIds);
    }

    private static Map<String, Long> transformTable(String tableName, String column) {
        Map<String, Long> idsMap = new HashMap<>();

        DBSession.addColumn(tableName, "rid", "AUTOINCREMENT_TYPE");
        Log.info("Add rid to ", tableName);

//        MutableBoolean isFailed = new MutableBoolean(false);
//
//        DBSession.select(tableName, "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
//            long idCounter = 0;
//
//            while (resultSet.next()) {
//                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));
//
//                String uuid = databaseResult.getString(column).orElse(null);
//                if (uuid == null)
//                    continue;
//
//                idsMap.put(uuid, idCounter++);
//            }
//        }).onFail(error -> isFailed.set(true)));
//
//        if (isFailed.get())
//            return Collections.emptyMap();
//
//        DBSession.addColumn(tableName, "id", "BIGINT");
//
//        CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
//                "UPDATE {prefix}" + tableName + " SET id=? WHERE " + column + "=?");
//        idsMap.forEach((uuid, id) -> {
//            updateTransaction.bindObject(id).bindObject(uuid).newBatch();
//        });
//
//        try {
//            DBSession.execute(updateTransaction).get();
//        } catch (InterruptedException | ExecutionException ignored) {
//        }

        return idsMap;
    }

    private static void convertTable(String tableName, String column, Map<String, Long> idsMap) {

    }

}
