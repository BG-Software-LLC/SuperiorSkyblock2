package com.bgsoftware.superiorskyblock.api.persistence;

/**
 * Represents an object that can store custom persistent data.
 */
public interface IPersistentDataHolder {

    /**
     * Get the container of custom persistent data of this object.
     */
    PersistentDataContainer getPersistentDataContainer();

}
