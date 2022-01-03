package com.bgsoftware.superiorskyblock.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.database.cache.CachedWarpCategoryInfo;
import com.bgsoftware.superiorskyblock.database.cache.CachedWarpInfo;
import com.bgsoftware.superiorskyblock.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.database.loader.v1.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

public final class IslandsDeserializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Gson gson = new GsonBuilder().create();
    private static final IDeserializer oldDataDeserializer = new MultipleDeserializer(
            new JsonDeserializer(null), new RawDeserializer(null, plugin)
    );

    private IslandsDeserializer() {

    }

    public static void deserializeMembers(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_members", membersRow -> {
            UUID uuid = UUID.fromString((String) membersRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            UUID playerUUID = UUID.fromString((String) membersRow.get("player"));
            PlayerRole playerRole = SPlayerRole.fromId((int) membersRow.get("role"));

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);
            //superiorPlayer.setIsland(island);
            superiorPlayer.setPlayerRole(playerRole);

            cachedIslandInfo.members.add(superiorPlayer);
        });
    }

    public static void deserializeBanned(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_bans", bansRow -> {
            UUID uuid = UUID.fromString((String) bansRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            UUID playerUUID = UUID.fromString((String) bansRow.get("player"));
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);
            cachedIslandInfo.banned.add(superiorPlayer);
        });
    }

    public static void deserializeVisitors(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_visitors", visitorsRow -> {
            UUID islandUUID = UUID.fromString((String) visitorsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(islandUUID);

            UUID uuid = UUID.fromString((String) visitorsRow.get("player"));
            long visitTime = (long) visitorsRow.get("visit_time");
            cachedIslandInfo.uniqueVisitors.add(new Pair<>(plugin.getPlayers().getSuperiorPlayer(uuid), visitTime));
        });
    }

    public static void deserializePlayerPermissions(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_player_permissions", playerPermissionRow -> {
            UUID uuid = UUID.fromString((String) playerPermissionRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            UUID playerUUID = UUID.fromString((String) playerPermissionRow.get("player"));
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID);

            try {
                IslandPrivilege islandPrivilege = IslandPrivilege.getByName((String) playerPermissionRow.get("permission"));
                cachedIslandInfo.playerPermissions.computeIfAbsent(superiorPlayer, s -> new PlayerPermissionNode(superiorPlayer, null))
                        .loadPrivilege(islandPrivilege, getAsByte(playerPermissionRow.get("status")));
            } catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading player permissions:");
                error.printStackTrace();
                PluginDebugger.debug(error);
            }
        });
    }

    public static void deserializeRolePermissions(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_role_permissions", rolePermissionRow -> {
            UUID uuid = UUID.fromString((String) rolePermissionRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            try {
                PlayerRole playerRole = SPlayerRole.fromId((int) rolePermissionRow.get("role"));
                IslandPrivilege islandPrivilege = IslandPrivilege.getByName((String) rolePermissionRow.get("permission"));
                cachedIslandInfo.rolePermissions.put(islandPrivilege, playerRole);
            } catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading role permissions:");
                error.printStackTrace();
                PluginDebugger.debug(error);
            }
        });
    }

    public static void deserializeUpgrades(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_upgrades", upgradeRow -> {
            UUID uuid = UUID.fromString((String) upgradeRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            String upgradeName = (String) upgradeRow.get("upgrade");
            int level = (int) upgradeRow.get("level");
            cachedIslandInfo.upgrades.put(upgradeName, level);
        });
    }

    public static void deserializeWarps(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_warps", islandWarpRow -> {
            String name = IslandUtils.getWarpName((String) islandWarpRow.get("name"));

            if (name.isEmpty())
                return;

            if (!IslandUtils.isWarpNameLengthValid(name))
                name = name.substring(0, IslandUtils.getMaxWarpNameLength());

            CachedWarpInfo cachedWarpInfo = new CachedWarpInfo();
            cachedWarpInfo.name = name;
            cachedWarpInfo.category = (String) islandWarpRow.getOrDefault("category", "");
            cachedWarpInfo.location = FileUtils.toLocation((String) islandWarpRow.get("location"));
            cachedWarpInfo.isPrivate = getAsByte(islandWarpRow.get("private")) == 1;
            if (!((String) islandWarpRow.getOrDefault("icon", "")).isEmpty())
                cachedWarpInfo.icon = ItemUtils.deserializeItem((String) islandWarpRow.get("icon"));

            UUID uuid = UUID.fromString((String) islandWarpRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            cachedIslandInfo.cachedWarpInfoList.add(cachedWarpInfo);
        });
    }

    public static void deserializeBlockCounts(String blocks, Island island) {
        if (blocks == null || blocks.isEmpty())
            return;

        JsonArray blockCounts;

        try {
            blockCounts = gson.fromJson(blocks, JsonArray.class);
        } catch (JsonSyntaxException error) {
            blockCounts = gson.fromJson(oldDataDeserializer.deserializeBlockCounts(blocks), JsonArray.class);
        }

        blockCounts.forEach(blockCountElement -> {
            JsonObject blockCountObject = blockCountElement.getAsJsonObject();
            Key blockKey = Key.of(blockCountObject.get("id").getAsString());
            BigInteger amount = new BigInteger(blockCountObject.get("amount").getAsString());
            island.handleBlockPlace(blockKey, amount, false, false);
        });
    }

    public static void deserializeBlockLimits(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_block_limits", blockLimitsRow -> {
            UUID uuid = UUID.fromString((String) blockLimitsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            Key blockKey = Key.of((String) blockLimitsRow.get("block"));
            int limit = (int) blockLimitsRow.get("limit");
            cachedIslandInfo.blockLimits.put(blockKey, new UpgradeValue<>(limit, i -> i < 0));
        });
    }

    public static void deserializeEntityLimits(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_entity_limits", entityLimitsRow -> {
            UUID uuid = UUID.fromString((String) entityLimitsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            Key entity = Key.of((String) entityLimitsRow.get("entity"));
            int limit = (int) entityLimitsRow.get("limit");
            cachedIslandInfo.entityLimits.put(entity, new UpgradeValue<>(limit, i -> i < 0));
        });
    }

    public static void deserializeRatings(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_ratings", ratingsRow -> {
            UUID islandUUID = UUID.fromString((String) ratingsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(islandUUID);

            UUID uuid = UUID.fromString((String) ratingsRow.get("player"));
            Rating rating = Rating.valueOf((int) ratingsRow.get("rating"));
            cachedIslandInfo.ratings.put(uuid, rating);
        });
    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_missions", missionsRow -> {
            UUID uuid = UUID.fromString((String) missionsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            String name = (String) missionsRow.get("name");
            int finishCount = (int) missionsRow.get("finish_count");

            Mission<?> mission = plugin.getMissions().getMission(name);

            if (mission != null)
                cachedIslandInfo.completedMissions.put(mission, finishCount);
        });
    }

    public static void deserializeIslandFlags(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_flags", islandFlagRow -> {
            UUID uuid = UUID.fromString((String) islandFlagRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            try {
                IslandFlag islandFlag = IslandFlag.getByName((String) islandFlagRow.get("name"));
                byte status = getAsByte(islandFlagRow.get("status"));
                cachedIslandInfo.islandSettings.put(islandFlag, status);
            } catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading island flags:");
                error.printStackTrace();
                PluginDebugger.debug(error);
            }
        });
    }

    public static void deserializeGenerators(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_generators", generatorsRow -> {
            UUID uuid = UUID.fromString((String) generatorsRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            try {
                int environment = World.Environment.valueOf((String) generatorsRow.get("environment")).ordinal();
                Key blockKey = Key.of((String) generatorsRow.get("block"));
                int rate = (int) generatorsRow.get("rate");
                (cachedIslandInfo.cobbleGeneratorValues[environment] = new KeyMap<>())
                        .put(blockKey, new UpgradeValue<>(rate, n -> n < 0));
            } catch (Exception error) {
                SuperiorSkyblockPlugin.log("&cError occurred while loading generators:");
                error.printStackTrace();
                PluginDebugger.debug(error);
            }
        });
    }

    public static void deserializeIslandHomes(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_homes", teleportLocationRow -> {
            UUID uuid = UUID.fromString((String) teleportLocationRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            int environment = World.Environment.valueOf((String) teleportLocationRow.get("environment")).ordinal();
            cachedIslandInfo.teleportLocations[environment] = FileUtils.toLocation((String) teleportLocationRow.get("location"));
        });
    }

    public static void deserializeVisitorHomes(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_visitor_homes", teleportLocationRow -> {
            UUID uuid = UUID.fromString((String) teleportLocationRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            int environment = World.Environment.valueOf((String) teleportLocationRow.get("environment")).ordinal();
            cachedIslandInfo.visitorsLocations[environment] = FileUtils.toLocation((String) teleportLocationRow.get("location"));
        });
    }

    public static void deserializeEffects(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_effects", islandEffectRow -> {
            UUID uuid = UUID.fromString((String) islandEffectRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            PotionEffectType potionEffectType = PotionEffectType.getByName((String) islandEffectRow.get("effect_type"));
            if (potionEffectType != null) {
                int level = (int) islandEffectRow.get("level");
                cachedIslandInfo.islandEffects.put(potionEffectType, new UpgradeValue<>(level, i -> i < 0));
            }
        });
    }

    public static void deserializeIslandChest(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_chests", islandChestRow -> {
            UUID uuid = UUID.fromString((String) islandChestRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            int index = (int) islandChestRow.get("index");
            String contents = (String) islandChestRow.get("contents");

            while (index > cachedIslandInfo.islandChest.size()) {
                cachedIslandInfo.islandChest.add(new ItemStack[plugin.getSettings().getIslandChests().getDefaultSize()]);
            }

            cachedIslandInfo.islandChest.add(ItemUtils.deserialize(contents));
        });
    }

    public static void deserializeRoleLimits(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_role_limits", roleLimitRaw -> {
            UUID uuid = UUID.fromString((String) roleLimitRaw.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            PlayerRole playerRole = SPlayerRole.fromId((int) roleLimitRaw.get("role"));
            if (playerRole != null) {
                int limit = (int) roleLimitRaw.get("limit");
                cachedIslandInfo.roleLimits.put(playerRole, new UpgradeValue<>(limit, i -> i < 0));
            }
        });
    }

    public static void deserializeWarpCategories(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_warp_categories", warpCategoryRow -> {
            String name = StringUtils.stripColors((String) warpCategoryRow.get("name"));

            CachedWarpCategoryInfo cachedWarpCategoryInfo = new CachedWarpCategoryInfo();
            cachedWarpCategoryInfo.name = name;
            cachedWarpCategoryInfo.slot = (int) warpCategoryRow.get("slot");
            cachedWarpCategoryInfo.icon = ItemUtils.deserializeItem((String) warpCategoryRow.get("icon"));

            UUID uuid = UUID.fromString((String) warpCategoryRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);
            cachedIslandInfo.cachedWarpCategoryInfoList.add(cachedWarpCategoryInfo);
        });
    }

    public static void deserializeIslandBank(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_banks", islandBankRow -> {
            UUID uuid = UUID.fromString((String) islandBankRow.get("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            BigDecimal balance = new BigDecimal((String) islandBankRow.get("balance"));
            long lastInterestTime = getAsLong(islandBankRow.get("last_interest_time"));
            if (lastInterestTime > (System.currentTimeMillis() / 1000))
                lastInterestTime /= 1000;

            cachedIslandInfo.balance = balance;
            cachedIslandInfo.lastInterestTime = lastInterestTime;
        });
    }

    public static void deserializeIslandSettings(DatabaseBridge databaseBridge, DatabaseCache databaseCache) {
        databaseBridge.loadAllObjects("islands_settings", islandSettingsRow -> {
            DatabaseResult islandSettings = new DatabaseResult(islandSettingsRow);

            UUID uuid = UUID.fromString(islandSettings.getString("island"));
            CachedIslandInfo cachedIslandInfo = databaseCache.addCachedIslandInfo(uuid);

            cachedIslandInfo.islandSize = new UpgradeValue<>(islandSettings.getInt("size"), i -> i < 0);
            cachedIslandInfo.teamLimit = new UpgradeValue<>(islandSettings.getInt("members_limit"), i -> i < 0);
            cachedIslandInfo.warpsLimit = new UpgradeValue<>(islandSettings.getInt("warps_limit"), i -> i < 0);
            cachedIslandInfo.cropGrowth = new UpgradeValue<>(islandSettings.getDouble("crop_growth_multiplier"), i -> i < 0);
            cachedIslandInfo.spawnerRates = new UpgradeValue<>(islandSettings.getDouble("spawner_rates_multiplier"), i -> i < 0);
            cachedIslandInfo.mobDrops = new UpgradeValue<>(islandSettings.getDouble("mob_drops_multiplier"), i -> i < 0);
            cachedIslandInfo.coopLimit = new UpgradeValue<>(islandSettings.getInt("coops_limit"), i -> i < 0);
            cachedIslandInfo.bankLimit = new UpgradeValue<>(islandSettings.getBigDecimal("bank_limit"), i -> i.compareTo(new BigDecimal(-1)) < 0);
        });
    }

    private static long getAsLong(Object value) {
        if (value instanceof Long) {
            return (long) value;
        } else if (value instanceof Integer) {
            return (int) value;
        } else {
            throw new IllegalArgumentException("Cannot cast " + value + " from type " + value.getClass() + " to long.");
        }
    }

    private static byte getAsByte(Object value) {
        if (value instanceof Byte) {
            return (byte) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? (byte) 1 : 0;
        } else if (value instanceof Integer) {
            return (byte) (int) value;
        } else {
            throw new IllegalArgumentException("Cannot cast " + value + " from type " + value.getClass() + " to byte.");
        }
    }

}
