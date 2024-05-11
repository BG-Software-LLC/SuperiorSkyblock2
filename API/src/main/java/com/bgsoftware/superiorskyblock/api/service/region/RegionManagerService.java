package com.bgsoftware.superiorskyblock.api.service.region;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface RegionManagerService {

    /**
     * Handle a block being placed.
     *
     * @param superiorPlayer The player that placed the block.
     * @param block          The block that was placed.
     * @return The result of the interaction.
     */
    InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block);

    /**
     * Handle a block being broken.
     *
     * @param superiorPlayer The player that broke the block.
     * @param block          The block that was broken.
     * @return The result of the interaction.
     */
    InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block);

    /**
     * Handle a player interacting with a block.
     *
     * @param superiorPlayer The player that made the interaction.
     * @param block          The block that the player interacted with.
     * @param action         The action that the player did.
     * @param usedItem       The item that was used to interact.
     * @return The result of the interaction.
     */
    InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action,
                                          @Nullable ItemStack usedItem);

    /**
     * Handle a player fertilizing a block.
     *
     * @param superiorPlayer The player that fertilized the block.
     * @param block          The block that was fertilized by the player.
     * @return The result of the interaction.
     */
    InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block);

    /**
     * Handle a player interacting with an entity.
     *
     * @param superiorPlayer The player that made the interaction.
     * @param entity         The entity that the player interacted with.
     * @param usedItem       The item that was used to interact.
     * @return The result of the interaction.
     */
    InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem);

    /**
     * Handle a player shearing an entity.
     *
     * @param superiorPlayer The player that sheared the entity.
     * @param entity         The entity that was sheared.
     * @return The result of the interaction.
     */
    InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity);

    /**
     * Handle a player leashing an entity.
     *
     * @param superiorPlayer The player that leashed the entity.
     * @param entity         The entity that was leashed.
     * @return The result of the interaction.
     */
    InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity);

    /**
     * Handle a player picking up an item.
     *
     * @param superiorPlayer The player that picked up the item.
     * @param item           The item that was picked up.
     * @return The result of the interaction.
     */
    InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item);

    /**
     * Handle a player dropping an item.
     *
     * @param superiorPlayer The player that dropped the item.
     * @param item           The item that was dropped.
     * @return The result of the interaction.
     */
    InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item);

    /**
     * Handle a player using an ender pearl.
     *
     * @param superiorPlayer The player that used the ender pearl.
     * @param destination    The location of the teleportation of the ender pearl.
     * @return The result of the interaction.
     */
    InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination);

    /**
     * Handle a player consuming a Chorus Fruit.
     *
     * @param superiorPlayer The player that consumed the Chorus Fruit.
     * @param location       The location of the player.
     * @return The result of the interaction.
     */
    InteractionResult handlePlayerConsumeChorusFruit(SuperiorPlayer superiorPlayer, Location location);

    /**
     * Handle an entity damaging another entity.
     *
     * @param damager The entity that dealt the damage.
     * @param entity  The entity that was damaged.
     * @return The result of the interaction.
     */
    InteractionResult handleEntityDamage(Entity damager, Entity entity);

    /**
     * Handle a custom interaction of a player.
     *
     * @param superiorPlayer  The player that made the interaction.
     * @param location        The location of the interaction.
     * @param islandPrivilege The privilege required for doing the interaction.
     * @return The result of the interaction.
     */
    InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location, IslandPrivilege islandPrivilege);

    /**
     * Handle a player movement.
     *
     * @param superiorPlayer The player that made the movement.
     * @param from           The location of the player.
     * @param to             The new location of the player after the movement.
     * @return The result of the movement.
     */
    MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to);

    /**
     * Handle a player teleportation.
     *
     * @param superiorPlayer The player that made the teleportation.
     * @param from           The location of the player.
     * @param to             The new location of the player after the teleportation.
     * @return The result of the teleportation.
     */
    MoveResult handlePlayerTeleport(SuperiorPlayer superiorPlayer, Location from, Location to);

    /**
     * Handle a player teleportation by a portal.
     *
     * @param superiorPlayer   The player that made the teleportation.
     * @param portalLocation   The location of the portal.
     * @param teleportLocation The new location of the player after the teleportation.
     * @return The result of the teleportation.
     */
    MoveResult handlePlayerTeleportByPortal(SuperiorPlayer superiorPlayer, Location portalLocation, Location teleportLocation);

    /**
     * Handle a player joining the server.
     *
     * @param superiorPlayer The player that joined the server.
     * @param location       The location of the player.
     * @return The result of joining the server.
     */
    MoveResult handlePlayerJoin(SuperiorPlayer superiorPlayer, Location location);

    /**
     * Handle a player leaving the server.
     *
     * @param superiorPlayer The player that left the server.
     * @param location       The location of the player.
     * @return The result of leaving the server.
     */
    MoveResult handlePlayerQuit(SuperiorPlayer superiorPlayer, Location location);

}
