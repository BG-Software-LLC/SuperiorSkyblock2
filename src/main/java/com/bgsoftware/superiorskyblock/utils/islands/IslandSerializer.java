package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.Set;
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
            warpsBuilder.append(";").append(warp).append("=").append(FileUtils.fromLocation(warpData.location)).append("=").append(warpData.privateFlag);
        });
        return warpsBuilder.length() == 0 ? "" : warpsBuilder.toString().substring(1);
    }

    public static String serializeRatings(Map<UUID, Rating> ratings){
        StringBuilder ratingsBuilder = new StringBuilder();
        ratings.keySet().forEach(_uuid ->
                ratingsBuilder.append(";").append(_uuid).append("=").append(ratings.get(_uuid).getValue()));
        return ratingsBuilder.length() == 0 ? "" : ratingsBuilder.toString().substring(1);
    }

    public static String serializeMissions(Set<String> missions){
        StringBuilder missionsBuilder = new StringBuilder();
        missions.forEach(mission ->
                missionsBuilder.append(";").append(mission));
        return missionsBuilder.length() == 0 ? "" : missionsBuilder.toString().substring(1);
    }

    public static String serializeSettings(Set<IslandSettings> islandSettings){
        StringBuilder missionsBuilder = new StringBuilder();
        islandSettings.forEach(settings ->
                missionsBuilder.append(";").append(settings));
        return missionsBuilder.length() == 0 ? "" : missionsBuilder.toString().substring(1);
    }

    public static String serializeGenerator(Map<String, Integer> cobbleGenerator){
        StringBuilder missionsBuilder = new StringBuilder();
        cobbleGenerator.forEach((key, value) ->
                missionsBuilder.append(",").append(key).append("=").append(value));
        return missionsBuilder.length() == 0 ? "" : missionsBuilder.toString().substring(1);
    }

    public static String serializeLocations(Map<World.Environment, Location> locations){
        StringBuilder locationsBuilder = new StringBuilder();
        for(Map.Entry<World.Environment, Location> entry : locations.entrySet()){
            Location loc = entry.getValue();
            String locationString = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
            locationsBuilder.append(";").append(entry.getKey().name().toLowerCase()).append("=").append(locationString);
        }
        return locationsBuilder.length() == 0 ? "" : locationsBuilder.substring(1);
    }

}
