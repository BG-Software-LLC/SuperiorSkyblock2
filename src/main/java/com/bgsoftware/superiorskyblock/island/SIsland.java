package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.IslandDataHandler;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.bank.IslandBank;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.EmptyDataHandler;
import com.bgsoftware.superiorskyblock.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.database.cache.CachedIslandInfo;
import com.bgsoftware.superiorskyblock.database.cache.DatabaseCache;
import com.bgsoftware.superiorskyblock.database.serialization.IslandsDeserializer;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.flags.IslandFlags;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.permissions.PermissionNodeAbstract;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.island.warps.SWarpCategory;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMapImpl;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeIslandEffects;
import com.bgsoftware.superiorskyblock.serialization.Serializers;
import com.bgsoftware.superiorskyblock.structure.CompletableFutureList;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.upgrade.DefaultUpgradeLevel;
import com.bgsoftware.superiorskyblock.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.locations.SmartLocation;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.wrappers.IslandArea;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class SIsland implements Island {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static int blocksUpdateCounter = 0;

    private final DatabaseBridge databaseBridge = plugin.getFactory().createDatabaseBridge(this);
    private final IslandBank islandBank = plugin.getFactory().createIslandBank(this, this::hasGiveInterestFailed);
    private final IslandCalculationAlgorithm calculationAlgorithm = plugin.getFactory().createIslandCalculationAlgorithm(this);
    private final IslandBlocksTrackerAlgorithm blocksTracker = plugin.getFactory().createIslandBlocksTrackerAlgorithm(this);
    private final IslandEntitiesTrackerAlgorithm entitiesTracker = plugin.getFactory().createIslandEntitiesTrackerAlgorithm(this);
    private final SyncedObject<BukkitTask> bankInterestTask = SyncedObject.of(null);

    /*
     * Island Flags
     */
    private volatile boolean beingRecalculated = false;

    /*
     * Island Identifiers
     */
    private final UUID uuid;
    private SuperiorPlayer owner;
    private final BlockPosition center;
    private final long creationTime;
    private String creationTimeDate;
    private final String schemName;

    /*
     * Island Time-Trackers
     */
    private volatile long lastTimeUpdate = -1;
    private volatile long lastInterest = -1L;
    private volatile long lastUpgradeTime = -1L;
    private volatile boolean giveInterestFailed = false;

    /*
     * Island Upgrade Values
     */
    private UpgradeValue<Integer> islandSize = UpgradeValue.NEGATIVE;
    private UpgradeValue<Integer> warpsLimit = UpgradeValue.NEGATIVE;
    private UpgradeValue<Integer> teamLimit = UpgradeValue.NEGATIVE;
    private UpgradeValue<Integer> coopLimit = UpgradeValue.NEGATIVE;
    private UpgradeValue<Double> cropGrowth = UpgradeValue.NEGATIVE_DOUBLE;
    private UpgradeValue<Double> spawnerRates = UpgradeValue.NEGATIVE_DOUBLE;
    private UpgradeValue<Double> mobDrops = UpgradeValue.NEGATIVE_DOUBLE;
    private UpgradeValue<BigDecimal> bankLimit = new UpgradeValue<>(new BigDecimal(-2), true);
    private final Map<PlayerRole, UpgradeValue<Integer>> roleLimits = new ConcurrentHashMap<>();
    private final KeyMap<UpgradeValue<Integer>>[] cobbleGeneratorValues = new KeyMap[World.Environment.values().length];
    private final KeyMap<UpgradeValue<Integer>> entityLimits = KeyMapImpl.createConcurrentHashMap();
    private final Map<PotionEffectType, UpgradeValue<Integer>> islandEffects = new ConcurrentHashMap<>();
    private final KeyMap<UpgradeValue<Integer>> blockLimits = KeyMapImpl.createConcurrentHashMap();

    /*
     * Island Player-Trackers
     */
    private final SyncedObject<SortedSet<SuperiorPlayer>> members = SyncedObject.of(new TreeSet<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final SyncedObject<SortedSet<SuperiorPlayer>> playersInside = SyncedObject.of(new TreeSet<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final SyncedObject<SortedSet<UniqueVisitor>> uniqueVisitors = SyncedObject.of(new TreeSet<>(SortingComparators.PAIRED_PLAYERS_NAMES_COMPARATOR));
    private final Set<SuperiorPlayer> bannedPlayers = Sets.newConcurrentHashSet();
    private final Set<SuperiorPlayer> coopPlayers = Sets.newConcurrentHashSet();
    private final Set<SuperiorPlayer> invitedPlayers = Sets.newConcurrentHashSet();
    private final Map<SuperiorPlayer, PlayerPermissionNode> playerPermissions = new ConcurrentHashMap<>();
    private final Map<UUID, Rating> ratings = new ConcurrentHashMap<>();

    /*
     * Island Warps
     */
    private final Map<String, IslandWarp> warpsByName = new ConcurrentHashMap<>();
    private final Map<Location, IslandWarp> warpsByLocation = new ConcurrentHashMap<>();
    private final Map<String, WarpCategory> warpCategories = new ConcurrentHashMap<>();

    /*
     * General Settings
     */
    private final SyncedObject<Location[]> islandHomes = SyncedObject.of(new Location[World.Environment.values().length]);
    private final SyncedObject<Location[]> visitorHomes = SyncedObject.of(new Location[World.Environment.values().length]);
    private final Map<IslandPrivilege, PlayerRole> rolePermissions = new ConcurrentHashMap<>();
    private final Map<IslandFlag, Byte> islandFlags = new ConcurrentHashMap<>();
    private final Map<String, Integer> upgrades = new ConcurrentHashMap<>();
    private final AtomicReference<BigDecimal> islandWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> islandLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final Map<Mission<?>, Integer> completedMissions = new ConcurrentHashMap<>();
    private final SyncedObject<IslandChest[]> islandChests = SyncedObject.of(new IslandChest[plugin.getSettings().getIslandChests().getDefaultPages()]);
    private volatile String discord = "None";
    private volatile String paypal = "None";
    private volatile boolean isLocked = false;
    private volatile boolean isTopIslandsIgnored = false;
    private volatile String islandName;
    private volatile String islandRawName;
    private volatile String description = "";
    private volatile Biome biome = null;
    private final AtomicInteger generatedSchematics = new AtomicInteger(0);
    private final AtomicInteger unlockedWorlds = new AtomicInteger(0);

    public SIsland(@Nullable SuperiorPlayer owner, UUID uuid, Location location, String islandName,
                   @Nullable String schemName) {
        this(owner, uuid, location, islandName, schemName, System.currentTimeMillis() / 1000);

        setSchematicGenerate(plugin.getSettings().getWorlds().getDefaultWorld());
        setLastInterestTime(this.creationTime);
        updateDatesFormatter();
        assignIslandChest();
        updateUpgrades();

        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class))
            this.entitiesTracker.recalculateEntityCounts();

        databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
    }

    private SIsland(@Nullable SuperiorPlayer owner, UUID uuid, Location location, String islandName,
                    @Nullable String schemName, long creationTime) {
        this.uuid = uuid;
        this.owner = owner;

        if (owner != null) {
            owner.setPlayerRole(SPlayerRole.lastRole());
            owner.setIsland(this);
        }

        this.center = new SBlockPosition(location);
        this.creationTime = creationTime;
        this.islandName = islandName;
        this.islandRawName = Formatters.STRIP_COLOR_FORMATTER.format(islandName);
        this.schemName = schemName;
    }

    public static Optional<Island> fromDatabase(DatabaseCache<CachedIslandInfo> cache, DatabaseResult resultSet) {
        Optional<UUID> uuid = resultSet.getUUID("uuid");
        if (!uuid.isPresent()) {
            SuperiorSkyblockPlugin.log("&cCannot load island with invalid uuid, skipping...");
            return Optional.empty();
        }

        Optional<SuperiorPlayer> owner = resultSet.getUUID("owner").map(plugin.getPlayers()::getSuperiorPlayer);
        if (!owner.isPresent()) {
            SuperiorSkyblockPlugin.log("&cCannot load island with invalid owner uuid, skipping...");
            return Optional.empty();
        }

        Optional<Location> center = resultSet.getString("center").map(Serializers.LOCATION_SERIALIZER::deserialize);
        if (!center.isPresent()) {
            SuperiorSkyblockPlugin.log("&cCannot load island with invalid center, skipping...");
            return Optional.empty();
        }

        if (center.get().getWorld() == null) {
            SuperiorSkyblockPlugin.log(
                    String.format("&cCannot load island invalid world %s for %s, skipping...",
                            ((SmartLocation) center.get()).getWorldName(), uuid.get()));
            return Optional.empty();
        }

        PluginDebugger.debug("Action: Load Island, UUID: " + uuid.get() + ", Owner: " + owner.get().getUniqueId());

        SIsland island = new SIsland(
                owner.get(),
                uuid.get(),
                center.get(),
                resultSet.getString("name").orElse(""),
                resultSet.getString("island_type").orElse(null),
                resultSet.getLong("creation_time").orElse(System.currentTimeMillis() / 1000L)
        );

        try {
            island.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.IDLE);

            island.discord = resultSet.getString("discord").orElse("None");
            island.paypal = resultSet.getString("paypal").orElse("None");
            island.bonusWorth.set(resultSet.getBigDecimal("worth_bonus").orElse(BigDecimal.ZERO));
            island.bonusLevel.set(resultSet.getBigDecimal("levels_bonus").orElse(BigDecimal.ZERO));
            island.isLocked = resultSet.getBoolean("locked").orElse(false);
            island.isTopIslandsIgnored = resultSet.getBoolean("ignored").orElse(false);
            island.description = resultSet.getString("description").orElse("");
            island.generatedSchematics.set(resultSet.getInt("generated_schematics").orElse(0));
            island.unlockedWorlds.set(resultSet.getInt("unlocked_worlds").orElse(0));
            island.lastTimeUpdate = resultSet.getLong("last_time_updated").orElse(System.currentTimeMillis() / 1000L);

            Optional<String> dirtyChunks = resultSet.getString("dirty_chunks");
            if (dirtyChunks.isPresent())
                ChunksTracker.deserialize(plugin.getGrid(), island, dirtyChunks.get());

            Optional<String> blockCountsString = resultSet.getString("block_counts");
            if (blockCountsString.isPresent())
                Executor.sync(() -> island.deserializeBlockCounts(blockCountsString.get()), 5L);

            CachedIslandInfo cachedIslandInfo = cache.getCachedInfo(uuid.get());

            if (cachedIslandInfo != null)
                island.loadFromCachedInfo(cachedIslandInfo);

            island.updateDatesFormatter();
            island.startBankInterest();
            island.checkMembersDuplication();
            island.updateOldUpgradeValues();
            island.updateUpgrades();
            island.updateIslandChests();

            // We want to save all the limits to the custom block keys
            plugin.getBlockValues().addCustomBlockKeys(island.blockLimits.keySet());
        } finally {
            island.getDatabaseBridge().setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
        }

        return Optional.of(island);
    }

    /*
     *  General methods
     */

    @Override
    public SuperiorPlayer getOwner() {
        return owner;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getCreationTimeDate() {
        return creationTimeDate;
    }

    @Override
    public void updateDatesFormatter() {
        this.creationTimeDate = Formatters.DATE_FORMATTER.format(new Date(creationTime * 1000));
    }

    /*
     *  Player related methods
     */

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        List<SuperiorPlayer> members = new ArrayList<>();

        if (includeOwner)
            members.add(owner);

        this.members.read(members::addAll);

        return members;
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        Preconditions.checkNotNull(playerRoles, "playerRoles parameter cannot be null.");

        List<PlayerRole> rolesToFilter = Arrays.asList(playerRoles);
        List<SuperiorPlayer> members = new ArrayList<>();

        if (rolesToFilter.contains(SPlayerRole.lastRole()))
            members.add(owner);

        members.addAll(this.members.readAndGet(_members -> _members.stream().filter(superiorPlayer ->
                rolesToFilter.contains(superiorPlayer.getPlayerRole())).collect(Collectors.toList())));

        return members;
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(this.bannedPlayers));
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return getIslandVisitors(true);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
        return playersInside.readAndGet(playersInside -> playersInside.stream()
                .filter(superiorPlayer -> !isMember(superiorPlayer) && (vanishPlayers || superiorPlayer.isShownAsOnline()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return playersInside.readAndGet(playersInside -> playersInside.stream().filter(SuperiorPlayer::isOnline).collect(Collectors.toList()));
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        return uniqueVisitors.readAndGet(uniqueVisitors -> uniqueVisitors.stream().map(UniqueVisitor::getSuperiorPlayer).collect(Collectors.toList())
        );
    }

    @Override
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
        return uniqueVisitors.readAndGet(uniqueVisitors -> uniqueVisitors.stream().map(UniqueVisitor::toPair).collect(Collectors.toList()));
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Invite, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        invitedPlayers.add(superiorPlayer);
        //Revoke the invite after 5 minutes
        Executor.sync(() -> revokeInvite(superiorPlayer), 6000L);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Invite Revoke, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        invitedPlayers.remove(superiorPlayer);
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return invitedPlayers.contains(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(this.invitedPlayers));
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        PluginDebugger.debug("Action: Add Member, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Role: " + playerRole);

        boolean addedNewMember = members.writeAndGet(members -> members.add(superiorPlayer));

        // This player is already an member of the island
        if (!addedNewMember)
            return;

        // Removing player from being a coop.
        if (isCoop(superiorPlayer)) {
            removeCoop(superiorPlayer);
        }

        superiorPlayer.setIsland(this);
        superiorPlayer.setPlayerRole(playerRole);

        plugin.getMenus().refreshMembers(this);

        updateLastTime();

        if (superiorPlayer.isOnline()) {
            updateIslandFly(superiorPlayer);
            setCurrentlyActive();
        }

        IslandsDatabaseBridge.addMember(this, superiorPlayer, System.currentTimeMillis());
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Kick Member, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        boolean removedMember = members.writeAndGet(members -> members.remove(superiorPlayer));

        if (!removedMember) {
            // If the remove method failed, we iterate through all the members and remove the member manually.
            // Should fix issues if members are not in the correct order.
            // Reference: https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/734
            removedMember = members.writeAndGet(members -> members.removeIf(superiorPlayer::equals));
        }

        // This player is not a member of the island.
        if (!removedMember)
            return;

        superiorPlayer.setIsland(null);

        if (superiorPlayer.isOnline()) {
            SuperiorMenu.killMenu(superiorPlayer);
            if (plugin.getSettings().isTeleportOnKick() && getAllPlayersInside().contains(superiorPlayer)) {
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            } else {
                updateIslandFly(superiorPlayer);
            }
        }

        plugin.getMissions().getAllMissions().stream().filter(mission -> {
            MissionData missionData = plugin.getMissions().getMissionData(mission).orElse(null);
            return missionData != null && missionData.isLeaveReset();
        }).forEach(superiorPlayer::resetMission);

        plugin.getMenus().destroyMemberManage(superiorPlayer);
        plugin.getMenus().destroyMemberRole(superiorPlayer);
        plugin.getMenus().refreshMembers(this);

        IslandsDatabaseBridge.removeMember(this, superiorPlayer);
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return owner.equals(superiorPlayer.getIslandLeader());
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer) {
        banMember(superiorPlayer, null);
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer, SuperiorPlayer whom) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Ban Player, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        boolean bannedPlayer = bannedPlayers.add(superiorPlayer);

        // This player is already banned.
        if (!bannedPlayer)
            return;

        if (isMember(superiorPlayer))
            kickMember(superiorPlayer);

        Location location = superiorPlayer.getLocation();

        if (location != null && isInside(location))
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());

        IslandsDatabaseBridge.addBannedPlayer(this,
                superiorPlayer, whom == null ? CONSOLE_UUID : whom.getUniqueId(),
                System.currentTimeMillis());
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Unban Player, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        boolean unbannedPlayer = bannedPlayers.remove(superiorPlayer);

        if (unbannedPlayer)
            IslandsDatabaseBridge.removeBannedPlayer(this, superiorPlayer);
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return bannedPlayers.contains(superiorPlayer);
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Coop, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        boolean coopPlayer = coopPlayers.add(superiorPlayer);

        if (coopPlayer)
            plugin.getMenus().refreshCoops(this);
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Uncoop, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        boolean uncoopPlayer = coopPlayers.remove(superiorPlayer);

        // This player was not coop.
        if (!uncoopPlayer)
            return;

        Location location = superiorPlayer.getLocation();

        if (isLocked() && location != null && isInside(location)) {
            SuperiorMenu.killMenu(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }

        plugin.getMenus().refreshCoops(this);
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return coopPlayers.contains(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(this.coopPlayers));
    }

    @Override
    public int getCoopLimit() {
        return this.coopLimit.get();
    }

    @Override
    public int getCoopLimitRaw() {
        return this.coopLimit.isSynced() ? -1 : this.coopLimit.get();
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        coopLimit = Math.max(0, coopLimit);

        PluginDebugger.debug("Action: Set Coop Limit, Island: " + owner.getName() + ", Coop Limit: " + coopLimit);

        // Original and new coop limit are the same
        if (coopLimit == getCoopLimitRaw())
            return;

        this.coopLimit = new UpgradeValue<>(coopLimit, false);
        IslandsDatabaseBridge.saveCoopLimit(this);
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (inside) {
            PluginDebugger.debug("Action: Entered Island, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        } else {
            PluginDebugger.debug("Action: Left Island, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        }

        boolean changePlayers = playersInside.writeAndGet(playersInside -> {
            if (inside)
                return playersInside.add(superiorPlayer);
            else
                return playersInside.remove(superiorPlayer);
        });

        // The players inside the player weren't changed.
        if (!changePlayers)
            return;

        if (!isMember(superiorPlayer) && superiorPlayer.isShownAsOnline()) {
            Optional<UniqueVisitor> uniqueVisitorOptional = uniqueVisitors.readAndGet(uniqueVisitors ->
                    uniqueVisitors.stream().filter(pair -> pair.getSuperiorPlayer().equals(superiorPlayer)).findFirst());

            long visitTime = System.currentTimeMillis();

            if (uniqueVisitorOptional.isPresent()) {
                uniqueVisitorOptional.get().setLastVisitTime(visitTime);
            } else {
                uniqueVisitors.write(uniqueVisitors -> uniqueVisitors.add(new UniqueVisitor(superiorPlayer, visitTime)));
            }

            plugin.getMenus().refreshUniqueVisitors(this);

            IslandsDatabaseBridge.saveVisitor(this, superiorPlayer, visitTime);
        }

        updateLastTime();

        plugin.getMenus().refreshVisitors(this);
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        return !isMember(superiorPlayer) && (!includeCoopStatus || !isCoop(superiorPlayer));
    }

    /*
     *  Location related methods
     */

    @Override
    public Location getCenter(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        World world = plugin.getGrid().getIslandsWorld(this, environment);

        Preconditions.checkNotNull(world, "Couldn't find world for environment " + environment + ".");

        return center.parse(world).add(0.5, 0, 0.5);
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        return this.getIslandHome(environment);
    }

    @Override
    public Map<World.Environment, Location> getTeleportLocations() {
        return this.getIslandHomes();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        this.setIslandHome(teleportLocation);
    }

    @Override
    public void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation) {
        this.setIslandHome(environment, teleportLocation);
    }

    @Override
    public Location getIslandHome(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        Location teleportLocation = islandHomes.readAndGet(teleportLocations -> teleportLocations[environment.ordinal()]);

        if (teleportLocation == null)
            teleportLocation = getCenter(environment);

        if (teleportLocation == null)
            return null;

        World world = plugin.getGrid().getIslandsWorld(this, environment);

        teleportLocation = teleportLocation.clone();
        teleportLocation.setWorld(world);

        return teleportLocation;
    }

    @Override
    public Map<World.Environment, Location> getIslandHomes() {
        return islandHomes.readAndGet(teleportLocations -> {
            Map<World.Environment, Location> map = new HashMap<>();
            for (World.Environment env : World.Environment.values()) {
                if (teleportLocations[env.ordinal()] != null)
                    map.put(env, teleportLocations[env.ordinal()]);
            }
            return Collections.unmodifiableMap(map);
        });
    }

    @Override
    public void setIslandHome(Location homeLocation) {
        Preconditions.checkNotNull(homeLocation, "homeLocation parameter cannot be null.");
        Preconditions.checkNotNull(homeLocation.getWorld(), "homeLocation's world cannot be null.");
        setIslandHome(homeLocation.getWorld().getEnvironment(), homeLocation);
    }

    @Override
    public void setIslandHome(World.Environment environment, @Nullable Location homeLocation) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        PluginDebugger.debug("Action: Change Home Location, Island: " + owner.getName() + ", Location: " +
                Formatters.LOCATION_FORMATTER.format(homeLocation));

        islandHomes.write(islandHomes -> islandHomes[environment.ordinal()] =
                homeLocation == null ? null : homeLocation.clone());

        IslandsDatabaseBridge.saveIslandHome(this, environment, homeLocation);
    }

    @Override
    public Location getVisitorsLocation() {
        Location visitorsLocation = this.visitorHomes.readAndGet(visitorsLocations -> visitorsLocations[0]);

        if (visitorsLocation == null)
            return null;

        World world = plugin.getGrid().getIslandsWorld(this, plugin.getSettings().getWorlds().getDefaultWorld());
        visitorsLocation.setWorld(world);

        return visitorsLocation;
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        if (visitorsLocation == null) {
            PluginDebugger.debug("Action: Delete Visitors Location, Island: " + owner.getName());
            this.visitorHomes.write(visitorsLocations -> visitorsLocations[0] = null);
            IslandsDatabaseBridge.removeVisitorLocation(this, World.Environment.NORMAL);
        } else {
            PluginDebugger.debug("Action: Change Visitors Location, Island: " + owner.getName() + ", Location: " +
                    Formatters.LOCATION_FORMATTER.format(visitorsLocation));
            this.visitorHomes.write(visitorsLocations -> visitorsLocations[0] = visitorsLocation.clone());
            IslandsDatabaseBridge.saveVisitorLocation(this, World.Environment.NORMAL, visitorsLocation);
        }
    }

    @Override
    public Location getMinimum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMinimumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).subtract(islandSize, 0, islandSize);
    }

    @Override
    public Location getMaximum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).add(islandSize, 0, islandSize);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        List<Chunk> chunks = new ArrayList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getAllChunks(environment, onlyProtected));
            } catch (NullPointerException ignored) {
            }
        }

        return chunks;
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(environment, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return getAllChunks(environment, onlyProtected, false);
    }

    @Override
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks).stream()
                .map(ChunkPosition::loadChunk).collect(Collectors.toList());
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        List<Chunk> chunks = new ArrayList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getLoadedChunks(environment, onlyProtected, noEmptyChunks));
            } catch (NullPointerException ignored) {
            }
        }

        return chunks;
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks).stream()
                .map(plugin.getNMSChunks()::getChunkIfLoaded).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getAllChunksAsync(this, world, onlyProtected, noEmptyChunks, ChunkLoadReason.API_REQUEST, onChunkLoad);
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        resetChunks(environment, onlyProtected, null);
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        World world = getCenter(environment).getWorld();
        List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, world, onlyProtected, true);

        if (chunkPositions.isEmpty()) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        IslandUtils.deleteChunks(this, chunkPositions, onFinish);
    }

    @Override
    public void resetChunks(boolean onlyProtected) {
        resetChunks(onlyProtected, null);
    }

    @Override
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        List<List<ChunkPosition>> worldsChunks = new ArrayList<>(
                IslandUtils.getChunkCoords(this, onlyProtected, true).values());

        if (worldsChunks.isEmpty()) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        for (int i = 0; i < worldsChunks.size() - 1; i++)
            IslandUtils.deleteChunks(this, worldsChunks.get(i), null);

        IslandUtils.deleteChunks(this, worldsChunks.get(worldsChunks.size() - 1), onFinish);
    }

    @Override
    public boolean isInside(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        IslandArea islandArea = new IslandArea(this.center, islandDistance);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return isInsideRange(location, 0);
    }

    public boolean isInsideRange(Location location, int extra) {
        if (location.getWorld() == null || !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.expand(extra);

        return islandArea.intercepts(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

        if (chunk.getWorld() == null || !plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return false;

        IslandArea islandArea = new IslandArea(center, getIslandSize());
        islandArea.rshift(4);

        return islandArea.intercepts(chunk.getX(), chunk.getZ());
    }

    @Override
    public boolean isNormalEnabled() {
        return plugin.getProviders().getWorldsProvider().isNormalUnlocked() || (unlockedWorlds.get() & 4) == 4;
    }

    @Override
    public void setNormalEnabled(boolean enabled) {
        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 4 : unlockedWorlds & 3);

        if (enabled) {
            PluginDebugger.debug("Action: Enable Normal, Island: " + owner.getName());
        } else {
            PluginDebugger.debug("Action: Disable Normal, Island: " + owner.getName());
        }

        IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public boolean isNetherEnabled() {
        return plugin.getProviders().getWorldsProvider().isNetherUnlocked() || (unlockedWorlds.get() & 1) == 1;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 1 : unlockedWorlds & 6);

        if (enabled) {
            PluginDebugger.debug("Action: Enable Nether, Island: " + owner.getName());
        } else {
            PluginDebugger.debug("Action: Disable Nether, Island: " + owner.getName());
        }

        IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public boolean isEndEnabled() {
        return plugin.getProviders().getWorldsProvider().isEndUnlocked() || (unlockedWorlds.get() & 2) == 2;
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 2 : unlockedWorlds & 5);

        if (enabled) {
            PluginDebugger.debug("Action: Enable End, Island: " + owner.getName());
        } else {
            PluginDebugger.debug("Action: Disable End, Island: " + owner.getName());
        }

        IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public int getUnlockedWorldsFlag() {
        return this.unlockedWorlds.get();
    }

    /*
     *  Permissions related methods
     */

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(sender, "sender parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        return sender instanceof ConsoleCommandSender || hasPermission(plugin.getPlayers().getSuperiorPlayer(sender), islandPrivilege);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PermissionNode playerNode = getPermissionNode(superiorPlayer);
        return superiorPlayer.hasBypassModeEnabled() || superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass.*") ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPrivilege.getName()) ||
                (playerNode != null && playerNode.hasPermission(islandPrivilege));
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        return getRequiredPlayerRole(islandPrivilege).getWeight() <= playerRole.getWeight();
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {
        if (value)
            this.setPermission(playerRole, islandPrivilege);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        PluginDebugger.debug("Action: Set Permission, Island: " + owner.getName() + ", Role: " + playerRole +
                ", Permission: " + islandPrivilege.getName());

        PlayerRole oldRole = rolePermissions.put(islandPrivilege, playerRole);

        if (islandPrivilege == IslandPrivileges.FLY) {
            getAllPlayersInside().forEach(this::updateIslandFly);
        } else if (islandPrivilege == IslandPrivileges.VILLAGER_TRADING) {
            getAllPlayersInside().forEach(superiorPlayer -> IslandUtils.updateTradingMenus(this, superiorPlayer));
        }

        if (oldRole != null)
            IslandsDatabaseBridge.removeRolePermission(this, oldRole);
        IslandsDatabaseBridge.saveRolePermission(this, playerRole, islandPrivilege);
    }

    @Override
    public void resetPermissions() {
        PluginDebugger.debug("Action: Reset Permissions, Island: " + owner.getName());

        rolePermissions.clear();

        getAllPlayersInside().forEach(superiorPlayer -> {
            updateIslandFly(superiorPlayer);
            IslandUtils.updateTradingMenus(this, superiorPlayer);
        });

        IslandsDatabaseBridge.clearRolePermissions(this);

        plugin.getMenus().refreshPermissions(this);
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        PluginDebugger.debug("Action: Set Permission, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Permission: " + islandPrivilege.getName() + ", Value: " + value);

        if (!playerPermissions.containsKey(superiorPlayer))
            playerPermissions.put(superiorPlayer, new PlayerPermissionNode(superiorPlayer, this));

        playerPermissions.get(superiorPlayer).setPermission(islandPrivilege, value);

        if (superiorPlayer.isOnline()) {
            if (islandPrivilege == IslandPrivileges.FLY) {
                updateIslandFly(superiorPlayer);
            } else if (islandPrivilege == IslandPrivileges.VILLAGER_TRADING) {
                IslandUtils.updateTradingMenus(this, superiorPlayer);
            }
        }

        IslandsDatabaseBridge.savePlayerPermission(this, superiorPlayer, islandPrivilege, value);

        plugin.getMenus().refreshPermissions(this, superiorPlayer);
    }

    @Override
    public void resetPermissions(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        PluginDebugger.debug("Action: Reset Permissions, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());

        playerPermissions.remove(superiorPlayer);

        if (superiorPlayer.isOnline()) {
            updateIslandFly(superiorPlayer);
            IslandUtils.updateTradingMenus(this, superiorPlayer);
        }

        IslandsDatabaseBridge.clearPlayerPermission(this, superiorPlayer);

        plugin.getMenus().refreshPermissions(this, superiorPlayer);
    }

    @Override
    public PermissionNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return playerPermissions.getOrDefault(superiorPlayer, new PlayerPermissionNode(superiorPlayer, this));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PlayerRole playerRole = rolePermissions.get(islandPrivilege);

        if (playerRole != null)
            return playerRole;

        return plugin.getRoles().getRoles().stream()
                .filter(_playerRole -> ((SPlayerRole) _playerRole).getDefaultPermissions().hasPermission(islandPrivilege))
                .min(Comparator.comparingInt(PlayerRole::getWeight)).orElse(SPlayerRole.lastRole());
    }

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return Collections.unmodifiableMap(playerPermissions);
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return Collections.unmodifiableMap(rolePermissions);
    }

    /*
     *  General methods
     */

    @Override
    public boolean isSpawn() {
        return false;
    }

    @Override
    public String getName() {
        return plugin.getSettings().getIslandNames().isColorSupport() ? islandName : islandRawName;
    }

    @Override
    public void setName(String islandName) {
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        PluginDebugger.debug("Action: Set Name, Island: " + owner.getName() + ", Name: " + islandName);

        this.islandName = islandName;
        this.islandRawName = Formatters.STRIP_COLOR_FORMATTER.format(this.islandName);

        IslandsDatabaseBridge.saveName(this);
    }

    @Override
    public String getRawName() {
        return islandRawName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        Preconditions.checkNotNull(description, "description parameter cannot be null.");
        PluginDebugger.debug("Action: Set Description, Island: " + owner.getName() + ", Description: " + description);

        this.description = description;

        IslandsDatabaseBridge.saveDescription(this);
    }

    @Override
    public void disbandIsland() {
        getIslandMembers(true).forEach(superiorPlayer -> {
            if (superiorPlayer.equals(owner)) {
                owner.setIsland(null);
            } else {
                kickMember(superiorPlayer);
            }

            if (plugin.getSettings().isDisbandInventoryClear())
                plugin.getNMSPlayers().clearInventory(superiorPlayer.asOfflinePlayer());

            plugin.getMissions().getAllMissions().stream().filter(mission -> {
                MissionData missionData = plugin.getMissions().getMissionData(mission).orElse(null);
                return missionData != null && missionData.isDisbandReset();
            }).forEach(superiorPlayer::resetMission);
        });

        if (BuiltinModules.BANK.disbandRefund > 0)
            plugin.getProviders().depositMoney(getOwner(), islandBank.getBalance()
                    .multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund)));

        plugin.getMissions().getAllMissions().forEach(this::resetMission);

        resetChunks(true);

        plugin.getGrid().deleteIsland(this);
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (superiorPlayer.equals(owner))
            return false;

        SuperiorPlayer previousOwner = getOwner();

        if (!plugin.getEventsBus().callIslandTransferEvent(this, previousOwner, superiorPlayer))
            return false;

        PluginDebugger.debug("Action: Transfer Owner, Island: " + owner.getName() + ", New Owner: " + superiorPlayer.getName());

        //Kick member without saving to database
        members.write(members -> members.remove(superiorPlayer));

        superiorPlayer.setPlayerRole(SPlayerRole.lastRole());

        //Add member without saving to database
        members.write(members -> members.add(previousOwner));

        PlayerRole previousRole = SPlayerRole.lastRole().getPreviousRole();
        previousOwner.setPlayerRole(previousRole == null ? SPlayerRole.lastRole() : previousRole);

        //Changing owner of the island.
        owner = superiorPlayer;

        IslandsDatabaseBridge.saveIslandLeader(this);
        IslandsDatabaseBridge.addMember(this, previousOwner, getCreationTime());

        plugin.getGrid().transferIsland(previousOwner.getUniqueId(), owner.getUniqueId());

        plugin.getMissions().getAllMissions().forEach(mission -> mission.transferData(previousOwner, owner));

        return true;
    }

    @Override
    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        Preconditions.checkNotNull(originalPlayer, "originalPlayer parameter cannot be null.");
        Preconditions.checkNotNull(newPlayer, "newPlayer parameter cannot be null.");

        if (owner == originalPlayer) {
            owner = newPlayer;
            IslandsDatabaseBridge.saveIslandLeader(this);
            plugin.getGrid().transferIsland(originalPlayer.getUniqueId(), owner.getUniqueId());
        } else if (isMember(originalPlayer)) {
            members.write(members -> {
                members.remove(originalPlayer);
                members.add(newPlayer);
            });
            IslandsDatabaseBridge.removeMember(this, originalPlayer);
            IslandsDatabaseBridge.addMember(this, newPlayer, System.currentTimeMillis());
        }

        replaceVisitor(originalPlayer, newPlayer);
        replaceBannedPlayer(originalPlayer, newPlayer);
        replacePermissions(originalPlayer, newPlayer);
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer asker) {
        calcIslandWorth(asker, null);
    }

    @Override
    public void calcIslandWorth(@Nullable SuperiorPlayer asker, @Nullable Runnable callback) {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(() -> calcIslandWorth(asker, callback));
            return;
        }

        long lastUpdateTime = getLastTimeUpdate();

        if (lastUpdateTime != -1 && (System.currentTimeMillis() / 1000) - lastUpdateTime >= 600) {
            finishCalcIsland(asker, callback, getIslandLevel(), getWorth());
            return;
        }

        beingRecalculated = true;

        PluginDebugger.debug("Action: Calculate Island, Island: " + owner.getName() + ", Target: " + (asker == null ? "Null" : asker.getName()));

        BigDecimal oldWorth = getWorth();
        BigDecimal oldLevel = getIslandLevel();

        CompletableFuture<IslandCalculationAlgorithm.IslandCalculationResult> calculationResult;

        try {
            // Legacy support
            // noinspection deprecation
            calculationResult = calculationAlgorithm.calculateIsland();
        } catch (UnsupportedOperationException ex) {
            calculationResult = calculationAlgorithm.calculateIsland(this);
        }

        calculationResult.whenComplete((result, error) -> {
            if (error != null) {
                if (error instanceof TimeoutException) {
                    if (asker != null)
                        Message.ISLAND_WORTH_TIME_OUT.send(asker);
                } else {
                    SuperiorSkyblockPlugin.log("&cError occurred when calculating the island:");
                    error.printStackTrace();

                    if (asker != null)
                        Message.ISLAND_WORTH_ERROR.send(asker);
                }

                beingRecalculated = false;

                return;
            }

            clearBlockCounts();
            result.getBlockCounts().forEach((blockKey, amount) ->
                    handleBlockPlace(blockKey, amount, false, false));

            BigDecimal newIslandLevel = getIslandLevel();
            BigDecimal newIslandWorth = getWorth();

            finishCalcIsland(asker, callback, newIslandLevel, newIslandWorth);

            plugin.getMenus().refreshValues(this);
            plugin.getMenus().refreshCounts(this);

            saveBlockCounts(oldWorth, oldLevel);
            updateLastTime();

            beingRecalculated = false;
        });
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return this.calculationAlgorithm;
    }

    @Override
    public void updateBorder() {
        PluginDebugger.debug("Action: Update Border, Island: " + owner.getName());
        getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.updateWorldBorder(this));
    }

    @Override
    public void updateIslandFly(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        IslandUtils.updateIslandFly(this, superiorPlayer);
    }

    @Override
    public int getIslandSize() {
        if (plugin.getSettings().isBuildOutsideIsland())
            return (int) Math.round(plugin.getSettings().getMaxIslandSize() * 1.5);

        return this.islandSize.get();
    }

    @Override
    public void setIslandSize(int islandSize) {
        islandSize = Math.max(1, islandSize);

        PluginDebugger.debug("Action: Set Size, Island: " + owner.getName() + ", Size: " + islandSize);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);

        if (cropGrowthEnabled) {
            // First, we want to remove all the current crop tile entities
            getLoadedChunks(true, false).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, true));
        }

        this.islandSize = new UpgradeValue<>(islandSize, false);

        if (cropGrowthEnabled) {
            // Now, we want to update the tile entities again
            getLoadedChunks(true, false).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, false));
        }

        IslandsDatabaseBridge.saveSize(this);
    }

    @Override
    public int getIslandSizeRaw() {
        return this.islandSize.isSynced() ? -1 : this.islandSize.get();
    }

    @Override
    public String getDiscord() {
        return discord;
    }

    @Override
    public void setDiscord(String discord) {
        Preconditions.checkNotNull(discord, "discord parameter cannot be null.");
        PluginDebugger.debug("Action: Set Discord, Island: " + owner.getName() + ", Discord: " + discord);

        this.discord = discord;
        IslandsDatabaseBridge.saveDiscord(this);
    }

    @Override
    public String getPaypal() {
        return paypal;
    }

    @Override
    public void setPaypal(String paypal) {
        Preconditions.checkNotNull(paypal, "paypal parameter cannot be null.");
        PluginDebugger.debug("Action: Set Paypal, Island: " + owner.getName() + ", Paypal: " + paypal);

        this.paypal = paypal;
        IslandsDatabaseBridge.savePaypal(this);
    }

    @Override
    public Biome getBiome() {
        if (biome == null) {
            // Loads the chunk and gets the biome on the first block.
            // This may be called if a placeholder is called before the island is loaded, for example.
            biome = getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlock().getBiome();
        }
        return biome;
    }

    @Override
    public void setBiome(Biome biome) {
        setBiome(biome, true);
    }

    @Override
    public void setBiome(Biome biome, boolean updateBlocks) {
        Preconditions.checkNotNull(biome, "biome parameter cannot be null.");

        this.biome = biome;

        if (!updateBlocks)
            return;

        PluginDebugger.debug("Action: Set Biome, Island: " + owner.getName() + ", Biome: " + biome.name());

        List<Player> playersToUpdate = getAllPlayersInside().stream().map(SuperiorPlayer::asPlayer).collect(Collectors.toList());

        {
            World normalWorld = getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getWorld();
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, normalWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, biome, playersToUpdate);
        }

        if (plugin.getProviders().getWorldsProvider().isNetherEnabled() && wasSchematicGenerated(World.Environment.NETHER)) {
            World netherWorld = getCenter(World.Environment.NETHER).getWorld();
            Biome netherBiome;

            try {
                netherBiome = Biome.valueOf(plugin.getSettings().getWorlds().getNether().getBiome());
            } catch (IllegalArgumentException error) {
                netherBiome = ServerVersion.isLegacy() ? Biome.HELL :
                        ServerVersion.isAtLeast(ServerVersion.v1_16) ? Biome.valueOf("NETHER_WASTES") : Biome.valueOf("NETHER");
            }

            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, netherWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, netherBiome, playersToUpdate);
        }

        if (plugin.getProviders().getWorldsProvider().isEndEnabled() && wasSchematicGenerated(World.Environment.THE_END)) {
            World endWorld = getCenter(World.Environment.THE_END).getWorld();
            Biome endBiome;

            try {
                endBiome = Biome.valueOf(plugin.getSettings().getWorlds().getEnd().getBiome());
            } catch (IllegalArgumentException error) {
                endBiome = ServerVersion.isLegacy() ? Biome.SKY : Biome.valueOf("THE_END");
            }

            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, endWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, endBiome, playersToUpdate);
        }

        for (World registeredWorld : plugin.getGrid().getRegisteredWorlds()) {
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, registeredWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, biome, playersToUpdate);
        }
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void setLocked(boolean locked) {
        PluginDebugger.debug("Action: Set Locked, Island: " + owner.getName() + ", Locked: " + locked);
        this.isLocked = locked;

        if (this.isLocked) {
            for (SuperiorPlayer victimPlayer : getAllPlayersInside()) {
                if (!hasPermission(victimPlayer, IslandPrivileges.CLOSE_BYPASS)) {
                    victimPlayer.teleport(plugin.getGrid().getSpawnIsland());
                    Message.ISLAND_WAS_CLOSED.send(victimPlayer);
                }
            }
        }

        IslandsDatabaseBridge.saveLockedStatus(this);
    }

    @Override
    public boolean isIgnored() {
        return isTopIslandsIgnored;
    }

    @Override
    public void setIgnored(boolean ignored) {
        PluginDebugger.debug("Action: Set Ignored, Island: " + owner.getName() + ", Ignored: " + ignored);
        this.isTopIslandsIgnored = ignored;
        plugin.getGrid().setForceSort(true); // We want top islands to get sorted again even if only 1 island exists
        IslandsDatabaseBridge.saveIgnoredStatus(this);
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        Preconditions.checkNotNull(message, "message parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        PluginDebugger.debug("Action: Send Message, Island: " + owner.getName() + ", Ignored Members: " + ignoredList + ", Message: " + message);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> Message.CUSTOM.send(superiorPlayer, message, false));
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration,
                          int fadeOut, UUID... ignoredMembers) {
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        PluginDebugger.debug("Action: Send Title, Island: " + owner.getName() + ", Ignored Members: " + ignoredList + ", Title: " + title + ", Subtitle: " + subtitle);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> plugin.getNMSPlayers().sendTitle(superiorPlayer.asPlayer(),
                        title, subtitle, fadeIn, duration, fadeOut));
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        Preconditions.checkNotNull(command, "command parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        PluginDebugger.debug("Action: Execute Command, Island: " + owner.getName() + ", Ignored Members: " + ignoredList + ", Command: " + command);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && (!onlyOnlineMembers || superiorPlayer.isOnline()))
                .forEach(superiorPlayer -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player-name}", superiorPlayer.getName())));
    }

    @Override
    public boolean isBeingRecalculated() {
        return beingRecalculated;
    }

    @Override
    public void updateLastTime() {
        if (this.lastTimeUpdate != -1)
            setLastTimeUpdate(System.currentTimeMillis() / 1000);
    }

    @Override
    public void setCurrentlyActive() {
        this.lastTimeUpdate = -1L;
    }

    @Override
    public long getLastTimeUpdate() {
        return lastTimeUpdate;
    }

    public void setLastTimeUpdate(long lastTimeUpdate) {
        PluginDebugger.debug("Action: Update Last Time, Island: " + owner.getName() + ", Last Time: " + lastTimeUpdate);
        this.lastTimeUpdate = lastTimeUpdate;
        if (lastTimeUpdate != -1)
            IslandsDatabaseBridge.saveLastTimeUpdate(this);
    }

    @Override
    public IslandBank getIslandBank() {
        return islandBank;
    }

    @Override
    public BigDecimal getBankLimit() {
        return bankLimit.get();
    }

    @Override
    public void setBankLimit(BigDecimal bankLimit) {
        Preconditions.checkNotNull(bankLimit, "bankLimit parameter cannot be null.");
        PluginDebugger.debug("Action: Set Bank Limit, Island: " + owner.getName() + ", Bank Limit: " + bankLimit);

        this.bankLimit = new UpgradeValue<>(bankLimit, i -> i.compareTo(new BigDecimal(-1)) < 0);

        // Trying to give interest again if the last one failed.
        if (hasGiveInterestFailed())
            giveInterest(false);

        IslandsDatabaseBridge.saveBankLimit(this);
    }

    /*
     *  Bank related methods
     */

    @Override
    public BigDecimal getBankLimitRaw() {
        return this.bankLimit.isSynced() ? new BigDecimal(-2) : this.bankLimit.get();
    }

    @Override
    public boolean giveInterest(boolean checkOnlineOwner) {
        long currentTime = System.currentTimeMillis() / 1000;

        if (checkOnlineOwner && BuiltinModules.BANK.bankInterestRecentActive > 0 &&
                currentTime - owner.getLastTimeStatus() > BuiltinModules.BANK.bankInterestRecentActive)
            return false;

        BigDecimal balance = islandBank.getBalance().max(BigDecimal.ONE);
        BigDecimal balanceToGive = balance.multiply(new BigDecimal(BuiltinModules.BANK.bankInterestPercentage / 100D));

        // If the money that will be given exceeds limit, we want to give money later.
        if (!islandBank.canDepositMoney(balanceToGive)) {
            giveInterestFailed = true;
            return false;
        }

        giveInterestFailed = false;

        PluginDebugger.debug("Action: Give Bank Interest, Island: " + owner.getName());

        islandBank.depositAdminMoney(Bukkit.getConsoleSender(), balanceToGive);
        plugin.getMenus().refreshIslandBank(this);

        setLastInterestTime(currentTime);

        return true;
    }


    private boolean hasGiveInterestFailed() {
        return this.giveInterestFailed;
    }

    @Override
    public long getLastInterestTime() {
        return lastInterest;
    }

    @Override
    public void setLastInterestTime(long lastInterest) {
        if (BuiltinModules.BANK.bankInterestEnabled) {
            long ticksToNextInterest = BuiltinModules.BANK.bankInterestInterval * 20L;
            this.bankInterestTask.set(bankInterestTask -> {
                if (bankInterestTask != null)
                    bankInterestTask.cancel();
                return Executor.sync(() -> giveInterest(true), ticksToNextInterest);
            });
        }

        this.lastInterest = lastInterest;
        IslandsDatabaseBridge.saveLastInterestTime(this);
    }

    @Override
    public long getNextInterest() {
        long currentTime = System.currentTimeMillis() / 1000;
        return BuiltinModules.BANK.bankInterestInterval - (currentTime - lastInterest);
    }

    @Override
    public void handleBlockPlace(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockPlace(KeyImpl.of(block), 1);
    }

    @Override
    public void handleBlockPlace(Block block, int amount) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockPlace(KeyImpl.of(block), amount, true);
    }

    @Override
    public void handleBlockPlace(Block block, int amount, boolean save) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockPlace(KeyImpl.of(block), amount, save);
    }

    /*
     *  Worth related methods
     */

    @Override
    public void handleBlockPlace(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockPlace(key, amount, true);
    }

    @Override
    public void handleBlockPlace(Key key, int amount, boolean save) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockPlace(key, BigInteger.valueOf(amount), save);
    }

    @Override
    public void handleBlockPlace(Key key, BigInteger amount, boolean save) {
        handleBlockPlace(key, amount, save, true);
    }

    @Override
    public void handleBlockPlace(Key key, BigInteger amount, boolean save, boolean updateLastTimeStatus) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        boolean trackedBlock = this.blocksTracker.trackBlock(key, amount);

        if (!trackedBlock)
            return;

        BigDecimal oldWorth = getWorth();
        BigDecimal oldLevel = getIslandLevel();

        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        if (blockValue.compareTo(BigDecimal.ZERO) != 0) {
            islandWorth.updateAndGet(islandWorth -> islandWorth.add(blockValue.multiply(new BigDecimal(amount))));
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            islandLevel.updateAndGet(islandLevel -> islandLevel.add(blockLevel.multiply(new BigDecimal(amount))));
        }

        if (updateLastTimeStatus)
            updateLastTime();

        if (save)
            saveBlockCounts(oldWorth, oldLevel);
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks) {
        Preconditions.checkNotNull(blocks, "blocks parameter cannot be null.");
        blocks.forEach((blockKey, amount) ->
                handleBlockPlace(blockKey, BigInteger.valueOf(amount), false, false));
        IslandsDatabaseBridge.saveBlockCounts(this);
        IslandsDatabaseBridge.saveDirtyChunks(this);
        updateLastTime();
    }

    @Override
    public void handleBlockBreak(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockBreak(KeyImpl.of(block), 1);
    }

    @Override
    public void handleBlockBreak(Block block, int amount) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockBreak(block, amount, true);
    }

    @Override
    public void handleBlockBreak(Block block, int amount, boolean save) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockBreak(KeyImpl.of(block), amount, save);
    }

    @Override
    public void handleBlockBreak(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockBreak(key, amount, true);
    }

    @Override
    public void handleBlockBreak(Key key, int amount, boolean save) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        handleBlockBreak(key, BigInteger.valueOf(amount), save);
    }

    @Override
    public void handleBlockBreak(Key key, BigInteger amount, boolean save) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        boolean untrackedBlocks = this.blocksTracker.untrackBlock(key, amount);

        if (!untrackedBlocks)
            return;

        BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();

        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        if (blockValue.compareTo(BigDecimal.ZERO) != 0) {
            this.islandWorth.updateAndGet(islandWorth -> islandWorth.subtract(blockValue.multiply(new BigDecimal(amount))));
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            this.islandLevel.updateAndGet(islandLevel -> islandLevel.subtract(blockLevel.multiply(new BigDecimal(amount))));
        }

        boolean hasBlockLimit = blockLimits.containsKey(key),
                valuesMenu = plugin.getBlockValues().isValuesMenu(key);

        updateLastTime();

        if (save)
            saveBlockCounts(oldWorth, oldLevel);
    }

    @Override
    public BigInteger getBlockCountAsBigInteger(Key key) {
        return this.blocksTracker.getBlockCount(key);
    }

    @Override
    public Map<Key, BigInteger> getBlockCountsAsBigInteger() {
        return this.blocksTracker.getBlockCounts();
    }

    @Override
    public BigInteger getExactBlockCountAsBigInteger(Key key) {
        return this.blocksTracker.getExactBlockCount(key);
    }

    @Override
    public void clearBlockCounts() {
        blocksTracker.clearBlockCounts();
        islandWorth.set(BigDecimal.ZERO);
        islandLevel.set(BigDecimal.ZERO);
    }

    @Override
    public IslandBlocksTrackerAlgorithm getBlocksTracker() {
        return this.blocksTracker;
    }

    @Override
    public BigDecimal getWorth() {
        double bankWorthRate = BuiltinModules.BANK.bankWorthRate;

        BigDecimal islandWorth = this.islandWorth.get();
        BigDecimal islandBank = this.islandBank.getBalance();
        BigDecimal bonusWorth = this.bonusWorth.get();
        BigDecimal finalIslandWorth = (bankWorthRate <= 0 ? getRawWorth() : islandWorth.add(
                islandBank.multiply(BigDecimal.valueOf(bankWorthRate)))).add(bonusWorth);

        if (!plugin.getSettings().isNegativeWorth() && finalIslandWorth.compareTo(BigDecimal.ZERO) < 0)
            return BigDecimal.ZERO;

        return finalIslandWorth;
    }

    @Override
    public BigDecimal getRawWorth() {
        return islandWorth.get();
    }

    @Override
    public BigDecimal getBonusWorth() {
        return bonusWorth.get();
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth) {
        Preconditions.checkNotNull(bonusWorth, "bonusWorth parameter cannot be null.");
        PluginDebugger.debug("Action: Set Bonus Worth, Island: " + owner.getName() + ", Bonus: " + bonusWorth);

        this.bonusWorth.set(bonusWorth);

        plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
        plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);

        IslandsDatabaseBridge.saveBonusWorth(this);
    }

    @Override
    public BigDecimal getBonusLevel() {
        return bonusLevel.get();
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        Preconditions.checkNotNull(bonusLevel, "bonusLevel parameter cannot be null.");
        PluginDebugger.debug("Action: Set Bonus Level, Island: " + owner.getName() + ", Bonus: " + bonusLevel);

        this.bonusLevel.set(bonusLevel);

        plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
        plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);

        IslandsDatabaseBridge.saveBonusLevel(this);
    }

    @Override
    public BigDecimal getIslandLevel() {
        BigDecimal bonusLevel = this.bonusLevel.get();
        BigDecimal islandLevel = this.islandLevel.get().add(bonusLevel);

        if (plugin.getSettings().isRoundedIslandLevels()) {
            islandLevel = islandLevel.setScale(0, RoundingMode.HALF_UP);
        }

        if (!plugin.getSettings().isNegativeLevel() && islandLevel.compareTo(BigDecimal.ZERO) < 0)
            islandLevel = BigDecimal.ZERO;

        return islandLevel;
    }

    @Override
    public BigDecimal getRawLevel() {
        BigDecimal islandLevel = this.islandLevel.get();

        if (plugin.getSettings().isRoundedIslandLevels()) {
            islandLevel = islandLevel.setScale(0, RoundingMode.HALF_UP);
        }

        if (!plugin.getSettings().isNegativeLevel() && islandLevel.compareTo(BigDecimal.ZERO) < 0)
            islandLevel = BigDecimal.ZERO;

        return islandLevel;
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        Preconditions.checkNotNull(upgrade, "upgrade parameter cannot be null.");
        return upgrade.getUpgradeLevel(getUpgrades().getOrDefault(upgrade.getName(), 1));
    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {
        Preconditions.checkNotNull(upgrade, "upgrade parameter cannot be null.");
        PluginDebugger.debug("Action: Set Upgrade, Island: " + owner.getName() + ", Upgrade: " + upgrade.getName() + ", Level: " + level);

        int currentLevel = getUpgradeLevel(upgrade).getLevel();

        upgrades.put(upgrade.getName(), Math.min(upgrade.getMaxUpgradeLevel(), level));

        lastUpgradeTime = System.currentTimeMillis();

        IslandsDatabaseBridge.saveUpgrade(this, upgrade, level);

        UpgradeLevel upgradeLevel = getUpgradeLevel(upgrade);

        // Level was downgraded, we need to clear the values of that level and sync all upgrades again
        if (currentLevel > level) {
            clearUpgrades(false);
            syncUpgrades(false);
        } else {
            syncUpgrade((SUpgradeLevel) upgradeLevel, false);
        }

        if (upgradeLevel.getBorderSize() != -1)
            updateBorder();

        plugin.getMenus().refreshUpgrades(this);
    }

    @Override
    public Map<String, Integer> getUpgrades() {
        return Collections.unmodifiableMap(upgrades);
    }

    @Override
    public void syncUpgrades() {
        syncUpgrades(true);
    }

    /*
     *  Upgrade related methods
     */

    @Override
    public void updateUpgrades() {
        clearUpgrades(false);
        // We want to sync the default upgrade first, then the actual upgrades
        syncUpgrade(DefaultUpgradeLevel.getInstance(), false);
        // Syncing all real upgrades
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> syncUpgrade((SUpgradeLevel) getUpgradeLevel(upgrade), false));
    }

    @Override
    public long getLastTimeUpgrade() {
        return lastUpgradeTime;
    }

    @Override
    public boolean hasActiveUpgradeCooldown() {
        long lastTimeUpgrade = getLastTimeUpgrade();
        long currentTime = System.currentTimeMillis();
        long upgradeCooldown = plugin.getSettings().getUpgradeCooldown();
        return upgradeCooldown > 0 && lastTimeUpgrade > 0 && currentTime - lastTimeUpgrade <= upgradeCooldown;
    }

    @Override
    public double getCropGrowthMultiplier() {
        return cropGrowth.get();
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        cropGrowth = Math.max(1, cropGrowth);
        PluginDebugger.debug("Action: Set Crop Growth, Island: " + owner.getName() + ", Crop Growth: " + cropGrowth);
        this.cropGrowth = new UpgradeValue<>(cropGrowth, false);
        IslandsDatabaseBridge.saveCropGrowth(this);
    }

    @Override
    public double getCropGrowthRaw() {
        return this.cropGrowth.isSynced() ? -1 : this.cropGrowth.get();
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return this.spawnerRates.get();
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        spawnerRates = Math.max(1, spawnerRates);
        PluginDebugger.debug("Action: Set Spawner Rates, Island: " + owner.getName() + ", Spawner Rates: " + spawnerRates);
        this.spawnerRates = new UpgradeValue<>(spawnerRates, false);
        IslandsDatabaseBridge.saveSpawnerRates(this);
    }

    @Override
    public double getSpawnerRatesRaw() {
        return this.spawnerRates.isSynced() ? -1 : this.spawnerRates.get();
    }

    @Override
    public double getMobDropsMultiplier() {
        return this.mobDrops.get();
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        mobDrops = Math.max(1, mobDrops);
        PluginDebugger.debug("Action: Set Mob Drops, Island: " + owner.getName() + ", Mob Drops: " + mobDrops);
        this.mobDrops = new UpgradeValue<>(mobDrops, false);
        IslandsDatabaseBridge.saveMobDrops(this);
    }

    @Override
    public double getMobDropsRaw() {
        return this.mobDrops.isSynced() ? -1 : this.mobDrops.get();
    }

    @Override
    public int getBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getOrDefault(key, IslandUtils.NO_LIMIT).get();
    }

    @Override
    public int getExactBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getRaw(key, IslandUtils.NO_LIMIT).get();
    }

    @Override
    public Key getBlockLimitKey(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getKey(key, key);
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return Collections.unmodifiableMap(this.blockLimits.entrySet().stream().collect(
                KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())
        ));
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return Collections.unmodifiableMap(this.blockLimits.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearBlockLimits() {
        PluginDebugger.debug("Action: Clear Block Limits, Island: " + owner.getName());
        blockLimits.clear();
        IslandsDatabaseBridge.clearBlockLimits(this);
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        int finalLimit = Math.max(0, limit);
        PluginDebugger.debug("Action: Set Block Limit, Island: " + owner.getName() + ", Block: " + key + ", Limit: " + finalLimit);
        blockLimits.put(key, new UpgradeValue<>(finalLimit, false));
        plugin.getBlockValues().addCustomBlockKey(key);
        IslandsDatabaseBridge.saveBlockLimit(this, key, limit);
    }

    @Override
    public void removeBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        PluginDebugger.debug("Action: Remove Block Limit, Island: " + owner.getName() + ", Block: " + key);
        blockLimits.remove(key);
        IslandsDatabaseBridge.removeBlockLimit(this, key);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return hasReachedBlockLimit(key, 1);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        int blockLimit = getExactBlockLimit(key);

        //Checking for the specific provided key.
        if (blockLimit > IslandUtils.NO_LIMIT.get())
            return getBlockCountAsBigInteger(key).add(BigInteger.valueOf(amount))
                    .compareTo(BigInteger.valueOf(blockLimit)) > 0;

        //Getting the global key values.
        key = KeyImpl.of(key.getGlobalKey(), "");
        blockLimit = getBlockLimit(key);

        return blockLimit > IslandUtils.NO_LIMIT.get() && getBlockCountAsBigInteger(key)
                .add(BigInteger.valueOf(amount)).compareTo(BigInteger.valueOf(blockLimit)) > 0;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return getEntityLimit(KeyImpl.of(entityType));
    }

    @Override
    public int getEntityLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return this.entityLimits.getOrDefault(key, IslandUtils.NO_LIMIT).get();
    }

    @Override
    public Map<Key, Integer> getEntitiesLimitsAsKeys() {
        return Collections.unmodifiableMap(this.entityLimits.entrySet().stream().collect(
                KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())
        ));
    }

    @Override
    public Map<Key, Integer> getCustomEntitiesLimits() {
        return Collections.unmodifiableMap(this.entityLimits.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearEntitiesLimits() {
        PluginDebugger.debug("Action: Clear Entity Limit, Island: " + owner.getName());
        entityLimits.clear();
        IslandsDatabaseBridge.clearEntityLimits(this);
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        setEntityLimit(KeyImpl.of(entityType), limit);
    }

    @Override
    public void setEntityLimit(Key key, int limit) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        int finalLimit = Math.max(0, limit);
        PluginDebugger.debug("Action: Set Entity Limit, Island: " + owner.getName() + ", Entity: " + key + ", Limit: " + finalLimit);
        entityLimits.put(key, new UpgradeValue<>(finalLimit, false));
        IslandsDatabaseBridge.saveEntityLimit(this, key, limit);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return hasReachedEntityLimit(KeyImpl.of(entityType));
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return hasReachedEntityLimit(key, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return hasReachedEntityLimit(KeyImpl.of(entityType), amount);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        CompletableFutureList<Chunk> chunks = new CompletableFutureList<>();
        int entityLimit = getEntityLimit(key);

        if (entityLimit <= IslandUtils.NO_LIMIT.get())
            return CompletableFuture.completedFuture(false);

        return CompletableFuture.completedFuture(this.entitiesTracker.getEntityCount(key) + amount > entityLimit);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm getEntitiesTracker() {
        return this.entitiesTracker;
    }

    @Override
    public int getTeamLimit() {
        return this.teamLimit.get();
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        teamLimit = Math.max(0, teamLimit);
        PluginDebugger.debug("Action: Set Team Limit, Island: " + owner.getName() + ", Team Limit: " + teamLimit);
        this.teamLimit = new UpgradeValue<>(teamLimit, false);
        IslandsDatabaseBridge.saveTeamLimit(this);
    }

    @Override
    public int getTeamLimitRaw() {
        return this.teamLimit.isSynced() ? -1 : this.teamLimit.get();
    }

    @Override
    public int getWarpsLimit() {
        return this.warpsLimit.get();
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        warpsLimit = Math.max(0, warpsLimit);
        PluginDebugger.debug("Action: Set Warps Limit, Island: " + owner.getName() + ", Warps Limit: " + warpsLimit);
        this.warpsLimit = new UpgradeValue<>(warpsLimit, false);
        IslandsDatabaseBridge.saveWarpsLimit(this);
    }

    @Override
    public int getWarpsLimitRaw() {
        return this.warpsLimit.isSynced() ? -1 : this.warpsLimit.get();
    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {
        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");
        PluginDebugger.debug("Action: Set Island Effect, Island: " + owner.getName() + ", Effect: " + type.getName() + ", Level: " + level);

        if (level <= 0) {
            islandEffects.remove(type);
            Executor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> {
                Player player = superiorPlayer.asPlayer();
                if (player != null)
                    player.removePotionEffect(type);
            }));
            IslandsDatabaseBridge.removeIslandEffect(this, type);
        } else {
            PotionEffect potionEffect = new PotionEffect(type, Integer.MAX_VALUE, level - 1);
            UpgradeValue<Integer> oldPotionLevel = islandEffects.put(type, new UpgradeValue<>(level, false));
            Executor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> {
                Player player = superiorPlayer.asPlayer();
                assert player != null;
                if (oldPotionLevel != null && oldPotionLevel.get() > level)
                    player.removePotionEffect(type);
                player.addPotionEffect(potionEffect, true);
            }));
            IslandsDatabaseBridge.saveIslandEffect(this, type, level);
        }
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");
        return islandEffects.getOrDefault(type, UpgradeValue.NEGATIVE).get();
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        Map<PotionEffectType, Integer> islandEffects = new HashMap<>();

        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            if (potionEffectType != null) {
                int level = getPotionEffectLevel(potionEffectType);
                if (level > 0)
                    islandEffects.put(potionEffectType, level);
            }
        }

        return islandEffects;
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class))
            return;

        Player player = superiorPlayer.asPlayer();
        if (player != null) {
            getPotionEffects().forEach((potionEffectType, level) -> player.addPotionEffect(
                    new PotionEffect(potionEffectType, Integer.MAX_VALUE, level - 1), true));
        }
    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class))
            removeEffectsNoUpgradeCheck(superiorPlayer);
    }

    @Override
    public void removeEffects() {
        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class))
            getAllPlayersInside().forEach(this::removeEffectsNoUpgradeCheck);
    }

    private void removeEffectsNoUpgradeCheck(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player != null)
            getPotionEffects().keySet().forEach(player::removePotionEffect);
    }

    @Override
    public void clearEffects() {
        PluginDebugger.debug("Action: Clear Island Effects, Island: " + owner.getName());
        islandEffects.clear();
        removeEffects();
        IslandsDatabaseBridge.clearIslandEffects(this);
    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        PluginDebugger.debug("Action: Set Role Limit, Island: " + owner.getName() + ", Role: " + playerRole.getName() + ", Limit: " + limit);

        if (limit < 0) {
            roleLimits.remove(playerRole);
            IslandsDatabaseBridge.removeRoleLimit(this, playerRole);
        } else {
            roleLimits.put(playerRole, new UpgradeValue<>(limit, false));
            IslandsDatabaseBridge.saveRoleLimit(this, playerRole, limit);
        }
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        return roleLimits.getOrDefault(playerRole, UpgradeValue.NEGATIVE).get();
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        UpgradeValue<Integer> upgradeValue = roleLimits.getOrDefault(playerRole, UpgradeValue.NEGATIVE);
        return upgradeValue.isSynced() ? -1 : upgradeValue.get();
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return roleLimits.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        return this.roleLimits.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    @Override
    public WarpCategory createWarpCategory(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        PluginDebugger.debug("Action: Create Warp Category, Island: " + owner.getName() + ", Name: " + name);

        WarpCategory warpCategory = warpCategories.get(name.toLowerCase(Locale.ENGLISH));

        if (warpCategory == null) {
            List<Integer> occupiedSlots = warpCategories.values().stream().map(WarpCategory::getSlot).collect(Collectors.toList());

            warpCategories.put(name.toLowerCase(Locale.ENGLISH), (warpCategory = new SWarpCategory(this, name)));

            int slot = 0;
            while (occupiedSlots.contains(slot))
                slot++;

            warpCategory.setSlot(slot);

            IslandsDatabaseBridge.saveWarpCategory(this, warpCategory);

            plugin.getMenus().refreshWarpCategories(this);
        }

        return warpCategory;
    }

    @Override
    public WarpCategory getWarpCategory(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return warpCategories.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public WarpCategory getWarpCategory(int slot) {
        return warpCategories.values().stream().filter(warpCategory -> warpCategory.getSlot() == slot)
                .findAny().orElse(null);
    }

    @Override
    public void renameCategory(WarpCategory warpCategory, String newName) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        Preconditions.checkNotNull(newName, "newName parameter cannot be null.");

        warpCategories.remove(warpCategory.getName().toLowerCase(Locale.ENGLISH));
        warpCategories.put(newName.toLowerCase(Locale.ENGLISH), warpCategory);
        warpCategory.setName(newName);
    }

    @Override
    public void deleteCategory(WarpCategory warpCategory) {
        Preconditions.checkNotNull(warpCategory, "warpCategory parameter cannot be null.");
        PluginDebugger.debug("Action: Delete Warp-Category, Island: " + owner.getName() + ", Category: " + warpCategory.getName());

        boolean validWarpRemoval = warpCategories.remove(warpCategory.getName().toLowerCase(Locale.ENGLISH)) != null;
        if (validWarpRemoval) {
            IslandsDatabaseBridge.removeWarpCategory(this, warpCategory);
            boolean shouldSaveWarps = !warpCategory.getWarps().isEmpty();
            if (shouldSaveWarps) {
                new ArrayList<>(warpCategory.getWarps()).forEach(islandWarp -> deleteWarp(islandWarp.getName()));
                plugin.getMenus().destroyWarps(warpCategory);
            }

            plugin.getMenus().destroyWarpCategories(this);
        }
    }

    /*
     *  Warps related methods
     */

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return Collections.unmodifiableMap(warpCategories);
    }

    @Override
    public IslandWarp createWarp(String name, Location location, @Nullable WarpCategory warpCategory) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");

        PluginDebugger.debug("Action: Create Warp, Island: " + owner.getName() + ", Name: " + name + ", Location: " +
                Formatters.LOCATION_FORMATTER.format(location));

        if (warpCategory == null)
            warpCategory = warpCategories.values().stream().findFirst().orElseGet(() -> createWarpCategory("Default Category"));

        IslandWarp islandWarp = new SIslandWarp(name, location.clone(), warpCategory);
        loadIslandWarp(islandWarp);

        IslandsDatabaseBridge.saveWarp(this, islandWarp);

        plugin.getMenus().refreshGlobalWarps();
        plugin.getMenus().refreshWarps(warpCategory);

        return islandWarp;
    }

    @Override
    public void renameWarp(IslandWarp islandWarp, String newName) {
        Preconditions.checkNotNull(islandWarp, "islandWarp parameter cannot be null.");
        Preconditions.checkNotNull(newName, "newName parameter cannot be null.");

        warpsByName.remove(islandWarp.getName().toLowerCase(Locale.ENGLISH));
        warpsByName.put(newName.toLowerCase(Locale.ENGLISH), islandWarp);
        islandWarp.setName(newName);
    }

    @Override
    public IslandWarp getWarp(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Location blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return warpsByLocation.get(blockLocation);
    }

    @Override
    public IslandWarp getWarp(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return warpsByName.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(warp, "warp parameter cannot be null.");

        IslandWarp islandWarp = getWarp(warp);

        if (islandWarp == null) {
            Message.INVALID_WARP.send(superiorPlayer, warp);
            return;
        }

        if (plugin.getSettings().getWarpsWarmup() > 0 && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermission("superior.admin.bypass.warmup")) {
            Message.TELEPORT_WARMUP.send(superiorPlayer, Formatters.TIME_FORMATTER.format(
                    Duration.ofMillis(plugin.getSettings().getWarpsWarmup()), superiorPlayer.getUserLocale()));
            superiorPlayer.setTeleportTask(Executor.sync(() ->
                    warpPlayerWithoutWarmup(superiorPlayer, islandWarp), plugin.getSettings().getWarpsWarmup() / 50));
        } else {
            warpPlayerWithoutWarmup(superiorPlayer, islandWarp);
        }
    }

    @Override
    public void deleteWarp(@Nullable SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        Location blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IslandWarp islandWarp = warpsByLocation.remove(blockLocation);
        if (islandWarp != null) {
            deleteWarp(islandWarp.getName());
            if (superiorPlayer != null)
                Message.DELETE_WARP.send(superiorPlayer, islandWarp.getName());
        }
    }

    @Override
    public void deleteWarp(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        PluginDebugger.debug("Action: Delete Warp, Island: " + owner.getName() + ", Warp: " + name);

        IslandWarp islandWarp = warpsByName.remove(name.toLowerCase(Locale.ENGLISH));
        WarpCategory warpCategory = islandWarp == null ? null : islandWarp.getCategory();

        if (islandWarp != null) {
            warpsByLocation.remove(islandWarp.getLocation());
            warpCategory.getWarps().remove(islandWarp);

            IslandsDatabaseBridge.removeWarp(this, islandWarp);

            if (warpCategory.getWarps().isEmpty())
                deleteCategory(warpCategory);
        }

        plugin.getMenus().refreshGlobalWarps();

        if (warpCategory != null)
            plugin.getMenus().refreshWarps(warpCategory);
    }

    @Override
    public Map<String, IslandWarp> getIslandWarps() {
        return Collections.unmodifiableMap(warpsByName);
    }

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return ratings.getOrDefault(superiorPlayer.getUniqueId(), Rating.UNKNOWN);
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(rating, "rating parameter cannot be null.");

        PluginDebugger.debug("Action: Set Rating, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Rating: " + rating);

        if (rating == Rating.UNKNOWN) {
            ratings.remove(superiorPlayer.getUniqueId());
            IslandsDatabaseBridge.removeRating(this, superiorPlayer);
        } else {
            ratings.put(superiorPlayer.getUniqueId(), rating);
            IslandsDatabaseBridge.saveRating(this, superiorPlayer, rating, System.currentTimeMillis());
        }

        plugin.getMenus().refreshIslandRatings(this);
    }

    @Override
    public double getTotalRating() {
        double avg = 0;

        for (Rating rating : ratings.values())
            avg += rating.getValue();

        return avg == 0 ? 0 : avg / getRatingAmount();
    }

    @Override
    public int getRatingAmount() {
        return ratings.size();
    }

    @Override
    public Map<UUID, Rating> getRatings() {
        return Collections.unmodifiableMap(ratings);
    }

    @Override
    public void removeRatings() {
        PluginDebugger.debug("Action: Remove Ratings, Island: " + owner.getName());
        ratings.clear();

        IslandsDatabaseBridge.clearRatings(this);

        plugin.getMenus().refreshIslandRatings(this);
    }

    /*
     *  Ratings related methods
     */

    @Override
    public boolean hasSettingsEnabled(IslandFlag settings) {
        Preconditions.checkNotNull(settings, "settings parameter cannot be null.");
        return islandFlags.getOrDefault(settings, (byte) (plugin.getSettings().getDefaultSettings().contains(settings.getName()) ? 1 : 0)) == 1;
    }

    @Override
    public Map<IslandFlag, Byte> getAllSettings() {
        return Collections.unmodifiableMap(islandFlags);
    }

    @Override
    public void enableSettings(IslandFlag settings) {
        Preconditions.checkNotNull(settings, "settings parameter cannot be null.");
        PluginDebugger.debug("Action: Enable Settings, Island: " + owner.getName() + ", Settings: " + settings.getName());

        islandFlags.put(settings, (byte) 1);

        boolean disableTime = false;
        boolean disableWeather = false;

        //Updating times / weather if necessary
        switch (settings.getName()) {
            case "ALWAYS_DAY":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerTime(0, false);
                });
                disableTime = true;
                break;
            case "ALWAYS_MIDDLE_DAY":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerTime(6000, false);
                });
                disableTime = true;
                break;
            case "ALWAYS_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerTime(14000, false);
                });
                disableTime = true;
                break;
            case "ALWAYS_MIDDLE_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerTime(18000, false);
                });
                disableTime = true;
                break;
            case "ALWAYS_SHINY":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerWeather(WeatherType.CLEAR);
                });
                disableWeather = true;
                break;
            case "ALWAYS_RAIN":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.setPlayerWeather(WeatherType.DOWNFALL);
                });
                disableWeather = true;
                break;
            case "PVP":
                if (plugin.getSettings().isTeleportOnPvPEnable())
                    getIslandVisitors().forEach(superiorPlayer -> {
                        superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                        Message.ISLAND_GOT_PVP_ENABLED_WHILE_INSIDE.send(superiorPlayer);
                    });
                break;
        }

        if (disableTime) {
            //Disabling settings without saving to database.
            if (settings != IslandFlags.ALWAYS_DAY)
                islandFlags.remove(IslandFlags.ALWAYS_DAY);
            if (settings != IslandFlags.ALWAYS_MIDDLE_DAY)
                islandFlags.remove(IslandFlags.ALWAYS_MIDDLE_DAY);
            if (settings != IslandFlags.ALWAYS_NIGHT)
                islandFlags.remove(IslandFlags.ALWAYS_NIGHT);
            if (settings != IslandFlags.ALWAYS_MIDDLE_NIGHT)
                islandFlags.remove(IslandFlags.ALWAYS_MIDDLE_NIGHT);
        }

        if (disableWeather) {
            if (settings != IslandFlags.ALWAYS_RAIN)
                islandFlags.remove(IslandFlags.ALWAYS_RAIN);
            if (settings != IslandFlags.ALWAYS_SHINY)
                islandFlags.remove(IslandFlags.ALWAYS_SHINY);
        }

        IslandsDatabaseBridge.saveIslandFlag(this, settings, 1);

        plugin.getMenus().refreshSettings(this);
    }

    @Override
    public void disableSettings(IslandFlag settings) {
        Preconditions.checkNotNull(settings, "settings parameter cannot be null.");
        PluginDebugger.debug("Action: Disable Settings, Island: " + owner.getName() + ", Settings: " + settings.getName());

        islandFlags.put(settings, (byte) 0);

        switch (settings.getName()) {
            case "ALWAYS_DAY":
            case "ALWAYS_MIDDLE_DAY":
            case "ALWAYS_NIGHT":
            case "ALWAYS_MIDDLE_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.resetPlayerTime();
                });
                break;
            case "ALWAYS_RAIN":
            case "ALWAYS_SHINY":
                getAllPlayersInside().forEach(superiorPlayer -> {
                    Player player = superiorPlayer.asPlayer();
                    if (player != null)
                        player.resetPlayerWeather();
                });
                break;
        }

        IslandsDatabaseBridge.saveIslandFlag(this, settings, 0);

        plugin.getMenus().refreshSettings(this);
    }

    @Override
    public void setGeneratorPercentage(Key key, int percentage, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        PluginDebugger.debug("Action: Set Generator, Island: " + owner.getName() + ", Block: " + key + ", Percentage: " + percentage + ", World: " + environment);

        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, true);

        Preconditions.checkArgument(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100 - got " + percentage + ".");

        if (percentage == 0) {
            setGeneratorAmount(key, 0, environment);
        } else if (percentage == 100) {
            cobbleGeneratorValues.clear();
            setGeneratorAmount(key, 1, environment);
        } else {
            //Removing the key from the generator
            setGeneratorAmount(key, 0, environment);
            int totalAmount = getGeneratorTotalAmount(environment);
            double realPercentage = percentage / 100D;
            double amount = (realPercentage * totalAmount) / (1 - realPercentage);
            if (amount < 1) {
                cobbleGeneratorValues.entrySet().forEach(entry -> entry.setValue(
                        new UpgradeValue<>(entry.getValue().get() * 10, entry.getValue().isSynced())
                ));
                amount *= 10;
            }
            setGeneratorAmount(key, (int) Math.round(amount), environment);
        }
    }

    @Override
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        int totalAmount = getGeneratorTotalAmount(environment);
        return totalAmount == 0 ? 0 : (getGeneratorAmount(key, environment) * 100) / totalAmount;
    }

    /*
     *  Missions related methods
     */

    @Override
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        return getGeneratorAmounts(environment).keySet().stream().collect(Collectors.toMap(key -> key,
                key -> getGeneratorAmount(KeyImpl.of(key), environment)));
    }

    @Override
    public void setGeneratorAmount(Key key, int amount, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, true);
        int finalAmount = Math.max(0, amount);
        PluginDebugger.debug("Action: Set Generator, Island: " + owner.getName() + ", Block: " + key + ", Amount: " + finalAmount + ", World: " + environment);
        cobbleGeneratorValues.put(key, new UpgradeValue<>(finalAmount, false));

        IslandsDatabaseBridge.saveGeneratorRate(this, environment, key, amount);
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, false);
        return (cobbleGeneratorValues == null ? UpgradeValue.ZERO :
                cobbleGeneratorValues.getOrDefault(key, UpgradeValue.ZERO)).get();
    }

    @Override
    public int getGeneratorTotalAmount(World.Environment environment) {
        int totalAmount = 0;
        for (int amt : getGeneratorAmounts(environment).values())
            totalAmount += amt;
        return totalAmount;
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, false);

        if (cobbleGeneratorValues == null)
            return Collections.unmodifiableMap(new HashMap<>());

        return Collections.unmodifiableMap(cobbleGeneratorValues.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                entry -> entry.getValue().get())));
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, false);

        if (cobbleGeneratorValues == null)
            return Collections.unmodifiableMap(new HashMap<>());

        return Collections.unmodifiableMap(cobbleGeneratorValues.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(environment, false);
        PluginDebugger.debug("Action: Clear Generator, Island: " + owner.getName() + ", World: " + environment);
        if (cobbleGeneratorValues != null) {
            cobbleGeneratorValues.clear();
            IslandsDatabaseBridge.clearGeneratorRates(this, environment);
        }
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        return generateBlock(location, location.getWorld().getEnvironment(), optimizeCobblestone);
    }

    @Override
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        int totalGeneratorAmounts = getGeneratorTotalAmount(environment);

        if (totalGeneratorAmounts == 0)
            return null;

        Map<String, Integer> generatorAmounts = getGeneratorAmounts(environment);

        String newState = "COBBLESTONE";

        if (totalGeneratorAmounts == 1) {
            newState = generatorAmounts.keySet().iterator().next();
        } else {
            int generatedIndex = ThreadLocalRandom.current().nextInt(totalGeneratorAmounts);
            int currentIndex = 0;
            for (Map.Entry<String, Integer> entry : generatorAmounts.entrySet()) {
                currentIndex += entry.getValue();
                if (generatedIndex < currentIndex) {
                    newState = entry.getKey();
                    break;
                }
            }
        }

        Key generatedBlock = KeyImpl.of(newState);

        String[] typeSections = newState.split(":");

        if (optimizeCobblestone && typeSections[0].contains("COBBLESTONE"))
            /* Block is being counted in BlocksListener#onBlockFromToMonitor */
            return generatedBlock;

        // If the block is a custom block, and the event was cancelled - we need to call the handleBlockPlace manually.
        handleBlockPlace(generatedBlock, 1);

        Material generateBlockType = Material.valueOf(typeSections[0]);
        byte blockData = typeSections.length == 2 ? Byte.parseByte(typeSections[1]) : 0;
        int combinedId = plugin.getNMSAlgorithms().getCombinedId(generateBlockType, blockData);

        if (combinedId == -1) {
            SuperiorSkyblockPlugin.log("&cFailed to generate block for type " + generateBlockType + ":" + blockData);
            generateBlockType = Material.COBBLESTONE;
            blockData = 0;
            combinedId = plugin.getNMSAlgorithms().getCombinedId(generateBlockType, blockData);
        }

        PluginDebugger.debug("Action: Generate Block, Island: " + getOwner().getName() +
                ", Block: " + generateBlockType + ":" + blockData);

        plugin.getNMSWorld().setBlock(location, combinedId);

        plugin.getNMSWorld().playGeneratorSound(location);

        return generatedBlock;
    }

    /*
     *  Settings related methods
     */

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        int generateBitChange = getGeneratedSchematicBitMask(environment);

        if (generateBitChange == 0)
            return false;

        return (generatedSchematics.get() & generateBitChange) != 0;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        setSchematicGenerate(environment, true);
    }

    @Override
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        PluginDebugger.debug("Action: Set Schematic, Island: " + owner.getName() + ", Environment: " + environment);

        int generateBitChange = getGeneratedSchematicBitMask(environment);

        if (generateBitChange == 0)
            return;

        this.generatedSchematics.updateAndGet(generatedSchematics -> {
            return generated ? generatedSchematics | generateBitChange : generatedSchematics & ~generateBitChange & 0xF;
        });

        IslandsDatabaseBridge.saveGeneratedSchematics(this);
    }

    @Override
    public int getGeneratedSchematicsFlag() {
        return this.generatedSchematics.get();
    }

    /*
     *  Generator related methods
     */

    @Override
    public String getSchematicName() {
        return this.schemName == null ? "" : this.schemName;
    }

    @Override
    public int getPosition(SortingType sortingType) {
        return plugin.getGrid().getIslandPosition(this, sortingType);
    }

    @Override
    public IslandChest[] getChest() {
        return islandChests.readAndGet(islandChests -> Arrays.copyOf(islandChests, islandChests.length));
    }

    @Override
    public int getChestSize() {
        return islandChests.readAndGet(islandChests -> islandChests.length);
    }

    @Override
    public void setChestRows(int index, int rows) {
        IslandChest[] islandChests = this.islandChests.get();
        int oldSize = islandChests.length;

        if (index >= oldSize) {
            islandChests = Arrays.copyOf(islandChests, index + 1);
            this.islandChests.set(islandChests);
            for (int i = oldSize; i <= index; i++) {
                (islandChests[i] = new SIslandChest(this, i)).setRows(plugin.getSettings().getIslandChests().getDefaultSize());
            }
        }

        islandChests[index].setRows(rows);

        IslandsDatabaseBridge.markIslandChestsToBeSaved(this, islandChests[index]);
    }

    @Override
    @Deprecated
    public IslandDataHandler getDataHandler() {
        return EmptyDataHandler.getInstance();
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    private void replaceVisitor(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        uniqueVisitors.write(uniqueVisitors -> {
            for (UniqueVisitor uniqueVisitor : uniqueVisitors) {
                if (uniqueVisitor.getSuperiorPlayer().equals(originalPlayer)) {
                    uniqueVisitor.setSuperiorPlayer(newPlayer);
                }
            }
        });
    }

    private void replaceBannedPlayer(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        if (bannedPlayers.remove(originalPlayer)) {
            bannedPlayers.add(newPlayer);
        }
    }

    /*
     *  Schematic methods
     */

    private void replacePermissions(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer) {
        PlayerPermissionNode playerPermissionNode = playerPermissions.remove(originalPlayer);
        if (playerPermissionNode != null) {
            playerPermissions.put(newPlayer, playerPermissionNode);
            IslandsDatabaseBridge.clearPlayerPermission(this, originalPlayer);
            for (Map.Entry<IslandPrivilege, Boolean> privilegeEntry : playerPermissionNode.getCustomPermissions().entrySet())
                IslandsDatabaseBridge.savePlayerPermission(this, newPlayer,
                        privilegeEntry.getKey(), privilegeEntry.getValue());
        }
    }

    private void saveBlockCounts(BigDecimal oldWorth, BigDecimal oldLevel) {
        BigDecimal newWorth = getWorth();
        BigDecimal newLevel = getIslandLevel();

        if (oldLevel.compareTo(newLevel) != 0 || oldWorth.compareTo(newWorth) != 0) {
            Executor.async(() -> plugin.getEventsBus().callIslandWorthUpdateEvent(this, oldWorth, oldLevel, newWorth, newLevel), 0L);
        }

        if (++blocksUpdateCounter >= Bukkit.getOnlinePlayers().size() * 10) {
            IslandsDatabaseBridge.saveBlockCounts(this);
            blocksUpdateCounter = 0;
            plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
            plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
            plugin.getMenus().refreshValues(this);
            plugin.getMenus().refreshCounts(this);
        } else {
            IslandsDatabaseBridge.markBlockCountsToBeSaved(this);
        }

    }

    public void syncUpgrades(boolean overrideCustom) {
        for (World.Environment environment : World.Environment.values())
            clearGeneratorAmounts(environment);
        clearEffects();
        clearBlockLimits();
        clearEntitiesLimits();
        clearUpgrades(true);

        // We want to sync the default upgrade first, then the actual upgrades
        syncUpgrade(DefaultUpgradeLevel.getInstance(), overrideCustom);
        // Syncing all real upgrades
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> syncUpgrade((SUpgradeLevel) getUpgradeLevel(upgrade), overrideCustom));

        if (getIslandSize() != -1)
            updateBorder();
    }

    private void warpPlayerWithoutWarmup(SuperiorPlayer superiorPlayer, IslandWarp islandWarp) {
        Location location = islandWarp.getLocation();
        superiorPlayer.setTeleportTask(null);

        // Warp doesn't exist anymore.
        if (getWarp(islandWarp.getName()) == null) {
            Message.INVALID_WARP.send(superiorPlayer, islandWarp.getName());
            deleteWarp(islandWarp.getName());
            return;
        }

        if (!isInsideRange(location)) {
            Message.UNSAFE_WARP.send(superiorPlayer);
            deleteWarp(islandWarp.getName());
            return;
        }

        if (!LocationUtils.isSafeBlock(location.getBlock())) {
            Message.UNSAFE_WARP.send(superiorPlayer);
            return;
        }

        superiorPlayer.teleport(location, success -> {
            if (success) {
                Message.TELEPORTED_TO_WARP.send(superiorPlayer);
                if (superiorPlayer.isShownAsOnline()) {
                    IslandUtils.sendMessage(this, Message.TELEPORTED_TO_WARP_ANNOUNCEMENT,
                            Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName(), islandWarp.getName());
                }
            }
        });
    }

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        PluginDebugger.debug("Action: Complete Mission, Island: " + owner.getName() + ", Mission: " + mission.getName());

        int finishCount = completedMissions.getOrDefault(mission, 0) + 1;
        completedMissions.put(mission, finishCount);

        IslandsDatabaseBridge.saveMission(this, mission, finishCount);

        plugin.getMenus().refreshMissionsCategory(mission.getMissionCategory());
    }

    /*
     *  Island top methods
     */

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        PluginDebugger.debug("Action: Reset Mission, Island: " + owner.getName() + ", Mission: " + mission.getName());

        int finishCount = completedMissions.getOrDefault(mission, 0) - 1;
        if (finishCount > 0) {
            completedMissions.put(mission, finishCount);
            IslandsDatabaseBridge.saveMission(this, mission, finishCount);
        } else {
            completedMissions.remove(mission);
            IslandsDatabaseBridge.removeMission(this, mission);
        }

        mission.clearData(getOwner());

        plugin.getMenus().refreshMissionsCategory(mission.getMissionCategory());
    }

    /*
     *  Vault related methods
     */

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return completedMissions.getOrDefault(mission, 0) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Optional<MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) <
                missionDataOptional.get().getResetAmount();
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return completedMissions.getOrDefault(mission, 0);
    }

    /*
     *  Data related methods
     */

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return Collections.unmodifiableList(new ArrayList<>(completedMissions.keySet()));
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        return Collections.unmodifiableMap(completedMissions);
    }

    /*
     *  Object related methods
     */

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Island && owner.equals(((Island) obj).getOwner());
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(Island other) {
        if (other == null)
            return -1;

        if (plugin.getSettings().getIslandTopOrder().equals("WORTH")) {
            int compare = getWorth().compareTo(other.getWorth());
            if (compare != 0) return compare;
        } else {
            int compare = getIslandLevel().compareTo(other.getIslandLevel());
            if (compare != 0) return compare;
        }

        return getOwner().getName().compareTo(other.getOwner().getName());
    }

    /*
     *  Private methods
     */

    private void assignIslandChest() {
        islandChests.write(islandChests -> {
            for (int i = 0; i < islandChests.length; i++) {
                islandChests[i] = new SIslandChest(this, i);
                islandChests[i].setRows(plugin.getSettings().getIslandChests().getDefaultSize());
            }
        });
    }

    private void deserializeBlockCounts(@Nullable String blockCounts) {
        try {
            this.blocksTracker.setLoadingDataMode(true);
            IslandsDeserializer.deserializeBlockCounts(blockCounts, this);
        } finally {
            this.blocksTracker.setLoadingDataMode(false);
        }

        if (this.blocksTracker.getBlockCounts().isEmpty())
            calcIslandWorth(null);
    }

    private void loadFromCachedInfo(CachedIslandInfo cachedIslandInfo) {
        this.islandHomes.set(cachedIslandInfo.islandHomes);
        this.members.write(members -> {
            members.addAll(cachedIslandInfo.members);
            members.forEach(member -> member.setIsland(this));
        });
        this.bannedPlayers.addAll(cachedIslandInfo.bannedPlayers);
        this.playerPermissions.putAll(cachedIslandInfo.playerPermissions);
        this.playerPermissions.values().forEach(permissionNode -> permissionNode.setIsland(this));
        this.rolePermissions.putAll(cachedIslandInfo.rolePermissions);
        this.upgrades.putAll(cachedIslandInfo.upgrades);
        this.blockLimits.putAll(cachedIslandInfo.blockLimits);
        this.ratings.putAll(cachedIslandInfo.ratings);
        this.completedMissions.putAll(cachedIslandInfo.completedMissions);
        this.islandFlags.putAll(cachedIslandInfo.islandFlags);
        System.arraycopy(cachedIslandInfo.cobbleGeneratorValues, 0, this.cobbleGeneratorValues,
                0, this.cobbleGeneratorValues.length);
        this.uniqueVisitors.write(uniqueVisitors -> uniqueVisitors.addAll(cachedIslandInfo.uniqueVisitors));
        this.entityLimits.putAll(cachedIslandInfo.entityLimits);
        this.islandEffects.putAll(cachedIslandInfo.islandEffects);
        IslandChest[] islandChests = new IslandChest[cachedIslandInfo.islandChests.size()];
        for (int index = 0; index < islandChests.length; ++index) {
            islandChests[index] = SIslandChest.createChest(this, index, cachedIslandInfo.islandChests.get(index));
        }
        this.islandChests.set(islandChests);
        this.roleLimits.putAll(cachedIslandInfo.roleLimits);
        this.visitorHomes.set(cachedIslandInfo.visitorHomes);

        this.islandSize = cachedIslandInfo.islandSize;
        this.teamLimit = cachedIslandInfo.teamLimit;
        this.warpsLimit = cachedIslandInfo.warpsLimit;
        this.cropGrowth = cachedIslandInfo.cropGrowth;
        this.spawnerRates = cachedIslandInfo.spawnerRates;
        this.mobDrops = cachedIslandInfo.mobDrops;
        this.coopLimit = cachedIslandInfo.coopLimit;
        this.bankLimit = cachedIslandInfo.bankLimit;

        this.islandBank.setBalance(cachedIslandInfo.balance);
        this.lastInterest = cachedIslandInfo.lastInterestTime;

        cachedIslandInfo.cachedWarpInfoList.forEach(cachedWarpInfo -> {
            WarpCategory warpCategory = null;

            if (!cachedWarpInfo.category.isEmpty())
                warpCategory = createWarpCategory(cachedWarpInfo.category);

            IslandWarp islandWarp = createWarp(cachedWarpInfo.name, cachedWarpInfo.location, warpCategory);
            islandWarp.setPrivateFlag(cachedWarpInfo.isPrivate);

            if (cachedWarpInfo.icon != null)
                islandWarp.setIcon(cachedWarpInfo.icon);
        });

        cachedIslandInfo.cachedWarpCategoryInfoList.forEach(cachedWarpCategoryInfo -> {
            WarpCategory warpCategory = getWarpCategory(cachedWarpCategoryInfo.name);

            if (warpCategory != null) {
                if (warpCategory.getWarps().isEmpty()) {
                    deleteCategory(warpCategory);
                    return;
                }

                warpCategory.setSlot(cachedWarpCategoryInfo.slot);

                if (cachedWarpCategoryInfo.icon != null)
                    warpCategory.setIcon(cachedWarpCategoryInfo.icon);
            }
        });

        cachedIslandInfo.bankTransactions.forEach(islandBank::loadTransaction);
    }

    private void startBankInterest() {
        if (BuiltinModules.BANK.bankInterestEnabled) {
            long currentTime = System.currentTimeMillis() / 1000;
            long ticksToNextInterest = (BuiltinModules.BANK.bankInterestInterval - (currentTime - this.lastInterest)) * 20;
            if (ticksToNextInterest <= 0) {
                giveInterest(true);
            } else {
                this.bankInterestTask.set(bankInterestTask -> {
                    if (bankInterestTask != null)
                        bankInterestTask.cancel();
                    return Executor.sync(() -> giveInterest(true), ticksToNextInterest);
                });
            }
        }
    }

    private void checkMembersDuplication() {
        members.write(members -> {
            Iterator<SuperiorPlayer> iterator = members.iterator();
            while (iterator.hasNext()) {
                SuperiorPlayer superiorPlayer = iterator.next();
                if (superiorPlayer.equals(owner) || !this.equals(superiorPlayer.getIsland())) {
                    iterator.remove();
                    IslandsDatabaseBridge.removeMember(this, superiorPlayer);
                }
            }
        });
    }

    private void updateOldUpgradeValues() {
        for (Key key : blockLimits.keySet()) {
            Integer defaultValue = plugin.getSettings().getDefaultValues().getBlockLimits().get(key);
            if (defaultValue != null && (int) blockLimits.get(key).get() == defaultValue)
                blockLimits.put(key, new UpgradeValue<>(defaultValue, true));
        }

        for (Key key : entityLimits.keySet()) {
            Integer defaultValue = plugin.getSettings().getDefaultValues().getEntityLimits().get(key);
            if (defaultValue != null && (int) entityLimits.get(key).get() == defaultValue)
                entityLimits.put(key, new UpgradeValue<>(defaultValue, true));
        }

        for (int i = 0; i < cobbleGeneratorValues.length; i++) {
            Map<Key, Integer> defaultGenerator = plugin.getSettings().getDefaultValues().getGenerators()[i];
            if (defaultGenerator != null) {
                if (cobbleGeneratorValues[i] == null)
                    cobbleGeneratorValues[i] = KeyMapImpl.createConcurrentHashMap();
                for (Key key : cobbleGeneratorValues[i].keySet()) {
                    Integer defaultValue = defaultGenerator.get(key);
                    if (defaultValue != null && (int) cobbleGeneratorValues[i].get(key).get() == defaultValue)
                        cobbleGeneratorValues[i].put(key, new UpgradeValue<>(defaultValue, true));
                }
            }
        }

        if (getIslandSize() == plugin.getSettings().getDefaultValues().getIslandSize())
            islandSize = DefaultUpgradeLevel.getInstance().getBorderSizeUpgradeValue();

        if (getWarpsLimit() == plugin.getSettings().getDefaultValues().getWarpsLimit())
            warpsLimit = DefaultUpgradeLevel.getInstance().getWarpsLimitUpgradeValue();

        if (getTeamLimit() == plugin.getSettings().getDefaultValues().getTeamLimit())
            teamLimit = DefaultUpgradeLevel.getInstance().getTeamLimitUpgradeValue();

        if (getCoopLimit() == plugin.getSettings().getDefaultValues().getCoopLimit())
            coopLimit = DefaultUpgradeLevel.getInstance().getCoopLimitUpgradeValue();

        if (getCropGrowthMultiplier() == plugin.getSettings().getDefaultValues().getCropGrowth())
            cropGrowth = DefaultUpgradeLevel.getInstance().getCropGrowthUpgradeValue();

        if (getSpawnerRatesMultiplier() == plugin.getSettings().getDefaultValues().getSpawnerRates())
            spawnerRates = DefaultUpgradeLevel.getInstance().getSpawnerRatesUpgradeValue();

        if (getMobDropsMultiplier() == plugin.getSettings().getDefaultValues().getMobDrops())
            mobDrops = DefaultUpgradeLevel.getInstance().getMobDropsUpgradeValue();
    }

    private void clearUpgrades(boolean overrideCustom) {
        if (overrideCustom || islandSize.isSynced()) {
            islandSize = new UpgradeValue<>(-1, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveSize(this);
        }
        if (overrideCustom || warpsLimit.isSynced()) {
            warpsLimit = new UpgradeValue<>(-1, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveWarpsLimit(this);
        }
        if (overrideCustom || teamLimit.isSynced()) {
            teamLimit = new UpgradeValue<>(-1, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveTeamLimit(this);
        }
        if (overrideCustom || coopLimit.isSynced()) {
            coopLimit = new UpgradeValue<>(-1, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveCoopLimit(this);
        }
        if (overrideCustom || cropGrowth.isSynced()) {
            cropGrowth = new UpgradeValue<>(-1D, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveCropGrowth(this);
        }
        if (overrideCustom || spawnerRates.isSynced()) {
            spawnerRates = new UpgradeValue<>(-1D, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveSpawnerRates(this);
        }
        if (overrideCustom || mobDrops.isSynced()) {
            mobDrops = new UpgradeValue<>(-1D, true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveMobDrops(this);
        }
        if (overrideCustom || bankLimit.isSynced()) {
            bankLimit = new UpgradeValue<>(new BigDecimal(-2), true);
            if (overrideCustom)
                IslandsDatabaseBridge.saveBankLimit(this);
        }

        blockLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(new UpgradeValue<>(-1, true)));

        entityLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(new UpgradeValue<>(-1, true)));

        for (KeyMap<UpgradeValue<Integer>> cobbleGeneratorValue : cobbleGeneratorValues) {
            if (cobbleGeneratorValue != null) {
                cobbleGeneratorValue.entrySet().stream()
                        .filter(entry -> overrideCustom || entry.getValue().isSynced())
                        .forEach(entry -> entry.setValue(new UpgradeValue<>(-1, true)));
            }
        }

        islandEffects.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(new UpgradeValue<>(-1, true)));

        roleLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(new UpgradeValue<>(-1, true)));
    }

    private void syncUpgrade(SUpgradeLevel upgradeLevel, boolean overrideCustom) {
        if ((overrideCustom || cropGrowth.isSynced()) && cropGrowth.get() < upgradeLevel.getCropGrowth())
            cropGrowth = upgradeLevel.getCropGrowthUpgradeValue();

        if ((overrideCustom || spawnerRates.isSynced()) && spawnerRates.get() < upgradeLevel.getSpawnerRates())
            spawnerRates = upgradeLevel.getSpawnerRatesUpgradeValue();

        if ((overrideCustom || mobDrops.isSynced()) && mobDrops.get() < upgradeLevel.getMobDrops())
            mobDrops = upgradeLevel.getMobDropsUpgradeValue();

        if ((overrideCustom || teamLimit.isSynced()) && teamLimit.get() < upgradeLevel.getTeamLimit())
            teamLimit = upgradeLevel.getTeamLimitUpgradeValue();

        if ((overrideCustom || warpsLimit.isSynced()) && warpsLimit.get() < upgradeLevel.getWarpsLimit())
            warpsLimit = upgradeLevel.getWarpsLimitUpgradeValue();

        if ((overrideCustom || coopLimit.isSynced()) && coopLimit.get() < upgradeLevel.getCoopLimit())
            coopLimit = upgradeLevel.getCoopLimitUpgradeValue();

        if ((overrideCustom || islandSize.isSynced()) && islandSize.get() < upgradeLevel.getBorderSize())
            islandSize = upgradeLevel.getBorderSizeUpgradeValue();

        if ((overrideCustom || bankLimit.isSynced()) && bankLimit.get().compareTo(upgradeLevel.getBankLimit()) < 0)
            bankLimit = upgradeLevel.getBankLimitUpgradeValue();

        for (Map.Entry<Key, UpgradeValue<Integer>> entry : upgradeLevel.getBlockLimitsUpgradeValue().entrySet()) {
            UpgradeValue<Integer> currentValue = blockLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || entry.getValue().get() > currentValue.get())
                blockLimits.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Key, UpgradeValue<Integer>> entry : upgradeLevel.getEntityLimitsUpgradeValue().entrySet()) {
            UpgradeValue<Integer> currentValue = entityLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || entry.getValue().get() > currentValue.get())
                entityLimits.put(entry.getKey(), entry.getValue());
        }

        for (int i = 0; i < cobbleGeneratorValues.length; i++) {
            Map<Key, UpgradeValue<Integer>> levelGenerator = upgradeLevel.getGeneratorUpgradeValue()[i];
            if (levelGenerator != null) {
                KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = getCobbleGeneratorValues(i, true);

                if (!levelGenerator.isEmpty()) {
                    new HashSet<>(cobbleGeneratorValues.entrySet()).stream().filter(entry -> entry.getValue().isSynced())
                            .forEach(entry -> cobbleGeneratorValues.remove(entry.getKey()));
                }

                cobbleGeneratorValues.putAll(levelGenerator);
            }
        }

        for (Map.Entry<PotionEffectType, UpgradeValue<Integer>> entry : upgradeLevel.getPotionEffectsUpgradeValue().entrySet()) {
            UpgradeValue<Integer> currentValue = islandEffects.get(entry.getKey());
            if (currentValue == null || entry.getValue().get() > currentValue.get())
                islandEffects.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<PlayerRole, UpgradeValue<Integer>> entry : upgradeLevel.getRoleLimitsUpgradeValue().entrySet()) {
            UpgradeValue<Integer> currentValue = roleLimits.get(entry.getKey());
            if (currentValue == null || entry.getValue().get() > currentValue.get())
                roleLimits.put(entry.getKey(), entry.getValue());
        }
    }

    private void updateIslandChests() {
        List<IslandChest> islandChestList = new ArrayList<>(Arrays.asList(this.islandChests.get()));
        boolean updatedChests = false;

        while (islandChestList.size() < plugin.getSettings().getIslandChests().getDefaultPages()) {
            IslandChest newIslandChest = new SIslandChest(this, islandChestList.size());
            newIslandChest.setRows(plugin.getSettings().getIslandChests().getDefaultSize());
            islandChestList.add(newIslandChest);
            updatedChests = true;
        }

        if (updatedChests) {
            this.islandChests.set(islandChestList.toArray(new IslandChest[0]));
        }
    }

    private void finishCalcIsland(SuperiorPlayer asker, Runnable callback, BigDecimal islandLevel, BigDecimal islandWorth) {
        plugin.getEventsBus().callIslandWorthCalculatedEvent(this, asker, islandLevel, islandWorth);

        if (asker != null)
            Message.ISLAND_WORTH_RESULT.send(asker, islandWorth, islandLevel);

        if (callback != null)
            callback.run();
    }

    private KeyMap<UpgradeValue<Integer>> getCobbleGeneratorValues(World.Environment environment, boolean createNew) {
        return getCobbleGeneratorValues(environment.ordinal(), createNew);
    }

    private KeyMap<UpgradeValue<Integer>> getCobbleGeneratorValues(int index, boolean createNew) {
        KeyMap<UpgradeValue<Integer>> cobbleGeneratorValues = this.cobbleGeneratorValues[index];

        if (cobbleGeneratorValues == null && createNew)
            cobbleGeneratorValues = this.cobbleGeneratorValues[index] = KeyMapImpl.createConcurrentHashMap();

        return cobbleGeneratorValues;
    }

    private void loadIslandWarp(IslandWarp islandWarp) {
        islandWarp.getCategory().getWarps().add(islandWarp);

        String warpName = islandWarp.getName().toLowerCase(Locale.ENGLISH);

        if (warpsByName.containsKey(warpName))
            deleteWarp(warpName);

        warpsByName.put(warpName, islandWarp);

        Location location = islandWarp.getLocation();

        warpsByLocation.put(new Location(location.getWorld(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ()), islandWarp);
    }

    private static int getGeneratedSchematicBitMask(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return 8;
            case NETHER:
                return 4;
            case THE_END:
                return 3;
            default:
                return 0;
        }
    }

    public static final class UniqueVisitor {

        private final Pair<SuperiorPlayer, Long> pair;

        private SuperiorPlayer superiorPlayer;
        private long lastVisitTime;

        public UniqueVisitor(SuperiorPlayer superiorPlayer, long lastVisitTime) {
            this.superiorPlayer = superiorPlayer;
            this.lastVisitTime = lastVisitTime;
            this.pair = new Pair<>(superiorPlayer, lastVisitTime);
        }

        public SuperiorPlayer getSuperiorPlayer() {
            return superiorPlayer;
        }

        public void setSuperiorPlayer(SuperiorPlayer superiorPlayer) {
            this.superiorPlayer = superiorPlayer;
            this.pair.setKey(superiorPlayer);
        }

        public long getLastVisitTime() {
            return lastVisitTime;
        }

        public void setLastVisitTime(long lastVisitTime) {
            this.lastVisitTime = lastVisitTime;
            this.pair.setValue(lastVisitTime);
        }

        public Pair<SuperiorPlayer, Long> toPair() {
            return this.pair;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueVisitor that = (UniqueVisitor) o;
            return lastVisitTime == that.lastVisitTime && superiorPlayer.equals(that.superiorPlayer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(superiorPlayer, lastVisitTime);
        }

    }

}
