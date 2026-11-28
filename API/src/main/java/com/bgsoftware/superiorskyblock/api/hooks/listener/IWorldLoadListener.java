package com.bgsoftware.superiorskyblock.api.hooks.listener;

import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.world.WorldLoadFlags;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;

/**
 * Listener used for {@link WorldsProvider#addWorldLoadListener(IWorldLoadListener)}
 */
public interface IWorldLoadListener {

    /**
     * The method to be called when a world is loaded.
     *
     * @param world          The world that was loaded.
     * @param worldDimension The dimension of the world.
     * @param flags          Flags to what the listener can do. More info at {@link WorldLoadFlags}
     */
    void onWorldLoad(World world, Dimension worldDimension, @WorldLoadFlags int flags);

}
