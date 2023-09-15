package com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.DatabaseLoader_V1;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonDeserializer implements IDeserializer {

    private static final Gson gson = new Gson();

    @Nullable
    private final DatabaseLoader_V1 databaseLoader;

    public JsonDeserializer(@Nullable DatabaseLoader_V1 databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    public Map<String, Integer> deserializeMissions(String missions) {
        Map<String, Integer> completedMissions = new HashMap<>();

        JsonArray missionsArray = gson.fromJson(missions, JsonArray.class);
        missionsArray.forEach(missionElement -> {
            JsonObject missionObject = missionElement.getAsJsonObject();

            String name = missionObject.get("name").getAsString();
            int finishCount = missionObject.get("finishCount").getAsInt();

            completedMissions.put(name, finishCount);
        });

        return completedMissions;
    }

    public String[] deserializeHomes(String locationParam) {
        String[] locations = new String[World.Environment.values().length];

        JsonArray locationsArray = gson.fromJson(locationParam, JsonArray.class);
        locationsArray.forEach(locationElement -> {
            JsonObject locationObject = locationElement.getAsJsonObject();
            try {
                int i = World.Environment.valueOf(locationObject.get("env").getAsString()).ordinal();
                locations[i] = locationObject.get("location").getAsString();
            } catch (Exception ignored) {
            }
        });

        return locations;
    }

    public List<PlayerAttributes> deserializePlayers(String players) {
        List<PlayerAttributes> playerAttributes = new LinkedList<>();
        if (databaseLoader != null) {
            JsonArray playersArray = gson.fromJson(players, JsonArray.class);
            playersArray.forEach(uuid -> {
                PlayerAttributes _playerAttributes = databaseLoader.getPlayerAttributes(uuid.getAsString());
                if (_playerAttributes != null)
                    playerAttributes.add(_playerAttributes);
            });
        }
        return Collections.unmodifiableList(playerAttributes);
    }

    public Map<UUID, PlayerPrivilegeNode> deserializePlayerPerms(String permissionNodes) {
        Map<UUID, PlayerPrivilegeNode> playerPermissions = new HashMap<>();

        JsonObject globalObject = gson.fromJson(permissionNodes, JsonObject.class);
        JsonArray playersArray = globalObject.getAsJsonArray("players");

        playersArray.forEach(playerElement -> {
            JsonObject playerObject = playerElement.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                JsonArray permsArray = playerObject.getAsJsonArray("permissions");
                PlayerPrivilegeNode playerPermissionNode = new PlayerPrivilegeNode(null, null, "");
                playerPermissions.put(uuid, playerPermissionNode);

                for (JsonElement permElement : permsArray) {
                    try {
                        JsonObject permObject = permElement.getAsJsonObject();
                        IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permObject.get("name").getAsString());
                        playerPermissionNode.setPermission(islandPrivilege, permObject.get("status").getAsString().equals("1"));
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        });

        return playerPermissions;
    }

    public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
        Map<IslandPrivilege, PlayerRole> rolePermissions = new HashMap<>();

        JsonObject globalObject = gson.fromJson(permissionNodes, JsonObject.class);
        JsonArray rolesArray = globalObject.getAsJsonArray("roles");

        rolesArray.forEach(roleElement -> {
            JsonObject roleObject = roleElement.getAsJsonObject();
            PlayerRole playerRole = SPlayerRole.fromId(roleObject.get("id").getAsInt());
            roleObject.getAsJsonArray("permissions").forEach(permElement -> {
                try {
                    IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permElement.getAsString());
                    rolePermissions.put(islandPrivilege, playerRole);
                } catch (Exception ignored) {
                }
            });
        });

        return rolePermissions;
    }

    public Map<String, Integer> deserializeUpgrades(String upgrades) {
        Map<String, Integer> upgradesMap = new HashMap<>();

        JsonArray upgradesArray = gson.fromJson(upgrades, JsonArray.class);
        upgradesArray.forEach(upgradeElement -> {
            JsonObject upgradeObject = upgradeElement.getAsJsonObject();
            String name = upgradeObject.get("name").getAsString();
            int level = upgradeObject.get("level").getAsInt();
            upgradesMap.put(name, level);
        });

        return upgradesMap;
    }

    public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
        List<IslandWarpAttributes> islandWarpList = new LinkedList<>();

        JsonArray warpsArray = gson.fromJson(islandWarps, JsonArray.class);
        warpsArray.forEach(warpElement -> {
            JsonObject warpObject = warpElement.getAsJsonObject();
            String name = warpObject.get("name").getAsString();
            String warpCategory = warpObject.get("category").getAsString();
            String location = warpObject.get("location").getAsString();
            boolean privateWarp = warpObject.get("private").getAsInt() == 1;
            String icon = warpObject.has("icon") ? warpObject.get("icon").getAsString() : "";

            islandWarpList.add(new IslandWarpAttributes()
                    .setValue(IslandWarpAttributes.Field.NAME, name)
                    .setValue(IslandWarpAttributes.Field.CATEGORY, warpCategory)
                    .setValue(IslandWarpAttributes.Field.LOCATION, location)
                    .setValue(IslandWarpAttributes.Field.PRIVATE_STATUS, privateWarp)
                    .setValue(IslandWarpAttributes.Field.ICON, icon));
        });

        return Collections.unmodifiableList(islandWarpList);
    }

    public KeyMap<Integer> deserializeBlockLimits(String blocks) {
        KeyMap<Integer> blockLimits = KeyMaps.createHashMap(KeyIndicator.MATERIAL);

        JsonArray blockLimitsArray = gson.fromJson(blocks, JsonArray.class);
        blockLimitsArray.forEach(blockLimitElement -> {
            JsonObject blockLimitObject = blockLimitElement.getAsJsonObject();
            Key blockKey = Keys.ofMaterialAndData(blockLimitObject.get("id").getAsString());
            int limit = blockLimitObject.get("limit").getAsInt();
            blockLimits.put(blockKey, limit);
        });

        return blockLimits;
    }

    public Map<UUID, Rating> deserializeRatings(String ratings) {
        Map<UUID, Rating> ratingsMap = new HashMap<>();

        JsonArray ratingsArray = gson.fromJson(ratings, JsonArray.class);
        ratingsArray.forEach(ratingElement -> {
            JsonObject ratingObject = ratingElement.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(ratingObject.get("player").getAsString());
                Rating rating = Rating.valueOf(ratingObject.get("rating").getAsInt());
                ratingsMap.put(uuid, rating);
            } catch (Exception ignored) {
            }
        });

        return ratingsMap;
    }

    public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
        Map<IslandFlag, Byte> islandFlags = new HashMap<>();

        JsonArray islandFlagsArray = gson.fromJson(settings, JsonArray.class);
        islandFlagsArray.forEach(islandFlagElement -> {
            JsonObject islandFlagObject = islandFlagElement.getAsJsonObject();
            try {
                IslandFlag islandFlag = IslandFlag.getByName(islandFlagObject.get("name").getAsString());
                byte status = islandFlagObject.get("status").getAsByte();
                islandFlags.put(islandFlag, status);
            } catch (Exception ignored) {
            }
        });

        return islandFlags;
    }

    public KeyMap<Integer>[] deserializeGenerators(String generator) {
        // noinspection all
        KeyMap<Integer>[] cobbleGenerator = new KeyMap[World.Environment.values().length];

        JsonArray generatorWorldsArray = gson.fromJson(generator, JsonArray.class);
        generatorWorldsArray.forEach(generatorWorldElement -> {
            JsonObject generatorWorldObject = generatorWorldElement.getAsJsonObject();
            try {
                int i = World.Environment.valueOf(generatorWorldObject.get("env").getAsString()).ordinal();
                generatorWorldObject.getAsJsonArray("rates").forEach(generatorElement -> {
                    JsonObject generatorObject = generatorElement.getAsJsonObject();
                    Key blockKey = Keys.ofMaterialAndData(generatorObject.get("id").getAsString());
                    int rate = generatorObject.get("rate").getAsInt();
                    (cobbleGenerator[i] = KeyMaps.createHashMap(KeyIndicator.MATERIAL)).put(blockKey, rate);
                });
            } catch (Exception ignored) {
            }
        });

        return cobbleGenerator;
    }

    public List<Pair<UUID, Long>> deserializeVisitors(String visitors) {
        List<Pair<UUID, Long>> visitorsList = new LinkedList<>();

        JsonArray playersArray = gson.fromJson(visitors, JsonArray.class);

        playersArray.forEach(playerElement -> {
            JsonObject playerObject = playerElement.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                long lastTimeRecorded = playerObject.get("lastTimeRecorded").getAsLong();
                visitorsList.add(new Pair<>(uuid, lastTimeRecorded));
            } catch (Exception ignored) {
            }
        });

        return Collections.unmodifiableList(visitorsList);
    }

    public KeyMap<Integer> deserializeEntityLimits(String entities) {
        KeyMap<Integer> entityLimits = KeyMaps.createIdentityHashMap(KeyIndicator.ENTITY_TYPE);

        JsonArray entityLimitsArray = gson.fromJson(entities, JsonArray.class);
        entityLimitsArray.forEach(entityLimitElement -> {
            JsonObject entityLimitObject = entityLimitElement.getAsJsonObject();
            Key entity = Keys.ofEntityType(entityLimitObject.get("id").getAsString());
            int limit = entityLimitObject.get("limit").getAsInt();
            entityLimits.put(entity, limit);
        });

        return entityLimits;
    }

    public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
        Map<PotionEffectType, Integer> islandEffects = new HashMap<>();

        JsonArray effectsArray = gson.fromJson(effects, JsonArray.class);
        effectsArray.forEach(effectElement -> {
            JsonObject effectObject = effectElement.getAsJsonObject();
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectObject.get("type").getAsString());
            if (potionEffectType != null) {
                int level = effectObject.get("level").getAsInt();
                islandEffects.put(potionEffectType, level);
            }
        });

        return islandEffects;
    }

    public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
        List<IslandChestAttributes> islandChestList = new LinkedList<>();

        JsonArray islandChestsArray = gson.fromJson(islandChest, JsonArray.class);
        islandChestsArray.forEach(islandChestElement -> {
            JsonObject islandChestObject = islandChestElement.getAsJsonObject();
            int index = islandChestObject.get("index").getAsInt();
            String contents = islandChestObject.get("contents").getAsString();

            islandChestList.add(new IslandChestAttributes()
                    .setValue(IslandChestAttributes.Field.INDEX, index)
                    .setValue(IslandChestAttributes.Field.CONTENTS, contents));
        });

        return Collections.unmodifiableList(islandChestList);
    }

    public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
        Map<PlayerRole, Integer> roleLimits = new HashMap<>();

        JsonArray roleLimitsArray = gson.fromJson(roles, JsonArray.class);
        roleLimitsArray.forEach(roleElement -> {
            JsonObject roleObject = roleElement.getAsJsonObject();
            PlayerRole playerRole = SPlayerRole.fromId(roleObject.get("id").getAsInt());
            if (playerRole != null) {
                int limit = roleObject.get("limit").getAsInt();
                roleLimits.put(playerRole, limit);
            }
        });

        return roleLimits;
    }

    public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
        List<WarpCategoryAttributes> warpCategories = new LinkedList<>();

        JsonArray warpCategoriesArray = gson.fromJson(categories, JsonArray.class);
        warpCategoriesArray.forEach(warpCategoryElement -> {
            JsonObject warpCategoryObject = warpCategoryElement.getAsJsonObject();
            String name = warpCategoryObject.get("name").getAsString();
            int slot = warpCategoryObject.get("slot").getAsInt();
            String icon = warpCategoryObject.get("icon").getAsString();
            warpCategories.add(new WarpCategoryAttributes()
                    .setValue(WarpCategoryAttributes.Field.NAME, name)
                    .setValue(WarpCategoryAttributes.Field.SLOT, slot)
                    .setValue(WarpCategoryAttributes.Field.ICON, icon));
        });

        return Collections.unmodifiableList(warpCategories);
    }

    @Override
    public String deserializeBlockCounts(String blockCountsParam) {
        gson.fromJson(blockCountsParam, JsonArray.class);
        return blockCountsParam;
    }

    @Override
    public String deserializeDirtyChunks(String dirtyChunksParam) {
        gson.fromJson(dirtyChunksParam, JsonObject.class);
        return dirtyChunksParam;
    }

}