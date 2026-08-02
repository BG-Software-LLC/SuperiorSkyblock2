package com.bgsoftware.superiorskyblock.module.warps.utils;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class WarpsUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private WarpsUtils() {

    }

    public static IslandSigns.Reason handleWarpSignPlace(@Nullable SuperiorPlayer superiorPlayer, Island island,
                                                          Location location, String[] lines) {
        int warpsLimit = island.getWarpsLimit();

        if (warpsLimit >= 0 && island.getIslandWarps().size() >= warpsLimit) {
            Message.NO_MORE_WARPS.send(superiorPlayer);
            return IslandSigns.Reason.LIMIT_REACHED;
        }

        String warpName = Formatters.STRIP_COLOR_FORMATTER.format(lines[1].trim());

        if (!IslandNames.isValidWarpName(superiorPlayer, island, warpName)) {
            return IslandSigns.Reason.INVALID_NAME;
        }

        String warpCategoryName = null;

        if (!Text.isBlank(lines[2].trim()) && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) {
            warpCategoryName = Formatters.STRIP_COLOR_FORMATTER.format(lines[2].trim());

            if (!IslandNames.isValidWarpCategoryName(superiorPlayer, warpCategoryName)) {
                return IslandSigns.Reason.INVALID_NAME;
            }

            if (island.getWarpCategory(warpCategoryName) == null &&
                    !PluginEventsFactory.callIslandCreateWarpCategoryEvent(island, superiorPlayer, warpCategoryName)) {
                return IslandSigns.Reason.EVENT_CANCELLED;
            }
        }

        WarpCategory warpCategory = warpCategoryName == null ? null : island.createWarpCategory(warpCategoryName);

        boolean privateFlag = BuiltinModules.WARPS.getConfiguration().isPrivateByDefault();

        if (!Text.isBlank(lines[3].trim()) && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) {
            privateFlag = Boolean.parseBoolean(lines[3].trim());
        } else if (!Text.isBlank(lines[2].trim())) {
            privateFlag = Boolean.parseBoolean(lines[2].trim());
        }

        if (!PluginEventsFactory.callIslandCreateWarpEvent(island, superiorPlayer, warpName, location, !privateFlag, warpCategory)) {
            return IslandSigns.Reason.EVENT_CANCELLED;
        }

        IslandWarp islandWarp = island.createWarp(warpName, location, warpCategory);
        islandWarp.setPrivateFlag(privateFlag);

        applyTemplate(islandWarp, lines, BuiltinModules.WARPS.getConfiguration().getSignsActiveLines());

        Message.WARP_SET.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(location), islandWarp.getName());

        return IslandSigns.Reason.SUCCESS;
    }

    public static void warpPlayerInternal(SuperiorPlayer superiorPlayer, Island island,
                                          @Nullable SuperiorPlayer targetPlayer, String warpName, boolean force) {
        IslandWarp islandWarp = island.getWarp(warpName);

        if (islandWarp == null) {
            Message.INVALID_WARP.send(superiorPlayer, warpName);
            return;
        }

        double charge = BuiltinModules.WARPS.getConfiguration().getChargeOnTeleport();

        if (!force && !superiorPlayer.hasBypassModeEnabled() && charge > 0) {
            if (plugin.getProviders().getEconomyProvider().getBalance(superiorPlayer)
                    .compareTo(BigDecimal.valueOf(charge)) < 0) {
                Message.NOT_ENOUGH_MONEY_TO_WARP.send(superiorPlayer);
                return;
            }

            plugin.getProviders().getEconomyProvider().withdrawMoney(superiorPlayer, charge);
        }

        EntityTeleports.warmupTeleport(superiorPlayer, BuiltinModules.WARPS.getConfiguration().getTeleportWarmup(),
                unused -> WarpsUtils.warpPlayerWithoutWarmup(superiorPlayer, island, targetPlayer, islandWarp));
    }

    private static void warpPlayerWithoutWarmup(SuperiorPlayer superiorPlayer, Island island,
                                                @Nullable SuperiorPlayer targetPlayer, IslandWarp islandWarp) {
        try (ObjectsPools.Wrapper<LazyWorldLocation> wrapper = ObjectsPools.LAZY_LOCATION.obtain()) {
            Location location = islandWarp.getLocation(wrapper.getHandle());

            if (location.getWorld() == null) {
                Location clonedLocation = location.clone();

                IslandWorlds.accessIslandWorldAsync(island, location, true, islandWorldResult ->
                    islandWorldResult.ifRight(Throwable::printStackTrace).ifLeft(world -> {
                        clonedLocation.setWorld(world);
                        warpPlayerWithoutWarmupWorldLoaded(superiorPlayer, island, targetPlayer, islandWarp, clonedLocation);
                    })
                );
            } else {
                warpPlayerWithoutWarmupWorldLoaded(superiorPlayer, island, targetPlayer, islandWarp, location);
            }
        }
    }

    private static void warpPlayerWithoutWarmupWorldLoaded(SuperiorPlayer superiorPlayer, Island island,
                                                           @Nullable SuperiorPlayer targetPlayer, IslandWarp islandWarp, Location location) {
        // Warp doesn't exist anymore.
        if (island.getWarp(islandWarp.getName()) == null) {
            Message.INVALID_WARP.send(superiorPlayer, islandWarp.getName());

            island.deleteWarp(islandWarp.getName());
            deactivateWarpSign(islandWarp, superiorPlayer.asPlayer());

            return;
        }

        superiorPlayer.setTeleportTask(null);

        if (!island.isInsideRange(location) || !WorldBlocks.isSafeBlock(location.getBlock())) {
            Message.WARP_TELEPORT_UNSAFE.send(superiorPlayer);

            if (BuiltinModules.WARPS.getConfiguration().isDeleteUnsafe()) {
                island.deleteWarp(islandWarp.getName());
                deactivateWarpSign(islandWarp, superiorPlayer.asPlayer());
            }

            return;
        }

        superiorPlayer.teleport(location, success -> {
            if (success) {
                if (targetPlayer != null) {
                    Message.WARP_TELEPORT_SUCCESS.send(superiorPlayer, targetPlayer.getName(), islandWarp.getName());
                } else {
                    Message.WARP_TELEPORT_SUCCESS_NAME.send(superiorPlayer, island.getName(), islandWarp.getName());
                }

                if (superiorPlayer.isShownAsOnline() && !island.isMember(superiorPlayer)) {
                    IslandUtils.sendMessage(island, Message.WARP_TELEPORT_ANNOUNCEMENT,
                            Collections.emptyList(), superiorPlayer.getName(), islandWarp.getName());
                }
            }
        });
    }

    public static boolean isWarpSignCreateLine(String line) {
        return BuiltinModules.WARPS.getConfiguration().isEnabled()
                && BuiltinModules.WARPS.getConfiguration().isSignsEnabled()
                && line.equalsIgnoreCase(BuiltinModules.WARPS.getConfiguration().getSignsCreateLine());
    }

    public static boolean isWarpSignLines(IslandWarp islandWarp, String[] lines) {
        return Arrays.equals(lines, buildLines(islandWarp, BuiltinModules.WARPS.getConfiguration().getSignsActiveLines()));
    }

    public static void updateWarpSign(IslandWarp islandWarp) {
        getWarpBlock(islandWarp, block ->
            BukkitExecutor.sync(() -> {
                BlockState blockState = block.getState();
                if (blockState instanceof Sign) {
                    Sign sign = (Sign) blockState;

                    String[] lines = buildLines(islandWarp, BuiltinModules.WARPS.getConfiguration().getSignsActiveLines());

                    applyLines(sign, lines);
                }
            }, 1L));
    }

    public static void deactivateWarpSign(IslandWarp islandWarp, CommandSender commandSender) {
        getWarpBlock(islandWarp, block ->
            BukkitExecutor.sync(() -> {
                BlockState blockState = block.getState();
                if (blockState instanceof Sign) {
                    Sign sign = (Sign) blockState;

                    String[] lines = buildLines(islandWarp, BuiltinModules.WARPS.getConfiguration().getSignsInactiveLines());

                    applyLines(sign, lines);

                    Message.WARP_SIGN_DEACTIVATED.send(commandSender);
                }
            }, 1L));
    }

    private static void getWarpBlock(IslandWarp islandWarp, Consumer<Block> consumer) {
        try (ObjectsPools.Wrapper<LazyWorldLocation> wrapper = ObjectsPools.LAZY_LOCATION.obtain()) {
            Location location = islandWarp.getLocation(wrapper.getHandle());

            if (location.getWorld() == null) {
                IslandWorlds.accessIslandWorldAsync(islandWarp.getIsland(), location, true, islandWorldResult ->
                    islandWorldResult.ifRight(Throwable::printStackTrace).ifLeft(unused ->
                        getWarpBlockWorldLoaded(islandWarp, consumer)));

                return;
            }
        }

        getWarpBlockWorldLoaded(islandWarp, consumer);
    }

    private static void getWarpBlockWorldLoaded(IslandWarp islandWarp, Consumer<Block> consumer) {
        ChunksProvider.loadChunk(ChunkPosition.of(islandWarp), ChunkLoadReason.EDIT_SIGN, unused ->
                getWarpBlockChunkLoaded(islandWarp, consumer));
    }

    private static void getWarpBlockChunkLoaded(IslandWarp islandWarp, Consumer<Block> consumer) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = islandWarp.getLocation(wrapper.getHandle());
            consumer.accept(location.getBlock());
        }
    }

    private static void applyTemplate(IslandWarp islandWarp, String[] lines, List<String> templateLines) {
        String[] generated = buildLines(islandWarp, templateLines);
        System.arraycopy(generated, 0, lines, 0, Math.min(lines.length, generated.length));
    }

    private static String[] buildLines(IslandWarp islandWarp, List<String> templateLines) {
        String[] lines = new String[templateLines.size()];

        for (int i = 0; i < templateLines.size(); i++) {
            lines[i] = templateLines.get(i).replace("{0}", islandWarp.getName())
                    .replace("{1}", islandWarp.getCategory().getName());
        }

        return lines;
    }

    private static void applyLines(Sign sign, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            sign.setLine(i, lines[i]);
        }

        sign.update();
    }

}
