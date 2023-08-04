package com.bgsoftware.superiorskyblock.core.database.bridge;

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
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsSerializer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.island.chunk.DirtyChunksContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
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

@SuppressWarnings("unchecked")
public class IslandsDatabaseBridge {

    private static final Map<UUID, Map<FutureSave, Set<Object>>> SAVE_METHODS_TO_BE_EXECUTED = new ConcurrentHashMap<>();

    private IslandsDatabaseBridge() {
    }

    public static void addMember(Island island, SuperiorPlayer superiorPlayer, long addTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_members",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("role", superiorPlayer.getPlayerRole().getId()),
                new Pair<>("join_time", addTime)
        ));
    }

    public static void removeMember(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_members",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        ));
    }

    public static void saveMemberRole(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_members",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString())),
                new Pair<>("role", superiorPlayer.getPlayerRole().getId())
        ));
    }

    public static void addBannedPlayer(Island island, SuperiorPlayer superiorPlayer, UUID banner, long banTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_bans",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("banned_by", banner.toString()),
                new Pair<>("banned_time", banTime)
        ));
    }

    public static void removeBannedPlayer(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_bans",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        ));
    }

    public static void saveCoopLimit(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("coops_limit", island.getCoopLimit())
        ));
    }

    public static void saveIslandHome(Island island, World.Environment environment, Location location) {
        if (location == null) {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_homes",
                    createFilter("island", island, new Pair<>("environment", environment.name()))
            ));
        } else {
            runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_homes",
                    new Pair<>("island", island.getUniqueId().toString()),
                    new Pair<>("environment", environment.name()),
                    new Pair<>("location", Serializers.LOCATION_SERIALIZER.serialize(location))
            ));
        }
    }

    public static void saveVisitorLocation(Island island, World.Environment environment, Location location) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_visitor_homes",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("environment", environment.name()),
                new Pair<>("location", Serializers.LOCATION_SERIALIZER.serialize(location))
        ));
    }

    public static void removeVisitorLocation(Island island, World.Environment environment) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_visitor_homes",
                createFilter("island", island, new Pair<>("environment", environment.name()))
        ));
    }

    public static void saveUnlockedWorlds(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("unlocked_worlds", island.getUnlockedWorldsFlag())
        ));
    }

    public static void savePlayerPermission(Island island, SuperiorPlayer superiorPlayer, IslandPrivilege privilege,
                                            boolean status) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_player_permissions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("permission", privilege.getName()),
                new Pair<>("status", status)
        ));
    }

    public static void clearPlayerPermission(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_player_permissions",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        ));
    }

    public static void saveRolePermission(Island island, PlayerRole playerRole, IslandPrivilege privilege) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_role_permissions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("role", playerRole.getId()),
                new Pair<>("permission", privilege.getName())
        ));
    }

    public static void clearRolePermissions(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_role_permissions",
                createFilter("island", island)));
    }

    public static void saveName(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("name", island.getName())
        ));
    }

    public static void saveDescription(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("description", island.getDescription())
        ));
    }

    public static void saveSize(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("size", island.getIslandSize())
        ));
    }

    public static void saveDiscord(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("discord", island.getDiscord())
        ));
    }

    public static void savePaypal(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("paypal", island.getPaypal())
        ));
    }

    public static void saveLockedStatus(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("locked", island.isLocked())
        ));
    }

    public static void saveIgnoredStatus(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("ignored", island.isIgnored())
        ));
    }

    public static void saveLastTimeUpdate(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("last_time_updated", island.getLastTimeUpdate())
        ));
    }

    public static void saveBankLimit(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("bank_limit", island.getBankLimit() + "")
        ));
    }

    public static void saveBonusWorth(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("worth_bonus", island.getBonusWorth() + "")
        ));
    }

    public static void saveBonusLevel(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("levels_bonus", island.getBonusLevel() + "")
        ));
    }

    public static void saveUpgrade(Island island, Upgrade upgrade, int level) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_upgrades",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("upgrade", upgrade.getName()),
                new Pair<>("level", level)
        ));
    }

    public static void saveCropGrowth(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("crop_growth_multiplier", island.getCropGrowthMultiplier())
        ));
    }

    public static void saveSpawnerRates(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("spawner_rates_multiplier", island.getSpawnerRatesMultiplier())
        ));
    }

    public static void saveMobDrops(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("mob_drops_multiplier", island.getMobDropsMultiplier())
        ));
    }

    public static void saveBlockLimit(Island island, Key block, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_block_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("block", block.toString()),
                new Pair<>("limit", limit)
        ));
    }

    public static void clearBlockLimits(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_block_limits",
                createFilter("island", island)));
    }

    public static void removeBlockLimit(Island island, Key block) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_block_limits",
                createFilter("island", island, new Pair<>("block", block.toString()))
        ));
    }

    public static void saveEntityLimit(Island island, Key entityType, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_entity_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("entity", entityType.toString()),
                new Pair<>("limit", limit)
        ));
    }

    public static void clearEntityLimits(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_entity_limits",
                createFilter("island", island)));
    }

    public static void saveTeamLimit(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("members_limit", island.getTeamLimit())
        ));
    }

    public static void saveWarpsLimit(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("warps_limit", island.getWarpsLimit())
        ));
    }

    public static void saveIslandEffect(Island island, PotionEffectType potionEffectType, int level) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_effects",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("effect_type", potionEffectType.getName()),
                new Pair<>("level", level)
        ));
    }

    public static void removeIslandEffect(Island island, PotionEffectType potionEffectType) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_effects",
                createFilter("island", island, new Pair<>("effect_type", potionEffectType.getName()))
        ));
    }

    public static void clearIslandEffects(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_effects",
                createFilter("island", island)));
    }

    public static void saveRoleLimit(Island island, PlayerRole playerRole, int limit) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_role_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("role", playerRole.getId()),
                new Pair<>("limit", limit)
        ));
    }

    public static void removeRoleLimit(Island island, PlayerRole playerRole) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_role_limits",
                createFilter("island", island, new Pair<>("role", playerRole.getId()))
        ));
    }

    public static void saveWarp(Island island, IslandWarp islandWarp) {
        WarpCategory category = islandWarp.getCategory();
        ItemStack icon = islandWarp.getRawIcon();
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_warps",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", islandWarp.getName()),
                new Pair<>("category", category == null ? "" : category.getName()),
                new Pair<>("location", Serializers.LOCATION_SERIALIZER.serialize(islandWarp.getLocation())),
                new Pair<>("private", islandWarp.hasPrivateFlag()),
                new Pair<>("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(icon))
        ));
    }

    public static void updateWarpName(Island island, IslandWarp islandWarp, String oldName) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", oldName)),
                new Pair<>("name", islandWarp.getName())
        ));
    }

    public static void updateWarpLocation(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("location", Serializers.LOCATION_SERIALIZER.serialize(islandWarp.getLocation()))
        ));
    }

    public static void updateWarpPrivateStatus(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("private", islandWarp.hasPrivateFlag())
        ));
    }

    public static void updateWarpIcon(Island island, IslandWarp islandWarp) {
        ItemStack icon = islandWarp.getRawIcon();
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(icon))
        ));
    }

    public static void removeWarp(Island island, IslandWarp islandWarp) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName()))
        ));
    }

    public static void saveRating(Island island, SuperiorPlayer superiorPlayer, Rating rating, long rateTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_ratings",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("rating", rating.getValue()),
                new Pair<>("rating_time", rateTime)
        ));
    }

    public static void removeRating(Island island, SuperiorPlayer superiorPlayer) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_ratings",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        ));
    }

    public static void clearRatings(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_ratings",
                createFilter("island", island)
        ));
    }

    public static void saveMission(Island island, Mission<?> mission, int finishCount) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_missions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", mission.getName().toLowerCase(Locale.ENGLISH)),
                new Pair<>("finish_count", finishCount)
        ));
    }

    public static void removeMission(Island island, Mission<?> mission) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_missions",
                createFilter("island", island, new Pair<>("name", mission.getName()))
        ));
    }

    public static void saveIslandFlag(Island island, IslandFlag islandFlag, int status) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_flags",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", islandFlag.getName()),
                new Pair<>("status", status)
        ));
    }

    public static void removeIslandFlag(Island island, IslandFlag islandFlag) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_flags",
                createFilter("island", island, new Pair<>("name", islandFlag.getName()))
        ));
    }

    public static void saveGeneratorRate(Island island, World.Environment environment, Key blockKey, int rate) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_generators",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("environment", environment.name()),
                new Pair<>("block", blockKey.toString()),
                new Pair<>("rate", rate)
        ));
    }

    public static void removeGeneratorRate(Island island, World.Environment environment, Key blockKey) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_generators",
                createFilter("island", island,
                        new Pair<>("environment", environment.name()),
                        new Pair<>("block", blockKey.toString()))
        ));
    }

    public static void clearGeneratorRates(Island island, World.Environment environment) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_generators",
                createFilter("island", island, new Pair<>("environment", environment.name()))
        ));
    }

    public static void saveGeneratedSchematics(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("generated_schematics", island.getGeneratedSchematicsFlag())
        ));
    }

    public static void saveDirtyChunks(DirtyChunksContainer dirtyChunksContainer) {
        runOperationIfRunning(dirtyChunksContainer.getIsland().getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", dirtyChunksContainer.getIsland()),
                new Pair<>("dirty_chunks", IslandsSerializer.serializeDirtyChunkPositions(dirtyChunksContainer.getDirtyChunks()))
        ));
    }

    public static void saveBlockCounts(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("block_counts", IslandsSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
        ));
    }

    public static void saveIslandChest(Island island, IslandChest islandChest) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_chests",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("index", islandChest.getIndex()),
                new Pair<>("contents", Serializers.INVENTORY_SERIALIZER.serialize(islandChest.getContents()))
        ));
    }

    public static void saveLastInterestTime(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_banks",
                createFilter("island", island),
                new Pair<>("last_interest_time", island.getLastInterestTime() * 1000)
        ));
    }

    public static void saveVisitor(Island island, SuperiorPlayer visitor, long visitTime) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_visitors",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", visitor.getUniqueId().toString()),
                new Pair<>("visit_time", visitTime)
        ));
    }

    public static void saveWarpCategory(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_warp_categories",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", warpCategory.getName()),
                new Pair<>("slot", warpCategory.getSlot()),
                new Pair<>("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(warpCategory.getRawIcon()))
        ));
    }

    public static void updateWarpCategory(Island island, IslandWarp islandWarp, String oldCategoryName) {
        WarpCategory category = islandWarp.getCategory();
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warps",
                createFilter("island", island, new Pair<>("category", oldCategoryName)),
                new Pair<>("category", category == null ? "" : category.getName())
        ));
    }

    public static void updateWarpCategoryName(Island island, WarpCategory warpCategory, String oldName) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", oldName)),
                new Pair<>("name", warpCategory.getName())
        ));
    }

    public static void updateWarpCategorySlot(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName())),
                new Pair<>("slot", warpCategory.getSlot())
        ));
    }

    public static void updateWarpCategoryIcon(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName())),
                new Pair<>("icon", Serializers.ITEM_STACK_SERIALIZER.serialize(warpCategory.getRawIcon()))
        ));
    }

    public static void removeWarpCategory(Island island, WarpCategory warpCategory) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName()))
        ));
    }

    public static void saveIslandLeader(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("owner", island.getOwner().getUniqueId().toString())
        ));
    }

    public static void saveBankBalance(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.updateObject("islands_banks",
                createFilter("island", island),
                new Pair<>("balance", island.getIslandBank().getBalance() + "")
        ));
    }

    public static void saveBankTransaction(Island island, BankTransaction bankTransaction) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("bank_transactions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", bankTransaction.getPlayer() == null ? "" : bankTransaction.getPlayer().toString()),
                new Pair<>("bank_action", bankTransaction.getAction().name()),
                new Pair<>("position", bankTransaction.getPosition()),
                new Pair<>("time", bankTransaction.getTime()),
                new Pair<>("failure_reason", bankTransaction.getFailureReason()),
                new Pair<>("amount", bankTransaction.getAmount() + "")
        ));
    }

    public static void savePersistentDataContainer(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("islands_custom_data",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("data", island.getPersistentDataContainer().serialize())
        ));
    }

    public static void removePersistentDataContainer(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("islands_custom_data", createFilter("island", island)));
    }

    public static void insertIsland(Island island, List<ChunkPosition> dirtyChunks) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            databaseBridge.insertObject("islands",
                    new Pair<>("uuid", island.getUniqueId().toString()),
                    new Pair<>("owner", island.getOwner().getUniqueId().toString()),
                    new Pair<>("center", Serializers.LOCATION_SERIALIZER.serialize(island.getCenter(World.Environment.NORMAL))),
                    new Pair<>("creation_time", island.getCreationTime()),
                    new Pair<>("island_type", island.getSchematicName()),
                    new Pair<>("discord", island.getDiscord()),
                    new Pair<>("paypal", island.getPaypal()),
                    new Pair<>("worth_bonus", island.getBonusWorth() + ""),
                    new Pair<>("levels_bonus", island.getBonusLevel() + ""),
                    new Pair<>("locked", island.isLocked()),
                    new Pair<>("ignored", island.isIgnored()),
                    new Pair<>("name", island.getName()),
                    new Pair<>("description", island.getDescription()),
                    new Pair<>("generated_schematics", island.getGeneratedSchematicsFlag()),
                    new Pair<>("unlocked_worlds", island.getUnlockedWorldsFlag()),
                    new Pair<>("last_time_updated", System.currentTimeMillis() / 1000L),
                    new Pair<>("dirty_chunks", IslandsSerializer.serializeDirtyChunkPositions(dirtyChunks)),
                    new Pair<>("block_counts", IslandsSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
            );

            databaseBridge.insertObject("islands_banks",
                    new Pair<>("island", island.getUniqueId().toString()),
                    new Pair<>("balance", island.getIslandBank().getBalance() + ""),
                    new Pair<>("last_interest_time", island.getLastInterestTime())
            );

            databaseBridge.insertObject("islands_settings",
                    new Pair<>("island", island.getUniqueId().toString()),
                    new Pair<>("size", island.getIslandSizeRaw()),
                    new Pair<>("bank_limit", island.getBankLimitRaw() + ""),
                    new Pair<>("coops_limit", island.getCoopLimitRaw()),
                    new Pair<>("members_limit", island.getTeamLimitRaw()),
                    new Pair<>("warps_limit", island.getWarpsLimitRaw()),
                    new Pair<>("crop_growth_multiplier", island.getCropGrowthRaw()),
                    new Pair<>("spawner_rates_multiplier", island.getSpawnerRatesRaw()),
                    new Pair<>("mob_drops_multiplier", island.getMobDropsRaw())
            );
        });
    }

    public static void deleteIsland(Island island) {
        runOperationIfRunning(island.getDatabaseBridge(), databaseBridge -> {
            DatabaseFilter islandFilter = createFilter("island", island);
            databaseBridge.deleteObject("islands", createFilter("uuid", island));
            databaseBridge.deleteObject("islands_banks", islandFilter);
            databaseBridge.deleteObject("islands_bans", islandFilter);
            databaseBridge.deleteObject("islands_block_limits", islandFilter);
            databaseBridge.deleteObject("islands_custom_data", islandFilter);
            databaseBridge.deleteObject("islands_chests", islandFilter);
            databaseBridge.deleteObject("islands_effects", islandFilter);
            databaseBridge.deleteObject("islands_entity_limits", islandFilter);
            databaseBridge.deleteObject("islands_flags", islandFilter);
            databaseBridge.deleteObject("islands_generators", islandFilter);
            databaseBridge.deleteObject("islands_homes", islandFilter);
            databaseBridge.deleteObject("islands_members", islandFilter);
            databaseBridge.deleteObject("islands_missions", islandFilter);
            databaseBridge.deleteObject("islands_player_permissions", islandFilter);
            databaseBridge.deleteObject("islands_ratings", islandFilter);
            databaseBridge.deleteObject("islands_role_limits", islandFilter);
            databaseBridge.deleteObject("islands_role_permissions", islandFilter);
            databaseBridge.deleteObject("islands_settings", islandFilter);
            databaseBridge.deleteObject("islands_upgrades", islandFilter);
            databaseBridge.deleteObject("islands_visitor_homes", islandFilter);
            databaseBridge.deleteObject("islands_visitors", islandFilter);
            databaseBridge.deleteObject("islands_warp_categories", islandFilter);
            databaseBridge.deleteObject("islands_warps", islandFilter);
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

    private static DatabaseFilter createFilter(String id, Island island, Pair<String, Object>... others) {
        List<Pair<String, Object>> filters = new LinkedList<>();
        filters.add(new Pair<>(id, island.getUniqueId().toString()));
        if (others != null)
            filters.addAll(Arrays.asList(others));
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
