package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.LegacyMasks;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.database.DBColumn;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsSerializer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.chunk.DirtyChunksContainer;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class IslandsDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<UUID, Map<FutureSave, Set<Object>>> SAVE_METHODS_TO_BE_EXECUTED = new ConcurrentHashMap<>();

    private IslandsDatabaseBridge() {
    }

    public static void addMember(Island island, SuperiorPlayer superiorPlayer, long addTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_members",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("role", superiorPlayer.getPlayerRole().getId()),
                        pool.obtain().withNameAndValue("join_time", addTime)
                );
            }
        });
    }

    public static void removeMember(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.deleteObject("islands_members",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString())));
            }
        });
    }

    public static void saveMemberRole(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_members",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString())),
                        pool.obtain().withNameAndValue("role", superiorPlayer.getPlayerRole().getId())
                );
            }
        });
    }

    public static void addBannedPlayer(Island island, SuperiorPlayer superiorPlayer, UUID banner, long banTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_bans",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("banned_by", banner.toString()),
                        pool.obtain().withNameAndValue("banned_time", banTime)
                );
            }
        });
    }

    public static void removeBannedPlayer(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString());
                databaseBridge.deleteObject("islands_bans", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveCoopLimit(Island island) {
        updateIslandSettingsValue(island, "coops_limit", island.getCoopLimit());
    }

    public static void saveIslandHome(Island island, Dimension dimension, @Nullable WorldPosition worldPosition) {
        if (worldPosition == null) {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
                try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                    DBColumn column = pool.obtain().withNameAndValue("environment", dimension.getName());
                    databaseBridge.deleteObject("islands_homes", createFilter(pool, "island", island, column));
                }
            });
        } else {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
                try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                    databaseBridge.insertObject("islands_homes",
                            pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                            pool.obtain().withNameAndValue("environment", dimension.getName()),
                            pool.obtain().withNameAndValue("location", Serializers.WORLD_POSITION_SERIALIZER.serialize(worldPosition))
                    );
                }
            });
        }
    }

    public static void saveVisitorLocation(Island island, Dimension dimension, @Nullable WorldPosition worldPosition) {
        if (worldPosition == null) {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
                try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                    DBColumn column = pool.obtain().withNameAndValue("environment", dimension.getName());
                    databaseBridge.deleteObject("islands_visitor_homes", createFilter(pool, "island", island, column));
                }
            });
        } else {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
                try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                    databaseBridge.insertObject("islands_visitor_homes",
                            pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                            pool.obtain().withNameAndValue("environment", dimension.getName()),
                            pool.obtain().withNameAndValue("location", Serializers.WORLD_POSITION_SERIALIZER.serialize(worldPosition))
                    );
                }
            });
        }
    }

    public static void saveUnlockedWorlds(Island island) {
        updateIslandValue(island, "unlocked_worlds",
                LegacyMasks.convertUnlockedWorldsMask(island.getUnlockedWorlds()));
    }

    public static void savePlayerPermission(Island island, SuperiorPlayer superiorPlayer, IslandPrivilege privilege,
                                            boolean status) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_player_permissions",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("permission", privilege.getName()),
                        pool.obtain().withNameAndValue("status", status)
                );
            }
        });
    }

    public static void clearPlayerPermission(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString());
                databaseBridge.deleteObject("islands_player_permissions", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveRolePermission(Island island, PlayerRole playerRole, IslandPrivilege privilege) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_role_permissions",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("role", playerRole.getId()),
                        pool.obtain().withNameAndValue("permission", privilege.getName())
                );
            }
        });
    }

    public static void clearRolePermissions(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_role_permissions", createFilter("island", island)));
    }

    public static void saveName(Island island) {
        updateIslandValue(island, "name", IslandNames.getNameForDatabase(island));
    }

    public static void saveDescription(Island island) {
        updateIslandValue(island, "description", island.getDescription());
    }

    public static void saveSize(Island island) {
        updateIslandSettingsValue(island, "size", island.getIslandSize());
    }

    public static void saveDiscord(Island island) {
        updateIslandValue(island, "discord", island.getDiscord());
    }

    public static void savePaypal(Island island) {
        updateIslandValue(island, "paypal", island.getPaypal());
    }

    public static void saveLockedStatus(Island island) {
        updateIslandValue(island, "locked", island.isLocked());
    }

    public static void saveIgnoredStatus(Island island) {
        updateIslandValue(island, "ignored", island.isIgnored());
    }

    public static void saveLastTimeUpdate(Island island) {
        updateIslandValue(island, "last_time_updated", island.getLastTimeUpdate());
    }

    public static void saveBankLimit(Island island) {
        updateIslandSettingsValue(island, "bank_limit", island.getBankLimit() + "");
    }

    public static void saveBonusWorth(Island island) {
        updateIslandValue(island, "worth_bonus", island.getBonusWorth() + "");
    }

    public static void saveBonusLevel(Island island) {
        updateIslandValue(island, "levels_bonus", island.getBonusLevel() + "");
    }

    public static void saveUpgrade(Island island, Upgrade upgrade, int level) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_upgrades",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("upgrade", upgrade.getName()),
                        pool.obtain().withNameAndValue("level", level)
                );
            }
        });
    }

    public static void saveCropGrowth(Island island) {
        updateIslandSettingsValue(island, "crop_growth_multiplier", island.getCropGrowthMultiplier());
    }

    public static void saveSpawnerRates(Island island) {
        updateIslandSettingsValue(island, "spawner_rates_multiplier", island.getSpawnerRatesMultiplier());
    }

    public static void saveMobDrops(Island island) {
        updateIslandSettingsValue(island, "mob_drops_multiplier", island.getMobDropsMultiplier());
    }

    public static void saveBlockLimit(Island island, Key block, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_block_limits",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("block", block.toString()),
                        pool.obtain().withNameAndValue("limit", limit)
                );
            }
        });
    }

    public static void clearBlockLimits(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_block_limits", createFilter("island", island)));
    }

    public static void removeBlockLimit(Island island, Key block) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("block", block.toString());
                databaseBridge.deleteObject("islands_block_limits", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveEntityLimit(Island island, Key entityType, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_entity_limits",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("entity", entityType.toString()),
                        pool.obtain().withNameAndValue("limit", limit)
                );
            }
        });
    }

    public static void clearEntityLimits(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_entity_limits", createFilter("island", island)));
    }

    public static void removeEntityLimit(Island island, Key entity) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("entity", entity.toString());
                databaseBridge.deleteObject("islands_entity_limits", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveTeamLimit(Island island) {
        updateIslandSettingsValue(island, "members_limit", island.getTeamLimit());
    }

    public static void saveWarpsLimit(Island island) {
        updateIslandSettingsValue(island, "warps_limit", island.getWarpsLimit());
    }

    public static void saveIslandEffect(Island island, PotionEffectType potionEffectType, int level) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_effects",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("effect_type", potionEffectType.getName()),
                        pool.obtain().withNameAndValue("level", level)
                );
            }
        });
    }

    public static void removeIslandEffect(Island island, PotionEffectType potionEffectType) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("effect_type", potionEffectType.getName());
                databaseBridge.deleteObject("islands_effects", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void clearIslandEffects(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_effects", createFilter("island", island)));
    }

    public static void saveRoleLimit(Island island, PlayerRole playerRole, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_role_limits",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("role", playerRole.getId()),
                        pool.obtain().withNameAndValue("limit", limit)
                );
            }
        });
    }

    public static void removeRoleLimit(Island island, PlayerRole playerRole) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("role", playerRole.getId());
                databaseBridge.deleteObject("islands_role_limits", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveWarp(Island island, IslandWarp islandWarp) {
        WarpCategory category = islandWarp.getCategory();
        ItemStack icon = islandWarp.getRawIcon();
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain();
                 ObjectsPools.Wrapper<LazyWorldLocation> wrapper = ObjectsPools.LAZY_LOCATION.obtain()) {
                databaseBridge.insertObject("islands_warps",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("name", islandWarp.getName()),
                        pool.obtain().withNameAndValue("category", category == null ? "" : category.getName()),
                        pool.obtain().withNameAndValue("location", Serializers.LOCATION_SERIALIZER.serialize(islandWarp.getLocation(wrapper.getHandle()))),
                        pool.obtain().withNameAndValue("private", islandWarp.hasPrivateFlag()),
                        pool.obtain().withNameAndValue("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(icon))
                );
            }
        });
    }

    public static void updateWarpName(Island island, IslandWarp islandWarp, String oldName) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warps",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", oldName)),
                        pool.obtain().withNameAndValue("name", islandWarp.getName())
                );
            }
        });
    }

    public static void updateWarpLocation(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain();
                 ObjectsPools.Wrapper<LazyWorldLocation> wrapper = ObjectsPools.LAZY_LOCATION.obtain()) {
                String islandWarpLocation = Serializers.LOCATION_SERIALIZER.serialize(islandWarp.getLocation(wrapper.getHandle()));
                databaseBridge.updateObject("islands_warps",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", islandWarp.getName())),
                        pool.obtain().withNameAndValue("location", islandWarpLocation)
                );
            }
        });
    }

    public static void updateWarpPrivateStatus(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warps",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", islandWarp.getName())),
                        pool.obtain().withNameAndValue("private", islandWarp.hasPrivateFlag())
                );
            }
        });
    }

    public static void updateWarpIcon(Island island, IslandWarp islandWarp) {

        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            ItemStack icon = islandWarp.getRawIcon();
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warps",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", islandWarp.getName())),
                        pool.obtain().withNameAndValue("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(icon))
                );
            }
        });
    }

    public static void removeWarp(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("name", islandWarp.getName());
                databaseBridge.deleteObject("islands_warps", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveRating(Island island, SuperiorPlayer superiorPlayer, Rating rating, long rateTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_ratings",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("rating", rating.getValue()),
                        pool.obtain().withNameAndValue("rating_time", rateTime)
                );
            }
        });
    }

    public static void removeRating(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("player", superiorPlayer.getUniqueId().toString());
                databaseBridge.deleteObject("islands_ratings", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void clearRatings(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_ratings", createFilter("island", island)));
    }

    public static void saveMission(Island island, Mission<?> mission, int finishCount) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_missions",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("name", mission.getName().toLowerCase(Locale.ENGLISH)),
                        pool.obtain().withNameAndValue("finish_count", finishCount)
                );
            }
        });
    }

    public static void removeMission(Island island, Mission<?> mission) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("name", mission.getName());
                databaseBridge.deleteObject("islands_missions", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveIslandFlag(Island island, IslandFlag islandFlag, int status) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_flags",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("name", islandFlag.getName()),
                        pool.obtain().withNameAndValue("status", status)
                );
            }
        });
    }

    public static void removeIslandFlag(Island island, IslandFlag islandFlag) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("name", islandFlag.getName());
                databaseBridge.deleteObject("islands_flags", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void clearIslandFlags(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_flags", createFilter("island", island)));
    }

    public static void saveGeneratorRate(Island island, Dimension dimension, Key blockKey, int rate) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_generators",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("environment", dimension.getName()),
                        pool.obtain().withNameAndValue("block", blockKey.toString()),
                        pool.obtain().withNameAndValue("rate", rate)
                );
            }
        });
    }

    public static void removeGeneratorRate(Island island, Dimension dimension, Key blockKey) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.deleteObject("islands_generators", createFilter(pool, "island", island,
                        pool.obtain().withNameAndValue("environment", dimension.getName()),
                        pool.obtain().withNameAndValue("block", blockKey.toString()))
                );
            }
        });
    }

    public static void clearGeneratorRates(Island island, Dimension dimension) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("environment", dimension.getName());
                databaseBridge.deleteObject("islands_generators", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveGeneratedSchematics(Island island) {
        updateIslandValue(island, "generated_schematics",
                LegacyMasks.convertGeneratedSchematicsMask(island.getGeneratedSchematics()));
    }

    public static void saveDirtyChunks(DirtyChunksContainer dirtyChunksContainer) {
        updateIslandValue(dirtyChunksContainer.getIsland(), "dirty_chunks",
                IslandsSerializer.serializeDirtyChunkPositions(dirtyChunksContainer));
    }

    public static void saveBlockCounts(Island island) {
        updateIslandValue(island, "block_counts",
                IslandsSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()));
    }

    public static void saveEntityCounts(Island island) {
        updateIslandValue(island, "entity_counts",
                IslandsSerializer.serializeEntityCounts(island.getEntitiesTracker().getEntitiesCounts()));
    }

    public static void saveIslandChest(Island island, IslandChest islandChest) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_chests",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("index", islandChest.getIndex()),
                        pool.obtain().withNameAndValue("contents", Serializers.INVENTORY_SERIALIZER.serialize(islandChest.getContents()))
                );
            }
        });
    }

    public static void saveLastInterestTime(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue("last_interest_time", island.getLastInterestTime() * 1000);
                databaseBridge.updateObject("islands_banks", createFilter("island", island), column);
            }
        });
    }

    public static void saveVisitor(Island island, SuperiorPlayer visitor, long visitTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_visitors",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", visitor.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("visit_time", visitTime)
                );
            }
        });
    }

    public static void saveWarpCategory(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_warp_categories",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("name", warpCategory.getName()),
                        pool.obtain().withNameAndValue("slot", warpCategory.getSlot()),
                        pool.obtain().withNameAndValue("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(warpCategory.getRawIcon()))
                );
            }
        });
    }

    public static void updateWarpCategory(Island island, IslandWarp islandWarp, String oldCategoryName) {
        WarpCategory category = islandWarp.getCategory();
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warps",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("category", oldCategoryName)),
                        pool.obtain().withNameAndValue("category", category == null ? "" : category.getName())
                );
            }
        });
    }

    public static void updateWarpCategoryName(Island island, WarpCategory warpCategory, String oldName) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warp_categories",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", oldName)),
                        pool.obtain().withNameAndValue("name", warpCategory.getName())
                );
            }
        });
    }

    public static void updateWarpCategorySlot(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warp_categories",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", warpCategory.getName())),
                        pool.obtain().withNameAndValue("slot", warpCategory.getSlot())
                );
            }
        });
    }

    public static void updateWarpCategoryIcon(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_warp_categories",
                        createFilter(pool, "island", island, pool.obtain().withNameAndValue("name", warpCategory.getName())),
                        pool.obtain().withNameAndValue("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(warpCategory.getRawIcon()))
                );
            }
        });
    }

    public static void removeWarpCategory(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                DBColumn column = pool.obtain().withNameAndValue("name", warpCategory.getName());
                databaseBridge.deleteObject("islands_warp_categories", createFilter(pool, "island", island, column));
            }
        });
    }

    public static void saveIslandLeader(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue("owner", island.getOwner().getUniqueId().toString());
                databaseBridge.updateObject("islands", createFilter("uuid", island), column);
            }
        });
    }

    public static void saveBankBalance(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue("balance", island.getIslandBank().getBalance() + "");
                databaseBridge.updateObject("islands_banks", createFilter("island", island), column);
            }
        });
    }

    public static void saveBankTransaction(Island island, BankTransaction bankTransaction) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("bank_transactions",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("player", bankTransaction.getPlayer() == null ? "" : bankTransaction.getPlayer().toString()),
                        pool.obtain().withNameAndValue("bank_action", bankTransaction.getAction().name()),
                        pool.obtain().withNameAndValue("position", bankTransaction.getPosition()),
                        pool.obtain().withNameAndValue("time", bankTransaction.getTime()),
                        pool.obtain().withNameAndValue("failure_reason", bankTransaction.getFailureReason()),
                        pool.obtain().withNameAndValue("amount", bankTransaction.getAmount() + "")
                );
            }
        });
    }

    public static void savePersistentDataContainer(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_custom_data",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("data", island.getPersistentDataContainer().serialize())
                );
            }
        });
    }

    public static void removePersistentDataContainer(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_custom_data", createFilter("island", island)));
    }

    public static void clearIslandSettings(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.updateObject("islands_settings",
                        createFilter("island", island),
                        pool.obtain().withNameAndValue("size", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("bank_limit", IslandUpgradeConstants.SYNCED_BANK_LIMIT_VALUE.toString()),
                        pool.obtain().withNameAndValue("coops_limit", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("members_limit", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("warps_limit", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("crop_growth_multiplier", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("spawner_rates_multiplier", IslandUpgradeConstants.SYNCED_VALUE),
                        pool.obtain().withNameAndValue("mob_drops_multiplier", IslandUpgradeConstants.SYNCED_VALUE)
                );
            }
        });
    }

    public static void insertIsland(Island island, @Nullable List<ChunkPosition> dirtyChunks) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            BlockPosition centerPosition = island.getCenterPosition();
            WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, Dimensions.NORMAL);
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands",
                        pool.obtain().withNameAndValue("uuid", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("owner", island.getOwner().getUniqueId().toString()),
                        pool.obtain().withNameAndValue("center", Serializers.LOCATION_SERIALIZER.serialize(LazyWorldLocation.of(worldInfo, centerPosition))),
                        pool.obtain().withNameAndValue("creation_time", island.getCreationTime()),
                        pool.obtain().withNameAndValue("island_type", island.getSchematicName()),
                        pool.obtain().withNameAndValue("discord", island.getDiscord()),
                        pool.obtain().withNameAndValue("paypal", island.getPaypal()),
                        pool.obtain().withNameAndValue("worth_bonus", island.getBonusWorth() + ""),
                        pool.obtain().withNameAndValue("levels_bonus", island.getBonusLevel() + ""),
                        pool.obtain().withNameAndValue("locked", island.isLocked()),
                        pool.obtain().withNameAndValue("ignored", island.isIgnored()),
                        pool.obtain().withNameAndValue("name", IslandNames.getNameForDatabase(island)),
                        pool.obtain().withNameAndValue("description", island.getDescription()),
                        pool.obtain().withNameAndValue("generated_schematics", LegacyMasks.convertGeneratedSchematicsMask(island.getGeneratedSchematics())),
                        pool.obtain().withNameAndValue("unlocked_worlds", LegacyMasks.convertUnlockedWorldsMask(island.getUnlockedWorlds())),
                        pool.obtain().withNameAndValue("last_time_updated", System.currentTimeMillis() / 1000L),
                        pool.obtain().withNameAndValue("dirty_chunks", dirtyChunks == null ? "" : IslandsSerializer.serializeDirtyChunkPositions(dirtyChunks)),
                        pool.obtain().withNameAndValue("block_counts", IslandsSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger())),
                        pool.obtain().withNameAndValue("entity_counts", IslandsSerializer.serializeEntityCounts(island.getEntitiesTracker().getEntitiesCounts()))
                );
            }
        });

        insertIslandBanks(island);
        insertIslandSettings(island);
    }

    public static void insertIslandBanks(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_banks",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("balance", island.getIslandBank().getBalance() + ""),
                        pool.obtain().withNameAndValue("last_interest_time", island.getLastInterestTime())
                );
            }
        });
    }

    public static void insertIslandSettings(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("islands_settings",
                        pool.obtain().withNameAndValue("island", island.getUniqueId().toString()),
                        pool.obtain().withNameAndValue("size", island.getIslandSizeRaw()),
                        pool.obtain().withNameAndValue("bank_limit", island.getBankLimitRaw() + ""),
                        pool.obtain().withNameAndValue("coops_limit", island.getCoopLimitRaw()),
                        pool.obtain().withNameAndValue("members_limit", island.getTeamLimitRaw()),
                        pool.obtain().withNameAndValue("warps_limit", island.getWarpsLimitRaw()),
                        pool.obtain().withNameAndValue("crop_growth_multiplier", island.getCropGrowthRaw()),
                        pool.obtain().withNameAndValue("spawner_rates_multiplier", island.getSpawnerRatesRaw()),
                        pool.obtain().withNameAndValue("mob_drops_multiplier", island.getMobDropsRaw())
                );
            }
        });
    }

    public static void deleteIsland(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            DatabaseFilter islandFilter = createFilter("island", island);

            databaseBridge.deleteObject("islands", createFilter("uuid", island));
            databaseBridge.deleteObject("islands_banks", islandFilter);
            databaseBridge.deleteObject("islands_settings", islandFilter);
            databaseBridge.deleteObject("bank_transactions", islandFilter);


            if (!island.getBannedPlayers().isEmpty())
                databaseBridge.deleteObject("islands_bans", islandFilter);
            if (!island.getBlocksLimits().isEmpty())
                databaseBridge.deleteObject("islands_block_limits", islandFilter);
            if (!island.isPersistentDataContainerEmpty())
                databaseBridge.deleteObject("islands_custom_data", islandFilter);
            if (island.getChest().length > 0)
                databaseBridge.deleteObject("islands_chests", islandFilter);
            if (!island.getPotionEffects().isEmpty())
                databaseBridge.deleteObject("islands_effects", islandFilter);
            if (!island.getEntitiesLimitsAsKeys().isEmpty())
                databaseBridge.deleteObject("islands_entity_limits", islandFilter);
            if (!island.getAllSettings().isEmpty())
                databaseBridge.deleteObject("islands_flags", islandFilter);
            for (Dimension dimension : Dimension.values()) {
                if (!island.getCustomGeneratorAmounts(dimension).isEmpty()) {
                    databaseBridge.deleteObject("islands_generators", islandFilter);
                    break;
                }
            }
            if (!island.getIslandHomes().isEmpty())
                databaseBridge.deleteObject("islands_homes", islandFilter);
            if (!island.getIslandMembers(false).isEmpty())
                databaseBridge.deleteObject("islands_members", islandFilter);
            if (!island.getCompletedMissions().isEmpty())
                databaseBridge.deleteObject("islands_missions", islandFilter);
            if (!island.getPlayerPermissions().isEmpty())
                databaseBridge.deleteObject("islands_player_permissions", islandFilter);
            if (!island.getRatings().isEmpty())
                databaseBridge.deleteObject("islands_ratings", islandFilter);
            if (!island.getCustomRoleLimits().isEmpty())
                databaseBridge.deleteObject("islands_role_limits", islandFilter);
            if (!island.getRolePermissions().isEmpty())
                databaseBridge.deleteObject("islands_role_permissions", islandFilter);
            if (!island.getUpgrades().isEmpty())
                databaseBridge.deleteObject("islands_upgrades", islandFilter);
            for (Dimension dimension : Dimension.values()) {
                if (island.getVisitorsPosition(dimension) != null) {
                    databaseBridge.deleteObject("islands_visitor_homes", islandFilter);
                    break;
                }
            }
            if (!island.getUniqueVisitors().isEmpty())
                databaseBridge.deleteObject("islands_visitors", islandFilter);
            if (!island.getWarpCategories().isEmpty()) {
                databaseBridge.deleteObject("islands_warp_categories", islandFilter);
                databaseBridge.deleteObject("islands_warps", islandFilter);
            }
        });
    }

    public static void markIslandChestsToBeSaved(Island island, IslandChest islandChest) {
        SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(island.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.ISLAND_CHESTS, e -> new HashSet<>())
                .add(islandChest);
    }

    public static void markBlockCountsToBeSaved(Island island) {
        Set<Object> varsForBlockCounts = SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(island.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.BLOCK_COUNTS, e -> new HashSet<>());
        if (varsForBlockCounts.isEmpty())
            varsForBlockCounts.add(new Object());
    }

    public static void markPersistentDataContainerToBeSaved(Island island) {
        Set<Object> varsForPersistentData = SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(island.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.PERSISTENT_DATA, e -> new HashSet<>());
        if (varsForPersistentData.isEmpty())
            varsForPersistentData.add(new Object());
    }

    public static boolean isModified(Island island) {
        return SAVE_METHODS_TO_BE_EXECUTED.containsKey(island.getUniqueId());
    }

    public static void executeFutureSaves(Island island) {
        Map<FutureSave, Set<Object>> futureSaves = SAVE_METHODS_TO_BE_EXECUTED.remove(island.getUniqueId());
        if (futureSaves != null) {
            for (Map.Entry<FutureSave, Set<Object>> futureSaveEntry : futureSaves.entrySet()) {
                switch (futureSaveEntry.getKey()) {
                    case BLOCK_COUNTS:
                        saveBlockCounts(island);
                        break;
                    case ISLAND_CHESTS:
                        for (Object islandChest : futureSaveEntry.getValue())
                            saveIslandChest(island, (IslandChest) islandChest);
                        break;
                    case PERSISTENT_DATA:
                        savePersistentDataContainer(island);
                        break;
                }
            }
        }
    }

    public static void executeFutureSaves(Island island, FutureSave futureSave) {
        Map<FutureSave, Set<Object>> futureSaves = SAVE_METHODS_TO_BE_EXECUTED.get(island.getUniqueId());

        if (futureSaves == null)
            return;

        Set<Object> values = futureSaves.remove(futureSave);

        if (values == null)
            return;

        if (futureSaves.isEmpty())
            SAVE_METHODS_TO_BE_EXECUTED.remove(island.getUniqueId());

        switch (futureSave) {
            case BLOCK_COUNTS:
                saveBlockCounts(island);
                break;
            case ISLAND_CHESTS:
                for (Object islandChest : values)
                    saveIslandChest(island, (IslandChest) islandChest);
                break;
            case PERSISTENT_DATA: {
                if (island.isPersistentDataContainerEmpty())
                    removePersistentDataContainer(island);
                else
                    savePersistentDataContainer(island);
                break;
            }
        }
    }

    private static void updateIslandValue(Island island, String columnName, Object value) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue(columnName, value);
                databaseBridge.updateObject("islands", createFilter("uuid", island), column);
            }
        });
    }

    private static void updateIslandSettingsValue(Island island, String columnName, Object value) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue(columnName, value);
                databaseBridge.updateObject("islands_settings", createFilter("island", island), column);
            }
        });
    }

    private static DatabaseFilter createFilter(String id, Island island) {
        return DatabaseFilter.fromFilter(id, island.getUniqueId().toString());
    }

    private static DatabaseFilter createFilter(ObjectsPools.Batch<DBColumn> pool, String id, Island island, DBColumn column) {
        List<Pair<String, Object>> filters = new LinkedList<>();
        filters.add(pool.obtain().withNameAndValue(id, island.getUniqueId().toString()));
        filters.add(column);
        return DatabaseFilter.fromFilters(filters);
    }

    private static DatabaseFilter createFilter(ObjectsPools.Batch<DBColumn> pool, String id, Island island, DBColumn... others) {
        List<Pair<String, Object>> filters = new LinkedList<>();
        filters.add(pool.obtain().withNameAndValue(id, island.getUniqueId().toString()));
        if (others != null && others.length > 0)
            Collections.addAll(filters, others);
        return DatabaseFilter.fromFilters(filters);
    }

    private static void runOperationIfRunning(DatabaseBridge databaseBridge, Consumer<DatabaseBridge> databaseBridgeConsumer) {
        if (databaseBridge.getDatabaseBridgeMode() == DatabaseBridgeMode.SAVE_DATA)
            databaseBridgeConsumer.accept(databaseBridge);
    }

    public enum FutureSave {

        BLOCK_COUNTS,
        ISLAND_CHESTS,
        PERSISTENT_DATA

    }

}
