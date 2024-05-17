package com.bgsoftware.superiorskyblock.api.service.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;

public interface DragonBattleService {

    /**
     * Prepare an end world for dragon fights.
     *
     * @param world The world to prepare.
     */
    void prepareEndWorld(World world);

    /**
     * Get the current active ender dragon of an island.
     * If there is no active fight, null is returned.
     *
     * @param island The island to get the dragon for.
     */
    @Nullable
    EnderDragon getEnderDragon(Island island, Dimension dimension);

    /**
     * Get the current active ender dragon of an island.
     * If there is no active fight, null is returned.
     *
     * @param island The island to get the dragon for.
     */
    @Nullable
    @Deprecated
    EnderDragon getEnderDragon(Island island);

    /**
     * Stop the dragon battle fight for an island.
     * The dragon will be killed and {@link #getEnderDragon(Island, Dimension)} will return null.
     */
    void stopEnderDragonBattle(Island island, Dimension dimension);

    /**
     * Stop the dragon battle fight for an island.
     * The dragon will be killed and {@link #getEnderDragon(Island, Dimension)} will return null.
     */
    @Deprecated
    void stopEnderDragonBattle(Island island);

    /**
     * Reset the dragon battle fight for an island.
     *
     * @param island The island to reset the fight for.
     */
    DragonBattleResetResult resetEnderDragonBattle(Island island, Dimension dimension);

    /**
     * Reset the dragon battle fight for an island.
     *
     * @param island The island to reset the fight for.
     */
    @Deprecated
    DragonBattleResetResult resetEnderDragonBattle(Island island);


}
