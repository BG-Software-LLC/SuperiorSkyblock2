package com.bgsoftware.superiorskyblock.database.bridge;

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
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public final class IslandsDatabaseBridge {

    private static final Map<UUID, Map<FutureSave, Set<Object>>> SAVE_METHODS_TO_BE_EXECUTED = new ConcurrentHashMap<>();

    private IslandsDatabaseBridge(){
    }

    public static void addMember(Island island, SuperiorPlayer superiorPlayer, long addTime) {
        island.getDatabaseBridge().insertObject("islands_members",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("role", superiorPlayer.getPlayerRole().getId()),
                new Pair<>("join_time", addTime)
        );
    }

    public static void removeMember(Island island, SuperiorPlayer superiorPlayer) {
        island.getDatabaseBridge().deleteObject("islands_members",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        );
    }

    public static void saveMemberRole(Island island, SuperiorPlayer superiorPlayer) {
        island.getDatabaseBridge().updateObject("islands_members",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString())),
                new Pair<>("role", superiorPlayer.getPlayerRole().getId())
        );
    }

    public static void addBannedPlayer(Island island, SuperiorPlayer superiorPlayer, UUID banner, long banTime) {
        island.getDatabaseBridge().insertObject("islands_bans",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("banned_by", banner.toString()),
                new Pair<>("banned_time", banTime)
        );
    }

    public static void removeBannedPlayer(Island island, SuperiorPlayer superiorPlayer) {
        island.getDatabaseBridge().deleteObject("islands_bans",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        );
    }

    public static void saveCoopLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("coops_limit", island.getCoopLimit()));
    }

    public static void saveTeleportLocation(Island island, World.Environment environment, Location location) {
        if(location == null){
            island.getDatabaseBridge().deleteObject("islands_homes",
                    createFilter("island", island, new Pair<>("environment", environment.name()))
            );
        }
        else {
            island.getDatabaseBridge().insertObject("islands_homes",
                    new Pair<>("island", island.getUniqueId().toString()),
                    new Pair<>("environment", environment.name()),
                    new Pair<>("location", LocationUtils.getLocation(location))
            );
        }
    }

    public static void saveVisitorLocation(Island island, World.Environment environment, Location location) {
        island.getDatabaseBridge().insertObject("islands_visitor_homes",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("environment", environment.name()),
                new Pair<>("location", LocationUtils.getLocation(location))
        );
    }

    public static void removeVisitorLocation(Island island, World.Environment environment){
        island.getDatabaseBridge().deleteObject("islands_visitor_homes",
                createFilter("island", island, new Pair<>("environment", environment.name()))
        );
    }

    public static void saveUnlockedWorlds(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("unlocked_worlds", island.getUnlockedWorldsFlag())
        );
    }

    public static void savePlayerPermission(Island island, SuperiorPlayer superiorPlayer, IslandPrivilege privilege,
                                            boolean status){
        island.getDatabaseBridge().insertObject("islands_player_permissions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", superiorPlayer.getUniqueId().toString()),
                new Pair<>("permission", privilege.getName()),
                new Pair<>("status", status)
        );
    }

    public static void clearPlayerPermission(Island island, SuperiorPlayer superiorPlayer){
        island.getDatabaseBridge().deleteObject("islands_player_permissions",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        );
    }

    public static void saveRolePermission(Island island, PlayerRole playerRole, IslandPrivilege privilege){
        island.getDatabaseBridge().insertObject("islands_role_permissions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("role", playerRole.getId()),
                new Pair<>("permission", privilege.getName())
        );
    }

    public static void removeRolePermission(Island island, PlayerRole playerRole){
        island.getDatabaseBridge().deleteObject("islands_role_permissions",
                createFilter("island", island, new Pair<>("role", playerRole.getId()))
        );
    }

    public static void clearRolePermissions(Island island){
        island.getDatabaseBridge().deleteObject("islands_role_permissions",
                createFilter("island", island));
    }

    public static void saveName(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("name", island.getName()));
    }

    public static void saveDescription(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("description", island.getDescription()));
    }

    public static void saveSize(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("size", island.getIslandSize()));
    }

    public static void saveDiscord(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("discord", island.getDiscord()));
    }

    public static void savePaypal(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("paypal", island.getPaypal()));
    }

    public static void saveLockedStatus(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("locked", island.isLocked()));
    }

    public static void saveIgnoredStatus(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("ignored", island.isIgnored()));
    }

    public static void saveLastTimeUpdate(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("last_time_updated", island.getLastTimeUpdate()));
    }

    public static void saveBankLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("bank_limit", island.getBankLimit() + ""));
    }

    public static void saveBonusWorth(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("worth_bonus", island.getBonusWorth() + ""));
    }

    public static void saveBonusLevel(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("levels_bonus", island.getBonusLevel() + ""));
    }

    public static void saveUpgrade(Island island, Upgrade upgrade, int level) {
        saveUpgrade(island, upgrade.getName(), level);
    }

    public static void saveUpgrade(Island island, String upgradeName, int level) {
        island.getDatabaseBridge().insertObject("islands_upgrades",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("upgrade", upgradeName),
                new Pair<>("level", level)
        );
    }

    public static void saveCropGrowth(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("crop_growth_multiplier", island.getCropGrowthMultiplier()));
    }

    public static void saveSpawnerRates(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("spawner_rates_multiplier", island.getSpawnerRatesMultiplier()));
    }

    public static void saveMobDrops(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("mob_drops_multiplier", island.getMobDropsMultiplier()));
    }

    public static void saveBlockLimit(Island island, Key block, int limit) {
        island.getDatabaseBridge().insertObject("islands_block_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("block", block.toString()),
                new Pair<>("limit", limit)
        );
    }

    public static void clearBlockLimits(Island island) {
        island.getDatabaseBridge().deleteObject("islands_block_limits",
                createFilter("island", island));
    }

    public static void removeBlockLimit(Island island, Key block) {
        island.getDatabaseBridge().deleteObject("islands_block_limits",
                createFilter("island", island, new Pair<>("block", block.toString()))
        );
    }

    public static void saveEntityLimit(Island island, Key entityType, int limit) {
        island.getDatabaseBridge().insertObject("islands_entity_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("entity", entityType.toString()),
                new Pair<>("limit", limit)
        );
    }

    public static void clearEntityLimits(Island island) {
        island.getDatabaseBridge().deleteObject("islands_entity_limits",
                createFilter("island", island));
    }

    public static void saveTeamLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("members_limit", island.getTeamLimit()));
    }

    public static void saveWarpsLimit(Island island) {
        island.getDatabaseBridge().updateObject("islands_settings",
                createFilter("island", island),
                new Pair<>("warps_limit", island.getWarpsLimit()));
    }

    public static void saveIslandEffect(Island island, PotionEffectType potionEffectType, int level) {
        island.getDatabaseBridge().insertObject("islands_effects",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("effect_type", potionEffectType.getName()),
                new Pair<>("level", level)
        );
    }

    public static void removeIslandEffect(Island island, PotionEffectType potionEffectType) {
        island.getDatabaseBridge().deleteObject("islands_effects",
                createFilter("island", island, new Pair<>("effect_type", potionEffectType.getName()))
        );
    }

    public static void clearIslandEffects(Island island) {
        island.getDatabaseBridge().deleteObject("islands_effects",
                createFilter("island", island));
    }

    public static void saveRoleLimit(Island island, PlayerRole playerRole, int limit) {
        island.getDatabaseBridge().insertObject("islands_role_limits",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("role", playerRole.getId()),
                new Pair<>("limit", limit)
        );
    }

    public static void removeRoleLimit(Island island, PlayerRole playerRole) {
        island.getDatabaseBridge().deleteObject("islands_role_limits",
                createFilter("island", island, new Pair<>("role", playerRole.getId()))
        );
    }

    public static void saveWarp(Island island, IslandWarp islandWarp) {
        WarpCategory category = islandWarp.getCategory();
        ItemStack icon = islandWarp.getRawIcon();
        island.getDatabaseBridge().insertObject("islands_warps",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", islandWarp.getName()),
                new Pair<>("category", category == null ? "" : category.getName()),
                new Pair<>("location", LocationUtils.getLocation(islandWarp.getLocation())),
                new Pair<>("private", islandWarp.hasPrivateFlag()),
                new Pair<>("icon", icon == null ? "" : ItemUtils.serializeItem(icon))
        );
    }

    public static void updateWarpName(Island island, IslandWarp islandWarp, String oldName) {
        island.getDatabaseBridge().updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", oldName)),
                new Pair<>("name", islandWarp.getName())
        );
    }

    public static void updateWarpLocation(Island island, IslandWarp islandWarp) {
        island.getDatabaseBridge().updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("location", LocationUtils.getLocation(islandWarp.getLocation()))
        );
    }

    public static void updateWarpPrivateStatus(Island island, IslandWarp islandWarp) {
        island.getDatabaseBridge().updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("private", islandWarp.hasPrivateFlag())
        );
    }

    public static void updateWarpIcon(Island island, IslandWarp islandWarp) {
        ItemStack icon = islandWarp.getRawIcon();
        island.getDatabaseBridge().updateObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName())),
                new Pair<>("icon", icon == null ? "" : ItemUtils.serializeItem(icon))
        );
    }

    public static void removeWarp(Island island, IslandWarp islandWarp) {
        island.getDatabaseBridge().deleteObject("islands_warps",
                createFilter("island", island, new Pair<>("name", islandWarp.getName()))
        );
    }

    public static void saveRating(Island island, SuperiorPlayer superiorPlayer, Rating rating, long rateTime) {
        saveRating(island, superiorPlayer.getUniqueId(), rating, rateTime);
    }

    public static void saveRating(Island island, UUID playerUUID, Rating rating, long rateTime) {
        island.getDatabaseBridge().insertObject("islands_ratings",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", playerUUID.toString()),
                new Pair<>("rating", rating.getValue()),
                new Pair<>("rating_time", rateTime)
        );
    }

    public static void removeRating(Island island, SuperiorPlayer superiorPlayer) {
        island.getDatabaseBridge().deleteObject("islands_ratings",
                createFilter("island", island, new Pair<>("player", superiorPlayer.getUniqueId().toString()))
        );
    }

    public static void clearRatings(Island island) {
        island.getDatabaseBridge().deleteObject("islands_ratings",
                createFilter("island", island)
        );
    }

    public static void saveMission(Island island, Mission<?> mission, int finishCount) {
        island.getDatabaseBridge().insertObject("islands_missions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", mission.getName().toLowerCase()),
                new Pair<>("finish_count", finishCount)
        );
    }

    public static void removeMission(Island island, Mission<?> mission) {
        island.getDatabaseBridge().deleteObject("islands_missions",
                createFilter("island", island, new Pair<>("name", mission.getName()))
        );
    }

    public static void saveIslandFlag(Island island, IslandFlag islandFlag, int status) {
        island.getDatabaseBridge().insertObject("islands_flags",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", islandFlag.getName()),
                new Pair<>("status", status)
        );
    }

    public static void saveGeneratorRate(Island island, World.Environment environment, Key blockKey, int rate) {
        island.getDatabaseBridge().insertObject("islands_generators",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("environment", environment.name()),
                new Pair<>("block", blockKey.toString()),
                new Pair<>("rate", rate)
        );
    }

    public static void clearGeneratorRates(Island island, World.Environment environment) {
        island.getDatabaseBridge().deleteObject("islands_generators",
                createFilter("island", island, new Pair<>("environment", environment.name()))
        );
    }

    public static void saveGeneratedSchematics(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("generated_schematics", island.getGeneratedSchematicsFlag()));
    }

    public static void saveDirtyChunks(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("dirty_chunks", ChunksTracker.serialize(island)));
    }

    public static void saveBlockCounts(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("block_counts", IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger())));
    }

    public static void saveIslandChest(Island island, IslandChest islandChest) {
        island.getDatabaseBridge().insertObject("islands_chests",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("index", islandChest.getIndex()),
                new Pair<>("contents", ItemUtils.serialize(islandChest.getContents()))
        );
    }

    public static void saveLastInterestTime(Island island) {
        island.getDatabaseBridge().updateObject("islands_banks",
                createFilter("island", island),
                new Pair<>("last_interest_time", island.getLastInterestTime() * 1000));
    }

    public static void saveVisitor(Island island, SuperiorPlayer visitor, long visitTime) {
        island.getDatabaseBridge().insertObject("islands_visitors",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", visitor.getUniqueId().toString()),
                new Pair<>("visit_time", visitTime)
        );
    }

    public static void saveWarpCategory(Island island, WarpCategory warpCategory) {
        island.getDatabaseBridge().insertObject("islands_warp_categories",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("name", warpCategory.getName()),
                new Pair<>("slot", warpCategory.getSlot()),
                new Pair<>("icon", ItemUtils.serializeItem(warpCategory.getRawIcon()))
        );
    }

    public static void updateWarpCategory(Island island, IslandWarp islandWarp, String oldCategoryName) {
        WarpCategory category = islandWarp.getCategory();
        island.getDatabaseBridge().updateObject("islands_warps",
                createFilter("island", island, new Pair<>("category", oldCategoryName)),
                new Pair<>("category", category == null ? "" : category.getName())
        );
    }

    public static void updateWarpCategoryName(Island island, WarpCategory warpCategory, String oldName) {
        island.getDatabaseBridge().updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", oldName)),
                new Pair<>("name", warpCategory.getName())
        );
    }

    public static void updateWarpCategorySlot(Island island, WarpCategory warpCategory) {
        island.getDatabaseBridge().updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName())),
                new Pair<>("slot", warpCategory.getSlot())
        );
    }

    public static void updateWarpCategoryIcon(Island island, WarpCategory warpCategory) {
        island.getDatabaseBridge().updateObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName())),
                new Pair<>("icon", ItemUtils.serializeItem(warpCategory.getRawIcon()))
        );
    }

    public static void removeWarpCategory(Island island, WarpCategory warpCategory) {
        island.getDatabaseBridge().deleteObject("islands_warp_categories",
                createFilter("island", island, new Pair<>("name", warpCategory.getName()))
        );
    }

    public static void saveIslandLeader(Island island) {
        island.getDatabaseBridge().updateObject("islands",
                createFilter("uuid", island),
                new Pair<>("owner", island.getOwner().getUniqueId().toString())
        );
    }

    public static void saveBankBalance(Island island) {
        island.getDatabaseBridge().updateObject("islands_banks",
                createFilter("island", island),
                new Pair<>("balance", island.getIslandBank().getBalance() + "")
        );
    }

    public static void saveBankTransaction(Island island, BankTransaction bankTransaction) {
        island.getDatabaseBridge().insertObject("bank_transactions",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("player", bankTransaction.getPlayer() == null ? "" : bankTransaction.getPlayer().toString()),
                new Pair<>("bank_action", bankTransaction.getAction().name()),
                new Pair<>("position", bankTransaction.getPosition()),
                new Pair<>("time", bankTransaction.getTime()),
                new Pair<>("failure_reason", bankTransaction.getFailureReason()),
                new Pair<>("amount", bankTransaction.getAmount() + "")
        );
    }

    public static void insertIsland(Island island){
        island.getDatabaseBridge().insertObject("islands",
                new Pair<>("uuid", island.getUniqueId().toString()),
                new Pair<>("owner", island.getOwner().getUniqueId().toString()),
                new Pair<>("center", LocationUtils.getLocation(island.getCenter(World.Environment.NORMAL))),
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
                new Pair<>("last_time_updated", island.getLastTimeUpdate()),
                new Pair<>("dirty_chunks", ChunksTracker.serialize(island)),
                new Pair<>("block_counts", IslandSerializer.serializeBlockCounts(island.getBlockCountsAsBigInteger()))
        );

        island.getDatabaseBridge().insertObject("islands_banks",
                new Pair<>("island", island.getUniqueId().toString()),
                new Pair<>("balance", island.getIslandBank().getBalance() + ""),
                new Pair<>("last_interest_time", island.getLastInterestTime())
        );

        island.getDatabaseBridge().insertObject("islands_settings",
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
    }

    public static void deleteIsland(Island island){
        island.getDatabaseBridge().deleteObject("islands", createFilter("uuid", island));
        island.getDatabaseBridge().deleteObject("islands_banks", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_bans", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_block_limits", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_chests", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_effects", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_entity_limits", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_flags", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_generators", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_homes", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_members", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_missions", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_player_permissions", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_ratings", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_role_limits", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_role_permissions", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_settings", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_upgrades", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_visitor_homes", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_visitors", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_warp_categories", createFilter("island", island));
        island.getDatabaseBridge().deleteObject("islands_warps", createFilter("island", island));
    }

    public static void markIslandChestsToBeSaved(Island island, IslandChest islandChest) {
        SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(island.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.ISLAND_CHESTS, e -> new HashSet<>())
                .add(islandChest);
    }

    public static void markBlockCountsToBeSaved(Island island) {
        Set<Object> varsForBlockCounts = SAVE_METHODS_TO_BE_EXECUTED.computeIfAbsent(island.getUniqueId(), u -> new EnumMap<>(FutureSave.class))
                .computeIfAbsent(FutureSave.BLOCK_COUNTS, e -> new HashSet<>());
        if(varsForBlockCounts.isEmpty())
            varsForBlockCounts.add(new Object());
    }

    public static boolean isModified(Island island){
        return SAVE_METHODS_TO_BE_EXECUTED.containsKey(island.getUniqueId());
    }

    public static void executeFutureSaves(Island island){
        Map<FutureSave, Set<Object>> futureSaves = SAVE_METHODS_TO_BE_EXECUTED.remove(island.getUniqueId());
        if(futureSaves != null){
            for(Map.Entry<FutureSave, Set<Object>> futureSaveEntry : futureSaves.entrySet()){
                switch (futureSaveEntry.getKey()){
                    case BLOCK_COUNTS:
                        saveBlockCounts(island);
                        break;
                    case ISLAND_CHESTS:
                        for(Object islandChest : futureSaveEntry.getValue())
                            saveIslandChest(island, (IslandChest) islandChest);
                        break;
                }
            }
        }
    }

    private static DatabaseFilter createFilter(String id, Island island, Pair<String, Object>... others){
        List<Pair<String, Object>> filters = new ArrayList<>();
        filters.add(new Pair<>(id, island.getUniqueId().toString()));
        if(others != null)
            filters.addAll(Arrays.asList(others));
        return new DatabaseFilter(filters);
    }

    private enum FutureSave {

        BLOCK_COUNTS,
        ISLAND_CHESTS

    }

}
