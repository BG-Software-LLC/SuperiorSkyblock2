package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.factory.BanksFactory;
import com.bgsoftware.superiorskyblock.api.factory.DatabaseBridgeFactory;
import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.factory.PlayersFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import java.math.BigDecimal;
import java.util.UUID;

public interface FactoriesManager {

    /**
     * Register a custom islands factory.
     *
     * @param islandsFactory The new factory to set.
     *                       If set to null, the default factory will be used.
     */
    void registerIslandsFactory(@Nullable IslandsFactory islandsFactory);

    /**
     * Get the current islands factory.
     */
    IslandsFactory getIslandsFactory();

    /**
     * Register a custom players factory.
     *
     * @param playersFactory The new factory to set.
     *                       If set to null, the default factory will be used.
     */
    void registerPlayersFactory(@Nullable PlayersFactory playersFactory);

    /**
     * Get the current players factory.
     */
    PlayersFactory getPlayersFactory();

    /**
     * Register a custom banks factory.
     *
     * @param banksFactory The new factory to set.
     *                     If set to null, the default factory will be used.
     */
    void registerBanksFactory(@Nullable BanksFactory banksFactory);

    /**
     * Get the current banks factory.
     */
    BanksFactory getBanksFactory();

    /**
     * Register a custom database-bridge factory.
     *
     * @param databaseBridgeFactory The new factory to set.
     *                              If set to null, the default factory will be used.
     */
    void registerDatabaseBridgeFactory(@Nullable DatabaseBridgeFactory databaseBridgeFactory);

    /**
     * Get the database bridge factory.
     */
    DatabaseBridgeFactory getDatabaseBridgeFactory();

    /**
     * Create a new Island object.
     * Warning: This island is not saved into the database unless inserting it manually!
     *
     * @param owner      The owner of the island.
     * @param uuid       The uuid of the island.
     * @param center     The location of the island.
     * @param islandName The name of the island.
     * @param schemName  The schematic used to create the island.
     */
    Island createIsland(@Nullable SuperiorPlayer owner, UUID uuid, Location center, String islandName, String schemName);

    /**
     * Create a new builder for a {@link Island} object.
     */
    Island.Builder createIslandBuilder();

    /**
     * Create a new SuperiorPlayer object.
     * Warning: This player is not saved into the database unless inserting it manually!
     *
     * @param playerUUID The uuid of the player.
     */
    SuperiorPlayer createPlayer(UUID playerUUID);

    /**
     * Create a new builder for a {@link SuperiorPlayer} object.
     */
    SuperiorPlayer.Builder createPlayerBuilder();

    /**
     * Create a {@link BlockOffset} object from given offsets.
     *
     * @param offsetX The x-coords offset.
     * @param offsetY The y-coords offset.
     * @param offsetZ The z-coords offset.
     */
    BlockOffset createBlockOffset(int offsetX, int offsetY, int offsetZ);

    /**
     * Create a {@link BlockPosition} object from given block coords.
     *
     * @param world  The name of the world of the block.
     * @param blockX The x-coords of the block.
     * @param blockY The y-coords of the block.
     * @param blockZ The z-coords of the block.
     */
    BlockPosition createBlockPosition(String world, int blockX, int blockY, int blockZ);

    /**
     * Create a {@link BlockPosition} object from a location.
     *
     * @param location The location.
     */
    BlockPosition createBlockPosition(Location location);

    /**
     * Create a new bank transaction.
     *
     * @param player        The player that made the transaction.
     *                      Can be null if console made it.
     * @param action        The transaction action
     * @param position      The position of the transaction.
     * @param time          The time the transaction was made.
     * @param failureReason The reason of failure for this transaction, if exists.
     *                      On successful transactions, empty string should be set.
     * @param amount        The amount of money that was transferred in this transaction.
     */
    BankTransaction createTransaction(@Nullable UUID player, BankAction action, int position,
                                      long time, String failureReason, BigDecimal amount);

    /**
     * Create a new world info.
     *
     * @param worldName   The name of the world.
     * @param environment The environment of the world.
     */
    WorldInfo createWorldInfo(String worldName, World.Environment environment);

    /**
     * Create a new game sound instance.
     *
     * @param sound  The sound to play.
     * @param volume The volume to play the sound.
     * @param pitch  The pitch to play the sound.
     * @return
     */
    GameSound createGameSound(Sound sound, float volume, float pitch);

}
