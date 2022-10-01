package com.bgsoftware.superiorskyblock.api.enums;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;

/**
 * Sync status for {@link IslandFlag} and {@link IslandPrivilege} in islands.
 */
public enum SyncStatus {

    /**
     * The target is enabled and is not synced.
     */
    ENABLED,

    /**
     * The target is disabled and is not synced.
     */
    DISABLED,

    /**
     * The target is synced with upgrades and default config values.
     */
    SYNCED

}
