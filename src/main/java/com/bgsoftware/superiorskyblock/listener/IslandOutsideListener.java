package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Computed;
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

import java.util.function.Function;

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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(rightClicked.getWorld()))
            return;

        SuperiorPlayer superiorPlayer = e.getArgs().superiorPlayerResolver.get();
        if (superiorPlayer.hasBypassModeEnabled())
            return;

        Island entityIsland = e.getArgs().entityIslandResolver.get(true);

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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(vehicle.getWorld()))
            return;

        Entity entity = e.getArgs().entity;

        if (entity instanceof Player && plugin.getPlayers().getSuperiorPlayer(entity).hasBypassModeEnabled())
            return;

        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain();
             ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location vehicleLocation = vehicle.getLocation(wrapper.getHandle());
            Island island = WorldBlocks.findIslandWithHint(
                    vehicleLocation,
                    blockPosition.set(vehicleLocation),
                    e.getArgs().islandResolver.get(true));
            if (island == null || !island.isInsideRange(blockPosition, 1D))
                e.setCancelled();
        }
    }

    private void onEntityMove(GameEvent<GameEventArgs.EntityMoveEvent> e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Location to = e.getArgs().to;
        World destinationWorld = to.getWorld();

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(destinationWorld))
            return;

        Entity entity = e.getArgs().entity;

        if (!entity.getWorld().equals(destinationWorld))
            return;

        Location from = e.getArgs().from;

        if (entity instanceof Player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);
            if (handlePlayerMove(superiorPlayer, from, l -> e.getArgs().fromIslandResolver.get(true),
                    to, l -> e.getArgs().toIslandResolver.get(true), true, false))
                e.setCancelled();
        } else {
            Entity passenger = entity.getPassenger();
            SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;

            if (superiorPlayer == null)
                return;

            handlePlayerMove(superiorPlayer, from, l -> e.getArgs().fromIslandResolver.get(true),
                    to, l -> e.getArgs().toIslandResolver.get(true), false, true);
        }
    }

    private boolean handlePlayerMove(SuperiorPlayer superiorPlayer,
                                     Location from, Function<Location, Island> fromIslandSupplier,
                                     Location to, Function<Location, Island> toIslandSupplier,
                                     boolean delayTeleport, boolean forceTeleport) {
        if (superiorPlayer.hasBypassModeEnabled())
            return false;

        Island toIsland = toIslandSupplier.apply(to);
        if (toIsland != null) {
            try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                if (toIsland.isInsideRange(blockPosition.set(to), 1D))
                    return false;
            }
        }

        if (delayTeleport) {
            // If we don't delay the teleport, it will not occur due to the cancellation of PlayerMoveEvent
            BukkitExecutor.sync(() -> handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, fromIslandSupplier, forceTeleport), 1L);
        } else {
            handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, fromIslandSupplier, forceTeleport);
        }

        return true;
    }

    private void handlePlayerMoveOutsideIslandTeleport(SuperiorPlayer superiorPlayer, Location from,
                                                       Function<Location, Island> fromIslandSupplier,
                                                       boolean forceTeleport) {
        Computed<Island> computedFromIsland = new Computed<>();

        Player player = superiorPlayer.asPlayer();

        // We don't teleport in case we're inside the island, we just cancel the event.
        // However, if the player is in vehicle, we want to eject him and teleport.
        if (!forceTeleport) {
            Island fromIsland = computedFromIsland.tryCompute(from, fromIslandSupplier);
            if (fromIsland != null && !player.isInsideVehicle()) {
                try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
                    if (fromIsland.isInsideRange(blockPosition.set(from)))
                        return;
                }
            }
        }

        player.eject();

        Island fromIsland = computedFromIsland.tryCompute(from, fromIslandSupplier);

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
