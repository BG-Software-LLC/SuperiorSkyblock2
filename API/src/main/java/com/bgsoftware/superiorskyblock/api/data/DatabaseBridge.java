package com.bgsoftware.superiorskyblock.api.data;

import com.bgsoftware.superiorskyblock.api.objects.Pair;

public interface DatabaseBridge {

    /**
     * Start saving data for the object.
     * If this method is not called, data should not be saved when saveRow is called.
     */
    void startSavingData();

    /**
     * Update the object in the database.
     * @param table The name of the table in the database.
     * @param columns All columns to be saved with their values.
     */
    void updateObject(String table, Pair<String, Object>... columns);

    /**
     * Insert the object in the database.
     * @param table The name of the table in the database.
     * @param columns All columns to be saved with their values.
     */
    void insertObject(String table, Pair<String, Object>... columns);

    /**
     * Delete the object from the database.
     * @param table The name of the table in the database.
     */
    void deleteObject(String table);

}
