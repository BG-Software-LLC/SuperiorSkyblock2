package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataInput;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataOutput;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpCategoryInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpInfo;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsSerializer;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class IslandsCache {

    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected static final int ISLANDS_TABLE_UUID_SIZE = Long.BYTES * 2;
    protected static final int ISLANDS_TABLE_ENTRY_SIZE = ISLANDS_TABLE_UUID_SIZE + Integer.BYTES;
    protected static final int MAX_MEMORY_LENGTH = 10000;

    protected final IslandsContainer islandsContainer;

    protected final byte[] islandsTableBytes;
    protected final int islandsTableElementsCount;

    protected IslandsCache(IslandsContainer islandsContainer, byte[] islandsTableBytes, int islandsTableElementsCount) {
        this.islandsContainer = islandsContainer;
        this.islandsTableBytes = islandsTableBytes;
        this.islandsTableElementsCount = islandsTableElementsCount;
    }

    public abstract <T extends IslandBase> T loadIsland(IslandBase islandBase, IslandLoadLevel<T> loadLevel);

    protected int getIslandIndex(UUID uuid) {
        int low = 0;
        int high = this.islandsTableElementsCount;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int elementStart = mid * ISLANDS_TABLE_ENTRY_SIZE;

            int cmp = compareElement(elementStart, uuid);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }

        return -1;  // key not found
    }

    protected int compareElement(int elementStart, UUID matchingUUID) {
        UUID elementUUID = new UUID(parseLong(elementStart), parseLong(elementStart + Long.BYTES));
        return elementUUID.compareTo(matchingUUID);
    }

    protected long parseLong(int start) {
        long value = 0;
        int shift = 56;

        for (int i = 0; i < 8; ++i) {
            value += (long) (this.islandsTableBytes[start + i] & 0xFF) << shift;
            shift -= 8;
        }

        return value;
    }

    protected static byte[] serializeIsland(Island island) {
        ByteArrayDataOutput dataOutput = new ByteArrayDataOutput();

        dataOutput.writeString(island.getDescription());
        dataOutput.writeString(island.getDiscord());
        dataOutput.writeString(island.getPaypal());
        dataOutput.writeBoolean(island.isLocked());
        dataOutput.writeInt(island.getUnlockedWorldsFlag());
        dataOutput.writeLong(island.getLastTimeUpdate());
        dataOutput.writeString(island.getSchematicName());
        dataOutput.writeBigDecimal(island.getBonusWorth());
        dataOutput.writeBigDecimal(island.getBonusLevel());
        dataOutput.writeBigDecimal(island.getBankLimitRaw());
        dataOutput.writeInt(island.getCoopLimitRaw());
        dataOutput.writeInt(island.getTeamLimitRaw());
        dataOutput.writeInt(island.getWarpsLimitRaw());
        dataOutput.writeDouble(island.getCropGrowthRaw());
        dataOutput.writeDouble(island.getSpawnerRatesRaw());
        dataOutput.writeDouble(island.getMobDropsRaw());
        dataOutput.writeBigDecimal(island.getIslandBank().getBalance());
        dataOutput.writeLong(island.getLastInterestTime());

        CacheSerializer.serializePlayers(island.getIslandMembers(false), dataOutput);
        CacheSerializer.serializePlayers(island.getBannedPlayers(), dataOutput);

        List<Pair<SuperiorPlayer, Long>> uniqueVisitors = island.getUniqueVisitorsWithTimes();
        dataOutput.writeInt(uniqueVisitors.size());
        uniqueVisitors.forEach(pair -> {
            dataOutput.writeUUID(pair.getKey().getUniqueId());
            dataOutput.writeLong(pair.getValue());
        });

        Map<World.Environment, Location> islandHomes = island.getIslandHomes();
        dataOutput.writeInt(islandHomes.size());
        islandHomes.forEach(((environment, location) -> {
            dataOutput.writeByte((byte) environment.ordinal());
            CacheSerializer.serializeLocation(location, dataOutput);
        }));

        CacheSerializer.serializeLocation(island.getVisitorsLocation(), dataOutput);

        Map<SuperiorPlayer, PermissionNode> playerPermissions = island.getPlayerPermissions();
        dataOutput.writeInt(playerPermissions.size());
        playerPermissions.forEach(((superiorPlayer, permissionNode) -> {
            dataOutput.writeUUID(superiorPlayer.getUniqueId());
            CacheSerializer.serializePermissionNode((PlayerPrivilegeNode) permissionNode, dataOutput);
        }));

        Map<IslandPrivilege, PlayerRole> rolePermissions = island.getRolePermissions();
        dataOutput.writeInt(rolePermissions.size());
        rolePermissions.forEach(((islandPrivilege, playerRole) -> {
            dataOutput.writeString(islandPrivilege.getName());
            dataOutput.writeInt(playerRole.getId());
        }));

        Map<Key, BigInteger> blockCounts = island.getBlockCountsAsBigInteger();
        dataOutput.writeInt(blockCounts.size());
        blockCounts.forEach(((key, bigInteger) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeBytes(bigInteger.toByteArray());
        }));

        Set<ChunkPosition> dirtyChunks = ChunksTracker.getDirtyChunks(island);
        dataOutput.writeInt(dirtyChunks.size());
        dirtyChunks.forEach(dirtyChunkPos -> {
            CacheSerializer.serializeChunkPosition(dirtyChunkPos, dataOutput);
        });
        ChunksTracker.removeIsland(island);

        Map<String, Integer> upgrades = island.getUpgrades();
        dataOutput.writeInt(upgrades.size());
        upgrades.forEach((upgradeName, level) -> {
            dataOutput.writeString(upgradeName);
            dataOutput.writeInt(level);
        });

        Map<Key, Integer> blockLimits = island.getCustomBlocksLimits();
        dataOutput.writeInt(blockLimits.size());
        blockLimits.forEach(((key, limit) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeInt(limit);
        }));

        Map<Key, Integer> entityLimits = island.getCustomEntitiesLimits();
        dataOutput.writeInt(entityLimits.size());
        entityLimits.forEach(((key, limit) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeInt(limit);
        }));

        Map<PotionEffectType, Integer> islandEffects = island.getPotionEffects();
        dataOutput.writeInt(islandEffects.size());
        islandEffects.forEach(((potionEffectType, level) -> {
            dataOutput.writeInt(potionEffectType.getId());
            dataOutput.writeInt(level);
        }));

        Map<PlayerRole, Integer> roleLimits = island.getRoleLimits();
        dataOutput.writeInt(roleLimits.size());
        roleLimits.forEach(((playerRole, limit) -> {
            dataOutput.writeInt(playerRole.getId());
            dataOutput.writeInt(limit);
        }));

        Collection<WarpCategory> warpCategories = island.getWarpCategories().values();
        dataOutput.writeInt(warpCategories.size());
        warpCategories.forEach(warpCategory -> {
            CacheSerializer.serializeWarpCategory(warpCategory, dataOutput);
        });

        Map<UUID, Rating> ratings = island.getRatings();
        dataOutput.writeInt(ratings.size());
        ratings.forEach((playerUUID, rating) -> {
            dataOutput.writeLong(playerUUID.getMostSignificantBits());
            dataOutput.writeLong(playerUUID.getLeastSignificantBits());
            dataOutput.writeByte((byte) rating.getValue());
        });

        Map<IslandFlag, Byte> islandFlags = island.getAllSettings();
        dataOutput.writeInt(islandFlags.size());
        islandFlags.forEach((islandFlag, isEnabled) -> {
            dataOutput.writeString(islandFlag.getName());
            dataOutput.writeByte(isEnabled);
        });

        for (World.Environment environment : World.Environment.values()) {
            Map<Key, Integer> generatorRates = island.getCustomGeneratorAmounts(environment);
            dataOutput.writeInt(generatorRates.size());
            generatorRates.forEach((block, rate) -> {
                dataOutput.writeString(block.toString());
                dataOutput.writeInt(rate);
            });
        }

        IslandChest[] islandChests = island.getChest();
        dataOutput.writeInt(islandChests.length);
        for (IslandChest islandChest : islandChests)
            CacheSerializer.serializeIslandChest(islandChest, dataOutput);

        dataOutput.writeBytes(island.getPersistentDataContainer().serialize());

        Map<Mission<?>, Integer> missions = island.getCompletedMissionsWithAmounts();
        dataOutput.writeInt(missions.size());
        missions.forEach((mission, completedAmounts) -> {
            dataOutput.writeString(mission.getName());
            dataOutput.writeInt(completedAmounts);
        });

        return dataOutput.toByteArray();
    }

    protected static Island deserializeIsland(IslandBase islandBase, ByteArrayDataInput dataInput) {
        CachedIslandInfo cachedIslandInfo = new CachedIslandInfo();
        Map<String, Object> resultSet = new HashMap<>();

        resultSet.put("uuid", islandBase.getUniqueId().toString());
        resultSet.put("owner", islandBase.getOwner().getUniqueId().toString());
        resultSet.put("center", Serializers.LOCATION_SERIALIZER.serialize(
                islandBase.getCenter(plugin.getSettings().getWorlds().getDefaultWorld())));
        resultSet.put("name", islandBase.getRawName());
        resultSet.put("creation_time", islandBase.getCreationTime());
        resultSet.put("description", dataInput.readString());
        resultSet.put("discord", dataInput.readString());
        resultSet.put("paypal", dataInput.readString());
        resultSet.put("locked", dataInput.readBoolean());
        resultSet.put("generated_schematics", islandBase.getGeneratedSchematicsFlag());
        resultSet.put("unlocked_worlds", dataInput.readInt());
        resultSet.put("last_time_updated", dataInput.readLong());
        resultSet.put("island_type", dataInput.readString());
        resultSet.put("worth_bonus", dataInput.readBigDecimal());
        resultSet.put("levels_bonus", dataInput.readBigDecimal());

        int borderSize = islandBase.getIslandSize();
        cachedIslandInfo.islandSize = borderSize < 0 ? Value.syncedFixed(borderSize) : Value.fixed(borderSize);

        BigDecimal bankLimit = dataInput.readBigDecimal();
        cachedIslandInfo.bankLimit = bankLimit.compareTo(SYNCED_BANK_LIMIT_VALUE) <= 0 ? Value.syncedFixed(bankLimit) : Value.fixed(bankLimit);

        int coopLimit = dataInput.readInt();
        cachedIslandInfo.coopLimit = coopLimit < 0 ? Value.syncedFixed(coopLimit) : Value.fixed(coopLimit);

        int membersLimit = dataInput.readInt();
        cachedIslandInfo.teamLimit = membersLimit < 0 ? Value.syncedFixed(membersLimit) : Value.fixed(membersLimit);

        int warpsLimit = dataInput.readInt();
        cachedIslandInfo.warpsLimit = warpsLimit < 0 ? Value.syncedFixed(warpsLimit) : Value.fixed(warpsLimit);

        double cropGrowth = dataInput.readDouble();
        cachedIslandInfo.cropGrowth = cropGrowth < 0 ? Value.syncedFixed(cropGrowth) : Value.fixed(cropGrowth);

        double spawnerRates = dataInput.readDouble();
        cachedIslandInfo.spawnerRates = spawnerRates < 0 ? Value.syncedFixed(spawnerRates) : Value.fixed(spawnerRates);

        double mobDrops = dataInput.readDouble();
        cachedIslandInfo.mobDrops = mobDrops < 0 ? Value.syncedFixed(mobDrops) : Value.fixed(mobDrops);

        cachedIslandInfo.balance = dataInput.readBigDecimal();
        cachedIslandInfo.lastInterestTime = dataInput.readLong();

        cachedIslandInfo.members.addAll(CacheSerializer.deserializePlayers(dataInput));
        cachedIslandInfo.bannedPlayers.addAll(CacheSerializer.deserializePlayers(dataInput));

        int uniqueVisitorsAmount = dataInput.readInt();
        List<SIsland.UniqueVisitor> uniqueVisitors = new ArrayList<>(uniqueVisitorsAmount);
        for (int i = 0; i < uniqueVisitorsAmount; ++i) {
            SuperiorPlayer superiorPlayer = CacheSerializer.deserializePlayer(dataInput);
            long lastTimeVisit = dataInput.readLong();
            uniqueVisitors.add(new SIsland.UniqueVisitor(superiorPlayer, lastTimeVisit));
        }
        cachedIslandInfo.uniqueVisitors.addAll(uniqueVisitors);

        int islandHomesAmount = dataInput.readInt();
        for (int i = 0; i < islandHomesAmount; ++i) {
            int islandHomeIndex = dataInput.readByte();
            cachedIslandInfo.islandHomes[islandHomeIndex] = CacheSerializer.deserializeLocation(dataInput);
        }

        cachedIslandInfo.visitorHomes[World.Environment.NORMAL.ordinal()] = CacheSerializer.deserializeLocation(dataInput);

        int playerPermissionsAmount = dataInput.readInt();
        for (int i = 0; i < playerPermissionsAmount; ++i) {
            PlayerPrivilegeNode playerPrivilegeNode = CacheSerializer.deserializePermissionNode(dataInput);
            cachedIslandInfo.playerPermissions.put(playerPrivilegeNode.getSuperiorPlayer(), playerPrivilegeNode);
        }

        int rolePermissionsAmount = dataInput.readInt();
        for (int i = 0; i < rolePermissionsAmount; ++i) {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(dataInput.readString());
            PlayerRole playerRole = SPlayerRole.fromId(dataInput.readInt());
            cachedIslandInfo.rolePermissions.put(islandPrivilege, playerRole);
        }

        KeyMap<BigInteger> blockCounts = KeyMapImpl.createHashMap();
        int blockCountsAmount = dataInput.readInt();
        for (int i = 0; i < blockCountsAmount; ++i) {
            Key block = KeyImpl.of(dataInput.readString());
            byte[] data = dataInput.readBytes();
            blockCounts.put(block, new BigInteger(data));
        }
        resultSet.put("block_counts", IslandsSerializer.serializeBlockCounts(blockCounts));

        Set<ChunkPosition> dirtyChunks = new HashSet<>();
        int dirtyChunksAmount = dataInput.readInt();
        for (int i = 0; i < dirtyChunksAmount; ++i) {
            dirtyChunks.add(CacheSerializer.deserializeChunkPosition(dataInput));
        }
        resultSet.put("dirty_chunks", IslandsSerializer.serializeDirtyChunks(dirtyChunks));

        int upgradesAmount = dataInput.readInt();
        for (int i = 0; i < upgradesAmount; ++i) {
            String upgradeName = dataInput.readString();
            int upgradeLevel = dataInput.readInt();
            cachedIslandInfo.upgrades.put(upgradeName, upgradeLevel);
        }

        int blockLimitsAmount = dataInput.readInt();
        for (int i = 0; i < blockLimitsAmount; ++i) {
            Key block = KeyImpl.of(dataInput.readString());
            int limit = dataInput.readInt();
            cachedIslandInfo.blockLimits.put(block, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        }

        int entityLimitsAmount = dataInput.readInt();
        for (int i = 0; i < entityLimitsAmount; ++i) {
            Key block = KeyImpl.of(dataInput.readString());
            int limit = dataInput.readInt();
            cachedIslandInfo.entityLimits.put(block, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        }

        int islandEffectsAmount = dataInput.readInt();
        for (int i = 0; i < islandEffectsAmount; ++i) {
            PotionEffectType potionEffectType = PotionEffectType.getById(dataInput.readInt());
            int level = dataInput.readInt();
            cachedIslandInfo.islandEffects.put(potionEffectType, level < 0 ? Value.syncedFixed(level) : Value.fixed(level));
        }

        int roleLimitsAmount = dataInput.readInt();
        for (int i = 0; i < roleLimitsAmount; ++i) {
            PlayerRole playerRole = SPlayerRole.fromId(dataInput.readInt());
            int limit = dataInput.readInt();
            cachedIslandInfo.roleLimits.put(playerRole, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        }

        int warpCategoriesAmount = dataInput.readInt();
        for (int i = 0; i < warpCategoriesAmount; ++i) {
            CachedWarpCategoryInfo cachedWarpCategoryInfo = CacheSerializer.deserializeWarpCategory(dataInput);
            cachedIslandInfo.cachedWarpCategoryInfoList.add(cachedWarpCategoryInfo);
            int warpsAmount = dataInput.readInt();
            for (int j = 0; j < warpsAmount; ++j) {
                CachedWarpInfo cachedWarpInfo = CacheSerializer.deserializeIslandWarp(dataInput);
                cachedWarpInfo.category = cachedWarpCategoryInfo.name;
                cachedIslandInfo.cachedWarpInfoList.add(cachedWarpInfo);
            }
        }

        int ratingsAmount = dataInput.readInt();
        for (int i = 0; i < ratingsAmount; ++i) {
            UUID ratingPlayer = dataInput.readUUID();
            Rating rating = Rating.valueOf(dataInput.readByte());
            cachedIslandInfo.ratings.put(ratingPlayer, rating);
        }

        int islandFlagsAmount = dataInput.readInt();
        for (int i = 0; i < islandFlagsAmount; ++i) {
            IslandFlag islandFlag = IslandFlag.getByName(dataInput.readString());
            byte isEnabled = dataInput.readByte();
            cachedIslandInfo.islandFlags.put(islandFlag, isEnabled);
        }

        for (World.Environment environment : World.Environment.values()) {
            int generatorRatesAmount = dataInput.readInt();
            if (generatorRatesAmount > 0) {
                KeyMap<Value<Integer>> generatorRates = KeyMapImpl.createHashMap();
                for (int i = 0; i < generatorRatesAmount; ++i) {
                    Key block = KeyImpl.of(dataInput.readString());
                    int rate = dataInput.readInt();
                    generatorRates.put(block, rate < 0 ? Value.syncedFixed(rate) : Value.fixed(rate));
                }
                cachedIslandInfo.cobbleGeneratorValues[environment.ordinal()] = generatorRates;
            }
        }

        int islandChestsAmount = dataInput.readInt();
        for (int i = 0; i < islandChestsAmount; ++i) {
            int index = dataInput.readInt();

            ItemStack[] chestContents = CacheSerializer.deserializeIslandChest(dataInput);

            if (index >= cachedIslandInfo.islandChests.size()) {
                while (index > cachedIslandInfo.islandChests.size()) {
                    cachedIslandInfo.islandChests.add(new ItemStack[plugin.getSettings().getIslandChests().getDefaultSize() * 9]);
                }

                cachedIslandInfo.islandChests.add(chestContents);
            } else {
                cachedIslandInfo.islandChests.set(index, chestContents);
            }
        }

        cachedIslandInfo.persistentData = dataInput.readBytes();

        int missionsAmount = dataInput.readInt();
        for (int i = 0; i < missionsAmount; ++i) {
            Mission<?> mission = plugin.getMissions().getMission(dataInput.readString());
            int completedAmount = dataInput.readInt();
            cachedIslandInfo.completedMissions.put(mission, completedAmount);
        }

        return plugin.getGrid().createIsland(cachedIslandInfo, new DatabaseResult(resultSet))
                .orElseThrow(IllegalStateException::new);
    }

    public static IslandsCache of(SuperiorSkyblockPlugin plugin, IslandsContainer islandsContainer, Collection<Island> islands) {
        MemoryIslandsCache memoryIslandsCache = MemoryIslandsCache.create(islandsContainer, islands);

        if (memoryIslandsCache.getBytesLength() >= MAX_MEMORY_LENGTH) {
            try {
                return memoryIslandsCache.toFileCache(plugin);
            } catch (IOException error) {
                SuperiorSkyblockPlugin.log("&cCouldn't load file cache (Using in-memory cache instead):");
                error.printStackTrace();
            }
        }

        return memoryIslandsCache;
    }

}
