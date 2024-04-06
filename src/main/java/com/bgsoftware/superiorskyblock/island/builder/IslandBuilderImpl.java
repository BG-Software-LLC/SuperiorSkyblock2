package com.bgsoftware.superiorskyblock.island.builder;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.enums.SyncStatus;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.DirtyChunk;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class IslandBuilderImpl implements Island.Builder {

    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Nullable
    public SuperiorPlayer owner;
    public UUID uuid = null;
    public Location center = null;
    public String islandName = "";
    @Nullable
    public String islandType;
    public long creationTime = System.currentTimeMillis() / 1000;
    public String discord = "None";
    public String paypal = "None";
    public BigDecimal bonusWorth = BigDecimal.ZERO;
    public BigDecimal bonusLevel = BigDecimal.ZERO;
    public boolean isLocked = false;
    public boolean isIgnored = false;
    public String description = "";
    public int generatedSchematicsMask = 0;
    public int unlockedWorldsMask = 0;
    public long lastTimeUpdated = System.currentTimeMillis() / 1000;
    public final Set<DirtyChunk> dirtyChunks = new LinkedHashSet<>();
    public final KeyMap<BigInteger> blockCounts = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
    public final EnumMap<World.Environment, Location> islandHomes = new EnumMap<>(World.Environment.class);
    public final List<SuperiorPlayer> members = new LinkedList<>();
    public final List<SuperiorPlayer> bannedPlayers = new LinkedList<>();
    public final Map<SuperiorPlayer, PlayerPrivilegeNode> playerPermissions = new LinkedHashMap<>();
    public final Map<IslandPrivilege, PlayerRole> rolePermissions = new LinkedHashMap<>();
    public final Map<String, Integer> upgrades = new LinkedHashMap<>();
    public final KeyMap<Value<Integer>> blockLimits = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
    public final Map<UUID, Rating> ratings = new LinkedHashMap<>();
    public final Map<MissionReference, Counter> completedMissions = new LinkedHashMap<>();
    public final Map<IslandFlag, Byte> islandFlags = new LinkedHashMap<>();
    public final EnumMap<World.Environment, KeyMap<Value<Integer>>> cobbleGeneratorValues = new EnumMap<>(World.Environment.class);
    public final List<SIsland.UniqueVisitor> uniqueVisitors = new LinkedList<>();
    public final KeyMap<Value<Integer>> entityLimits = KeyMaps.createIdentityHashMap(KeyIndicator.ENTITY_TYPE);
    public final Map<PotionEffectType, Value<Integer>> islandEffects = new LinkedHashMap<>();
    public final List<ItemStack[]> islandChests = new ArrayList<>(plugin.getSettings().getIslandChests().getDefaultPages());
    public final Map<PlayerRole, Value<Integer>> roleLimits = new LinkedHashMap<>();
    public final EnumMap<World.Environment, Location> visitorHomes = new EnumMap<>(World.Environment.class);
    public Value<Integer> islandSize = Value.syncedFixed(-1);
    public Value<Integer> warpsLimit = Value.syncedFixed(-1);
    public Value<Integer> teamLimit = Value.syncedFixed(-1);
    public Value<Integer> coopLimit = Value.syncedFixed(-1);
    public Value<Double> cropGrowth = Value.syncedFixed(-1D);
    public Value<Double> spawnerRates = Value.syncedFixed(-1D);
    public Value<Double> mobDrops = Value.syncedFixed(-1D);
    public Value<BigDecimal> bankLimit = Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE);
    public BigDecimal balance = BigDecimal.ZERO;
    public long lastInterestTime = System.currentTimeMillis() / 1000;
    public List<WarpRecord> warps = new LinkedList<>();
    public List<WarpCategoryRecord> warpCategories = new LinkedList<>();
    public List<BankTransaction> bankTransactions = new LinkedList<>();
    public byte[] persistentData = new byte[0];

    public IslandBuilderImpl() {

    }

    @Override
    public Island.Builder setOwner(@Nullable SuperiorPlayer owner) {
        this.owner = owner;
        return this;
    }

    @Override
    @Nullable
    public SuperiorPlayer getOwner() {
        return this.owner;
    }

    @Override
    public Island.Builder setUniqueId(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        Preconditions.checkState(plugin.getGrid().getIslandByUUID(uuid) == null, "The provided uuid is not unique.");
        this.uuid = uuid;
        return this;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Island.Builder setCenter(Location center) {
        Preconditions.checkNotNull(center, "center parameter cannot be null.");
        Preconditions.checkState(isValidCenter(center), "The provided center is not centered. center: " + center +
                ", maxIslandSize: " + plugin.getSettings().getMaxIslandSize());
        this.center = center;
        return this;
    }

    @Override
    public Location getCenter() {
        return this.center;
    }

    @Override
    public Island.Builder setName(String islandName) {
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        this.islandName = islandName;
        return this;
    }

    @Override
    public String getName() {
        return this.islandName;
    }

    @Override
    public Island.Builder setSchematicName(String islandType) {
        Preconditions.checkNotNull(islandType, "islandType parameter cannot be null.");
        this.islandType = islandType;
        return this;
    }

    @Override
    public String getScehmaticName() {
        return this.islandType;
    }

    @Override
    public Island.Builder setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public Island.Builder setDiscord(String discord) {
        Preconditions.checkNotNull(discord, "discord parameter cannot be null.");
        this.discord = discord;
        return this;
    }

    @Override
    public String getDiscord() {
        return this.discord;
    }

    @Override
    public Island.Builder setPaypal(String paypal) {
        Preconditions.checkNotNull(paypal, "paypal parameter cannot be null.");
        this.paypal = paypal;
        return this;
    }

    @Override
    public String getPaypal() {
        return this.paypal;
    }

    @Override
    public Island.Builder setBonusWorth(BigDecimal bonusWorth) {
        Preconditions.checkNotNull(bonusWorth, "bonusWorth parameter cannot be null.");
        this.bonusWorth = bonusWorth;
        return this;
    }

    @Override
    public BigDecimal getBonusWorth() {
        return this.bonusWorth;
    }

    @Override
    public Island.Builder setBonusLevel(BigDecimal bonusLevel) {
        Preconditions.checkNotNull(bonusLevel, "bonusLevel parameter cannot be null.");
        this.bonusLevel = bonusLevel;
        return this;
    }

    @Override
    public BigDecimal getBonusLevel() {
        return this.bonusLevel;
    }

    @Override
    public Island.Builder setLocked(boolean isLocked) {
        this.isLocked = isLocked;
        return this;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public Island.Builder setIgnored(boolean isIgnored) {
        this.isIgnored = isIgnored;
        return this;
    }

    @Override
    public boolean isIgnored() {
        return this.isIgnored;
    }

    @Override
    public Island.Builder setDescription(String description) {
        Preconditions.checkNotNull(description, "description parameter cannot be null.");
        this.description = description;
        return this;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public Island.Builder setGeneratedSchematics(int generatedSchematicsMask) {
        this.generatedSchematicsMask = generatedSchematicsMask;
        return this;
    }

    @Override
    public int getGeneratedSchematicsMask() {
        return this.generatedSchematicsMask;
    }

    @Override
    public Island.Builder setUnlockedWorlds(int unlockedWorldsMask) {
        this.unlockedWorldsMask = unlockedWorldsMask;
        return this;
    }

    @Override
    public int getUnlockedWorldsMask() {
        return this.unlockedWorldsMask;
    }

    @Override
    public Island.Builder setLastTimeUpdated(long lastTimeUpdated) {
        this.lastTimeUpdated = lastTimeUpdated;
        return this;
    }

    @Override
    public long getLastTimeUpdated() {
        return this.lastTimeUpdated;
    }

    @Override
    public Island.Builder setDirtyChunk(String worldName, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null.");
        this.dirtyChunks.add(new DirtyChunk(worldName, chunkX, chunkZ));
        return this;
    }

    @Override
    public boolean isDirtyChunk(String worldName, int chunkX, int chunkZ) {
        return this.dirtyChunks.contains(new DirtyChunk(worldName, chunkX, chunkZ));
    }

    @Override
    public Island.Builder setBlockCount(Key block, BigInteger count) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        Preconditions.checkNotNull(count, "count parameter cannot be null.");
        this.blockCounts.put(block, count);
        return this;
    }

    @Override
    public KeyMap<BigInteger> getBlockCounts() {
        return KeyMaps.unmodifiableKeyMap(this.blockCounts);
    }

    @Override
    public Island.Builder setIslandHome(Location location, World.Environment environment) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        this.islandHomes.put(environment, location);
        return this;
    }

    @Override
    public Map<World.Environment, Location> getIslandHomes() {
        return Collections.unmodifiableMap(this.islandHomes);
    }

    @Override
    public Island.Builder addIslandMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        this.members.add(superiorPlayer);
        return this;
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers() {
        return Collections.unmodifiableList(this.members);
    }

    @Override
    public Island.Builder addBannedPlayer(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        this.bannedPlayers.add(superiorPlayer);
        return this;
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return Collections.unmodifiableList(this.bannedPlayers);
    }

    @Override
    public Island.Builder setPlayerPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        this.playerPermissions.computeIfAbsent(superiorPlayer, p -> new PlayerPrivilegeNode(superiorPlayer, null))
                .loadPrivilege(islandPrivilege, (byte) (value ? 1 : 0));
        return this;
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return Collections.unmodifiableMap(this.playerPermissions);
    }

    @Override
    public Island.Builder setRolePermission(IslandPrivilege islandPrivilege, PlayerRole requiredRole) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        Preconditions.checkNotNull(requiredRole, "requiredRole parameter cannot be null.");
        this.rolePermissions.put(islandPrivilege, requiredRole);
        return this;
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return Collections.unmodifiableMap(this.rolePermissions);
    }

    @Override
    public Island.Builder setUpgrade(Upgrade upgrade, int level) {
        Preconditions.checkNotNull(upgrade, "upgrade parameter cannot be null.");
        this.upgrades.put(upgrade.getName(), level);
        return this;
    }

    @Override
    public Map<Upgrade, Integer> getUpgrades() {
        return Collections.unmodifiableMap(this.upgrades.entrySet().stream().collect(Collectors.toMap(
                entry -> plugin.getUpgrades().getUpgrade(entry.getKey()),
                Map.Entry::getValue
        )));
    }

    @Override
    public Island.Builder setBlockLimit(Key block, int limit) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        this.blockLimits.put(block, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        return this;
    }

    @Override
    public KeyMap<Integer> getBlockLimits() {
        return KeyMap.createKeyMap(convertFromValuesToRaw(this.blockLimits));
    }

    @Override
    public Island.Builder setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(rating, "rating parameter cannot be null.");
        this.ratings.put(superiorPlayer.getUniqueId(), rating);
        return this;
    }

    @Override
    public Map<SuperiorPlayer, Rating> getRatings() {
        return Collections.unmodifiableMap(this.ratings.entrySet().stream().collect(Collectors.toMap(
                entry -> plugin.getPlayers().getSuperiorPlayer(entry.getKey()),
                Map.Entry::getValue
        )));
    }

    @Override
    public Island.Builder setCompletedMission(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.completedMissions.put(new MissionReference(mission), new Counter(finishCount));
        return this;
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissions() {
        Map<Mission<?>, Integer> completedMissions = new LinkedHashMap<>();

        this.completedMissions.forEach((mission, finishCount) -> {
            if (mission.isValid())
                completedMissions.put(mission.getMission(), finishCount.get());
        });

        return completedMissions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(completedMissions);
    }

    @Override
    public Island.Builder setIslandFlag(IslandFlag islandFlag, boolean value) {
        Preconditions.checkNotNull(islandFlag, "islandFlag parameter cannot be null.");
        this.islandFlags.put(islandFlag, (byte) (value ? 1 : 0));
        return this;
    }

    @Override
    public Map<IslandFlag, SyncStatus> getIslandFlags() {
        return Collections.unmodifiableMap(this.islandFlags.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() == 1 ? SyncStatus.ENABLED : SyncStatus.DISABLED
        )));
    }

    @Override
    public Island.Builder setGeneratorRate(Key block, int rate, World.Environment environment) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        this.cobbleGeneratorValues.computeIfAbsent(environment, e -> KeyMaps.createHashMap(KeyIndicator.MATERIAL))
                .put(block, rate < 0 ? Value.syncedFixed(rate) : Value.fixed(rate));
        return this;
    }

    @Override
    public Map<World.Environment, KeyMap<Integer>> getGeneratorRates() {
        Map<World.Environment, KeyMap<Integer>> result = new EnumMap<>(World.Environment.class);

        this.cobbleGeneratorValues.forEach(((environment, generatorRates) ->
                result.put(environment, KeyMap.createKeyMap(convertFromValuesToRaw(generatorRates)))));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public Island.Builder addUniqueVisitor(SuperiorPlayer superiorPlayer, long visitTime) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        this.uniqueVisitors.add(new SIsland.UniqueVisitor(superiorPlayer, visitTime));
        return this;
    }

    @Override
    public Map<SuperiorPlayer, Long> getUniqueVisitors() {
        LinkedHashMap<SuperiorPlayer, Long> result = new LinkedHashMap<>();
        this.uniqueVisitors.forEach(uniqueVisitor ->
                result.put(uniqueVisitor.getSuperiorPlayer(), uniqueVisitor.getLastVisitTime()));
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Island.Builder setEntityLimit(Key entity, int limit) {
        Preconditions.checkNotNull(entity, "entity parameter cannot be null.");
        this.entityLimits.put(entity, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        return this;
    }

    @Override
    public KeyMap<Integer> getEntityLimits() {
        return KeyMap.createKeyMap(convertFromValuesToRaw(this.entityLimits));
    }

    @Override
    public Island.Builder setIslandEffect(PotionEffectType potionEffectType, int level) {
        Preconditions.checkNotNull(potionEffectType, "potionEffectType parameter cannot be null.");
        this.islandEffects.put(potionEffectType, level < 0 ? Value.syncedFixed(level) : Value.fixed(level));
        return this;
    }

    @Override
    public Map<PotionEffectType, Integer> getIslandEffects() {
        return Collections.unmodifiableMap(convertFromValuesToRaw(this.islandEffects));
    }

    @Override
    public Island.Builder setIslandChest(int index, ItemStack[] contents) {
        Preconditions.checkNotNull(contents, "contents parameter cannot be null.");

        if (index >= this.islandChests.size()) {
            while (index > this.islandChests.size()) {
                this.islandChests.add(new ItemStack[plugin.getSettings().getIslandChests().getDefaultSize() * 9]);
            }

            this.islandChests.add(contents);
        } else {
            this.islandChests.set(index, contents);
        }

        return this;
    }

    @Override
    public List<ItemStack[]> getIslandChests() {
        return Collections.unmodifiableList(this.islandChests);
    }

    @Override
    public Island.Builder setRoleLimit(PlayerRole playerRole, int limit) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        this.roleLimits.put(playerRole, limit < 0 ? Value.syncedFixed(limit) : Value.fixed(limit));
        return this;
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return Collections.unmodifiableMap(convertFromValuesToRaw(this.roleLimits));
    }

    @Override
    public Island.Builder setVisitorHome(Location location, World.Environment environment) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        this.visitorHomes.put(environment, location);
        return this;
    }

    @Override
    public Map<World.Environment, Location> getVisitorHomes() {
        return Collections.unmodifiableMap(visitorHomes);
    }

    @Override
    public Island.Builder setIslandSize(int islandSize) {
        this.islandSize = islandSize < 0 ? Value.syncedFixed(islandSize) : Value.fixed(islandSize);
        return this;
    }

    @Override
    public int getIslandSize() {
        return this.islandSize.get();
    }

    @Override
    public Island.Builder setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit < 0 ? Value.syncedFixed(teamLimit) : Value.fixed(teamLimit);
        return this;
    }

    @Override
    public int getTeamLimit() {
        return this.teamLimit.get();
    }

    @Override
    public Island.Builder setWarpsLimit(int warpsLimit) {
        this.warpsLimit = warpsLimit < 0 ? Value.syncedFixed(warpsLimit) : Value.fixed(warpsLimit);
        return this;
    }

    @Override
    public int getWarpsLimit() {
        return this.warpsLimit.get();
    }

    @Override
    public Island.Builder setCropGrowth(double cropGrowth) {
        this.cropGrowth = cropGrowth < 0 ? Value.syncedFixed(cropGrowth) : Value.fixed(cropGrowth);
        return this;
    }

    @Override
    public double getCropGrowth() {
        return this.cropGrowth.get();
    }

    @Override
    public Island.Builder setSpawnerRates(double spawnerRates) {
        this.spawnerRates = spawnerRates < 0 ? Value.syncedFixed(spawnerRates) : Value.fixed(spawnerRates);
        return this;
    }

    @Override
    public double getSpawnerRates() {
        return this.spawnerRates.get();
    }

    @Override
    public Island.Builder setMobDrops(double mobDrops) {
        this.mobDrops = mobDrops < 0 ? Value.syncedFixed(mobDrops) : Value.fixed(mobDrops);
        return this;
    }

    @Override
    public double getMobDrops() {
        return this.mobDrops.get();
    }

    @Override
    public Island.Builder setCoopLimit(int coopLimit) {
        this.coopLimit = coopLimit < 0 ? Value.syncedFixed(coopLimit) : Value.fixed(coopLimit);
        return this;
    }

    @Override
    public int getCoopLimit() {
        return this.coopLimit.get();
    }

    @Override
    public Island.Builder setBankLimit(BigDecimal bankLimit) {
        Preconditions.checkNotNull(bankLimit, "bankLimit parameter cannot be null.");
        this.bankLimit = bankLimit.compareTo(SYNCED_BANK_LIMIT_VALUE) <= 0 ? Value.syncedFixed(bankLimit) : Value.fixed(bankLimit);
        return this;
    }

    @Override
    public BigDecimal getBankLimit() {
        return this.bankLimit.get();
    }

    @Override
    public Island.Builder setBalance(BigDecimal balance) {
        Preconditions.checkNotNull(balance, "balance parameter cannot be null.");
        this.balance = balance;
        return this;
    }

    @Override
    public BigDecimal getBalance() {
        return this.balance;
    }

    @Override
    public Island.Builder setLastInterestTime(long lastInterestTime) {
        this.lastInterestTime = lastInterestTime;
        return this;
    }

    @Override
    public long getLastInterestTime() {
        return this.lastInterestTime;
    }

    @Override
    public Island.Builder addWarp(String name, String category, Location location, boolean isPrivate, @Nullable ItemStack icon) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        Preconditions.checkNotNull(category, "category parameter cannot be null.");
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        this.warps.add(new WarpRecord(name, category, LazyWorldLocation.of(location), isPrivate, icon));
        return this;
    }

    @Override
    public boolean hasWarp(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null");

        for (WarpRecord warpRecord : this.warps) {
            if (warpRecord.name.equals(name))
                return true;
        }

        return false;
    }

    @Override
    public boolean hasWarp(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null");

        for (WarpRecord warpRecord : this.warps) {
            if (warpRecord.location.equals(location))
                return true;
        }

        return false;
    }

    @Override
    public Island.Builder addWarpCategory(String name, int slot, @Nullable ItemStack icon) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        Preconditions.checkArgument(slot >= 0, "slot must be positive.");
        this.warpCategories.add(new WarpCategoryRecord(name, slot, icon));
        return this;
    }

    @Override
    public boolean hasWarpCategory(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null");

        for (WarpCategoryRecord warpCategoryRecord : this.warpCategories) {
            if (warpCategoryRecord.name.equals(name))
                return true;
        }

        return false;
    }

    @Override
    public Island.Builder addBankTransaction(BankTransaction bankTransaction) {
        Preconditions.checkNotNull(bankTransaction, "bankTransaction parameter cannot be null.");
        this.bankTransactions.add(bankTransaction);
        return this;
    }

    @Override
    public List<BankTransaction> getBankTransactions() {
        return Collections.unmodifiableList(this.bankTransactions);
    }

    @Override
    public Island.Builder setPersistentData(byte[] persistentData) {
        Preconditions.checkNotNull(persistentData, "persistentData parameter cannot be null.");
        this.persistentData = persistentData;
        return this;
    }

    @Override
    public byte[] getPersistentData() {
        return this.persistentData;
    }

    @Override
    public Island build() {
        if (this.uuid == null)
            throw new IllegalStateException("Cannot create an island with no valid uuid.");
        if (this.center == null)
            throw new IllegalStateException("Cannot create an island with no valid location.");

        return plugin.getFactory().createIsland(this);
    }

    private static boolean isValidCenter(Location center) {
        int maxIslandSize = plugin.getSettings().getMaxIslandSize() * 3;
        return center.getBlockX() % maxIslandSize == 0 && center.getBlockZ() % maxIslandSize == 0 &&
                plugin.getGrid().getIslandAt(center) == null;
    }

    private static <K, V extends Number> Map<K, V> convertFromValuesToRaw(Map<K, Value<V>> input) {
        return input.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get()
        ));
    }

}
