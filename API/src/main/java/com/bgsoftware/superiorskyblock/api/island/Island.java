package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.enums.SyncStatus;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface Island extends Comparable<Island>, IMissionsHolder, IPersistentDataHolder, IDatabaseBridgeHolder {

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
     *
     * @param includeOwner Whether the owner should be returned.
     */
    List<SuperiorPlayer> getIslandMembers(boolean includeOwner);

    /**
     * Get the list of members of the island with specific roles.
     *
     * @param playerRoles The roles to filter with.
     */
    List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles);

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
     *
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
     *
     * @param superiorPlayer The player to invite.
     */
    void inviteMember(SuperiorPlayer superiorPlayer);

    /**
     * Revoke an invitation of a player.
     *
     * @param superiorPlayer The player to revoke his invite.
     */
    void revokeInvite(SuperiorPlayer superiorPlayer);

    /**
     * Checks whether the player has been invited to the island.
     */
    boolean isInvited(SuperiorPlayer superiorPlayer);

    /**
     * Get all the invited players of the island.
     */
    List<SuperiorPlayer> getInvitedPlayers();

    /**
     * Add a player to the island.
     *
     * @param superiorPlayer The player to add.
     * @param playerRole     The role to give to the player.
     */
    void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole);

    /**
     * Kick a member from the island.
     *
     * @param superiorPlayer The player to kick.
     */
    void kickMember(SuperiorPlayer superiorPlayer);

    /**
     * Check whether a player is a member of the island.
     *
     * @param superiorPlayer The player to check.
     */
    boolean isMember(SuperiorPlayer superiorPlayer);

    /**
     * Ban a member from the island.
     *
     * @param superiorPlayer The player to ban.
     */
    void banMember(SuperiorPlayer superiorPlayer);

    /**
     * Ban a member from the island.
     *
     * @param superiorPlayer The player to ban.
     * @param whom           The player that executed the ban command.
     *                       If null, CONSOLE will be chosen as the banner.
     */
    void banMember(SuperiorPlayer superiorPlayer, @Nullable SuperiorPlayer whom);

    /**
     * Unban a player from the island.
     *
     * @param superiorPlayer The player to unban.
     */
    void unbanMember(SuperiorPlayer superiorPlayer);

    /**
     * Checks whether a player is banned from the island.
     *
     * @param superiorPlayer The player to check.
     */
    boolean isBanned(SuperiorPlayer superiorPlayer);

    /**
     * Add a player to the island as a co-op member.
     *
     * @param superiorPlayer The player to add.
     */
    void addCoop(SuperiorPlayer superiorPlayer);

    /**
     * Remove a player from being a co-op member.
     *
     * @param superiorPlayer The player to remove.
     */
    void removeCoop(SuperiorPlayer superiorPlayer);

    /**
     * Check whether a player is a co-op member of the island.
     *
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
     *
     * @param coopLimit The coop players limit to set.
     */
    void setCoopLimit(int coopLimit);

    /**
     * Update status of a player if he's inside the island or not.
     *
     * @param superiorPlayer The player to add.
     */
    void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside);

    /**
     * Check whether a player is a visitor of the island or not.
     *
     * @param superiorPlayer  The player to check.
     * @param checkCoopStatus Whether to check for coop status or not.
     *                        If enabled, coops will not be considered as visitors.
     */
    boolean isVisitor(SuperiorPlayer superiorPlayer, boolean checkCoopStatus);

    /*
     *  Location related methods
     */

    /**
     * Get the center location of the island, depends on the world environment.
     *
     * @param environment The environment.
     */
    Location getCenter(World.Environment environment);

    /**
     * Get the center position of the island.
     */
    BlockPosition getCenterPosition();

    /**
     * Get the members' teleport location of the island, depends on the world environment.
     * Similar to {@link #getIslandHome(World.Environment)}
     *
     * @param environment The environment.
     */
    @Nullable
    Location getTeleportLocation(World.Environment environment);

    /**
     * Get all the teleport locations of the island.
     * Similar to {@link #getIslandHomes()}
     */
    Map<World.Environment, Location> getTeleportLocations();

    /**
     * Set the members' teleport location of the island.
     * Similar to {@link #setIslandHome(Location)}
     *
     * @param teleportLocation The new teleport location.
     */
    void setTeleportLocation(Location teleportLocation);

    /**
     * Set the members' teleport location of the island.
     * Similar to {@link #setIslandHome(org.bukkit.World.Environment, Location)}
     *
     * @param environment      The environment to change teleport location for.
     * @param teleportLocation The new teleport location.
     */
    void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation);

    /**
     * Get the members' home location of the island, depends on the world environment.
     *
     * @param environment The environment.
     */
    @Nullable
    Location getIslandHome(World.Environment environment);

    /**
     * Get all the home locations of the island.
     */
    Map<World.Environment, Location> getIslandHomes();

    /**
     * Set the members' teleport location of the island.
     *
     * @param homeLocation The new home location.
     */
    void setIslandHome(Location homeLocation);

    /**
     * Set the members' teleport location of the island.
     *
     * @param environment  The environment to change teleport location for.
     * @param homeLocation The new home location.
     */
    void setIslandHome(World.Environment environment, @Nullable Location homeLocation);

    /**
     * Get the visitors' teleport location of the island.
     *
     * @deprecated See {@link #getVisitorsLocation(World.Environment)}
     */
    @Nullable
    @Deprecated
    Location getVisitorsLocation();

    /**
     * Get the visitors' teleport location of the island.
     *
     * @param environment The environment to get the visitors-location from.
     *                    Currently unused, it has no effect.
     */
    @Nullable
    Location getVisitorsLocation(World.Environment environment);

    /**
     * Set the visitors' teleport location of the island.
     *
     * @param visitorsLocation The new visitors location.
     */
    void setVisitorsLocation(@Nullable Location visitorsLocation);

    /**
     * Get the minimum location of the island.
     */
    Location getMinimum();

    /**
     * Get the minimum location of the island.
     */
    BlockPosition getMinimumPosition();

    /**
     * Get the minimum protected location of the island.
     */
    Location getMinimumProtected();

    /**
     * Get the minimum location of the island.
     */
    BlockPosition getMinimumProtectedPosition();

    /**
     * Get the maximum location of the island.
     */
    Location getMaximum();

    /**
     * Get the maximum location of the island.
     */
    BlockPosition getMaximumPosition();

    /**
     * Get the minimum protected location of the island.
     */
    Location getMaximumProtected();

    /**
     * Get the minimum protected location of the island.
     */
    BlockPosition getMaximumProtectedPosition();

    /**
     * Get all the chunks of the island from all the environments.
     * Similar to {@link #getAllChunks(int)} with 0 as flags parameter.
     */
    List<Chunk> getAllChunks();

    /**
     * Get all the chunks of the island from all the environments.
     *
     * @param flags See {@link IslandChunkFlags}
     */
    List<Chunk> getAllChunks(@IslandChunkFlags int flags);

    /**
     * Get all the chunks of the island.
     * Similar to {@link #getAllChunks(org.bukkit.World.Environment, int)} with 0 as flags parameter.
     *
     * @param environment The environment to get the chunks from.
     */
    List<Chunk> getAllChunks(World.Environment environment);

    /**
     * Get all the chunks of the island.
     *
     * @param environment The environment to get the chunks from.
     * @param flags       See {@link IslandChunkFlags}
     */
    List<Chunk> getAllChunks(World.Environment environment, @IslandChunkFlags int flags);

    /**
     * Get all the chunks of the island from all the environments.
     *
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @deprecated See {@link #getAllChunks(int)}
     */
    @Deprecated
    List<Chunk> getAllChunks(boolean onlyProtected);

    /**
     * Get all the chunks of the island, including empty ones.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @deprecated See {@link #getAllChunks(World.Environment, int)}
     */
    @Deprecated
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected);

    /**
     * Get all the chunks of the island.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @deprecated See {@link #getAllChunks(World.Environment, int)}
     */
    @Deprecated
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     * Similar to {@link #getLoadedChunks(int)} with 0 as flags parameter.
     */
    List<Chunk> getLoadedChunks();

    /**
     * Get all the loaded chunks of the island.
     *
     * @param flags See {@link IslandChunkFlags}
     */
    List<Chunk> getLoadedChunks(@IslandChunkFlags int flags);

    /**
     * Get all the loaded chunks of the island.
     * Similar to {@link #getLoadedChunks(World.Environment, int)} with 0 as flags parameter.
     *
     * @param environment The environment to get the chunks from.
     */
    List<Chunk> getLoadedChunks(World.Environment environment);

    /**
     * Get all the loaded chunks of the island.
     *
     * @param environment The environment to get the chunks from.
     * @param flags       See {@link IslandChunkFlags}
     */
    List<Chunk> getLoadedChunks(World.Environment environment, @IslandChunkFlags int flags);

    /**
     * Get all the loaded chunks of the island.
     *
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @deprecated See {@link #getLoadedChunks(int)}
     */
    @Deprecated
    List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @deprecated See {@link #getLoadedChunks(World.Environment, int)}
     */
    @Deprecated
    List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     * Similar to {@link #getAllChunksAsync(World.Environment, int, Consumer)}, with 0 as flags parameter.
     *
     * @param environment The environment to get the chunks from.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     *
     * @param environment The environment to get the chunks from.
     * @param flags       See {@link IslandChunkFlags}
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @IslandChunkFlags int flags);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     * Similar to {@link #getAllChunksAsync(World.Environment, int, Consumer)}, with 0 as flags parameter.
     *
     * @param environment The environment to get the chunks from.
     * @param onChunkLoad A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     *
     * @param environment The environment to get the chunks from.
     * @param flags       See {@link IslandChunkFlags}
     * @param onChunkLoad A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @IslandChunkFlags int flags,
                                                     @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param onChunkLoad   A consumer that will be ran when the chunk is loaded. Can be null.
     * @deprecated See {@link #getAllChunksAsync(World.Environment, int, Consumer)}
     */
    @Deprecated
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                     @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Get all the chunks of the island asynchronized.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @param onChunkLoad   A consumer that will be ran when the chunk is loaded. Can be null.
     * @deprecated See {@link #getAllChunksAsync(World.Environment, int, Consumer)}
     */
    @Deprecated
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     * Similar to {@link #resetChunks(int)}, with 0 as flags parameter.
     */
    void resetChunks();

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     * Similar to {@link #resetChunks(int, Runnable)}, with 0 as flags parameter.
     *
     * @param onFinish Callback runnable.
     */
    void resetChunks(@Nullable Runnable onFinish);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     * Similar to {@link #resetChunks(World.Environment, int)}, with 0 as flags parameter.
     *
     * @param environment The environment to reset chunks in.
     */
    void resetChunks(World.Environment environment);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     *
     * @param environment The environment to reset chunks in.
     * @param onFinish    Callback runnable.
     */
    void resetChunks(World.Environment environment, @Nullable Runnable onFinish);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     *
     * @param flags See {@link IslandChunkFlags}
     */
    void resetChunks(@IslandChunkFlags int flags);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     *
     * @param flags    See {@link IslandChunkFlags}
     * @param onFinish Callback runnable.
     */
    void resetChunks(@IslandChunkFlags int flags, @Nullable Runnable onFinish);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     *
     * @param environment The environment to reset chunks in.
     * @param flags       See {@link IslandChunkFlags}
     */
    void resetChunks(World.Environment environment, @IslandChunkFlags int flags);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     *
     * @param environment The environment to reset chunks in.
     * @param flags       See {@link IslandChunkFlags}
     * @param onFinish    Callback runnable.
     */
    void resetChunks(World.Environment environment, @IslandChunkFlags int flags, @Nullable Runnable onFinish);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     *
     * @param environment   The environment to reset chunks in.
     * @param onlyProtected Whether only chunks inside the protected area should be reset.
     * @deprecated See {@link #resetChunks(World.Environment, int)}
     */
    @Deprecated
    void resetChunks(World.Environment environment, boolean onlyProtected);

    /**
     * Reset all the chunks of the island (will make all chunks empty).
     *
     * @param environment   The environment to reset chunks in.
     * @param onlyProtected Whether only chunks inside the protected area should be reset.
     * @param onFinish      Callback runnable.
     * @deprecated See {@link #resetChunks(World.Environment, int, Runnable)}
     */
    @Deprecated
    void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     *
     * @param onlyProtected Whether only chunks inside the protected area should be reset.
     * @deprecated See {@link #resetChunks(int)}
     */
    @Deprecated
    void resetChunks(boolean onlyProtected);

    /**
     * Reset all the chunks of the island from all the worlds (will make all chunks empty).
     *
     * @param onlyProtected Whether only chunks inside the protected area should be reset.
     * @param onFinish      Callback runnable.
     * @deprecated See {@link #resetChunks(int, Runnable)}
     */
    @Deprecated
    void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish);

    /**
     * Check if the location is inside the island's area.
     *
     * @param location The location to check.
     */
    boolean isInside(Location location);

    /**
     * Check if a chunk location is inside the island's area.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     */
    boolean isInside(World world, int chunkX, int chunkZ);

    /**
     * Check if the location is inside the island's protected area.
     *
     * @param location The location to check.
     */
    boolean isInsideRange(Location location);

    /**
     * Check if the location is inside the island's protected area.
     *
     * @param location    The location to check.
     * @param extraRadius Add extra radius to the protected range.
     */
    boolean isInsideRange(Location location, int extraRadius);

    /**
     * Check if the chunk is inside the island's protected area.
     *
     * @param chunk The chunk to check.
     */
    boolean isInsideRange(Chunk chunk);

    /**
     * Is the normal world enabled for the island?
     */
    boolean isNormalEnabled();

    /**
     * Enable/disable the normal world for the island.
     */
    void setNormalEnabled(boolean enabled);

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
     *
     * @param sender          The command-sender to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege);

    /**
     * Check if a player has a permission.
     *
     * @param superiorPlayer  The player to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege);

    /**
     * Check if a role has a permission.
     *
     * @param playerRole      The role to check.
     * @param islandPrivilege The permission to check.
     */
    boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege);

    /**
     * Set a permission to a specific role.
     *
     * @param playerRole      The role to set the permission to.
     * @param islandPrivilege The permission to set.
     * @param value           The value to give the permission (Unused)
     * @deprecated See {@link #setPermission(PlayerRole, IslandPrivilege)}
     */
    @Deprecated
    void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value);

    /**
     * Set a permission to a specific role.
     *
     * @param playerRole      The role to set the permission to.
     * @param islandPrivilege The permission to set.
     */
    void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege);

    /**
     * Reset the roles permissions to default values.
     */
    void resetPermissions();

    /**
     * Set a permission to a specific player.
     *
     * @param superiorPlayer  The player to set the permission to.
     * @param islandPrivilege The permission to set.
     * @param value           The value to give the permission.
     */
    void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value);

    /**
     * Reset player permissions to default values.
     */
    void resetPermissions(SuperiorPlayer superiorPlayer);

    /**
     * Get the permission-node of a player.
     *
     * @param superiorPlayer The player to check.
     */
    PermissionNode getPermissionNode(SuperiorPlayer superiorPlayer);

    /**
     * Get the required role for a specific permission.
     *
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
     * Checks whether the island is the spawn island.
     */
    boolean isSpawn();

    /**
     * Get the name of the island.
     */
    String getName();

    /**
     * Set the name of the island.
     *
     * @param islandName The name to set.
     */
    void setName(String islandName);

    /**
     * Get the name of the island, unformatted.
     */
    String getRawName();

    /**
     * Get the description of the island.
     */
    String getDescription();

    /**
     * Set the description of the island.
     *
     * @param description The description to set.
     */
    void setDescription(String description);

    /**
     * Disband the island.
     */
    void disbandIsland();

    /**
     * Transfer the island's leadership to another player.
     *
     * @param superiorPlayer The player to transfer the leadership to.
     * @return True if the transfer was succeed, otherwise false.
     */
    boolean transferIsland(SuperiorPlayer superiorPlayer);

    /**
     * Replace a player with a new player.
     *
     * @param originalPlayer The old player to be replaced.
     * @param newPlayer      The new player.
     *                       If null, the original player should just be removed.
     *                       If this is the owner of the island, the island will be disbanded.
     */
    void replacePlayers(SuperiorPlayer originalPlayer, @Nullable SuperiorPlayer newPlayer);

    /**
     * Recalculate the island's worth value.
     *
     * @param asker The player who makes the operation.
     */
    void calcIslandWorth(@Nullable SuperiorPlayer asker);

    /**
     * Recalculate the island's worth value.
     *
     * @param asker    The player who makes the operation.
     * @param callback Runnable which will be ran when process is finished.
     */
    void calcIslandWorth(@Nullable SuperiorPlayer asker, @Nullable Runnable callback);

    /**
     * Get the calculation algorithm used by this island.
     */
    IslandCalculationAlgorithm getCalculationAlgorithm();

    /**
     * Update the border of all the players inside the island.
     */
    void updateBorder();

    /**
     * Update the fly status for a player on this island.
     *
     * @param superiorPlayer The player to update.
     */
    void updateIslandFly(SuperiorPlayer superiorPlayer);

    /**
     * Get the island radius of the island.
     */
    int getIslandSize();

    /**
     * Set the radius of the island.
     *
     * @param islandSize The radius for the island.
     */
    void setIslandSize(int islandSize);

    /**
     * Get the island radius of the island that was set with a command.
     */
    int getIslandSizeRaw();

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
     *
     * @param updateBlocks Whether the blocks get updated or not.
     */
    void setBiome(Biome biome, boolean updateBlocks);

    /**
     * Check whether the island is locked to visitors.
     */
    boolean isLocked();

    /**
     * Lock or unlock the island to visitors.
     *
     * @param locked Whether the island should be locked to visitors.
     */
    void setLocked(boolean locked);

    /**
     * Checks whether the island is ignored in the top islands.
     */
    boolean isIgnored();

    /**
     * Set whether the island should be ignored in the top islands.
     */
    void setIgnored(boolean ignored);

    /**
     * Send a plain message to all the members of the island.
     *
     * @param message        The message to send
     * @param ignoredMembers An array of ignored members.
     */
    void sendMessage(String message, UUID... ignoredMembers);

    /**
     * Send a message to all the members of the island.
     *
     * @param messageComponent The message to send
     * @param args             Arguments for the component.
     */
    void sendMessage(IMessageComponent messageComponent, Object... args);

    /**
     * Send a message to all the members of the island.
     *
     * @param messageComponent The message to send
     * @param ignoredMembers   An array of ignored members.
     * @param args             Arguments for the component.
     */
    void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args);

    /**
     * Send a plain message to all the members of the island.
     *
     * @param title          The main title to send.
     * @param subtitle       The sub title to send.
     * @param fadeIn         The fade-in duration in ticks.
     * @param duration       The title duration in ticks.
     * @param fadeOut        The fade-out duration in ticks.
     * @param ignoredMembers An array of ignored members.
     */
    void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration, int fadeOut, UUID... ignoredMembers);

    /**
     * Execute a command on all the members of the island.
     * You can use {player-name} as a placeholder for the member's name.
     *
     * @param command           The command to execute.
     * @param onlyOnlineMembers Whether the command should be executed only for online members.
     * @param ignoredMembers    An array of ignored members.
     */
    void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers);

    /**
     * Checks whether the island is being recalculated currently.
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
     * Set whether the island is currently active.
     * Active islands are islands that have at least one island member online.
     *
     * @param active Whether the island is active.
     */
    void setCurrentlyActive(boolean active);

    /**
     * Check whether the island is currently active.
     * Active islands are islands that have at least one island member online.
     */
    boolean isCurrentlyActive();

    /**
     * Get the last time the island was updated.
     * In case the island is active, -1 will be returned.
     */
    long getLastTimeUpdate();

    /**
     * Set the last time the island was updated.
     *
     * @param lastTimeUpdate The last time the island was updated.
     */
    void setLastTimeUpdate(long lastTimeUpdate);

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
     * Set a new limit for the bank.
     *
     * @param bankLimit The limit to set. Use -1 to remove the limit.
     */
    void setBankLimit(BigDecimal bankLimit);

    /**
     * Get the limit of the bank that was set using a command.
     */
    BigDecimal getBankLimitRaw();

    /**
     * Give the bank interest to this island.
     *
     * @param checkOnlineOwner Check if the island-owner was online recently.
     * @return Whether the money was given.
     */
    boolean giveInterest(boolean checkOnlineOwner);

    /**
     * Get the last time that the bank interest was given.
     */
    long getLastInterestTime();

    /**
     * Set the last time that the bank interest was given.
     *
     * @param lastInterest The time it was given.
     */
    void setLastInterestTime(long lastInterest);

    /**
     * Get the duration until the bank interest will be given again, in seconds
     */
    long getNextInterest();

    /*
     *  Worth related methods
     */

    /**
     * Handle a placement of a block.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block The block that was placed.
     */
    void handleBlockPlace(Block block);

    /**
     * Handle a placement of a block.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block The block that was placed.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Block block);

    /**
     * Handle a placement of a block's key.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key The block's key that was placed.
     */
    void handleBlockPlace(Key key);

    /**
     * Handle a placement of a block's key.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key The block's key that was placed.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Key key);

    /**
     * Handle a placement of a block with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block  The block that was placed.
     * @param amount The amount of the block.
     */
    void handleBlockPlace(Block block, @Size int amount);

    /**
     * Handle a placement of a block with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block  The block that was placed.
     * @param amount The amount of the block.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount);

    /**
     * Handle a placement of a block's key with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     */
    void handleBlockPlace(Key key, @Size int amount);

    /**
     * Handle a placement of a block's key with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount);

    /**
     * Handle a placement of a block with a specific amount.
     *
     * @param block  The block that was placed.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlockPlace(Block block, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a placement of a block with a specific amount.
     *
     * @param block  The block that was placed.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a placement of a block's key with a specific amount.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlockPlace(Key key, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a placement of a block's key with a specific amount.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a placement of a block with a specific amount.
     * This will update the last time status of the island.
     *
     * @param block  The block that was placed.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into database.
     */
    @Deprecated
    void handleBlockPlace(Block block, @Size int amount, boolean save);

    /**
     * Handle a placement of a block's key with a specific amount.
     * This will update the last time status of the island.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into database.
     */
    @Deprecated
    void handleBlockPlace(Key key, @Size int amount, boolean save);

    /**
     * Handle a placement of a block's key with a specific amount.
     * This will update the last time status of the island.
     *
     * @param key    The block's key that was placed.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into database.
     */
    @Deprecated
    void handleBlockPlace(Key key, @Size BigInteger amount, boolean save);

    /**
     * Handle a placement of a block's key with a specific amount.
     *
     * @param key                  The block's key that was placed.
     * @param amount               The amount of the block.
     * @param save                 Whether the block counts should be saved into database.
     * @param updateLastTimeStatus Whether to update last time island was updated or not.
     */
    @Deprecated
    void handleBlockPlace(Key key, @Size BigInteger amount, boolean save, boolean updateLastTimeStatus);

    /**
     * Handle placements of many blocks in one time.
     * This will save the block counts and update the last time status of the island.
     *
     * @param blocks All the blocks to place.
     */
    void handleBlocksPlace(Map<Key, Integer> blocks);

    /**
     * Handle placements of many blocks in one time.
     * This will save the block counts and update the last time status of the island.
     *
     * @param blocks All the blocks to place.
     * @return Results per block key. Only non-successful results will be returned.
     */
    Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks);

    /**
     * Handle placements of many blocks in one time.
     *
     * @param blocks All the blocks to place.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlocksPlace(Map<Key, Integer> blocks, @IslandBlockFlags int flags);

    /**
     * Handle placements of many blocks in one time.
     *
     * @param blocks All the blocks to place.
     * @param flags  See {@link IslandBlockFlags}
     * @return Results per block key. Only non-successful results will be returned.
     */
    Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks, @IslandBlockFlags int flags);

    /**
     * Handle a break of a block.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block The block that was broken.
     */
    void handleBlockBreak(Block block);

    /**
     * Handle a break of a block.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block The block that was broken.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockBreakWithResult(Block block);

    /**
     * Handle a break of a block's key.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key The block's key that was broken.
     */
    void handleBlockBreak(Key key);

    /**
     * Handle a break of a block's key.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key The block's key that was broken.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockBreakWithResult(Key key);

    /**
     * Handle a break of a block with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block  The block that was broken.
     * @param amount The amount of the block.
     */
    void handleBlockBreak(Block block, @Size int amount);

    /**
     * Handle a break of a block with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param block  The block that was broken.
     * @param amount The amount of the block.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount);

    /**
     * Handle a break of a block's key with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     */
    void handleBlockBreak(Key key, @Size int amount);

    /**
     * Handle a break of a block's key with a specific amount.
     * This will save the block counts and update the last time status of the island.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     * @return The result of the block place.
     */
    BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount);

    /**
     * Handle a break of a block with a specific amount.
     *
     * @param block  The block that was broken.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlockBreak(Block block, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a break of a block with a specific amount.
     *
     * @param block  The block that was broken.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a break of a block's key with a specific amount.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlockBreak(Key key, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a break of a block's key with a specific amount.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     * @param flags  See {@link IslandBlockFlags}
     */
    BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount, @IslandBlockFlags int flags);

    /**
     * Handle a break of a block with a specific amount.
     * This will update the last time status of the island.
     *
     * @param block  The block that was broken.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into the database.
     */
    @Deprecated
    void handleBlockBreak(Block block, @Size int amount, boolean save);

    /**
     * Handle a break of a block with a specific amount.
     * This will update the last time status of the island.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into the database.
     */
    @Deprecated
    void handleBlockBreak(Key key, @Size int amount, boolean save);

    /**
     * Handle a break of a block with a specific amount.
     * This will update the last time status of the island.
     *
     * @param key    The block's key that was broken.
     * @param amount The amount of the block.
     * @param save   Whether the block counts should be saved into the database.
     */
    @Deprecated
    void handleBlockBreak(Key key, @Size BigInteger amount, boolean save);

    /**
     * Handle break of many blocks in one time.
     * This will save the block counts and update the last time status of the island.
     *
     * @param blocks All the blocks to break.
     */
    void handleBlocksBreak(Map<Key, Integer> blocks);

    /**
     * Handle break of many blocks in one time.
     * This will save the block counts and update the last time status of the island.
     *
     * @param blocks All the blocks to break.
     * @return Results per block key. Only non-successful results will be returned.
     */
    Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks);

    /**
     * Handle break of many blocks in one time.
     *
     * @param blocks All the blocks to break.
     * @param flags  See {@link IslandBlockFlags}
     */
    void handleBlocksBreak(Map<Key, Integer> blocks, @IslandBlockFlags int flags);

    /**
     * Handle break of many blocks in one time.
     *
     * @param blocks All the blocks to break.
     * @param flags  See {@link IslandBlockFlags}
     * @return Results per block key. Only non-successful results will be returned.
     */
    Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks, @IslandBlockFlags int flags);

    /**
     * Check whether a chunk has blocks inside it.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     */
    boolean isChunkDirty(World world, int chunkX, int chunkZ);

    /**
     * Check whether a chunk has blocks inside it.
     *
     * @param worldName The name of the world of the chunk.
     * @param chunkX    The x-coords of the chunk.
     * @param chunkZ    The z-coords of the chunk.
     */
    boolean isChunkDirty(String worldName, int chunkX, int chunkZ);

    /**
     * Mark a chunk as it has blocks inside it.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     * @param save   Whether to save the changes to database.
     */
    void markChunkDirty(World world, int chunkX, int chunkZ, boolean save);

    /**
     * Mark a chunk as it has no blocks inside it.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     * @param save   Whether to save the changes to database.
     */
    void markChunkEmpty(World world, int chunkX, int chunkZ, boolean save);

    /**
     * Get the amount of blocks that are on the island.
     *
     * @param key The block's key to check.
     */
    BigInteger getBlockCountAsBigInteger(Key key);

    /**
     * Get all the blocks that are on the island.
     */
    Map<Key, BigInteger> getBlockCountsAsBigInteger();

    /**
     * Get the amount of blocks that are on the island.
     * Unlike getBlockCount(Key), this method returns the count for
     * the exactly block that is given as a parameter.
     *
     * @param key The block's key to check.
     */
    BigInteger getExactBlockCountAsBigInteger(Key key);

    /**
     * Clear all the block counts of the island.
     */
    void clearBlockCounts();

    /**
     * Get the blocks-tracker used by this island.
     */
    IslandBlocksTrackerAlgorithm getBlocksTracker();

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
     *
     * @param bonusWorth The bonus to give.
     */
    void setBonusWorth(BigDecimal bonusWorth);

    /**
     * Get the bonus level of the island.
     */
    BigDecimal getBonusLevel();

    /**
     * Set a bonus level for the island.
     *
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
     *
     * @param upgrade The upgrade to check.
     */
    UpgradeLevel getUpgradeLevel(Upgrade upgrade);

    /**
     * Set the level of an upgrade for the island.
     *
     * @param upgrade The upgrade to set the level.
     * @param level   The level to set.
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
     * Get the last time the island was upgraded.
     */
    long getLastTimeUpgrade();

    /**
     * Check if the island has an active upgrade cooldown.
     */
    boolean hasActiveUpgradeCooldown();

    /**
     * Get the crop-growth multiplier for the island.
     */
    double getCropGrowthMultiplier();

    /**
     * Set the crop-growth multiplier for the island.
     *
     * @param cropGrowth The multiplier to set.
     */
    void setCropGrowthMultiplier(double cropGrowth);

    /**
     * Get the crop-growth multiplier for the island that was set using a command.
     */
    double getCropGrowthRaw();

    /**
     * Get the spawner-rates multiplier for the island.
     */
    double getSpawnerRatesMultiplier();

    /**
     * Set the spawner-rates multiplier for the island.
     *
     * @param spawnerRates The multiplier to set.
     */
    void setSpawnerRatesMultiplier(double spawnerRates);

    /**
     * Get the spawner-rates multiplier for the island that was set using a command.
     */
    double getSpawnerRatesRaw();

    /**
     * Get the mob-drops multiplier for the island.
     */
    double getMobDropsMultiplier();

    /**
     * Set the mob-drops multiplier for the island.
     *
     * @param mobDrops The multiplier to set.
     */
    void setMobDropsMultiplier(double mobDrops);

    /**
     * Get the mob-drops multiplier for the island that was set using a command.
     */
    double getMobDropsRaw();

    /**
     * Get the block limit of a block.
     *
     * @param key The block's key to check.
     */
    int getBlockLimit(Key key);

    /**
     * Get the block limit of a block.
     * Unlike getBlockLimit(Key), this method returns the count for
     * the exactly block that is given as a parameter.
     *
     * @param key The block's key to check.
     */
    int getExactBlockLimit(Key key);

    /**
     * Get the block key used as a limit for another block key.
     *
     * @param key The block's key to check.
     */
    Key getBlockLimitKey(Key key);

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
     *
     * @param key   The block's key to set the limit to.
     * @param limit The limit to set.
     */
    void setBlockLimit(Key key, int limit);

    /**
     * Remove the limit of a block.
     *
     * @param key The block's key to remove it's limit.
     */
    void removeBlockLimit(Key key);

    /**
     * A method to check if a specific block has reached the limit.
     * This method checks for the block and it's global block key.
     *
     * @param key The block's key to check.
     */
    boolean hasReachedBlockLimit(Key key);

    /**
     * A method to check if a specific block has reached the limit.
     * This method checks for the block and it's global block key.
     *
     * @param key    The block's key to check.
     * @param amount Amount of the block to be placed.
     */
    boolean hasReachedBlockLimit(Key key, int amount);

    /**
     * Get the entity limit of an entity.
     *
     * @param entityType The entity's type to check.
     */
    int getEntityLimit(EntityType entityType);

    /**
     * Get the entity limit of an entity.
     *
     * @param key The key of the entity to check.
     */
    int getEntityLimit(Key key);

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
     *
     * @param entityType The entity's type to set the limit to.
     * @param limit      The limit to set.
     */
    void setEntityLimit(EntityType entityType, int limit);

    /**
     * Set the entity limit of an entity.
     *
     * @param key   The key of the entity to set the limit to.
     * @param limit The limit to set.
     */
    void setEntityLimit(Key key, int limit);

    /**
     * A method to check if a specific entity has reached the limit.
     *
     * @param entityType The entity's type to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType);

    /**
     * A method to check if a specific entity has reached the limit.
     *
     * @param key The key of the entity to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(Key key);

    /**
     * A method to check if a specific entity has reached the limit.
     *
     * @param amount     The amount of entities that were added.
     * @param entityType The entity's type to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount);

    /**
     * A method to check if a specific entity has reached the limit.
     *
     * @param amount The amount of entities that were added.
     * @param key    The key of the entity to check.
     */
    CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount);

    /**
     * Get the entities tracker used by the island.
     */
    IslandEntitiesTrackerAlgorithm getEntitiesTracker();

    /**
     * Get the team limit of the island.
     */
    int getTeamLimit();

    /**
     * Set the team limit of the island.
     *
     * @param teamLimit The team limit to set.
     */
    void setTeamLimit(int teamLimit);

    /**
     * Get the team limit of the island that was set with a command.
     */
    int getTeamLimitRaw();

    /**
     * Get the warps limit of the island.
     */
    int getWarpsLimit();

    /**
     * Set the warps limit for the island.
     *
     * @param warpsLimit The limit to set.
     */
    void setWarpsLimit(int warpsLimit);

    /**
     * Get the warps limit of the island that was set using a command.
     */
    int getWarpsLimitRaw();

    /**
     * Add a potion effect to the island.
     *
     * @param type  The potion effect to add.
     * @param level The level of the potion effect.
     *              If the level is 0 or below, then the effect will be removed.
     */
    void setPotionEffect(PotionEffectType type, int level);

    /**
     * Remove a potion effect from the island.
     *
     * @param type The potion effect to remove.
     */
    void removePotionEffect(PotionEffectType type);

    /**
     * Get the level of an island effect.
     *
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
     * If the player is offline, nothing will happen.
     *
     * @param superiorPlayer The player to give the effect to.
     */
    void applyEffects(SuperiorPlayer superiorPlayer);

    /**
     * Give all the island effects to the players inside the island.
     */
    void applyEffects();

    /**
     * Remove all the island effects from a player.
     * If the player is offline, nothing will happen.
     *
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

    /**
     * Set the limit of the amount of players that can have the role in the island.
     *
     * @param playerRole The role to set the limit to.
     * @param limit      The limit to set.
     */
    void setRoleLimit(PlayerRole playerRole, int limit);

    /**
     * Remove the limit of the amount of players that can have the role in the island.
     *
     * @param playerRole The role to remove the limit.
     */
    void removeRoleLimit(PlayerRole playerRole);

    /**
     * Get the limit of players that can have the same role at a time.
     *
     * @param playerRole The role to check.
     */
    int getRoleLimit(PlayerRole playerRole);

    /**
     * Get the limit of players that can have the same role at a time that was set using a command.
     *
     * @param playerRole The role to check.
     */
    int getRoleLimitRaw(PlayerRole playerRole);

    /**
     * Get all the role limits for the island.
     */
    Map<PlayerRole, Integer> getRoleLimits();

    /**
     * Get all the custom role limits for the island.
     */
    Map<PlayerRole, Integer> getCustomRoleLimits();

    /*
     *  Warps related methods
     */

    /**
     * Create a new warp category.
     * If a category already exists, it will be returned instead of a new created one.
     *
     * @param name The name of the category.
     */
    WarpCategory createWarpCategory(String name);

    /**
     * Get a warp category.
     *
     * @param name The name of the category.
     */
    @Nullable
    WarpCategory getWarpCategory(String name);

    /**
     * Get a warp category by the slot inside the manage menu.
     *
     * @param slot The slot to check.
     */
    @Nullable
    WarpCategory getWarpCategory(int slot);

    /**
     * Rename a category.
     *
     * @param warpCategory The category to rename.
     * @param newName      A new name to set.
     */
    void renameCategory(WarpCategory warpCategory, String newName);

    /**
     * Delete a warp category.
     * All the warps inside it will be deleted as well.
     *
     * @param warpCategory The category to delete.
     */
    void deleteCategory(WarpCategory warpCategory);

    /**
     * Get all the warp categories of the island.
     */
    Map<String, WarpCategory> getWarpCategories();

    /**
     * Create a warp for the island.
     *
     * @param name         The name of the warp.
     * @param location     The location of the warp.
     * @param warpCategory The category to add the island.
     * @return The new island warp object.
     */
    IslandWarp createWarp(String name, Location location, @Nullable WarpCategory warpCategory);

    /**
     * Rename a warp.
     *
     * @param islandWarp The warp to rename.
     * @param newName    A new name to set.
     */
    void renameWarp(IslandWarp islandWarp, String newName);

    /**
     * Get an island warp in a specific location.
     *
     * @param location The location to check.
     */
    @Nullable
    IslandWarp getWarp(Location location);

    /**
     * Get an island warp by it's name..
     *
     * @param name The name to check.
     */
    @Nullable
    IslandWarp getWarp(String name);

    /**
     * Teleport a player to a warp.
     *
     * @param superiorPlayer The player to teleport.
     * @param warp           The warp's name to teleport the player to.
     */
    void warpPlayer(SuperiorPlayer superiorPlayer, String warp);

    /**
     * Delete a warp from the island.
     *
     * @param superiorPlayer The player who requested the operation.
     * @param location       The location of the warp.
     */
    void deleteWarp(@Nullable SuperiorPlayer superiorPlayer, Location location);

    /**
     * Delete a warp from the island.
     *
     * @param name The warp's name to delete.
     */
    void deleteWarp(String name);

    /**
     * Get all the warps of the island.
     */
    Map<String, IslandWarp> getIslandWarps();

    /*
     *  Ratings related methods
     */

    /**
     * Get the rating that a player has given the island.
     *
     * @param superiorPlayer The player to check.
     */
    Rating getRating(SuperiorPlayer superiorPlayer);

    /**
     * Set a rating of a player.
     *
     * @param superiorPlayer The player that sets the rating.
     * @param rating         The rating to set.
     */
    void setRating(SuperiorPlayer superiorPlayer, Rating rating);

    /**
     * Remove a rating of a player.
     *
     * @param superiorPlayer The player to remove the rating of.
     */
    void removeRating(SuperiorPlayer superiorPlayer);

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
     *  Settings related methods
     */

    /**
     * Check whether a settings is enabled or not.
     *
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
     *
     * @param islandFlag The settings to enable.
     */
    void enableSettings(IslandFlag islandFlag);

    /**
     * Disable an island settings.
     *
     * @param islandFlag The settings to disable.
     */
    void disableSettings(IslandFlag islandFlag);

    /*
     *  Generator related methods
     */

    /**
     * Set a percentage for a specific key in a specific world.
     * Percentage can be between 0 and 100 (0 will remove the key from the list).
     * Calling this method will not make events get fired.
     * <p>
     * This function sets the amount of the key using the following formula:
     * amount = (percentage * total_amount) / (1 - percentage)
     * <p>
     * If the percentage is 100, the rest of the amounts will be cleared and
     * the material's amount will be set to 1.
     * <p>
     * The amount is rounded to ensure a smaller loss, and currently it's 1%~ loss.
     *
     * @param key         The block to change the generator rate of.
     * @param percentage  The percentage to set the new rate.
     * @param environment The world to change the rates in.
     */
    void setGeneratorPercentage(Key key, int percentage, World.Environment environment);

    /**
     * Set a percentage for a specific key in a specific world.
     * Percentage can be between 0 and 100 (0 will remove the key from the list).
     * <p>
     * This function sets the amount of the key using the following formula:
     * amount = (percentage * total_amount) / (1 - percentage)
     * <p>
     * If the percentage is 100, the rest of the amounts will be cleared and
     * the material's amount will be set to 1.
     * <p>
     * The amount is rounded to ensure a smaller loss, and currently it's 1%~ loss.
     *
     * @param key         The block to change the generator rate of.
     * @param percentage  The percentage to set the new rate.
     * @param environment The world to change the rates in.
     * @param caller      The player that changes the percentages (used for the event).
     *                    If null, it means the console did the operation.
     * @param callEvent   Whether to call the {@link com.bgsoftware.superiorskyblock.api.events.IslandChangeGeneratorRateEvent}
     * @return Whether the operation succeed.
     * The operation may fail if callEvent is true and the event was cancelled.
     */
    boolean setGeneratorPercentage(Key key, int percentage, World.Environment environment,
                                   @Nullable SuperiorPlayer caller, boolean callEvent);

    /**
     * Get the percentage for a specific key in a specific world.
     * The formula is (amount * 100) / total_amount.
     *
     * @param key         The material key
     * @param environment The world environment.
     */
    int getGeneratorPercentage(Key key, World.Environment environment);

    /**
     * Get the percentages of the materials for the cobblestone generator in the island for a specific world.
     */
    Map<String, Integer> getGeneratorPercentages(World.Environment environment);

    /**
     * Set an amount for a specific key in a specific world.
     */
    void setGeneratorAmount(Key key, @Size int amount, World.Environment environment);

    /**
     * Remove a rate for a specific key in a specific world.
     */
    void removeGeneratorAmount(Key key, World.Environment environment);

    /**
     * Get the amount of a specific key in a specific world.
     */
    int getGeneratorAmount(Key key, World.Environment environment);

    /**
     * Get the total amount of all the generator keys together.
     */
    int getGeneratorTotalAmount(World.Environment environment);

    /**
     * Get the amounts of the materials for the cobblestone generator in the island.
     */
    Map<String, Integer> getGeneratorAmounts(World.Environment environment);

    /**
     * Get the custom amounts of the materials for the cobblestone generator in the island.
     */
    Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment);

    /**
     * Clear all the custom generator amounts for this island.
     */
    void clearGeneratorAmounts(World.Environment environment);

    /**
     * Generate a block at a specified location.
     * The method calculates a block to generate from {@link #getGeneratorAmounts(World.Environment)}.
     * It doesn't look for any conditions for generating it - lava, water, etc are not required.
     * The method will fail if there are no valid generator rates for the environment.
     *
     * @param location            The location to generate block at.
     * @param optimizeCobblestone When set to true and cobblestone needs to be generated, the plugin will
     *                            not play effects, count the block towards the block counts or set the block.
     *                            This is useful when calling the method from BlockFromToEvent, and instead of letting
     *                            the player do the logic of vanilla, the plugin lets the game do it.
     * @return The block type that was generated, null if failed.
     */
    @Nullable
    Key generateBlock(Location location, boolean optimizeCobblestone);


    /**
     * Generate a block at a specified location.
     * The method calculates a block to generate from {@link #getGeneratorAmounts(World.Environment)}.
     * It doesn't look for any conditions for generating it - lava, water, etc are not required.
     * The method will fail if there are no valid generator rates for the environment.
     *
     * @param location            The location to generate block at.
     * @param environment         The world to get generator rates from.
     * @param optimizeCobblestone When set to true and cobblestone needs to be generated, the plugin will
     *                            not play effects, count the block towards the block counts or set the block.
     *                            This is useful when calling the method from BlockFromToEvent, and instead of letting
     *                            the player do the logic of vanilla, the plugin lets the game do it.
     * @return The block type that was generated, null if failed.
     */
    @Nullable
    Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone);

    /*
     *  Schematic methods
     */

    /**
     * Checks if a schematic was generated already.
     *
     * @param environment The environment to check.
     */
    boolean wasSchematicGenerated(World.Environment environment);

    /**
     * Set schematic generated flag to true.
     *
     * @param environment The environment to set.
     */
    void setSchematicGenerate(World.Environment environment);

    /**
     * Set schematic generated flag.
     *
     * @param environment The environment to set.
     * @param generated   The flag to set.
     */
    void setSchematicGenerate(World.Environment environment, boolean generated);

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
     *
     * @param index The index of the page (0 or above)
     * @param rows  The amount of rows for that page.
     */
    void setChestRows(int index, int rows);

    /**
     * Create a new builder for a {@link Island} object.
     */
    static Builder newBuilder() {
        return SuperiorSkyblockAPI.getFactory().createIslandBuilder();
    }

    /**
     * The {@link Builder} interface is used to create {@link Island} objects with predefined values.
     * All of its methods are setters for all the values possible to create an island with.
     * Use {@link Builder#build()} to create the new {@link Island} object. You must set
     * {@link Builder#setOwner(SuperiorPlayer)}, {@link Builder#setUniqueId(UUID)} and
     * {@link Builder#setCenter(Location)} before creating a new {@link Island}
     */
    interface Builder {

        Builder setOwner(@Nullable SuperiorPlayer owner);

        @Nullable
        SuperiorPlayer getOwner();

        Builder setUniqueId(UUID uuid);

        UUID getUniqueId();

        Builder setCenter(Location center);

        Location getCenter();

        Builder setName(String islandName);

        String getName();

        Builder setSchematicName(String schematicName);

        String getScehmaticName();

        Builder setCreationTime(long creationTime);

        long getCreationTime();

        Builder setDiscord(String discord);

        String getDiscord();

        Builder setPaypal(String paypal);

        String getPaypal();

        Builder setBonusWorth(BigDecimal bonusWorth);

        BigDecimal getBonusWorth();

        Builder setBonusLevel(BigDecimal bonusLevel);

        BigDecimal getBonusLevel();

        Builder setLocked(boolean isLocked);

        boolean isLocked();

        Builder setIgnored(boolean isIgnored);

        boolean isIgnored();

        Builder setDescription(String description);

        String getDescription();

        Builder setGeneratedSchematics(int generatedSchematicsMask);

        int getGeneratedSchematicsMask();

        Builder setUnlockedWorlds(int unlockedWorldsMask);

        int getUnlockedWorldsMask();

        Builder setLastTimeUpdated(long lastTimeUpdated);

        long getLastTimeUpdated();

        Builder setDirtyChunk(String worldName, int chunkX, int chunkZ);

        boolean isDirtyChunk(String worldName, int chunkX, int chunkZ);

        Builder setBlockCount(Key block, BigInteger count);

        KeyMap<BigInteger> getBlockCounts();

        Builder setIslandHome(Location location, World.Environment environment);

        Map<World.Environment, Location> getIslandHomes();

        Builder addIslandMember(SuperiorPlayer superiorPlayer);

        List<SuperiorPlayer> getIslandMembers();

        Builder addBannedPlayer(SuperiorPlayer superiorPlayer);

        List<SuperiorPlayer> getBannedPlayers();

        Builder setPlayerPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value);

        Map<SuperiorPlayer, PermissionNode> getPlayerPermissions();

        Builder setRolePermission(IslandPrivilege islandPrivilege, PlayerRole requiredRole);

        Map<IslandPrivilege, PlayerRole> getRolePermissions();

        Builder setUpgrade(Upgrade upgrade, int level);

        Map<Upgrade, Integer> getUpgrades();

        Builder setBlockLimit(Key block, int limit);

        KeyMap<Integer> getBlockLimits();

        Builder setRating(SuperiorPlayer superiorPlayer, Rating rating);

        Map<SuperiorPlayer, Rating> getRatings();

        Builder setCompletedMission(Mission<?> mission, int finishCount);

        Map<Mission<?>, Integer> getCompletedMissions();

        Builder setIslandFlag(IslandFlag islandFlag, boolean value);

        Map<IslandFlag, SyncStatus> getIslandFlags();

        Builder setGeneratorRate(Key block, int rate, World.Environment environment);

        Map<World.Environment, KeyMap<Integer>> getGeneratorRates();

        Builder addUniqueVisitor(SuperiorPlayer superiorPlayer, long visitTime);

        Map<SuperiorPlayer, Long> getUniqueVisitors();

        Builder setEntityLimit(Key entity, int limit);

        KeyMap<Integer> getEntityLimits();

        Builder setIslandEffect(PotionEffectType potionEffectType, int level);

        Map<PotionEffectType, Integer> getIslandEffects();

        Builder setIslandChest(int index, ItemStack[] contents);

        List<ItemStack[]> getIslandChests();

        Builder setRoleLimit(PlayerRole playerRole, int limit);

        Map<PlayerRole, Integer> getRoleLimits();

        Builder setVisitorHome(Location location, World.Environment environment);

        Map<World.Environment, Location> getVisitorHomes();

        Builder setIslandSize(int islandSize);

        int getIslandSize();

        Builder setTeamLimit(int teamLimit);

        int getTeamLimit();

        Builder setWarpsLimit(int warpsLimit);

        int getWarpsLimit();

        Builder setCropGrowth(double cropGrowth);

        double getCropGrowth();

        Builder setSpawnerRates(double spawnerRates);

        double getSpawnerRates();

        Builder setMobDrops(double mobDrops);

        double getMobDrops();

        Builder setCoopLimit(int coopLimit);

        int getCoopLimit();

        Builder setBankLimit(BigDecimal bankLimit);

        BigDecimal getBankLimit();

        Builder setBalance(BigDecimal balance);

        BigDecimal getBalance();

        Builder setLastInterestTime(long lastInterestTime);

        long getLastInterestTime();

        Builder addWarp(String name, String category, Location location, boolean isPrivate, @Nullable ItemStack icon);

        boolean hasWarp(String name);

        boolean hasWarp(Location location);

        Builder addWarpCategory(String name, int slot, @Nullable ItemStack icon);

        boolean hasWarpCategory(String name);

        Builder addBankTransaction(BankTransaction bankTransaction);

        List<BankTransaction> getBankTransactions();

        Builder setPersistentData(byte[] persistentData);

        byte[] getPersistentData();

        Island build();


    }

}