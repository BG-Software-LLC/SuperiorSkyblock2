package com.bgsoftware.superiorskyblock.api.hooks.world;

import com.bgsoftware.superiorskyblock.api.hooks.listener.IWorldLoadListener;

/**
 * The integer value element annotated with {@link WorldLoadFlags} represents flags related to what to do
 * when a world is loaded. It is mainly used within the default {@link IWorldLoadListener} interface of the plugin.
 */
public @interface WorldLoadFlags {

    /**
     * Enable dragon fights for end worlds.
     */
    int END_DRAGON_FIGHT = 1 << 0;

    /**
     * Remove the anti-xray world patches.
     */
    int REMOVE_ANTI_XRAY = 1 << 1;

    /**
     * Update the ocean level of the world to the configured islands-height.
     */
    int UPDATE_OCEAN_LEVEL = 1 << 2;

    /**
     * Make the plugin listen to block neighbor changes and self-changing blocks.
     */
    int LISTEN_BLOCK_CHANGES = 1 << 3;

}
