package com.bgsoftware.superiorskyblock.module.visit.utils;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.SWorldPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.util.Collections;
import java.util.function.Consumer;

public class VisitUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private VisitUtils() {

    }

    public static IslandSigns.Reason handleSignPlace(@Nullable SuperiorPlayer superiorPlayer, Island island,
                                                     Dimension dimension, Location location, String[] lines) {
        if (BuiltinModules.VISIT.getConfiguration().isOnlyDefaultDimension()
                && dimension != plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            Message.VISITOR_HOME_SET_OUTSIDE_DEFAULT_DIMENSION.send(superiorPlayer);

            return IslandSigns.Reason.NOT_DEFAULT_DIMENSION;
        }

        PluginEvent<PluginEventArgs.IslandSetVisitorHome> setVisitorHomeEvent =
                PluginEventsFactory.callIslandSetVisitorHomeEvent(island, superiorPlayer, location);

        if (setVisitorHomeEvent.isCancelled()) {
            return IslandSigns.Reason.EVENT_CANCELLED;
        }

        lines[0] = BuiltinModules.VISIT.getConfiguration().getSignsActiveLine();

        if (BuiltinModules.VISIT.getConfiguration().isDescriptionsEnabled()) {
            StringBuilder description = new StringBuilder();

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                if (!Text.isBlank(line)) {
                    String formattedLine = BuiltinModules.VISIT.getConfiguration().getDescriptionsLineFormat().replace("{0}", line);
                    Text.appendWithLine(description, ChatColor.RESET).append(formattedLine);
                }
            }

            if (description.length() > 0) {
                PluginEvent<PluginEventArgs.IslandChangeDescription> changeDescriptionEvent =
                        PluginEventsFactory.callIslandChangeDescriptionEvent(island, superiorPlayer, description.toString());

                if (!changeDescriptionEvent.isCancelled()) {
                    island.setDescription(changeDescriptionEvent.getArgs().description);
                }
            }

            if (BuiltinModules.VISIT.getConfiguration().isDescriptionsEnabled()) {
                for (int i = 1; i < lines.length; i++) {
                    lines[i] = Formatters.COLOR_FORMATTER.format(lines[i]);
                }
            }
        } else {
            for (int i = 1; i < lines.length; i++) {
                lines[i] = "";
            }
        }

        Location oldLocation = island.getVisitorsLocation(dimension);

        boolean deactivated = false;
        if (oldLocation != null) {
            deactivateVisitorSign(island, oldLocation);
            deactivated = true;
        }

        island.setVisitorsLocation(dimension, SWorldPosition.of(setVisitorHomeEvent.getArgs().islandVisitorHome));
        Message.VISITOR_HOME_SET.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));

        if (deactivated) {
            Message.VISITOR_HOME_SET_PREVIOUS_REMOVED.send(superiorPlayer);
        }

        return IslandSigns.Reason.SUCCESS;
    }

    public static void teleportPlayerInternal(SuperiorPlayer superiorPlayer, Island island,
                                              @Nullable SuperiorPlayer targetPlayer, Dimension dimension) {
        Location location = island.getVisitorsLocation(dimension);
        boolean isSign = true;

        if (location == null && !BuiltinModules.VISIT.getConfiguration().isSignsRequiredForVisit()) {
            location = island.getIslandHome(dimension);
            isSign = false;
        }

        if (location == null) {
            Message.VISITOR_HOME_TELEPORT_NOT_SET.send(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled()) {
                return;
            }

            location = island.getIslandHome(dimension);
            isSign = false;

            Message.VISITOR_HOME_TELEPORT_NOT_SET_BYPASS.send(superiorPlayer);
        }

        if (island.isLocked() && !island.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        Location finalLocation = location;
        boolean finalIsSign = isSign;

        EntityTeleports.warmupTeleport(superiorPlayer, BuiltinModules.VISIT.getConfiguration().getTeleportWarmup(), afterWarmup ->
                teleportPlayerNoWarmup(superiorPlayer, island, targetPlayer, dimension, finalLocation, finalIsSign, afterWarmup /*checkIslandLock*/));
    }

    private static void teleportPlayerNoWarmup(SuperiorPlayer superiorPlayer, Island island,
                                               @Nullable SuperiorPlayer targetPlayer, Dimension dimension,
                                               Location location, boolean isVisitorSign, boolean checkIslandLock) {
        if (location.getWorld() == null) {
            IslandWorlds.accessIslandWorldAsync(island, location, true, islandWorldResult ->
                    islandWorldResult.ifRight(Throwable::printStackTrace).ifLeft(world -> {
                location.setWorld(world);
                teleportPlayerNoWarmupWorldLoaded(superiorPlayer, island, targetPlayer, dimension, location, isVisitorSign, checkIslandLock);
            }));
        } else {
            teleportPlayerNoWarmupWorldLoaded(superiorPlayer, island, targetPlayer, dimension, location, isVisitorSign, checkIslandLock);
        }
    }

    private static void teleportPlayerNoWarmupWorldLoaded(SuperiorPlayer superiorPlayer, Island island,
                                                          @Nullable SuperiorPlayer targetPlayer, Dimension dimension,
                                                          Location location, boolean isVisitorSign, boolean checkIslandLock) {
        superiorPlayer.setTeleportTask(null);

        if (checkIslandLock && island.isLocked() && !island.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return;
        }

        if (isVisitorSign && !WorldBlocks.isSafeBlock(location.getBlock())) {
            Message.VISITOR_HOME_TELEPORT_UNSAFE.send(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled()) {
                if (PluginEventsFactory.callIslandRemoveVisitorHomeEvent(island, superiorPlayer)) {
                    island.setVisitorsLocation(null);
                    deactivateVisitorSign(island, location);
                }

                return;
            }

            Message.VISITOR_HOME_TELEPORT_UNSAFE_BYPASS.send(superiorPlayer);
        }

        superiorPlayer.teleport(location, success -> {
            if (success) {
                if (targetPlayer != null) {
                    Message.VISITOR_HOME_TELEPORT_SUCCESS.send(superiorPlayer, targetPlayer.getName(),
                            Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
                } else {
                    Message.VISITOR_HOME_TELEPORT_SUCCESS_NAME.send(superiorPlayer, island.getName(),
                            Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
                }

                if (superiorPlayer.isShownAsOnline() && !island.isMember(superiorPlayer)) {
                    IslandUtils.sendMessage(island, Message.VISITOR_HOME_TELEPORT_ANNOUNCEMENT, Collections.emptyList(),
                            superiorPlayer.getName(), Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
                }
            }
        });
    }

    public static boolean isVisitorSignCreateLine(String line) {
        return BuiltinModules.VISIT.getConfiguration().isEnabled()
                && line.equalsIgnoreCase(BuiltinModules.VISIT.getConfiguration().getSignsCreateLine());
    }

    public static boolean isVisitorSignLines(String[] lines) {
        return lines[0].equals(BuiltinModules.VISIT.getConfiguration().getSignsActiveLine());
    }

    public static void deactivateVisitorSign(Island island, Location location) {
        getVisitorBlock(island, location, block ->
                BukkitExecutor.sync(() -> {
                    BlockState blockState = block.getState();
                    if (blockState instanceof Sign) {
                        Sign sign = (Sign) blockState;

                        sign.setLine(0, BuiltinModules.VISIT.getConfiguration().getSignsInactiveLine());

                        sign.update();
                    }
                }, 1L));
    }

    private static void getVisitorBlock(Island island, Location location, Consumer<Block> consumer) {
        if (location.getWorld() == null) {
            IslandWorlds.accessIslandWorldAsync(island, location, true, islandWorldResult ->
                    islandWorldResult.ifRight(Throwable::printStackTrace).ifLeft(unused ->
                            getVisitorBlockWorldLoaded(location, consumer)));
        } else {
            getVisitorBlockWorldLoaded(location, consumer);
        }
    }

    private static void getVisitorBlockWorldLoaded(Location location, Consumer<Block> consumer) {
        ChunksProvider.loadChunk(ChunkPosition.of(location), ChunkLoadReason.EDIT_SIGN, unused ->
            consumer.accept(location.getBlock()));
    }

}
