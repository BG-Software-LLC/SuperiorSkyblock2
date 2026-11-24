package com.bgsoftware.superiorskyblock.platform.event.args;

public interface IEventArgs {

    /* Called on args built for a later priority of the same Bukkit event as `previous`, which has the same
       class. Copies the values that `previous` already computed and that no priority can change. */
    default void reuseLocationCaches(IEventArgs previous) {
        // Nothing is shared by default.
    }

}
