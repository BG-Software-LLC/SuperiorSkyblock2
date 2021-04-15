package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class IslandSerializer {

    private static final Gson gson = new GsonBuilder().create();

    private IslandSerializer(){

    }

    public static String serializePlayers(Collection<SuperiorPlayer> playersCollection) {
        JsonArray playersArray = new JsonArray();
        playersCollection.forEach(superiorPlayer -> playersArray.add(new JsonPrimitive(
                superiorPlayer.getUniqueId().toString())));
        return gson.toJson(playersArray);
    }

    public static String serializePlayersWithTimes(Collection<Pair<SuperiorPlayer, Long>> playersCollection) {
        JsonArray playersArray = new JsonArray();
        playersCollection.forEach(pair -> {
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("uuid", pair.getKey().getUniqueId().toString());
            playerObject.addProperty("lastTimeRecorded", pair.getValue());
            playersArray.add(playerObject);
        });
        return gson.toJson(playersArray);
    }

    public static String serializePermissions(Map<SuperiorPlayer, PermissionNode> playerPermissions, Map<IslandPrivilege, PlayerRole> playerRoles){
        JsonObject globalObject = new JsonObject();
        JsonArray playersArray = new JsonArray(), rolesArray = new JsonArray();
        globalObject.add("players", playersArray);
        globalObject.add("roles", rolesArray);

        playerPermissions.forEach((superiorPlayer, permissionNode) -> {
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("uuid", superiorPlayer.getUniqueId().toString());
            playerObject.add("permissions", ((PlayerPermissionNode) permissionNode).serialize());
            playersArray.add(playerObject);
        });

        Map<PlayerRole, Set<IslandPrivilege>> roleToPrivileges = new HashMap<>();
        playerRoles.forEach((islandPrivilege, playerRole) -> roleToPrivileges
                .computeIfAbsent(playerRole, s -> new HashSet<>()).add(islandPrivilege));

        roleToPrivileges.forEach((playerRole, islandPrivileges) -> {
            JsonObject roleObject = new JsonObject();
            JsonArray permsArray = new JsonArray();

            roleObject.addProperty("id", playerRole.getId());
            roleObject.add("permissions", permsArray);
            islandPrivileges.forEach(islandPrivilege -> permsArray.add(new JsonPrimitive(islandPrivilege.getName())));

            rolesArray.add(roleObject);
        });

        return gson.toJson(globalObject);
    }


    public static String serializeBlockCounts(Map<Key, BigInteger> blockCounts){
        JsonArray blockCountsArray = new JsonArray();
        blockCounts.forEach((key, amount) -> {
            JsonObject blockCountObject = new JsonObject();
            blockCountObject.addProperty("id", key.toString());
            blockCountObject.addProperty("amount", amount.toString());
            blockCountsArray.add(blockCountObject);
        });
        return gson.toJson(blockCountsArray);
    }

    public static String serializeBlockLimits(Map<Key, Integer> blockLimits){
        JsonArray blockLimitsArray = new JsonArray();
        blockLimits.forEach((key, limit) -> {
            JsonObject blockLimitObject = new JsonObject();
            blockLimitObject.addProperty("id", key.toString());
            blockLimitObject.addProperty("limit", limit);
            blockLimitsArray.add(blockLimitObject);
        });
        return gson.toJson(blockLimitsArray);
    }

    public static String serializeEntityLimits(Map<Key, Integer> entityLimits){
        JsonArray entityLimitsArray = new JsonArray();
        entityLimits.forEach((key, limit) -> {
            JsonObject entityLimitObject = new JsonObject();
            entityLimitObject.addProperty("id", key.toString());
            entityLimitObject.addProperty("limit", limit);
            entityLimitsArray.add(entityLimitObject);
        });
        return gson.toJson(entityLimitsArray);
    }

    public static String serializeUpgrades(Map<String, Integer> upgrades){
        JsonArray upgradesArray = new JsonArray();
        upgrades.forEach((upgrade, level) -> {
            JsonObject upgradeObject = new JsonObject();
            upgradeObject.addProperty("name", upgrade);
            upgradeObject.addProperty("level", level);
            upgradesArray.add(upgradeObject);
        });
        return gson.toJson(upgradesArray);
    }

    public static String serializeWarps(Map<String, IslandWarp> islandWarps){
        JsonArray warpsArray = new JsonArray();
        islandWarps.values().forEach(islandWarp -> {
            JsonObject warpObject = new JsonObject();
            warpObject.addProperty("name", islandWarp.getName());
            if(islandWarp.getCategory() != null)
                warpObject.addProperty("category", islandWarp.getCategory().getName());
            warpObject.addProperty("location", FileUtils.fromLocation(islandWarp.getLocation()));
            warpObject.addProperty("private", islandWarp.hasPrivateFlag() ? 1 : 0);
            if(islandWarp.getRawIcon() != null)
                warpObject.addProperty("icon", ItemUtils.serializeItem(islandWarp.getRawIcon()));
            warpsArray.add(warpObject);
        });
        return gson.toJson(warpsArray);
    }

    public static String serializeRatings(Map<UUID, Rating> ratings){
        JsonArray ratingsArray = new JsonArray();
        ratings.forEach((player, rating) -> {
            JsonObject ratingObject = new JsonObject();
            ratingObject.addProperty("player", player.toString());
            ratingObject.addProperty("rating", rating.getValue());
            ratingsArray.add(ratingObject);
        });
        return gson.toJson(ratingsArray);
    }


    public static String serializeMissions(Map<Mission<?>, Integer> missions){
        JsonArray missionsArray = new JsonArray();
        missions.forEach((mission, finishCount) -> {
            JsonObject missionObject = new JsonObject();
            missionObject.addProperty("name", mission.getName());
            missionObject.addProperty("finishCount", finishCount);
            missionsArray.add(missionObject);
        });
        return gson.toJson(missionsArray);
    }

    public static String serializeIslandFlags(Map<IslandFlag, Byte> islandFlags){
        JsonArray islandFlagsArray = new JsonArray();
        islandFlags.forEach((islandFlag, status) -> {
            JsonObject islandFlagObject = new JsonObject();
            islandFlagObject.addProperty("name", islandFlag.getName());
            islandFlagObject.addProperty("status", status);
            islandFlagsArray.add(islandFlagObject);
        });
        return gson.toJson(islandFlagsArray);
    }

    public static String serializeGenerator(Map<Key, Integer>[] cobbleGenerators){
        JsonArray generatorWorldsArray = new JsonArray();

        for(int i = 0; i < cobbleGenerators.length; i++){
            World.Environment environment = World.Environment.values()[i];
            JsonObject generatorWorldObject = new JsonObject();
            JsonArray ratesArray = new JsonArray();

            generatorWorldObject.addProperty("env", environment.name());
            generatorWorldObject.add("rates", ratesArray);

            cobbleGenerators[i].forEach((key, value) -> {
                JsonObject rateObject = new JsonObject();
                rateObject.addProperty("id", key.toString());
                rateObject.addProperty("rate", value);
                ratesArray.add(rateObject);
            });

            generatorWorldsArray.add(generatorWorldObject);
        }

        return gson.toJson(generatorWorldsArray);
    }

    public static String serializeLocations(Map<World.Environment, Location> locations){
        JsonArray locationsArray = new JsonArray();
        locations.forEach((env, location) -> {
            JsonObject locationObject = new JsonObject();
            locationObject.addProperty("env", env.name());
            locationObject.addProperty("location", FileUtils.fromLocation(location));
            locationsArray.add(locationObject);
        });
        return gson.toJson(locationsArray);
    }

    public static String serializeEffects(Map<PotionEffectType, Integer> effects){
        JsonArray effectsArray = new JsonArray();
        effects.forEach((potionEffectType, level) -> {
            JsonObject effectObject = new JsonObject();
            effectObject.addProperty("type", potionEffectType.getName());
            effectObject.addProperty("level", level);
            effectsArray.add(effectObject);
        });
        return gson.toJson(effectsArray);
    }

    public static String serializeIslandChest(IslandChest[] islandChests){
        JsonArray islandChestsArray = new JsonArray();
        for (IslandChest islandChest : islandChests) {
            if(islandChest != null) {
                JsonObject islandChestObject = new JsonObject();
                islandChestObject.addProperty("index", islandChest.getIndex());
                islandChestObject.addProperty("contents", ItemUtils.serialize(islandChest.getContents()));
                islandChestsArray.add(islandChestObject);
            }
        }
        return gson.toJson(islandChestsArray);
    }

    public static String serializeRoleLimits(Map<PlayerRole, Integer> roleLimits){
        JsonArray roleLimitsArray = new JsonArray();
        roleLimits.forEach((role, limit) -> {
            JsonObject roleLimitObject = new JsonObject();
            roleLimitObject.addProperty("id", role.getId());
            roleLimitObject.addProperty("limit", limit);
            roleLimitsArray.add(roleLimitObject);
        });
        return gson.toJson(roleLimitsArray);
    }

    public static String serializeWarpCategories(Map<String, WarpCategory> warpCategories){
        JsonArray warpCategoriesArray = new JsonArray();
        warpCategories.values().forEach(warpCategory -> {
            JsonObject warpCategoryObject = new JsonObject();
            warpCategoryObject.addProperty("name", warpCategory.getName());
            warpCategoryObject.addProperty("slot", warpCategory.getSlot());
            warpCategoryObject.addProperty("icon", ItemUtils.serializeItem(warpCategory.getRawIcon()));
            warpCategoriesArray.add(warpCategoryObject);
        });
        return gson.toJson(warpCategoriesArray);
    }

    public static String serializeDirtyChunks(Set<ChunkPosition> dirtyChunks){
        JsonObject dirtyChunksObject = new JsonObject();
        dirtyChunks.forEach(chunkPosition -> {
            JsonArray dirtyChunksArray;

            if(dirtyChunksObject.has(chunkPosition.getWorldName())){
                dirtyChunksArray = dirtyChunksObject.getAsJsonArray(chunkPosition.getWorldName());
            }
            else{
                dirtyChunksArray = new JsonArray();
                dirtyChunksObject.add(chunkPosition.getWorldName(), dirtyChunksArray);
            }

            dirtyChunksArray.add(new JsonPrimitive(chunkPosition.getX() + "," + chunkPosition.getZ()));
        });
        return gson.toJson(dirtyChunksObject);
    }

}
