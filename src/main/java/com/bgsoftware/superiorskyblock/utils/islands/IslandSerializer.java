package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;

import java.util.Map;
import java.util.UUID;

public final class IslandSerializer {

    public static String serializePermissions(Map<Object, SPermissionNode> permissions){
        StringBuilder permissionNodes = new StringBuilder();
        permissions.keySet().forEach(_islandRole ->
                permissionNodes.append(",").append(_islandRole.toString()).append("=").append(permissions.get(_islandRole).getAsStatementString()));
        return permissionNodes.toString();
    }

    public static String serializeBlockCounts(KeyMap<Integer> blocks){
        StringBuilder blockCounts = new StringBuilder();
        blocks.keySet().forEach(blockKey ->
                blockCounts.append(";").append(blockKey).append("=").append(blocks.get(blockKey)));
        return blockCounts.length() == 0 ? "" : blockCounts.toString().substring(1);
    }

    public static String serializeBlockLimits(KeyMap<Integer> blocks){
        StringBuilder blockLimits = new StringBuilder();
        blocks.keySet().forEach(blockKey ->
                blockLimits.append(",").append(blockKey).append("=").append(blocks.get(blockKey)));
        return blockLimits.length() == 0 ? "" : blockLimits.toString().substring(1);
    }


    public static String serializeUpgrades(Map<String, Integer> upgrades){
        StringBuilder upgradesBuilder = new StringBuilder();
        upgrades.keySet().forEach(upgrade ->
                upgradesBuilder.append(",").append(upgrade).append("=").append(upgrades.get(upgrade)));
        return upgradesBuilder.toString();
    }

    public static String serializeWarps(Map<String, SIsland.WarpData> warps){
        StringBuilder warpsBuilder = new StringBuilder();
        warps.keySet().forEach(warp -> {
            SIsland.WarpData warpData = warps.get(warp);
            warpsBuilder.append(";").append(warp).append("=").append(FileUtil.fromLocation(warpData.location)).append("=").append(warpData.privateFlag);
        });
        return warpsBuilder.length() == 0 ? "" : warpsBuilder.toString().substring(1);
    }

    public static String serializeRatings(Map<UUID, Rating> ratings){
        StringBuilder ratingsBuilder = new StringBuilder();
        ratings.keySet().forEach(_uuid ->
                ratingsBuilder.append(";").append(_uuid).append("=").append(ratings.get(_uuid).getValue()));
        return ratingsBuilder.length() == 0 ? "" : ratingsBuilder.toString().substring(1);
    }


}
