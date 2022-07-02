package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataInput;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataOutput;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpCategoryInfo;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedWarpInfo;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.container.value.SyncedValue;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
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

    protected static byte[] serializeIsland(CachedIslandInfo island) {
        ByteArrayDataOutput dataOutput = new ByteArrayDataOutput();

        dataOutput.writeString(island.description);
        dataOutput.writeString(island.discord);
        dataOutput.writeString(island.paypal);
        dataOutput.writeBoolean(island.isLocked);
        dataOutput.writeInt(island.unlockedWorlds);
        dataOutput.writeLong(island.lastTimeUpdated);
        dataOutput.writeString(island.islandType);
        dataOutput.writeBigDecimal(island.bonusWorth);
        dataOutput.writeBigDecimal(island.bonusLevel);
        dataOutput.writeBigDecimal(island.bankLimit instanceof SyncedValue ? SYNCED_BANK_LIMIT_VALUE : island.bankLimit.get());
        dataOutput.writeInt(island.coopLimit instanceof SyncedValue ? -1 : island.coopLimit.get());
        dataOutput.writeInt(island.teamLimit instanceof SyncedValue ? -1 : island.teamLimit.get());
        dataOutput.writeInt(island.warpsLimit instanceof SyncedValue ? -1 : island.warpsLimit.get());
        dataOutput.writeDouble(island.cropGrowth instanceof SyncedValue ? -1D : island.cropGrowth.get());
        dataOutput.writeDouble(island.spawnerRates instanceof SyncedValue ? -1D : island.spawnerRates.get());
        dataOutput.writeDouble(island.mobDrops instanceof SyncedValue ? -1D : island.mobDrops.get());
        dataOutput.writeBigDecimal(island.balance);
        dataOutput.writeLong(island.lastInterestTime);

        CacheSerializer.serializePlayers(island.members, dataOutput);
        CacheSerializer.serializePlayers(island.bannedPlayers, dataOutput);

        List<SIsland.UniqueVisitor> uniqueVisitors = island.uniqueVisitors;
        dataOutput.writeInt(uniqueVisitors.size());
        uniqueVisitors.forEach(uniqueVisitor -> {
            dataOutput.writeUUID(uniqueVisitor.getSuperiorPlayer().getUniqueId());
            dataOutput.writeLong(uniqueVisitor.getLastVisitTime());
        });

        Location[] islandHomes = island.islandHomes;
        dataOutput.writeInt(islandHomes.length);
        for (Location location : islandHomes) {
            CacheSerializer.serializeLocation(location, dataOutput);
        }

        CacheSerializer.serializeLocation(island.visitorHomes[World.Environment.NORMAL.ordinal()], dataOutput);

        Map<SuperiorPlayer, PlayerPrivilegeNode> playerPermissions = island.playerPermissions;
        dataOutput.writeInt(playerPermissions.size());
        playerPermissions.forEach(((superiorPlayer, permissionNode) -> {
            dataOutput.writeUUID(superiorPlayer.getUniqueId());
            CacheSerializer.serializePermissionNode(permissionNode, dataOutput);
        }));

        Map<IslandPrivilege, PlayerRole> rolePermissions = island.rolePermissions;
        dataOutput.writeInt(rolePermissions.size());
        rolePermissions.forEach(((islandPrivilege, playerRole) -> {
            dataOutput.writeString(islandPrivilege.getName());
            dataOutput.writeInt(playerRole.getId());
        }));

        Map<Key, BigInteger> blockCounts = island.blockCounts;
        dataOutput.writeInt(blockCounts.size());
        blockCounts.forEach(((key, bigInteger) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeBytes(bigInteger.toByteArray());
        }));

        Set<ChunkPosition> dirtyChunks = island.dirtyChunks;
        dataOutput.writeInt(dirtyChunks.size());
        dirtyChunks.forEach(dirtyChunkPos -> {
            CacheSerializer.serializeChunkPosition(dirtyChunkPos, dataOutput);
        });

        Map<String, Integer> upgrades = island.upgrades;
        dataOutput.writeInt(upgrades.size());
        upgrades.forEach((upgradeName, level) -> {
            dataOutput.writeString(upgradeName);
            dataOutput.writeInt(level);
        });

        Map<Key, Integer> blockLimits = island.blockLimits.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get()));
        dataOutput.writeInt(blockLimits.size());
        blockLimits.forEach(((key, limit) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeInt(limit);
        }));

        Map<Key, Integer> entityLimits = island.entityLimits.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get()));
        dataOutput.writeInt(entityLimits.size());
        entityLimits.forEach(((key, limit) -> {
            dataOutput.writeString(key.toString());
            dataOutput.writeInt(limit);
        }));

        Map<PotionEffectType, Value<Integer>> islandEffects = island.islandEffects;
        dataOutput.writeInt(islandEffects.size());
        islandEffects.forEach(((potionEffectType, level) -> {
            dataOutput.writeInt(potionEffectType.getId());
            dataOutput.writeInt(level.get());
        }));

        Map<PlayerRole, Value<Integer>> roleLimits = island.roleLimits;
        dataOutput.writeInt(roleLimits.size());
        roleLimits.forEach(((playerRole, limit) -> {
            dataOutput.writeInt(playerRole.getId());
            dataOutput.writeInt(limit.get());
        }));

        Collection<CachedWarpCategoryInfo> warpCategories = island.cachedWarpCategoryInfoList;
        dataOutput.writeInt(warpCategories.size());
        warpCategories.forEach(warpCategory -> {
            CacheSerializer.serializeWarpCategory(warpCategory, dataOutput);
        });

        Collection<CachedWarpInfo> islandWarps = island.cachedWarpInfoList;
        dataOutput.writeInt(islandWarps.size());
        islandWarps.forEach(islandWarp -> {
            CacheSerializer.serializeIslandWarp(islandWarp, dataOutput);
        });

        Map<UUID, Rating> ratings = island.ratings;
        dataOutput.writeInt(ratings.size());
        ratings.forEach((playerUUID, rating) -> {
            dataOutput.writeLong(playerUUID.getMostSignificantBits());
            dataOutput.writeLong(playerUUID.getLeastSignificantBits());
            dataOutput.writeByte((byte) rating.getValue());
        });

        Map<IslandFlag, Byte> islandFlags = island.islandFlags;
        dataOutput.writeInt(islandFlags.size());
        islandFlags.forEach((islandFlag, isEnabled) -> {
            dataOutput.writeString(islandFlag.getName());
            dataOutput.writeByte(isEnabled);
        });

        for (World.Environment environment : World.Environment.values()) {
            Map<Key, Value<Integer>> generatorRates = island.cobbleGeneratorValues[environment.ordinal()];
            if (generatorRates == null) {
                dataOutput.writeInt(0);
            } else {
                dataOutput.writeInt(generatorRates.size());
                generatorRates.forEach((block, rate) -> {
                    dataOutput.writeString(block.toString());
                    dataOutput.writeInt(rate.get());
                });
            }
        }

        List<ItemStack[]> islandChests = island.islandChests;
        dataOutput.writeInt(islandChests.size());
        for (int i = 0; i < islandChests.size(); ++i) {
            dataOutput.writeInt(i);
            CacheSerializer.serializeIslandChest(islandChests.get(i), dataOutput);
        }

        dataOutput.writeBytes(island.persistentData);

        Map<Mission<?>, Integer> missions = island.completedMissions;
        dataOutput.writeInt(missions.size());
        missions.forEach((mission, completedAmounts) -> {
            dataOutput.writeString(mission.getName());
            dataOutput.writeInt(completedAmounts);
        });

        return dataOutput.toByteArray();
    }

    protected static Island deserializeIsland(IslandBase islandBase, ByteArrayDataInput dataInput) {
        CachedIslandInfo cachedIslandInfo = new CachedIslandInfo(islandBase.getUniqueId());

        cachedIslandInfo.owner = islandBase.getOwner().getUniqueId();
        cachedIslandInfo.center = islandBase.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());
        cachedIslandInfo.name = islandBase.getRawName();
        cachedIslandInfo.creationTime = islandBase.getCreationTime();
        cachedIslandInfo.generatedSchematics = islandBase.getGeneratedSchematicsFlag();

        cachedIslandInfo.description = dataInput.readString();
        cachedIslandInfo.discord = dataInput.readString();
        cachedIslandInfo.paypal = dataInput.readString();
        cachedIslandInfo.isLocked = dataInput.readBoolean();
        cachedIslandInfo.unlockedWorlds = dataInput.readInt();
        cachedIslandInfo.lastTimeUpdated = dataInput.readLong();
        cachedIslandInfo.islandType = dataInput.readString();
        cachedIslandInfo.bonusWorth = dataInput.readBigDecimal();
        cachedIslandInfo.bonusLevel = dataInput.readBigDecimal();

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
            cachedIslandInfo.islandHomes[i] = CacheSerializer.deserializeLocation(dataInput);
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

        int blockCountsAmount = dataInput.readInt();
        for (int i = 0; i < blockCountsAmount; ++i) {
            Key block = KeyImpl.of(dataInput.readString());
            byte[] data = dataInput.readBytes();
            cachedIslandInfo.blockCounts.put(block, new BigInteger(data));
        }

        int dirtyChunksAmount = dataInput.readInt();
        for (int i = 0; i < dirtyChunksAmount; ++i) {
            cachedIslandInfo.dirtyChunks.add(CacheSerializer.deserializeChunkPosition(dataInput));
        }

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

        return plugin.getGrid().createIsland(cachedIslandInfo).orElseThrow(IllegalStateException::new);
    }

    public static IslandsCache of(SuperiorSkyblockPlugin plugin, IslandsContainer islandsContainer,
                                  Collection<CachedIslandInfo> islands) {
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
