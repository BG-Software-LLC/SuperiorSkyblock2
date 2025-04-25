package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
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

        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        if (superiorPlayer.hasBypassModeEnabled())
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = rightClicked.getLocation(wrapper.getHandle());
            Island entityIsland = plugin.getGrid().getIslandAt(entityLocation);

            if (entityIsland != null && entityIsland.isInsideRange(entityLocation, 1D))
                return;
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

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location vehicleLocation = vehicle.getLocation(wrapper.getHandle());
            Island entityIsland = plugin.getGrid().getIslandAt(vehicleLocation);

            if (entityIsland != null && entityIsland.isInsideRange(vehicleLocation, 1D))
                return;
        }

        e.setCancelled();
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
            if (handlePlayerMove(superiorPlayer, from, to, true, false))
                e.setCancelled();
        } else {
            Entity passenger = entity.getPassenger();
            SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;

            if (superiorPlayer == null)
                return;

            handlePlayerMove(superiorPlayer, from, to, false, true);
        }
    }

    private boolean handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to,
                                     boolean delayTeleport, boolean forceTeleport) {
        if (superiorPlayer.hasBypassModeEnabled())
            return false;

        Island toIsland = plugin.getGrid().getIslandAt(to);
        if (toIsland != null && toIsland.isInsideRange(to, 1D))
            return false;

        if (delayTeleport) {
            // If we don't delay the teleport, it will not occur due to the cancellation of PlayerMoveEvent
            BukkitExecutor.sync(() -> handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, forceTeleport), 1L);
        } else {
            handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, forceTeleport);
        }

        return true;
    }

    private void handlePlayerMoveOutsideIslandTeleport(SuperiorPlayer superiorPlayer, Location from, boolean forceTeleport) {
        Island fromIsland = plugin.getGrid().getIslandAt(from);

        // We don't teleport in case we're inside the island, we just cancel the event.
        if (!forceTeleport && fromIsland != null && fromIsland.isInsideRange(from))
            return;

        if (fromIsland != null) {
            superiorPlayer.teleport(fromIsland, result -> {
                if (!result) {
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                }
            });
        } else {
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }
    }

}
