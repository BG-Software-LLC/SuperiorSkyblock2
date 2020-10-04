package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Island extends Comparable<Island> {

    /*
     *  General methods
     */

    /**
     * Get the owner of the island.
     */
    SuperiorPlayer getOwner();

    /**
     * Get the unique-id of the island.
     */
    UUID getUniqueId();

    /**
     * Get the creation time of the island.
     */
    long getCreationTime();

    /**
     * Get the creation time of the island, in a formatted string.
     */
    String getCreationTimeDate();

    /**
     * Re-sync the island with a new dates formatter.
     */
    void updateDatesFormatter();

    /*
     *  Player related methods
     */

    /**
     * Get the list of members of the island.
     * @param includeOwner Whether or not the owner should be returned.
     */
    List<SuperiorPlayer> getIslandMembers(boolean includeOwner);

    /**
     * Get the list of all banned players.
     */
    List<SuperiorPlayer> getBannedPlayers();

    /**
     * Get the list of all visitors that are on the island, including vanished ones.
     */
    List<SuperiorPlayer> getIslandVisitors();

    /**
     * Get the list of all visitors that are on the island.
     * @param vanishPlayers Should vanish players be included?
     */
    List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers);

    /**
     * Get the list of all the players that are on the island.
     */
    List<SuperiorPlayer> getAllPlayersInside();

    /**
     * Get all the visitors that visited the island until now.
     */
    List<SuperiorPlayer> getUniqueVisitors();

    /**
     * Get all the visitors that visited the island until now, with the time they last visited.
     */
    List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes();

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
     * Get all the invited players of the island.
     */
    List<SuperiorPlayer> getInvitedPlayers();

    /**
     * Add a player to the island.
     * @param superiorPlayer The player to add.
     * @param playerRole The role to give to the player.
     */
    void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole);

    /**
     * Kick a member from the island.
     * @param superiorPlayer The player to kick.
     */
    void kickMember(SuperiorPlayer superiorPlayer);

    /**
     * Check whether or not a player is a member of the island.
     * @param superiorPlayer The player to check.
     */
    boolean isMember(SuperiorPlayer superiorPlayer);

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
     * Add a player to the island as a co-op member.
     * @param superiorPlayer The player to add.
     */
    void addCoop(SuperiorPlayer superiorPlayer);

    /**
     * Remove a player from being a co-op member.
     * @param superiorPlayer The player to remove.
     */
    void removeCoop(SuperiorPlayer superiorPlayer);

    /**
     * Check whether or not a player is a co-op member of the island.
     * @param superiorPlayer The player to check.
     */
    boolean isCoop(SuperiorPlayer superiorPlayer);

    /**
     * Get the list of all co-op players.
     */
    List<SuperiorPlayer> getCoopPlayers();

    /**
     * Get the coop players limit of the island.
     */
    int getCoopLimit();

    /**
     * Get the coop players limit of the island that was set using a command.
     */
    int getCoopLimitRaw();

    /**
     * Set the coop players limit of the island.
     * @param coopLimit The coop players limit to set.
     */
    void setCoopLimit(int coopLimit);


    /**
     * Update status of a player if he's inside the island or not.
     * @param superiorPlayer The player to add.
     */
    void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside);

    /**
     * Check whether or not a player is a visitor of the island.
     * @param superiorPlayer The player to check.
     */
    boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus);

    /*
     *  Location related methods
     */

    /**
     * Get the center location of the island.
     *
     * @deprecated See getCenter(Environment)
     */
    @Deprecated
    Location getCenter();

    /**
     * Get the center location of the island, depends on the world environment.
     * @param environment The environment.
     */
    Location getCenter(World.Environment environment);

    /**
     * Get the members' teleport location of the island.
     *
     * @deprecated See getTeleportLocation(Environment)
     */
    @Deprecated
    Location getTeleportLocation();

    /**
     * Get the members' teleport location of the island, depends on the world environment.
     * @param environment The environment.
     */
    Location getTeleportLocation(World.Environment environment);

    /**
     * Get all the teleport locations of the island.
     */
    Map<World.Environment, Location> getTeleportLocations();

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
     * Get the minimum protected location of the island.
     */
    Location getMinimumProtected();

    /**
     * Get the maximum location of the island.
     */
    Location getMaximum();

    /**
     * Get the minimum protected location of the island.
     */
    Location getMaximumProtected();

    /**
     * Get all the chunks of the island from all the environments.
     */
    List<Chunk> getAllChunks();

    /**
     * Get all the chunks of the island from all the environments.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     */
    List<Chunk> getAllChunks(boolean onlyProtected);

    /**
     * Get all the chunks of the island.
     * @param environment The environment to get the chunks from.
     */
    List<Chunk> getAllChunks(World.Environment environment);

    /**
     * Get all the chunks of the island, including empty ones.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     */
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected);

    /**
     * Get all the chunks of the island.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param whenComplete A consumer that will be attached to all the CompletableFuture objects.
     *
     * @deprecated See getAllChunksAsync(World.Environment, Boolean, Consumer)
     */
    @Deprecated
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, BiConsumer<Chunk, Throwable> whenComplete);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param onChunkLoad A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, Consumer<Chunk> onChunkLoad);

    /**
     * Get all the chunks of the island asynchronized.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @param whenComplete A consumer that will be attached to all the CompletableFuture objects.
     *
     * @deprecated See getAllChunksAsync(World.Environment, Boolean, Boolean, Consumer)
     */
    @Deprecated
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, BiConsumer<Chunk, Throwable> whenComplete);

    /**
     * Get all the chunks of the island asynchronized.
     * @param environment The environment to get the chunks from.
     * @param onlyProtected Whether or not only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @param onChunkLoad A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     * @param environment The environment to reset chunks in.
     * @param onlyProtected Whether or not only chunks inside the protected area should be reset.
     */
    void resetChunks(World.Environment environment, boolean onlyProtected);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     * @param environment The environment to reset chunks in.
     * @param onlyProtected Whether or not only chunks inside the protected area should be reset.
     * @param onFinish Callback runnable.
     */
    void resetChunks(World.Environment environment, boolean onlyProtected, Runnable onFinish);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     * @param onlyProtected Whether or not only chunks inside the protected area should be reset.
     */
    void resetChunks(boolean onlyProtected);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     * @param onlyProtected Whether or not only chunks inside the protected area should be reset.
     * @param onFinish Callback runnable.
     */
    void resetChunks(boolean onlyProtected, Runnable onFinish);

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
     * Check if the location is inside the island's protected area.
     * @param location The location to check.
     * @param extraRadius Add extra radius to the protected range.
     */
    boolean isInsideRange(Location location, int extraRadius);

    /**
     * Check if the chunk is inside the island's protected area.
     * @param chunk The chunk to check.
     */
    boolean isInsideRange(Chunk chunk);

    /**
     * Is the nether world enabled for the island?
     */
    boolean isNetherEnabled();

    /**
     * Enable/disable the nether world for the island.
     */
    void setNetherEnabled(boolean enabled);

    /**
     * Is the end world enabled for the island?
     */
    boolean isEndEnabled();

    /**
     * Enable/disable the end world for the island.
     */
    void setEndEnabled(boolean enabled);

    /**
     * Get the unlocked worlds flag.
     */
    int getUnlockedWorldsFlag();

    /*
     *  Permissions related methods
     */

    /**
     * Check if a CommandSender has a permission.
     * @param sender The command-sender to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege);

    /**
     * Check if a player has a permission.
     * @param superiorPlayer The player to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege);

    /**
     * Check if a role has a permission.
     * @param playerRole The role to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege);

    /**
     * Set a permission to a specific role.
     * @param playerRole The role to set the permission to.
     * @param islandPrivilege The permission to set.
     * @param value The value to give the permission.
     */
    void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value);

    /**
     * Reset the roles permissions to default values.
     */
    void resetPermissions();

    /**
     * Set a permission to a specific player.
     * @param superiorPlayer The player to set the permission to.
     * @param islandPrivilege The permission to set.
     * @param value The value to give the permission.
     */
    void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value);

    /**
     * Reset player permissions to default values.
     */
    void resetPermissions(SuperiorPlayer superiorPlayer);

    /**
     * Get the permission-node of a role.
     * @param playerRole The role to check.
     *
     * @deprecated See hasPermission(PlayerRole, IslandPrivilege)
     */
    @Deprecated
    PermissionNode getPermissionNode(PlayerRole playerRole);

    /**
     * Get the permission-node of a player.
     * @param superiorPlayer The player to check.
     */
    PermissionNode getPermissionNode(SuperiorPlayer superiorPlayer);

    /**
     * Get the required role for a specific permission.
     * @param islandPrivilege The permission to check.
     */
    PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege);

    /**
     * Get all the custom player permissions of the island.
     */
    Map<SuperiorPlayer, PermissionNode> getPlayerPermissions();

    /**
     * Get the permissions and their required roles.
     */
    Map<IslandPrivilege, PlayerRole> getRolePermissions();

    /*
     *  General methods
     */

    /**
     * Checks whether or not the island is the spawn island.
     */
    boolean isSpawn();

    /**
     * Get the name of the island.
     */
    String getName();

    /**
     * Get the name of the island, unformatted.
     */
    String getRawName();

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
     * Disband the island.
     */
    void disbandIsland();

    /**
     * Transfer the island's leadership to another player.
     * @param superiorPlayer The player to transfer the leadership to.
     * @return True if the transfer was succeed, otherwise false.
     */
    boolean transferIsland(SuperiorPlayer superiorPlayer);

    /**
     * Replace a player with a new player.
     * @param originalPlayer The old player to be replaced.
     * @param newPlayer The new player.
     */
    void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer);

    /**
     * Recalculate the island's worth value.
     * @param asker The player who makes the operation, may be null.
     */
    void calcIslandWorth(SuperiorPlayer asker);

    /**
     * Recalculate the island's worth value.
     * @param asker The player who makes the operation, may be null.
     * @param callback Runnable which will be ran when process is finished.
     */
    void calcIslandWorth(SuperiorPlayer asker, Runnable callback);

    /**
     * Update the border of all the players inside the island.
     */
    void updateBorder();

    /**
     * Update the fly status for a player on this island.
     * @param superiorPlayer The player to update.
     */
    void updateIslandFly(SuperiorPlayer superiorPlayer);

    /**
     * Get the island radius of the island.
     */
    int getIslandSize();

    /**
     * Get the island radius of the island that was set with a command.
     */
    int getIslandSizeRaw();

    /**
     * Set the radius of the island.
     * @param islandSize The radius for the island.
     */
    void setIslandSize(int islandSize);

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
     * The current biome of the island.
     */
    Biome getBiome();

    /**
     * Change the biome of the island's area.
     */
    void setBiome(Biome biome);

    /**
     * Change the biome of the island's area.
     * @param updateBlocks Whether the blocks get updated or not.
     */
    void setBiome(Biome biome, boolean updateBlocks);

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
     * Checks whether or not the island is ignored in the top islands.
     */
    boolean isIgnored();

    /**
     * Set whether or not the island should be ignored in the top islands.
     */
    void setIgnored(boolean ignored);

    /**
     * Send a plain message to all the members of the island.
     * @param message The message to send
     * @param ignoredMembers An array of ignored members.
     */
    void sendMessage(String message, UUID... ignoredMembers);

    /**
     * Checks whether or not the island is being recalculated currently.
     */
    boolean isBeingRecalculated();

    /**
     * Update the last time the island was used.
     */
    void updateLastTime();

    /**
     * Flag the island as a currently active island.
     */
    void setCurrentlyActive();

    /**
     * Get the last time the island was updated.
     */
    long getLastTimeUpdate();

    /*
     *  Bank related methods
     */

    /**
     * Get the bank of the island.
     */
    IslandBank getIslandBank();

    /**
     * Get the limit of the bank.
     */
    BigDecimal getBankLimit();

    /**
     * Get the limit of the bank that was set using a command.
     */
    BigDecimal getBankLimitRaw();

    /**
     * Set a new limit for the bank.
     * @param bankLimit The limit to set. Use -1 to remove the limit.
     */
    void setBankLimit(BigDecimal bankLimit);

    /**
     * Give the bank interest to this island.
     * @param checkOnlineOwner Check if the island-owner was online recently.
     * @return Whether or not the money was given.
     */
    boolean giveInterest(boolean checkOnlineOwner);

    /**
     * Get the last time that the bank interest was given.
     */
    long getLastInterestTime();

    /**
     * Get the money in the bank of the island.
     *
     * @deprecated See IslandBank#getBalance
     */
    @Deprecated
    BigDecimal getMoneyInBank();

    /**
     * Deposit money into the bank.
     * @param amount The amount to deposit.
     *
     * @deprecated See IslandBank#depositMoney
     */
    @Deprecated
    void depositMoney(double amount);

    /**
     * Deposit money into the bank.
     * @param amount The amount to deposit.
     *
     * @deprecated See IslandBank#depositMoney
     */
    @Deprecated
    void depositMoney(BigDecimal amount);

    /**
     * Withdraw money from the bank.
     * @param amount The amount to withdraw.
     *
     * @deprecated See IslandBank#withdrawMoney
     */
    @Deprecated
    void withdrawMoney(double amount);

    /**
     * Withdraw money from the bank.
     * @param amount The amount to withdraw.
     *
     * @deprecated See IslandBank#withdrawMoney
     */
    @Deprecated
    void withdrawMoney(BigDecimal amount);

    /*
     *  Worth related methods
     */

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
     * Handle placements of many blocks in one time.
     * @param blocks All the blocks to place.
     */
    void handleBlocksPlace(Map<Key, Integer> blocks);

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
     * Get all the blocks that are on the island.
     */
    Map<Key, Integer> getBlockCounts();

    /**
     * Get the amount of blocks that are on the island.
     * Unlike getBlockCount(Key), this method returns the count for
     * the exactly block that is given as a parameter.
     * @param key The block's key to check.
     */
    int getExactBlockCount(Key key);

    /**
     * Get the worth value of the island, including the money in the bank.
     */
    BigDecimal getWorth();

    /**
     * Get the worth value of the island, excluding bonus worth and the money in the bank.
     */
    BigDecimal getRawWorth();

    /**
     * Get the bonus worth of the island.
     */
    BigDecimal getBonusWorth();

    /**
     * Set a bonus worth for the island.
     * @param bonusWorth The bonus to give.
     */
    void setBonusWorth(BigDecimal bonusWorth);

    /**
     * Get the bonus level of the island.
     */
    BigDecimal getBonusLevel();

    /**
     * Set a bonus level for the island.
     * @param bonusLevel The bonus to give.
     */
    void setBonusLevel(BigDecimal bonusLevel);

    /**
     * Get the level of the island.
     */
    BigDecimal getIslandLevel();

    /**
     * Get the level value of the island, excluding the bonus level.
     */
    BigDecimal getRawLevel();

    /*
     *  Upgrades related methods
     */

    /**
     * Get the level of an upgrade for the island.
     * @param upgradeName The upgrade's name to check.
     *
     * @deprecated see getUpgradeLevel(Upgrade)
     */
    @Deprecated
    int getUpgradeLevel(String upgradeName);

    /**
     * Get the level of an upgrade for the island.
     * @param upgrade The upgrade to check.
     */
    UpgradeLevel getUpgradeLevel(Upgrade upgrade);

    /**
     * Set the level of an upgrade for the island.
     * @param upgradeName The upgrade's name to set the level.
     * @param level The level to set.
     *
     * @deprecated See setUpgradeLevel(Upgrade, Integer)
     */
    @Deprecated
    void setUpgradeLevel(String upgradeName, int level);

    /**
     * Set the level of an upgrade for the island.
     * @param upgrade The upgrade to set the level.
     * @param level The level to set.
     */
    void setUpgradeLevel(Upgrade upgrade, int level);

    /**
     * Get all the upgrades with their levels.
     */
    Map<String, Integer> getUpgrades();

    /**
     * Sync all the upgrade values again.
     * This will remove custom values that were set using the set commands.
     */
    void syncUpgrades();

    /**
     * Update the upgrade values from default values of config & upgrades file.
     */
    void updateUpgrades();

    /**
     * Get the crop-growth multiplier for the island.
     */
    double getCropGrowthMultiplier();

    /**
     * Get the crop-growth multiplier for the island that was set using a command.
     */
    double getCropGrowthRaw();

    /**
     * Set the crop-growth multiplier for the island.
     * @param cropGrowth The multiplier to set.
     */
    void setCropGrowthMultiplier(double cropGrowth);

    /**
     * Get the spawner-rates multiplier for the island.
     */
    double getSpawnerRatesMultiplier();

    /**
     * Get the spawner-rates multiplier for the island that was set using a command.
     */
    double getSpawnerRatesRaw();

    /**
     * Set the spawner-rates multiplier for the island.
     * @param spawnerRates The multiplier to set.
     */
    void setSpawnerRatesMultiplier(double spawnerRates);

    /**
     * Get the mob-drops multiplier for the island.
     */
    double getMobDropsMultiplier();

    /**
     * Get the mob-drops multiplier for the island that was set using a command.
     */
    double getMobDropsRaw();

    /**
     * Set the mob-drops multiplier for the island.
     * @param mobDrops The multiplier to set.
     */
    void setMobDropsMultiplier(double mobDrops);

    /**
     * Get the block limit of a block.
     * @param key The block's key to check.
     */
    int getBlockLimit(Key key);

    /**
     * Get the block limit of a block.
     * Unlike getBlockLimit(Key), this method returns the count for
     * the exactly block that is given as a parameter.
     * @param key The block's key to check.
     */
    int getExactBlockLimit(Key key);

    /**
     * Get all the blocks limits for the island.
     */
    Map<Key, Integer> getBlocksLimits();

    /**
     * Get all the custom blocks limits for the island.
     */
    Map<Key, Integer> getCustomBlocksLimits();

    /**
     * Clear all the block limits of the island.
     */
    void clearBlockLimits();

    /**
     * Set the block limit of a block.
     * @param key The block's key to set the limit to.
     * @param limit The limit to set.
     */
    void setBlockLimit(Key key, int limit);

    /**
     * Remove the limit of a block.
     * @param key The block's key to remove it's limit.
     */
    void removeBlockLimit(Key key);

    /**
     * A method to check if a specific block has reached the limit.
     * This method checks for the block and it's global block key.
     * @param key The block's key to check.
     */
    boolean hasReachedBlockLimit(Key key);

    /**
     * A method to check if a specific block has reached the limit.
     * This method checks for the block and it's global block key.
     * @param key The block's key to check.
     * @param amount Amount of the block to be placed.
     */
    boolean hasReachedBlockLimit(Key key, int amount);

    /**
     * Get the entity limit of an entity.
     * @param entityType The entity's type to check.
     */
    int getEntityLimit(EntityType entityType);

    /**
     * Get the entity limit of an entity.
     * @param key The key of the entity to check.
     */
    int getEntityLimit(Key key);

    /**
     * Get all the entities limits for the island.
     *
     * @deprecated Check getEntitiesLimitsAsKeys()
     */
    @Deprecated
    Map<EntityType, Integer> getEntitiesLimits();

    /**
     * Get all the entities limits for the island.
     */
    Map<Key, Integer> getEntitiesLimitsAsKeys();

    /**
     * Get all the custom entities limits for the island.
     */
    Map<Key, Integer> getCustomEntitiesLimits();

    /**
     * Clear all the entities limits from the island.
     */
    void clearEntitiesLimits();

    /**
     * Set the entity limit of an entity.
     * @param entityType The entity's type to set the limit to.
     * @param limit The limit to set.
     */
    void setEntityLimit(EntityType entityType, int limit);

    /**
     * Set the entity limit of an entity.
     * @param key The key of the entity to set the limit to.
     * @param limit The limit to set.
     */
    void setEntityLimit(Key key, int limit);

    /**
     * A method to check if a specific entity has reached the limit.
     * @param entityType The entity's type to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType);

    /**
     * A method to check if a specific entity has reached the limit.
     * @param key The key of the entity to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(Key key);

    /**
     * A method to check if a specific entity has reached the limit.
     * @param amount The amount of entities that were added.
     * @param entityType The entity's type to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount);

    /**
     * A method to check if a specific entity has reached the limit.
     * @param amount The amount of entities that were added.
     * @param key The key of the entity to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount);

    /**
     * Get the team limit of the island.
     */
    int getTeamLimit();

    /**
     * Get the team limit of the island that was set with a command.
     */
    int getTeamLimitRaw();

    /**
     * Set the team limit of the island.
     * @param teamLimit The team limit to set.
     */
    void setTeamLimit(int teamLimit);

    /**
     * Get the warps limit of the island.
     */
    int getWarpsLimit();

    /**
     * Get the warps limit of the island that was set using a command.
     */
    int getWarpsLimitRaw();

    /**
     * Set the warps limit for the island.
     * @param warpsLimit The limit to set.
     */
    void setWarpsLimit(int warpsLimit);

    /**
     * Add a potion effect to the island.
     * @param type The potion effect to add.
     * @param level The level of the potion effect.
     *       If the level is 0 or below, then the effect will be removed.
     */
    void setPotionEffect(PotionEffectType type, int level);

    /**
     * Get the level of an island effect.
     * @param type The potion to check.
     * @return The level of the potion. If 0, it means that this is not an active effect on the island.
     */
    int getPotionEffectLevel(PotionEffectType type);

    /**
     * Get a list of all active island effects with their levels.
     */
    Map<PotionEffectType, Integer> getPotionEffects();

    /**
     * Give all the island effects to a player.
     * @param superiorPlayer The player to give the effect to.
     */
    void applyEffects(SuperiorPlayer superiorPlayer);

    /**
     * Remove all the island effects from a player.
     * @param superiorPlayer The player to remove the effects to.
     */
    void removeEffects(SuperiorPlayer superiorPlayer);

    /**
     * Remove all the island effects from the players inside the island.
     */
    void removeEffects();

    /**
     * Remove all the effects from the island.
     */
    void clearEffects();

    /*
     *  Warps related methods
     */

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
     * Check whether or not a location is a warp of an island.
     * @param location The location to check.
     */
    boolean isWarpLocation(Location location);

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
     * Get all the warps of the island.
     */
    Map<String, IslandWarp> getIslandWarps();

    /**
     * Check whether or not the island can create more warps.
     */
    boolean hasMoreWarpSlots();

    /*
     *  Ratings related methods
     */

    /**
     * Get the rating that a player has given the island.
     * @param superiorPlayer The player to check.
     */
    Rating getRating(SuperiorPlayer superiorPlayer);

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
     * Remove all the ratings of the island.
     */
    void removeRatings();

    /*
     *  Missions related methods
     */

    /**
     * Complete a mission.
     * @param mission The mission to complete.
     */
    void completeMission(Mission<?> mission);

    /**
     * Reset a mission.
     * @param mission The mission to reset.
     */
    void resetMission(Mission<?> mission);

    /**
     * Check whether the island has completed the mission before.
     * @param mission The mission to check.
     */
    boolean hasCompletedMission(Mission<?> mission);

    /**
     * Check whether the island can complete a mission again.
     * @param mission The mission to check.
     */
    boolean canCompleteMissionAgain(Mission<?> mission);

    /**
     * Get the amount of times mission was completed.
     * @param mission The mission to check.
     */
    int getAmountMissionCompleted(Mission<?> mission);

    /**
     * Get the list of the completed missions of the player.
     */
    List<Mission<?>> getCompletedMissions();

    /**
     * Get all the completed missions with the amount of times they were completed.
     */
    Map<Mission<?>, Integer> getCompletedMissionsWithAmounts();

    /*
     *  Settings related methods
     */

    /**
     * Check whether a settings is enabled or not.
     * @param islandFlag The settings to check.
     */
    boolean hasSettingsEnabled(IslandFlag islandFlag);

    /**
     * Get all the settings of the island.
     * If the byte value is 1, the setting is enabled. Otherwise, it's disabled.
     */
    Map<IslandFlag, Byte> getAllSettings();

    /**
     * Enable an island settings.
     * @param islandFlag The settings to enable.
     */
    void enableSettings(IslandFlag islandFlag);

    /**
     * Disable an island settings.
     * @param islandFlag The settings to disable.
     */
    void disableSettings(IslandFlag islandFlag);

    /*
     *  Generator related methods
     */

    /**
     * Set a percentage for a specific key.
     * Percentage can be between 0 and 100 (0 will remove the key from the list).
     *
     * This function sets the amount of the key using the following formula:
     * amount = (percentage * total_amount) / (1 - percentage)
     *
     * If the percentage is 100, the rest of the amounts will be cleared and
     * the material's amount will be set to 1.
     *
     * The amount is rounded to ensure a smaller loss, and currently it's 1%~ loss.
     */
    void setGeneratorPercentage(Key key, int percentage);

    /**
     * Get the percentage for a specific key.
     * The formula is (amount * 100) / total_amount.
     */
    int getGeneratorPercentage(Key key);

    /**
     * Get the percentages of the materials for the cobblestone generator in the island.
     */
    Map<String, Integer> getGeneratorPercentages();

    /**
     * Set an amount for a specific key.
     */
    void setGeneratorAmount(Key key, int amount);

    /**
     * Get the amount of a specific key.
     */
    int getGeneratorAmount(Key key);

    /**
     * Get the total amount of all the generator keys together.
     */
    int getGeneratorTotalAmount();

    /**
     * Get the amounts of the materials for the cobblestone generator in the island.
     */
    Map<String, Integer> getGeneratorAmounts();

    /**
     * Get the custom amounts of the materials for the cobblestone generator in the island.
     */
    Map<Key, Integer> getCustomGeneratorAmounts();

    /**
     * Get an array of materials for the cobblestone generator.
     */
    String[] getGeneratorArray();

    /**
     * Clear all the custom generator amounts for this island.
     */
    void clearGeneratorAmounts();

    /*
     *  Schematic methods
     */

    /**
     * Checks if a schematic was generated already.
     * @param environment The environment to check.
     */
    boolean wasSchematicGenerated(World.Environment environment);

    /**
     * Set schematic generated flag to true.
     * @param environment The environment to set.
     */
    void setSchematicGenerate(World.Environment environment);

    /**
     * Get the generated schematics flag.
     */
    int getGeneratedSchematicsFlag();

    /**
     * Get the schematic that was used to create the island.
     */
    String getSchematicName();

    /*
     *  Island top methods
     */

    int getPosition(SortingType sortingType);

    /*
     *  Vault related methods
     */

    /**
     * Get the island chest.
     */
    IslandChest[] getChest();

    /**
     * Get the amount of pages the island chest has.
     */
    int getChestSize();

    /**
     * Set the amount of rows for the chest in a specific index.
     * @param index The index of the page (0 or above)
     * @param rows The amount of rows for that page.
     */
    void setChestRows(int index, int rows);

    /*
     *  Data related methods
     */

    /**
     * Get the data handler of the object.
     */
    IslandDataHandler getDataHandler();

}