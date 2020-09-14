package com.bgsoftware.superiorskyblock.api.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

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
     * Check all the custom requirements of the upgrade.
     * @param superiorPlayer The player to check the requirements on.
     * @return The error message for the failed requirements.
     *         If all the requirements were passed, an empty string will be returned.
     */
    String checkRequirements(SuperiorPlayer superiorPlayer);

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
     * Get the limit of an entity for this level.
     * @param key The key of the entity to check.
     */
    int getEntityLimit(Key key);

    /**
     * Get all the entity limits for this level.
     *
     * @deprecated Check getEntityLimitsAsKeys()
     */
    @Deprecated
    Map<EntityType, Integer> getEntityLimits();

    /**
     * Get all the entity limits for this level.
     */
    Map<Key, Integer> getEntityLimitsAsKeys();

    /**
     * Get the team limit of this level.
     */
    int getTeamLimit();

    /**
     * Get the warps limit of this level.
     */
    int getWarpsLimit();

    /**
     * Get the coop players limit of this level.
     */
    int getCoopLimit();

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

    /**
     * Get the potion effect for this level.
     * @param potionEffectType The potion effect to check.
     */
    int getPotionEffect(PotionEffectType potionEffectType);

    /**
     * Get all the potion effects for this level.
     */
    Map<PotionEffectType, Integer> getPotionEffects();

}
