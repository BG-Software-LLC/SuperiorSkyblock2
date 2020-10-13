package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class IslandSerializer {

    private IslandSerializer(){

    }

    public static String serializePlayers(Collection<SuperiorPlayer> collection) {
        StringBuilder builder = new StringBuilder();
        collection.forEach(superiorPlayer -> builder.append(",").append(superiorPlayer.getUniqueId().toString()));
        return builder.toString();
    }

    public static String serializePlayersWithTimes(Collection<Pair<SuperiorPlayer, Long>> collection) {
        StringBuilder builder = new StringBuilder();
        collection.forEach(pair -> builder.append(",").append(pair.getKey().getUniqueId().toString()).append(";").append(pair.getValue()));
        return builder.toString();
    }

    public static String serializePermissions(Map<SuperiorPlayer, PermissionNode> playerPermissions, Map<IslandPrivilege, PlayerRole> playerRoles){
        StringBuilder permissionNodes = new StringBuilder();
        playerPermissions.forEach((key, value) -> permissionNodes.append(",").append(key.getUniqueId().toString())
                .append("=").append(((PlayerPermissionNode) value).getAsStatementString()));

        Registry<PlayerRole, Set<IslandPrivilege>> reorderRoles = Registry.createRegistry();
        playerRoles.forEach((key, value) -> reorderRoles.computeIfAbsent(value, s -> new HashSet<>()).add(key));

        reorderRoles.entries().forEach(entry ->
                permissionNodes.append(",").append(entry.getKey().toString()).append("=").append(getAsStatementString(entry.getValue())));

        reorderRoles.delete();

        return permissionNodes.toString();
    }

    private static String getAsStatementString(Set<IslandPrivilege> islandPrivileges){
        StringBuilder stringBuilder = new StringBuilder();
        islandPrivileges.forEach(privilege -> stringBuilder.append(";").append(privilege.getName()).append(":").append("1"));
        return stringBuilder.length() == 0 ? "" : stringBuilder.substring(1);
    }

    public static String serializeBlockCounts(Map<Key, Integer> blocks){
        StringBuilder blockCounts = new StringBuilder();
        blocks.keySet().forEach(blockKey ->
                blockCounts.append(";").append(blockKey).append("=").append(blocks.get(blockKey)));
        return blockCounts.length() == 0 ? "" : blockCounts.toString().substring(1);
    }

    public static String serializeBlockLimits(Map<Key, Integer> blocks){
        StringBuilder blockLimits = new StringBuilder();
        blocks.forEach((blockKey, integer) ->
                blockLimits.append(",").append(blockKey).append("=").append(integer));
        return blockLimits.length() == 0 ? "" : blockLimits.toString().substring(1);
    }

    public static String serializeEntityLimits(Map<Key, Integer> entities){
        StringBuilder entityLimits = new StringBuilder();
        entities.forEach((entityKey, integer) ->
                entityLimits.append(",").append(entityKey).append("=").append(integer));
        return entityLimits.length() == 0 ? "" : entityLimits.toString().substring(1);
    }

    public static String serializeUpgrades(Map<String, Integer> upgrades){
        StringBuilder upgradesBuilder = new StringBuilder();
        upgrades.forEach((upgrade, level) ->
                upgradesBuilder.append(",").append(upgrade).append("=").append(level));
        return upgradesBuilder.toString();
    }

    public static String serializeWarps(Map<String, IslandWarp> warps){
        StringBuilder warpsBuilder = new StringBuilder();
        warps.forEach((warpName, warpData) -> warpsBuilder.append(";").append(warpName).append("=")
                .append(FileUtils.fromLocation(warpData.getLocation())).append("=").append(warpData.hasPrivateFlag()));
        return warpsBuilder.length() == 0 ? "" : warpsBuilder.toString().substring(1);
    }

    public static String serializeRatings(Map<UUID, Rating> ratings){
        StringBuilder ratingsBuilder = new StringBuilder();
        ratings.forEach((uuid, rating) ->
                ratingsBuilder.append(";").append(uuid).append("=").append(rating.getValue()));
        return ratingsBuilder.length() == 0 ? "" : ratingsBuilder.toString().substring(1);
    }


    public static String serializeMissions(Map<Mission<?>, Integer> missions){
        StringBuilder missionsBuilder = new StringBuilder();
        missions.forEach((key, value) -> missionsBuilder.append(";").append(key).append("=").append(value));
        return missionsBuilder.length() == 0 ? "" : missionsBuilder.toString().substring(1);
    }

    public static String serializeSettings(Map<IslandFlag, Byte> islandSettings){
        StringBuilder missionsBuilder = new StringBuilder();
        islandSettings.forEach((key, value) -> missionsBuilder.append(";").append(key.getName()).append("=").append(value));
        return missionsBuilder.length() == 0 ? "" : missionsBuilder.toString().substring(1);
    }

    public static String serializeGenerator(Map<Key, Integer>[] cobbleGenerators){
        StringBuilder generatorsBuilder = new StringBuilder();
        for(int i = 0; i < cobbleGenerators.length; i++) {
            StringBuilder generatorBuilder = new StringBuilder();
            World.Environment environment = World.Environment.values()[i];
            cobbleGenerators[i].forEach((key, value) ->
                    generatorBuilder.append(",").append(key).append("=").append(value));
            generatorsBuilder.append(";").append(environment).append(":")
                    .append(generatorBuilder.length() == 0 ? "" : generatorBuilder.toString().substring(1));
        }
        return generatorsBuilder.length() == 0 ? "" : generatorsBuilder.toString().substring(1);
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

    public static String serializeEffects(Map<PotionEffectType, Integer> effects){
        StringBuilder islandEffects = new StringBuilder();
        effects.forEach((potionEffectType, level) ->
                islandEffects.append(",").append(potionEffectType.getName()).append("=").append(level));
        return islandEffects.length() == 0 ? "" : islandEffects.toString().substring(1);
    }

    public static String serializeIslandChest(SyncedObject<IslandChest[]> islandChest){
        return islandChest.readAndGet(IslandSerializer::serializeIslandChest);
    }

    public static String serializeIslandChest(IslandChest[] islandChest){
        StringBuilder stringBuilder = new StringBuilder();

        for(IslandChest _islandChest : islandChest) {
            if(_islandChest != null) {
                stringBuilder.append("\n").append(ItemUtils.serialize(_islandChest.getContents()));
            }
        }

        return stringBuilder.length() == 0 ? "" : stringBuilder.substring(1);
    }

}
