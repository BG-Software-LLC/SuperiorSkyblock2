package com.bgsoftware.superiorskyblock.island.signs;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.module.visit.utils.VisitUtils;
import com.bgsoftware.superiorskyblock.module.warps.utils.WarpsUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class IslandSigns {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandSigns() {

    }

    public static Result handleSignPlace(@Nullable SuperiorPlayer superiorPlayer, Location location, String[] lines) {
        // Adjust to the middle of the block
        location.add(0.5, 0, 0.5);

        Island island = plugin.getGrid().getIslandAt(location);

        if (island == null) {
            return new Result(Reason.NOT_IN_ISLAND, false);
        }

        IslandWarp islandWarp = island.getWarp(location);

        if (islandWarp != null) {
            Message.WARP_SIGN_EDIT.send(superiorPlayer);

            return new Result(Reason.SIGN_EDIT, true);
        } else {
            Dimension dimension = plugin.getGrid().getIslandsWorldDimension(location.getWorld());

            if (isSamePosition(island.getVisitorsLocation(dimension), location)) {
                Message.VISITOR_HOME_SIGN_EDIT.send(superiorPlayer);

                return new Result(Reason.SIGN_EDIT, true);
            }
        }

        // If the sign was placed by a player, we set the location's yaw to the player's yaw.
        // We do not do this when the sign is placed during island creation, it doesn't make sense.
        if (superiorPlayer != null) {
            superiorPlayer.runIfOnline(player -> {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    location.setYaw(player.getLocation(wrapper.getHandle()).getYaw());
                }
            });
        }

        if (WarpsUtils.isWarpSignCreateLine(lines[0])) {
            Reason reason = WarpsUtils.handleWarpSignPlace(superiorPlayer, island, location, lines);

            return new Result(reason, true);
        } else if (VisitUtils.isVisitorSignCreateLine(lines[0])) {
            Dimension dimension = plugin.getGrid().getIslandsWorldDimension(location.getWorld());
            Reason reason = VisitUtils.handleSignPlace(superiorPlayer, island, dimension, location, lines);

            return new Result(reason, true);
        }

        return new Result(Reason.SUCCESS, false);
    }

    public static Result handleSignBreak(@Nullable Island island, @Nullable SuperiorPlayer superiorPlayer, Sign sign) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = sign.getLocation(wrapper.getHandle());

            if (island == null) {
                island = plugin.getGrid().getIslandAt(location);

                if (island == null) {
                    return new Result(Reason.NOT_IN_ISLAND, false);
                }
            }

            String[] lines = sign.getLines();
            IslandWarp islandWarp = island.getWarp(location);

            if (islandWarp != null && WarpsUtils.isWarpSignLines(islandWarp, lines)) {
                if (!PluginEventsFactory.callIslandDeleteWarpEvent(island, superiorPlayer, islandWarp)) {
                    return new Result(Reason.EVENT_CANCELLED, true);
                }

                island.deleteWarp(superiorPlayer, location);
            } else {
                Dimension dimension = plugin.getGrid().getIslandsWorldDimension(location.getWorld());

                if (isSamePosition(island.getVisitorsLocation(dimension), location) && VisitUtils.isVisitorSignLines(lines)) {
                    if (!PluginEventsFactory.callIslandRemoveVisitorHomeEvent(island, superiorPlayer)) {
                        return new Result(Reason.EVENT_CANCELLED, true);
                    }

                    island.setVisitorsLocation(dimension, null);

                    Message.VISITOR_HOME_REMOVE.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
                }
            }
        }

        return new Result(Reason.SUCCESS, false);
    }

    private static boolean isSamePosition(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return false;
        }

        return location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY()
                && location1.getBlockZ() == location2.getBlockZ();
    }

    public enum Reason {

        NOT_IN_ISLAND,
        SIGN_EDIT,
        NOT_DEFAULT_DIMENSION,
        LIMIT_REACHED,
        INVALID_NAME,
        EVENT_CANCELLED,
        SUCCESS

    }

    public static class Result {

        private final Reason reason;
        private final boolean cancelEvent;

        public Result(Reason reason, boolean cancelEvent) {
            this.reason = reason;
            this.cancelEvent = cancelEvent;
        }

        public Reason getReason() {
            return reason;
        }

        public boolean isCancelEvent() {
            return cancelEvent;
        }

    }

}
