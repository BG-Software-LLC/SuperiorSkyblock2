package com.bgsoftware.superiorskyblock.api.persistence;

/**
 * Represents an object that can store custom persistent data.
 */
public interface IPersistentDataHolder {

    /**
     * Get the container of custom persistent data of this object.
     */
    PersistentDataContainer getPersistentDataContainer();

    /**
     * Check if the persistent data container is empty.
     */
    boolean isPersistentDataContainerEmpty();

    /**
     * When saving data into the container, it's not immediately saved to database.
     * Call this method to save the persistent data container into database.
     */
    void savePersistentDataContainer();

}
