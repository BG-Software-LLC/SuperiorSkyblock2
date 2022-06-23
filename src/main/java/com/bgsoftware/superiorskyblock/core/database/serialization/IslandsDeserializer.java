package com.bgsoftware.superiorskyblock.core.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpCategoryInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.UUID;

public class IslandsDeserializer {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Gson gson = new GsonBuilder().create();
    private static final IDeserializer oldDataDeserializer = new MultipleDeserializer(
            new JsonDeserializer(null), new RawDeserializer(null, plugin)
    );

    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);

    private IslandsDeserializer() {

    }

    public static void deserializeMembers(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_members", membersRow -> {
            DatabaseResult members = new DatabaseResult(membersRow);

            Optional<UUID> uuid = members.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island members for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = members.getUUID("player");
            if (!playerUUID.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island members with invalid uuids for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);

            PlayerRole playerRole = members.getInt("role").map(SPlayerRole::fromId)
                    .orElse(SPlayerRole.defaultRole());

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get());
            superiorPlayer.setPlayerRole(playerRole);

            cachedIslandInfo.members.add(superiorPlayer);
        });
    }

    public static void deserializeBanned(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_bans", bansRow -> {
            DatabaseResult bans = new DatabaseResult(bansRow);

            Optional<UUID> uuid = bans.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load banned players for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = bans.getUUID("player");
            if (!playerUUID.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load banned players with invalid uuids for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get());
            cachedIslandInfo.bannedPlayers.add(superiorPlayer);
        });
    }

    public static void deserializeVisitors(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_visitors", visitorsRow -> {
            DatabaseResult visitors = new DatabaseResult(visitorsRow);

            Optional<UUID> islandUUID = visitors.getUUID("island");
            if (!islandUUID.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island visitors for null islands, skipping...");
                return;
            }

            Optional<UUID> uuid = visitors.getUUID("player");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island visitors with invalid uuids for %s, skipping...", islandUUID.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(islandUUID.get(), CachedIslandInfo::new);
            SuperiorPlayer visitorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid.get());
            long visitTime = visitors.getLong("visit_time").orElse(System.currentTimeMillis());
            cachedIslandInfo.uniqueVisitors.add(new SIsland.UniqueVisitor(visitorPlayer, visitTime));
        });
    }

    public static void deserializePlayerPermissions(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_player_permissions", playerPermissionRow -> {
            DatabaseResult playerPermissions = new DatabaseResult(playerPermissionRow);

            Optional<UUID> uuid = playerPermissions.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load player permissions for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = playerPermissions.getUUID("player");
            if (!playerUUID.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load player permissions for invalid players on %s, skipping...", uuid.get()));
                return;
            }

            Optional<IslandPrivilege> islandPrivilege = playerPermissions.getString("permission").map(name -> {
                try {
                    return IslandPrivilege.getByName(name);
                } catch (NullPointerException error) {
                    return null;
                }
            });
            if (!islandPrivilege.isPresent()) {
                SuperiorSkyblockPlugin.log(String.format("&cCannot load player permissions with invalid permission " +
                        "for player %s, skipping...", playerUUID.get()));
                return;
            }

            Optional<Byte> status = playerPermissions.getByte("status");
            if (!status.isPresent()) {
                SuperiorSkyblockPlugin.log(String.format("&cCannot load player permissions with invalid status " +
                        "for player %s, skipping...", playerUUID.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get());
            PlayerPrivilegeNode permissionNode = cachedIslandInfo.playerPermissions.computeIfAbsent(superiorPlayer,
                    s -> new PlayerPrivilegeNode(superiorPlayer, null));
            permissionNode.loadPrivilege(islandPrivilege.get(), status.get());
        });
    }

    public static void deserializeRolePermissions(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_role_permissions", rolePermissionsRow -> {
            DatabaseResult rolePermissions = new DatabaseResult(rolePermissionsRow);

            Optional<UUID> uuid = rolePermissions.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load role permissions for null islands, skipping...");
                return;
            }

            Optional<PlayerRole> playerRole = rolePermissions.getInt("role").map(SPlayerRole::fromId);
            if (!playerRole.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load role permissions with invalid role for %s, skipping...", uuid.get()));
                return;
            }

            Optional<IslandPrivilege> islandPrivilege = rolePermissions.getString("permission").map(name -> {
                try {
                    return IslandPrivilege.getByName(name);
                } catch (NullPointerException error) {
                    return null;
                }
            });
            if (!islandPrivilege.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load role permissions with invalid permission for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.rolePermissions.put(islandPrivilege.get(), playerRole.get());
        });
    }

    public static void deserializeUpgrades(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_upgrades", upgradesRow -> {
            DatabaseResult upgrades = new DatabaseResult(upgradesRow);

            Optional<UUID> uuid = upgrades.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load upgrades for null islands, skipping...");
                return;
            }

            Optional<String> upgrade = upgrades.getString("upgrade");
            if (!upgrade.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load upgrades with invalid upgrade names for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> level = upgrades.getInt("level");
            if (!level.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load upgrades with invalid levels for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.upgrades.put(upgrade.get(), level.get());
        });
    }

    public static void deserializeWarps(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_warps", islandWarpsRow -> {
            DatabaseResult islandWarp = new DatabaseResult(islandWarpsRow);

            Optional<UUID> uuid = islandWarp.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load warps for null islands, skipping...");
                return;
            }

            Optional<String> name = islandWarp.getString("name").map(_name -> {
                return IslandUtils.isWarpNameLengthValid(_name) ? _name : _name.substring(0, IslandUtils.getMaxWarpNameLength());
            });
            if (!name.isPresent() || name.get().isEmpty()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load warps with invalid names for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Location> location = islandWarp.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load warps with invalid locations for %s, skipping...", uuid.get()));
                return;
            }

            CachedWarpInfo cachedWarpInfo = new CachedWarpInfo();
            cachedWarpInfo.name = name.get();
            cachedWarpInfo.category = islandWarp.getString("category").orElse("");
            cachedWarpInfo.location = location.get();
            cachedWarpInfo.isPrivate = islandWarp.getBoolean("private").orElse(true);
            cachedWarpInfo.icon = islandWarp.getString("icon").map(Serializers.ITEM_STACK_SERIALIZER::deserialize).orElse(null);

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.cachedWarpInfoList.add(cachedWarpInfo);
        });
    }

    public static void deserializeBlockCounts(String blocks, Island island) {
        if (Text.isBlank(blocks))
            return;

        JsonArray blockCounts;

        try {
            blockCounts = gson.fromJson(blocks, JsonArray.class);
        } catch (JsonSyntaxException error) {
            blockCounts = gson.fromJson(oldDataDeserializer.deserializeBlockCounts(blocks), JsonArray.class);
        }

        blockCounts.forEach(blockCountElement -> {
            JsonObject blockCountObject = blockCountElement.getAsJsonObject();
            Key blockKey = KeyImpl.of(blockCountObject.get("id").getAsString());
            BigInteger amount = new BigInteger(blockCountObject.get("amount").getAsString());
            island.handleBlockPlace(blockKey, amount, false, false);
        });
    }

    public static void deserializeBlockLimits(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_block_limits", blockLimitRow -> {
            DatabaseResult blockLimits = new DatabaseResult(blockLimitRow);

            Optional<UUID> uuid = blockLimits.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load block limits for null islands, skipping...");
                return;
            }

            Optional<Key> block = blockLimits.getString("block").map(KeyImpl::of);
            if (!block.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load block limits for invalid blocks for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> limit = blockLimits.getInt("limit");
            if (!limit.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load block limits with invalid limits for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.blockLimits.put(block.get(), limit.get() < 0 ? Value.syncedFixed(limit.get()) : Value.fixed(limit.get()));
        });
    }

    public static void deserializeEntityLimits(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_entity_limits", entityLimitsRow -> {
            DatabaseResult entityLimits = new DatabaseResult(entityLimitsRow);

            Optional<UUID> uuid = entityLimits.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load entity limits for null islands, skipping...");
                return;
            }

            Optional<Key> entity = entityLimits.getString("entity").map(KeyImpl::of);
            if (!entity.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load entity limits for invalid entities on %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> limit = entityLimits.getInt("limit");
            if (!limit.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load entity limits with invalid limits for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.entityLimits.put(entity.get(), limit.get() < 0 ? Value.syncedFixed(limit.get()) : Value.fixed(limit.get()));
        });
    }

    public static void deserializeRatings(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_ratings", ratingsRow -> {
            DatabaseResult ratings = new DatabaseResult(ratingsRow);

            Optional<UUID> islandUUID = ratings.getUUID("island");
            if (!islandUUID.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load ratings for null islands, skipping...");
                return;
            }

            Optional<UUID> uuid = ratings.getUUID("player");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load ratings with invalid players for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Rating> rating = ratings.getInt("rating").map(value -> {
                try {
                    return Rating.valueOf(value);
                } catch (ArrayIndexOutOfBoundsException error) {
                    return null;
                }
            });
            if (!rating.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load ratings with invalid rating value for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(islandUUID.get(), CachedIslandInfo::new);
            cachedIslandInfo.ratings.put(uuid.get(), rating.get());
        });
    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_missions", missionsRow -> {
            DatabaseResult missions = new DatabaseResult(missionsRow);

            Optional<UUID> uuid = missions.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island missions for null islands, skipping...");
                return;
            }

            Optional<String> missionName = missions.getString("name");
            Optional<Mission<?>> mission = missionName.map(plugin.getMissions()::getMission);
            if (!mission.isPresent()) {
                if (!missionName.isPresent()) {
                    SuperiorSkyblockPlugin.log(
                            String.format("&cCannot load island missions with invalid missions for %s, skipping...", uuid.get()));
                } else {
                    SuperiorSkyblockPlugin.log(
                            String.format("&cCannot load island missions with invalid mission %s for %s, skipping...",
                                    missionName.get(), uuid.get()));
                }
                return;
            }

            Optional<Integer> finishCount = missions.getInt("finish_count");
            if (!finishCount.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island missions with invalid finish count for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.completedMissions.put(mission.get(), finishCount.get());
        });
    }

    public static void deserializeIslandFlags(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_flags", islandFlagRow -> {
            DatabaseResult islandFlagResult = new DatabaseResult(islandFlagRow);

            Optional<UUID> uuid = islandFlagResult.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island flags for null islands, skipping...");
                return;
            }

            Optional<IslandFlag> islandFlag = islandFlagResult.getString("name").map(name -> {
                try {
                    return IslandFlag.getByName(name);
                } catch (NullPointerException error) {
                    return null;
                }
            });
            if (!islandFlag.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island flags with invalid flags for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Byte> status = islandFlagResult.getByte("status");
            if (!status.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island flags with invalid status for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.islandFlags.put(islandFlag.get(), status.get());
        });
    }

    public static void deserializeGenerators(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_generators", generatorsRow -> {
            DatabaseResult generators = new DatabaseResult(generatorsRow);

            Optional<UUID> uuid = generators.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load generator rates for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = generators.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load generator rates with invalid environment for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Key> block = generators.getString("block").map(KeyImpl::of);
            if (!block.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load generator rates with invalid block for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> rate = generators.getInt("rate");
            if (!rate.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load generator rates with invalid rate for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            KeyMap<Value<Integer>> generatorRates = cachedIslandInfo.cobbleGeneratorValues[environment.get()];

            if (generatorRates == null)
                generatorRates = cachedIslandInfo.cobbleGeneratorValues[environment.get()] = KeyMapImpl.createHashMap();

            generatorRates.put(block.get(), rate.get() < 0 ? Value.syncedFixed(rate.get()) : Value.fixed(rate.get()));
        });
    }

    public static void deserializeIslandHomes(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_homes", islandHomesRow -> {
            DatabaseResult islandHomes = new DatabaseResult(islandHomesRow);

            Optional<UUID> uuid = islandHomes.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island homes for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = islandHomes.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island homes with invalid environment for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Location> location = islandHomes.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island homes with invalid location for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.islandHomes[environment.get()] = location.get();
        });
    }

    public static void deserializeVisitorHomes(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_visitor_homes", islandVisitorHomesRow -> {
            DatabaseResult islandVisitorHomes = new DatabaseResult(islandVisitorHomesRow);

            Optional<UUID> uuid = islandVisitorHomes.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island homes for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = islandVisitorHomes.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island homes with invalid environment for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Location> location = islandVisitorHomes.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island homes with invalid location for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.visitorHomes[environment.get()] = location.get();
        });
    }

    public static void deserializeEffects(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_effects", islandEffectRow -> {
            DatabaseResult islandEffects = new DatabaseResult(islandEffectRow);

            Optional<UUID> uuid = islandEffects.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island effects for null islands, skipping...");
                return;
            }

            Optional<PotionEffectType> effectType = islandEffects.getString("effect_type")
                    .map(PotionEffectType::getByName);
            if (!effectType.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island effects with invalid effect for %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> level = islandEffects.getInt("level");
            if (!level.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island effects with invalid level for %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.islandEffects.put(effectType.get(), level.get() < 0 ? Value.syncedFixed(level.get()) : Value.fixed(level.get()));
        });
    }

    public static void deserializeIslandChest(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_chests", islandChestsRow -> {
            DatabaseResult islandChests = new DatabaseResult(islandChestsRow);

            Optional<UUID> uuid = islandChests.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island chests for null islands, skipping...");
                return;
            }

            Optional<Integer> index = islandChests.getInt("index");
            if (!index.isPresent() || index.get() < 0) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island chest with invalid index for %s, skipping...", uuid.get()));
                return;
            }

            Optional<ItemStack[]> contents = islandChests.getString("contents").map(Serializers.INVENTORY_SERIALIZER::deserialize);
            if (!contents.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island chest with invalid contents for %s, skipping...", uuid.get()));
                return;
            }

            int contentsLength = contents.get().length;
            ItemStack[] chestContents;

            if (contentsLength % 9 != 0) {
                int amountOfRows = Math.min(1, Math.max(6, (contentsLength / 9) + 1));
                chestContents = new ItemStack[amountOfRows * 9];
                int amountOfContentsToCopy = Math.min(contentsLength, chestContents.length);
                System.arraycopy(contents.get(), 0, chestContents, 0, amountOfContentsToCopy);
            } else if (contentsLength > 54) {
                chestContents = new ItemStack[54];
                System.arraycopy(contents.get(), 0, chestContents, 0, 54);
            } else if (contentsLength < 9) {
                chestContents = new ItemStack[9];
                System.arraycopy(contents.get(), 0, chestContents, 0, contentsLength);
            } else {
                chestContents = contents.get();
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);

            if (index.get() >= cachedIslandInfo.islandChests.size()) {
                while (index.get() > cachedIslandInfo.islandChests.size()) {
                    cachedIslandInfo.islandChests.add(new ItemStack[plugin.getSettings().getIslandChests().getDefaultSize() * 9]);
                }

                cachedIslandInfo.islandChests.add(chestContents);
            } else {
                cachedIslandInfo.islandChests.set(index.get(), chestContents);
            }
        });
    }

    public static void deserializeRoleLimits(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_role_limits", roleLimitRaw -> {
            DatabaseResult roleLimits = new DatabaseResult(roleLimitRaw);

            Optional<UUID> uuid = roleLimits.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load role limits for null islands, skipping...");
                return;
            }

            Optional<PlayerRole> playerRole = roleLimits.getInt("role").map(SPlayerRole::fromId);
            if (!playerRole.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load role limit for invalid role on %s, skipping...", uuid.get()));
                return;
            }

            Optional<Integer> limit = roleLimits.getInt("limit");
            if (!limit.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load role limit for invalid limit on %s, skipping...", uuid.get()));
                return;
            }

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.roleLimits.put(playerRole.get(), limit.get() < 0 ? Value.syncedFixed(limit.get()) : Value.fixed(limit.get()));
        });
    }

    public static void deserializeWarpCategories(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_warp_categories", warpCategoryRow -> {
            DatabaseResult warpCategory = new DatabaseResult(warpCategoryRow);

            Optional<UUID> uuid = warpCategory.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load warp categories for null islands, skipping...");
                return;
            }

            Optional<String> name = warpCategory.getString("name").map(Formatters.STRIP_COLOR_FORMATTER::format);
            if (!name.isPresent() || name.get().isEmpty()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load warp categories with invalid name for %s, skipping...", uuid.get()));
                return;
            }

            CachedWarpCategoryInfo cachedWarpCategoryInfo = new CachedWarpCategoryInfo();
            cachedWarpCategoryInfo.name = name.get();
            cachedWarpCategoryInfo.slot = warpCategory.getInt("slot").orElse(-1);
            cachedWarpCategoryInfo.icon = warpCategory.getString("icon").map(Serializers.ITEM_STACK_SERIALIZER::deserialize).orElse(null);

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.cachedWarpCategoryInfoList.add(cachedWarpCategoryInfo);
        });
    }

    public static void deserializeIslandBank(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_banks", islandBankRow -> {
            DatabaseResult islandBank = new DatabaseResult(islandBankRow);

            Optional<UUID> uuid = islandBank.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island banks for null islands, skipping...");
                return;
            }

            Optional<BigDecimal> balance = islandBank.getBigDecimal("balance");
            if (!balance.isPresent()) {
                SuperiorSkyblockPlugin.log(
                        String.format("&cCannot load island banks with invalid balance for %s, skipping...", uuid.get()));
                return;
            }

            long currentTime = System.currentTimeMillis() / 1000;

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.balance = balance.get();
            cachedIslandInfo.lastInterestTime = islandBank.getLong("last_interest_time").orElse(currentTime);

            if (cachedIslandInfo.lastInterestTime > currentTime)
                cachedIslandInfo.lastInterestTime /= 1000L;
        });
    }

    public static void deserializeIslandSettings(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_settings", islandSettingsRow -> {
            DatabaseResult islandSettings = new DatabaseResult(islandSettingsRow);

            Optional<String> island = islandSettings.getString("island");
            if (!island.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load island settings of null island, skipping ");
                return;
            }

            UUID uuid = UUID.fromString(island.get());
            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid, CachedIslandInfo::new);

            int borderSize = islandSettings.getInt("size").orElse(-1);
            cachedIslandInfo.islandSize = borderSize < 0 ? Value.syncedFixed(borderSize) : Value.fixed(borderSize);

            int membersLimit = islandSettings.getInt("members_limit").orElse(-1);
            cachedIslandInfo.teamLimit = membersLimit < 0 ? Value.syncedFixed(membersLimit) : Value.fixed(membersLimit);

            int warpsLimit = islandSettings.getInt("warps_limit").orElse(-1);
            cachedIslandInfo.warpsLimit = warpsLimit < 0 ? Value.syncedFixed(warpsLimit) : Value.fixed(warpsLimit);

            double cropGrowth = islandSettings.getDouble("crop_growth_multiplier").orElse(-1D);
            cachedIslandInfo.cropGrowth = cropGrowth < 0 ? Value.syncedFixed(cropGrowth) : Value.fixed(cropGrowth);

            double spawnerRates = islandSettings.getDouble("spawner_rates_multiplier").orElse(-1D);
            cachedIslandInfo.spawnerRates = spawnerRates < 0 ? Value.syncedFixed(spawnerRates) : Value.fixed(spawnerRates);

            double mobDrops = islandSettings.getDouble("mob_drops_multiplier").orElse(-1D);
            cachedIslandInfo.mobDrops = mobDrops < 0 ? Value.syncedFixed(mobDrops) : Value.fixed(mobDrops);

            int coopLimit = islandSettings.getInt("coops_limit").orElse(-1);
            cachedIslandInfo.coopLimit = coopLimit < 0 ? Value.syncedFixed(coopLimit) : Value.fixed(coopLimit);

            BigDecimal bankLimit = islandSettings.getBigDecimal("bank_limit").orElse(SYNCED_BANK_LIMIT_VALUE);
            cachedIslandInfo.bankLimit = bankLimit.compareTo(SYNCED_BANK_LIMIT_VALUE) <= 0 ? Value.syncedFixed(bankLimit) : Value.fixed(bankLimit);
        });
    }

    public static void deserializeBankTransactions(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        if (BuiltinModules.BANK.bankLogs && BuiltinModules.BANK.cacheAllLogs) {
            databaseBridge.loadAllObjects("bank_transactions", bankTransactionRow -> {
                DatabaseResult bankTransaction = new DatabaseResult(bankTransactionRow);

                Optional<UUID> uuid = bankTransaction.getUUID("island");
                if (!uuid.isPresent()) {
                    SuperiorSkyblockPlugin.log("&cCannot load bank transaction for null islands, skipping...");
                    return;
                }

                CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
                SBankTransaction.fromDatabase(bankTransaction).ifPresent(cachedIslandInfo.bankTransactions::add);
            });
        }
    }

    public static void deserializePersistentDataContainer(DatabaseBridge databaseBridge, DatabaseCache<CachedIslandInfo> databaseCache) {
        databaseBridge.loadAllObjects("islands_custom_data", customDataRow -> {
            DatabaseResult customData = new DatabaseResult(customDataRow);

            Optional<UUID> uuid = customData.getUUID("island");
            if (!uuid.isPresent()) {
                SuperiorSkyblockPlugin.log("&cCannot load custom data for null islands, skipping...");
                return;
            }

            byte[] persistentData = customData.getBlob("data").orElse(new byte[0]);

            if (persistentData.length == 0)
                return;

            CachedIslandInfo cachedIslandInfo = databaseCache.computeIfAbsentInfo(uuid.get(), CachedIslandInfo::new);
            cachedIslandInfo.persistentData = persistentData;
        });
    }

}
