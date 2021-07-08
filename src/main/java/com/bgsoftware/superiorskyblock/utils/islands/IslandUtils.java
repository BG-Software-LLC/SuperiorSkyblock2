package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class IslandUtils {

    public static final String VISITORS_WARP_NAME = "visit";
    public static final UpgradeValue<Integer> NO_LIMIT = UpgradeValue.NEGATIVE;

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandUtils(){

    }

    public static List<ChunkPosition> getChunkCoords(Island island, World world, boolean onlyProtected, boolean noEmptyChunks){
        List<ChunkPosition> chunkCoords = new ArrayList<>();

        Location min = onlyProtected ? island.getMinimumProtected() : island.getMinimum();
        Location max = onlyProtected ? island.getMaximumProtected() : island.getMaximum();

        for(int x = min.getBlockX() >> 4; x <= max.getBlockX() >> 4; x++){
            for(int z = min.getBlockZ() >> 4; z <= max.getBlockZ() >> 4; z++){
                if(!noEmptyChunks || ChunksTracker.isMarkedDirty(island, world, x, z)) {
                    chunkCoords.add(ChunkPosition.of(world, x, z));
                }
            }
        }

        return chunkCoords;
    }

    public static List<ChunkPosition> getChunkCoords(Island island, boolean onlyProtected, boolean noEmptyChunks){
        List<ChunkPosition> chunkCoords = new ArrayList<>();

        {
            World normalWorld = island.getCenter(World.Environment.NORMAL).getWorld();
            chunkCoords.addAll(getChunkCoords(island, normalWorld, onlyProtected, noEmptyChunks));
        }

        if(plugin.getProviders().isNetherEnabled() && island.wasSchematicGenerated(World.Environment.NETHER)){
            World netherWorld = island.getCenter(World.Environment.NETHER).getWorld();
            chunkCoords.addAll(getChunkCoords(island, netherWorld, onlyProtected, noEmptyChunks));
        }

        if(plugin.getProviders().isEndEnabled() && island.wasSchematicGenerated(World.Environment.THE_END)){
            World endWorld = island.getCenter(World.Environment.THE_END).getWorld();
            chunkCoords.addAll(getChunkCoords(island, endWorld, onlyProtected, noEmptyChunks));
        }

        for(World registeredWorld : plugin.getGrid().getRegisteredWorlds()){
            chunkCoords.addAll(getChunkCoords(island, registeredWorld, onlyProtected, noEmptyChunks));
        }

        return chunkCoords;
    }

    public static List<CompletableFuture<Chunk>> getAllChunksAsync(Island island, World world, boolean onlyProtected, boolean noEmptyChunks, BiConsumer<Chunk, Throwable> whenComplete){
        return IslandUtils.getChunkCoords(island, world, onlyProtected, noEmptyChunks).stream().map(chunkPosition -> {
            CompletableFuture<Chunk> completableFuture = ChunksProvider.loadChunk(chunkPosition, null);
            return whenComplete == null ? completableFuture : completableFuture.whenComplete(whenComplete);
        }).collect(Collectors.toList());
    }

    public static List<CompletableFuture<Chunk>> getAllChunksAsync(Island island, World world, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad){
        return IslandUtils.getChunkCoords(island, world, onlyProtected, noEmptyChunks).stream()
                .map(chunkPosition -> ChunksProvider.loadChunk(chunkPosition, onChunkLoad))
                .collect(Collectors.toList());
    }

    public static List<CompletableFuture<Chunk>> getAllChunksAsync(Island island, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad){
        List<CompletableFuture<Chunk>> chunkCoords = new ArrayList<>();

        {
            World normalWorld = island.getCenter(World.Environment.NORMAL).getWorld();
            chunkCoords.addAll(getAllChunksAsync(island, normalWorld, onlyProtected, noEmptyChunks, onChunkLoad));
        }

        if(plugin.getProviders().isNetherEnabled() && island.wasSchematicGenerated(World.Environment.NETHER)){
            World netherWorld = island.getCenter(World.Environment.NETHER).getWorld();
            chunkCoords.addAll(getAllChunksAsync(island, netherWorld, onlyProtected, noEmptyChunks, onChunkLoad));
        }

        if(plugin.getProviders().isEndEnabled() && island.wasSchematicGenerated(World.Environment.THE_END)){
            World endWorld = island.getCenter(World.Environment.THE_END).getWorld();
            chunkCoords.addAll(getAllChunksAsync(island, endWorld, onlyProtected, noEmptyChunks, onChunkLoad));
        }

        for(World registeredWorld : plugin.getGrid().getRegisteredWorlds()){
            chunkCoords.addAll(getAllChunksAsync(island, registeredWorld, onlyProtected, noEmptyChunks, onChunkLoad));
        }

        return chunkCoords;
    }

    public static void updateIslandFly(Island island, SuperiorPlayer superiorPlayer){
        superiorPlayer.runIfOnline(player -> {
            if(!player.isFlying() && superiorPlayer.hasIslandFlyEnabled() && island.hasPermission(superiorPlayer, IslandPrivileges.FLY)){
                player.setAllowFlight(true);
                player.setFlying(true);
                Locale.ISLAND_FLY_ENABLED.send(player);
            }
            else if(player.isFlying() && !island.hasPermission(superiorPlayer, IslandPrivileges.FLY)){
                player.setAllowFlight(false);
                player.setFlying(false);
                Locale.ISLAND_FLY_DISABLED.send(player);
            }
        });
    }

    public static void updateTradingMenus(Island island, SuperiorPlayer superiorPlayer){
        superiorPlayer.runIfOnline(player -> {
            Inventory openInventory = player.getOpenInventory().getTopInventory();
            if(openInventory != null && openInventory.getType() == InventoryType.MERCHANT && !island.hasPermission(superiorPlayer, IslandPrivileges.VILLAGER_TRADING))
                player.closeInventory();
        });
    }

    public static void resetChunksExcludedFromList(Island island, Collection<ChunkPosition> excludedChunkPositions) {
        List<ChunkPosition> chunksToDelete = IslandUtils.getChunkCoords(island, false, false);
        chunksToDelete.removeAll(excludedChunkPositions);
        chunksToDelete.forEach(chunkPosition -> plugin.getNMSBlocks().deleteChunk(island, chunkPosition, null));
    }

    public static void sendMessage(Island island, Locale message, List<UUID> ignoredMembers, Object... args){
        island.getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredMembers.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> message.send(superiorPlayer, args));
    }

    public static double getGeneratorPercentageDecimal(Island island, com.bgsoftware.superiorskyblock.api.key.Key key, World.Environment environment){
        int totalAmount = island.getGeneratorTotalAmount(environment);
        return totalAmount == 0 ? 0 : (island.getGeneratorAmount(key, environment) * 100D) / totalAmount;
    }

    public static boolean checkKickRestrictions(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer){
        if(!island.isMember(targetPlayer)){
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        if(!targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())){
            Locale.KICK_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void handleKickPlayer(SuperiorPlayer caller, Island island, SuperiorPlayer target){
        handleKickPlayer(caller, caller.getName(), island, target);
    }

    public static void handleKickPlayer(SuperiorPlayer caller, String callerName, Island island, SuperiorPlayer target){
        EventsCaller.callIslandKickEvent(caller, target, island);

        island.kickMember(target);

        IslandUtils.sendMessage(island, Locale.KICK_ANNOUNCEMENT, new ArrayList<>(), target.getName(), callerName);

        Locale.GOT_KICKED.send(target, callerName);
    }

    public static boolean checkBanRestrictions(SuperiorPlayer superiorPlayer, Island island, SuperiorPlayer targetPlayer){
        Island playerIsland = superiorPlayer.getIsland();
        if(playerIsland != null && playerIsland.isMember(targetPlayer) &&
                !targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole())) {
            Locale.BAN_PLAYERS_WITH_LOWER_ROLE.send(superiorPlayer);
            return false;
        }

        if(island.isBanned(targetPlayer)){
            Locale.PLAYER_ALREADY_BANNED.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void handleBanPlayer(SuperiorPlayer caller, Island island, SuperiorPlayer target){
        island.banMember(target);

        IslandUtils.sendMessage(island, Locale.BAN_ANNOUNCEMENT, new ArrayList<>(), target.getName(), caller.getName());

        Locale.GOT_BANNED.send(target, island.getOwner().getName());
    }

    public static void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish){
        plugin.getNMSBlocks().deleteChunk(island, chunkPosition, onFinish);
        plugin.getGrid().removeStackedBlocks(island, chunkPosition);
        EventsCaller.callIslandChunkResetEvent(island, chunkPosition);
    }

    public static boolean isValidRoleForLimit(PlayerRole playerRole){
        return playerRole.isRoleLadder() && !playerRole.isFirstRole() && !playerRole.isLastRole();
    }

    public static String getWarpName(String rawName){
        return StringUtils.removeNonAlphabet(rawName, Collections.singletonList('_'));
    }

}
