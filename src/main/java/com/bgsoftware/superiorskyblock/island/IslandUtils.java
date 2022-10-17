package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class IslandUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final EnumMap<World.Environment, Biome> DEFAULT_WORLD_BIOMES = new EnumMap<>(World.Environment.class);

    static {
        try {
            DEFAULT_WORLD_BIOMES.put(World.Environment.NORMAL, Biome.valueOf(plugin.getSettings().getWorlds().getNormal().getBiome()));
        } catch (IllegalArgumentException error) {
            DEFAULT_WORLD_BIOMES.put(World.Environment.NORMAL, Biome.PLAINS);
        }
        try {
            DEFAULT_WORLD_BIOMES.put(World.Environment.NETHER, Biome.valueOf(plugin.getSettings().getWorlds().getNether().getBiome()));
        } catch (IllegalArgumentException error) {
            DEFAULT_WORLD_BIOMES.put(World.Environment.NETHER, getBiome("NETHER_WASTES", "NETHER", "HELL"));
        }
        try {
            DEFAULT_WORLD_BIOMES.put(World.Environment.THE_END, Biome.valueOf(plugin.getSettings().getWorlds().getEnd().getBiome()));
        } catch (IllegalArgumentException error) {
            DEFAULT_WORLD_BIOMES.put(World.Environment.THE_END, getBiome("THE_END", "SKY"));
        }
    }

    private IslandUtils() {

    }

    public static List<ChunkPosition> getChunkCoords(Island island, WorldInfo worldInfo, boolean onlyProtected, boolean noEmptyChunks) {
        List<ChunkPosition> chunkCoords = new LinkedList<>();

        Location min = onlyProtected ? island.getMinimumProtected() : island.getMinimum();
        Location max = onlyProtected ? island.getMaximumProtected() : island.getMaximum();

        for (int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++) {
            for (int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++) {
                if (!noEmptyChunks || island.isChunkDirty(worldInfo.getName(), x, z)) {
                    chunkCoords.add(ChunkPosition.of(worldInfo, x, z));
                }
            }
        }

        return chunkCoords;
    }

    public static Map<WorldInfo, List<ChunkPosition>> getChunkCoords(Island island, boolean onlyProtected, boolean noEmptyChunks) {
        Map<WorldInfo, List<ChunkPosition>> chunkCoords = new HashMap<>();

        {
            if (plugin.getProviders().getWorldsProvider().isNormalEnabled() && island.wasSchematicGenerated(World.Environment.NORMAL)) {
                WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, World.Environment.NORMAL);
                List<ChunkPosition> chunkPositions = getChunkCoords(island, worldInfo, onlyProtected, noEmptyChunks);
                if (!chunkPositions.isEmpty())
                    chunkCoords.put(worldInfo, chunkPositions);
            }
        }

        if (plugin.getProviders().getWorldsProvider().isNetherEnabled() && island.wasSchematicGenerated(World.Environment.NETHER)) {
            WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, World.Environment.NETHER);
            List<ChunkPosition> chunkPositions = getChunkCoords(island, worldInfo, onlyProtected, noEmptyChunks);
            if (!chunkPositions.isEmpty())
                chunkCoords.put(worldInfo, chunkPositions);
        }

        if (plugin.getProviders().getWorldsProvider().isEndEnabled() && island.wasSchematicGenerated(World.Environment.THE_END)) {
            WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, World.Environment.THE_END);
            List<ChunkPosition> chunkPositions = getChunkCoords(island, worldInfo, onlyProtected, noEmptyChunks);
            if (!chunkPositions.isEmpty())
                chunkCoords.put(worldInfo, chunkPositions);
        }

        for (World registeredWorld : plugin.getGrid().getRegisteredWorlds()) {
            WorldInfo worldInfo = WorldInfo.of(registeredWorld);
            List<ChunkPosition> chunkPositions = getChunkCoords(island, worldInfo, onlyProtected, noEmptyChunks);
            if (!chunkPositions.isEmpty())
                chunkCoords.put(worldInfo, chunkPositions);
        }

        return chunkCoords;
    }

    public static List<CompletableFuture<Chunk>> getAllChunksAsync(Island island,
                                                                   World world,
                                                                   boolean onlyProtected,
                                                                   boolean noEmptyChunks,
                                                                   ChunkLoadReason chunkLoadReason,
                                                                   Consumer<Chunk> onChunkLoad) {
        return new SequentialListBuilder<CompletableFuture<Chunk>>()
                .mutable()
                .build(IslandUtils.getChunkCoords(island, WorldInfo.of(world), onlyProtected, noEmptyChunks), chunkPosition ->
                        ChunksProvider.loadChunk(chunkPosition, chunkLoadReason, onChunkLoad));
    }

    public static List<CompletableFuture<Chunk>> getAllChunksAsync(Island island,
                                                                   boolean onlyProtected,
                                                                   boolean noEmptyChunks,
                                                                   ChunkLoadReason chunkLoadReason,
                                                                   Consumer<Chunk> onChunkLoad) {
        List<CompletableFuture<Chunk>> chunkCoords = new LinkedList<>();

        {
            if (plugin.getProviders().getWorldsProvider().isNormalEnabled() && island.wasSchematicGenerated(World.Environment.NORMAL)) {
                World normalWorld = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getWorld();
                chunkCoords.addAll(getAllChunksAsync(island, normalWorld, onlyProtected, noEmptyChunks, chunkLoadReason, onChunkLoad));
            }
        }

        if (plugin.getProviders().getWorldsProvider().isNetherEnabled() && island.wasSchematicGenerated(World.Environment.NETHER)) {
            World netherWorld = island.getCenter(World.Environment.NETHER).getWorld();
            chunkCoords.addAll(getAllChunksAsync(island, netherWorld, onlyProtected, noEmptyChunks, chunkLoadReason, onChunkLoad));
        }

        if (plugin.getProviders().getWorldsProvider().isEndEnabled() && island.wasSchematicGenerated(World.Environment.THE_END)) {
            World endWorld = island.getCenter(World.Environment.THE_END).getWorld();
            chunkCoords.addAll(getAllChunksAsync(island, endWorld, onlyProtected, noEmptyChunks, chunkLoadReason, onChunkLoad));
        }

        for (World registeredWorld : plugin.getGrid().getRegisteredWorlds()) {
            chunkCoords.addAll(getAllChunksAsync(island, registeredWorld, onlyProtected, noEmptyChunks, chunkLoadReason, onChunkLoad));
        }

        return chunkCoords;
    }

    public static void updateIslandFly(Island island, SuperiorPlayer superiorPlayer) {
        superiorPlayer.runIfOnline(player -> {
            if (!player.getAllowFlight() && superiorPlayer.hasIslandFlyEnabled() && island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
                player.setAllowFlight(true);
                player.setFlying(true);
                Message.ISLAND_FLY_ENABLED.send(player);
            } else if (player.getAllowFlight() && !island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
                player.setAllowFlight(false);
                player.setFlying(false);
                Message.ISLAND_FLY_DISABLED.send(player);
            }
        });
    }

    public static void updateTradingMenus(Island island, SuperiorPlayer superiorPlayer) {
        superiorPlayer.runIfOnline(player -> {
            Inventory openInventory = player.getOpenInventory().getTopInventory();
            if (openInventory != null && openInventory.getType() == InventoryType.MERCHANT && !island.hasPermission(superiorPlayer, IslandPrivileges.VILLAGER_TRADING))
                player.closeInventory();
        });
    }

    public static void resetChunksExcludedFromList(Island island, Collection<ChunkPosition> excludedChunkPositions) {
        Map<WorldInfo, List<ChunkPosition>> chunksToDelete = IslandUtils.getChunkCoords(island, false, false);
        chunksToDelete.values().forEach(chunkPositions -> {
            List<ChunkPosition> clonedChunkPositions = new LinkedList<>(chunkPositions);
            clonedChunkPositions.removeAll(excludedChunkPositions);
            deleteChunks(island, clonedChunkPositions, null);
        });
    }

    public static void sendMessage(Island island, Message message, List<UUID> ignoredMembers, Object... args) {
        for (SuperiorPlayer islandMember : island.getIslandMembers(true)) {
            if (!ignoredMembers.contains(islandMember.getUniqueId()))
                message.send(islandMember, args);
        }
    }

    public static double getGeneratorPercentageDecimal(Island island, Key key, World.Environment environment) {
        int totalAmount = island.getGeneratorTotalAmount(environment);
        return totalAmount == 0 ? 0 : (island.getGeneratorAmount(key, environment) * 100D) / totalAmount;
    }

    public static boolean checkKickRestrictions(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        if (!island.isMember(targetPlayer)) {
            Message.PLAYER_NOT_INSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        if (!targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())) {
            Message.KICK_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void handleKickPlayer(SuperiorPlayer caller, Island island, SuperiorPlayer target) {
        handleKickPlayer(caller, caller.getName(), island, target);
    }

    public static void handleKickPlayer(SuperiorPlayer caller, String callerName, Island island, SuperiorPlayer target) {
        plugin.getEventsBus().callIslandKickEvent(caller, target, island);

        island.kickMember(target);

        IslandUtils.sendMessage(island, Message.KICK_ANNOUNCEMENT, Collections.emptyList(), target.getName(), callerName);

        Message.GOT_KICKED.send(target, callerName);
    }

    public static boolean checkBanRestrictions(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer) {
        Island playerIsland = superiorPlayer.getIsland();
        if (playerIsland != null && playerIsland.isMember(targetPlayer) &&
                !targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())) {
            Message.BAN_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return false;
        }

        if (island.isBanned(targetPlayer)) {
            Message.PLAYER_ALREADY_BANNED.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void handleBanPlayer(SuperiorPlayer caller, Island island, SuperiorPlayer target) {
        if (!plugin.getEventsBus().callIslandBanEvent(caller, target, island))
            return;

        island.banMember(target, caller);

        IslandUtils.sendMessage(island, Message.BAN_ANNOUNCEMENT, Collections.emptyList(), target.getName(), caller.getName());

        Message.GOT_BANNED.send(target, island.getOwner().getName());
    }

    public static void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish) {
        plugin.getNMSChunks().deleteChunks(island, chunkPositions, onFinish);
        chunkPositions.forEach(chunkPosition -> {
            plugin.getStackedBlocks().removeStackedBlocks(chunkPosition);
            plugin.getEventsBus().callIslandChunkResetEvent(island, chunkPosition);
        });
    }

    public static boolean isValidRoleForLimit(PlayerRole playerRole) {
        return playerRole.isRoleLadder() && !playerRole.isFirstRole() && !playerRole.isLastRole();
    }

    public static boolean isWarpNameLengthValid(String warpName) {
        return warpName.length() <= getMaxWarpNameLength();
    }

    public static int getMaxWarpNameLength() {
        return 255;
    }

    public static boolean handleBorderColorUpdate(SuperiorPlayer superiorPlayer, BorderColor borderColor) {
        if (!superiorPlayer.hasWorldBorderEnabled()) {
            if (!plugin.getEventsBus().callPlayerToggleBorderEvent(superiorPlayer))
                return false;

            superiorPlayer.toggleWorldBorder();
        }

        if (!plugin.getEventsBus().callPlayerChangeBorderColorEvent(superiorPlayer, borderColor))
            return false;

        superiorPlayer.setBorderColor(borderColor);
        plugin.getNMSWorld().setWorldBorder(superiorPlayer,
                plugin.getGrid().getIslandAt(superiorPlayer.getLocation()));

        Message.BORDER_PLAYER_COLOR_UPDATED.send(superiorPlayer,
                Formatters.BORDER_COLOR_FORMATTER.format(borderColor, superiorPlayer.getUserLocale()));

        return true;
    }

    public static Biome getDefaultWorldBiome(World.Environment environment) {
        return Objects.requireNonNull(DEFAULT_WORLD_BIOMES.get(environment));
    }

    public static List<Biome> getDefaultWorldBiomes() {
        return new SequentialListBuilder<Biome>().build(DEFAULT_WORLD_BIOMES.values());
    }

    private static Biome getBiome(String... biomeNames) {
        for (String biomeName : biomeNames) {
            try {
                return Biome.valueOf(biomeName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        throw new IllegalArgumentException();
    }

}
