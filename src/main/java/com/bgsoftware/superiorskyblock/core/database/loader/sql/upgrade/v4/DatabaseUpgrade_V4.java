package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v4;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.transaction.IDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.sql.DBSession;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.island.IslandNames;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V4 implements Runnable {

    public static final DatabaseUpgrade_V4 INSTANCE = new DatabaseUpgrade_V4();

    private DatabaseUpgrade_V4() {

    }

    @Override
    public void run() {
        updateWarps("islands_warps", "warp", false);
        updateWarps("islands_warp_categories", "warp category", true);
    }

    private static void updateWarps(String tableName, String itemType, boolean isCategoryUpdate) {
        List<UpdateNameItem> itemsToUpdateNames = new LinkedList<>();

        MutableBoolean isFailed = new MutableBoolean(false);
        Set<String> existingNames = new HashSet<>();

        DBSession.select(tableName, "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));

                String uuid = databaseResult.getString("island").orElse(null);
                if (uuid == null)
                    continue;

                String name = databaseResult.getString("name").orElse(null);
                if (name == null) {
                    // Null items are deleted
                    continue;
                }

                String newName;

                if (!IslandNames.isWarpNameLengthValid(name)) {
                    newName = name.substring(0, IslandNames.getMaxWarpNameLength());
                } else if (name.contains(" ")) {
                    newName = name.replace(" ", "_");
                } else if (name.length() != Formatters.STRIP_COLOR_FORMATTER.format(name).length()) {
                    newName = Formatters.STRIP_COLOR_FORMATTER.format(name);
                } else {
                    existingNames.add(name);
                    continue;
                }

                itemsToUpdateNames.add(new UpdateNameItem(uuid, name, newName));
            }
        }).onFail(error -> isFailed.set(true)));

        if (isFailed.get() || itemsToUpdateNames.isEmpty())
            return;

        List<DeleteNameItem> itemsToDelete = new LinkedList<>();

        Iterator<UpdateNameItem> iterator = itemsToUpdateNames.iterator();
        while (iterator.hasNext()) {
            UpdateNameItem updateNameItem = iterator.next();
            String newName = updateNameItem.newName;
            int counter = 0;
            while (Text.isBlank(newName) || existingNames.contains(newName)) {
                newName = updateNameItem.newName + (counter++);
                if (!IslandNames.isWarpNameLengthValid(newName)) {
                    // Couldn't find a valid name. Removing this warp + warning in console
                    Log.warn("Could not find a suitable name for the ", itemType, " ", updateNameItem.oldName, " - removing and continuing...");
                    itemsToDelete.add(new DeleteNameItem(updateNameItem.uuid, updateNameItem.oldName));
                    iterator.remove();
                    break;
                }
            }
            existingNames.add(newName);
            updateNameItem.newName = newName;
        }

        List<IDatabaseTransaction> transactionsList = new LinkedList<>();

        CustomSQLDatabaseTransaction deleteTransactionNullNames = new CustomSQLDatabaseTransaction(
                "DELETE FROM {prefix}" + tableName + " WHERE name IS NULL");
        transactionsList.add(deleteTransactionNullNames);

        if (!itemsToDelete.isEmpty()) {
            CustomSQLDatabaseTransaction deleteTransaction = new CustomSQLDatabaseTransaction(
                    "DELETE FROM {prefix}" + tableName + " WHERE uuid=? AND name=?");
            deleteManyItems(itemsToDelete, deleteTransaction);
            transactionsList.add(deleteTransaction);
        }

        if (!itemsToUpdateNames.isEmpty()) {
            CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
                    "UPDATE {prefix}" + tableName + " SET name=? WHERE uuid=? AND name=?");
            updateManyItems(itemsToUpdateNames, updateTransaction);
            transactionsList.add(updateTransaction);

            if (isCategoryUpdate) {
                // Update category names for warps as well
                CustomSQLDatabaseTransaction categoryUpdateTransaction = new CustomSQLDatabaseTransaction(
                        "UPDATE {prefix}islands_warps SET category=? WHERE uuid=? AND category=?");
                updateManyItems(itemsToUpdateNames, categoryUpdateTransaction);
                transactionsList.add(categoryUpdateTransaction);
            }
        }

        try {
            DBSession.execute(transactionsList).get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static void deleteManyItems(List<DeleteNameItem> items, CustomSQLDatabaseTransaction deleteTransaction) {
        for (DeleteNameItem item : items) {
            deleteTransaction
                    .bindObject(item.uuid)
                    .bindObject(item.name)
                    .newBatch();
        }
    }

    private static void updateManyItems(List<UpdateNameItem> items, CustomSQLDatabaseTransaction updateTransaction) {
        for (UpdateNameItem item : items) {
            updateTransaction
                    .bindObject(item.newName)
                    .bindObject(item.uuid)
                    .bindObject(item.oldName)
                    .newBatch();
        }
    }

    private static class DeleteNameItem {

        private final String uuid;
        @Nullable
        private final String name;

        DeleteNameItem(String uuid, @Nullable String name) {
            this.uuid = uuid;
            this.name = name;
        }

    }

    private static class UpdateNameItem {

        private final String uuid;
        private final String oldName;
        private String newName;

        UpdateNameItem(String uuid, String oldName, String newName) {
            this.uuid = uuid;
            this.oldName = oldName;
            this.newName = newName;
        }

    }

}
