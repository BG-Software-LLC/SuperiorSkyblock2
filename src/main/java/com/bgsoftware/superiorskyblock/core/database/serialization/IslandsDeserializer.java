package com.bgsoftware.superiorskyblock.core.database.serialization;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.IDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.JsonDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.MultipleDeserializer;
import com.bgsoftware.superiorskyblock.core.database.loader.v1.deserializer.RawDeserializer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.bank.SBankTransaction;
import com.bgsoftware.superiorskyblock.island.builder.IslandBuilderImpl;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
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

    public static void deserializeMembers(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_members", membersRow -> {
            DatabaseResult members = new DatabaseResult(membersRow);

            Optional<UUID> uuid = members.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island members for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = members.getUUID("player");
            if (!playerUUID.isPresent()) {
                Log.warn("Cannot load island members with invalid uuids for ", uuid.get(), ", skipping...");
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get(), false);
            if (superiorPlayer == null) {
                Log.warn("Cannot load island member with unrecognized uuid: " + playerUUID.get() + ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);

            PlayerRole playerRole = members.getInt("role").map(SPlayerRole::fromId)
                    .orElse(SPlayerRole.defaultRole());


            superiorPlayer.setPlayerRole(playerRole);
            builder.addIslandMember(superiorPlayer);
        });
    }

    public static void deserializeBanned(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_bans", bansRow -> {
            DatabaseResult bans = new DatabaseResult(bansRow);

            Optional<UUID> uuid = bans.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load banned players for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = bans.getUUID("player");
            if (!playerUUID.isPresent()) {
                Log.warn("Cannot load banned players with invalid uuids for ", uuid.get(), ", skipping...");
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get(), false);
            if (superiorPlayer == null) {
                Log.warn("Cannot load island ban with unrecognized uuid: " + playerUUID.get() + ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.addBannedPlayer(superiorPlayer);
        });
    }

    public static void deserializeVisitors(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_visitors", visitorsRow -> {
            DatabaseResult visitors = new DatabaseResult(visitorsRow);

            Optional<UUID> islandUUID = visitors.getUUID("island");
            if (!islandUUID.isPresent()) {
                Log.warn("Cannot load island visitors for null islands, skipping...");
                return;
            }

            Optional<UUID> uuid = visitors.getUUID("player");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island visitors with invalid uuids for ", islandUUID.get(), ", skipping...");
                return;
            }

            SuperiorPlayer visitorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid.get(), false);
            if (visitorPlayer == null) {
                Log.warn("Cannot load island visitor with unrecognized uuid: " + uuid.get() + ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(islandUUID.get(), IslandBuilderImpl::new);
            long visitTime = visitors.getLong("visit_time").orElse(System.currentTimeMillis());
            builder.addUniqueVisitor(visitorPlayer, visitTime);
        });
    }

    public static void deserializePlayerPermissions(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_player_permissions", playerPermissionRow -> {
            DatabaseResult playerPermissions = new DatabaseResult(playerPermissionRow);

            Optional<UUID> uuid = playerPermissions.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load player permissions for null islands, skipping...");
                return;
            }

            Optional<UUID> playerUUID = playerPermissions.getUUID("player");
            if (!playerUUID.isPresent()) {
                Log.warn("Cannot load player permissions for invalid players on ", uuid.get(), ", skipping...");
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(playerUUID.get(), false);
            if (superiorPlayer == null) {
                Log.warn("Cannot load island player permissions with unrecognized uuid: " + playerUUID.get() + ", skipping...");
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
                Log.warn("Cannot load player permissions with invalid permission for player ", playerUUID.get(), ", skipping...");
                return;
            }

            Optional<Byte> status = playerPermissions.getByte("status");
            if (!status.isPresent()) {
                Log.warn("Cannot load player permissions with invalid status for player ", playerUUID.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setPlayerPermission(superiorPlayer, islandPrivilege.get(), status.get() == 1);
        });
    }

    public static void deserializeRolePermissions(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_role_permissions", rolePermissionsRow -> {
            DatabaseResult rolePermissions = new DatabaseResult(rolePermissionsRow);

            Optional<UUID> uuid = rolePermissions.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load role permissions for null islands, skipping...");
                return;
            }

            Optional<PlayerRole> playerRole = rolePermissions.getInt("role").map(SPlayerRole::fromId);
            if (!playerRole.isPresent()) {
                Log.warn("Cannot load role permissions with invalid role for ", uuid.get(), ", skipping...");
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
                Log.warn("Cannot load role permissions with invalid permission for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setRolePermission(islandPrivilege.get(), playerRole.get());
        });
    }

    public static void deserializeUpgrades(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_upgrades", upgradesRow -> {
            DatabaseResult upgrades = new DatabaseResult(upgradesRow);

            Optional<UUID> uuid = upgrades.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load upgrades for null islands, skipping...");
                return;
            }

            Optional<Upgrade> upgrade = upgrades.getString("upgrade").map(plugin.getUpgrades()::getUpgrade);
            if (!upgrade.isPresent()) {
                Log.warn("Cannot load upgrades with invalid upgrade names for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> level = upgrades.getInt("level");
            if (!level.isPresent()) {
                Log.warn("Cannot load upgrades with invalid levels for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setUpgrade(upgrade.get(), level.get());
        });
    }

    public static void deserializeWarps(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_warps", islandWarpsRow -> {
            DatabaseResult islandWarp = new DatabaseResult(islandWarpsRow);

            Optional<UUID> uuid = islandWarp.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load warps for null islands, skipping...");
                return;
            }

            Optional<String> name = islandWarp.getString("name").map(_name -> {
                return IslandUtils.isWarpNameLengthValid(_name) ? _name : _name.substring(0, IslandUtils.getMaxWarpNameLength());
            });
            if (!name.isPresent() || name.get().isEmpty()) {
                Log.warn("Cannot load warps with invalid names for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Location> location = islandWarp.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                Log.warn("Cannot load warps with invalid locations for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.addWarp(name.get(), islandWarp.getString("category").orElse(""),
                    location.get(), islandWarp.getBoolean("private").orElse(!plugin.getSettings().isPublicWarps()),
                    islandWarp.getString("icon").map(Serializers.ITEM_STACK_SERIALIZER::deserialize).orElse(null));
        });
    }

    public static void deserializeDirtyChunks(Island.Builder builder, String dirtyChunks) {
        if (Text.isBlank(dirtyChunks))
            return;

        try {
            JsonObject dirtyChunksObject = gson.fromJson(dirtyChunks, JsonObject.class);
            dirtyChunksObject.entrySet().forEach(dirtyChunkEntry -> {
                String worldName = dirtyChunkEntry.getKey();
                JsonArray dirtyChunksArray = dirtyChunkEntry.getValue().getAsJsonArray();

                dirtyChunksArray.forEach(dirtyChunkElement -> {
                    String[] chunkPositionSections = dirtyChunkElement.getAsString().split(",");
                    builder.setDirtyChunk(worldName, Integer.parseInt(chunkPositionSections[0]),
                            Integer.parseInt(chunkPositionSections[1]));
                });
            });
        } catch (JsonSyntaxException ex) {
            if (dirtyChunks.contains("|")) {
                String[] serializedSections = dirtyChunks.split("\\|");

                for (String section : serializedSections) {
                    String[] worldSections = section.split("=");
                    if (worldSections.length == 2) {
                        String[] dirtyChunkSections = worldSections[1].split(";");
                        for (String dirtyChunk : dirtyChunkSections) {
                            String[] dirtyChunkSection = dirtyChunk.split(",");
                            if (dirtyChunkSection.length == 2) {
                                builder.setDirtyChunk(worldSections[0],
                                        Integer.parseInt(dirtyChunkSection[0]), Integer.parseInt(dirtyChunkSection[1]));
                            }
                        }
                    }
                }
            } else {
                String[] dirtyChunkSections = dirtyChunks.split(";");
                for (String dirtyChunk : dirtyChunkSections) {
                    String[] dirtyChunkSection = dirtyChunk.split(",");
                    if (dirtyChunkSection.length == 3) {
                        builder.setDirtyChunk(dirtyChunkSection[0],
                                Integer.parseInt(dirtyChunkSection[1]), Integer.parseInt(dirtyChunkSection[2]));
                    }
                }
            }
        }
    }

    public static void deserializeBlockCounts(Island.Builder builder, String blocks) {
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
            Key blockKey = Keys.ofMaterialAndData(blockCountObject.get("id").getAsString());
            BigInteger amount = new BigInteger(blockCountObject.get("amount").getAsString());
            builder.setBlockCount(blockKey, amount);
        });
    }

    public static void deserializeBlockLimits(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_block_limits", blockLimitRow -> {
            DatabaseResult blockLimits = new DatabaseResult(blockLimitRow);

            Optional<UUID> uuid = blockLimits.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load block limits for null islands, skipping...");
                return;
            }

            Optional<Key> block = blockLimits.getString("block").map(Keys::ofMaterialAndData);
            if (!block.isPresent()) {
                Log.warn("Cannot load block limits for invalid blocks for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> limit = blockLimits.getInt("limit");
            if (!limit.isPresent()) {
                Log.warn("Cannot load block limits with invalid limits for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setBlockLimit(block.get(), limit.get());
        });
    }

    public static void deserializeEntityLimits(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_entity_limits", entityLimitsRow -> {
            DatabaseResult entityLimits = new DatabaseResult(entityLimitsRow);

            Optional<UUID> uuid = entityLimits.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load entity limits for null islands, skipping...");
                return;
            }

            Optional<Key> entity = entityLimits.getString("entity").map(Keys::ofEntityType);
            if (!entity.isPresent()) {
                Log.warn("Cannot load entity limits for invalid entities on ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> limit = entityLimits.getInt("limit");
            if (!limit.isPresent()) {
                Log.warn("Cannot load entity limits with invalid limits for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setEntityLimit(entity.get(), limit.get());
        });
    }

    public static void deserializeRatings(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_ratings", ratingsRow -> {
            DatabaseResult ratings = new DatabaseResult(ratingsRow);

            Optional<UUID> islandUUID = ratings.getUUID("island");
            if (!islandUUID.isPresent()) {
                Log.warn("Cannot load ratings for null islands, skipping...");
                return;
            }

            Optional<UUID> uuid = ratings.getUUID("player");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load ratings with invalid players for ", islandUUID.get(), ", skipping...");
                return;
            }

            SuperiorPlayer ratingPlayer = plugin.getPlayers().getSuperiorPlayer(uuid.get(), false);
            if (ratingPlayer == null) {
                Log.warn("Cannot load island rating with unrecognized uuid: " + uuid.get() + ", skipping...");
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
                Log.warn("Cannot load ratings with invalid rating value for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(islandUUID.get(), IslandBuilderImpl::new);
            builder.setRating(ratingPlayer, rating.get());
        });
    }

    public static void deserializeMissions(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_missions", missionsRow -> {
            DatabaseResult missions = new DatabaseResult(missionsRow);

            Optional<UUID> uuid = missions.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island missions for null islands, skipping...");
                return;
            }

            Optional<String> missionName = missions.getString("name");
            Optional<Mission<?>> mission = missionName.map(plugin.getMissions()::getMission);
            if (!mission.isPresent()) {
                if (!missionName.isPresent()) {
                    Log.warn("Cannot load island missions with invalid missions for ", uuid.get(), ", skipping...");
                } else {
                    Log.warn("Cannot load island missions with invalid mission ",
                            missionName.get(), " for ", uuid.get(), ", skipping...");
                }
                return;
            }

            Optional<Integer> finishCount = missions.getInt("finish_count");
            if (!finishCount.isPresent()) {
                Log.warn("Cannot load island missions with invalid finish count for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setCompletedMission(mission.get(), finishCount.get());
        });
    }

    public static void deserializeIslandFlags(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_flags", islandFlagRow -> {
            DatabaseResult islandFlagResult = new DatabaseResult(islandFlagRow);

            Optional<UUID> uuid = islandFlagResult.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island flags for null islands, skipping...");
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
                Log.warn("Cannot load island flags with invalid flags for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Byte> status = islandFlagResult.getByte("status");
            if (!status.isPresent()) {
                Log.warn("Cannot load island flags with invalid status for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setIslandFlag(islandFlag.get(), status.get() == 1);
        });
    }

    public static void deserializeGenerators(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_generators", generatorsRow -> {
            DatabaseResult generators = new DatabaseResult(generatorsRow);

            Optional<UUID> uuid = generators.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load generator rates for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = generators.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                Log.warn("Cannot load generator rates with invalid environment for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Key> block = generators.getString("block").map(Keys::ofMaterialAndData);
            if (!block.isPresent()) {
                Log.warn("Cannot load generator rates with invalid block for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> rate = generators.getInt("rate");
            if (!rate.isPresent()) {
                Log.warn("Cannot load generator rates with invalid rate for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setGeneratorRate(block.get(), rate.get(), World.Environment.values()[environment.get()]);
        });
    }

    public static void deserializeIslandHomes(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_homes", islandHomesRow -> {
            DatabaseResult islandHomes = new DatabaseResult(islandHomesRow);

            Optional<UUID> uuid = islandHomes.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island homes for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = islandHomes.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                Log.warn("Cannot load island homes with invalid environment for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Location> location = islandHomes.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                Log.warn("Cannot load island homes with invalid location for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setIslandHome(location.get(), World.Environment.values()[environment.get()]);
        });
    }

    public static void deserializeVisitorHomes(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_visitor_homes", islandVisitorHomesRow -> {
            DatabaseResult islandVisitorHomes = new DatabaseResult(islandVisitorHomesRow);

            Optional<UUID> uuid = islandVisitorHomes.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island homes for null islands, skipping...");
                return;
            }

            Optional<Integer> environment = islandVisitorHomes.getEnum("environment", World.Environment.class)
                    .map(Enum::ordinal);
            if (!environment.isPresent()) {
                Log.warn("Cannot load island homes with invalid environment for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Location> location = islandVisitorHomes.getString("location").map(Serializers.LOCATION_SERIALIZER::deserialize);
            if (!location.isPresent()) {
                Log.warn("Cannot load island homes with invalid location for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setVisitorHome(location.get(), World.Environment.values()[environment.get()]);
        });
    }

    public static void deserializeEffects(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_effects", islandEffectRow -> {
            DatabaseResult islandEffects = new DatabaseResult(islandEffectRow);

            Optional<UUID> uuid = islandEffects.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island effects for null islands, skipping...");
                return;
            }

            Optional<PotionEffectType> effectType = islandEffects.getString("effect_type")
                    .map(PotionEffectType::getByName);
            if (!effectType.isPresent()) {
                Log.warn("Cannot load island effects with invalid effect for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> level = islandEffects.getInt("level");
            if (!level.isPresent()) {
                Log.warn("Cannot load island effects with invalid level for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setIslandEffect(effectType.get(), level.get());
        });
    }

    public static void deserializeIslandChest(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_chests", islandChestsRow -> {
            DatabaseResult islandChests = new DatabaseResult(islandChestsRow);

            Optional<UUID> uuid = islandChests.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island chests for null islands, skipping...");
                return;
            }

            Optional<Integer> index = islandChests.getInt("index");
            if (!index.isPresent() || index.get() < 0) {
                Log.warn("Cannot load island chest with invalid index for ", uuid.get(), ", skipping...");
                return;
            }

            Optional<ItemStack[]> contents = islandChests.getBlob("contents").map(Serializers.INVENTORY_SERIALIZER::deserialize);
            if (!contents.isPresent()) {
                Log.warn("Cannot load island chest with invalid contents for ", uuid.get(), ", skipping...");
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

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setIslandChest(index.get(), chestContents);
        });
    }

    public static void deserializeRoleLimits(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_role_limits", roleLimitRaw -> {
            DatabaseResult roleLimits = new DatabaseResult(roleLimitRaw);

            Optional<UUID> uuid = roleLimits.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load role limits for null islands, skipping...");
                return;
            }

            Optional<PlayerRole> playerRole = roleLimits.getInt("role").map(SPlayerRole::fromId);
            if (!playerRole.isPresent()) {
                Log.warn("Cannot load role limit for invalid role on ", uuid.get(), ", skipping...");
                return;
            }

            Optional<Integer> limit = roleLimits.getInt("limit");
            if (!limit.isPresent()) {
                Log.warn("Cannot load role limit for invalid limit on ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setRoleLimit(playerRole.get(), limit.get());
        });
    }

    public static void deserializeWarpCategories(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_warp_categories", warpCategoryRow -> {
            DatabaseResult warpCategory = new DatabaseResult(warpCategoryRow);

            Optional<UUID> uuid = warpCategory.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load warp categories for null islands, skipping...");
                return;
            }

            Optional<String> name = warpCategory.getString("name").map(Formatters.STRIP_COLOR_FORMATTER::format);
            if (!name.isPresent() || name.get().isEmpty()) {
                Log.warn("Cannot load warp categories with invalid name for ", uuid.get(), ", skipping...");
                return;
            }

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.addWarpCategory(name.get(), warpCategory.getInt("slot").orElse(-1),
                    warpCategory.getString("icon").map(Serializers.ITEM_STACK_SERIALIZER::deserialize).orElse(null));
        });
    }

    public static void deserializeIslandBank(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_banks", islandBankRow -> {
            DatabaseResult islandBank = new DatabaseResult(islandBankRow);

            Optional<UUID> uuid = islandBank.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load island banks for null islands, skipping...");
                return;
            }

            Optional<BigDecimal> balance = islandBank.getBigDecimal("balance");
            if (!balance.isPresent()) {
                Log.warn("Cannot load island banks with invalid balance for ", uuid.get(), ", skipping...");
                return;
            }

            long currentTime = System.currentTimeMillis() / 1000;

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setBalance(balance.get());
            long lastInterestTime = islandBank.getLong("last_interest_time").orElse(currentTime);
            builder.setLastInterestTime(lastInterestTime > currentTime ? lastInterestTime / 1000 : lastInterestTime);
        });
    }

    public static void deserializeIslandSettings(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_settings", islandSettingsRow -> {
            DatabaseResult islandSettings = new DatabaseResult(islandSettingsRow);

            Optional<String> island = islandSettings.getString("island");
            if (!island.isPresent()) {
                Log.warn("Cannot load island settings of null island, skipping ");
                return;
            }

            UUID uuid = UUID.fromString(island.get());
            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid, IslandBuilderImpl::new);

            builder.setIslandSize(islandSettings.getInt("size").orElse(-1));
            builder.setTeamLimit(islandSettings.getInt("members_limit").orElse(-1));
            builder.setWarpsLimit(islandSettings.getInt("warps_limit").orElse(-1));
            builder.setCropGrowth(islandSettings.getDouble("crop_growth_multiplier").orElse(-1D));
            builder.setSpawnerRates(islandSettings.getDouble("spawner_rates_multiplier").orElse(-1D));
            builder.setMobDrops(islandSettings.getDouble("mob_drops_multiplier").orElse(-1D));
            builder.setCoopLimit(islandSettings.getInt("coops_limit").orElse(-1));
            builder.setBankLimit(islandSettings.getBigDecimal("bank_limit").orElse(SYNCED_BANK_LIMIT_VALUE));
        });
    }

    public static void deserializeBankTransactions(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        if (BuiltinModules.BANK.bankLogs && BuiltinModules.BANK.cacheAllLogs) {
            databaseBridge.loadAllObjects("bank_transactions", bankTransactionRow -> {
                DatabaseResult bankTransaction = new DatabaseResult(bankTransactionRow);

                Optional<UUID> uuid = bankTransaction.getUUID("island");
                if (!uuid.isPresent()) {
                    Log.warn("Cannot load bank transaction for null islands, skipping...");
                    return;
                }

                Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
                SBankTransaction.fromDatabase(bankTransaction).ifPresent(builder::addBankTransaction);
            });
        }
    }

    public static void deserializePersistentDataContainer(DatabaseBridge databaseBridge, DatabaseCache<Island.Builder> databaseCache) {
        databaseBridge.loadAllObjects("islands_custom_data", customDataRow -> {
            DatabaseResult customData = new DatabaseResult(customDataRow);

            Optional<UUID> uuid = customData.getUUID("island");
            if (!uuid.isPresent()) {
                Log.warn("Cannot load custom data for null islands, skipping...");
                return;
            }

            byte[] persistentData = customData.getBlob("data").orElse(new byte[0]);

            if (persistentData.length == 0)
                return;

            Island.Builder builder = databaseCache.computeIfAbsentInfo(uuid.get(), IslandBuilderImpl::new);
            builder.setPersistentData(persistentData);
        });
    }
}
