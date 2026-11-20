package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.MutableBlockPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class IslandOutsideListener extends AbstractGameEventListener {

    public IslandOutsideListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);

        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.NORMAL, this::onMinecartRightClick);
        registerCallback(GameEventType.ENTITY_RIDE_EVENT, GameEventPriority.NORMAL, this::onEntityRide);
        registerCallback(GameEventType.ENTITY_MOVE_EVENT, GameEventPriority.NORMAL, this::onEntityMove);
    }

    private void onMinecartRightClick(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Entity rightClicked = e.getArgs().clickedEntity;
        if (rightClicked == null)
            return;

        if (!plugin.getGrid().isIslandsWorld(rightClicked.getWorld()))
            return;

        SuperiorPlayer superiorPlayer = e.getArgs().superiorPlayerResolver.get();
        if (superiorPlayer.hasBypassModeEnabled())
            return;

        Island entityIsland = e.getArgs().entityIslandResolver.get();

        if (entityIsland != null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location entityLocation = rightClicked.getLocation(wrapper.getHandle());
                try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                    if (entityIsland.isInsideRange(blockPosition.set(entityLocation), 1D))
                        return;
                }
            }
        }

        e.setCancelled();
    }

    private void onEntityRide(GameEvent<GameEventArgs.EntityRideEvent> e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Entity vehicle = e.getArgs().vehicle;

        if (!plugin.getGrid().isIslandsWorld(vehicle.getWorld()))
            return;

        Entity entity = e.getArgs().entity;

        if (entity instanceof Player && plugin.getPlayers().getSuperiorPlayer(entity).hasBypassModeEnabled())
            return;

        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain();
             ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = entity.getLocation(wrapper.getHandle());
            Island island = WorldBlocks.findIslandWithHint(
                    location,
                    blockPosition.set(location),
                    e.getArgs().islandResolver.get());
            if (island != null && !island.isInsideRange(blockPosition, 1D))
                e.setCancelled();
        }
    }

    private void onEntityMove(GameEvent<GameEventArgs.EntityMoveEvent> e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Location to = e.getArgs().to;
        World destinationWorld = to.getWorld();

        if (!plugin.getGrid().isIslandsWorld(destinationWorld))
            return;

        Entity entity = e.getArgs().entity;

        if (!entity.getWorld().equals(destinationWorld))
            return;

        Location from = e.getArgs().from;

        if (entity instanceof Player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);
            if (handlePlayerMove(superiorPlayer,
                    from, e.getArgs().fromIslandResolver.get(),
                    to, e.getArgs().toIslandResolver.get(),
                    true, false))
                e.setCancelled();
        } else {
            Entity passenger = entity.getPassenger();
            SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;

            if (superiorPlayer == null)
                return;

            handlePlayerMove(superiorPlayer,
                    from, e.getArgs().fromIslandResolver.get(),
                    to, e.getArgs().toIslandResolver.get(),
                    false, true);
        }
    }

    private boolean handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, @Nullable Island fromIsland,
                                     Location to, @Nullable Island toIsland,
                                     boolean delayTeleport, boolean forceTeleport) {
        if (superiorPlayer.hasBypassModeEnabled())
            return false;

        if (toIsland != null) {
            try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                if (toIsland.isInsideRange(blockPosition.set(to), 1D))
                    return false;
            }
        }

        if (delayTeleport) {
            // If we don't delay the teleport, it will not occur due to the cancellation of PlayerMoveEvent
            BukkitExecutor.sync(() -> handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, fromIsland, forceTeleport), 1L);
        } else {
            handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, fromIsland, forceTeleport);
        }

        return true;
    }

    private void handlePlayerMoveOutsideIslandTeleport(SuperiorPlayer superiorPlayer, Location from, @Nullable Island fromIsland, boolean forceTeleport) {
        Player player = superiorPlayer.asPlayer();

        // We don't teleport in case we're inside the island, we just cancel the event.
        // However, if the player is in vehicle, we want to eject him and teleport.
        if (!forceTeleport && fromIsland != null && !player.isInsideVehicle()) {
            try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                if (fromIsland.isInsideRange(blockPosition.set(from)))
                    return;
            }
        }

        player.eject();

        if (fromIsland != null) {
            superiorPlayer.teleportWithResult(fromIsland, result -> {
                if (result != PlayerTeleportAlgorithm.TeleportResult.SUCCESS) {
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                }
            });
        } else {
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }
    }

}
