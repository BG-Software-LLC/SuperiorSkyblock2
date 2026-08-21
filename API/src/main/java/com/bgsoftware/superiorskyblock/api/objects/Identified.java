package com.bgsoftware.superiorskyblock.api.objects;


import java.util.UUID;

public interface Identified {


    /**
     * Get the UUID of the object.
     */
    UUID getUniqueId();

    /**
     * Get the id of the object.
     * <p>
     * Unlike {@link #getUniqueId()}, this id is temporary for a server session, and should mainly be used
     * for referencing the object in memory.
     */
    int getId();

}
