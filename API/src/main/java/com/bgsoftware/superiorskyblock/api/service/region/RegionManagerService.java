package com.bgsoftware.superiorskyblock.api.service.region;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public interface RegionManagerService {

    InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block);

    InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block);

    InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action,
                                          @Nullable ItemStack usedItem);

    InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block);

    InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem);

    InteractionResult handleEntityDamage(Entity damager, Entity entity);

    InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity);

    InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity);

    InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item);

    InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item);

    InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination);

    InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location, IslandPrivilege islandPrivilege);

    MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to);

    MoveResult handlePlayerTeleport(SuperiorPlayer superiorPlayer, Location from, Location to);

    MoveResult handlePlayerJoin(SuperiorPlayer superiorPlayer);

    MoveResult handlePlayerJoin(SuperiorPlayer superiorPlayer, Location location);

    MoveResult handlePlayerQuit(SuperiorPlayer superiorPlayer);

    MoveResult handlePlayerQuit(SuperiorPlayer superiorPlayer, Location location);

}
