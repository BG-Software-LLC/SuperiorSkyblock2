package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
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
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.IslandArea;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.island.builder.IslandBuilderImpl;
import com.bgsoftware.superiorskyblock.island.container.value.SyncedValue;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
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
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeIslandEffects;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SIsland implements Island {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static int blocksUpdateCounter = 0;

    private final DatabaseBridge databaseBridge;
    private final IslandBank islandBank;
    private final IslandCalculationAlgorithm calculationAlgorithm;
    private final IslandBlocksTrackerAlgorithm blocksTracker;
    private final IslandEntitiesTrackerAlgorithm entitiesTracker;
    private final Synchronized<BukkitTask> bankInterestTask = Synchronized.of(null);
    @Nullable
    private PersistentDataContainer persistentDataContainer;

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
    @Nullable
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
    private final Synchronized<Value<Integer>> islandSize = Synchronized.of(Value.syncedFixed(-1));
    private final Synchronized<Value<Integer>> warpsLimit = Synchronized.of(Value.syncedFixed(-1));
    private final Synchronized<Value<Integer>> teamLimit = Synchronized.of(Value.syncedFixed(-1));
    private final Synchronized<Value<Integer>> coopLimit = Synchronized.of(Value.syncedFixed(-1));
    private final Synchronized<Value<Double>> cropGrowth = Synchronized.of(Value.syncedFixed(-1D));
    private final Synchronized<Value<Double>> spawnerRates = Synchronized.of(Value.syncedFixed(-1D));
    private final Synchronized<Value<Double>> mobDrops = Synchronized.of(Value.syncedFixed(-1D));
    private final Synchronized<Value<BigDecimal>> bankLimit = Synchronized.of(Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE));
    private final Map<PlayerRole, Value<Integer>> roleLimits = new ConcurrentHashMap<>();
    private final Synchronized<EnumMap<World.Environment, KeyMap<Value<Integer>>>> cobbleGeneratorValues = Synchronized.of(new EnumMap<>(World.Environment.class));
    private final Map<PotionEffectType, Value<Integer>> islandEffects = new ConcurrentHashMap<>();
    private final KeyMap<Value<Integer>> blockLimits = KeyMapImpl.createConcurrentHashMap();
    private final KeyMap<Value<Integer>> entityLimits = KeyMapImpl.createConcurrentHashMap();

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
    private final Synchronized<EnumMap<World.Environment, Location>> islandHomes = Synchronized.of(new EnumMap<>(World.Environment.class));
    private final Synchronized<EnumMap<World.Environment, Location>> visitorHomes = Synchronized.of(new EnumMap<>(World.Environment.class));
    private final Map<IslandPrivilege, PlayerRole> rolePermissions = new ConcurrentHashMap<>();
    private final Map<IslandFlag, Byte> islandFlags = new ConcurrentHashMap<>();
    private final Map<String, Integer> upgrades = new ConcurrentHashMap<>();
    private final AtomicReference<BigDecimal> islandWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> islandLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusWorth = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicReference<BigDecimal> bonusLevel = new AtomicReference<>(BigDecimal.ZERO);
    private final Map<Mission<?>, Integer> completedMissions = new ConcurrentHashMap<>();
    private final Synchronized<IslandChest[]> islandChests = Synchronized.of(new IslandChest[plugin.getSettings().getIslandChests().getDefaultPages()]);
    private volatile String discord;
    private volatile String paypal;
    private volatile boolean isLocked;
    private volatile boolean isTopIslandsIgnored;
    private volatile String islandName;
    private volatile String islandRawName;
    private volatile String description;
    private volatile Biome biome = null;
    private final Synchronized<CompletableFuture<Biome>> biomeGetterTask = Synchronized.of(null);
    private final AtomicInteger generatedSchematics = new AtomicInteger(0);
    private final AtomicInteger unlockedWorlds = new AtomicInteger(0);

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
        this.generatedSchematics.set(builder.generatedSchematicsMask);
        this.unlockedWorlds.set(builder.unlockedWorldsMask);
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

        // We make sure the default world is always marked as generated.
        if (!wasSchematicGenerated(plugin.getSettings().getWorlds().getDefaultWorld())) {
            setSchematicGenerate(plugin.getSettings().getWorlds().getDefaultWorld());
        }

        builder.dirtyChunks.forEach(chunkPosition -> ChunksTracker.markDirty(this, chunkPosition, false));
        if (!builder.blockCounts.isEmpty()) {
            BukkitExecutor.sync(() -> builder.blockCounts.forEach((block, count) ->
                    handleBlockPlace(block, count, false, false)
            ), 20L);
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

        this.islandBank.setBalance(builder.balance);
        builder.bankTransactions.forEach(this.islandBank::loadTransaction);
        if (builder.persistentData.length > 0)
            getPersistentDataContainer().load(builder.persistentData);

        this.databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
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

        Log.debug(Debug.INVITE_MEMBER, "SIsland", "inviteMember", owner.getName(), superiorPlayer.getName());

        invitedPlayers.add(superiorPlayer);
        superiorPlayer.addInvite(this);

        //Revoke the invite after 5 minutes
        BukkitExecutor.sync(() -> revokeInvite(superiorPlayer), 6000L);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REVOKE_INVITE, "SIsland", "revokeInvite", owner.getName(), superiorPlayer.getName());

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

        Log.debug(Debug.ADD_MEMBER, "SIsland", "addMember", owner.getName(), superiorPlayer.getName(), playerRole);

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

        Log.debug(Debug.KICK_MEMBER, "SIsland", "kickMember", owner.getName(), superiorPlayer.getName());

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
    public void banMember(SuperiorPlayer superiorPlayer, @Nullable SuperiorPlayer whom) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.BAN_PLAYER, "SIsland", "banMember", owner.getName(), superiorPlayer.getName(), whom);

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

        Log.debug(Debug.UNBAN_PLAYER, "SIsland", "unbanMember", owner.getName(), superiorPlayer.getName());

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

        Log.debug(Debug.ADD_COOP, "SIsland", "addCoop", owner.getName(), superiorPlayer.getName());

        boolean coopPlayer = coopPlayers.add(superiorPlayer);

        if (coopPlayer)
            plugin.getMenus().refreshCoops(this);
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REMOVE_COOP, "SIsland", "removeCoop", owner.getName(), superiorPlayer.getName());

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
        return plugin.getSettings().isCoopMembers() && coopPlayers.contains(superiorPlayer);
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return new SequentialListBuilder<SuperiorPlayer>().build(this.coopPlayers);
    }

    @Override
    public int getCoopLimit() {
        return this.coopLimit.readAndGet(coopLimit -> coopLimit.get());
    }

    @Override
    public int getCoopLimitRaw() {
        return this.coopLimit.readAndGet(coopLimit -> coopLimit instanceof SyncedValue ? -1 : coopLimit.get());
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        coopLimit = Math.max(0, coopLimit);

        Log.debug(Debug.SET_COOP_LIMIT, "SIsland", "setCoopLimit", owner.getName(), coopLimit);

        // Original and new coop limit are the same
        if (coopLimit == getCoopLimitRaw())
            return;

        this.coopLimit.set(Value.fixed(coopLimit));
        IslandsDatabaseBridge.saveCoopLimit(this);
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if (inside) {
            Log.debug(Debug.ENTER_ISLAND, "SIsland", "setPlayerInside", owner.getName(), superiorPlayer.getName());
        } else {
            Log.debug(Debug.LEAVE_ISLAND, "SIsland", "setPlayerInside", owner.getName(), superiorPlayer.getName());
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

        Location teleportLocation = islandHomes.readAndGet(teleportLocations -> teleportLocations.get(environment));

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
        return islandHomes.readAndGet(islandHomes -> Collections.unmodifiableMap(islandHomes));
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

        Log.debug(Debug.SET_ISLAND_HOME, "SIsland", "setIslandHome", owner.getName(), environment, homeLocation);

        islandHomes.write(islandHomes ->
                islandHomes.put(environment, homeLocation == null ? null : homeLocation.clone()));

        IslandsDatabaseBridge.saveIslandHome(this, environment, homeLocation);
    }

    @Override
    public Location getVisitorsLocation() {
        return getVisitorsLocation(null /* unused */);
    }

    @Nullable
    @Override
    public Location getVisitorsLocation(World.Environment unused) {
        Location visitorsLocation = this.visitorHomes.readAndGet(visitorsLocations ->
                visitorsLocations.get(plugin.getSettings().getWorlds().getDefaultWorld()));

        if (visitorsLocation == null)
            return null;

        if (adjustLocationToCenterOfBlock(visitorsLocation))
            IslandsDatabaseBridge.saveVisitorLocation(this, plugin.getSettings().getWorlds().getDefaultWorld(), visitorsLocation);

        World world = plugin.getGrid().getIslandsWorld(this, plugin.getSettings().getWorlds().getDefaultWorld());
        visitorsLocation.setWorld(world);

        return visitorsLocation.clone();
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        Log.debug(Debug.SET_VISITOR_HOME, "SIsland", "setVisitorsLocation", owner.getName(), visitorsLocation);

        if (visitorsLocation == null) {
            this.visitorHomes.write(visitorsLocations ->
                    visitorsLocations.remove(plugin.getSettings().getWorlds().getDefaultWorld()));

            IslandsDatabaseBridge.removeVisitorLocation(this, plugin.getSettings().getWorlds().getDefaultWorld());
        } else {
            adjustLocationToCenterOfBlock(visitorsLocation);

            this.visitorHomes.write(visitorsLocations ->
                    visitorsLocations.put(plugin.getSettings().getWorlds().getDefaultWorld(), visitorsLocation.clone()));

            IslandsDatabaseBridge.saveVisitorLocation(this, plugin.getSettings().getWorlds().getDefaultWorld(), visitorsLocation);
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
        List<Chunk> chunks = new LinkedList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getAllChunks(environment, onlyProtected));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
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
        return new SequentialListBuilder<Chunk>()
                .build(IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks), ChunkPosition::loadChunk);
    }

    @Override
    public List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks) {
        List<Chunk> chunks = new LinkedList<>();

        for (World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getLoadedChunks(environment, onlyProtected, noEmptyChunks));
            } catch (NullPointerException ignored) {
            }
        }

        return Collections.unmodifiableList(chunks);
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = getCenter(environment).getWorld();
        return new SequentialListBuilder<Chunk>()
                .filter(Objects::nonNull)
                .build(IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks), plugin.getNMSChunks()::getChunkIfLoaded);
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
        LinkedList<List<ChunkPosition>> worldsChunks = new LinkedList<>(
                IslandUtils.getChunkCoords(this, onlyProtected, true).values());


        if (worldsChunks.isEmpty()) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        for (List<ChunkPosition> chunkPositions : worldsChunks)
            IslandUtils.deleteChunks(this, chunkPositions, chunkPositions == worldsChunks.getLast() ? onFinish : null);
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
        Log.debug(Debug.SET_NORMAL_ENABLED, "SIsland", "setNormalEnabled", owner.getName(), enabled);

        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 4 : unlockedWorlds & 3);

        IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public boolean isNetherEnabled() {
        return plugin.getProviders().getWorldsProvider().isNetherUnlocked() || (unlockedWorlds.get() & 1) == 1;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        Log.debug(Debug.SET_NETHER_ENABLED, "SIsland", "setNetherEnabled", owner.getName(), enabled);

        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 1 : unlockedWorlds & 6);

        IslandsDatabaseBridge.saveUnlockedWorlds(this);
    }

    @Override
    public boolean isEndEnabled() {
        return plugin.getProviders().getWorldsProvider().isEndUnlocked() || (unlockedWorlds.get() & 2) == 2;
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        Log.debug(Debug.SET_END_ENABLED, "SIsland", "setEndEnabled", owner.getName(), enabled);

        this.unlockedWorlds.updateAndGet(unlockedWorlds -> enabled ? unlockedWorlds | 2 : unlockedWorlds & 5);

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

        Log.debug(Debug.SET_PERMISSION, "SIsland", "setPermission", owner.getName(), playerRole, islandPrivilege);

        rolePermissions.put(islandPrivilege, playerRole);

        if (islandPrivilege == IslandPrivileges.FLY) {
            getAllPlayersInside().forEach(this::updateIslandFly);
        } else if (islandPrivilege == IslandPrivileges.VILLAGER_TRADING) {
            getAllPlayersInside().forEach(superiorPlayer -> IslandUtils.updateTradingMenus(this, superiorPlayer));
        }

        IslandsDatabaseBridge.saveRolePermission(this, playerRole, islandPrivilege);
    }

    @Override
    public void resetPermissions() {
        Log.debug(Debug.RESET_PERMISSIONS, "SIsland", "resetPermissions", owner.getName());

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

        Log.debug(Debug.SET_PERMISSION, "SIsland", "setPermission", owner.getName(),
                superiorPlayer.getName(), islandPrivilege, value);

        if (!playerPermissions.containsKey(superiorPlayer))
            playerPermissions.put(superiorPlayer, new PlayerPrivilegeNode(superiorPlayer, this));

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

        Log.debug(Debug.RESET_PERMISSIONS, "SIsland", "resetPermissions", owner.getName(), superiorPlayer.getName());

        playerPermissions.remove(superiorPlayer);

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

        Log.debug(Debug.SET_NAME, "SIsland", "setName", owner.getName(), islandName);

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

        Log.debug(Debug.SET_DESCRIPTION, "SIsland", "setDescription", owner.getName(), description);

        this.description = description;

        IslandsDatabaseBridge.saveDescription(this);
    }

    @Override
    public void disbandIsland() {
        forEachIslandMember(Collections.emptyList(), false, islandMember -> {
            if (islandMember.equals(owner)) {
                owner.setIsland(null);
            } else {
                kickMember(islandMember);
            }

            if (plugin.getSettings().isDisbandInventoryClear())
                plugin.getNMSPlayers().clearInventory(islandMember.asOfflinePlayer());

            for (Mission<?> mission : plugin.getMissions().getAllMissions()) {
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

        Log.debug(Debug.TRANSFER_ISLAND, "SIsland", "transferIsland", owner.getName(), superiorPlayer.getName());

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
        Log.debug(Debug.CALCULATE_ISLAND, "SIsland", "calcIslandWorth", owner.getName(), asker);

        long lastUpdateTime = getLastTimeUpdate();

        if (lastUpdateTime != -1 && (System.currentTimeMillis() / 1000) - lastUpdateTime >= 600) {
            Log.debugResult(Debug.CALCULATE_ISLAND, "SIsland", "calcIslandWorth", "Result Cooldown", owner.getName());
            finishCalcIsland(asker, callback, getIslandLevel(), getWorth());
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            calcIslandWorthInternal(asker, callback);
        } else {
            BukkitExecutor.sync(() -> calcIslandWorthInternal(asker, callback));
        }
    }

    private void calcIslandWorthInternal(@Nullable SuperiorPlayer asker, @Nullable Runnable callback) {
        Log.debug(Debug.CALCULATE_ISLAND, "SIsland", "calcIslandWorthInternal", owner.getName(), asker);

        beingRecalculated = true;

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
                    Log.entering("SIsland", "calcIslandWorthInternal", owner.getName(), asker);
                    Log.error(error, "An unexepcted error occurred while calculating island worth:");

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
        Log.debug(Debug.UPDATE_BORDER, "SIsland", "updateBorder", owner.getName());
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

        return this.islandSize.readAndGet(islandSize -> islandSize.get());
    }

    @Override
    public void setIslandSize(int islandSize) {
        islandSize = Math.max(1, islandSize);

        Log.debug(Debug.SET_SIZE, "SIsland", "setIslandSize", owner.getName(), islandSize);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);

        if (cropGrowthEnabled) {
            // First, we want to remove all the current crop tile entities
            getLoadedChunks(true, false).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, true));
        }

        this.islandSize.set(Value.fixed(islandSize));

        if (cropGrowthEnabled) {
            // Now, we want to update the tile entities again
            getLoadedChunks(true, false).forEach(chunk ->
                    plugin.getNMSChunks().startTickingChunk(this, chunk, false));
        }

        IslandsDatabaseBridge.saveSize(this);
    }

    @Override
    public int getIslandSizeRaw() {
        return this.islandSize.readAndGet(islandSize -> islandSize instanceof SyncedValue ? -1 : islandSize.get());
    }

    @Override
    public String getDiscord() {
        return discord;
    }

    @Override
    public void setDiscord(String discord) {
        Preconditions.checkNotNull(discord, "discord parameter cannot be null.");

        Log.debug(Debug.SET_DISCORD, "SIsland", "setDiscord", owner.getName(), discord);

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

        Log.debug(Debug.SET_PAYPAL, "SIsland", "setPaypal", owner.getName(), paypal);

        this.paypal = paypal;
        IslandsDatabaseBridge.savePaypal(this);
    }

    @Override
    public Biome getBiome() {
        if (biome == null) {
            biomeGetterTask.set(task -> {
                if (task != null)
                    return task;

                Location centerBlock = getCenter(plugin.getSettings().getWorlds().getDefaultWorld());

                ChunkPosition centerChunkPosition = ChunkPosition.of(centerBlock);

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

            return IslandUtils.getDefaultWorldBiome(plugin.getSettings().getWorlds().getDefaultWorld());
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

        Log.debug(Debug.SET_BIOME, "SIsland", "setBiome", owner.getName(), biome, updateBlocks);

        this.biome = biome;

        if (!updateBlocks)
            return;

        List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                .build(getAllPlayersInside(), SuperiorPlayer::asPlayer);

        {
            World normalWorld = getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getWorld();
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, normalWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, biome, playersToUpdate);
        }

        if (plugin.getProviders().getWorldsProvider().isNetherEnabled() && wasSchematicGenerated(World.Environment.NETHER)) {
            World netherWorld = getCenter(World.Environment.NETHER).getWorld();
            Biome netherBiome = IslandUtils.getDefaultWorldBiome(World.Environment.NETHER);
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(this, netherWorld, false, false);
            plugin.getNMSChunks().setBiome(chunkPositions, netherBiome, playersToUpdate);
        }

        if (plugin.getProviders().getWorldsProvider().isEndEnabled() && wasSchematicGenerated(World.Environment.THE_END)) {
            World endWorld = getCenter(World.Environment.THE_END).getWorld();
            Biome endBiome = IslandUtils.getDefaultWorldBiome(World.Environment.THE_END);
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
        Log.debug(Debug.SET_LOCKED, "SIsland", "setLocked", owner.getName(), locked);

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
        Log.debug(Debug.SET_IGNORED, "SIsland", "setIgnored", owner.getName(), ignored);

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

        Log.debug(Debug.SEND_MESSAGE, "SIsland", "sendMessage", owner.getName(), message, ignoredList);

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

        Log.debug(Debug.SEND_MESSAGE, "SIsland", "sendMessage", owner.getName(),
                messageComponent.getMessage(), ignoredMembers, Arrays.asList(args));

        forEachIslandMember(ignoredMembers, false, islandMember -> messageComponent.sendMessage(islandMember.asPlayer(), args));
    }

    @Override
    public void sendTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int duration,
                          int fadeOut, UUID... ignoredMembers) {
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = ignoredMembers.length == 0 ? Collections.emptyList() : Arrays.asList(ignoredMembers);

        Log.debug(Debug.SEND_TITLE, "SIsland", "sendTitle", owner.getName(),
                title, subtitle, fadeIn, duration, fadeOut, ignoredList);

        forEachIslandMember(ignoredList, true, islandMember ->
                plugin.getNMSPlayers().sendTitle(islandMember.asPlayer(), title, subtitle, fadeIn, duration, fadeOut)
        );
    }

    @Override
    public void executeCommand(String command, boolean onlyOnlineMembers, UUID... ignoredMembers) {
        Preconditions.checkNotNull(command, "command parameter cannot be null.");
        Preconditions.checkNotNull(ignoredMembers, "ignoredMembers parameter cannot be null.");

        List<UUID> ignoredList = ignoredMembers.length == 0 ? Collections.emptyList() : Arrays.asList(ignoredMembers);

        Log.debug(Debug.EXECUTE_ISLAND_COMMANDS, "SIsland", "executeCommand", owner.getName(), command, onlyOnlineMembers, ignoredList);

        forEachIslandMember(ignoredList, true, islandMember ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player-name}", islandMember.getName()))
        );
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
        Log.debug(Debug.SET_ISLAND_LAST_TIME_UPDATED, "SIsland", "setLastTimeUpdate",
                owner.getName(), lastTimeUpdate);

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
        return this.bankLimit.readAndGet(bankLimit -> bankLimit.get());
    }

    @Override
    public void setBankLimit(BigDecimal bankLimit) {
        Preconditions.checkNotNull(bankLimit, "bankLimit parameter cannot be null.");

        Log.debug(Debug.SET_BANK_LIMIT, "SIsland", "setBankLimit", owner.getName(), bankLimit);

        if (bankLimit.compareTo(SYNCED_BANK_LIMIT_VALUE) <= 0) {
            this.bankLimit.set(() -> SYNCED_BANK_LIMIT_VALUE);

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

    /*
     *  Bank related methods
     */

    @Override
    public BigDecimal getBankLimitRaw() {
        return this.bankLimit.readAndGet(bankLimit -> bankLimit instanceof SyncedValue ? SYNCED_BANK_LIMIT_VALUE : bankLimit.get());
    }

    @Override
    public boolean giveInterest(boolean checkOnlineOwner) {
        Log.debug(Debug.GIVE_BANK_INTEREST, "SIsland", "giveInterest", owner.getName());

        long currentTime = System.currentTimeMillis() / 1000;

        if (checkOnlineOwner && BuiltinModules.BANK.bankInterestRecentActive > 0 &&
                currentTime - owner.getLastTimeStatus() > BuiltinModules.BANK.bankInterestRecentActive) {
            Log.debugResult(Debug.GIVE_BANK_INTEREST, "SIsland", "giveInterest", "Return Cooldown", owner.getName());
            return false;
        }

        BigDecimal balance = islandBank.getBalance().max(BigDecimal.ONE);
        BigDecimal balanceToGive = balance.multiply(new BigDecimal(BuiltinModules.BANK.bankInterestPercentage / 100D));

        // If the money that will be given exceeds limit, we want to give money later.
        if (!islandBank.canDepositMoney(balanceToGive)) {
            Log.debugResult(Debug.GIVE_BANK_INTEREST, "SIsland", "giveInterest",
                    "Return Cannot Deposit Money", owner.getName());
            giveInterestFailed = true;
            return false;
        }

        Log.debugResult(Debug.GIVE_BANK_INTEREST, "SIsland", "giveInterest",
                "Return Success", owner.getName());

        giveInterestFailed = false;

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
            if (save)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            islandLevel.updateAndGet(islandLevel -> islandLevel.add(blockLevel.multiply(new BigDecimal(amount))));
            if (save)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
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
            if (save)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_WORTH, this);
        }

        if (blockLevel.compareTo(BigDecimal.ZERO) != 0) {
            this.islandLevel.updateAndGet(islandLevel -> islandLevel.subtract(blockLevel.multiply(new BigDecimal(amount))));
            if (save)
                plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_LEVEL, this);
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

        Log.debug(Debug.SET_BONUS_WORTH, "SIsland", "setBonusWorth", owner.getName(), bonusWorth);

        this.bonusWorth.set(bonusWorth);

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

        Log.debug(Debug.SET_BONUS_LEVEL, "SIsland", "setBonusWorth", owner.getName(), bonusLevel);

        this.bonusLevel.set(bonusLevel);

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

        Log.debug(Debug.SET_UPGRADE, "SIsland", "setUpgradeLevel", owner.getName(), upgrade.getName(), level);

        int currentLevel = getUpgradeLevel(upgrade).getLevel();

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
        return this.cropGrowth.readAndGet(cropGrowth -> cropGrowth.get());
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        cropGrowth = Math.max(1, cropGrowth);

        Log.debug(Debug.SET_CROP_GROWTH, "SIsland", "setCropGrowthMultiplier", owner.getName(), cropGrowth);

        this.cropGrowth.set(Value.fixed(cropGrowth));

        IslandsDatabaseBridge.saveCropGrowth(this);

        // Update the crop growth ticking
        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class)) {
            World world = plugin.getProviders().getWorldsProvider().getIslandsWorld(this, World.Environment.NORMAL);
            plugin.getNMSChunks().updateCropsTicker(
                    IslandUtils.getChunkCoords(this, world, true, true),
                    cropGrowth - 1);
        }
    }

    @Override
    public double getCropGrowthRaw() {
        return this.cropGrowth.readAndGet(cropGrowth -> cropGrowth instanceof SyncedValue ? -1D : cropGrowth.get());
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return this.spawnerRates.readAndGet(spawnerRates -> spawnerRates.get());
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        spawnerRates = Math.max(1, spawnerRates);

        Log.debug(Debug.SET_SPAWNER_RATES, "SIsland", "setSpawnerRatesMultiplier", owner.getName(), spawnerRates);

        this.spawnerRates.set(Value.fixed(spawnerRates));
        IslandsDatabaseBridge.saveSpawnerRates(this);
    }

    @Override
    public double getSpawnerRatesRaw() {
        return this.spawnerRates.readAndGet(spawnerRates -> spawnerRates instanceof SyncedValue ? -1D : spawnerRates.get());
    }

    @Override
    public double getMobDropsMultiplier() {
        return this.mobDrops.readAndGet(mobDrops -> mobDrops.get());
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        mobDrops = Math.max(1, mobDrops);

        Log.debug(Debug.SET_MOB_DROPS, "SIsland", "setMobDropsMultiplier", owner.getName(), mobDrops);

        this.mobDrops.set(Value.fixed(mobDrops));
        IslandsDatabaseBridge.saveMobDrops(this);
    }

    @Override
    public double getMobDropsRaw() {
        return this.mobDrops.readAndGet(mobDrops -> mobDrops instanceof SyncedValue ? -1D : mobDrops.get());
    }

    @Override
    public int getBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Value<Integer> blockLimit = blockLimits.get(key);
        return blockLimit == null ? -1 : blockLimit.get();
    }

    @Override
    public int getExactBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Value<Integer> blockLimit = blockLimits.getRaw(key, null);
        return blockLimit == null ? -1 : blockLimit.get();
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
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearBlockLimits() {
        Log.debug(Debug.CLEAR_BLOCK_LIMITS, "SIsland", "clearBlockLimits", owner.getName());
        blockLimits.clear();
        IslandsDatabaseBridge.clearBlockLimits(this);
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        int finalLimit = Math.max(0, limit);

        Log.debug(Debug.SET_BLOCK_LIMIT, "SIsland", "setBlockLimit", owner.getName(), key, finalLimit);

        blockLimits.put(key, Value.fixed(finalLimit));
        plugin.getBlockValues().addCustomBlockKey(key);
        IslandsDatabaseBridge.saveBlockLimit(this, key, limit);
    }

    @Override
    public void removeBlockLimit(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Log.debug(Debug.REMOVE_BLOCK_LIMIT, "SIsland", "removeBlockLimit", owner.getName(), key);

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
        if (blockLimit >= 0) {
            return getBlockCountAsBigInteger(key).add(BigInteger.valueOf(amount))
                    .compareTo(BigInteger.valueOf(blockLimit)) > 0;
        }

        //Getting the global key values.
        key = KeyImpl.of(key.getGlobalKey(), "");
        blockLimit = getBlockLimit(key);

        return blockLimit >= 0 && getBlockCountAsBigInteger(key)
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
        Value<Integer> entityLimit = entityLimits.get(key);
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
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearEntitiesLimits() {
        Log.debug(Debug.CLEAR_ENTITY_LIMITS, "SIsland", "clearEntitiesLimits", owner.getName());
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

        Log.debug(Debug.SET_ENTITY_LIMIT, "SIsland", "setEntityLimit", owner.getName(), key, finalLimit);

        entityLimits.put(key, Value.fixed(limit));
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
        return this.teamLimit.readAndGet(teamLimit -> teamLimit.get());
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        teamLimit = Math.max(0, teamLimit);

        Log.debug(Debug.SET_TEAM_LIMIT, "SIsland", "setTeamLimit", owner.getName(), teamLimit);

        this.teamLimit.set(Value.fixed(teamLimit));
        IslandsDatabaseBridge.saveTeamLimit(this);
    }

    @Override
    public int getTeamLimitRaw() {
        return this.teamLimit.readAndGet(teamLimit -> teamLimit instanceof SyncedValue ? -1 : teamLimit.get());
    }

    @Override
    public int getWarpsLimit() {
        return this.warpsLimit.readAndGet(warpsLimit -> warpsLimit.get());
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        warpsLimit = Math.max(0, warpsLimit);

        Log.debug(Debug.SET_WARPS_LIMIT, "SIsland", "setWarpsLimit", owner.getName(), warpsLimit);

        this.warpsLimit.set(Value.fixed(warpsLimit));
        IslandsDatabaseBridge.saveWarpsLimit(this);
    }

    @Override
    public int getWarpsLimitRaw() {
        return this.warpsLimit.readAndGet(warpsLimit -> warpsLimit instanceof SyncedValue ? -1 : warpsLimit.get());
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

        Log.debug(Debug.SET_ISLAND_EFFECT, "SIsland", "setPotionEffect", owner.getName(), type.getName(), level);

        PotionEffect potionEffect = new PotionEffect(type, Integer.MAX_VALUE, level - 1);
        Value<Integer> oldPotionLevel = islandEffects.put(type, Value.fixed(level));

        BukkitExecutor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> {
            Player player = superiorPlayer.asPlayer();
            assert player != null;
            if (oldPotionLevel != null && oldPotionLevel.get() > level)
                player.removePotionEffect(type);
            player.addPotionEffect(potionEffect, true);
        }));

        IslandsDatabaseBridge.saveIslandEffect(this, type, level);
    }

    @Override
    public void removePotionEffect(PotionEffectType type) {
        Preconditions.checkNotNull(type, "potionEffectType parameter cannot be null.");

        Log.debug(Debug.REMOVE_ISLAND_EFFECT, "SIsland", "removePotionEffect", owner.getName(), type.getName());

        islandEffects.remove(type);

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
        Value<Integer> islandEffect = islandEffects.get(type);
        return islandEffect == null ? -1 : islandEffect.get();
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
        Log.debug(Debug.CLEAR_ISLAND_EFFECTS, "SIsland", "clearEffects", owner.getName());
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

        Log.debug(Debug.SET_ROLE_LIMIT, "SIsland", "setRoleLimit", owner.getName(), playerRole.getName(), limit);

        roleLimits.put(playerRole, Value.fixed(limit));

        IslandsDatabaseBridge.saveRoleLimit(this, playerRole, limit);
    }

    @Override
    public void removeRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");

        Log.debug(Debug.REMOVE_ROLE_LIMIT, "SIsland", "removeRoleLimit", owner.getName(), playerRole.getName());

        roleLimits.remove(playerRole);

        IslandsDatabaseBridge.removeRoleLimit(this, playerRole);
    }

    @Override
    public int getRoleLimit(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        Value<Integer> roleLimit = roleLimits.get(playerRole);
        return roleLimit == null ? -1 : roleLimit.get();
    }

    @Override
    public int getRoleLimitRaw(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        Value<Integer> roleLimit = roleLimits.get(playerRole);
        return roleLimit == null || roleLimit instanceof SyncedValue ? -1 : roleLimit.get();
    }

    @Override
    public Map<PlayerRole, Integer> getRoleLimits() {
        return roleLimits.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    @Override
    public Map<PlayerRole, Integer> getCustomRoleLimits() {
        return this.roleLimits.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    @Override
    public WarpCategory createWarpCategory(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        Log.debug(Debug.CREATE_WARP_CATEGORY, "SIsland", "createWarpCategory", owner.getName(), name);

        WarpCategory warpCategory = warpCategories.get(name.toLowerCase(Locale.ENGLISH));

        if (warpCategory == null) {
            Log.debugResult(Debug.CREATE_WARP_CATEGORY, "SIsland", "createWarpCategory",
                    "Result New Category", name);
            List<Integer> occupiedSlots = warpCategories.values().stream().map(WarpCategory::getSlot).collect(Collectors.toList());

            int slot = 0;
            while (occupiedSlots.contains(slot))
                ++slot;

            warpCategory = loadWarpCategory(name, slot, null);

            IslandsDatabaseBridge.saveWarpCategory(this, warpCategory);

            plugin.getMenus().refreshWarpCategories(this);
        } else {
            Log.debugResult(Debug.CREATE_WARP_CATEGORY, "SIsland", "createWarpCategory",
                    "Result Already Exists", name);
        }

        return warpCategory;
    }

    private WarpCategory loadWarpCategory(String name, int slot, @Nullable ItemStack icon) {
        WarpCategory warpCategory = new SWarpCategory(getUniqueId(), name, slot, icon);
        warpCategories.put(name.toLowerCase(Locale.ENGLISH), warpCategory);
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

        Log.debug(Debug.DELETE_WARP_CATEGORY, "SIsland", "deleteCategory", owner.getName(), warpCategory.getName());

        boolean validWarpRemoval = warpCategories.remove(warpCategory.getName().toLowerCase(Locale.ENGLISH)) != null;

        if (validWarpRemoval) {
            IslandsDatabaseBridge.removeWarpCategory(this, warpCategory);
            boolean shouldSaveWarps = !warpCategory.getWarps().isEmpty();
            if (shouldSaveWarps) {
                new LinkedList<>(warpCategory.getWarps()).forEach(islandWarp -> deleteWarp(islandWarp.getName()));
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
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");

        Log.debug(Debug.CREATE_WARP, "SIsland", "createWarp", owner.getName(), name, location, warpCategory);

        IslandWarp islandWarp = loadIslandWarp(name, location, warpCategory, !plugin.getSettings().isPublicWarps(), null);

        IslandsDatabaseBridge.saveWarp(this, islandWarp);

        plugin.getMenus().refreshGlobalWarps();
        plugin.getMenus().refreshWarps(islandWarp.getCategory());

        return islandWarp;
    }

    public IslandWarp loadIslandWarp(String name, Location location, @Nullable WarpCategory warpCategory,
                                     boolean isPrivate, @Nullable ItemStack icon) {
        if (warpCategory == null)
            warpCategory = warpCategories.values().stream().findFirst().orElseGet(() -> createWarpCategory("Default Category"));

        IslandWarp islandWarp = new SIslandWarp(name, location.clone(), warpCategory, isPrivate, icon);

        islandWarp.getCategory().getWarps().add(islandWarp);

        String warpName = islandWarp.getName().toLowerCase(Locale.ENGLISH);

        if (warpsByName.containsKey(warpName))
            deleteWarp(warpName);

        warpsByName.put(warpName, islandWarp);

        warpsByLocation.put(new LocationKey(location), islandWarp);

        return islandWarp;
    }

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
            superiorPlayer.setTeleportTask(BukkitExecutor.sync(() ->
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

        Log.debug(Debug.DELETE_WARP, "SIsland", "deleteWarp", owner.getName(), name);

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
        // Legacy support for rating can be set to UNKNOWN in order to remove rating.
        // Nowadays, removeRating exists.
        if (rating == Rating.UNKNOWN) {
            removeRating(superiorPlayer);
            return;
        }

        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(rating, "rating parameter cannot be null.");

        Log.debug(Debug.SET_RATING, "SIsland", "setRating", owner.getName(), superiorPlayer.getName(), rating);

        ratings.put(superiorPlayer.getUniqueId(), rating);

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_RATING, this);

        IslandsDatabaseBridge.saveRating(this, superiorPlayer, rating, System.currentTimeMillis());

        plugin.getMenus().refreshIslandRatings(this);
    }

    @Override
    public void removeRating(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        Log.debug(Debug.REMOVE_RATING, "SIsland", "removeRating", owner.getName(), superiorPlayer.getName());

        ratings.remove(superiorPlayer.getUniqueId());

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
        Log.debug(Debug.REMOVE_RATINGS, "SIsland", "removeRatings", owner.getName());

        ratings.clear();

        plugin.getGrid().getIslandsContainer().notifyChange(SortingTypes.BY_RATING, this);

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

        Log.debug(Debug.ENABLE_ISLAND_FLAG, "SIsland", "enableSettings", owner.getName(), settings.getName());

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

        Log.debug(Debug.DISABLE_ISLAND_FLAG, "SIsland", "disableSettings", owner.getName(), settings.getName());

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
        setGeneratorPercentage(key, percentage, environment, null, false);
    }

    @Override
    public boolean setGeneratorPercentage(Key key, int percentage, World.Environment environment,
                                          @Nullable SuperiorPlayer caller, boolean callEvent) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        Log.debug(Debug.SET_GENERATOR_PERCENTAGE, "SIsland", "setGeneratorPercentage", owner.getName(),
                key, percentage, environment, caller, callEvent);

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.writeAndGet(cobbleGeneratorValues ->
                cobbleGeneratorValues.computeIfAbsent(environment, e -> KeyMapImpl.createConcurrentHashMap()));

        Preconditions.checkArgument(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100 - got " + percentage + ".");

        if (percentage == 0) {
            if (callEvent && !plugin.getEventsBus().callIslandRemoveGeneratorRateEvent(caller, this, key, environment))
                return false;

            removeGeneratorAmount(key, environment);
        } else if (percentage == 100) {
            KeyMap<Value<Integer>> cobbleGeneratorValuesOriginal = KeyMapImpl.createConcurrentHashMap(worldGeneratorRates);
            worldGeneratorRates.clear();

            int generatorRate = 1;

            if (callEvent) {
                EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(caller, this, key, environment, generatorRate);
                if (eventResult.isCancelled()) {
                    // Restore the original values
                    worldGeneratorRates.putAll(cobbleGeneratorValuesOriginal);
                    return false;
                }
                generatorRate = eventResult.getResult();
            }

            setGeneratorAmount(key, generatorRate, environment);
        } else {
            //Removing the key from the generator
            removeGeneratorAmount(key, environment);

            int totalAmount = getGeneratorTotalAmount(environment);
            double realPercentage = percentage / 100D;
            double amount = (realPercentage * totalAmount) / (1 - realPercentage);
            if (amount < 1) {
                worldGeneratorRates.entrySet().forEach(entry -> {
                    int newAmount = entry.getValue().get() * 10;
                    if (entry.getValue() instanceof SyncedValue) {
                        entry.setValue(Value.syncedFixed(newAmount));
                    } else {
                        entry.setValue(Value.fixed(newAmount));
                    }
                });
                amount *= 10;
            }

            EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(caller,
                    this, key, environment, (int) Math.round(amount));

            if (eventResult.isCancelled())
                return false;

            setGeneratorAmount(key, eventResult.getResult(), environment);
        }

        return true;
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

        amount = Math.max(0, amount);

        Log.debug(Debug.SET_GENERATOR_RATE, "SIsland", "setGeneratorAmount", owner.getName(),
                key, amount, environment);

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.writeAndGet(cobbleGeneratorValues ->
                cobbleGeneratorValues.computeIfAbsent(environment, e -> KeyMapImpl.createConcurrentHashMap()));

        worldGeneratorRates.put(key, Value.fixed(amount));

        IslandsDatabaseBridge.saveGeneratorRate(this, environment, key, amount);
    }

    @Override
    public void removeGeneratorAmount(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(environment));

        if (worldGeneratorRates == null)
            return;

        Log.debug(Debug.REMOVE_GENERATOR_RATE, "SIsland", "removeGeneratorAmount", owner.getName(), key, environment);

        worldGeneratorRates.remove(key);

        IslandsDatabaseBridge.removeGeneratorRate(this, environment, key);
    }

    @Override
    public int getGeneratorAmount(Key key, World.Environment environment) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(environment));

        if (worldGeneratorRates == null)
            return 0;

        Value<Integer> generatorRate = worldGeneratorRates.get(key);
        return generatorRate == null ? 0 : generatorRate.get();
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
        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(environment));

        if (worldGeneratorRates == null)
            return Collections.emptyMap();

        return Collections.unmodifiableMap(worldGeneratorRates.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                entry -> entry.getValue().get())));
    }

    @Override
    public Map<Key, Integer> getCustomGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(environment));

        if (worldGeneratorRates == null)
            return Collections.emptyMap();

        return Collections.unmodifiableMap(worldGeneratorRates.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof SyncedValue))
                .collect(KeyMap.getCollector(Map.Entry::getKey, entry -> entry.getValue().get())));
    }

    @Override
    public void clearGeneratorAmounts(World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");

        Log.debug(Debug.CLEAR_GENERATOR_RATES, "SIsland", "clearGeneratorAmounts", owner.getName(), environment);

        KeyMap<Value<Integer>> worldGeneratorRates = this.cobbleGeneratorValues.readAndGet(
                cobbleGeneratorValues -> cobbleGeneratorValues.get(environment));
        if (worldGeneratorRates != null) {
            worldGeneratorRates.clear();
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

        Log.debug(Debug.GENERATE_BLOCK, "SIsland", "generateBlock", owner.getName(), location,
                environment, optimizeCobblestone);

        int totalGeneratorAmounts = getGeneratorTotalAmount(environment);

        if (totalGeneratorAmounts == 0) {
            Log.debugResult(Debug.GENERATE_BLOCK, "SIsland", "generateBlock",
                    "Return No Generator Rates", "null");
            return null;
        }

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

        EventResult<EventsBus.GenerateBlockResult> eventResult = plugin.getEventsBus().callIslandGenerateBlockEvent(
                this, location, KeyImpl.of(newState));

        if (eventResult.isCancelled()) {
            Log.debugResult(Debug.GENERATE_BLOCK, "SIsland", "generateBlock",
                    "Return Event Cancelled", "null");
            return null;
        }

        Key generatedBlock = eventResult.getResult().getBlock();

        if (optimizeCobblestone && generatedBlock.getGlobalKey().contains("COBBLESTONE")) {
            Log.debugResult(Debug.GENERATE_BLOCK, "SIsland", "generateBlock",
                    "Return Cobblestone", generatedBlock);
            /* Block is being counted in BlocksListener#onBlockFromToMonitor */
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

        Log.debugResult(Debug.GENERATE_BLOCK, "SIsland", "generateBlock",
                "Return", generatedBlock);

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

        Log.debug(Debug.SET_SCHEMATIC, "SIsland", "setSchematicGenerate", environment, generated);

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
        PlayerPrivilegeNode playerPermissionNode = playerPermissions.remove(originalPlayer);
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
            BukkitExecutor.async(() -> plugin.getEventsBus().callIslandWorthUpdateEvent(this, oldWorth, oldLevel, newWorth, newLevel), 0L);
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
        clearUpgrades(overrideCustom);

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

    @Override
    public void completeMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        setAmountMissionCompleted(mission, completedMissions.getOrDefault(mission, 0) + 1);
    }

    /*
     *  Island top methods
     */

    @Override
    public void resetMission(Mission<?> mission) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        setAmountMissionCompleted(mission, completedMissions.getOrDefault(mission, 0) - 1);
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

    @Override
    public void setAmountMissionCompleted(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        Log.debug(Debug.SET_ISLAND_MISSION_COMPLETED, "SIsland", "setAmountMissionCompleted", mission.getName(), finishCount);

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
     *  Data related methods
     */

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new SequentialListBuilder<Mission<?>>().build(completedMissions.keySet());
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
        return this.uuid.hashCode();
    }

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
                this.blockLimits.put(block, Value.syncedFixed(defaultValue));
        });

        this.entityLimits.forEach((entity, limit) -> {
            Integer defaultValue = plugin.getSettings().getDefaultValues().getEntityLimits().get(entity);
            if (defaultValue != null && (int) limit.get() == defaultValue)
                this.entityLimits.put(entity, Value.syncedFixed(defaultValue));
        });

        this.cobbleGeneratorValues.write(cobbleGeneratorValues -> {
            for (World.Environment environment : World.Environment.values()) {
                Map<Key, Integer> defaultGenerator = plugin.getSettings().getDefaultValues().getGenerators()[environment.ordinal()];
                if (defaultGenerator != null) {
                    KeyMap<Value<Integer>> worldGeneratorRates = cobbleGeneratorValues.get(environment);

                    if (worldGeneratorRates == null)
                        continue;

                    worldGeneratorRates.forEach((key, rate) -> {
                        Integer defaultValue = defaultGenerator.get(key);
                        if (defaultValue != null && (int) rate.get() == defaultValue)
                            worldGeneratorRates.put(key, Value.syncedFixed(defaultValue));
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
        islandSize.set(islandSize -> {
            if (overrideCustom || islandSize instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveSize(this);
                return Value.syncedFixed(-1);
            }

            return islandSize;
        });

        warpsLimit.set(warpsLimit -> {
            if (overrideCustom || warpsLimit instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveWarpsLimit(this);
                return Value.syncedFixed(-1);
            }
            return warpsLimit;
        });

        teamLimit.set(teamLimit -> {
            if (overrideCustom || teamLimit instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveTeamLimit(this);
                return Value.syncedFixed(-1);
            }
            return teamLimit;
        });

        coopLimit.set(coopLimit -> {
            if (overrideCustom || coopLimit instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveCoopLimit(this);
                return Value.syncedFixed(-1);
            }
            return coopLimit;
        });

        cropGrowth.set(cropGrowth -> {
            if (overrideCustom || cropGrowth instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveCropGrowth(this);
                return Value.syncedFixed(-1D);
            }
            return cropGrowth;
        });

        spawnerRates.set(spawnerRates -> {
            if (overrideCustom || spawnerRates instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveSpawnerRates(this);
                return Value.syncedFixed(-1D);
            }
            return spawnerRates;
        });

        mobDrops.set(mobDrops -> {
            if (overrideCustom || mobDrops instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveMobDrops(this);
                return Value.syncedFixed(-1D);
            }
            return mobDrops;
        });

        bankLimit.set(bankLimit -> {
            if (overrideCustom || bankLimit instanceof SyncedValue) {
                if (overrideCustom)
                    IslandsDatabaseBridge.saveBankLimit(this);
                return Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE);
            }
            return bankLimit;
        });

        blockLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue() instanceof SyncedValue)
                .forEach(entry -> entry.setValue(Value.syncedFixed(-1)));

        entityLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue() instanceof SyncedValue)
                .forEach(entry -> entry.setValue(Value.syncedFixed(-1)));

        cobbleGeneratorValues.write(cobbleGeneratorValues -> {
            cobbleGeneratorValues.values().forEach(cobbleGeneratorValue -> {
                cobbleGeneratorValue.entrySet().stream()
                        .filter(entry -> overrideCustom || entry.getValue() instanceof SyncedValue)
                        .forEach(entry -> entry.setValue(Value.syncedFixed(-1)));
            });
        });

        islandEffects.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue() instanceof SyncedValue)
                .forEach(entry -> entry.setValue(Value.syncedFixed(-1)));

        roleLimits.entrySet().stream()
                .filter(entry -> overrideCustom || entry.getValue() instanceof SyncedValue)
                .forEach(entry -> entry.setValue(Value.syncedFixed(-1)));
    }

    private void syncUpgrade(SUpgradeLevel upgradeLevel, boolean overrideCustom) {
        cropGrowth.set(cropGrowth -> {
            if ((overrideCustom || cropGrowth instanceof SyncedValue) && cropGrowth.get() < upgradeLevel.getCropGrowth())
                return upgradeLevel.getCropGrowthUpgradeValue();
            return cropGrowth;
        });

        spawnerRates.set(spawnerRates -> {
            if ((overrideCustom || spawnerRates instanceof SyncedValue) && spawnerRates.get() < upgradeLevel.getSpawnerRates())
                return upgradeLevel.getSpawnerRatesUpgradeValue();
            return spawnerRates;
        });

        mobDrops.set(mobDrops -> {
            if ((overrideCustom || mobDrops instanceof SyncedValue) && mobDrops.get() < upgradeLevel.getMobDrops())
                return upgradeLevel.getMobDropsUpgradeValue();
            return mobDrops;
        });

        teamLimit.set(teamLimit -> {
            if ((overrideCustom || teamLimit instanceof SyncedValue) && teamLimit.get() < upgradeLevel.getTeamLimit())
                return upgradeLevel.getTeamLimitUpgradeValue();
            return teamLimit;
        });

        warpsLimit.set(warpsLimit -> {
            if ((overrideCustom || warpsLimit instanceof SyncedValue) && warpsLimit.get() < upgradeLevel.getWarpsLimit())
                return upgradeLevel.getWarpsLimitUpgradeValue();
            return warpsLimit;
        });

        coopLimit.set(coopLimit -> {
            if ((overrideCustom || coopLimit instanceof SyncedValue) && coopLimit.get() < upgradeLevel.getCoopLimit())
                return upgradeLevel.getCoopLimitUpgradeValue();
            return coopLimit;
        });

        islandSize.set(islandSize -> {
            if ((overrideCustom || islandSize instanceof SyncedValue) && islandSize.get() < upgradeLevel.getBorderSize())
                return upgradeLevel.getBorderSizeUpgradeValue();
            return islandSize;
        });

        bankLimit.set(bankLimit -> {
            if ((overrideCustom || bankLimit instanceof SyncedValue) && bankLimit.get().compareTo(upgradeLevel.getBankLimit()) < 0)
                return upgradeLevel.getBankLimitUpgradeValue();
            return bankLimit;
        });

        for (Map.Entry<Key, Value<Integer>> entry : upgradeLevel.getBlockLimitsUpgradeValue().entrySet()) {
            Value<Integer> currentValue = blockLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || ((overrideCustom || currentValue instanceof SyncedValue) && currentValue.get() < entry.getValue().get()))
                blockLimits.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Key, Value<Integer>> entry : upgradeLevel.getEntityLimitsUpgradeValue().entrySet()) {
            Value<Integer> currentValue = entityLimits.getRaw(entry.getKey(), null);
            if (currentValue == null || ((overrideCustom || currentValue instanceof SyncedValue) && currentValue.get() < entry.getValue().get()))
                entityLimits.put(entry.getKey(), entry.getValue());
        }

        Map<World.Environment, Map<Key, Value<Integer>>> upgradeGeneratorRates = upgradeLevel.getGeneratorUpgradeValue();
        if (!upgradeGeneratorRates.isEmpty()) {
            this.cobbleGeneratorValues.write(cobbleGeneratorValues -> {
                for (World.Environment environment : World.Environment.values()) {
                    Map<Key, Value<Integer>> upgradeLevelGeneratorRates = upgradeGeneratorRates.get(environment);

                    if (upgradeLevelGeneratorRates == null)
                        continue;

                    KeyMap<Value<Integer>> worldGeneratorRates = cobbleGeneratorValues.get(environment);

                    if (worldGeneratorRates != null && !upgradeLevelGeneratorRates.isEmpty())
                        worldGeneratorRates.entrySet().removeIf(entry -> entry.getValue() instanceof SyncedValue);

                    for (Map.Entry<Key, Value<Integer>> entry : upgradeLevelGeneratorRates.entrySet()) {
                        Key block = entry.getKey();
                        Value<Integer> rate = entry.getValue();

                        Value<Integer> currentValue = worldGeneratorRates == null ? null : worldGeneratorRates.get(block);
                        if (currentValue == null || ((overrideCustom || currentValue instanceof SyncedValue) &&
                                currentValue.get() < rate.get())) {
                            if (worldGeneratorRates == null) {
                                worldGeneratorRates = KeyMapImpl.createConcurrentHashMap();
                                cobbleGeneratorValues.put(environment, worldGeneratorRates);
                            }

                            worldGeneratorRates.put(block, rate);
                        }
                    }
                }
            });
        }

        for (Map.Entry<PotionEffectType, Value<Integer>> entry : upgradeLevel.getPotionEffectsUpgradeValue().entrySet()) {
            Value<Integer> currentValue = islandEffects.get(entry.getKey());
            if (currentValue == null || ((overrideCustom || currentValue instanceof SyncedValue) && currentValue.get() < entry.getValue().get()))
                islandEffects.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<PlayerRole, Value<Integer>> entry : upgradeLevel.getRoleLimitsUpgradeValue().entrySet()) {
            Value<Integer> currentValue = roleLimits.get(entry.getKey());
            if (currentValue == null || ((overrideCustom || currentValue instanceof SyncedValue) && currentValue.get() < entry.getValue().get()))
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
            if (!ignoredMembers.contains(islandMember) && (!onlyOnline || islandMember.isOnline())) {
                islandMemberConsumer.accept(islandMember);
            }
        }
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
