package com.bgsoftware.superiorskyblock.data.loaders;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.data.sql.SQLSession;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.attributes.IslandAttributes;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.island.warps.SWarpCategory;
import com.bgsoftware.superiorskyblock.player.PlayerAttributes;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DatabaseLoader_V1 implements DatabaseLoader {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Gson gson = new Gson();

    private static File databaseFile;
    private static SQLSession sqlSession;

    private final List<SuperiorPlayer> loadedPlayers = new ArrayList<>();
    private final List<Island> loadedIslands = new ArrayList<>();
    private final Map<String, WarpCategory> loadedWarpCategories = new HashMap<>();

    @Override
    public DatabaseLoadedData loadData() {
        SuperiorSkyblockPlugin.log("&a[Database-Converter] Detected old database - starting to convert data...");

        sqlSession.executeQuery("SELECT * FROM {prefix}players;", resultSet -> {
            while (resultSet.next()) {
                loadedPlayers.add(loadPlayer(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedPlayers.size() + " players in the database.");

        sqlSession.executeQuery("SELECT * FROM {prefix}islands;", resultSet -> {
            while (resultSet.next()) {
                loadedIslands.add(loadIsland(resultSet));
            }
        });

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Found " + loadedIslands.size() + " islands in the database.");

        sqlSession.close();

        AtomicBoolean failedBackup = new AtomicBoolean(false);

        if(sqlSession.isUsingMySQL()){
            sqlSession.executeUpdate("RENAME TABLE {prefix}islands TO {prefix}islands_bkp", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}players TO {prefix}players_bkp", failure -> failedBackup.set(true));
        }
        else {
            if (!databaseFile.renameTo(new File(databaseFile.getParentFile(), "database-bkp.db"))) {
                failedBackup.set(true);
            }
        }

        if(failedBackup.get()){
            SuperiorSkyblockPlugin.log("&c[Database-Converter] Failed to create a backup for the database file.");
        }
        else{
            SuperiorSkyblockPlugin.log("&a[Database-Converter] Successfully created a backup for the database.");
        }

        return new DatabaseLoadedData(loadedPlayers, loadedIslands);
    }

    public static void register(){
        if(isDatabaseOldFormat())
            plugin.getDataHandler().addDatabaseLoader(new DatabaseLoader_V1());
    }

    private static boolean isDatabaseOldFormat(){
        sqlSession = new SQLSession();

        if(!sqlSession.isUsingMySQL()) {
            databaseFile = new File("plugins", "SuperiorSkyblock2\\database.db");

            if (!databaseFile.exists())
                return false;
        }

        if (!sqlSession.createConnection(SuperiorSkyblockPlugin.getPlugin(), false)) {
            sqlSession.close();
            return false;
        }

        if(sqlSession.doesTableExist("islands_members")){
            sqlSession.close();
            return false;
        }

        return true;
    }

    private SuperiorPlayer loadPlayer(ResultSet resultSet) throws SQLException {
        PlayerRole playerRole;

        try{
            playerRole = SPlayerRole.fromId(Integer.parseInt(resultSet.getString("islandRole")));
        }catch(Exception ex){
            playerRole = SPlayerRole.of(resultSet.getString("islandRole"));
        }

        return new SSuperiorPlayer(new PlayerAttributes()
                .setValue(PlayerAttributes.Field.UUID, UUID.fromString(resultSet.getString("player")))
                .setValue(PlayerAttributes.Field.ISLAND_LEADER, UUID.fromString(resultSet.getString("teamLeader")))
                .setValue(PlayerAttributes.Field.LAST_USED_NAME, resultSet.getString("name"))
                .setValue(PlayerAttributes.Field.LAST_USED_SKIN, resultSet.getString("textureValue"))
                .setValue(PlayerAttributes.Field.ISLAND_ROLE, playerRole)
                .setValue(PlayerAttributes.Field.DISBANDS, resultSet.getInt("disbands"))
                .setValue(PlayerAttributes.Field.LAST_TIME_UPDATED, resultSet.getLong("lastTimeStatus"))
                .setValue(PlayerAttributes.Field.COMPLETED_MISSIONS, deserializeMissions(resultSet.getString("missions")))
                .setValue(PlayerAttributes.Field.TOGGLED_PANEL, resultSet.getBoolean("toggledPanel"))
                .setValue(PlayerAttributes.Field.ISLAND_FLY, resultSet.getBoolean("islandFly"))
                .setValue(PlayerAttributes.Field.BORDER_COLOR, BorderColor.valueOf(resultSet.getString("borderColor")))
                .setValue(PlayerAttributes.Field.LANGUAGE, LocaleUtils.getLocale(resultSet.getString("language")))
                .setValue(PlayerAttributes.Field.TOGGLED_BORDER, resultSet.getBoolean("toggledBorder"))
        );
    }

    private Island loadIsland(ResultSet resultSet) throws SQLException {
        UUID ownerUUID = UUID.fromString(resultSet.getString("owner"));
        UUID islandUUID;

        String uuidRaw = resultSet.getString("uuid");
        if (uuidRaw == null || uuidRaw.isEmpty()) {
            islandUUID = ownerUUID;
        } else {
            islandUUID = UUID.fromString(uuidRaw);
        }

        int generatedSchematics = 0;
        String generatedSchematicsRaw = resultSet.getString("generatedSchematics");
        try {
            generatedSchematics = Integer.parseInt(generatedSchematicsRaw);
        } catch (Exception ex) {
            if (generatedSchematicsRaw.contains("normal"))
                generatedSchematics |= 8;
            if (generatedSchematicsRaw.contains("nether"))
                generatedSchematics |= 4;
            if (generatedSchematicsRaw.contains("the_end"))
                generatedSchematics |= 3;
        }

        int unlockedWorlds = 0;
        String unlockedWorldsRaw = resultSet.getString("unlockedWorlds");
        try {
            unlockedWorlds = Integer.parseInt(unlockedWorldsRaw);
        } catch (Exception ex) {
            if (unlockedWorldsRaw.contains("nether"))
                unlockedWorlds |= 1;
            if (unlockedWorldsRaw.contains("the_end"))
                unlockedWorlds |= 2;
        }

        return new SIsland(new IslandAttributes()
                .setValue(IslandAttributes.Field.UUID, islandUUID)
                .setValue(IslandAttributes.Field.OWNER, getSuperiorPlayer(ownerUUID))
                .setValue(IslandAttributes.Field.CENTER, SBlockPosition.of(Objects.requireNonNull(
                        LocationUtils.getLocation(resultSet.getString("center")))))
                .setValue(IslandAttributes.Field.CREATION_TIME, resultSet.getLong("creationTime"))
                .setValue(IslandAttributes.Field.ISLAND_TYPE, resultSet.getString("schemName"))
                .setValue(IslandAttributes.Field.DISCORD, resultSet.getString("discord"))
                .setValue(IslandAttributes.Field.PAYPAL, resultSet.getString("paypal"))
                .setValue(IslandAttributes.Field.WORTH_BONUS, new BigDecimal(resultSet.getString("bonusWorth")))
                .setValue(IslandAttributes.Field.LEVELS_BONUS, new BigDecimal(resultSet.getString("bonusLevel")))
                .setValue(IslandAttributes.Field.LOCKED, resultSet.getBoolean("locked"))
                .setValue(IslandAttributes.Field.IGNORED, resultSet.getBoolean("ignored"))
                .setValue(IslandAttributes.Field.NAME, resultSet.getString("name"))
                .setValue(IslandAttributes.Field.DESCRIPTION, resultSet.getString("description"))
                .setValue(IslandAttributes.Field.GENERATED_SCHEMATICS, generatedSchematics)
                .setValue(IslandAttributes.Field.UNLOCKED_WORLDS, unlockedWorlds)
                .setValue(IslandAttributes.Field.LAST_TIME_UPDATED, resultSet.getLong("lastTimeUpdate"))
                .setValue(IslandAttributes.Field.DIRTY_CHUNKS, resultSet.getString("dirtyChunks"))
                .setValue(IslandAttributes.Field.BLOCK_COUNTS, resultSet.getString("blockCounts"))
                .setValue(IslandAttributes.Field.HOMES, deserializeHomes(resultSet.getString("teleportLocation")))
                .setValue(IslandAttributes.Field.MEMBERS, deserializePlayers(resultSet.getString("members")))
                .setValue(IslandAttributes.Field.BANS, deserializePlayers(resultSet.getString("banned")))
                .setValue(IslandAttributes.Field.PLAYER_PERMISSIONS, deserializePlayerPerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.ROLE_PERMISSIONS, deserializeRolePerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.UPGRADES, deserializeUpgrades(resultSet.getString("upgrades")))
                .setValue(IslandAttributes.Field.WARPS, deserializeWarps(resultSet.getString("warps")))
                .setValue(IslandAttributes.Field.BLOCK_LIMITS, deserializeBlockLimits(resultSet.getString("blockLimits")))
                .setValue(IslandAttributes.Field.RATINGS, deserializeRatings(resultSet.getString("ratings")))
                .setValue(IslandAttributes.Field.MISSIONS, deserializeMissions(resultSet.getString("missions")))
                .setValue(IslandAttributes.Field.ISLAND_FLAGS, deserializeIslandFlags(resultSet.getString("settings")))
                .setValue(IslandAttributes.Field.GENERATORS, deserializeGenerators(resultSet.getString("generator")))
                .setValue(IslandAttributes.Field.VISITORS, deserializeVisitors(resultSet.getString("uniqueVisitors")))
                .setValue(IslandAttributes.Field.ENTITY_LIMITS, deserializeEntityLimits(resultSet.getString("entityLimits")))
                .setValue(IslandAttributes.Field.EFFECTS, deserializeEffects(resultSet.getString("islandEffects")))
                .setValue(IslandAttributes.Field.ISLAND_CHESTS, deserializeIslandChests(resultSet.getString("islandChest")))
                .setValue(IslandAttributes.Field.ROLE_LIMITS, deserializeRoleLimits(resultSet.getString("roleLimits")))
                .setValue(IslandAttributes.Field.WARP_CATEGORIES, deserializeWarpCategories(resultSet.getString("warpCategories")))
                .setValue(IslandAttributes.Field.BANK_BALANCE, new BigDecimal(resultSet.getString("islandBank")))
                .setValue(IslandAttributes.Field.BANK_LAST_INTEREST, resultSet.getLong("lastInterest"))
                .setValue(IslandAttributes.Field.VISITOR_HOMES, new Location[] { LocationUtils.getLocation(resultSet.getString("visitorsLocation")) })
                .setValue(IslandAttributes.Field.ISLAND_SIZE, new UpgradeValue<>(resultSet.getInt("islandSize"), i -> i < 0))
                .setValue(IslandAttributes.Field.TEAM_LIMIT, new UpgradeValue<>(resultSet.getInt("teamLimit"), i -> i < 0))
                .setValue(IslandAttributes.Field.WARPS_LIMIT, new UpgradeValue<>(resultSet.getInt("warpsLimit"), i -> i < 0))
                .setValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER, new UpgradeValue<>(resultSet.getDouble("cropGrowth"), i -> i < 0))
                .setValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER, new UpgradeValue<>(resultSet.getDouble("spawnerRates"), i -> i < 0))
                .setValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER, new UpgradeValue<>(resultSet.getDouble("mobDrops"), i -> i < 0))
                .setValue(IslandAttributes.Field.COOP_LIMIT, new UpgradeValue<>(resultSet.getInt("coopLimit"), i -> i < 0))
                .setValue(IslandAttributes.Field.BANK_LIMIT, new UpgradeValue<>(new BigDecimal(resultSet.getString("bankLimit")),
                        i -> i.compareTo(new BigDecimal(-1)) < 0))
        );
    }

    private SuperiorPlayer getSuperiorPlayer(UUID uuid){
        return loadedPlayers.stream()
                .filter(superiorPlayer -> superiorPlayer.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    private static Map<Mission<?>, Integer> deserializeMissions(String missions){
        Map<Mission<?>, Integer> completedMissions = new HashMap<>();

        JsonArray missionsArray = gson.fromJson(missions, JsonArray.class);
        missionsArray.forEach(missionElement -> {
            JsonObject missionObject = missionElement.getAsJsonObject();

            String name = missionObject.get("name").getAsString();
            int finishCount = missionObject.get("finishCount").getAsInt();

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                completedMissions.put(mission, finishCount);
        });

        return completedMissions;
    }

    private Location[] deserializeHomes(String locationParam){
        Location[] locations = new Location[World.Environment.values().length];

        JsonArray locationsArray = gson.fromJson(locationParam, JsonArray.class);
        locationsArray.forEach(locationElement -> {
            JsonObject locationObject = locationElement.getAsJsonObject();
            try {
                int i = World.Environment.valueOf(locationObject.get("env").getAsString()).ordinal();
                locations[i] = FileUtils.toLocation(locationObject.get("location").getAsString());
            } catch (Exception ignored) {
            }
        });

        return locations;
    }

    private List<SuperiorPlayer> deserializePlayers(String players) {
        List<SuperiorPlayer> superiorPlayers = new ArrayList<>();
        JsonArray playersArray = gson.fromJson(players, JsonArray.class);
        playersArray.forEach(uuid -> {
            UUID playerUUID = UUID.fromString(uuid.getAsString());
            superiorPlayers.add(getSuperiorPlayer(playerUUID));
        });
        return superiorPlayers;
    }

    private Map<SuperiorPlayer, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes) {
        Map<SuperiorPlayer, PlayerPermissionNode> playerPermissions = new HashMap<>();

        JsonObject globalObject = gson.fromJson(permissionNodes, JsonObject.class);
        JsonArray playersArray = globalObject.getAsJsonArray("players");

        playersArray.forEach(playerElement -> {
            JsonObject playerObject = playerElement.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                SuperiorPlayer superiorPlayer = getSuperiorPlayer(uuid);
                JsonArray permsArray = playerObject.getAsJsonArray("permissions");
                PlayerPermissionNode playerPermissionNode = new PlayerPermissionNode(superiorPlayer, null, "");
                playerPermissions.put(superiorPlayer, playerPermissionNode);

                for(JsonElement permElement : permsArray){
                    try {
                        JsonObject permObject = permElement.getAsJsonObject();
                        IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permObject.get("name").getAsString());
                        playerPermissionNode.setPermission(islandPrivilege, permObject.get("status").getAsString().equals("1"));
                    }catch (Exception ignored){}
                }
            } catch (Exception ignored) {
            }
        });

        return playerPermissions;
    }

    private Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes){
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

    private Map<String, Integer> deserializeUpgrades(String upgrades){
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

    private List<IslandWarp> deserializeWarps(String islandWarps){
        List<IslandWarp> islandWarpList = new ArrayList<>();

        JsonArray warpsArray = gson.fromJson(islandWarps, JsonArray.class);
        warpsArray.forEach(warpElement -> {
            JsonObject warpObject = warpElement.getAsJsonObject();
            String name = IslandUtils.getWarpName(warpObject.get("name").getAsString());

            if (name.isEmpty())
                return;

            WarpCategory warpCategory = null;
            if (warpObject.has("category")) {
                warpCategory = new SWarpCategory(null, warpObject.get("category").getAsString());
                loadedWarpCategories.put(warpCategory.getName().toLowerCase(), warpCategory);
            }

            Location location = FileUtils.toLocation(warpObject.get("location").getAsString());
            boolean privateWarp = warpObject.get("private").getAsInt() == 1;

            IslandWarp islandWarp = new SIslandWarp(name, location, warpCategory);
            islandWarp.setPrivateFlag(privateWarp);

            if (warpObject.has("icon"))
                islandWarp.setIcon(ItemUtils.deserializeItem(warpObject.get("icon").getAsString()));

            islandWarpList.add(islandWarp);
        });

        return islandWarpList;
    }

    private KeyMap<UpgradeValue<Integer>> deserializeBlockLimits(String blocks){
        KeyMap<UpgradeValue<Integer>> blockLimits = new KeyMap<>();

        JsonArray blockLimitsArray = gson.fromJson(blocks, JsonArray.class);
        blockLimitsArray.forEach(blockLimitElement -> {
            JsonObject blockLimitObject = blockLimitElement.getAsJsonObject();
            Key blockKey = Key.of(blockLimitObject.get("id").getAsString());
            int limit = blockLimitObject.get("limit").getAsInt();
            blockLimits.put(blockKey, new UpgradeValue<>(limit, i -> i < 0));
        });

        return blockLimits;
    }

    private Map<UUID, Rating> deserializeRatings(String ratings){
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

    private Map<IslandFlag, Byte> deserializeIslandFlags(String settings){
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

    private KeyMap<UpgradeValue<Integer>>[] deserializeGenerators(String generator){
        // noinspection all
        KeyMap<UpgradeValue<Integer>>[] cobbleGenerator = new KeyMap[World.Environment.values().length];

        JsonArray generatorWorldsArray = gson.fromJson(generator, JsonArray.class);
        generatorWorldsArray.forEach(generatorWorldElement -> {
            JsonObject generatorWorldObject = generatorWorldElement.getAsJsonObject();
            try {
                int i = World.Environment.valueOf(generatorWorldObject.get("env").getAsString()).ordinal();
                generatorWorldObject.getAsJsonArray("rates").forEach(generatorElement -> {
                    JsonObject generatorObject = generatorElement.getAsJsonObject();
                    Key blockKey = Key.of(generatorObject.get("id").getAsString());
                    int rate = generatorObject.get("rate").getAsInt();
                    (cobbleGenerator[i] = new KeyMap<>()).put(blockKey, new UpgradeValue<>(rate, n -> n < 0));
                });
            } catch (Exception ignored) {
            }
        });

        return cobbleGenerator;
    }

    private List<Pair<SuperiorPlayer, Long>> deserializeVisitors(String visitors){
        List<Pair<SuperiorPlayer, Long>> visitorsList = new ArrayList<>();

        JsonArray playersArray = gson.fromJson(visitors, JsonArray.class);

        playersArray.forEach(playerElement -> {
            JsonObject playerObject = playerElement.getAsJsonObject();
            try {
                UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                long lastTimeRecorded = playerObject.get("lastTimeRecorded").getAsLong();
                visitorsList.add(new Pair<>(plugin.getPlayers().getSuperiorPlayer(uuid), lastTimeRecorded));
            } catch (Exception ignored) {
            }
        });

        return visitorsList;
    }

    private KeyMap<UpgradeValue<Integer>> deserializeEntityLimits(String entities){
        KeyMap<UpgradeValue<Integer>> entityLimits = new KeyMap<>();

        JsonArray entityLimitsArray = gson.fromJson(entities, JsonArray.class);
        entityLimitsArray.forEach(entityLimitElement -> {
            JsonObject entityLimitObject = entityLimitElement.getAsJsonObject();
            Key entity = Key.of(entityLimitObject.get("id").getAsString());
            int limit = entityLimitObject.get("limit").getAsInt();
            entityLimits.put(entity, new UpgradeValue<>(limit, i -> i < 0));
        });

        return entityLimits;
    }

    private Map<PotionEffectType, UpgradeValue<Integer>> deserializeEffects(String effects){
        Map<PotionEffectType, UpgradeValue<Integer>> islandEffects = new HashMap<>();

        JsonArray effectsArray = gson.fromJson(effects, JsonArray.class);
        effectsArray.forEach(effectElement -> {
            JsonObject effectObject = effectElement.getAsJsonObject();
            PotionEffectType potionEffectType = PotionEffectType.getByName(effectObject.get("type").getAsString());
            if (potionEffectType != null) {
                int level = effectObject.get("level").getAsInt();
                islandEffects.put(potionEffectType, new UpgradeValue<>(level, i -> i < 0));
            }
        });

        return islandEffects;
    }

    private IslandChest[] deserializeIslandChests(String islandChest){
        JsonArray islandChestsArray = gson.fromJson(islandChest, JsonArray.class);
        List<IslandChest> islandChestList = new ArrayList<>();

        islandChestsArray.forEach(islandChestElement -> {
            JsonObject islandChestObject = islandChestElement.getAsJsonObject();
            int i = islandChestObject.get("index").getAsInt();
            String contents = islandChestObject.get("contents").getAsString();

            if (i >= islandChestList.size()) {
                islandChestList.add(SIslandChest.createChest(null, i, ItemUtils.deserialize(contents)));
            } else
                islandChestList.add(i, SIslandChest.createChest(null, i, ItemUtils.deserialize(contents)));

        });

        return islandChestList.toArray(new IslandChest[0]);
    }

    private Map<PlayerRole, UpgradeValue<Integer>> deserializeRoleLimits(String roles){
        Map<PlayerRole, UpgradeValue<Integer>> roleLimits = new HashMap<>();

        JsonArray roleLimitsArray = gson.fromJson(roles, JsonArray.class);
        roleLimitsArray.forEach(roleElement -> {
            JsonObject roleObject = roleElement.getAsJsonObject();
            PlayerRole playerRole = SPlayerRole.fromId(roleObject.get("id").getAsInt());
            if (playerRole != null) {
                int limit = roleObject.get("limit").getAsInt();
                roleLimits.put(playerRole, new UpgradeValue<>(limit, i -> i < 0));
            }
        });

        return roleLimits;
    }

    private Map<String, WarpCategory> deserializeWarpCategories(String categories){
        Map<String, WarpCategory> warpCategories = new HashMap<>();

        JsonArray warpCategoriesArray = gson.fromJson(categories, JsonArray.class);
        warpCategoriesArray.forEach(warpCategoryElement -> {
            JsonObject warpCategoryObject = warpCategoryElement.getAsJsonObject();
            String name = StringUtils.stripColors(warpCategoryObject.get("name").getAsString());

            WarpCategory warpCategory = loadedWarpCategories.get(name.toLowerCase());

            if (warpCategory != null) {
                if (warpCategory.getWarps().isEmpty()) {
                    return;
                }

                int slot = warpCategoryObject.get("slot").getAsInt();
                warpCategory.setSlot(slot);

                ItemStack icon = ItemUtils.deserializeItem(warpCategoryObject.get("icon").getAsString());
                if (icon != null)
                    warpCategory.setIcon(icon);

                warpCategories.put(name.toLowerCase(), warpCategory);
            }
        });

        return warpCategories;
    }

}
