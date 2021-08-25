package com.bgsoftware.superiorskyblock.data.loaders;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.data.sql.SQLSession;
import com.bgsoftware.superiorskyblock.data.sql.StatementHolder;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.attributes.IslandAttributes;
import com.bgsoftware.superiorskyblock.utils.attributes.IslandChestAttributes;
import com.bgsoftware.superiorskyblock.utils.attributes.IslandWarpAttributes;
import com.bgsoftware.superiorskyblock.utils.attributes.PlayerAttributes;
import com.bgsoftware.superiorskyblock.utils.attributes.WarpCategoryAttributes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class DatabaseLoader_V1 implements DatabaseLoader {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final Gson gson = new Gson();

    private static File databaseFile;
    private static SQLSession sqlSession;

    private final List<PlayerAttributes> loadedPlayers = new ArrayList<>();
    private final List<IslandAttributes> loadedIslands = new ArrayList<>();

    private final IDeserializer deserializer = new MultipleDeserializer(new JsonDeserializer(), new RawDeserializer());

    @Override
    public void loadData() {
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

        if (sqlSession.isUsingMySQL()) {
            sqlSession.executeUpdate("RENAME TABLE {prefix}islands TO {prefix}islands_bkp", failure -> failedBackup.set(true));
            sqlSession.executeUpdate("RENAME TABLE {prefix}players TO {prefix}players_bkp", failure -> failedBackup.set(true));
        } else {
            if (!databaseFile.renameTo(new File(databaseFile.getParentFile(), "database-bkp.db"))) {
                failedBackup.set(true);
            }
        }

        if (failedBackup.get()) {
            SuperiorSkyblockPlugin.log("&c[Database-Converter] Failed to create a backup for the database file.");
        } else {
            SuperiorSkyblockPlugin.log("&a[Database-Converter] Successfully created a backup for the database.");
        }
    }

    @Override
    public void saveData() {
        long currentTime = System.currentTimeMillis();

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting players...");

        StatementHolder playersQuery = new StatementHolder("INSERT INTO {prefix}players VALUES(?,?,?,?,?)");
        StatementHolder playersMissionsQuery = new StatementHolder("INSERT INTO {prefix}players_missions VALUES(?,?,?)");
        StatementHolder playersSettingsQuery = new StatementHolder("INSERT INTO {prefix}players_settings VALUES(?,?,?,?,?,?)");

        for (PlayerAttributes playerAttributes : loadedPlayers) {
            insertPlayer(playerAttributes, playersQuery, playersMissionsQuery, playersSettingsQuery);
        }

        playersQuery.executeBatch(false);
        playersMissionsQuery.executeBatch(false);
        playersSettingsQuery.executeBatch(false);

        SuperiorSkyblockPlugin.log("&a[Database-Converter] Converting islands...");

        StatementHolder islandsQuery = new StatementHolder("INSERT INTO {prefix}islands VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        StatementHolder islandsBanksQuery = new StatementHolder("INSERT INTO {prefix}islands_banks VALUES(?,?,?)");
        StatementHolder islandsBansQuery = new StatementHolder("INSERT INTO {prefix}islands_bans VALUES(?,?,?,?)");
        StatementHolder islandsBlockLimitsQuery = new StatementHolder("INSERT INTO {prefix}islands_block_limits VALUES(?,?,?)");
        StatementHolder islandsChestsQuery = new StatementHolder("INSERT INTO {prefix}islands_chests VALUES(?,?,?)");
        StatementHolder islandsEffectsQuery = new StatementHolder("INSERT INTO {prefix}islands_effects VALUES(?,?,?)");
        StatementHolder islandsEntityLimitsQuery = new StatementHolder("INSERT INTO {prefix}islands_entity_limits VALUES(?,?,?)");
        StatementHolder islandsFlagsQuery = new StatementHolder("INSERT INTO {prefix}islands_flags VALUES(?,?,?)");
        StatementHolder islandsGeneratorsQuery = new StatementHolder("INSERT INTO {prefix}islands_generators VALUES(?,?,?,?)");
        StatementHolder islandsHomesQuery = new StatementHolder("INSERT INTO {prefix}islands_homes VALUES(?,?,?)");
        StatementHolder islandsMembersQuery = new StatementHolder("INSERT INTO {prefix}islands_members VALUES(?,?,?,?)");
        StatementHolder islandsMissionsQuery = new StatementHolder("INSERT INTO {prefix}islands_missions VALUES(?,?,?)");
        StatementHolder islandsPlayerPermissionsQuery = new StatementHolder("INSERT INTO {prefix}islands_player_permissions VALUES(?,?,?,?)");
        StatementHolder islandsRatingsQuery = new StatementHolder("INSERT INTO {prefix}islands_ratings VALUES(?,?,?,?)");
        StatementHolder islandsRoleLimitsQuery = new StatementHolder("INSERT INTO {prefix}islands_role_limits VALUES(?,?,?)");
        StatementHolder islandsRolePermissionsQuery = new StatementHolder("INSERT INTO {prefix}islands_role_permissions VALUES(?,?,?)");
        StatementHolder islandsSettingsQuery = new StatementHolder("INSERT INTO {prefix}islands_settings VALUES(?,?,?,?,?,?,?,?,?)");
        StatementHolder islandsUpgradesQuery = new StatementHolder("INSERT INTO {prefix}islands_upgrades VALUES(?,?,?)");
        StatementHolder islandsVisitorHomesQuery = new StatementHolder("INSERT INTO {prefix}islands_visitor_homes VALUES(?,?,?)");
        StatementHolder islandsVisitorsQuery = new StatementHolder("INSERT INTO {prefix}islands_visitors VALUES(?,?,?)");
        StatementHolder islandsWarpCategoriesQuery = new StatementHolder("INSERT INTO {prefix}islands_warp_categories VALUES(?,?,?,?)");
        StatementHolder islandsWarpsQuery = new StatementHolder("INSERT INTO {prefix}islands_warps VALUES(?,?,?,?,?,?)");

        for (IslandAttributes islandAttributes : loadedIslands) {
            insertIsland(islandAttributes, currentTime, islandsQuery, islandsBanksQuery, islandsBansQuery,
                    islandsBlockLimitsQuery, islandsChestsQuery, islandsEffectsQuery, islandsEntityLimitsQuery,
                    islandsFlagsQuery, islandsGeneratorsQuery, islandsHomesQuery, islandsMembersQuery,
                    islandsMissionsQuery, islandsPlayerPermissionsQuery, islandsRatingsQuery, islandsRoleLimitsQuery,
                    islandsRolePermissionsQuery, islandsSettingsQuery, islandsUpgradesQuery, islandsVisitorHomesQuery,
                    islandsVisitorsQuery, islandsWarpCategoriesQuery, islandsWarpsQuery);
        }

        islandsQuery.executeBatch(false);
        islandsBanksQuery.executeBatch(false);
        islandsBansQuery.executeBatch(false);
        islandsBlockLimitsQuery.executeBatch(false);
        islandsChestsQuery.executeBatch(false);
        islandsEffectsQuery.executeBatch(false);
        islandsEntityLimitsQuery.executeBatch(false);
        islandsFlagsQuery.executeBatch(false);
        islandsGeneratorsQuery.executeBatch(false);
        islandsHomesQuery.executeBatch(false);
        islandsMembersQuery.executeBatch(false);
        islandsMissionsQuery.executeBatch(false);
        islandsPlayerPermissionsQuery.executeBatch(false);
        islandsRatingsQuery.executeBatch(false);
        islandsRoleLimitsQuery.executeBatch(false);
        islandsRolePermissionsQuery.executeBatch(false);
        islandsSettingsQuery.executeBatch(false);
        islandsUpgradesQuery.executeBatch(false);
        islandsVisitorHomesQuery.executeBatch(false);
        islandsVisitorsQuery.executeBatch(false);
        islandsWarpCategoriesQuery.executeBatch(false);
        islandsWarpsQuery.executeBatch(false);
    }

    public static void register() {
        if (isDatabaseOldFormat())
            plugin.getDataHandler().addDatabaseLoader(new DatabaseLoader_V1());
    }

    private static boolean isDatabaseOldFormat() {
        sqlSession = new SQLSession();

        if (!sqlSession.isUsingMySQL()) {
            databaseFile = new File("plugins", "SuperiorSkyblock2\\database.db");

            if (!databaseFile.exists())
                return false;
        }

        if (!sqlSession.createConnection(SuperiorSkyblockPlugin.getPlugin(), false)) {
            sqlSession.close();
            return false;
        }

        if (sqlSession.doesTableExist("islands_members")) {
            sqlSession.close();
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private void insertPlayer(PlayerAttributes playerAttributes,
                              StatementHolder playersQuery,
                              StatementHolder playersMissionsQuery,
                              StatementHolder playersSettingsQuery) {
        UUID playerUUID = playerAttributes.getValue(PlayerAttributes.Field.UUID);
        playersQuery.setObject(playerUUID)
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_NAME))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_USED_SKIN))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.DISBANDS))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LAST_TIME_UPDATED))
                .addBatch();
        ((Map<String, Integer>) playerAttributes.getValue(PlayerAttributes.Field.COMPLETED_MISSIONS)).forEach((missionName, finishCount) ->
                playersMissionsQuery.setObject(playerUUID)
                        .setObject(missionName.toLowerCase())
                        .setObject(finishCount)
                        .addBatch());
        playersSettingsQuery.setObject(playerUUID)
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.LANGUAGE))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_PANEL))
                .setObject(((BorderColor) playerAttributes.getValue(PlayerAttributes.Field.BORDER_COLOR)).name())
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.TOGGLED_BORDER))
                .setObject(playerAttributes.getValue(PlayerAttributes.Field.ISLAND_FLY))
                .addBatch();
    }

    @SuppressWarnings({"unchecked"})
    private void insertIsland(IslandAttributes islandAttributes, long currentTime,
                              StatementHolder islandsQuery, StatementHolder islandsBanksQuery,
                              StatementHolder islandsBansQuery, StatementHolder islandsBlockLimitsQuery,
                              StatementHolder islandsChestsQuery, StatementHolder islandsEffectsQuery,
                              StatementHolder islandsEntityLimitsQuery, StatementHolder islandsFlagsQuery,
                              StatementHolder islandsGeneratorsQuery, StatementHolder islandsHomesQuery,
                              StatementHolder islandsMembersQuery, StatementHolder islandsMissionsQuery,
                              StatementHolder islandsPlayerPermissionsQuery, StatementHolder islandsRatingsQuery,
                              StatementHolder islandsRoleLimitsQuery, StatementHolder islandsRolePermissionsQuery,
                              StatementHolder islandsSettingsQuery, StatementHolder islandsUpgradesQuery,
                              StatementHolder islandsVisitorHomesQuery, StatementHolder islandsVisitorsQuery,
                              StatementHolder islandsWarpCategoriesQuery, StatementHolder islandsWarpsQuery) {
        UUID islandUUID = islandAttributes.getValue(IslandAttributes.Field.UUID);
        islandsQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.OWNER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CENTER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CREATION_TIME))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_TYPE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DISCORD))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.PAYPAL))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.WORTH_BONUS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LEVELS_BONUS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LOCKED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.IGNORED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.NAME))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DESCRIPTION))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.GENERATED_SCHEMATICS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.UNLOCKED_WORLDS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.LAST_TIME_UPDATED))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.DIRTY_CHUNKS))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BLOCK_COUNTS))
                .addBatch();
        islandsBanksQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_BALANCE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LAST_INTEREST))
                .addBatch();
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.BANS)).forEach(playerAttributes ->
                islandsBansQuery.setObject(islandUUID)
                        .setObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .setObject(CONSOLE_UUID)
                        .setObject(currentTime)
                        .addBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.BLOCK_LIMITS)).forEach((key, limit) ->
                islandsBlockLimitsQuery.setObject(islandUUID)
                        .setObject(key)
                        .setObject(limit)
                        .addBatch());
        ((List<IslandChestAttributes>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_CHESTS)).forEach(islandChestAttributes ->
                islandsChestsQuery.setObject(islandUUID)
                        .setObject(islandChestAttributes.getValue(IslandChestAttributes.Field.INDEX))
                        .setObject(islandChestAttributes.getValue(IslandChestAttributes.Field.CONTENTS))
                        .addBatch());
        ((Map<PotionEffectType, Integer>) islandAttributes.getValue(IslandAttributes.Field.EFFECTS)).forEach((type, level) ->
                islandsEffectsQuery.setObject(islandUUID)
                        .setObject(type.getName())
                        .setObject(level)
                        .addBatch());
        ((KeyMap<Integer>) islandAttributes.getValue(IslandAttributes.Field.ENTITY_LIMITS)).forEach((entity, limit) ->
                islandsEntityLimitsQuery.setObject(islandUUID)
                        .setObject(entity)
                        .setObject(limit)
                        .addBatch());
        ((Map<IslandFlag, Byte>) islandAttributes.getValue(IslandAttributes.Field.ISLAND_FLAGS)).forEach((islandFlag, status) ->
                islandsFlagsQuery.setObject(islandUUID)
                        .setObject(islandFlag.getName())
                        .setObject(status)
                        .addBatch());
        runOnEnvironments((KeyMap<Integer>[]) islandAttributes.getValue(IslandAttributes.Field.GENERATORS), (generatorRates, environment) ->
                generatorRates.forEach((block, rate) ->
                        islandsGeneratorsQuery.setObject(islandUUID)
                                .setObject(environment.name())
                                .setObject(block)
                                .setObject(rate)
                                .addBatch()));
        runOnEnvironments((String[]) islandAttributes.getValue(IslandAttributes.Field.HOMES), (islandHome, environment) ->
                islandsHomesQuery.setObject(islandUUID)
                        .setObject(environment.name())
                        .setObject(islandHome)
                        .addBatch());
        ((List<PlayerAttributes>) islandAttributes.getValue(IslandAttributes.Field.MEMBERS)).forEach(playerAttributes ->
                islandsMembersQuery.setObject(islandUUID)
                        .setObject(playerAttributes.getValue(PlayerAttributes.Field.UUID))
                        .setObject(((PlayerRole) playerAttributes.getValue(PlayerAttributes.Field.ISLAND_ROLE)).getId())
                        .setObject(currentTime)
                        .addBatch());
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.MISSIONS)).forEach((mission, finishCount) ->
                islandsMissionsQuery.setObject(islandUUID)
                        .setObject(mission)
                        .setObject(finishCount)
                        .addBatch());
        ((Map<UUID, PlayerPermissionNode>) islandAttributes.getValue(IslandAttributes.Field.PLAYER_PERMISSIONS)).forEach((playerUUID, node) -> {
            for (Map.Entry<IslandPrivilege, Boolean> playerPermission : node.getCustomPermissions().entrySet())
                islandsPlayerPermissionsQuery.setObject(islandUUID)
                        .setObject(playerUUID)
                        .setObject(playerPermission.getKey().getName())
                        .setObject(playerPermission.getValue())
                        .addBatch();
        });
        ((Map<UUID, Rating>) islandAttributes.getValue(IslandAttributes.Field.RATINGS)).forEach((playerUUID, rating) ->
                islandsRatingsQuery.setObject(islandUUID)
                        .setObject(playerUUID)
                        .setObject(rating.getValue())
                        .setObject(currentTime)
                        .addBatch());
        ((Map<PlayerRole, Integer>) islandAttributes.getValue(IslandAttributes.Field.ROLE_LIMITS)).forEach((role, limit) ->
                islandsRoleLimitsQuery.setObject(islandUUID)
                        .setObject(role.getId())
                        .setObject(limit)
                        .addBatch());
        ((Map<IslandPrivilege, PlayerRole>) islandAttributes.getValue(IslandAttributes.Field.ROLE_PERMISSIONS)).forEach((privilege, role) ->
                islandsRolePermissionsQuery.setObject(islandUUID)
                        .setObject(role.getId())
                        .setObject(privilege.getName())
                        .addBatch());
        islandsSettingsQuery.setObject(islandUUID)
                .setObject(islandAttributes.getValue(IslandAttributes.Field.ISLAND_SIZE))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.BANK_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.COOP_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.TEAM_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.WARPS_LIMIT))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER))
                .setObject(islandAttributes.getValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER))
                .addBatch();
        ((Map<String, Integer>) islandAttributes.getValue(IslandAttributes.Field.UPGRADES)).forEach((upgradeName, level) ->
                islandsUpgradesQuery.setObject(islandUUID)
                        .setObject(upgradeName)
                        .setObject(level)
                        .addBatch());
        String visitorHome = islandAttributes.getValue(IslandAttributes.Field.VISITOR_HOMES);
        if (visitorHome != null && !visitorHome.isEmpty())
            islandsVisitorHomesQuery.setObject(islandUUID)
                    .setObject(World.Environment.NORMAL.name())
                    .setObject(visitorHome)
                    .addBatch();
        ((List<Pair<UUID, Long>>) islandAttributes.getValue(IslandAttributes.Field.VISITORS)).forEach(visitor ->
                islandsVisitorsQuery.setObject(islandUUID)
                        .setObject(visitor.getKey().toString())
                        .setObject(visitor.getValue())
                        .addBatch());
        ((List<WarpCategoryAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARP_CATEGORIES)).forEach(warpCategoryAttributes ->
                islandsWarpCategoriesQuery.setObject(islandUUID)
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.NAME))
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.SLOT))
                        .setObject(warpCategoryAttributes.getValue(WarpCategoryAttributes.Field.ICON))
                        .addBatch());
        ((List<IslandWarpAttributes>) islandAttributes.getValue(IslandAttributes.Field.WARPS)).forEach(islandWarpAttributes ->
                islandsWarpsQuery.setObject(islandUUID)
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.NAME))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.CATEGORY))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.LOCATION))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.PRIVATE_STATUS))
                        .setObject(islandWarpAttributes.getValue(IslandWarpAttributes.Field.ICON))
                        .addBatch());
    }

    private <T> void runOnEnvironments(T[] arr, BiConsumer<T, World.Environment> consumer) {
        for (World.Environment environment : World.Environment.values()) {
            if (arr[environment.ordinal()] != null) {
                consumer.accept(arr[environment.ordinal()], environment);
            }
        }
    }

    private PlayerAttributes loadPlayer(ResultSet resultSet) throws SQLException {
        PlayerRole playerRole;

        try {
            playerRole = SPlayerRole.fromId(Integer.parseInt(resultSet.getString("islandRole")));
        } catch (Exception ex) {
            playerRole = SPlayerRole.of(resultSet.getString("islandRole"));
        }

        return new PlayerAttributes()
                .setValue(PlayerAttributes.Field.UUID, UUID.fromString(resultSet.getString("player")))
                .setValue(PlayerAttributes.Field.ISLAND_LEADER, UUID.fromString(resultSet.getString("teamLeader")))
                .setValue(PlayerAttributes.Field.LAST_USED_NAME, resultSet.getString("name"))
                .setValue(PlayerAttributes.Field.LAST_USED_SKIN, resultSet.getString("textureValue"))
                .setValue(PlayerAttributes.Field.ISLAND_ROLE, playerRole)
                .setValue(PlayerAttributes.Field.DISBANDS, resultSet.getInt("disbands"))
                .setValue(PlayerAttributes.Field.LAST_TIME_UPDATED, resultSet.getLong("lastTimeStatus"))
                .setValue(PlayerAttributes.Field.COMPLETED_MISSIONS, deserializer.deserializeMissions(resultSet.getString("missions")))
                .setValue(PlayerAttributes.Field.TOGGLED_PANEL, resultSet.getBoolean("toggledPanel"))
                .setValue(PlayerAttributes.Field.ISLAND_FLY, resultSet.getBoolean("islandFly"))
                .setValue(PlayerAttributes.Field.BORDER_COLOR, BorderColor.valueOf(resultSet.getString("borderColor")))
                .setValue(PlayerAttributes.Field.LANGUAGE, resultSet.getString("language"))
                .setValue(PlayerAttributes.Field.TOGGLED_BORDER, resultSet.getBoolean("toggledBorder")
                );
    }

    private IslandAttributes loadIsland(ResultSet resultSet) throws SQLException {
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

        return new IslandAttributes()
                .setValue(IslandAttributes.Field.UUID, islandUUID)
                .setValue(IslandAttributes.Field.OWNER, ownerUUID)
                .setValue(IslandAttributes.Field.CENTER, resultSet.getString("center"))
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
                .setValue(IslandAttributes.Field.HOMES, deserializer.deserializeHomes(resultSet.getString("teleportLocation")))
                .setValue(IslandAttributes.Field.MEMBERS, deserializer.deserializePlayers(resultSet.getString("members")))
                .setValue(IslandAttributes.Field.BANS, deserializer.deserializePlayers(resultSet.getString("banned")))
                .setValue(IslandAttributes.Field.PLAYER_PERMISSIONS, deserializer.deserializePlayerPerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.ROLE_PERMISSIONS, deserializer.deserializeRolePerms(resultSet.getString("permissionNodes")))
                .setValue(IslandAttributes.Field.UPGRADES, deserializer.deserializeUpgrades(resultSet.getString("upgrades")))
                .setValue(IslandAttributes.Field.WARPS, deserializer.deserializeWarps(resultSet.getString("warps")))
                .setValue(IslandAttributes.Field.BLOCK_LIMITS, deserializer.deserializeBlockLimits(resultSet.getString("blockLimits")))
                .setValue(IslandAttributes.Field.RATINGS, deserializer.deserializeRatings(resultSet.getString("ratings")))
                .setValue(IslandAttributes.Field.MISSIONS, deserializer.deserializeMissions(resultSet.getString("missions")))
                .setValue(IslandAttributes.Field.ISLAND_FLAGS, deserializer.deserializeIslandFlags(resultSet.getString("settings")))
                .setValue(IslandAttributes.Field.GENERATORS, deserializer.deserializeGenerators(resultSet.getString("generator")))
                .setValue(IslandAttributes.Field.VISITORS, deserializer.deserializeVisitors(resultSet.getString("uniqueVisitors")))
                .setValue(IslandAttributes.Field.ENTITY_LIMITS, deserializer.deserializeEntityLimits(resultSet.getString("entityLimits")))
                .setValue(IslandAttributes.Field.EFFECTS, deserializer.deserializeEffects(resultSet.getString("islandEffects")))
                .setValue(IslandAttributes.Field.ISLAND_CHESTS, deserializer.deserializeIslandChests(resultSet.getString("islandChest")))
                .setValue(IslandAttributes.Field.ROLE_LIMITS, deserializer.deserializeRoleLimits(resultSet.getString("roleLimits")))
                .setValue(IslandAttributes.Field.WARP_CATEGORIES, deserializer.deserializeWarpCategories(resultSet.getString("warpCategories")))
                .setValue(IslandAttributes.Field.BANK_BALANCE, new BigDecimal(resultSet.getString("islandBank")))
                .setValue(IslandAttributes.Field.BANK_LAST_INTEREST, resultSet.getLong("lastInterest"))
                .setValue(IslandAttributes.Field.VISITOR_HOMES, resultSet.getString("visitorsLocation"))
                .setValue(IslandAttributes.Field.ISLAND_SIZE, resultSet.getInt("islandSize"))
                .setValue(IslandAttributes.Field.TEAM_LIMIT, resultSet.getInt("teamLimit"))
                .setValue(IslandAttributes.Field.WARPS_LIMIT, resultSet.getInt("warpsLimit"))
                .setValue(IslandAttributes.Field.CROP_GROWTH_MULTIPLIER, resultSet.getDouble("cropGrowth"))
                .setValue(IslandAttributes.Field.SPAWNER_RATES_MULTIPLIER, resultSet.getDouble("spawnerRates"))
                .setValue(IslandAttributes.Field.MOB_DROPS_MULTIPLIER, resultSet.getDouble("mobDrops"))
                .setValue(IslandAttributes.Field.COOP_LIMIT, resultSet.getInt("coopLimit"))
                .setValue(IslandAttributes.Field.BANK_LIMIT, new BigDecimal(resultSet.getString("bankLimit")));
    }

    private PlayerAttributes getPlayerAttributes(UUID uuid) {
        return loadedPlayers.stream().filter(playerAttributes ->
                        playerAttributes.getValue(PlayerAttributes.Field.UUID).equals(uuid))
                .findFirst()
                .orElse(null);
    }

    interface IDeserializer {

        Map<String, Integer> deserializeMissions(String missions);

        String[] deserializeHomes(String locationParam);

        List<PlayerAttributes> deserializePlayers(String players);

        Map<UUID, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes);

        Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes);

        Map<String, Integer> deserializeUpgrades(String upgrades);

        List<IslandWarpAttributes> deserializeWarps(String islandWarps);

        KeyMap<Integer> deserializeBlockLimits(String blocks);

        Map<UUID, Rating> deserializeRatings(String ratings);

        Map<IslandFlag, Byte> deserializeIslandFlags(String settings);

        KeyMap<Integer>[] deserializeGenerators(String generator);

        List<Pair<UUID, Long>> deserializeVisitors(String visitors);

        KeyMap<Integer> deserializeEntityLimits(String entities);

        Map<PotionEffectType, Integer> deserializeEffects(String effects);

        List<IslandChestAttributes> deserializeIslandChests(String islandChest);

        Map<PlayerRole, Integer> deserializeRoleLimits(String roles);

        List<WarpCategoryAttributes> deserializeWarpCategories(String categories);

    }

    private static class MultipleDeserializer implements IDeserializer {

        private final List<IDeserializer> deserializers;

        public MultipleDeserializer(IDeserializer... deserializers){
            this.deserializers = Arrays.asList(deserializers);
        }

        private <T> T runDeserializers(Function<IDeserializer, T> function){
            for(IDeserializer deserializer : deserializers){
                try {
                    return function.apply(deserializer);
                } catch (Exception ignored) {}
            }

            throw new RuntimeException("No valid deserializer found.");
        }

        @Override
        public Map<String, Integer> deserializeMissions(String missions) {
            return runDeserializers(deserializer -> deserializer.deserializeMissions(missions));
        }

        @Override
        public String[] deserializeHomes(String locationParam) {
            return runDeserializers(deserializer -> deserializer.deserializeHomes(locationParam));
        }

        @Override
        public List<PlayerAttributes> deserializePlayers(String players) {
            return runDeserializers(deserializer -> deserializer.deserializePlayers(players));
        }

        @Override
        public Map<UUID, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes) {
            return runDeserializers(deserializer -> deserializer.deserializePlayerPerms(permissionNodes));
        }

        @Override
        public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
            return runDeserializers(deserializer -> deserializer.deserializeRolePerms(permissionNodes));
        }

        @Override
        public Map<String, Integer> deserializeUpgrades(String upgrades) {
            return runDeserializers(deserializer -> deserializer.deserializeUpgrades(upgrades));
        }

        @Override
        public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
            return runDeserializers(deserializer -> deserializer.deserializeWarps(islandWarps));
        }

        @Override
        public KeyMap<Integer> deserializeBlockLimits(String blocks) {
            return runDeserializers(deserializer -> deserializer.deserializeBlockLimits(blocks));
        }

        @Override
        public Map<UUID, Rating> deserializeRatings(String ratings) {
            return runDeserializers(deserializer -> deserializer.deserializeRatings(ratings));
        }

        @Override
        public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
            return runDeserializers(deserializer -> deserializer.deserializeIslandFlags(settings));
        }

        @Override
        public KeyMap<Integer>[] deserializeGenerators(String generator) {
            return runDeserializers(deserializer -> deserializer.deserializeGenerators(generator));
        }

        @Override
        public List<Pair<UUID, Long>> deserializeVisitors(String visitors) {
            return runDeserializers(deserializer -> deserializer.deserializeVisitors(visitors));
        }

        @Override
        public KeyMap<Integer> deserializeEntityLimits(String entities) {
            return runDeserializers(deserializer -> deserializer.deserializeEntityLimits(entities));
        }

        @Override
        public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
            return runDeserializers(deserializer -> deserializer.deserializeEffects(effects));
        }

        @Override
        public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
            return runDeserializers(deserializer -> deserializer.deserializeIslandChests(islandChest));
        }

        @Override
        public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
            return runDeserializers(deserializer -> deserializer.deserializeRoleLimits(roles));
        }

        @Override
        public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
            return runDeserializers(deserializer -> deserializer.deserializeWarpCategories(categories));
        }
    }

    private class JsonDeserializer implements IDeserializer {

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
            List<PlayerAttributes> playerAttributes = new ArrayList<>();
            JsonArray playersArray = gson.fromJson(players, JsonArray.class);
            playersArray.forEach(uuid -> playerAttributes.add(getPlayerAttributes(UUID.fromString(uuid.getAsString()))));
            return playerAttributes;
        }

        public Map<UUID, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes) {
            Map<UUID, PlayerPermissionNode> playerPermissions = new HashMap<>();

            JsonObject globalObject = gson.fromJson(permissionNodes, JsonObject.class);
            JsonArray playersArray = globalObject.getAsJsonArray("players");

            playersArray.forEach(playerElement -> {
                JsonObject playerObject = playerElement.getAsJsonObject();
                try {
                    UUID uuid = UUID.fromString(playerObject.get("uuid").getAsString());
                    JsonArray permsArray = playerObject.getAsJsonArray("permissions");
                    PlayerPermissionNode playerPermissionNode = new PlayerPermissionNode(null, null, "");
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
            List<IslandWarpAttributes> islandWarpList = new ArrayList<>();

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

            return islandWarpList;
        }

        public KeyMap<Integer> deserializeBlockLimits(String blocks) {
            KeyMap<Integer> blockLimits = new KeyMap<>();

            JsonArray blockLimitsArray = gson.fromJson(blocks, JsonArray.class);
            blockLimitsArray.forEach(blockLimitElement -> {
                JsonObject blockLimitObject = blockLimitElement.getAsJsonObject();
                Key blockKey = Key.of(blockLimitObject.get("id").getAsString());
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
                        Key blockKey = Key.of(generatorObject.get("id").getAsString());
                        int rate = generatorObject.get("rate").getAsInt();
                        (cobbleGenerator[i] = new KeyMap<>()).put(blockKey, rate);
                    });
                } catch (Exception ignored) {
                }
            });

            return cobbleGenerator;
        }

        public List<Pair<UUID, Long>> deserializeVisitors(String visitors) {
            List<Pair<UUID, Long>> visitorsList = new ArrayList<>();

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

            return visitorsList;
        }

        public KeyMap<Integer> deserializeEntityLimits(String entities) {
            KeyMap<Integer> entityLimits = new KeyMap<>();

            JsonArray entityLimitsArray = gson.fromJson(entities, JsonArray.class);
            entityLimitsArray.forEach(entityLimitElement -> {
                JsonObject entityLimitObject = entityLimitElement.getAsJsonObject();
                Key entity = Key.of(entityLimitObject.get("id").getAsString());
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
            List<IslandChestAttributes> islandChestList = new ArrayList<>();

            JsonArray islandChestsArray = gson.fromJson(islandChest, JsonArray.class);
            islandChestsArray.forEach(islandChestElement -> {
                JsonObject islandChestObject = islandChestElement.getAsJsonObject();
                int index = islandChestObject.get("index").getAsInt();
                String contents = islandChestObject.get("contents").getAsString();

                if (index >= islandChestList.size()) {
                    islandChestList.add(null);
                } else
                    islandChestList.add(index, new IslandChestAttributes()
                            .setValue(IslandChestAttributes.Field.INDEX, index)
                            .setValue(IslandChestAttributes.Field.CONTENTS, contents));
            });

            return islandChestList;
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
            List<WarpCategoryAttributes> warpCategories = new ArrayList<>();

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

            return warpCategories;
        }

    }

    private class RawDeserializer implements IDeserializer {

        @Override
        public Map<String, Integer> deserializeMissions(String missions) {
            Map<String, Integer> completedMissions = new HashMap<>();

            if(missions != null) {
                for (String mission : missions.split(";")) {
                    String[] missionSections = mission.split("=");
                    int completeAmount = missionSections.length > 1 ? Integer.parseInt(missionSections[1]) : 1;
                    completedMissions.put(missionSections[0], completeAmount);
                }
            }

            return completedMissions;
        }

        @Override
        public String[] deserializeHomes(String locationParam) {
            String[] islandHomes = new String[World.Environment.values().length];

            if(locationParam == null)
                return islandHomes;

            String _locationParam = locationParam.contains("=") ? locationParam : "normal=" + locationParam;

            for (String worldSection : _locationParam.split(";")) {
                try {
                    String[] locationSection = worldSection.split("=");
                    String environment = locationSection[0].toUpperCase();
                    islandHomes[World.Environment.valueOf(environment).ordinal()] = locationSection[1];
                } catch (Exception ignored) {
                }
            }

            return islandHomes;
        }

        @Override
        public List<PlayerAttributes> deserializePlayers(String players) {
            List<PlayerAttributes> playerAttributesList = new ArrayList<>();

            if(players != null) {

                for (String uuid : players.split(",")) {
                    try {
                        playerAttributesList.add(getPlayerAttributes(UUID.fromString(uuid)));
                    } catch (Exception ignored) {}
                }
            }

            return playerAttributesList;
        }

        @Override
        public Map<UUID, PlayerPermissionNode> deserializePlayerPerms(String permissionNodes) {
            Map<UUID, PlayerPermissionNode> playerPermissions = new HashMap<>();

            if(permissionNodes == null)
                return playerPermissions;

            for(String entry : permissionNodes.split(",")) {
                try {
                    String[] sections = entry.split("=");

                    try {
                        try{
                            int id = Integer.parseInt(sections[0]);
                            SPlayerRole.fromId(id);
                        }catch (Exception ex){
                            SPlayerRole.of(sections[0]);
                        }
                    }catch(Exception ex){
                        playerPermissions.put(UUID.fromString(sections[0]), new PlayerPermissionNode(null,
                                null, sections.length == 1 ? "" : sections[1]));
                    }
                }catch(Exception ignored){}
            }

            return playerPermissions;
        }

        @Override
        public Map<IslandPrivilege, PlayerRole> deserializeRolePerms(String permissionNodes) {
            Map<IslandPrivilege, PlayerRole> rolePermissions = new HashMap<>();

            if(permissionNodes == null)
                return rolePermissions;

            for(String entry : permissionNodes.split(",")) {
                try {
                    String[] sections = entry.split("=");

                    PlayerRole playerRole;

                    try{
                        int id = Integer.parseInt(sections[0]);
                        playerRole = SPlayerRole.fromId(id);
                    }catch (Exception ex){
                        playerRole = SPlayerRole.of(sections[0]);
                    }

                    if(sections.length != 1){
                        String[] permission = sections[1].split(";");
                        for (String perm : permission) {
                            String[] permissionSections = perm.split(":");
                            try {
                                IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permissionSections[0]);
                                if (permissionSections.length == 2 && permissionSections[1].equals("1")) {
                                    rolePermissions.put(islandPrivilege, playerRole);
                                }
                            }catch(Exception ignored){}
                        }
                    }
                }catch(Exception ignored){}
            }

            return rolePermissions;
        }

        @Override
        public Map<String, Integer> deserializeUpgrades(String upgrades) {
            Map<String, Integer> upgradesMap = new HashMap<>();

            if(upgrades != null) {
                for (String entry : upgrades.split(",")) {
                    try {
                        String[] sections = entry.split("=");
                        upgradesMap.put(sections[0], Integer.parseInt(sections[1]));
                    } catch (Exception ignored) {
                    }
                }
            }

            return upgradesMap;
        }

        @Override
        public List<IslandWarpAttributes> deserializeWarps(String islandWarps) {
            List<IslandWarpAttributes> warpAttributes = new ArrayList<>();

            if(islandWarps == null)
                return warpAttributes;

            for(String entry : islandWarps.split(";")) {
                try {
                    String[] sections = entry.split("=");
                    String name = StringUtils.stripColors(sections[0].trim());
                    String category = "";
                    boolean privateFlag = sections.length == 3 && Boolean.parseBoolean(sections[2]);

                    if(name.contains("-")){
                        String[] nameSections = name.split("-");
                        category = IslandUtils.getWarpName(nameSections[0]);
                        name = nameSections[1];
                    }

                    name = IslandUtils.getWarpName(name);

                    if(name.isEmpty())
                        continue;

                    warpAttributes.add(new IslandWarpAttributes()
                            .setValue(IslandWarpAttributes.Field.NAME, name)
                            .setValue(IslandWarpAttributes.Field.CATEGORY, category)
                            .setValue(IslandWarpAttributes.Field.LOCATION, sections[1])
                            .setValue(IslandWarpAttributes.Field.PRIVATE_STATUS, privateFlag)
                            .setValue(IslandWarpAttributes.Field.ICON, sections[3]));
                }catch(Exception ignored){}
            }

            return warpAttributes;
        }

        @Override
        public KeyMap<Integer> deserializeBlockLimits(String blocks) {
            KeyMap<Integer> blockLimits = new KeyMap<>();

            if(blocks != null) {
                for (String limit : blocks.split(",")) {
                    try {
                        String[] sections = limit.split("=");
                        blockLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                    } catch (Exception ignored) {
                    }
                }
            }

            return blockLimits;
        }

        @Override
        public Map<UUID, Rating> deserializeRatings(String ratings) {
            Map<UUID, Rating> ratingsMap = new HashMap<>();

            if(ratings != null) {
                for (String entry : ratings.split(";")) {
                    try {
                        String[] sections = entry.split("=");
                        ratingsMap.put(UUID.fromString(sections[0]), Rating.valueOf(Integer.parseInt(sections[1])));
                    } catch (Exception ignored) {
                    }
                }
            }

            return ratingsMap;
        }

        @Override
        public Map<IslandFlag, Byte> deserializeIslandFlags(String settings) {
            Map<IslandFlag, Byte> islandSettings = new HashMap<>();

            if(settings != null) {
                for (String setting : settings.split(";")) {
                    try {
                        if (setting.contains("=")) {
                            String[] settingSections = setting.split("=");
                            islandSettings.put(IslandFlag.getByName(settingSections[0]), Byte.valueOf(settingSections[1]));
                        } else {
                            if (!plugin.getSettings().defaultSettings.contains(setting))
                                islandSettings.put(IslandFlag.getByName(setting), (byte) 1);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return islandSettings;
        }

        @Override
        @SuppressWarnings("unchecked")
        public KeyMap<Integer>[] deserializeGenerators(String generator) {
            KeyMap<Integer>[] cobbleGenerator = new KeyMap[World.Environment.values().length];

            if(generator == null)
                return cobbleGenerator;

            if(generator.contains(";")){
                for(String env : generator.split(";")){
                    String[] sections = env.split(":");
                    try{
                        World.Environment environment = World.Environment.valueOf(sections[0]);
                        deserializeGenerators(sections[1], cobbleGenerator[environment.ordinal()] = new KeyMap<>());
                    }catch (Exception ignored){}
                }
            }
            else {
                deserializeGenerators(generator, cobbleGenerator[0] = new KeyMap<>());
            }

            return cobbleGenerator;
        }

        private void deserializeGenerators(String generator, KeyMap<Integer> cobbleGenerator) {
            for (String limit : generator.split(",")) {
                try {
                    String[] sections = limit.split("=");
                    cobbleGenerator.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public List<Pair<UUID, Long>> deserializeVisitors(String visitorsRaw) {
            List<Pair<UUID, Long>> visitors = new ArrayList<>();

            if(visitorsRaw != null) {
                for (String visitor : visitorsRaw.split(",")) {
                    try {
                        String[] visitorSections = visitor.split(";");
                        long lastTimeJoined = visitorSections.length == 2 ? Long.parseLong(visitorSections[1]) : System.currentTimeMillis();
                        visitors.add(new Pair<>(UUID.fromString(visitorSections[0]), lastTimeJoined));
                    } catch (Exception ignored) {
                    }
                }
            }

            return visitors;
        }

        @Override
        public KeyMap<Integer> deserializeEntityLimits(String entities) {
            KeyMap<Integer> entityLimits = new KeyMap<>();

            if(entities != null) {
                for (String limit : entities.split(",")) {
                    try {
                        String[] sections = limit.split("=");
                        entityLimits.put(Key.of(sections[0]), Integer.parseInt(sections[1]));
                    } catch (Exception ignored) {
                    }
                }
            }

            return entityLimits;
        }

        @Override
        public Map<PotionEffectType, Integer> deserializeEffects(String effects) {
            Map<PotionEffectType, Integer> islandEffects = new HashMap<>();

            if(effects != null) {
                for (String effect : effects.split(",")) {
                    String[] sections = effect.split("=");
                    PotionEffectType potionEffectType = PotionEffectType.getByName(sections[0]);
                    if (potionEffectType != null)
                        islandEffects.put(potionEffectType, Integer.parseInt(sections[1]));
                }
            }

            return islandEffects;
        }

        @Override
        public List<IslandChestAttributes> deserializeIslandChests(String islandChest) {
            List<IslandChestAttributes> islandChestAttributes = new ArrayList<>();

            if(islandChest == null || islandChest.isEmpty())
                return islandChestAttributes;

            String[] islandChestsSections = islandChest.split("\n");

            for(int i = 0; i < islandChestsSections.length; i++){
                islandChestAttributes.add(new IslandChestAttributes()
                        .setValue(IslandChestAttributes.Field.INDEX, i)
                        .setValue(IslandChestAttributes.Field.CONTENTS, islandChestsSections[i]));
            }

            return islandChestAttributes;
        }

        @Override
        public Map<PlayerRole, Integer> deserializeRoleLimits(String roles) {
            Map<PlayerRole, Integer> roleLimits = new HashMap<>();

            if(roles != null) {
                for (String limit : roles.split(",")) {
                    try {
                        String[] sections = limit.split("=");
                        PlayerRole playerRole = SPlayerRole.fromId(Integer.parseInt(sections[0]));
                        if (playerRole != null)
                            roleLimits.put(playerRole, Integer.parseInt(sections[1]));
                    } catch (Exception ignored) {
                    }
                }
            }

            return roleLimits;
        }

        @Override
        public List<WarpCategoryAttributes> deserializeWarpCategories(String categories) {
            List<WarpCategoryAttributes> warpCategoryAttributes = new ArrayList<>();

            if(categories == null)
                return warpCategoryAttributes;

            for(String entry : categories.split(";")) {
                try {
                    String[] sections = entry.split("=");
                    String name = StringUtils.stripColors(sections[0].trim());
                    int slot = Integer.parseInt(sections[1]);
                    String icon = sections[2];

                    warpCategoryAttributes.add(new WarpCategoryAttributes()
                            .setValue(WarpCategoryAttributes.Field.NAME, name)
                            .setValue(WarpCategoryAttributes.Field.SLOT, slot)
                            .setValue(WarpCategoryAttributes.Field.ICON, icon));
                }catch(Exception ignored){}
            }

            return warpCategoryAttributes;
        }
    }

}
