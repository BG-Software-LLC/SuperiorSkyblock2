package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.List;

public final class BlocksLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[] {
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    private BlocksLogic(){
    }

    public static void handleBreak(Block block){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        Key blockKey = Key.of(block);

        if(!blockKey.getGlobalKey().contains("SPAWNER") || plugin.getProviders().shouldListenToSpawnerChanges())
            island.handleBlockBreak(blockKey, plugin.getNMSWorld().getDefaultAmount(block));

        EnumMap<BlockFace, Key> nearbyBlocks = new EnumMap<>(BlockFace.class);

        for(BlockFace nearbyFace : NEARBY_BLOCKS){
            Block nearbyBlock = block.getRelative(nearbyFace);
            if(!nearbyBlock.getType().isSolid()) {
                Key nearbyBlockKey = Key.of(nearbyBlock);
                if (!nearbyBlockKey.getGlobalKey().equals("AIR"))
                    nearbyBlocks.put(nearbyFace, nearbyBlockKey);
            }
        }

        Executor.sync(() -> {
            if(plugin.getNMSChunks().isChunkEmpty(block.getChunk()))
                ChunksTracker.markEmpty(island, block, true);

            for(BlockFace nearbyFace : NEARBY_BLOCKS){
                Key nearbyBlock = Key.of(block.getRelative(nearbyFace));
                Key oldNearbyBlock = nearbyBlocks.getOrDefault(nearbyFace, ConstantKeys.AIR);
                if(oldNearbyBlock != ConstantKeys.AIR && !nearbyBlock.equals(oldNearbyBlock)) {
                    island.handleBlockBreak(oldNearbyBlock, 1);
                }
            }
        }, 2L);
    }

    public static void handlePlace(Block block, BlockState oldBlockState){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island != null) {
            if(oldBlockState != null) {
                if (oldBlockState.getType().name().contains("LAVA"))
                    island.handleBlockBreak(ConstantKeys.LAVA, 1);
                else if (oldBlockState.getType().name().contains("WATER"))
                    island.handleBlockBreak(ConstantKeys.WATER, 1);
            }

            Key blockKey = Key.of(block);

            if(blockKey.equals(ConstantKeys.END_PORTAL_FRAME_WITH_EYE))
                island.handleBlockBreak(ConstantKeys.END_PORTAL_FRAME, 1);

            if(!blockKey.getGlobalKey().contains("SPAWNER") || plugin.getProviders().shouldListenToSpawnerChanges())
                island.handleBlockPlace(blockKey, 1);

            ChunksTracker.markDirty(island, block, true);
        }
    }

    public static boolean isWarpSign(String firstSignLine){
        return firstSignLine.equalsIgnoreCase(plugin.getSettings().getSignWarpLine());
    }

    public static boolean isVisitorsSign(String firstSignLine){
        return firstSignLine.equalsIgnoreCase(plugin.getSettings().getVisitorsSign().getLine());
    }

    public static boolean handleSignPlace(SuperiorPlayer superiorPlayer, Island island, Location warpLocation,
                                          String[] warpLines, boolean sendMessage){
        Location playerLocation = superiorPlayer.getLocation();
        if(playerLocation != null)
            warpLocation.setYaw(playerLocation.getYaw());

        if(isWarpSign(warpLines[0])){
            return handleWarpSignPlace(superiorPlayer, island, warpLocation, warpLines, sendMessage);
        }

        else if(isVisitorsSign(warpLines[0])){
            return handleVisitorsSignPlace(superiorPlayer, island, warpLocation, warpLines, sendMessage);
        }

        return false;
    }

    public static boolean handleWarpSignPlace(SuperiorPlayer superiorPlayer, Island island, Location warpLocation,
                                              String[] signLines, boolean sendMessage){
        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            if(sendMessage)
                Locale.NO_MORE_WARPS.send(superiorPlayer);
            for (int i = 0; i < 4; i++)
                signLines[i] = "";
            return true;
        }

        String warpName = IslandUtils.getWarpName(StringUtils.stripColors(signLines[1].trim()));
        boolean privateFlag = signLines[2].equalsIgnoreCase("private");

        boolean creationFailed = false;

        if(warpName.isEmpty()) {
            if(sendMessage)
                Locale.WARP_ILLEGAL_NAME.send(superiorPlayer);
            creationFailed = true;
        }

        else if(island.getWarp(warpName) != null) {
            if(sendMessage)
                Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
            creationFailed = true;
        }

        else if(!IslandUtils.isWarpNameLengthValid(warpName)) {
            if(sendMessage)
                Locale.WARP_NAME_TOO_LONG.send(superiorPlayer);
            creationFailed = true;
        }

        if(creationFailed){
            for (int i = 0; i < 4; i++) {
                signLines[i] = "";
            }
        }
        else {
            List<String> signWarp = plugin.getSettings().getSignWarp();

            for (int i = 0; i < signWarp.size(); i++)
                signLines[i] = signWarp.get(i).replace("{0}", warpName);

            IslandWarp islandWarp = island.createWarp(warpName, warpLocation, null);
            islandWarp.setPrivateFlag(privateFlag);
            if(sendMessage)
                Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));
        }

        return true;
    }

    public static boolean handleVisitorsSignPlace(SuperiorPlayer superiorPlayer, Island island, Location visitorsLocation,
                                                  String[] warpLines, boolean sendMessage){
        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            if(sendMessage)
                Locale.NO_MORE_WARPS.send(superiorPlayer);
            for (int i = 0; i < 4; i++)
                warpLines[i] = "";
            return true;
        }

        StringBuilder descriptionBuilder = new StringBuilder();

        for(int i = 1; i < 4; i++){
            String line = warpLines[i];
            if(!line.isEmpty())
                descriptionBuilder.append("\n").append(ChatColor.RESET).append(line);
        }

        String description = descriptionBuilder.length() < 1 ? "" : descriptionBuilder.substring(1);

        warpLines[0] = plugin.getSettings().getVisitorsSign().getActive();

        for (int i = 1; i <= 3; i++)
            warpLines[i] = StringUtils.translateColors(warpLines[i]);

        Block oldWelcomeSignBlock = island.getVisitorsLocation() == null ? null : island.getVisitorsLocation().getBlock();
        if(oldWelcomeSignBlock != null && oldWelcomeSignBlock.getType().name().contains("SIGN")) {
            Sign oldWelcomeSign = (Sign) oldWelcomeSignBlock.getState();
            oldWelcomeSign.setLine(0, plugin.getSettings().getVisitorsSign().getInactive());
            oldWelcomeSign.update();
        }

        island.setVisitorsLocation(visitorsLocation);
        island.setDescription(description);

        if(sendMessage)
            Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(visitorsLocation));

        return true;
    }

    public static void handleSignBreak(Player player, Sign sign){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(sign.getLocation());

        if(island == null)
            return;

        if(island.getWarp(sign.getLocation()) != null){
            island.deleteWarp(superiorPlayer, sign.getLocation());
        }
        else{
            if(sign.getLine(0).equalsIgnoreCase(plugin.getSettings().getVisitorsSign().getActive())){
                island.setVisitorsLocation(null);
            }
        }
    }

}
