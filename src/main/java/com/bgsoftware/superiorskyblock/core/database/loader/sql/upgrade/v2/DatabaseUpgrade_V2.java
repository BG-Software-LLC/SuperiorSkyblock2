package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v2;

import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.sql.ResultSetMapBridge;
import com.bgsoftware.superiorskyblock.core.database.sql.SQLHelper;
import com.bgsoftware.superiorskyblock.core.database.sql.session.QueryResult;
import com.bgsoftware.superiorskyblock.core.database.sql.transaction.CustomSQLDatabaseTransaction;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DatabaseUpgrade_V2 implements Runnable {

    public static final DatabaseUpgrade_V2 INSTANCE = new DatabaseUpgrade_V2();

    private DatabaseUpgrade_V2() {

    }

    @Override
    public void run() {
        // Update all item stacks saved to DB to the new serialization
        updateWarpsIcons();
        updateWarpCategoriesIcons();
        updateIslandChests();
    }

    private static void updateWarpsIcons() {
        List<DatabaseItem> warpIcons = new LinkedList<>();

        MutableBoolean isFailed = new MutableBoolean(false);

        SQLHelper.select("islands_warps", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));

                String island = databaseResult.getString("island").orElse(null);
                if (island == null)
                    continue;

                String name = databaseResult.getString("name").orElse(null);
                if (name == null)
                    continue;

                String serializedIcon = databaseResult.getString("icon").orElse(null);
                if (serializedIcon == null)
                    continue;

                ItemStack icon = Serializers.ITEM_STACK_SERIALIZER.deserialize(serializedIcon);
                warpIcons.add(new DatabaseItem(icon, island, name));
            }
        }).onFail(error -> isFailed.set(true)));

        if (isFailed.get())
            return;

        CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_warps SET icon=? WHERE island=? AND name=?");
        updateManyItems(warpIcons, updateTransaction);
    }

    private static void updateWarpCategoriesIcons() {
        List<DatabaseItem> categoryIcons = new LinkedList<>();

        MutableBoolean isFailed = new MutableBoolean(false);

        SQLHelper.select("islands_warp_categories", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));

                String island = databaseResult.getString("island").orElse(null);
                if (island == null)
                    continue;

                String name = databaseResult.getString("name").orElse(null);
                if (name == null)
                    continue;

                String serializedIcon = databaseResult.getString("icon").orElse(null);
                if (serializedIcon == null)
                    continue;

                ItemStack icon = Serializers.ITEM_STACK_SERIALIZER.deserialize(serializedIcon);
                categoryIcons.add(new DatabaseItem(icon, island, name));
            }
        }).onFail(error -> isFailed.set(true)));

        if (isFailed.get())
            return;

        CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_warp_categories SET icon=? WHERE island=? AND name=?");
        updateManyItems(categoryIcons, updateTransaction);
    }

    private static void updateIslandChests() {
        List<DatabaseChest> islandContents = new LinkedList<>();

        MutableBoolean isFailed = new MutableBoolean(false);

        SQLHelper.select("islands_chests", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                DatabaseResult databaseResult = new DatabaseResult(new ResultSetMapBridge(resultSet));

                String island = databaseResult.getString("island").orElse(null);
                if (island == null)
                    continue;

                Integer index = databaseResult.getInt("index").orElse(null);
                if (index == null)
                    continue;

                byte[] serializedContents = databaseResult.getBlob("contents").orElse(null);
                if (serializedContents == null)
                    continue;

                ItemStack[] contents = Serializers.INVENTORY_SERIALIZER.deserialize(serializedContents);
                islandContents.add(new DatabaseChest(island, index, contents));
            }
        }).onFail(error -> isFailed.set(true)));

        if (isFailed.get())
            return;

        CustomSQLDatabaseTransaction updateTransaction = new CustomSQLDatabaseTransaction(
                "UPDATE {prefix}islands_chests SET contents=? WHERE island=? AND `index`=?");

        for (DatabaseChest chestData : islandContents) {
            updateTransaction
                    .bindObject(Serializers.INVENTORY_SERIALIZER.serialize(chestData.contents))
                    .bindObject(chestData.island)
                    .bindObject(chestData.index)
                    .newBatch();
        }

        try {
            updateTransaction.execute().get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static void updateManyItems(List<DatabaseItem> items,
                                        CustomSQLDatabaseTransaction updateTransaction) {
        for (DatabaseItem iconData : items) {
            updateTransaction
                    .bindObject(Serializers.ITEM_STACK_SERIALIZER.serialize(iconData.itemStack))
                    .bindObject(iconData.island)
                    .bindObject(iconData.identifier)
                    .newBatch();
        }

        try {
            updateTransaction.execute().get();
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    private static class DatabaseItem {

        private final ItemStack itemStack;
        private final String island;
        private final String identifier;

        DatabaseItem(ItemStack itemStack, String island, String identifier) {
            this.itemStack = itemStack;
            this.island = island;
            this.identifier = identifier;
        }

    }

    private static class DatabaseChest {

        private final String island;
        private final int index;
        private final ItemStack[] contents;

        DatabaseChest(String island, int index, ItemStack[] contents) {
            this.island = island;
            this.index = index;
            this.contents = contents;
        }

    }

}
