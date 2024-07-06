package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.annotations.Size;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.hooks.LazyWorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.BlockChangeResult;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBlockFlags;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
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
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.IslandArea;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.LegacyMasks;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.profiler.ProfileType;
import com.bgsoftware.superiorskyblock.core.profiler.Profiler;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.core.value.DoubleValue;
import com.bgsoftware.superiorskyblock.core.value.IntValue;
import com.bgsoftware.superiorskyblock.core.value.Value;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import com.bgsoftware.superiorskyblock.island.builder.IslandBuilderImpl;
import com.bgsoftware.superiorskyblock.island.chunk.DirtyChunksContainer;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.island.privilege.PrivilegeNodeAbstract;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.island.upgrade.DefaultUpgradeLevel;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.island.warp.SIslandWarp;
import com.bgsoftware.superiorskyblock.island.warp.SWarpCategory;
import com.bgsoftware.superiorskyblock.mission.MissionData;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeIslandEffects;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SIsland implements Island {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final DatabaseBridge databaseBridge;
    private final IslandBank islandBank;
    private final IslandCalculationAlgorithm calculationAlgorithm;
    private final IslandBlocksTrackerAlgorithm blocksTracker;
    private final IslandEntitiesTrackerAlgorithm entitiesTracker;
    private final Synchronized<BukkitTask> bankInterestTask = Synchronized.of(null);
    private final DirtyChunksContainer dirtyChunksContainer;

    /*
     * Island Identifiers
     */
    private final UUID uuid;
    private final BlockPosition center;
    private final long creationTime;
    @Nullable
    private final String schemName;
    /*
     * Island Upgrade Values
     */
    private final Synchronized<IntValue> islandSize = Synchronized.of(IntValue.syncedFixed(-1));
    private final Synchronized<IntValue> warpsLimit = Synchronized.of(IntValue.syncedFixed(-1));
    private final Synchronized<IntValue> teamLimit = Synchronized.of(IntValue.syncedFixed(-1));
    private final Synchronized<IntValue> coopLimit = Synchronized.of(IntValue.syncedFixed(-1));
    private final Synchronized<DoubleValue> cropGrowth = Synchronized.of(DoubleValue.syncedFixed(-1D));
    private final Synchronized<DoubleValue> spawnerRates = Synchronized.of(DoubleValue.syncedFixed(-1D));
    private final Synchronized<DoubleValue> mobDrops = Synchronized.of(DoubleValue.syncedFixed(-1D));
    private final Synchronized<Value<BigDecimal>> bankLimit = Synchronized.of(Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE));
    private final Map<PlayerRole, IntValue> roleLimits = new ConcurrentHashMap<>();
    private final Synchronized<EnumerateMap<Dimension, KeyMap<IntValue>>> cobbleGeneratorValues = Synchronized.of(new EnumerateMap<>(Dimension.values()));
    private final Map<PotionEffectType, IntValue> islandEffects = new ConcurrentHashMap<>();
    private final KeyMap<IntValue> blockLimits = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);
    private final KeyMap<IntValue> entityLimits = KeyMaps.createConcurrentHashMap(KeyIndicator.ENTITY_TYPE);
    /*
     * Island Player-Trackers
     */
    private final Synchronized<SortedSet<SuperiorPlayer>> members = Synchronized.of(new TreeSet<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final Synchronized<SortedSet<SuperiorPlayer>> playersInside = Synchronized.of(new TreeSet<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final Synchronized<SortedSet<UniqueVisitor>> uniqueVisitors = Synchronized.of(new TreeSet<>(SortingComparators.PAIRED_PLAYERS_NAMES_COMPARATOR));
    private final Set<SuperiorPlayer> bannedPlayers = Sets.newConcurrentHashSet();
    private final Set<SuperiorPlayer> coopPlayers = Sets.newConcurrentHashSet();
    private final Set<SuperiorPlayer> invitedPlayers = Sets.newConcurrentHashSet();
    private final Map<SuperiorPlayer, PlayerPrivilegeNode> playerPermissions = new ConcurrentHashMap<>();
    private final Map<UUID, Rating> ratings = new ConcurrentHashMap<>();
    /*
     * Island Warps
     */
    private final Map<String, IslandWarp> warpsByName = new ConcurrentHashMap<>();
    private final Map<LocationKey, IslandWarp> warpsByLocation = new ConcurrentHashMap<>();
    private final Map<String, WarpCategory> warpCategories = new ConcurrentHashMap<>();
    /*
     * General Settings
     */
    private final Synchronized<EnumerateMap<Dimension, Location>> islandHomes = Synchronized.of(new EnumerateMap<>(Dimension.values()));
    private final Synchronized<EnumerateMap<Dimension, Location>> visitorHomes = Synchronized.of(new EnumerateMap<>(Dimension.values()));
    private final Map<IslandPrivilege, PlayerRole> rolePermissions = new ConcurrentHashMap<>();
    private final Map<IslandFlag, Byte> islandFlags = new ConcurrentHashMap<>();
    private final Map<String, Integer> upgrades = new ConcurrentHashMap<>();
    private final AtomicReference<BigDecimal> islandWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> islandLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final Map<MissionReference, Counter> completedMissions = new ConcurrentHashMap<>();
    private final Synchronized<IslandChest[]> islandChests = Synchronized.of(createDefaultIslandChests());
    private final Synchronized<CompletableFuture<Biome>> biomeGetterTask = Synchronized.of(null);
    private final Synchronized<EnumerateSet<Dimension>> generatedSchematics = Synchronized.of(new EnumerateSet<>(Dimension.values()));
    private final Synchronized<EnumerateSet<Dimension>> unlockedWorlds = Synchronized.of(new EnumerateSet<>(Dimension.values()));
    @Nullable
    private PersistentDataContainer persistentDataContainer;
    /*
     * Island Flags
     */
    private volatile boolean beingRecalculated = false;
    private final AtomicReference<BigInteger> currentTotalBlockCounts = new AtomicReference<>(BigInteger.ZERO);
    private volatile BigInteger lastSavedBlockCounts = BigInteger.ZERO;
    private SuperiorPlayer owner;
    private String creationTimeDate;
    /*
     * Island Time-Trackers
     */
    private volatile long lastTimeUpdate;
    private volatile boolean currentlyActive = false;
    private volatile long lastInterest;
    private volatile long lastUpgradeTime = -1L;
    private volatile boolean giveInterestFailed = false;
    private volatile String discord;
    private volatile String paypal;
    private volatile boolean isLocked;
    private volatile boolean isTopIslandsIgnored;
    private volatile String islandName;
    private volatile String islandRawName;
    private volatile String description;
    private volatile Biome biome = null;

    public SIsland(IslandBuilderImpl builder) {
        this.uuid = builder.uuid;
        this.owner = builder.owner;

        if (this.owner != null) {
            this.owner.setPlayerRole(SPlayerRole.lastRole());
            this.owner.setIsland(this);
        }

        this.center = new SBlockPosition(builder.center);
        this.creationTime = builder.creationTime;
        this.islandName = builder.islandName;
        this.islandRawName = Formatters.STRIP_COLOR_FORMATTER.format(this.islandName);
        this.schemName = builder.islandType;
        this.discord = builder.discord;
        this.paypal = builder.paypal;
        this.bonusWorth.set(builder.bonusWorth);
        this.bonusLevel.set(builder.bonusLevel);
        this.isLocked = builder.isLocked;
        this.isTopIslandsIgnored = builder.isIgnored;
        this.description = builder.description;
        this.generatedSchematics.set(builder.generatedSchematics);
        this.unlockedWorlds.set(builder.unlockedWorlds);
        this.lastTimeUpdate = builder.lastTimeUpdated;
        this.islandHomes.write(islandHomes -> islandHomes.putAll(builder.islandHomes));
        this.members.write(members -> {
            members.addAll(builder.members);
            members.forEach(member -> member.setIsland(this));
        });
        this.bannedPlayers.addAll(builder.bannedPlayers);
        this.playerPermissions.putAll(builder.playerPermissions);
        this.playerPermissions.values().forEach(permissionNode -> permissionNode.setIsland(this));
        this.rolePermissions.putAll(builder.rolePermissions);
        this.upgrades.putAll(builder.upgrades);
        this.blockLimits.putAll(builder.blockLimits);
        this.ratings.putAll(builder.ratings);
        this.completedMissions.putAll(builder.completedMissions);
        this.islandFlags.putAll(builder.islandFlags);
        this.cobbleGeneratorValues.write(cobbleGeneratorValues -> cobbleGeneratorValues.putAll(builder.cobbleGeneratorValues));
        this.uniqueVisitors.write(uniqueVisitors -> uniqueVisitors.addAll(builder.uniqueVisitors));
        this.entityLimits.putAll(builder.entityLimits);
        this.islandEffects.putAll(builder.islandEffects);
        IslandChest[] islandChests = new IslandChest[builder.islandChests.size()];
        for (int index = 0; index < islandChests.length; ++index) {
            islandChests[index] = SIslandChest.createChest(this, index, builder.islandChests.get(index));
        }
        this.islandChests.set(islandChests);
        this.roleLimits.putAll(builder.roleLimits);
        this.visitorHomes.set(builder.visitorHomes);
        this.islandSize.set(builder.islandSize);
        this.teamLimit.set(builder.teamLimit);
        this.warpsLimit.set(builder.warpsLimit);
        this.cropGrowth.set(builder.cropGrowth);
        this.spawnerRates.set(builder.spawnerRates);
        this.mobDrops.set(builder.mobDrops);
        this.coopLimit.set(builder.coopLimit);
        this.bankLimit.set(builder.bankLimit);
        this.lastInterest = builder.lastInterestTime;

        this.databaseBridge = plugin.getFactory().createDatabaseBridge(this);
        this.islandBank = plugin.getFactory().createIslandBank(this, this::hasGiveInterestFailed);
        this.calculationAlgorithm = plugin.getFactory().createIslandCalculationAlgorithm(this);
        this.blocksTracker = plugin.getFactory().createIslandBlocksTrackerAlgorithm(this);
        this.entitiesTracker = plugin.getFactory().createIslandEntitiesTrackerAlgorithm(this);
        this.dirtyChunksContainer = new DirtyChunksContainer(this);

        // We make sure the default world is always marked as generated.
        if (!wasSchematicGenerated(plugin.getSettings().getWorlds().getDefaultWorldDimension())) {
            setSchematicGenerate(plugin.getSettings().getWorlds().getDefaultWorldDimension());
        }

        builder.dirtyChunks.forEach(dirtyChunk -> {
            try {
                WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, dirtyChunk.getWorldName());
                if (worldInfo != null)
                    this.dirtyChunksContainer.markDirty(ChunkPosition.of(worldInfo, dirtyChunk.getX(), dirtyChunk.getZ()), false);
            } catch (IllegalStateException ignored) {
            }
        });
        if (!builder.blockCounts.isEmpty()) {
            plugin.getProviders().addPricesLoadCallback(() -> {
                builder.blockCounts.forEach((block, count) -> handleBlockPlaceInternal(block, count, 0));
                this.lastSavedBlockCounts = this.currentTotalBlockCounts.get();
            });
        }

        builder.warpCategories.forEach(warpCategoryRecord -> {
            loadWarpCategory(warpCategoryRecord.name, warpCategoryRecord.slot, warpCategoryRecord.icon);
        });

        builder.warps.forEach(warpRecord -> {
            WarpCategory warpCategory = null;

            if (!warpRecord.category.isEmpty())
                warpCategory = getWarpCategory(warpRecord.category);

            loadIslandWarp(warpRecord.name, warpRecord.location, warpCategory, warpRecord.isPrivate, warpRecord.icon);
        });

        // We want to save all the limits to the custom block keys
        plugin.getBlockValues().addCustomBlockKeys(builder.blockLimits.keySet());

        updateDatesFormatter();
        startBankInterest();
        checkMembersDuplication();
        updateOldUpgradeValues();
        updateUpgrades();
        updateIslandChests();

        // We can only track entity counts after upgrades are set up
        if (!builder.entityCounts.isEmpty()) {
            builder.entityCounts.forEach((entity, count) -> this.entitiesTracker.trackEntity(entity, count.intValue()));
        }

        this.islandBank.setBalance(builder.balance);
        builder.bankTransactions.forEach(this.islandBank::loadTransaction);
        if (builder.persistentData.length > 0)
            getPersistentDataContainer().load(builder.persistentData);

        this.databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
    }

    /*
     *  General methods
     */

    private static boolean adjustLocationToCenterOfBlock(Location location) {
        boolean changed = false;

        if (location.getX() - 0.5 != location.getBlockX()) {
            location.setX(location.getBlockX() + 0.5);
            changed = true;
        }

        if (location.getZ() - 0.5 != location.getBlockZ()) {
            location.setZ(location.getBlockZ() + 0.5);
            changed = true;
        }

        return changed;
    }

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

    /*
     *  Player related methods
     */

    @Override
    public String getCreationTimeDate() {
        return creationTimeDate;
    }

    @Override
    public void updateDatesFormatter() {
        this.creationTimeDate = Formatters.DATE_FORMATTER.format(new Date(creationTime * 1000));
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        List<SuperiorPlayer> members = this.members.readAndGet(_members -> new SequentialListBuilder<SuperiorPlayer>()
                .mutable()
                .build(_members));

        if (includeOwner)
            members.add(owner);

        return Collections.unmodifiableList(members);
    }

    @Override
    public List<SuperiorPlayer> getIslandMembers(PlayerRole... playerRoles) {
        Preconditions.checkNotNull(playerRoles, "playerRoles parameter cannot be null.");

        List<PlayerRole> rolesToFilter = Arrays.asList(playerRoles);
        List<SuperiorPlayer> members = this.members.readAndGet(_members -> new SequentialListBuilder<SuperiorPlayer>()
                .mutable()
                .filter(superiorPlayer -> rolesToFilter.contains(superiorPlayer.getPlayerRole()))
                .build(_members));


        if (rolesToFilter.contains(SPlayerRole.lastRole()))
            members.add(owner);

        return Collections.unmodifiableList(members);
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return new SequentialListBuilder<SuperiorPlayer>().build(this.bannedPlayers);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return getIslandVisitors(true);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
        return playersInside.readAndGet(playersInside -> new SequentialListBuilder<SuperiorPlayer>()
                .filter(superiorPlayer -> !isMember(superiorPlayer) && (vanishPlayers || superiorPlayer.isShownAsOnline()))
                .build(playersInside));
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return playersInside.readAndGet(playersInside -> new SequentialListBuilder<SuperiorPlayer>()
                .filter(SuperiorPlayer::isOnline)
                .build(playersInside));
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        return uniqueVisitors.readAndGet(uniqueVisitors -> new SequentialListBuilder<SuperiorPlayer>()
                .build(uniqueVisitors, UniqueVisitor::getSuperiorPlayer));
    }

    @Override
    public List<Pair<SuperiorPlayer, Long>> getUniqueVisitorsWithTimes() {
        return uniqueVisitors.readAndGet(uniqueVisitors -> new SequentialListBuilder<Pair<SuperiorPlayer, Long>>()
                .build(uniqueVisitors, UniqueVisitor::toPair));
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.INVITE_MEMBER, owner.getName(), superiorPlayer.getName());

        invitedPlayers.add(superiorPlayer);
        superiorPlayer.addInvite(this);

        //Revoke the invite after 5 minutes
        BukkitExecutor.sync(() -> revokeInvite(superiorPlayer), 6000L);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REVOKE_INVITE, owner.getName(), superiorPlayer.getName());

        invitedPlayers.remove(superiorPlayer);
        superiorPlayer.removeInvite(this);
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return invitedPlayers.contains(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        return new SequentialListBuilder<SuperiorPlayer>().build(this.invitedPlayers);
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");

        Log.debug(Debug.ADD_MEMBER, owner.getName(), superiorPlayer.getName(), playerRole);

        boolean addedNewMember = members.writeAndGet(members -> members.add(superiorPlayer));

        // This player is already an member of the island
        if (!addedNewMember)
            return;

        // Removing player from being a coop.
        if (isCoop(superiorPlayer)) {
            removeCoop(superiorPlayer);
        }

        superiorPlayer.setIsland(this);

        if (plugin.getEventsBus().callPlayerChangeRoleEvent(superiorPlayer, playerRole)) {
            superiorPlayer.setPlayerRole(playerRole);
        } else {
            superiorPlayer.setPlayerRole(SPlayerRole.defaultRole());
        }

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

        Log.debug(Debug.KICK_MEMBER, owner.getName(), superiorPlayer.getName());

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

        superiorPlayer.runIfOnline(player -> {
            MenuView<?, ?> openedView = superiorPlayer.getOpenedView();

            if (openedView != null)
                openedView.closeView();

            if (plugin.getSettings().isTeleportOnKick() && getAllPlayersInside().contains(superiorPlayer)) {
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            } else {
                updateIslandFly(superiorPlayer);
            }
        });

        plugin.getMissions().getPlayerMissions().forEach(mission -> {
            MissionData missionData = plugin.getMissions().getMissionData(mission).orElse(null);
            if (missionData != null && missionData.isLeaveReset())
                superiorPlayer.resetMission(mission);
        });

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
    public void banMember(SuperiorPlayer superiorPlayer, @Nullable SuperiorPlayer whom) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.BAN_PLAYER, owner.getName(), superiorPlayer.getName(), whom);

        boolean bannedPlayer = bannedPlayers.add(superiorPlayer);

        // This player is already banned.
        if (!bannedPlayer)
            return;

        if (isMember(superiorPlayer))
            kickMember(superiorPlayer);

        plugin.getMenus().refreshIslandBannedPlayers(this);

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

        Log.debug(Debug.UNBAN_PLAYER, owner.getName(), superiorPlayer.getName());

        boolean unbannedPlayer = bannedPlayers.remove(superiorPlayer);

        if (unbannedPlayer) {
            plugin.getMenus().refreshIslandBannedPlayers(this);

            IslandsDatabaseBridge.removeBannedPlayer(this, superiorPlayer);
        }
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return bannedPlayers.contains(superiorPlayer);
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.ADD_COOP, owner.getName(), superiorPlayer.getName());

        boolean coopPlayer = coopPlayers.add(superiorPlayer);

        if (coopPlayer)
            plugin.getMenus().refreshCoops(this);
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REMOVE_COOP, owner.getName(), superiorPlayer.getName());

        boolean uncoopPlayer = coopPlayers.remove(superiorPlayer);

        // This player was not coop.
        if (!uncoopPlayer)
            return;

        Location location = superiorPlayer.getLocation();

        if (isLocked() && location != null && isInside(location)) {
            MenuView<?, ?> openedView = superiorPlayer.getOpenedView();
            if (openedView != null)
                openedView.closeView();

            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }

        plugin.getMenus().refreshCoops(this);
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return plugin.getSettings().isCoopMembers() && coopPlayers.contains(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return new SequentialListBuilder<SuperiorPlayer>().build(this.coopPlayers);
    }

    @Override
    public int getCoopLimit() {
        return this.coopLimit.readAndGet(IntValue::get);
    }

    @Override
    public int getCoopLimitRaw() {
        return this.coopLimit.readAndGet(coopLimit -> coopLimit.getNonSynced(-1));
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        coopLimit = Math.max(0, coopLimit);

        Log.debug(Debug.SET_COOP_LIMIT, owner.getName(), coopLimit);

        // Original and new coop limit are the same
        if (coopLimit == getCoopLimitRaw())
            return;

        this.coopLimit.set(IntValue.fixed(coopLimit));
        IslandsDatabaseBridge.saveCoopLimit(this);
    }

    /*
     *  Location related methods
     */

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (inside) {
            Log.debug(Debug.ENTER_ISLAND, owner.getName(), superiorPlayer.getName());
        } else {
            Log.debug(Debug.LEAVE_ISLAND, owner.getName(), superiorPlayer.getName());
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

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_PLAYERS, this);

        if (!isMember(superiorPlayer) && superiorPlayer.isShownAsOnline()) {
            Optional<UniqueVisitor> uniqueVisitorOptional = uniqueVisitors.readAndGet(uniqueVisitors ->
                    uniqueVisitors.stream().filter(pair -> pair.getSuperiorPlayer().equals(superiorPlayer)).findFirst());

            long visitTime = System.currentTimeMillis();

            boolean updateVisitor;

            if (uniqueVisitorOptional.isPresent()) {
                uniqueVisitorOptional.get().setLastVisitTime(visitTime);
                updateVisitor = true;
            } else {
                updateVisitor = uniqueVisitors.writeAndGet(uniqueVisitors -> uniqueVisitors.add(new UniqueVisitor(superiorPlayer, visitTime)));
            }

            if (updateVisitor) {
                plugin.getMenus().refreshUniqueVisitors(this);

                IslandsDatabaseBridge.saveVisitor(this, superiorPlayer, visitTime);
            }
        }

        updateLastTime();

        plugin.getMenus().refreshVisitors(this);
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        return !isMember(superiorPlayer) && (!includeCoopStatus || !isCoop(superiorPlayer));
    }

    @Override
    public Location getCenter(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        World world = plugin.getGrid().getIslandsWorld(this, dimension);

        Preconditions.checkNotNull(world, "Couldn't find world for dimension " + dimension + ".");

        // noinspection deprecation
        return center.parse(world).add(0.5, 0, 0.5);
    }

    @Override
    @Deprecated
    public Location getCenter(World.Environment environment) {
        return getCenter(Dimensions.fromEnvironment(environment));
    }

    @Override
    public BlockPosition getCenterPosition() {
        return center;
    }

    @Override
    @Deprecated
    public Location getTeleportLocation(World.Environment environment) {
        return this.getIslandHome(Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public Map<World.Environment, Location> getTeleportLocations() {
        return this.getIslandHomes();
    }

    @Override
    @Deprecated
    public void setTeleportLocation(Location teleportLocation) {
        this.setIslandHome(teleportLocation);
    }

    @Override
    @Deprecated
    public void setTeleportLocation(World.Environment environment, @Nullable Location teleportLocation) {
        this.setIslandHome(Dimensions.fromEnvironment(environment), teleportLocation);
    }

    @Override
    public Location getIslandHome(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Location islandHome = islandHomes.readAndGet(islandHomes -> islandHomes.get(dimension));

        if (islandHome == null)
            islandHome = getCenter(dimension);

        if (islandHome == null)
            return null;

        World world = plugin.getGrid().getIslandsWorld(this, dimension);

        islandHome = islandHome.clone();
        islandHome.setWorld(world);

        return islandHome;
    }

    @Override
    @Deprecated
    public Location getIslandHome(World.Environment environment) {
        return getIslandHome(Dimensions.fromEnvironment(environment));
    }

    @Override
    public Map<Dimension, Location> getIslandHomesAsDimensions() {
        return Collections.unmodifiableMap(islandHomes.readAndGet(islandHomes -> islandHomes.collect(Dimension.values())));
    }

    @Override
    @Deprecated
    public Map<World.Environment, Location> getIslandHomes() {
        EnumMap<World.Environment, Location> islandHomes = new EnumMap<>(World.Environment.class);

        this.islandHomes.read(islandHomesAsDimensions -> {
            for (Dimension dimension : Dimension.values()) {
                Location islandHome = islandHomesAsDimensions.get(dimension);
                if (islandHome != null) {
                    Object oldValue = islandHomes.put(dimension.getEnvironment(), islandHome);
                    if (oldValue != null)
                        throw new IllegalStateException("Called getIslandHomes but there are multiple environments. " +
                                "Use getIslandHomesAsDimensions instead.");
                }
            }
        });

        return Collections.unmodifiableMap(islandHomes);
    }

    @Override
    public void setIslandHome(Location homeLocation) {
        Preconditions.checkNotNull(homeLocation, "homeLocation parameter cannot be null.");
        Preconditions.checkNotNull(homeLocation.getWorld(), "homeLocation's world cannot be null.");
        Preconditions.checkArgument(isInside(homeLocation), "homeLocation must be inside island.");

        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(homeLocation.getWorld());

        setIslandHome(dimension, homeLocation);
    }

    @Override
    public void setIslandHome(Dimension dimension, @Nullable Location homeLocation) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Log.debug(Debug.SET_ISLAND_HOME, owner.getName(), dimension, homeLocation);

        Location oldHome = islandHomes.writeAndGet(islandHomes ->
                islandHomes.put(dimension, homeLocation == null ? null : homeLocation.clone()));

        if (!Objects.equals(oldHome, homeLocation))
            IslandsDatabaseBridge.saveIslandHome(this, dimension, homeLocation);
    }

    @Override
    public void setIslandHome(World.Environment environment, @Nullable Location homeLocation) {
        setIslandHome(Dimensions.fromEnvironment(environment), homeLocation);
    }

    @Override
    public Location getVisitorsLocation() {
        return getVisitorsLocation((Dimension) null /* unused */);
    }

    @Nullable
    @Override
    public Location getVisitorsLocation(Dimension unused) {
        Dimension defaultWorldDimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();

        Location visitorsLocation = this.visitorHomes.readAndGet(visitorsLocations ->
                visitorsLocations.get(defaultWorldDimension));

        if (visitorsLocation == null)
            return null;

        if (adjustLocationToCenterOfBlock(visitorsLocation))
            IslandsDatabaseBridge.saveVisitorLocation(this, defaultWorldDimension, visitorsLocation);

        World world = plugin.getGrid().getIslandsWorld(this, defaultWorldDimension);
        visitorsLocation.setWorld(world);

        return visitorsLocation.clone();
    }

    @Override
    @Deprecated
    public Location getVisitorsLocation(World.Environment unused) {
        return getVisitorsLocation((Dimension) null /* unused */);
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        Log.debug(Debug.SET_VISITOR_HOME, owner.getName(), visitorsLocation);

        Dimension defaultWorldDimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();

        if (visitorsLocation == null) {
            Location oldVisitorsLocation = this.visitorHomes.writeAndGet(visitorsLocations ->
                    visitorsLocations.remove(defaultWorldDimension));
            if (oldVisitorsLocation != null)
                IslandsDatabaseBridge.removeVisitorLocation(this, defaultWorldDimension);
        } else {
            adjustLocationToCenterOfBlock(visitorsLocation);

            Location oldVisitorsLocation = this.visitorHomes.writeAndGet(visitorsLocations ->
                    visitorsLocations.put(defaultWorldDimension, visitorsLocation.clone()));

            if (!Objects.equals(oldVisitorsLocation, visitorsLocation))
                IslandsDatabaseBridge.saveVisitorLocation(this, defaultWorldDimension, visitorsLocation);
        }
    }

    @Override
    public Location getMinimum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension()).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public BlockPosition getMinimumPosition() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenterPosition().offset(-islandDistance, 0, -islandDistance);
    }

    @Override
    public Location getMinimumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension()).subtract(islandSize, 0, islandSize);
    }

    @Override
    public BlockPosition getMinimumProtectedPosition() {
        int islandSize = getIslandSize();
        return getCenterPosition().offset(-islandSize, 0, -islandSize);
    }

    @Override
    public Location getMaximum() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension()).add(islandDistance, 0, islandDistance);
    }

    @Override
    public BlockPosition getMaximumPosition() {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        return getCenterPosition().offset(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        int islandSize = getIslandSize();
        return getCenter(plugin.getSettings().getWorlds().getDefaultWorldDimension()).add(islandSize, 0, islandSize);
    }

    @Override
    public BlockPosition getMaximumProtectedPosition() {
        int islandSize = getIslandSize();
        return getCenterPosition().offset(islandSize, 0, islandSize);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(0);
    }

    @Override
    public List<Chunk> getAllChunks(int flags) {
        List<Chunk> chunks = new LinkedList<>();

        for (Dimension dimension : Dimension.values()) {
            try {
                chunks.addAll(getAllChunks(dimension, flags));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getAllChunks(Dimension dimension) {
        return getAllChunks(dimension, 0);
    }

    @Override
    public List<Chunk> getAllChunks(Dimension dimension, @IslandChunkFlags int flags) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");

        World world = getCenter(dimension).getWorld();
        return new SequentialListBuilder<Chunk>().build(IslandUtils.getChunkCoords(this, WorldInfo.of(world), flags),
                chunkPosition -> world.getChunkAt(chunkPosition.getX(), chunkPosition.getZ()));
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment) {
        return getAllChunks(Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, @IslandChunkFlags int flags) {
        return getAllChunks(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(boolean onlyProtected) {
        return getAllChunks(onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected) {
        return getAllChunks(Dimensions.fromEnvironment(environment), onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getAllChunks(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    public List<Chunk> getLoadedChunks() {
        return getLoadedChunks(0);
    }

    @Override
    public List<Chunk> getLoadedChunks(@IslandChunkFlags int flags) {
        List<Chunk> chunks = new LinkedList<>();

        for (Dimension dimension : Dimension.values()) {
            try {
                chunks.addAll(getLoadedChunks(dimension, flags));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getLoadedChunks(Dimension dimension) {
        return getLoadedChunks(dimension, 0);
    }

    @Override
    public List<Chunk> getLoadedChunks(Dimension dimension, @IslandChunkFlags int flags) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");

        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, dimension);

        return new SequentialListBuilder<Chunk>().filter(Objects::nonNull).build(
                IslandUtils.getChunkCoords(this, worldInfo, flags), plugin.getNMSChunks()::getChunkIfLoaded);
    }

    @Override
    @Deprecated
    public List<Chunk> getLoadedChunks(World.Environment environment) {
        return getLoadedChunks(Dimensions.fromEnvironment(environment));
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, @IslandChunkFlags int flags) {
        return getLoadedChunks(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getLoadedChunks(flags);
    }

    @Override
    @Deprecated
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getLoadedChunks(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension dimension) {
        return getAllChunksAsync(dimension, 0);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension dimension, @IslandChunkFlags int flags) {
        return getAllChunksAsync(dimension, flags, null);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension dimension,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(dimension, 0, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(Dimension dimension, @IslandChunkFlags int flags,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");

        World world = getCenter(dimension).getWorld();
        return IslandUtils.getAllChunksAsync(this, world, flags, ChunkLoadReason.API_REQUEST, onChunkLoad);
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment) {
        return getAllChunksAsync(Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @IslandChunkFlags int flags) {
        return getAllChunksAsync(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(Dimensions.fromEnvironment(environment), onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, @IslandChunkFlags int flags,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(Dimensions.fromEnvironment(environment), flags, onChunkLoad);
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0, onChunkLoad);
    }

    @Override
    @Deprecated
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment,
                                                            boolean onlyProtected, boolean noEmptyChunks,
                                                            @Nullable Consumer<Chunk> onChunkLoad) {
        int flags = 0;
        if (onlyProtected) flags |= IslandChunkFlags.ONLY_PROTECTED;
        if (noEmptyChunks) flags |= IslandChunkFlags.NO_EMPTY_CHUNKS;
        return getAllChunksAsync(environment, flags, onChunkLoad);
    }

    @Override
    public void resetChunks() {
        resetChunks((Runnable) null);
    }

    @Override
    public void resetChunks(@Nullable Runnable onFinish) {
        resetChunks(0, onFinish);
    }

    @Override
    public void resetChunks(Dimension dimension) {
        resetChunks(dimension, 0);
    }

    @Override
    public void resetChunks(Dimension dimension, @Nullable Runnable onFinish) {
        resetChunks(dimension, 0, onFinish);
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags) {
        resetChunks(flags, null);
    }

    @Override
    public void resetChunks(@IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        LinkedList<List<ChunkPosition>> worldsChunks = new LinkedList<>(
                IslandUtils.getChunkCoords(this, flags | IslandChunkFlags.NO_EMPTY_CHUNKS).values());


        if (worldsChunks.isEmpty()) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        for (List<ChunkPosition> chunkPositions : worldsChunks)
            IslandUtils.deleteChunks(this, chunkPositions, chunkPositions == worldsChunks.getLast() ? onFinish : null);
    }

    @Override
    public void resetChunks(Dimension dimension, @IslandChunkFlags int flags) {
        resetChunks(dimension, flags, null);
    }

    @Override
    public void resetChunks(Dimension dimension, @IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");

        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, dimension);

        List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this,
                worldInfo, flags | IslandChunkFlags.NO_EMPTY_CHUNKS);

        if (chunkPositions.isEmpty()) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        IslandUtils.deleteChunks(this, chunkPositions, onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment) {
        resetChunks(Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, @Nullable Runnable onFinish) {
        resetChunks(Dimensions.fromEnvironment(environment), onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags) {
        resetChunks(Dimensions.fromEnvironment(environment), flags);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, @IslandChunkFlags int flags, @Nullable Runnable onFinish) {
        resetChunks(Dimensions.fromEnvironment(environment), flags, onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        resetChunks(Dimensions.fromEnvironment(environment), onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public void resetChunks(World.Environment environment, boolean onlyProtected, @Nullable Runnable onFinish) {
        resetChunks(Dimensions.fromEnvironment(environment), onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0, onFinish);
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected) {
        resetChunks(onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0);
    }

    @Override
    @Deprecated
    public void resetChunks(boolean onlyProtected, @Nullable Runnable onFinish) {
        resetChunks(onlyProtected ? IslandChunkFlags.ONLY_PROTECTED : 0, onFinish);
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
    public boolean isInside(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");

        if (!plugin.getGrid().isIslandsWorld(world))
            return false;

        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        IslandArea islandArea = new IslandArea(this.center, islandDistance);
        islandArea.rshift(4);

        return islandArea.intercepts(chunkX, chunkZ);
    }

    private boolean isChunkInside(int chunkX, int chunkZ) {
        int islandDistance = (int) Math.round(plugin.getSettings().getMaxIslandSize() *
                (plugin.getSettings().isBuildOutsideIsland() ? 1.5 : 1D));
        IslandArea islandArea = new IslandArea(this.center, islandDistance);
        islandArea.rshift(4);

        return islandArea.intercepts(chunkX, chunkZ);
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
        return isDimensionEnabled(Dimensions.NORMAL);
    }

    @Override
    public void setNormalEnabled(boolean enabled) {
        setDimensionEnabled(Dimensions.NORMAL, enabled);
    }

    @Override
    public boolean isNetherEnabled() {
        return isDimensionEnabled(Dimensions.NETHER);
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        setDimensionEnabled(Dimensions.NETHER, enabled);
    }

    @Override
    public boolean isEndEnabled() {
        return isDimensionEnabled(Dimensions.THE_END);
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        setDimensionEnabled(Dimensions.THE_END, enabled);
    }

    @Override
    public boolean isDimensionEnabled(Dimension dimension) {
        return plugin.getProviders().getWorldsProvider().isDimensionUnlocked(dimension) ||
                unlockedWorlds.readAndGet(unlockedWorlds -> unlockedWorlds.contains(dimension));
    }

    @Override
    public void setDimensionEnabled(Dimension dimension, boolean enabled) {
        Log.debug(Debug.SET_DIMENSION_ENABLED, owner.getName(), dimension.getName(), enabled);

        boolean updated = this.unlockedWorlds.writeAndGet(unlockedWorlds -> {
            return enabled ? unlockedWorlds.add(dimension) : unlockedWorlds.remove(dimension);
        });

        if (updated)
            IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public Set<Dimension> getUnlockedWorlds() {
        return Collections.unmodifiableSet(this.unlockedWorlds.readAndGet(unlockedWorlds ->
                unlockedWorlds.collect(Dimension.values())));
    }

    @Override
    @Deprecated
    public int getUnlockedWorldsFlag() {
        return this.unlockedWorlds.readAndGet(LegacyMasks::convertUnlockedWorldsMask);
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

        Log.debug(Debug.SET_PERMISSION, owner.getName(), playerRole, islandPrivilege);

        PlayerRole oldRole = rolePermissions.put(islandPrivilege, playerRole);

        if (oldRole == playerRole)
            return;

        if (islandPrivilege == IslandPrivileges.FLY) {
            getAllPlayersInside().forEach(this::updateIslandFly);
        } else if (islandPrivilege == IslandPrivileges.VILLAGER_TRADING) {
            getAllPlayersInside().forEach(superiorPlayer -> IslandUtils.updateTradingMenus(this, superiorPlayer));
        }

        IslandsDatabaseBridge.saveRolePermission(this, playerRole, islandPrivilege);
    }

    @Override
    public void resetPermissions() {
        Log.debug(Debug.RESET_PERMISSIONS, owner.getName());

        if (rolePermissions.isEmpty())
            return;

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

        Log.debug(Debug.SET_PERMISSION, owner.getName(),
                superiorPlayer.getName(), islandPrivilege, value);

        PlayerPrivilegeNode privilegeNode = playerPermissions.computeIfAbsent(superiorPlayer,
                s -> new PlayerPrivilegeNode(superiorPlayer, this));

        privilegeNode.setPermission(islandPrivilege, value);

        if (superiorPlayer.isOnline() && isInside(superiorPlayer.getLocation())) {
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

        Log.debug(Debug.RESET_PERMISSIONS, owner.getName(), superiorPlayer.getName());

        PlayerPrivilegeNode oldPrivilegeNode = playerPermissions.remove(superiorPlayer);

        if (oldPrivilegeNode == null)
            return;

        if (superiorPlayer.isOnline()) {
            updateIslandFly(superiorPlayer);
            IslandUtils.updateTradingMenus(this, superiorPlayer);
        }

        IslandsDatabaseBridge.clearPlayerPermission(this, superiorPlayer);

        plugin.getMenus().refreshPermissions(this, superiorPlayer);
    }

    @Override
    public PrivilegeNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return playerPermissions.getOrDefault(superiorPlayer, new PlayerPrivilegeNode(superiorPlayer, this));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");

        PlayerRole playerRole = rolePermissions.get(islandPrivilege);

        if (playerRole != null)
            return playerRole;

        return plugin.getRoles().getRoles().stream()
                .filter(_playerRole -> {
                    if (!plugin.getSettings().isCoopMembers() && _playerRole == SPlayerRole.coopRole())
                        return false;

                    return ((SPlayerRole) _playerRole).getDefaultPermissions().hasPermission(islandPrivilege);
                })
                .min(Comparator.comparingInt(PlayerRole::getWeight)).orElse(SPlayerRole.lastRole());
    }

    /*
     *  General methods
     */

    @Override
    public Map<SuperiorPlayer, PermissionNode> getPlayerPermissions() {
        return Collections.unmodifiableMap(playerPermissions);
    }

    @Override
    public Map<IslandPrivilege, PlayerRole> getRolePermissions() {
        return Collections.unmodifiableMap(rolePermissions);
    }

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

        Log.debug(Debug.SET_NAME, owner.getName(), islandName);

        if (Objects.equals(islandName, this.islandName))
            return;

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

        Log.debug(Debug.SET_DESCRIPTION, owner.getName(), description);

        if (Objects.equals(this.description, description))
            return;

        this.description = description;

        IslandsDatabaseBridge.saveDescription(this);
    }

    @Override
    public void disbandIsland() {
        long profilerId = Profiler.start(ProfileType.DISBAND_ISLAND, 2);

        forEachIslandMember(Collections.emptyList(), false, islandMember -> {
            if (islandMember.equals(owner)) {
                owner.setIsland(null);
            } else {
                kickMember(islandMember);
            }

            if (plugin.getSettings().isDisbandInventoryClear())
                plugin.getNMSPlayers().clearInventory(islandMember.asOfflinePlayer());

            for (Mission<?> mission : plugin.getMissions().getPlayerMissions()) {
                MissionData missionData = plugin.getMissions().getMissionData(mission).orElse(null);
                if (missionData != null && missionData.isDisbandReset()) {
                    islandMember.resetMission(mission);
                }
            }
        });

        invitedPlayers.forEach(invitedPlayer -> invitedPlayer.removeInvite(this));

        if (BuiltinModules.BANK.disbandRefund > 0)
            plugin.getProviders().depositMoney(getOwner(), islandBank.getBalance()
                    .multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund)));

        plugin.getMissions().getIslandMissions().forEach(this::resetMission);

        resetChunks(IslandChunkFlags.ONLY_PROTECTED, () -> Profiler.end(profilerId));

        plugin.getGrid().deleteIsland(this);

        Profiler.end(profilerId);
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (superiorPlayer.equals(owner))
            return false;

        SuperiorPlayer previousOwner = getOwner();

        if (!plugin.getEventsBus().callIslandTransferEvent(this, previousOwner, superiorPlayer))
            return false;

        Log.debug(Debug.TRANSFER_ISLAND, owner.getName(), superiorPlayer.getName());

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

        plugin.getMissions().getIslandMissions().forEach(mission ->
                mission.transferData(previousOwner, owner));

        return true;
    }

    @Override
    public void replacePlayers(SuperiorPlayer originalPlayer, @Nullable SuperiorPlayer newPlayer) {
        Preconditions.checkNotNull(originalPlayer, "originalPlayer parameter cannot be null.");
        Preconditions.checkState(originalPlayer != newPlayer, "originalPlayer and newPlayer cannot equal.");

        Log.debug(Debug.REPLACE_PLAYER, owner, originalPlayer, newPlayer);

        if (owner.equals(originalPlayer)) {
            if (newPlayer == null) {
                Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Disband Island");
                this.disbandIsland();
            } else {
                Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Replace Owner");
                owner = newPlayer;
            }
        } else if (isMember(originalPlayer)) {
            Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Replace Member");
            members.write(members -> {
                members.remove(originalPlayer);
                if (newPlayer != null)
                    members.add(newPlayer);
            });
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
        Log.debug(Debug.CALCULATE_ISLAND, owner.getName(), asker);

        long lastUpdateTime = getLastTimeUpdate();

        if (lastUpdateTime != -1 && (System.currentTimeMillis() / 1000) - lastUpdateTime >= 600) {
            Log.debugResult(Debug.CALCULATE_ISLAND, "Result Cooldown", owner.getName());
            finishCalcIsland(asker, callback, getIslandLevel(), getWorth());
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            calcIslandWorthInternal(asker, callback);
        } else {
            BukkitExecutor.sync(() -> calcIslandWorthInternal(asker, callback));
        }
    }

    @Override
    public IslandCalculationAlgorithm getCalculationAlgorithm() {
        return this.calculationAlgorithm;
    }

    @Override
    public void updateBorder() {
        Log.debug(Debug.UPDATE_BORDER, owner.getName());
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

        return this.islandSize.readAndGet(IntValue::get);
    }

    @Override
    public void setIslandSize(int islandSize) {
        islandSize = Math.max(1, islandSize);

        Preconditions.checkArgument(islandSize <= plugin.getSettings().getMaxIslandSize(), "Border size " + islandSize + " cannot be larger than max island size: " + plugin.getSettings().getMaxIslandSize());

        Log.debug(Debug.SET_SIZE, owner.getName(), islandSize);

        if (islandSize == getIslandSizeRaw())
            return;

        setIslandSizeInternal(IntValue.fixed(islandSize));

        IslandsDatabaseBridge.saveSize(this);
    }

    private void setIslandSizeInternal(IntValue islandSize) {
        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);

        if (cropGrowthEnabled) {
            // First, we want to remove all the current crop tile entities
            getLoadedChunks(IslandChunkFlags.ONLY_PROTECTED).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, true));
        }

        this.islandSize.set(islandSize);

        if (cropGrowthEnabled) {
            // Now, we want to update the tile entities again
            getLoadedChunks(IslandChunkFlags.ONLY_PROTECTED).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, false));
        }

        updateBorder();
    }

    @Override
    public int getIslandSizeRaw() {
        return this.islandSize.readAndGet(islandSize -> islandSize.getNonSynced(-1));
    }

    @Override
    public String getDiscord() {
        return discord;
    }

    @Override
    public void setDiscord(String discord) {
        Preconditions.checkNotNull(discord, "discord parameter cannot be null.");

        Log.debug(Debug.SET_DISCORD, owner.getName(), discord);

        if (Objects.equals(discord, this.discord))
            return;

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

        Log.debug(Debug.SET_PAYPAL, owner.getName(), paypal);

        if (Objects.equals(paypal, this.paypal))
            return;

        this.paypal = paypal;
        IslandsDatabaseBridge.savePaypal(this);
    }

    @Override
    public Biome getBiome() {
        if (biome == null) {
            biomeGetterTask.set(task -> {
                if (task != null)
                    return task;

                Dimension defaultWorldDimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
                WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, defaultWorldDimension);
                Location centerBlock = getCenter(defaultWorldDimension);

                ChunkPosition centerChunkPosition = ChunkPosition.of(worldInfo,
                        centerBlock.getBlockX() >> 4, centerBlock.getBlockZ() >> 4);

                return ChunksProvider.loadChunk(centerChunkPosition, ChunkLoadReason.BIOME_REQUEST, null)
                        .thenApply(chunk -> centerBlock.getBlock().getBiome())
                        .whenComplete((biome, error) -> {
                            if (error != null)
                                error.printStackTrace();
                            else {
                                this.biome = biome;
                                biomeGetterTask.set((CompletableFuture<Biome>) null);
                            }
                        });
            });

            return IslandUtils.getDefaultWorldBiome(plugin.getSettings().getWorlds().getDefaultWorldDimension());
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

        Log.debug(Debug.SET_BIOME, owner.getName(), biome, updateBlocks);

        this.biome = biome;

        if (!updateBlocks)
            return;

        List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                .build(getAllPlayersInside(), SuperiorPlayer::asPlayer);

        for (Dimension dimension : Dimension.values()) {
            if (plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension) && wasSchematicGenerated(dimension)) {
                WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, dimension);
                Biome worldBiome = plugin.getSettings().getWorlds().getDefaultWorldDimension() == dimension ?
                        biome : IslandUtils.getDefaultWorldBiome(dimension);
                List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, worldInfo, 0);
                plugin.getNMSChunks().setBiome(chunkPositions, worldBiome, playersToUpdate);
            }
        }

        for (World registeredWorld : plugin.getGrid().getRegisteredWorlds()) {
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, WorldInfo.of(registeredWorld), 0);
            plugin.getNMSChunks().setBiome(chunkPositions, biome, playersToUpdate);
        }
    }

    @Override
    public boolean isLocked() {
        return isLocked;
    }

    @Override
    public void setLocked(boolean locked) {
        Log.debug(Debug.SET_LOCKED, owner.getName(), locked);

        if (this.isLocked == locked)
            return;

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
        Log.debug(Debug.SET_IGNORED, owner.getName(), ignored);

        if (this.isTopIslandsIgnored == ignored)
            return;

        this.isTopIslandsIgnored = ignored;

        // We want top islands to get sorted again even if only 1 island exists
        plugin.getGrid().setForceSort(true);

        IslandsDatabaseBridge.saveIgnoredStatus(this);
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers) {
        Preconditions.checkNotNull(message, "message parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = ignoredMembers.length == 0 ? Collections.emptyList() : Arrays.asList(ignoredMembers);

        Log.debug(Debug.SEND_MESSAGE, owner.getName(), message, ignoredList);

        forEachIslandMember(ignoredList, false, islandMember -> Message.CUSTOM.send(islandMember, message, false));
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, Object... args) {
        this.sendMessage(messageComponent, Collections.emptyList(), args);
    }

    @Override
    public void sendMessage(IMessageComponent messageComponent, List<UUID> ignoredMembers, Object... args) {
        Preconditions.checkNotNull(messageComponent, "messageComponent parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        Log.debug(Debug.SEND_MESSAGE, owner.getName(), messageComponent.getMessage(), ignoredMembers, Arrays.asList(args));

        forEachIslandMember(ignoredMembers, false, islandMember -> messageComponent.sendMessage(islandMember.asPlayer(), args));
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration,
                          int fadeOut, UUID... ignoredMembers) {
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = ignoredMembers.length == 0 ? Collections.emptyList() : Arrays.asList(ignoredMembers);

        Log.debug(Debug.SEND_TITLE, owner.getName(), title, subtitle, fadeIn, duration, fadeOut, ignoredList);

        forEachIslandMember(ignoredList, true, islandMember ->
                plugin.getNMSPlayers().sendTitle(islandMember.asPlayer(), title, subtitle, fadeIn, duration, fadeOut)
        );
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        Preconditions.checkNotNull(command, "command parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = ignoredMembers.length == 0 ? Collections.emptyList() : Arrays.asList(ignoredMembers);

        Log.debug(Debug.EXECUTE_ISLAND_COMMANDS, owner.getName(), command, onlyOnlineMembers, ignoredList);

        forEachIslandMember(ignoredList, onlyOnlineMembers, islandMember ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player-name}", islandMember.getName()))
        );
    }

    @Override
    public boolean isBeingRecalculated() {
        return beingRecalculated;
    }

    @Override
    public void updateLastTime() {
        setLastTimeUpdate(System.currentTimeMillis() / 1000);
    }

    @Override
    public void setCurrentlyActive() {
        setCurrentlyActive(true);
    }

    @Override
    public void setCurrentlyActive(boolean active) {
        this.currentlyActive = active;
    }

    @Override
    public boolean isCurrentlyActive() {
        return this.currentlyActive;
    }

    @Override
    public long getLastTimeUpdate() {
        return this.currentlyActive ? -1 : lastTimeUpdate;
    }

    @Override
    public void setLastTimeUpdate(long lastTimeUpdate) {
        Log.debug(Debug.SET_ISLAND_LAST_TIME_UPDATED, owner.getName(), lastTimeUpdate);

        if (this.lastTimeUpdate == lastTimeUpdate)
            return;

        this.lastTimeUpdate = lastTimeUpdate;

        if (!isCurrentlyActive())
            IslandsDatabaseBridge.saveLastTimeUpdate(this);
    }

    @Override
    public IslandBank getIslandBank() {
        return islandBank;
    }

    @Override
    public BigDecimal getBankLimit() {
        return this.bankLimit.readAndGet(Value::get);
    }

    /*
     *  Bank related methods
     */

    @Override
    public void setBankLimit(BigDecimal bankLimit) {
        Preconditions.checkNotNull(bankLimit, "bankLimit parameter cannot be null.");

        Log.debug(Debug.SET_BANK_LIMIT, owner.getName(), bankLimit);

        if (Objects.equals(bankLimit, getBankLimitRaw()))
            return;

        if (bankLimit.compareTo(SYNCED_BANK_LIMIT_VALUE) <= 0) {
            this.bankLimit.set(Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE));

            getUpgrades().forEach((upgradeName, level) -> {
                Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);
                if (upgrade != null) {
                    UpgradeLevel upgradeLevel = upgrade.getUpgradeLevel(level);
                    if (upgradeLevel.getBankLimit().compareTo(getBankLimit()) > 0) {
                        this.bankLimit.set(Value.syncedFixed(upgradeLevel.getBankLimit()));
                    }
                }
            });
        } else {
            this.bankLimit.set(Value.fixed(bankLimit));
        }

        // Trying to give interest again if the last one failed.
        if (hasGiveInterestFailed())
            giveInterest(false);

        IslandsDatabaseBridge.saveBankLimit(this);
    }

    @Override
    public BigDecimal getBankLimitRaw() {
        return this.bankLimit.readAndGet(bankLimit -> bankLimit.getNonSynced(SYNCED_BANK_LIMIT_VALUE));
    }

    @Override
    public boolean giveInterest(boolean checkOnlineOwner) {
        Log.debug(Debug.GIVE_BANK_INTEREST, owner.getName());

        long currentTime = System.currentTimeMillis() / 1000;

        if (checkOnlineOwner && BuiltinModules.BANK.bankInterestRecentActive > 0 &&
                currentTime - owner.getLastTimeStatus() > BuiltinModules.BANK.bankInterestRecentActive) {
            Log.debugResult(Debug.GIVE_BANK_INTEREST, "Return Cooldown", owner.getName());
            return false;
        }

        BigDecimal balance = islandBank.getBalance().max(BigDecimal.ONE);
        BigDecimal balanceToGive = balance.multiply(new BigDecimal(BuiltinModules.BANK.bankInterestPercentage / 100D));

        // If the money that will be given exceeds limit, we want to give money later.
        if (!islandBank.canDepositMoney(balanceToGive)) {
            Log.debugResult(Debug.GIVE_BANK_INTEREST, "Return Cannot Deposit Money", owner.getName());
            giveInterestFailed = true;
            return false;
        }

        Log.debugResult(Debug.GIVE_BANK_INTEREST, "Return Success", owner.getName());

        giveInterestFailed = false;

        islandBank.depositAdminMoney(Bukkit.getConsoleSender(), balanceToGive);
        plugin.getMenus().refreshIslandBank(this);

        setLastInterestTime(currentTime);

        return true;
    }

    @Override
    public long getLastInterestTime() {
        return lastInterest;
    }

    @Override
    public void setLastInterestTime(long lastInterest) {
        if (this.lastInterest == lastInterest)
            return;

        if (BuiltinModules.BANK.bankInterestEnabled) {
            long ticksToNextInterest = BuiltinModules.BANK.bankInterestInterval * 20L;
            this.bankInterestTask.set(bankInterestTask -> {
                if (bankInterestTask != null)
                    bankInterestTask.cancel();
                return BukkitExecutor.sync(() -> giveInterest(true), ticksToNextInterest);
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

    /*
     *  Worth related methods
     */

    @Override
    public void handleBlockPlace(Block block) {
        handleBlockPlace(block, 1);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block) {
        return handleBlockPlaceWithResult(block, 1);
    }

    @Override
    public void handleBlockPlace(Key key) {
        handleBlockPlace(key, 1);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key) {
        return handleBlockPlaceWithResult(key, 1);
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount) {
        handleBlockPlace(block, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount) {
        return handleBlockPlaceWithResult(block, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount) {
        handleBlockPlace(key, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount) {
        return handleBlockPlaceWithResult(key, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlockPlace(Block block, @Size int amount, @IslandBlockFlags int flags) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockPlace(Keys.of(block), amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Block block, @Size int amount, @IslandBlockFlags int flags) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return handleBlockPlaceWithResult(Keys.of(block), amount, flags);
    }

    @Override
    public void handleBlockPlace(Key key, @Size int amount, @IslandBlockFlags int flags) {
        handleBlockPlaceWithResult(key, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockPlaceWithResult(Key key, @Size int amount, @IslandBlockFlags int flags) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkArgument(amount > 0, "amount parameter must be positive.");

        BigInteger amountBig = BigInteger.valueOf(amount);

        return handleBlockPlaceInternal(key, amountBig, flags);
    }

    private BlockChangeResult handleBlockPlaceInternal(Key key, @Size BigInteger amount, @IslandBlockFlags int flags) {
        boolean trackedBlock = this.blocksTracker.trackBlock(key, amount);

        if (!trackedBlock)
            return BlockChangeResult.MISSING_BLOCK_VALUE;

        BigInteger newTotalBlocksCount = this.currentTotalBlockCounts.updateAndGet(count -> count.add(amount));

        BigDecimal oldWorth = getWorth();
        BigDecimal oldLevel = getIslandLevel();

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(key);
        BigDecimal blockWorth = blockValue.getWorth();
        BigDecimal blockLevel = blockValue.getLevel();

        boolean saveBlockCounts = (flags & IslandBlockFlags.SAVE_BLOCK_COUNTS) != 0;
        boolean updateLastTimeStatus = (flags & IslandBlockFlags.UPDATE_LAST_TIME_STATUS) != 0;

        if (blockWorth.compareTo(BigDecimal.ZERO) != 0) {
            islandWorth.updateAndGet(islandWorth -> islandWorth.add(blockWorth.multiply(new BigDecimal(amount))));
            if (saveBlockCounts)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            islandLevel.updateAndGet(islandLevel -> islandLevel.add(blockLevel.multiply(new BigDecimal(amount))));
            if (saveBlockCounts)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
        }

        if (updateLastTimeStatus)
            updateLastTime();

        if (saveBlockCounts)
            saveBlockCounts(newTotalBlocksCount, oldWorth, oldLevel);

        return BlockChangeResult.SUCCESS;
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Block block, @Size int amount, boolean save) {
        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
        handleBlockPlace(block, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, @Size int amount, boolean save) {
        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
        handleBlockPlace(key, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, @Size BigInteger amount, boolean save) {
        Preconditions.checkNotNull(key, "key argument cannot be null");
        Preconditions.checkNotNull(amount, "amount argument cannot be null");

        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;

        handleBlockPlace(key, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockPlace(Key key, @Size BigInteger amount, boolean save, boolean updateLastTimeStatus) {
        Preconditions.checkNotNull(key, "key argument cannot be null");
        Preconditions.checkNotNull(amount, "amount argument cannot be null");

        int flags = 0;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
        if (updateLastTimeStatus) flags |= IslandBlockFlags.UPDATE_LAST_TIME_STATUS;

        handleBlockPlace(key, amount, flags);
    }

    @Deprecated
    private void handleBlockPlace(Key key, @Size BigInteger amount, @IslandBlockFlags int flags) {
        BigInteger MAX_INT_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);
        while (amount.compareTo(MAX_INT_VALUE) > 0) {
            handleBlockPlace(key, Integer.MAX_VALUE, flags);
            amount = amount.subtract(MAX_INT_VALUE);
        }

        handleBlockPlace(key, amount.intValueExact(), flags);
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks) {
        handleBlocksPlace(blocks, IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks) {
        return handleBlocksPlaceWithResult(blocks,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlocksPlace(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        handleBlocksPlaceWithResult(blocks, flags);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksPlaceWithResult(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        Preconditions.checkNotNull(blocks, "blocks parameter cannot be null.");

        if (blocks.isEmpty())
            return KeyMaps.createEmptyMap();

        BigDecimal oldWorth = getWorth();
        BigDecimal oldLevel = getIslandLevel();

        KeyMap<BlockChangeResult> result = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);

        blocks.forEach((blockKey, amount) -> {
            BlockChangeResult blockResult = handleBlockPlaceWithResult(blockKey, amount, 0);
            if (blockResult != BlockChangeResult.SUCCESS)
                result.put(blockKey, blockResult);
        });

        boolean saveBlockCounts = (flags & IslandBlockFlags.SAVE_BLOCK_COUNTS) != 0;
        boolean updateLastTimeStatus = (flags & IslandBlockFlags.UPDATE_LAST_TIME_STATUS) != 0;

        if (saveBlockCounts)
            saveBlockCounts(this.currentTotalBlockCounts.get(), oldWorth, oldLevel);

        if (updateLastTimeStatus)
            updateLastTime();

        return result.isEmpty() ? KeyMaps.createEmptyMap() : KeyMaps.unmodifiableKeyMap(result);
    }

    @Override
    public void handleBlockBreak(Block block) {
        handleBlockBreak(block, 1);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block) {
        return handleBlockBreakWithResult(block, 1);
    }

    @Override
    public void handleBlockBreak(Key key) {
        handleBlockBreak(key, 1);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key) {
        return handleBlockBreakWithResult(key, 1);
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount) {
        handleBlockBreak(block, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount) {
        return handleBlockBreakWithResult(block, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount) {
        handleBlockBreak(key, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount) {
        return handleBlockBreakWithResult(key, amount,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlockBreak(Block block, @Size int amount, @IslandBlockFlags int flags) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        handleBlockBreak(Keys.of(block), amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Block block, @Size int amount, int flags) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return handleBlockBreakWithResult(Keys.of(block), amount, flags);
    }

    @Override
    public void handleBlockBreak(Key key, @Size int amount, @IslandBlockFlags int flags) {
        handleBlockBreakWithResult(key, amount, flags);
    }

    @Override
    public BlockChangeResult handleBlockBreakWithResult(Key key, @Size int amount, int flags) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkArgument(amount > 0, "amount parameter must be positive.");

        BigInteger amountBig = BigInteger.valueOf(amount);

        boolean untrackedBlocks = this.blocksTracker.untrackBlock(key, amountBig);

        if (!untrackedBlocks)
            return BlockChangeResult.MISSING_BLOCK_VALUE;

        BigInteger newTotalBlocksCount = this.currentTotalBlockCounts.updateAndGet(count -> count.subtract(amountBig));

        BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();

        BlockValue blockValue = plugin.getBlockValues().getBlockValue(key);
        BigDecimal blockWorth = blockValue.getWorth();
        BigDecimal blockLevel = blockValue.getLevel();

        boolean saveBlockCounts = (flags & IslandBlockFlags.SAVE_BLOCK_COUNTS) != 0;
        boolean updateLastTimeStatus = (flags & IslandBlockFlags.UPDATE_LAST_TIME_STATUS) != 0;

        if (blockWorth.compareTo(BigDecimal.ZERO) != 0) {
            this.islandWorth.updateAndGet(islandWorth -> islandWorth.subtract(blockWorth.multiply(new BigDecimal(amount))));
            if (saveBlockCounts)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            this.islandLevel.updateAndGet(islandLevel -> islandLevel.subtract(blockLevel.multiply(new BigDecimal(amount))));
            if (saveBlockCounts)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
        }

        if (updateLastTimeStatus)
            updateLastTime();

        if (saveBlockCounts)
            saveBlockCounts(newTotalBlocksCount, oldWorth, oldLevel);

        return BlockChangeResult.SUCCESS;
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Block block, @Size int amount, boolean save) {
        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
        handleBlockBreak(block, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, @Size int amount, boolean save) {
        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;
        handleBlockBreak(key, amount, flags);
    }

    @Override
    @Deprecated
    public void handleBlockBreak(Key key, @Size BigInteger amount, boolean save) {
        Preconditions.checkNotNull(key, "key argument cannot be null");
        Preconditions.checkNotNull(amount, "amount argument cannot be null");

        int flags = IslandBlockFlags.UPDATE_LAST_TIME_STATUS;
        if (save) flags |= IslandBlockFlags.SAVE_BLOCK_COUNTS;

        BigInteger MAX_INT_VALUE = BigInteger.valueOf(Integer.MAX_VALUE);
        while (amount.compareTo(MAX_INT_VALUE) > 0) {
            handleBlockBreak(key, Integer.MAX_VALUE, flags);
            amount = amount.subtract(MAX_INT_VALUE);
        }

        handleBlockBreak(key, amount.intValueExact(), flags);
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks) {
        handleBlocksBreak(blocks,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks) {
        return handleBlocksBreakWithResult(blocks,
                IslandBlockFlags.SAVE_BLOCK_COUNTS | IslandBlockFlags.UPDATE_LAST_TIME_STATUS);
    }

    @Override
    public void handleBlocksBreak(Map<Key, Integer> blocks, @IslandBlockFlags int flags) {
        handleBlocksBreakWithResult(blocks, flags);
    }

    @Override
    public Map<Key, BlockChangeResult> handleBlocksBreakWithResult(Map<Key, Integer> blocks, int flags) {
        Preconditions.checkNotNull(blocks, "blocks parameter cannot be null.");

        if (blocks.isEmpty())
            return KeyMaps.createEmptyMap();

        BigDecimal oldWorth = getWorth();
        BigDecimal oldLevel = getIslandLevel();

        KeyMap<BlockChangeResult> result = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);

        blocks.forEach((blockKey, amount) -> {
            BlockChangeResult blockResult = handleBlockBreakWithResult(blockKey, amount, 0);
            if (blockResult != BlockChangeResult.SUCCESS)
                result.put(blockKey, blockResult);
        });

        boolean saveBlockCounts = (flags & IslandBlockFlags.SAVE_BLOCK_COUNTS) != 0;
        boolean updateLastTimeStatus = (flags & IslandBlockFlags.UPDATE_LAST_TIME_STATUS) != 0;

        if (saveBlockCounts)
            saveBlockCounts(this.currentTotalBlockCounts.get(), oldWorth, oldLevel);

        if (updateLastTimeStatus)
            updateLastTime();

        return result.isEmpty() ? KeyMaps.createEmptyMap() : KeyMaps.unmodifiableKeyMap(result);
    }

    @Override
    public boolean isChunkDirty(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        return this.isChunkDirty(world.getName(), chunkX, chunkZ);
    }

    @Override
    public boolean isChunkDirty(String worldName, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null.");
        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(this, worldName);
        Preconditions.checkArgument(worldInfo != null && isChunkInside(chunkX, chunkZ),
                "Chunk must be within the island boundaries.");
        return this.dirtyChunksContainer.isMarkedDirty(ChunkPosition.of(worldInfo, chunkX, chunkZ));
    }

    @Override
    public void markChunkDirty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        this.dirtyChunksContainer.markDirty(ChunkPosition.of(WorldInfo.of(world), chunkX, chunkZ), save);
    }

    @Override
    public void markChunkEmpty(World world, int chunkX, int chunkZ, boolean save) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        Preconditions.checkArgument(isInside(world, chunkX, chunkZ), "Chunk must be within the island boundaries.");
        this.dirtyChunksContainer.markEmpty(ChunkPosition.of(WorldInfo.of(world), chunkX, chunkZ), save);
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
        this.currentTotalBlockCounts.set(BigInteger.ZERO);

        islandWorth.set(BigDecimal.ZERO);
        islandLevel.set(BigDecimal.ZERO);

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
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

        Log.debug(Debug.SET_BONUS_WORTH, owner.getName(), bonusWorth);

        BigDecimal oldBonusWorth = this.bonusWorth.getAndSet(bonusWorth);

        if (Objects.equals(oldBonusWorth, bonusWorth))
            return;

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);

        IslandsDatabaseBridge.saveBonusWorth(this);
    }

    @Override
    public BigDecimal getBonusLevel() {
        return bonusLevel.get();
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        Preconditions.checkNotNull(bonusLevel, "bonusLevel parameter cannot be null.");

        Log.debug(Debug.SET_BONUS_LEVEL, owner.getName(), bonusLevel);

        BigDecimal oldBonusLevel = this.bonusLevel.getAndSet(bonusLevel);

        if (Objects.equals(oldBonusLevel, bonusLevel))
            return;

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
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

        Log.debug(Debug.SET_UPGRADE, owner.getName(), upgrade.getName(), level);

        int currentLevel = getUpgradeLevel(upgrade).getLevel();

        if (currentLevel == level)
            return;

        upgrades.put(upgrade.getName(), Math.min(upgrade.getMaxUpgradeLevel(), level));

        lastUpgradeTime = System.currentTimeMillis();

        IslandsDatabaseBridge.saveUpgrade(this, upgrade, level);

        UpgradeLevel upgradeLevel = getUpgradeLevel(upgrade);

        // Level was downgraded, we need to clear the values of that level and sync all upgrades again
        if (currentLevel > level) {
            syncUpgrades(false);
        } else {
            syncUpgrade((SUpgradeLevel) upgradeLevel, false);
        }

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
        return this.cropGrowth.readAndGet(DoubleValue::get);
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        cropGrowth = Math.max(1, cropGrowth);

        Log.debug(Debug.SET_CROP_GROWTH, owner.getName(), cropGrowth);

        if (cropGrowth == getCropGrowthRaw())
            return;

        DoubleValue oldCropGrowth = this.cropGrowth.set(DoubleValue.fixed(cropGrowth));

        if (cropGrowth == DoubleValue.getNonSynced(oldCropGrowth, -1D))
            return;

        IslandsDatabaseBridge.saveCropGrowth(this);

        notifyCropGrowthChange(cropGrowth);
    }

    @Override
    public double getCropGrowthRaw() {
        return this.cropGrowth.readAndGet(cropGrowth -> cropGrowth.getNonSynced(-1D));
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return this.spawnerRates.readAndGet(DoubleValue::get);
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        spawnerRates = Math.max(1, spawnerRates);

        Log.debug(Debug.SET_SPAWNER_RATES, owner.getName(), spawnerRates);

        DoubleValue oldSpawnerRates = this.spawnerRates.set(DoubleValue.fixed(spawnerRates));

        if (spawnerRates == DoubleValue.getNonSynced(oldSpawnerRates, -1D))
            return;

        IslandsDatabaseBridge.saveSpawnerRates(this);
    }

    @Override
    public double getSpawnerRatesRaw() {
        return this.spawnerRates.readAndGet(spawnerRates -> spawnerRates.getNonSynced(-1D));
    }

    @Override
    public double getMobDropsMultiplier() {
        return this.mobDrops.readAndGet(DoubleValue::get);
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        mobDrops = Math.max(1, mobDrops);

        Log.debug(Debug.SET_MOB_DROPS, owner.getName(), mobDrops);

        DoubleValue oldMobDrops = this.mobDrops.set(DoubleValue.fixed(mobDrops));

        if (mobDrops == DoubleValue.getNonSynced(oldMobDrops, -1D))
            return;

        IslandsDatabaseBridge.saveMobDrops(this);
    }

    @Override
    public double getMobDropsRaw() {
        return this.mobDrops.readAndGet(mobDrops -> mobDrops.getNonSynced(-1D));
    }

    @Override
    public int getBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        IntValue blockLimit = blockLimits.get(key);
        return blockLimit == null ? -1 : blockLimit.get();
    }

    @Override
    public int getExactBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        IntValue blockLimit = blockLimits.getRaw(key, null);
        return blockLimit == null ? -1 : blockLimit.get();
    }

    @Override
    public Key getBlockLimitKey(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return blockLimits.getKey(key, key);
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        KeyMap<Integer> blockLimits = KeyMap.createKeyMap();
        this.blockLimits.forEach((block, limit) -> blockLimits.put(block, limit.get()));
        return Collections.unmodifiableMap(blockLimits);
    }

    @Override
    public Map<Key, Integer> getCustomBlocksLimits() {
        return Collections.unmodifiableMap(this.blockLimits.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearBlockLimits() {
        Log.debug(Debug.CLEAR_BLOCK_LIMITS, owner.getName());

        if (blockLimits.isEmpty())
            return;

        blockLimits.clear();
        IslandsDatabaseBridge.clearBlockLimits(this);
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        int finalLimit = Math.max(0, limit);

        Log.debug(Debug.SET_BLOCK_LIMIT, owner.getName(), key, finalLimit);

        IntValue oldLimit = blockLimits.put(key, IntValue.fixed(finalLimit));

        if (limit == IntValue.getNonSynced(oldLimit, -1))
            return;

        plugin.getBlockValues().addCustomBlockKey(key);
        IslandsDatabaseBridge.saveBlockLimit(this, key, limit);
    }

    @Override
    public void removeBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.REMOVE_BLOCK_LIMIT, owner.getName(), key);

        IntValue oldBlockLimit = blockLimits.remove(key);

        if (oldBlockLimit == null)
            return;

        IslandsDatabaseBridge.removeBlockLimit(this, key);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return hasReachedBlockLimit(key, 1);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, @Size int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkArgument(amount >= 0, "amount parameter must be non-negative.");

        int blockLimit = getExactBlockLimit(key);

        //Checking for the specific provided key.
        if (blockLimit >= 0) {
            return getBlockCountAsBigInteger(key).add(BigInteger.valueOf(amount))
                    .compareTo(BigInteger.valueOf(blockLimit)) > 0;
        }

        //Getting the global key values.
        key = ((BaseKey<?>) key).toGlobalKey();
        blockLimit = getBlockLimit(key);

        return blockLimit >= 0 && getBlockCountAsBigInteger(key)
                .add(BigInteger.valueOf(amount)).compareTo(BigInteger.valueOf(blockLimit)) > 0;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return getEntityLimit(Keys.of(entityType));
    }

    @Override
    public int getEntityLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        IntValue entityLimit = entityLimits.get(key);
        return entityLimit == null ? -1 : entityLimit.get();
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
        Log.debug(Debug.CLEAR_ENTITY_LIMITS, owner.getName());

        if (entityLimits.isEmpty())
            return;

        entityLimits.clear();
        IslandsDatabaseBridge.clearEntityLimits(this);
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        setEntityLimit(Keys.of(entityType), limit);
    }

    @Override
    public void setEntityLimit(Key key, int limit) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        int finalLimit = Math.max(0, limit);

        Log.debug(Debug.SET_ENTITY_LIMIT, owner.getName(), key, finalLimit);

        IntValue oldEntityLimit = entityLimits.put(key, IntValue.fixed(limit));

        if (limit == IntValue.getNonSynced(oldEntityLimit, -1))
            return;

        IslandsDatabaseBridge.saveEntityLimit(this, key, limit);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return hasReachedEntityLimit(Keys.of(entityType));
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return hasReachedEntityLimit(key, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, @Size int amount) {
        Preconditions.checkNotNull(entityType, "entityType parameter cannot be null.");
        return hasReachedEntityLimit(Keys.of(entityType), amount);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(Key key, @Size int amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkArgument(amount >= 0, "amount parameter must be non-negative.");

        int entityLimit = getEntityLimit(key);

        if (entityLimit < 0)
            return CompletableFuture.completedFuture(false);

        return CompletableFuture.completedFuture(this.entitiesTracker.getEntityCount(key) + amount > entityLimit);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm getEntitiesTracker() {
        return this.entitiesTracker;
    }

    @Override
    public int getTeamLimit() {
        return this.teamLimit.readAndGet(IntValue::get);
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        teamLimit = Math.max(0, teamLimit);

        Log.debug(Debug.SET_TEAM_LIMIT, owner.getName(), teamLimit);

        IntValue oldTeamLimit = this.teamLimit.set(IntValue.fixed(teamLimit));

        if (teamLimit == IntValue.getNonSynced(oldTeamLimit, -1))
            return;

        IslandsDatabaseBridge.saveTeamLimit(this);
    }

    @Override
    public int getTeamLimitRaw() {
        return this.teamLimit.readAndGet(teamLimit -> teamLimit.getNonSynced(-1));
    }

    @Override
    public int getWarpsLimit() {
        return this.warpsLimit.readAndGet(IntValue::get);
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        warpsLimit = Math.max(0, warpsLimit);

        Log.debug(Debug.SET_WARPS_LIMIT, owner.getName(), warpsLimit);

        IntValue oldWarpsLimit = this.warpsLimit.set(IntValue.fixed(warpsLimit));

        if (warpsLimit == IntValue.getNonSynced(oldWarpsLimit, -1))
            return;

        IslandsDatabaseBridge.saveWarpsLimit(this);
    }

    @Override
    public int getWarpsLimitRaw() {
        return this.warpsLimit.readAndGet(warpsLimit -> warpsLimit.getNonSynced(-1));
    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {
        // Legacy support for levels can be set to <= 0 for removing the effect.
        // Nowadays, removePotionEffect exists.
        if (level <= 0) {
            removePotionEffect(type);
            return;
        }

        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");

        Log.debug(Debug.SET_ISLAND_EFFECT, owner.getName(), type.getName(), level);

        IntValue oldPotionLevel = islandEffects.put(type, IntValue.fixed(level));

        if (level == IntValue.getNonSynced(oldPotionLevel, -1))
            return;

        BukkitExecutor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            assert player != null;
            if (oldPotionLevel != null && oldPotionLevel.get() > level)
                player.removePotionEffect(type);

            PotionEffect potionEffect = new PotionEffect(type, Integer.MAX_VALUE, level - 1);
            player.addPotionEffect(potionEffect, true);
        }));

        IslandsDatabaseBridge.saveIslandEffect(this, type, level);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");

        Log.debug(Debug.REMOVE_ISLAND_EFFECT, owner.getName(), type.getName());

        IntValue oldEffectLevel = islandEffects.remove(type);

        if (oldEffectLevel == null)
            return;

        BukkitExecutor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            if (player != null)
                player.removePotionEffect(type);
        }));

        IslandsDatabaseBridge.removeIslandEffect(this, type);
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");
        IntValue islandEffect = islandEffects.get(type);
        return islandEffect == null ? -1 : islandEffect.get();
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        Map<PotionEffectType, Integer> islandEffects = new ArrayMap<>();

        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            if (potionEffectType != null) {
                int level = getPotionEffectLevel(potionEffectType);
                if (level > 0)
                    islandEffects.put(potionEffectType, level);
            }
        }

        return Collections.unmodifiableMap(islandEffects);
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class))
            return;

        applyEffectsNoUpgradeCheck(superiorPlayer);
    }

    @Override
    public void applyEffects() {
        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class))
            getAllPlayersInside().forEach(this::applyEffectsNoUpgradeCheck);
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

    @Override
    public void clearEffects() {
        Log.debug(Debug.CLEAR_ISLAND_EFFECTS, owner.getName());

        if (islandEffects.isEmpty())
            return;

        islandEffects.clear();
        removeEffects();

        IslandsDatabaseBridge.clearIslandEffects(this);
    }

    @Override
    public void setRoleLimit(PlayerRole playerRole, int limit) {
        // Legacy support for limits can be set to < 0 for removing the limit.
        // Nowadays, removeRoleLimit exists.
        if (limit < 0) {
            removeRoleLimit(playerRole);
            return;
        }

        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");

        Log.debug(Debug.SET_ROLE_LIMIT, owner.getName(), playerRole.getName(), limit);

        IntValue oldRoleLimit = roleLimits.put(playerRole, IntValue.fixed(limit));

        if (limit == IntValue.getNonSynced(oldRoleLimit, -1))
            return;

        IslandsDatabaseBridge.saveRoleLimit(this, playerRole, limit);
    }

    @Override
    public void removeRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");

        Log.debug(Debug.REMOVE_ROLE_LIMIT, owner.getName(), playerRole.getName());

        IntValue oldRoleLimit = roleLimits.remove(playerRole);

        if (oldRoleLimit == null)
            return;

        IslandsDatabaseBridge.removeRoleLimit(this, playerRole);
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        IntValue roleLimit = roleLimits.get(playerRole);
        return roleLimit == null ? -1 : roleLimit.get();
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        return IntValue.getNonSynced(roleLimits.get(playerRole), -1);
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

        Log.debug(Debug.CREATE_WARP_CATEGORY, owner.getName(), name);

        WarpCategory warpCategory = warpCategories.get(name.toLowerCase(Locale.ENGLISH));

        if (warpCategory == null) {
            Log.debugResult(Debug.CREATE_WARP_CATEGORY, "Result New Category", name);
            List<Integer> occupiedSlots = warpCategories.values().stream().map(WarpCategory::getSlot).collect(Collectors.toList());

            int slot = 0;
            while (occupiedSlots.contains(slot))
                ++slot;

            warpCategory = loadWarpCategory(name, slot, null);

            IslandsDatabaseBridge.saveWarpCategory(this, warpCategory);

            plugin.getMenus().refreshWarpCategories(this);
        } else {
            Log.debugResult(Debug.CREATE_WARP_CATEGORY, "Result Already Exists", name);
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

        Log.debug(Debug.DELETE_WARP_CATEGORY, owner.getName(), warpCategory.getName());

        boolean validCategoryRemoval = warpCategories.remove(warpCategory.getName().toLowerCase(Locale.ENGLISH)) != null;

        if (!validCategoryRemoval)
            return;

        IslandsDatabaseBridge.removeWarpCategory(this, warpCategory);

        boolean shouldSaveWarps = !warpCategory.getWarps().isEmpty();

        if (shouldSaveWarps) {
            new LinkedList<>(warpCategory.getWarps()).forEach(islandWarp -> deleteWarp(islandWarp.getName()));
            plugin.getMenus().destroyWarps(warpCategory);
        }

        plugin.getMenus().destroyWarpCategories(this);
    }

    @Override
    public Map<String, WarpCategory> getWarpCategories() {
        return Collections.unmodifiableMap(warpCategories);
    }

    @Override
    public IslandWarp createWarp(String name, Location location, @Nullable WarpCategory warpCategory) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        Preconditions.checkState(getWarp(name) == null, "Warp already exists: " + name);

        Log.debug(Debug.CREATE_WARP, owner.getName(), name, location, warpCategory);

        IslandWarp islandWarp = loadIslandWarp(name, LazyWorldLocation.of(location), warpCategory,
                !plugin.getSettings().isPublicWarps(), null);

        IslandsDatabaseBridge.saveWarp(this, islandWarp);

        plugin.getMenus().refreshGlobalWarps();
        plugin.getMenus().refreshWarps(islandWarp.getCategory());

        return islandWarp;
    }

    /*
     *  Warps related methods
     */

    @Override
    public void renameWarp(IslandWarp islandWarp, String newName) {
        Preconditions.checkNotNull(islandWarp, "islandWarp parameter cannot be null.");
        Preconditions.checkNotNull(newName, "newName parameter cannot be null.");
        Preconditions.checkArgument(IslandUtils.isWarpNameLengthValid(newName), "Warp names must cannot be longer than 255 chars.");
        Preconditions.checkState(getWarp(newName) == null, "Cannot rename warps to an already existing warps.");

        warpsByName.remove(islandWarp.getName().toLowerCase(Locale.ENGLISH));
        warpsByName.put(newName.toLowerCase(Locale.ENGLISH), islandWarp);
        islandWarp.setName(newName);
    }

    @Override
    public IslandWarp getWarp(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return warpsByLocation.get(new LocationKey(location));
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
            superiorPlayer.setTeleportTask(BukkitExecutor.sync(() ->
                    warpPlayerWithoutWarmup(superiorPlayer, islandWarp, true), plugin.getSettings().getWarpsWarmup() / 50));
        } else {
            warpPlayerWithoutWarmup(superiorPlayer, islandWarp, true);
        }
    }

    @Override
    public void deleteWarp(@Nullable SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        IslandWarp islandWarp = warpsByLocation.remove(new LocationKey(location));
        if (islandWarp != null) {
            deleteWarp(islandWarp.getName());
            if (superiorPlayer != null)
                Message.DELETE_WARP.send(superiorPlayer, islandWarp.getName());
        }
    }

    @Override
    public void deleteWarp(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        Log.debug(Debug.DELETE_WARP, owner.getName(), name);

        IslandWarp islandWarp = warpsByName.remove(name.toLowerCase(Locale.ENGLISH));
        WarpCategory warpCategory = islandWarp == null ? null : islandWarp.getCategory();

        if (islandWarp != null) {
            warpsByLocation.remove(new LocationKey(islandWarp.getLocation()));
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
        // Legacy support for rating can be set to UNKNOWN in order to remove rating.
        // Nowadays, removeRating exists.
        if (rating == Rating.UNKNOWN) {
            removeRating(superiorPlayer);
            return;
        }

        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(rating, "rating parameter cannot be null.");

        Log.debug(Debug.SET_RATING, owner.getName(), superiorPlayer.getName(), rating);

        Rating oldRating = ratings.put(superiorPlayer.getUniqueId(), rating);

        if (rating == oldRating)
            return;

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_RATING, this);

        IslandsDatabaseBridge.saveRating(this, superiorPlayer, rating, System.currentTimeMillis());

        plugin.getMenus().refreshIslandRatings(this);
    }

    @Override
    public void removeRating(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REMOVE_RATING, owner.getName(), superiorPlayer.getName());

        Rating oldRating = ratings.remove(superiorPlayer.getUniqueId());

        if (oldRating == null)
            return;

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_RATING, this);

        IslandsDatabaseBridge.removeRating(this, superiorPlayer);

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
        Log.debug(Debug.REMOVE_RATINGS, owner.getName());

        if (ratings.isEmpty())
            return;

        ratings.clear();

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_RATING, this);

        IslandsDatabaseBridge.clearRatings(this);

        plugin.getMenus().refreshIslandRatings(this);
    }

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

        Log.debug(Debug.ENABLE_ISLAND_FLAG, owner.getName(), settings.getName());

        Byte oldStatus = islandFlags.put(settings, (byte) 1);

        if (Objects.equals(oldStatus, 1))
            return;

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
            if (settings != IslandFlags.ALWAYS_DAY && islandFlags.remove(IslandFlags.ALWAYS_DAY) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_DAY);
            if (settings != IslandFlags.ALWAYS_MIDDLE_DAY && islandFlags.remove(IslandFlags.ALWAYS_MIDDLE_DAY) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_MIDDLE_DAY);
            if (settings != IslandFlags.ALWAYS_NIGHT && islandFlags.remove(IslandFlags.ALWAYS_NIGHT) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_NIGHT);
            if (settings != IslandFlags.ALWAYS_MIDDLE_NIGHT && islandFlags.remove(IslandFlags.ALWAYS_MIDDLE_NIGHT) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_MIDDLE_NIGHT);
        }

        if (disableWeather) {
            if (settings != IslandFlags.ALWAYS_RAIN && islandFlags.remove(IslandFlags.ALWAYS_RAIN) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_RAIN);
            if (settings != IslandFlags.ALWAYS_SHINY && islandFlags.remove(IslandFlags.ALWAYS_SHINY) != null)
                IslandsDatabaseBridge.removeIslandFlag(this, IslandFlags.ALWAYS_SHINY);
        }

        IslandsDatabaseBridge.saveIslandFlag(this, settings, 1);

        plugin.getMenus().refreshSettings(this);
    }

    /*
     *  Ratings related methods
     */

    @Override
    public void disableSettings(IslandFlag settings) {
        Preconditions.checkNotNull(settings, "settings parameter cannot be null.");

        Log.debug(Debug.DISABLE_ISLAND_FLAG, owner.getName(), settings.getName());

        Byte oldStatus = islandFlags.put(settings, (byte) 0);

        if (Objects.equals(oldStatus, 0))
            return;

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
    public void setGeneratorPercentage(Key key, int percentage, Dimension dimension) {
        setGeneratorPercentage(key, percentage, dimension, null, false);
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int percentage, Dimension dimension,
                                          @Nullable SuperiorPlayer caller, boolean callEvent) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Log.debug(Debug.SET_GENERATOR_PERCENTAGE, owner.getName(), key, percentage, dimension.getName(), caller, callEvent);

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.writeAndGet(cobbleGeneratorValues ->
                cobbleGeneratorValues.computeIfAbsent(dimension, e -> KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL)));

        Preconditions.checkArgument(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100 - got " + percentage + ".");

        if (percentage == 0) {
            if (callEvent && !plugin.getEventsBus().callIslandRemoveGeneratorRateEvent(
                    caller, this, key, dimension))
                return false;

            removeGeneratorAmount(key, dimension);
        } else if (percentage == 100) {
            KeyMap<IntValue> cobbleGeneratorValuesOriginal = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);
            cobbleGeneratorValuesOriginal.putAll(worldGeneratorRates);
            worldGeneratorRates.clear();

            int generatorRate = 1;

            if (callEvent) {
                EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(
                        caller, this, key, dimension, generatorRate);
                if (eventResult.isCancelled()) {
                    // Restore the original values
                    worldGeneratorRates.putAll(cobbleGeneratorValuesOriginal);
                    return false;
                }
                generatorRate = eventResult.getResult();
            }

            setGeneratorAmount(key, generatorRate, dimension);
        } else {
            //Removing the key from the generator
            removeGeneratorAmount(key, dimension);

            int totalAmount = getGeneratorTotalAmount(dimension);
            double realPercentage = percentage / 100D;
            double amount = (realPercentage * totalAmount) / (1 - realPercentage);
            if (amount < 1) {
                worldGeneratorRates.entrySet().forEach(entry -> {
                    int newAmount = entry.getValue().get() * 10;
                    if (entry.getValue().isSynced()) {
                        entry.setValue(IntValue.syncedFixed(newAmount));
                    } else {
                        entry.setValue(IntValue.fixed(newAmount));
                    }
                });
                amount *= 10;
            }

            EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(caller,
                    this, key, dimension, (int) Math.round(amount));

            if (eventResult.isCancelled())
                return false;

            setGeneratorAmount(key, eventResult.getResult(), dimension);
        }

        return true;
    }

    @Override
    @Deprecated
    public void setGeneratorPercentage(Key key, int percentage, World.Environment environment) {
        setGeneratorPercentage(key, percentage, Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public boolean setGeneratorPercentage(Key key, int percentage, World.Environment environment, SuperiorPlayer caller, boolean callEvent) {
        return setGeneratorPercentage(key, percentage, Dimensions.fromEnvironment(environment), caller, callEvent);
    }

    @Override
    public int getGeneratorPercentage(Key key, Dimension dimension) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        int totalAmount = getGeneratorTotalAmount(dimension);
        return totalAmount == 0 ? 0 : (getGeneratorAmount(key, dimension) * 100) / totalAmount;
    }

    @Override
    @Deprecated
    public int getGeneratorPercentage(Key key, World.Environment environment) {
        return getGeneratorPercentage(key, Dimensions.fromEnvironment(environment));
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        return getGeneratorAmounts(dimension).keySet().stream().collect(Collectors.toMap(key -> key,
                key -> getGeneratorAmount(Keys.ofMaterialAndData(key), dimension)));
    }

    @Override
    @Deprecated
    public Map<String, Integer> getGeneratorPercentages(World.Environment environment) {
        return getGeneratorPercentages(Dimensions.fromEnvironment(environment));
    }

    @Override
    public void setGeneratorAmount(Key key, @Size int amount, Dimension dimension) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        Preconditions.checkArgument(amount >= 0, "amount parameter must be non-negative.");

        Log.debug(Debug.SET_GENERATOR_RATE, owner.getName(), key, amount, dimension);

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.writeAndGet(cobbleGeneratorValues ->
                cobbleGeneratorValues.computeIfAbsent(dimension, e -> KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL)));

        IntValue oldGeneratorRate = worldGeneratorRates.put(key, IntValue.fixed(amount));

        if (amount == IntValue.getNonSynced(oldGeneratorRate, -1))
            return;

        IslandsDatabaseBridge.saveGeneratorRate(this, dimension, key, amount);
    }

    @Override
    public void setGeneratorAmount(Key key, int amount, World.Environment environment) {
        setGeneratorAmount(key, amount, Dimensions.fromEnvironment(environment));
    }

    @Override
    public void removeGeneratorAmount(Key key, Dimension dimension) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Log.debug(Debug.REMOVE_GENERATOR_RATE, owner.getName(), key, dimension);

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(dimension));

        if (worldGeneratorRates == null)
            return;

        IntValue oldGeneratorRate = worldGeneratorRates.get(key);

        if (oldGeneratorRate == null || oldGeneratorRate.get() <= 0)
            return;

        if (oldGeneratorRate.isSynced()) {
            // In case the old rate was upgrade-synced, we want to keep it in DB and cache as a 0 rate.
            IslandsDatabaseBridge.saveGeneratorRate(this, dimension, key, 0);
            worldGeneratorRates.put(key, IntValue.fixed(0));
        } else {
            IslandsDatabaseBridge.removeGeneratorRate(this, dimension, key);
            worldGeneratorRates.remove(key);
        }
    }

    @Override
    @Deprecated
    public void removeGeneratorAmount(Key key, World.Environment environment) {
        removeGeneratorAmount(key, Dimensions.fromEnvironment(environment));
    }

    @Override
    public int getGeneratorAmount(Key key, Dimension dimension) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(dimension));

        if (worldGeneratorRates == null)
            return 0;

        IntValue generatorRate = worldGeneratorRates.get(key);
        return generatorRate == null ? 0 : generatorRate.get();
    }

    @Override
    @Deprecated
    public int getGeneratorAmount(Key key, World.Environment environment) {
        return getGeneratorAmount(key, Dimensions.fromEnvironment(environment));
    }

    @Override
    public int getGeneratorTotalAmount(Dimension dimension) {
        int totalAmount = 0;
        for (int amt : getGeneratorAmounts(dimension).values())
            totalAmount += amt;
        return totalAmount;
    }

    @Override
    @Deprecated
    public int getGeneratorTotalAmount(World.Environment environment) {
        return getGeneratorTotalAmount(Dimensions.fromEnvironment(environment));
    }

    @Override
    public Map<String, Integer> getGeneratorAmounts(Dimension dimension) {
        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(dimension));

        if (worldGeneratorRates == null)
            return Collections.emptyMap();

        Map<String, Integer> generatorAmountsResult = new HashMap<>();

        worldGeneratorRates.forEach((blockKey, valueAmount) -> {
            int amount = valueAmount.get();
            if (amount > 0) {
                generatorAmountsResult.put(blockKey.toString(), amount);
            }
        });

        return generatorAmountsResult.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(generatorAmountsResult);
    }

    @Override
    @Deprecated
    public Map<String, Integer> getGeneratorAmounts(World.Environment environment) {
        return getGeneratorAmounts(Dimensions.fromEnvironment(environment));
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(dimension));

        if (worldGeneratorRates == null)
            return Collections.emptyMap();

        return Collections.unmodifiableMap(worldGeneratorRates.entrySet().stream()
                .filter(entry -> !entry.getValue().isSynced())
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    @Deprecated
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        return getCustomGeneratorAmounts(Dimensions.fromEnvironment(environment));
    }

    @Override
    public void clearGeneratorAmounts(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Log.debug(Debug.CLEAR_GENERATOR_RATES, owner.getName(), dimension.getName());

        KeyMap<IntValue> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(dimension));
        if (worldGeneratorRates != null && !worldGeneratorRates.isEmpty()) {
            worldGeneratorRates.clear();
            IslandsDatabaseBridge.clearGeneratorRates(this, dimension);
        }
    }

    @Override
    @Deprecated
    public void clearGeneratorAmounts(World.Environment environment) {
        clearGeneratorAmounts(Dimensions.fromEnvironment(environment));
    }

    @Nullable
    @Override
    public Key generateBlock(Location location, boolean optimizeCobblestone) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        Preconditions.checkArgument(isInside(location), "location must be inside island");
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(location.getWorld());
        return generateBlock(location, dimension, optimizeCobblestone);
    }

    @Override
    public Key generateBlock(Location location, Dimension dimension, boolean optimizeCobblestone) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        Preconditions.checkNotNull(dimension, "environment parameter cannot be null.");

        Log.debug(Debug.GENERATE_BLOCK, owner.getName(), location, dimension.getName(), optimizeCobblestone);

        int totalGeneratorAmounts = getGeneratorTotalAmount(dimension);

        if (totalGeneratorAmounts == 0) {
            Log.debugResult(Debug.GENERATE_BLOCK, "Return No Generator Rates", "null");
            return null;
        }

        Map<String, Integer> generatorAmounts = getGeneratorAmounts(dimension);

        Key newStateKey = ConstantKeys.COBBLESTONE;

        if (totalGeneratorAmounts == 1) {
            newStateKey = Keys.ofMaterialAndData(generatorAmounts.keySet().iterator().next());
        } else {
            int generatedIndex = ThreadLocalRandom.current().nextInt(totalGeneratorAmounts);
            int currentIndex = 0;
            for (Map.Entry<String, Integer> entry : generatorAmounts.entrySet()) {
                currentIndex += entry.getValue();
                if (generatedIndex < currentIndex) {
                    newStateKey = Keys.ofMaterialAndData(entry.getKey());
                    break;
                }
            }
        }

        EventResult<EventsBus.GenerateBlockResult> eventResult = plugin.getEventsBus().callIslandGenerateBlockEvent(
                this, location, newStateKey);

        if (eventResult.isCancelled()) {
            Log.debugResult(Debug.GENERATE_BLOCK, "Return Event Cancelled", "null");
            return null;
        }

        Key generatedBlock = eventResult.getResult().getBlock();

        if (optimizeCobblestone && generatedBlock.getGlobalKey().equals("COBBLESTONE")) {
            Log.debugResult(Debug.GENERATE_BLOCK, "Return Cobblestone", generatedBlock);
            return generatedBlock;
        }

        // If the block is a custom block, and the event was cancelled - we need to call the handleBlockPlace manually.
        handleBlockPlace(generatedBlock, 1);

        // Checking whether the plugin should set the block in the world.
        if (eventResult.getResult().isPlaceBlock()) {
            int combinedId;

            try {
                Material generateBlockType = Material.valueOf(generatedBlock.getGlobalKey());
                byte blockData = generatedBlock.getSubKey().isEmpty() ? 0 : Byte.parseByte(generatedBlock.getSubKey());
                combinedId = plugin.getNMSAlgorithms().getCombinedId(generateBlockType, blockData);
            } catch (IllegalArgumentException error) {
                Log.error("Invalid block for generating block: ", generatedBlock);
                combinedId = plugin.getNMSAlgorithms().getCombinedId(Material.COBBLESTONE, (byte) 0);
            }

            plugin.getNMSWorld().setBlock(location, combinedId);
        }

        plugin.getNMSWorld().playGeneratorSound(location);

        Log.debugResult(Debug.GENERATE_BLOCK, "Return", generatedBlock);

        return generatedBlock;
    }

    @Override
    @Deprecated
    public Key generateBlock(Location location, World.Environment environment, boolean optimizeCobblestone) {
        return generateBlock(location, Dimensions.fromEnvironment(environment), optimizeCobblestone);
    }

    @Override
    public boolean wasSchematicGenerated(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        return this.generatedSchematics.readAndGet(generatedSchematics -> generatedSchematics.contains(dimension));
    }

    @Override
    @Deprecated
    public boolean wasSchematicGenerated(World.Environment environment) {
        return wasSchematicGenerated(Dimensions.fromEnvironment(environment));
    }

    @Override
    public void setSchematicGenerate(Dimension dimension) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");
        setSchematicGenerate(dimension, true);
    }

    @Override
    public void setSchematicGenerate(Dimension dimension, boolean generated) {
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null.");

        Log.debug(Debug.SET_SCHEMATIC, dimension.getName(), generated);

        boolean updated = this.generatedSchematics.writeAndGet(generatedSchematics ->
                generated ? generatedSchematics.add(dimension) : generatedSchematics.remove(dimension));

        if (!updated)
            return;

        IslandsDatabaseBridge.saveGeneratedSchematics(this);
    }

    @Override
    @Deprecated
    public void setSchematicGenerate(World.Environment environment) {
        setSchematicGenerate(Dimensions.fromEnvironment(environment));
    }

    @Override
    @Deprecated
    public void setSchematicGenerate(World.Environment environment, boolean generated) {
        setSchematicGenerate(Dimensions.fromEnvironment(environment), generated);
    }

    @Override
    public Set<Dimension> getGeneratedSchematics() {
        return Collections.unmodifiableSet(this.generatedSchematics.readAndGet(generatedSchematics ->
                generatedSchematics.collect(Dimension.values())));
    }

    @Override
    @Deprecated
    public int getGeneratedSchematicsFlag() {
        return this.generatedSchematics.readAndGet(LegacyMasks::convertGeneratedSchematicsMask);
    }

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

    /*
     *  Generator related methods
     */

    @Override
    public int getChestSize() {
        return islandChests.readAndGet(islandChests -> islandChests.length);
    }

    @Override
    public void setChestRows(int index, int rows) {
        IslandChest[] islandChests = this.islandChests.get();
        int oldSize = islandChests.length;

        boolean updatedIslandChests = false;

        if (index >= oldSize) {
            updatedIslandChests = true;
            islandChests = Arrays.copyOf(islandChests, index + 1);
            this.islandChests.set(islandChests);
            for (int i = oldSize; i <= index; i++) {
                (islandChests[i] = new SIslandChest(this, i)).setRows(plugin.getSettings().getIslandChests().getDefaultSize());
            }
        }

        IslandChest islandChest = islandChests[index];

        if (!updatedIslandChests && islandChest.getRows() == rows)
            return;

        islandChests[index].setRows(rows);

        IslandsDatabaseBridge.markIslandChestsToBeSaved(this, islandChests[index]);
    }

    private void calcIslandWorthInternal(@Nullable SuperiorPlayer asker, @Nullable Runnable callback) {
        try {
            this.beingRecalculated = true;
            runCalcIslandWorthInternal(asker, callback);
        } catch (Throwable error) {
            // In case of an error, we get out of the recalculate state.
            this.beingRecalculated = false;
            throw error;
        }
    }

    private void runCalcIslandWorthInternal(@Nullable SuperiorPlayer asker, @Nullable Runnable callback) {
        Log.debug(Debug.CALCULATE_ISLAND, owner.getName(), asker);

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
            beingRecalculated = false;

            if (error != null) {
                if (error instanceof TimeoutException) {
                    if (asker != null)
                        Message.ISLAND_WORTH_TIME_OUT.send(asker);
                } else {
                    Log.entering(owner.getName(), asker);
                    Log.error(error, "An unexepcted error occurred while calculating island worth:");

                    if (asker != null)
                        Message.ISLAND_WORTH_ERROR.send(asker);
                }

                return;
            }

            clearBlockCounts();
            result.getBlockCounts().forEach((blockKey, amount) -> handleBlockPlaceInternal(blockKey, amount, 0));

            BigDecimal newIslandLevel = getIslandLevel();
            BigDecimal newIslandWorth = getWorth();

            finishCalcIsland(asker, callback, newIslandLevel, newIslandWorth);

            plugin.getMenus().refreshValues(this);
            plugin.getMenus().refreshCounts(this);

            saveBlockCounts(this.currentTotalBlockCounts.get(), oldWorth, oldLevel, true);
            updateLastTime();
        });
    }

    private boolean hasGiveInterestFailed() {
        return this.giveInterestFailed;
    }

    private void applyEffectsNoUpgradeCheck(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player != null) {
            getPotionEffects().forEach((potionEffectType, level) -> player.addPotionEffect(
                    new PotionEffect(potionEffectType, Integer.MAX_VALUE, level - 1), true));
        }
    }

    private void removeEffectsNoUpgradeCheck(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player != null)
            getPotionEffects().keySet().forEach(player::removePotionEffect);
    }

    private WarpCategory loadWarpCategory(String name, int slot, @Nullable ItemStack icon) {
        WarpCategory warpCategory = new SWarpCategory(getUniqueId(), name, slot, icon);
        warpCategories.put(name.toLowerCase(Locale.ENGLISH), warpCategory);
        return warpCategory;
    }

    public IslandWarp loadIslandWarp(String name, LazyWorldLocation location, @Nullable WarpCategory warpCategory,
                                     boolean isPrivate, @Nullable ItemStack icon) {
        if (warpCategory == null)
            warpCategory = warpCategories.values().stream().findFirst().orElseGet(() -> createWarpCategory("Default Category"));

        IslandWarp islandWarp = new SIslandWarp(name, location.clone(true), warpCategory, isPrivate, icon);

        islandWarp.getCategory().getWarps().add(islandWarp);

        String warpName = islandWarp.getName().toLowerCase(Locale.ENGLISH);

        if (warpsByName.containsKey(warpName))
            deleteWarp(warpName);

        warpsByName.put(warpName, islandWarp);

        warpsByLocation.put(new LocationKey(location), islandWarp);

        return islandWarp;
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        if (persistentDataContainer == null)
            persistentDataContainer = plugin.getFactory().createPersistentDataContainer(this);
        return persistentDataContainer;
    }

    @Override
    public boolean isPersistentDataContainerEmpty() {
        return persistentDataContainer == null || persistentDataContainer.isEmpty();
    }

    @Override
    public void savePersistentDataContainer() {
        IslandsDatabaseBridge.executeFutureSaves(this, IslandsDatabaseBridge.FutureSave.PERSISTENT_DATA);
    }

    /*
     *  Schematic methods
     */

    private void replaceVisitor(SuperiorPlayer originalPlayer, @Nullable SuperiorPlayer newPlayer) {
        uniqueVisitors.write(uniqueVisitors -> {
            Iterator<UniqueVisitor> iterator = uniqueVisitors.iterator();
            while (iterator.hasNext()) {
                UniqueVisitor uniqueVisitor = iterator.next();
                if (uniqueVisitor.getSuperiorPlayer().equals(originalPlayer)) {
                    Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Replace Visitor");
                    if (newPlayer == null) {
                        iterator.remove();
                    } else {
                        uniqueVisitor.setSuperiorPlayer(newPlayer);
                    }
                }
            }
        });
    }

    private void replaceBannedPlayer(SuperiorPlayer originalPlayer, @Nullable SuperiorPlayer newPlayer) {
        if (bannedPlayers.remove(originalPlayer)) {
            Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Replace Banned Player");
            if (newPlayer != null)
                bannedPlayers.add(newPlayer);
        }
    }

    private void replacePermissions(SuperiorPlayer originalPlayer, @Nullable SuperiorPlayer newPlayer) {
        PlayerPrivilegeNode playerPermissionNode = playerPermissions.remove(originalPlayer);
        if (playerPermissionNode != null) {
            Log.debugResult(Debug.REPLACE_PLAYER, "Action", "Replace Permissions");
            if (newPlayer != null)
                playerPermissions.put(newPlayer, playerPermissionNode);
        }
    }

    private void saveBlockCounts(BigInteger currentTotalBlocksCount, BigDecimal oldWorth, BigDecimal oldLevel) {
        saveBlockCounts(currentTotalBlocksCount, oldWorth, oldLevel, false);
    }

    private void saveBlockCounts(BigInteger currentTotalBlocksCount, BigDecimal oldWorth, BigDecimal oldLevel,
                                 boolean forceBlocksCountSave) {
        BigDecimal newWorth = getWorth();
        BigDecimal newLevel = getIslandLevel();

        if (oldLevel.compareTo(newLevel) != 0 || oldWorth.compareTo(newWorth) != 0) {
            BukkitExecutor.async(() -> plugin.getEventsBus().callIslandWorthUpdateEvent(this, oldWorth, oldLevel, newWorth, newLevel), 0L);
        }

        BigInteger deltaBlockCounts = this.lastSavedBlockCounts.subtract(currentTotalBlocksCount);
        if (deltaBlockCounts.compareTo(BigInteger.ZERO) < 0)
            deltaBlockCounts = deltaBlockCounts.negate();

        if (forceBlocksCountSave || deltaBlockCounts.compareTo(plugin.getSettings().getBlockCountsSaveThreshold()) >= 0) {
            this.lastSavedBlockCounts = currentTotalBlocksCount;
            IslandsDatabaseBridge.saveBlockCounts(this);
            plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
            plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
            plugin.getMenus().refreshValues(this);
            plugin.getMenus().refreshCounts(this);
        } else {
            IslandsDatabaseBridge.markBlockCountsToBeSaved(this);
        }
    }

    public void syncUpgrades(boolean overrideCustom) {
        clearUpgrades(overrideCustom);

        // We want to sync the default upgrade first, then the actual upgrades
        syncUpgrade(DefaultUpgradeLevel.getInstance(), overrideCustom);

        // Syncing all real upgrades
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> syncUpgrade((SUpgradeLevel) getUpgradeLevel(upgrade), overrideCustom));
    }

    /*
     *  Island top methods
     */

    private void warpPlayerWithoutWarmup(SuperiorPlayer superiorPlayer, IslandWarp islandWarp, boolean shouldRetryOnNullWorld) {
        // Warp doesn't exist anymore.
        if (getWarp(islandWarp.getName()) == null) {
            Message.INVALID_WARP.send(superiorPlayer, islandWarp.getName());
            deleteWarp(islandWarp.getName());
            return;
        }

        Location location = islandWarp.getLocation();
        if (location.getWorld() == null) {
            if (shouldRetryOnNullWorld && location instanceof LazyWorldLocation &&
                    plugin.getProviders().getWorldsProvider() instanceof LazyWorldsProvider) {
                LazyWorldsProvider worldsProvider = (LazyWorldsProvider) plugin.getProviders().getWorldsProvider();
                WorldInfo worldInfo = worldsProvider.getIslandsWorldInfo(this, ((LazyWorldLocation) location).getWorldName());
                worldsProvider.prepareWorld(this, worldInfo.getDimension(),
                        () -> warpPlayerWithoutWarmup(superiorPlayer, islandWarp, false));
                return;
            }
        }

        superiorPlayer.setTeleportTask(null);

        if (!isInsideRange(location)) {
            Message.UNSAFE_WARP.send(superiorPlayer);
            if (plugin.getSettings().getDeleteUnsafeWarps())
                deleteWarp(islandWarp.getName());
            return;
        }

        if (!WorldBlocks.isSafeBlock(location.getBlock())) {
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

    /*
     *  Vault related methods
     */

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkArgument(mission.getIslandMission(), "mission parameter must be island-mission.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.inc(1));
    }

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkArgument(mission.getIslandMission(), "mission parameter must be island-mission.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.inc(-1));
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return getAmountMissionCompleted(mission) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        if (!mission.getIslandMission())
            return false;

        Optional<MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) <
                missionDataOptional.get().getResetAmount();
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Counter finishCount = mission.getIslandMission() ? completedMissions.get(new MissionReference(mission)) : null;
        return finishCount == null ? 0 : finishCount.get();
    }

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkArgument(mission.getIslandMission(), "mission parameter must be island-mission.");
        this.changeAmountMissionsCompletedInternal(mission, counter -> counter.set(finishCount));
    }

    private void changeAmountMissionsCompletedInternal(Mission<?> mission, Function<Counter, Integer> action) {
        String missionName = mission.getName();

        MissionReference missionReference = new MissionReference(mission);

        Counter finishCount = completedMissions.computeIfAbsent(missionReference, r -> new Counter(0));
        int oldFinishCount = action.apply(finishCount);
        int newFinishCount = finishCount.get();

        Log.debug(Debug.SET_ISLAND_MISSION_COMPLETED, missionName, finishCount);

        // We always want to reset data
        mission.clearData(getOwner());

        if (newFinishCount > 0) {
            if (newFinishCount == oldFinishCount)
                return;

            IslandsDatabaseBridge.saveMission(this, mission, newFinishCount);
        } else {
            completedMissions.remove(missionReference);

            if (oldFinishCount <= 0)
                return;

            IslandsDatabaseBridge.removeMission(this, mission);
        }

        plugin.getMenus().refreshMissionsCategory(mission.getMissionCategory());
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new SequentialListBuilder<MissionReference>()
                .filter(MissionReference::isValid)
                .map(completedMissions.keySet(), MissionReference::getMission);
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissionsWithAmounts() {
        Map<Mission<?>, Integer> completedMissions = new LinkedHashMap<>();

        this.completedMissions.forEach((mission, finishCount) -> {
            if (mission.isValid())
                completedMissions.put(mission.getMission(), finishCount.get());
        });

        return completedMissions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(completedMissions);
    }

    /*
     *  Object related methods
     */

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    /*
     *  Private methods
     */

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Island && (this == obj || this.uuid.equals(((Island) obj).getUniqueId()));
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

    private IslandChest[] createDefaultIslandChests() {
        IslandChest[] islandChests = new IslandChest[plugin.getSettings().getIslandChests().getDefaultPages()];
        for (int i = 0; i < islandChests.length; i++) {
            if (islandChests[i] == null) {
                islandChests[i] = new SIslandChest(this, i);
                islandChests[i].setRows(plugin.getSettings().getIslandChests().getDefaultSize());
            }
        }
        return islandChests;
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
                    return BukkitExecutor.sync(() -> giveInterest(true), ticksToNextInterest);
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
        this.blockLimits.forEach((block, limit) -> {
            Integer defaultValue = plugin.getSettings().getDefaultValues().getBlockLimits().get(block);
            if (defaultValue != null && (int) limit.get() == defaultValue)
                this.blockLimits.put(block, IntValue.syncedFixed(defaultValue));
        });

        this.entityLimits.forEach((entity, limit) -> {
            Integer defaultValue = plugin.getSettings().getDefaultValues().getEntityLimits().get(entity);
            if (defaultValue != null && (int) limit.get() == defaultValue)
                this.entityLimits.put(entity, IntValue.syncedFixed(defaultValue));
        });

        this.cobbleGeneratorValues.write(cobbleGeneratorValues -> {
            for (Dimension dimension : Dimension.values()) {
                Map<Key, Integer> defaultGenerator = plugin.getSettings().getDefaultValues().getGenerators()[dimension.ordinal()];
                if (defaultGenerator != null) {
                    KeyMap<IntValue> worldGeneratorRates = cobbleGeneratorValues.get(dimension);

                    if (worldGeneratorRates == null)
                        continue;

                    worldGeneratorRates.forEach((key, rate) -> {
                        Integer defaultValue = defaultGenerator.get(key);
                        if (defaultValue != null && (int) rate.get() == defaultValue)
                            worldGeneratorRates.put(key, IntValue.syncedFixed(defaultValue));
                    });
                }
            }
        });

        if (getIslandSize() == plugin.getSettings().getDefaultValues().getIslandSize())
            this.islandSize.set(DefaultUpgradeLevel.getInstance().getBorderSizeUpgradeValue());

        if (getWarpsLimit() == plugin.getSettings().getDefaultValues().getWarpsLimit())
            this.warpsLimit.set(DefaultUpgradeLevel.getInstance().getWarpsLimitUpgradeValue());

        if (getTeamLimit() == plugin.getSettings().getDefaultValues().getTeamLimit())
            this.teamLimit.set(DefaultUpgradeLevel.getInstance().getTeamLimitUpgradeValue());

        if (getCoopLimit() == plugin.getSettings().getDefaultValues().getCoopLimit())
            this.coopLimit.set(DefaultUpgradeLevel.getInstance().getCoopLimitUpgradeValue());

        if (getCropGrowthMultiplier() == plugin.getSettings().getDefaultValues().getCropGrowth())
            this.cropGrowth.set(DefaultUpgradeLevel.getInstance().getCropGrowthUpgradeValue());

        if (getSpawnerRatesMultiplier() == plugin.getSettings().getDefaultValues().getSpawnerRates())
            this.spawnerRates.set(DefaultUpgradeLevel.getInstance().getSpawnerRatesUpgradeValue());

        if (getMobDropsMultiplier() == plugin.getSettings().getDefaultValues().getMobDrops())
            this.mobDrops.set(DefaultUpgradeLevel.getInstance().getMobDropsUpgradeValue());
    }

    private void clearUpgrades(boolean overrideCustom) {
        if (overrideCustom || this.islandSize.get().isSynced()) {
            if (overrideCustom)
                IslandsDatabaseBridge.saveSize(this);

            setIslandSizeInternal(IntValue.syncedFixed(-1));
        }

        warpsLimit.set(warpsLimit -> {
            if (overrideCustom || warpsLimit.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveWarpsLimit(this);
                return IntValue.syncedFixed(-1);
            }
            return warpsLimit;
        });

        teamLimit.set(teamLimit -> {
            if (overrideCustom || teamLimit.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveTeamLimit(this);
                return IntValue.syncedFixed(-1);
            }
            return teamLimit;
        });

        coopLimit.set(coopLimit -> {
            if (overrideCustom || coopLimit.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveCoopLimit(this);
                return IntValue.syncedFixed(-1);
            }
            return coopLimit;
        });

        cropGrowth.set(cropGrowth -> {
            if (overrideCustom || cropGrowth.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveCropGrowth(this);

                notifyCropGrowthChange(-1D);

                return DoubleValue.syncedFixed(-1D);
            }

            return cropGrowth;
        });

        spawnerRates.set(spawnerRates -> {
            if (overrideCustom || spawnerRates.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveSpawnerRates(this);
                return DoubleValue.syncedFixed(-1D);
            }
            return spawnerRates;
        });

        mobDrops.set(mobDrops -> {
            if (overrideCustom || mobDrops.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveMobDrops(this);
                return DoubleValue.syncedFixed(-1D);
            }
            return mobDrops;
        });

        bankLimit.set(bankLimit -> {
            if (overrideCustom || bankLimit.isSynced()) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveBankLimit(this);
                return Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE);
            }
            return bankLimit;
        });

        blockLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(IntValue.syncedFixed(-1)));

        entityLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(IntValue.syncedFixed(-1)));

        cobbleGeneratorValues.write(cobbleGeneratorValues -> {
            cobbleGeneratorValues.values().forEach(cobbleGeneratorValue -> {
                cobbleGeneratorValue.entrySet().stream()
                        .filter(entry -> overrideCustom || entry.getValue().isSynced())
                        .forEach(entry -> entry.setValue(IntValue.syncedFixed(-1)));
            });
        });

        islandEffects.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(IntValue.syncedFixed(-1)));

        roleLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue().isSynced())
                .forEach(entry -> entry.setValue(IntValue.syncedFixed(-1)));
    }

    private void syncUpgrade(SUpgradeLevel upgradeLevel, boolean overrideCustom) {
        cropGrowth.set(cropGrowth -> {
            if ((overrideCustom || cropGrowth.isSynced()) && cropGrowth.get() < upgradeLevel.getCropGrowth()) {
                notifyCropGrowthChange(upgradeLevel.getCropGrowth());
                return upgradeLevel.getCropGrowthUpgradeValue();
            }

            return cropGrowth;
        });

        spawnerRates.set(spawnerRates -> {
            if ((overrideCustom || spawnerRates.isSynced()) && spawnerRates.get() < upgradeLevel.getSpawnerRates())
                return upgradeLevel.getSpawnerRatesUpgradeValue();
            return spawnerRates;
        });

        mobDrops.set(mobDrops -> {
            if ((overrideCustom || mobDrops.isSynced()) && mobDrops.get() < upgradeLevel.getMobDrops())
                return upgradeLevel.getMobDropsUpgradeValue();
            return mobDrops;
        });

        teamLimit.set(teamLimit -> {
            if ((overrideCustom || teamLimit.isSynced()) && teamLimit.get() < upgradeLevel.getTeamLimit())
                return upgradeLevel.getTeamLimitUpgradeValue();
            return teamLimit;
        });

        warpsLimit.set(warpsLimit -> {
            if ((overrideCustom || warpsLimit.isSynced()) && warpsLimit.get() < upgradeLevel.getWarpsLimit())
                return upgradeLevel.getWarpsLimitUpgradeValue();
            return warpsLimit;
        });

        coopLimit.set(coopLimit -> {
            if ((overrideCustom || coopLimit.isSynced()) && coopLimit.get() < upgradeLevel.getCoopLimit())
                return upgradeLevel.getCoopLimitUpgradeValue();
            return coopLimit;
        });

        IntValue islandSize = this.islandSize.get();
        if ((overrideCustom || islandSize.isSynced()) && islandSize.get() < upgradeLevel.getBorderSize())
            setIslandSizeInternal(upgradeLevel.getBorderSizeUpgradeValue());

        bankLimit.set(bankLimit -> {
            if ((overrideCustom || bankLimit.isSynced()) && bankLimit.get().compareTo(upgradeLevel.getBankLimit()) < 0)
                return upgradeLevel.getBankLimitUpgradeValue();
            return bankLimit;
        });

        for (Map.Entry<Key, IntValue> entry : upgradeLevel.getBlockLimitsUpgradeValue().entrySet()) {
            IntValue currentValue = blockLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || ((overrideCustom || currentValue.isSynced()) && currentValue.get() < entry.getValue().get()))
                blockLimits.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Key, IntValue> entry : upgradeLevel.getEntityLimitsUpgradeValue().entrySet()) {
            IntValue currentValue = entityLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || ((overrideCustom || currentValue.isSynced()) && currentValue.get() < entry.getValue().get()))
                entityLimits.put(entry.getKey(), entry.getValue());
        }

        EnumerateMap<Dimension, Map<Key, IntValue>> upgradeGeneratorRates = upgradeLevel.getGeneratorUpgradeValue();
        if (!upgradeGeneratorRates.isEmpty()) {
            this.cobbleGeneratorValues.write(cobbleGeneratorValues -> {
                for (Dimension dimension : Dimension.values()) {
                    Map<Key, IntValue> upgradeLevelGeneratorRates = upgradeGeneratorRates.get(dimension);

                    if (upgradeLevelGeneratorRates == null)
                        continue;

                    KeyMap<IntValue> worldGeneratorRates = cobbleGeneratorValues.get(dimension);

                    if (worldGeneratorRates != null && !upgradeLevelGeneratorRates.isEmpty()) {
                        KeyMap<IntValue> worldGeneratorRatesCopy = worldGeneratorRates;
                        worldGeneratorRatesCopy.removeIf(key -> worldGeneratorRatesCopy.get(key).isSynced());
                    }

                    for (Map.Entry<Key, IntValue> entry : upgradeLevelGeneratorRates.entrySet()) {
                        Key block = entry.getKey();
                        IntValue rate = entry.getValue();

                        IntValue currentValue = worldGeneratorRates == null ? null : worldGeneratorRates.get(block);
                        if (currentValue == null || ((overrideCustom || currentValue.isSynced()) &&
                                currentValue.get() < rate.get())) {
                            if (worldGeneratorRates == null) {
                                worldGeneratorRates = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);
                                cobbleGeneratorValues.put(dimension, worldGeneratorRates);
                            }

                            worldGeneratorRates.put(block, rate);
                        }
                    }
                }
            });
        }

        boolean editedIslandEffects = false;

        for (Map.Entry<PotionEffectType, IntValue> entry : upgradeLevel.getPotionEffectsUpgradeValue().entrySet()) {
            IntValue currentValue = islandEffects.get(entry.getKey());
            if (currentValue == null || ((overrideCustom || currentValue.isSynced()) && currentValue.get() < entry.getValue().get())) {
                islandEffects.put(entry.getKey(), entry.getValue());
                editedIslandEffects = true;
            }
        }

        if (editedIslandEffects) {
            applyEffects();
        }

        for (Map.Entry<PlayerRole, IntValue> entry : upgradeLevel.getRoleLimitsUpgradeValue().entrySet()) {
            IntValue currentValue = roleLimits.get(entry.getKey());
            if (currentValue == null || ((overrideCustom || currentValue.isSynced()) && currentValue.get() < entry.getValue().get()))
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

    private void forEachIslandMember(List<UUID> ignoredMembers, boolean onlyOnline, Consumer<SuperiorPlayer> islandMemberConsumer) {
        for (SuperiorPlayer islandMember : getIslandMembers(true)) {
            if (!ignoredMembers.contains(islandMember.getUniqueId()) && (!onlyOnline || islandMember.isOnline())) {
                islandMemberConsumer.accept(islandMember);
            }
        }
    }

    private void notifyCropGrowthChange(double newCropGrowth) {
        if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class))
            return;

        double newCropGrowthMultiplier = newCropGrowth - 1;
        IslandUtils.getChunkCoords(this, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS)
                .values().forEach(chunkPositions -> plugin.getNMSChunks().updateCropsTicker(chunkPositions, newCropGrowthMultiplier));
    }

    public static class UniqueVisitor {

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
        public int hashCode() {
            return Objects.hash(superiorPlayer, lastVisitTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UniqueVisitor that = (UniqueVisitor) o;
            return lastVisitTime == that.lastVisitTime && superiorPlayer.equals(that.superiorPlayer);
        }

    }

}
