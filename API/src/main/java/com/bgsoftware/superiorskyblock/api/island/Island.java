package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Island extends Comparable<Island> {

    /**
     * Get the owner of the island.
     */
    SuperiorPlayer getOwner();

    /**
     * Get the list of members of the island, excluding the owner.
     */
    List<UUID> getMembers();

    /**
     * Checks whether or not the island is the spawn island.
     */
    boolean isSpawn();

    /**
     * Invite a player to the island.
     * @param superiorPlayer The player to invite.
     */
    void inviteMember(SuperiorPlayer superiorPlayer);

    /**
     * Revoke an invitation of a player.
     * @param superiorPlayer The player to revoke his invite.
     */
    void revokeInvite(SuperiorPlayer superiorPlayer);

    /**
     * Checks whether or not the player has been invited to the island.
     */
    boolean isInvited(SuperiorPlayer superiorPlayer);

    /**
     * Add a player to the island.
     * @param superiorPlayer The player to add.
     * @param islandRole The role to give to the player.
     *
     * @deprecated See addMember(SuperiorPlayer, PlayerRole)
     */
    @Deprecated
    void addMember(SuperiorPlayer superiorPlayer, IslandRole islandRole);

    /**
     * Add a player to the island.
     * @param superiorPlayer The player to add.
     * @param playerRole The role to give to the player.
     */
    void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole);

    /**
     * Add a player to the island as a co-op member.
     * @param superiorPlayer The player to add.
     */
    void addCoop(SuperiorPlayer superiorPlayer);

    /**
     * Kick a member from the island.
     * @param superiorPlayer The player to kick.
     */
    void kickMember(SuperiorPlayer superiorPlayer);

    /**
     * Remove a player from being a co-op member.
     * @param superiorPlayer The player to remove.
     */
    void removeCoop(SuperiorPlayer superiorPlayer);

    /**
     * Ban a member from the island.
     * @param superiorPlayer The player to ban.
     */
    void banMember(SuperiorPlayer superiorPlayer);

    /**
     * Unban a player from the island.
     * @param superiorPlayer The player to unban.
     */
    void unbanMember(SuperiorPlayer superiorPlayer);

    /**
     * Checks whether or not a player is banned from the island.
     * @param superiorPlayer The player to check.
     */
    boolean isBanned(SuperiorPlayer superiorPlayer);

    /**
     * Get the list of all banned players.
     */
    List<UUID> getAllBannedMembers();

    /**
     * Get the list of all members, including the owner.
     */
    List<UUID> getAllMembers();

    /**
     * Get the list of all visitors that are on the island.
     */
    List<UUID> getVisitors();

    /**
     * Get the list of all the players that are on the island.
     */
    List<UUID> allPlayersInside();

    /**
     * Check whether or not a player is a member of the island.
     * @param superiorPlayer The player to check.
     */
    boolean isMember(SuperiorPlayer superiorPlayer);

    /**
     * Check whether or not a player is a co-op member of the island.
     * @param superiorPlayer The player to check.
     */
    boolean isCoop(SuperiorPlayer superiorPlayer);

    /**
     * Get the center location of the island.
     */
    Location getCenter();

    /**
     * Get the members' teleport location of the island.
     */
    Location getTeleportLocation();

    /**
     * Get the visitors' teleport location of the island.
     */
    Location getVisitorsLocation();

    /**
     * Set the members' teleport location of the island.
     * @param teleportLocation The new teleport location.
     */
    void setTeleportLocation(Location teleportLocation);

    /**
     * Set the visitors' teleport location of the island.
     * @param visitorsLocation The new visitors location.
     */
    void setVisitorsLocation(Location visitorsLocation);

    /**
     * Get the minimum location of the island.
     */
    Location getMinimum();

    /**
     * Get the maximum location of the island.
     */
    Location getMaximum();

    /**
     * Check if a CommandSender has a permission.
     * @param sender The command-sender to check.
     * @param islandPermission The permission to check.
     */
    boolean hasPermission(CommandSender sender, IslandPermission islandPermission);

    /**
     * Check if a player has a permission.
     * @param superiorPlayer The player to check.
     * @param islandPermission The permission to check.
     */
    boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission);

    /**
     * Set a permission to a specific role.
     * @param islandRole The role to set the permission to.
     * @param islandPermission The permission to set.
     * @param value The value to give the permission.
     *
     * @deprecated See setPermission(PlayerRole, IslandPermission, Boolean)
     */
    @Deprecated
    void setPermission(IslandRole islandRole, IslandPermission islandPermission, boolean value);

    /**
     * Set a permission to a specific role.
     * @param playerRole The role to set the permission to.
     * @param islandPermission The permission to set.
     * @param value The value to give the permission.
     */
    void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value);

    /**
     * Set a permission to a specific player.
     * @param superiorPlayer The player to set the permission to.
     * @param islandPermission The permission to set.
     * @param value The value to give the permission.
     */
    void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value);

    /**
     * Get the permission-node of a role.
     * @param islandRole The role to check.
     *
     * @deprecated See getPermissionNode(PlayerRole)
     */
    @Deprecated
    PermissionNode getPermissionNode(IslandRole islandRole);

    /**
     * Get the permission-node of a role.
     * @param playerRole The role to check.
     */
    PermissionNode getPermissionNode(PlayerRole playerRole);

    /**
     * Get the permission-node of a player.
     * @param superiorPlayer The player to check.
     */
    PermissionNode getPermissionNode(SuperiorPlayer superiorPlayer);

    /**
     * Get the required role for a specific permission.
     * @param islandPermission The permission to check.
     */
    @Deprecated
    IslandRole getRequiredRole(IslandPermission islandPermission);

    /**
     * Get the required role for a specific permission.
     * @param islandPermission The permission to check.
     */
    PlayerRole getRequiredPlayerRole(IslandPermission islandPermission);

    /**
     * Disband the island.
     */
    void disbandIsland();

    /**
     * Get all the chunks of the island.
     */
    List<Chunk> getAllChunks();

    /**
     * Get all the chunks of the island.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     */
    List<Chunk> getAllChunks(boolean onlyProtected);

    /**
     * Get the money in the bank of the island.
     */
    BigDecimal getMoneyInBankAsBigDecimal();

    /**
     * Deposit money into the bank.
     * @param amount The amount to deposit.
     */
    void depositMoney(double amount);

    /**
     * Withdraw money from the bank.
     * @param amount The amount to withdraw.
     */
    void withdrawMoney(double amount);

    /**
     * Recalculate the island's worth value.
     * @param asker The player who makes the operation, may be null.
     */
    void calcIslandWorth(SuperiorPlayer asker);

    /**
     * Handle a placement of a block.
     * @param block The block that was placed.
     */
    void handleBlockPlace(Block block);

    /**
     * Handle a placement of a block with a specific amount.
     * @param block The block that was placed.
     * @param amount The amount of the block.
     */
    void handleBlockPlace(Block block, int amount);

    /**
     * Handle a placement of a block with a specific amount.
     * @param block The block that was placed.
     * @param amount The amount of the block.
     * @param save Whether or not the block counts should be saved into database.
     */
    void handleBlockPlace(Block block, int amount, boolean save);

    /**
     * Handle a placement of a block's key with a specific amount.
     * @param key The block's key that was placed.
     * @param amount The amount of the block.
     */
    void handleBlockPlace(Key key, int amount);

    /**
     * Handle a placement of a block's key with a specific amount.
     * @param key The block's key that was placed.
     * @param amount The amount of the block.
     * @param save Whether or not the block counts should be saved into database.
     */
    void handleBlockPlace(Key key, int amount, boolean save);

    /**
     * Handle a break of a block.
     * @param block The block that was broken.
     */
    void handleBlockBreak(Block block);

    /**
     * Handle a break of a block with a specific amount.
     * @param block The block that was broken.
     * @param amount The amount of the block.
     */
    void handleBlockBreak(Block block, int amount);

    /**
     * Handle a break of a block with a specific amount.
     * @param block The block that was broken.
     * @param amount The amount of the block.
     * @param save Whether or not the block counts should be saved into the database.
     */
    void handleBlockBreak(Block block, int amount, boolean save);

    /**
     * Handle a break of a block's key with a specific amount.
     * @param key The block's key that was broken.
     * @param amount The amount of the block.
     */
    void handleBlockBreak(Key key, int amount);

    /**
     * Handle a break of a block with a specific amount.
     * @param key The block's key that was broken.
     * @param amount The amount of the block.
     * @param save Whether or not the block counts should be saved into the database.
     */
    void handleBlockBreak(Key key, int amount, boolean save);

    /**
     * Get the amount of blocks that are on the island.
     * @param key The block's key to check.
     */
    int getBlockCount(Key key);

    /**
     * Get the worth value of the island, including the money in the bank.
     */
    BigDecimal getWorthAsBigDecimal();

    /**
     * Get the worth value of the island, excluding the money in the bank.
     */
    BigDecimal getRawWorthAsBigDecimal();

    /**
     * Set a bonus worth for the island.
     * @param bonusWorth The bonus to give.
     */
    void setBonusWorth(BigDecimal bonusWorth);

    /**
     * Get the level of the island.
     */
    BigDecimal getIslandLevelAsBigDecimal();

    /**
     * Check if the location is inside the island's area.
     * @param location The location to check.
     */
    boolean isInside(Location location);

    /**
     * Check if the location is inside the island's protected area.
     * @param location The location to check.
     */
    boolean isInsideRange(Location location);

    /**
     * Get the level of an upgrade for the island.
     * @param upgradeName The upgrade's name to check.
     */
    int getUpgradeLevel(String upgradeName);

    /**
     * Set the level of an upgrade for the island.
     * @param upgradeName The upgrade's name to set the level.
     * @param level The level to set.
     */
    void setUpgradeLevel(String upgradeName, int level);

    /**
     * Update the border of all the players inside the island.
     */
    void updateBorder();

    /**
     * Get the island radius of the island.
     */
    int getIslandSize();

    /**
     * Get the block limit of a block.
     * @param key The block's key to check.
     */
    int getBlockLimit(Key key);

    /**
     * Get the team limit of the island.
     */
    int getTeamLimit();

    /**
     * Get the crop-growth multiplier for the island.
     */
    double getCropGrowthMultiplier();

    /**
     * Get the spawner-rates multiplier for the island.
     */
    double getSpawnerRatesMultiplier();

    /**
     * Get the mob-drops multiplier for the island.
     */
    double getMobDropsMultiplier();

    /**
     * Set the radius of the island.
     * @param islandSize The radius for the island.
     */
    void setIslandSize(int islandSize);

    /**
     * Set the block limit of a block.
     * @param key The block's key to set the limit to.
     * @param limit The limit to set.
     */
    void setBlockLimit(Key key, int limit);

    /**
     * Set the team limit of the island.
     * @param teamLimit The team limit to set.
     */
    void setTeamLimit(int teamLimit);

    /**
     * Set the crop-growth multiplier for the island.
     * @param cropGrowth The multiplier to set.
     */
    void setCropGrowthMultiplier(double cropGrowth);

    /**
     * Set the spawner-rates multiplier for the island.
     * @param spawnerRates The multiplier to set.
     */
    void setSpawnerRatesMultiplier(double spawnerRates);

    /**
     * Set the mob-drops multiplier for the island.
     * @param mobDrops The multiplier to set.
     */
    void setMobDropsMultiplier(double mobDrops);

    /**
     * Get the discord that is associated with the island.
     */
    String getDiscord();

    /**
     * Set the discord that will be associated with the island.
     */
    void setDiscord(String discord);

    /**
     * Get the paypal that is associated with the island.
     */
    String getPaypal();

    /**
     * Get the paypal that will be associated with the island.
     */
    void setPaypal(String paypal);

    /**
     * Change the biome of the island's area.
     */
    void setBiome(Biome biome);

    /**
     * Send a plain message to all the members of the island.
     * @param message The message to send
     * @param ignoredMembers An array of ignored members.
     */
    void sendMessage(String message, UUID... ignoredMembers);

    /**
     * Get the location of a warp.
     * @param name The warp's name to check.
     */
    Location getWarpLocation(String name);

    /**
     * Check whether or not a warp is private.
     * @param name The warp's name to check.
     */
    boolean isWarpPrivate(String name);

    /**
     * Set the location of a warp.
     * @param name The warp's name to set the location to.
     * @param location The location to set.
     * @param privateFlag Flag to determine if the warp is private or not.
     */
    void setWarpLocation(String name, Location location, boolean privateFlag);

    /**
     * Teleport a player to a warp.
     * @param superiorPlayer The player to teleport.
     * @param warp The warp's name to teleport the player to.
     */
    void warpPlayer(SuperiorPlayer superiorPlayer, String warp);

    /**
     * Delete a warp from the island.
     * @param superiorPlayer The player who requested the operation, may be null.
     * @param location The location of the warp.
     */
    void deleteWarp(SuperiorPlayer superiorPlayer, Location location);

    /**
     * Delete a warp from the island.
     * @param name The warp's name to delete.
     */
    void deleteWarp(String name);

    /**
     * Get all the warps' names of the island.
     */
    List<String> getAllWarps();

    /**
     * Check whether or not the island can create more warps.
     */
    boolean hasMoreWarpSlots();

    /**
     * Set the warps limit for the island.
     * @param warpsLimit The limit to set.
     */
    void setWarpsLimit(int warpsLimit);

    /**
     * Get the warps limit of the island.
     */
    int getWarpsLimit();

    /**
     * Transfer the island's leadership to another player.
     * @param superiorPlayer The player to transfer the leadership to.
     * @return True if the transfer was succeed, otherwise false.
     */
    boolean transferIsland(SuperiorPlayer superiorPlayer);

    /**
     * Check whether or not the island is locked to visitors.
     */
    boolean isLocked();

    /**
     * Lock or unlock the island to visitors.
     * @param locked Whether or not the island should be locked to visitors.
     */
    void setLocked(boolean locked);

    /**
     * Get the name of the island.
     */
    String getName();

    /**
     * Set the name of the island.
     * @param islandName The name to set.
     */
    void setName(String islandName);

    /**
     * Get the description of the island.
     */
    String getDescription();

    /**
     * Set the description of the island.
     * @param description The description to set.
     */
    void setDescription(String description);

    /**
     * Get the rating that a player has given the island.
     * @param uuid The uuid of the player to check.
     *
     * @deprecated See getRating(SuperiorPlayer)
     */
    @Deprecated
    Rating getRating(UUID uuid);

    /**
     * Get the rating that a player has given the island.
     * @param superiorPlayer The player to check.
     *
     * @deprecated See getRating(SuperiorPlayer)
     */
    Rating getRating(SuperiorPlayer superiorPlayer);

    /**
     * Set a rating of a player.
     * @param uuid The uuid of the player that sets the rating.
     * @param rating The rating to set.
     *
     * @deprecated See setRating(SuperiorPlayer, Rating)
     */
    void setRating(UUID uuid, Rating rating);

    /**
     * Set a rating of a player.
     * @param superiorPlayer The player that sets the rating.
     * @param rating The rating to set.
     */
    void setRating(SuperiorPlayer superiorPlayer, Rating rating);

    /**
     * Get the total rating of the island.
     */
    double getTotalRating();

    /**
     * Get the amount of ratings that have have been given to the island.
     */
    int getRatingAmount();

    /**
     * Get all the ratings of the island.
     */
    Map<UUID, Rating> getRatings();

    /**
     * Complete a mission.
     * @param mission The mission to complete.
     */
    void completeMission(Mission mission);

    /**
     * Reset a mission.
     * @param mission The mission to reset.
     */
    void resetMission(Mission mission);

    /**
     * Check whether the island has completed the mission before.
     * @param mission The mission to check.
     */
    boolean hasCompletedMission(Mission mission);

    /**
     * Get the list of the completed missions of the player.
     */
    List<Mission> getCompletedMissions();

    /**
     * The current biome of the island.
     */
    Biome getBiome();

    /**
     * Check whether a settings is enabled or not.
     * @param islandSettings The settings to check.
     */
    boolean hasSettingsEnabled(IslandSettings islandSettings);

    /**
     * Enable an island settings.
     * @param islandSettings The settings to enable.
     */
    void enableSettings(IslandSettings islandSettings);

    /**
     * Disable an island settings.
     * @param islandSettings The settings to disable.
     */
    void disableSettings(IslandSettings islandSettings);

    /**
     * Checks whether or not the island is ignored in the top islands.
     */
    boolean isIgnored();

    /**
     * Set whether or not the island should be ignored in the top islands.
     */
    void setIgnored(boolean ignored);

}