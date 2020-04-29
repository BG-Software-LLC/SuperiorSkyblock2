package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

public interface UpgradeLevel {

    /**
     * Get the level of the current upgrade-level.
     */
    int getLevel();

    /**
     * Get the price required to upgrade to the next level.
     */
    double getPrice();

    /**
     * Get all commands that will be executed when upgrading to the next level.
     */
    List<String> getCommands();

    /**
     * Get the permission required to upgrade to this level.
     */
    String getPermission();

    /**
     * Get the crop growth multiplier of this level.
     */
    double getCropGrowth();

    /**
     * Get the spawner rates multiplier of this level.
     */
    double getSpawnerRates();

    /**
     * Get the mob drops multiplier of this level.
     */
    double getMobDrops();

    /**
     * Get the limit of a block for this level.
     * @param key The block to check.
     */
    int getBlockLimit(Key key);

    /**
     * Get the exact limit of a block for this level.
     * @param key The block to check.
     */
    int getExactBlockLimit(Key key);

    /**
     * Get all the block limits for this level.
     */
    Map<Key, Integer> getBlockLimits();

    /**
     * Get the limit of an entity for this level.
     * @param entityType The entity's type to check.
     */
    int getEntityLimit(EntityType entityType);

    /**
     * Get all the entity limits for this level.
     */
    Map<EntityType, Integer> getEntityLimits();

    /**
     * Get the team limit of this level.
     */
    int getTeamLimit();

    /**
     * Get the warps limit of this level.
     */
    int getWarpsLimit();

    /**
     * Get the border size of this level.
     */
    int getBorderSize();

    /**
     * Get the generator rate of a block for this level.
     * @param key The block to check.
     */
    int getGeneratorAmount(Key key);

    /**
     * Get all the generator rates for this level.
     */
    Map<String, Integer> getGeneratorAmounts();

}
