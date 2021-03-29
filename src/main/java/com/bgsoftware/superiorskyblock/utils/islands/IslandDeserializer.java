package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class IslandDeserializer {

    static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    static final Gson gson = new GsonBuilder().create();

    private IslandDeserializer(){

    }

    private static <T> T decode(String raw, Class<T> typeOf) throws JsonSyntaxException {
        if(raw == null || raw.isEmpty()) throw new JsonSyntaxException("");
        return gson.fromJson(raw, typeOf);
    }

    public static void deserializePlayers(String members, SyncedObject<? extends Collection<SuperiorPlayer>> membersSetSync){
        try{
            JsonArray playersArray = decode(members, JsonArray.class);
            membersSetSync.write(membersSet -> playersArray.forEach(uuid ->
                    membersSet.add(plugin.getPlayers().getSuperiorPlayer(UUID.fromString(uuid.getAsString())))));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializePlayers(members, membersSetSync);
        }
    }

    public static void deserializePlayersWithTimes(String members, SyncedObject<? extends Collection<Pair<SuperiorPlayer, Long>>> membersSetSync){
        try{
            JsonArray playersArray = decode(members, JsonArray.class);
            membersSetSync.write(membersSet -> playersArray.forEach(playerElement -> {
                JsonObject playerObject = playerElement.getAsJsonObject();
                try {
                    UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                    long lastTimeRecorded = playerObject.get("lastTimeRecorded").getAsLong();
                    membersSet.add(new Pair<>(plugin.getPlayers().getSuperiorPlayer(uuid), lastTimeRecorded));
                }catch (Exception ignored){}
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializePlayersWithTimes(members, membersSetSync);
        }
    }

    public static void deserializePermissions(String permissions, Registry<SuperiorPlayer, PlayerPermissionNode> playerPermissions, Registry<IslandPrivilege, PlayerRole> rolePermissions, Island island){
        try{
            JsonObject globalObject = decode(permissions, JsonObject.class);
            JsonArray playersArray = globalObject.getAsJsonArray("players");
            JsonArray rolesArray = globalObject.getAsJsonArray("roles");

            playersArray.forEach(playerElement -> {
                JsonObject playerObject = playerElement.getAsJsonObject();
                try {
                    UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                    SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid);
                    JsonArray permsArray = playerObject.getAsJsonArray("permissions");
                    playerPermissions.add(superiorPlayer, new PlayerPermissionNode(superiorPlayer, island, permsArray));
                }catch (Exception ignored){}
            });

            rolesArray.forEach(roleElement -> {
                JsonObject roleObject = roleElement.getAsJsonObject();
                PlayerRole playerRole = SPlayerRole.fromId(roleObject.get("id").getAsInt());
                roleObject.getAsJsonArray("permissions").forEach(permElement -> {
                    try{
                        IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permElement.getAsString());
                        rolePermissions.add(islandPrivilege, playerRole);
                    }catch (Exception ignored){}
                });
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializePermissions(permissions, playerPermissions, rolePermissions, island);
        }
    }

    public static void deserializeUpgrades(String upgrades, Registry<String, Integer> upgradesMap){
        try{
            JsonArray upgradesArray = decode(upgrades, JsonArray.class);
            upgradesArray.forEach(upgradeElement -> {
                JsonObject upgradeObject = upgradeElement.getAsJsonObject();
                String name = upgradeObject.get("name").getAsString();
                int level = upgradeObject.get("level").getAsInt();
                if(plugin.getUpgrades().getUpgrade(name) != null)
                    upgradesMap.add(name, level);
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeUpgrades(upgrades, upgradesMap);
        }
    }

    public static void deserializeWarps(String warps, Island island){
        try{
            JsonArray warpsArray = decode(warps, JsonArray.class);
            warpsArray.forEach(warpElement -> {
                JsonObject warpObject = warpElement.getAsJsonObject();
                String name = IslandUtils.getWarpName(warpObject.get("name").getAsString());

                if(name.isEmpty())
                    return;

                WarpCategory warpCategory = null;
                if(warpObject.has("category"))
                    warpCategory = island.createWarpCategory(warpObject.get("category").getAsString());

                Location location = FileUtils.toLocation(warpObject.get("location").getAsString());
                boolean privateWarp = warpObject.get("private").getAsInt() == 1;

                IslandWarp islandWarp = island.createWarp(name, location, warpCategory);
                islandWarp.setPrivateFlag(privateWarp);

                if(warpObject.has("icon"))
                    islandWarp.setIcon(ItemUtils.deserializeItem(warpObject.get("icon").getAsString()));
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeWarps(warps, island);
        }
    }

    public static void deserializeBlockCounts(String blocks, Island island){
        try{
            JsonArray blockCountsArray = decode(blocks, JsonArray.class);
            blockCountsArray.forEach(blockCountElement -> {
                JsonObject blockCountObject = blockCountElement.getAsJsonObject();
                Key blockKey = Key.of(blockCountObject.get("id").getAsString());
                BigInteger amount = new BigInteger(blockCountObject.get("amount").getAsString());
                island.handleBlockPlace(blockKey, amount, false);
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeBlockCounts(blocks, island);
        }
    }

    public static void deserializeBlockLimits(String blocks, SyncedObject<KeyMap<UpgradeValue<Integer>>> blockLimits){
        try{
            JsonArray blockLimitsArray = decode(blocks, JsonArray.class);
            blockLimits.write(_blockLimits -> blockLimitsArray.forEach(blockLimitElement -> {
                JsonObject blockLimitObject = blockLimitElement.getAsJsonObject();
                Key blockKey = Key.of(blockLimitObject.get("id").getAsString());
                int limit = blockLimitObject.get("limit").getAsInt();
                _blockLimits.put(blockKey, new UpgradeValue<>(limit, i -> i < 0));
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeBlockLimits(blocks, blockLimits);
        }
    }

    public static void deserializeEntityLimits(String entities, SyncedObject<KeyMap<UpgradeValue<Integer>>> entityLimits){
        try{
            JsonArray entityLimitsArray = decode(entities, JsonArray.class);
            entityLimits.write(_entityLimits -> entityLimitsArray.forEach(entityLimitElement -> {
                JsonObject entityLimitObject = entityLimitElement.getAsJsonObject();
                Key entity = Key.of(entityLimitObject.get("id").getAsString());
                int limit = entityLimitObject.get("limit").getAsInt();
                _entityLimits.put(entity, new UpgradeValue<>(limit, i -> i < 0));
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeEntityLimits(entities, entityLimits);
        }
    }

    public static void deserializeRatings(String ratings, Registry<UUID, Rating> ratingsMap){
        try{
            JsonArray ratingsArray = decode(ratings, JsonArray.class);
            ratingsArray.forEach(ratingElement -> {
                JsonObject ratingObject = ratingElement.getAsJsonObject();
                try{
                    UUID uuid = UUID.fromString(ratingObject.get("player").getAsString());
                    Rating rating = Rating.valueOf(ratingObject.get("rating").getAsInt());
                    ratingsMap.add(uuid, rating);
                }catch (Exception ignored){}
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeRatings(ratings, ratingsMap);
        }
    }

    public static void deserializeMissions(String missions, Registry<Mission<?>, Integer> completedMissions){
        try{
            JsonArray missionsArray = decode(missions, JsonArray.class);
            missionsArray.forEach(missionElement -> {
                JsonObject missionObject = missionElement.getAsJsonObject();

                String name = missionObject.get("name").getAsString();
                int finishCount = missionObject.get("finishCount").getAsInt();

                Mission<?> mission = plugin.getMissions().getMission(name);

                if(mission != null)
                    completedMissions.add(mission, finishCount);
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeMissions(missions, completedMissions);
        }
    }

    public static void deserializeIslandFlags(String settings, Registry<IslandFlag, Byte> islandFlags){
        try{
            JsonArray islandFlagsArray = decode(settings, JsonArray.class);
            islandFlagsArray.forEach(islandFlagElement -> {
                JsonObject islandFlagObject = islandFlagElement.getAsJsonObject();
                try{
                    IslandFlag islandFlag = IslandFlag.getByName(islandFlagObject.get("name").getAsString());
                    byte status = islandFlagObject.get("status").getAsByte();
                    islandFlags.add(islandFlag, status);
                }catch (Exception ignored){}
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeIslandFlags(settings, islandFlags);
        }
    }

    public static void deserializeGenerators(String generator, SyncedObject<KeyMap<UpgradeValue<Integer>>>[] cobbleGenerator){
        try{
            JsonArray generatorWorldsArray = decode(generator, JsonArray.class);
            generatorWorldsArray.forEach(generatorWorldElement -> {
                JsonObject generatorWorldObject = generatorWorldElement.getAsJsonObject();
                try{
                    int i = World.Environment.valueOf(generatorWorldObject.get("env").getAsString()).ordinal();
                    generatorWorldObject.getAsJsonArray("rates").forEach(generatorElement -> {
                        JsonObject generatorObject = generatorElement.getAsJsonObject();
                        Key blockKey = Key.of(generatorObject.get("id").getAsString());
                        int rate = generatorObject.get("rate").getAsInt();
                        (cobbleGenerator[i] = SyncedObject.of(new KeyMap<>())).write(_cobbleGenerator ->
                                _cobbleGenerator.put(blockKey, new UpgradeValue<>(rate, n -> n < 0)));
                    });
                }catch (Exception ignored){}
            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeGenerators(generator, cobbleGenerator);
        }
    }

    public static void deserializeLocations(String locationParam, SyncedObject<Location[]> locations){
        try{
            JsonArray locationsArray = decode(locationParam, JsonArray.class);
            locations.write(_locations -> locationsArray.forEach(locationElement -> {
                JsonObject locationObject = locationElement.getAsJsonObject();
                try{
                    int i = World.Environment.valueOf(locationObject.get("env").getAsString()).ordinal();
                    _locations[i] = FileUtils.toLocation(locationObject.get("location").getAsString());
                }catch (Exception ignored){}
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeLocations(locationParam, locations);
        }
    }

    public static void deserializeEffects(String effects, SyncedObject<Map<PotionEffectType, UpgradeValue<Integer>>> islandEffects){
        try{
            JsonArray effectsArray = decode(effects, JsonArray.class);
            islandEffects.write(_islandEffects -> effectsArray.forEach(effectElement -> {
                JsonObject effectObject = effectElement.getAsJsonObject();
                PotionEffectType potionEffectType = PotionEffectType.getByName(effectObject.get("type").getAsString());
                if(potionEffectType != null){
                    int level = effectObject.get("level").getAsInt();
                    _islandEffects.put(potionEffectType, new UpgradeValue<>(level, i -> i < 0));
                }
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeEffects(effects, islandEffects);
        }
    }

    public static void deserializeIslandChest(Island island, String islandChest, SyncedObject<IslandChest[]> islandChestSync){
        try{
            JsonArray islandChestsArray = decode(islandChest, JsonArray.class);
            List<IslandChest> islandChestList = new ArrayList<>();

            islandChestsArray.forEach(islandChestElement -> {
                JsonObject islandChestObject = islandChestElement.getAsJsonObject();
                int i = islandChestObject.get("index").getAsInt();
                String contents = islandChestObject.get("contents").getAsString();
                islandChestList.add(i, SIslandChest.createChest(island, i, ItemUtils.deserialize(contents)));
            });

            islandChestSync.set(islandChestList.toArray(new IslandChest[0]));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeIslandChest(island, islandChest, islandChestSync);
        }
    }

    public static void deserializeRoleLimits(String roles, SyncedObject<Map<PlayerRole, UpgradeValue<Integer>>> roleLimits){
        try{
            JsonArray roleLimitsArray = decode(roles, JsonArray.class);
            roleLimits.write(_roleLimits -> roleLimitsArray.forEach(roleElement -> {
                JsonObject roleObject = roleElement.getAsJsonObject();
                PlayerRole playerRole = SPlayerRole.fromId(roleObject.get("id").getAsInt());
                if(playerRole != null){
                    int limit = roleObject.get("limit").getAsInt();
                    _roleLimits.put(playerRole, new UpgradeValue<>(limit, i -> i < 0));
                }
            }));
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeRoleLimits(roles, roleLimits);
        }
    }

    public static void deserializeWarpCategories(String warpCategories, Island island){
        try{
            JsonArray warpCategoriesArray = decode(warpCategories, JsonArray.class);
            warpCategoriesArray.forEach(warpCategoryElement -> {
                JsonObject warpCategoryObject = warpCategoryElement.getAsJsonObject();
                String name = StringUtils.stripColors(warpCategoryObject.get("name").getAsString());

                WarpCategory warpCategory = island.getWarpCategory(name);

                if(warpCategory != null){
                    if(warpCategory.getWarps().isEmpty()) {
                        island.deleteCategory(warpCategory);
                        return;
                    }

                    int slot = warpCategoryObject.get("slot").getAsInt();
                    warpCategory.setSlot(slot);

                    ItemStack icon = ItemUtils.deserializeItem(warpCategoryObject.get("icon").getAsString());
                    if(icon != null)
                        warpCategory.setIcon(icon);
                }

            });
        }catch (JsonSyntaxException ex){
            IslandDeserializer_Old.deserializeWarpCategories(warpCategories, island);
        }
    }

}
