package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.database.Query;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.menu.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.menu.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.menu.MenuMemberManage;
import com.bgsoftware.superiorskyblock.menu.MenuMemberRole;
import com.bgsoftware.superiorskyblock.menu.MenuMembers;
import com.bgsoftware.superiorskyblock.menu.MenuPermissions;
import com.bgsoftware.superiorskyblock.menu.MenuSettings;
import com.bgsoftware.superiorskyblock.menu.MenuUpgrades;
import com.bgsoftware.superiorskyblock.menu.MenuValues;
import com.bgsoftware.superiorskyblock.menu.MenuVisitors;
import com.bgsoftware.superiorskyblock.menu.MenuWarps;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.DoubleTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.queue.Queue;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class SIsland extends DatabaseObject implements Island {

    public static final String VISITORS_WARP_NAME = "visit";
    private static final int NO_BLOCK_LIMIT = -1;

    protected static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static boolean calcProcess = false;
    private static Queue<CalcIslandData> islandCalcsQueue = new Queue<>();

    /*
     * SIsland identifiers
     */
    private SuperiorPlayer owner;
    private final BlockPosition center;

    /*
     * SIsland data
     */

    private final PriorityQueue<SuperiorPlayer> members = new PriorityQueue<>(SortingComparators.ISLAND_MEMBERS_COMPARATOR);
    private final PriorityQueue<SuperiorPlayer> playersInside = new PriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR);
    private final Set<SuperiorPlayer> banned = new HashSet<>(), coop = new HashSet<>(), invitedPlayers = new HashSet<>();
    private final Map<Object, SPermissionNode> permissionNodes = new HashMap<>();
    private final Map<String, Integer> cobbleGenerator = new HashMap<>();
    private final Set<IslandSettings> islandSettings = new HashSet<>();
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final KeyMap<Integer> blockCounts = new KeyMap<>();
    private final KeyMap<Integer> blockLimits = new KeyMap<>(plugin.getSettings().defaultBlockLimits);
    private final Map<String, WarpData> warps = new HashMap<>();
    private BigDecimalFormatted islandBank = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted islandWorth = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted islandLevel = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted bonusWorth = BigDecimalFormatted.ZERO;
    private String discord = "None", paypal = "None";
    private int islandSize = plugin.getSettings().defaultIslandSize;
    private Map<World.Environment, Location> teleportLocations = new HashMap<>();
    private Location visitorsLocation;
    private boolean locked = false;
    private String islandName = "";
    private String description = "";
    private final Map<UUID, Rating> ratings = new HashMap<>();
    private final Set<String> completedMissions = new HashSet<>();
    private Biome biome;
    private boolean ignored = false;
    private String generatedSchematics = "normal";
    private String schemName = "";

    /*
     * SIsland multipliers & limits
     */

    private int warpsLimit = plugin.getSettings().defaultWarpsLimit;
    private int teamLimit = plugin.getSettings().defaultTeamLimit;
    private double cropGrowth = plugin.getSettings().defaultCropGrowth;
    private double spawnerRates = plugin.getSettings().defaultSpawnerRates;
    private double mobDrops = plugin.getSettings().defaultMobDrops;

    public SIsland(CachedResultSet resultSet){
        this.owner = SSuperiorPlayer.of(UUID.fromString(resultSet.getString("owner")));

        this.center = SBlockPosition.of(Objects.requireNonNull(LocationUtils.getLocation(resultSet.getString("center"))));
        this.visitorsLocation = LocationUtils.getLocation(resultSet.getString("visitorsLocation"));

        IslandDeserializer.deserializeLocations(resultSet.getString("teleportLocation"), this.teleportLocations);
        IslandDeserializer.deserializeMembers(resultSet.getString("members"), this.members);
        IslandDeserializer.deserializeBanned(resultSet.getString("banned"), this.banned);
        IslandDeserializer.deserializePermissions(resultSet.getString("permissionNodes"), this.permissionNodes);
        IslandDeserializer.deserializeUpgrades(resultSet.getString("upgrades"), this.upgrades);
        IslandDeserializer.deserializeWarps(resultSet.getString("warps"), this.warps);
        IslandDeserializer.deserializeBlockCounts(resultSet.getString("blockCounts"), this);
        IslandDeserializer.deserializeBlockLimits(resultSet.getString("blockLimits"), this.blockLimits);
        IslandDeserializer.deserializeRatings(resultSet.getString("ratings"), this.ratings);
        IslandDeserializer.deserializeMissions(resultSet.getString("missions"), this.completedMissions);
        IslandDeserializer.deserializeSettings(resultSet.getString("settings"), this.islandSettings);
        IslandDeserializer.deserializeGenerators(resultSet.getString("generator"), this.cobbleGenerator);

        this.islandBank = BigDecimalFormatted.of(resultSet.getString("islandBank"));
        this.bonusWorth = BigDecimalFormatted.of(resultSet.getString("bonusWorth"));
        this.islandSize = resultSet.getInt("islandSize");
        this.teamLimit = resultSet.getInt("teamLimit");
        this.warpsLimit =  resultSet.getInt("warpsLimit");
        this.cropGrowth = resultSet.getDouble("cropGrowth");
        this.spawnerRates = resultSet.getDouble("spawnerRates");
        this.mobDrops = resultSet.getDouble("mobDrops");
        this.discord = resultSet.getString("discord");
        this.paypal = resultSet.getString("paypal");
        this.locked = resultSet.getBoolean("locked");
        this.islandName = resultSet.getString("name");
        this.description = resultSet.getString("description");
        this.ignored = resultSet.getBoolean("ignored");
        this.generatedSchematics = resultSet.getString("generatedSchematics");
        this.schemName = resultSet.getString("schemName");

        if(blockCounts.isEmpty())
            calcIslandWorth(null);

        assignPermissionNodes();
        assignSettings();
        checkMembersDuplication();

        Executor.sync(() -> biome = getCenter(World.Environment.NORMAL).getBlock().getBiome());
    }

    public SIsland(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();
        this.owner = SSuperiorPlayer.of(UUID.fromString(((StringTag) compoundValues.get("owner")).getValue()));
        this.center = SBlockPosition.of(((StringTag) compoundValues.get("center")).getValue());

        this.teleportLocations.put(World.Environment.NORMAL, compoundValues.containsKey("teleportLocation") ?
                LocationUtils.getLocation(((StringTag) compoundValues.get("teleportLocation")).getValue()) : getCenter(World.Environment.NORMAL));

        List<Tag> members = ((ListTag) compoundValues.get("members")).getValue();
        for(Tag _tag : members)
            this.members.add(SSuperiorPlayer.of(UUID.fromString(((StringTag) _tag).getValue())));

        List<Tag> banned = ((ListTag) compoundValues.get("banned")).getValue();
        for(Tag _tag : banned)
            this.banned.add(SSuperiorPlayer.of(UUID.fromString(((StringTag) _tag).getValue())));

        Map<String, Tag> permissionNodes = ((CompoundTag) compoundValues.get("permissionNodes")).getValue();
        for(String playerRole : permissionNodes.keySet())
            this.permissionNodes.put(SPlayerRole.of(playerRole), new SPermissionNode((ListTag) permissionNodes.get(playerRole)));

        Map<String, Tag> upgrades = ((CompoundTag) compoundValues.get("upgrades")).getValue();
        for(String upgrade : upgrades.keySet())
            this.upgrades.put(upgrade, ((IntTag) upgrades.get(upgrade)).getValue());

        Map<String, Tag> warps = ((CompoundTag) compoundValues.get("warps")).getValue();
        for(String warp : warps.keySet())
            this.warps.put(warp, new WarpData(FileUtils.toLocation(((StringTag) warps.get(warp)).getValue()), false));

        this.islandBank = BigDecimalFormatted.of(compoundValues.get("islandBank"));
        this.islandSize = ((IntTag) compoundValues.getOrDefault("islandSize", new IntTag(plugin.getSettings().defaultIslandSize))).getValue();

        this.blockLimits.put(Key.of("HOPPER"), ((IntTag) compoundValues.get("hoppersLimit")).getValue());
        this.teamLimit = ((IntTag) compoundValues.get("teamLimit")).getValue();
        this.warpsLimit = compoundValues.containsKey("warpsLimit") ? ((IntTag) compoundValues.get("warpsLimit")).getValue() : plugin.getSettings().defaultWarpsLimit;
        this.cropGrowth = ((DoubleTag) compoundValues.get("cropGrowth")).getValue();
        this.spawnerRates = ((DoubleTag) compoundValues.get("spawnerRates")).getValue();
        this.mobDrops = ((DoubleTag) compoundValues.get("mobDrops")).getValue();
        this.discord = ((StringTag) compoundValues.get("discord")).getValue();
        this.paypal = ((StringTag) compoundValues.get("paypal")).getValue();

        if(blockCounts.isEmpty())
            calcIslandWorth(null);

        assignPermissionNodes();
        assignSettings();
        assignGenerator();
        checkMembersDuplication();
    }

    public SIsland(SuperiorPlayer superiorPlayer, Location location, String islandName, String schemName){
        this(superiorPlayer, SBlockPosition.of(location), islandName, schemName);
    }

    @SuppressWarnings("WeakerAccess")
    public SIsland(SuperiorPlayer superiorPlayer, SBlockPosition wrappedLocation, String islandName, String schemName){
        if(superiorPlayer != null){
            this.owner = superiorPlayer.getIslandLeader();
            superiorPlayer.setPlayerRole(SPlayerRole.lastRole());
        }else{
            this.owner = null;
        }
        this.center = wrappedLocation;
        this.islandName = islandName;
        this.schemName = schemName;
        assignPermissionNodes();
        assignSettings();
        assignGenerator();
    }

    /*
     *  General methods
     */

    @Override
    public SuperiorPlayer getOwner() {
        return owner;
    }

    /*
     *  Player related methods
     */

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        List<SuperiorPlayer> members = new ArrayList<>();

        if(includeOwner)
            members.add(owner);

        members.addAll(this.members);

        return members;
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return new ArrayList<>(banned);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return playersInside.stream().filter(superiorPlayer -> !isMember(superiorPlayer)).collect(Collectors.toList());
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return new ArrayList<>(playersInside);
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer){
        if(invitedPlayers.contains(superiorPlayer))
            return;

        invitedPlayers.add(superiorPlayer);
        //Revoke the invite after 5 minutes
        Executor.sync(() -> revokeInvite(superiorPlayer), 6000L);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer){
        invitedPlayers.remove(superiorPlayer);
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer){
        return invitedPlayers.contains(superiorPlayer);
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        members.add(superiorPlayer);
        superiorPlayer.setIslandLeader(owner);
        superiorPlayer.setPlayerRole(playerRole);

        MenuMembers.refreshMenus();

        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getPlayerCollectionString(members))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer){
        members.remove(superiorPlayer);
        superiorPlayer.setIslandLeader(superiorPlayer);

        if(superiorPlayer.isOnline())
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());

        MenuMemberManage.destroyMenus(superiorPlayer);
        MenuMemberRole.destroyMenus(superiorPlayer);
        MenuMembers.refreshMenus();

        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getPlayerCollectionString(members))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer){
        return owner.equals(superiorPlayer.getIslandLeader());
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer){
        banned.add(superiorPlayer);

        if(isMember(superiorPlayer))
            kickMember(superiorPlayer);

        if(superiorPlayer.isOnline() && isInside(superiorPlayer.getLocation()))
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());

        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getPlayerCollectionString(banned))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        banned.remove(superiorPlayer);
        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getPlayerCollectionString(banned))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer){
        return banned.contains(superiorPlayer);
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        coop.add(superiorPlayer);
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        coop.remove(superiorPlayer);
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        return coop.contains(superiorPlayer);
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        if(inside)
            playersInside.add(superiorPlayer);
        else
            playersInside.remove(superiorPlayer);

        MenuVisitors.refreshMenus();
    }

    /*
     *  Location related methods
     */

    @Override
    @Deprecated
    public Location getCenter(){
        return getCenter(World.Environment.NORMAL);
    }

    @Override
    public Location getCenter(World.Environment environment){
        World world = plugin.getGrid().getIslandsWorld(environment);

        if(world == null)
            throw new NullPointerException("Couldn't find world for environment " + environment);

        return center.parse(world).add(0.5, 0, 0.5);
    }

    @Override
    public Location getVisitorsLocation() {
        return visitorsLocation == null ? null : visitorsLocation.clone();
    }

    @Override
    @Deprecated
    public Location getTeleportLocation() {
        return getTeleportLocation(World.Environment.NORMAL);
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        Location teleportLocation = teleportLocations.get(environment);

        if(teleportLocation == null)
            teleportLocation = getCenter(environment);

        return teleportLocation == null ? null : teleportLocation.clone();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        teleportLocations.put(teleportLocation.getWorld().getEnvironment(), teleportLocation.clone());
        Query.ISLAND_SET_TELEPORT_LOCATION.getStatementHolder()
                .setString(IslandSerializer.serializeLocations(teleportLocations))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        this.visitorsLocation = visitorsLocation;

        if(visitorsLocation == null){
            deleteWarp(VISITORS_WARP_NAME);
        }
        else{
            setWarpLocation(VISITORS_WARP_NAME, visitorsLocation, false);
        }

        Query.ISLAND_SET_VISITORS_LOCATION.getStatementHolder()
                .setString(LocationUtils.getLocation(getVisitorsLocation()))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public Location getMinimum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter(World.Environment.NORMAL).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter(World.Environment.NORMAL).add(islandDistance, 0, islandDistance);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected){
        int islandSize = getIslandSize();
        Location min = onlyProtected ? center.parse().subtract(islandSize, 0, islandSize) : getMinimum();
        Location max = onlyProtected ? center.parse().add(islandSize, 0, islandSize) : getMaximum();
        Chunk minChunk = min.getChunk(), maxChunk = max.getChunk();

        List<Chunk> chunks = new ArrayList<>();

        for(int x = minChunk.getX(); x <= maxChunk.getX(); x++){
            for(int z = minChunk.getZ(); z <= maxChunk.getZ(); z++){
                chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }


        return chunks;
    }

    @Override
    public boolean isInside(Location location){
        if(!plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        Location min = getMinimum(), max = getMaximum();
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @Override
    public boolean isInsideRange(Location location){
        if(!plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        int islandSize = getIslandSize();
        Location min = center.parse().subtract(islandSize, 0, islandSize);
        Location max = center.parse().add(islandSize, 0, islandSize);
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    /*
     *  Permissions related methods
     */

    @Override
    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission){
        return sender instanceof ConsoleCommandSender || hasPermission(SSuperiorPlayer.of(sender), islandPermission);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission){
        return superiorPlayer.hasBypassModeEnabled() || superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPermission) || getPermissionNode(superiorPlayer).hasPermission(islandPermission);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value) {
        permissionNodes.get(playerRole).setPermission(islandPermission, value);

        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {
        SPermissionNode permissionNode = permissionNodes.getOrDefault(superiorPlayer.getUniqueId(), new SPermissionNode("", null));

        permissionNode.setPermission(islandPermission, value);

        permissionNodes.put(superiorPlayer.getUniqueId(), permissionNode);

        MenuPermissions.refreshMenus();

        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public SPermissionNode getPermissionNode(PlayerRole playerRole) {
        return permissionNodes.get(playerRole);
    }

    @Override
    public SPermissionNode getPermissionNode(SuperiorPlayer superiorPlayer) {
        PlayerRole playerRole = isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();
        return permissionNodes.getOrDefault(superiorPlayer.getUniqueId(), getPermissionNode(playerRole));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPermission islandPermission) {
        return plugin.getPlayers().getRoles().stream()
                .filter(playerRole -> getPermissionNode(playerRole).hasPermission(islandPermission))
                .min(Comparator.comparingInt(PlayerRole::getWeight)).orElse(SPlayerRole.guestRole());
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
        return islandName;
    }

    @Override
    public void setName(String islandName) {
        this.islandName = islandName;

        Query.ISLAND_SET_NAME.getStatementHolder()
                .setString(islandName)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;

        Query.ISLAND_SET_DESCRIPTION.getStatementHolder()
                .setString(description)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void disbandIsland(){
        getIslandMembers(true).forEach(superiorPlayer -> {
            if (members.contains(superiorPlayer))
                kickMember(superiorPlayer);

            if (plugin.getSettings().disbandInventoryClear)
                plugin.getNMSAdapter().clearInventory(superiorPlayer.asOfflinePlayer());

            superiorPlayer.getCompletedMissions().forEach(mission -> {
                MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission);
                if (missionData != null && missionData.disbandReset)
                    superiorPlayer.resetMission(mission);
            });
        });

        plugin.getGrid().deleteIsland(this);

        plugin.getNMSAdapter().regenerateChunks(getAllChunks(true));
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker) {
        calcIslandWorth(asker, null);
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker, Runnable callback) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> calcIslandWorth(asker, callback));
            return;
        }

        if(calcProcess) {
            islandCalcsQueue.push(new CalcIslandData(this, asker, callback));
            return;
        }

        calcProcess = true;

        List<Chunk> chunks = getAllChunks(true);
        List<ChunkSnapshot> chunkSnapshots = new ArrayList<>();

        for (Chunk chunk : chunks) {
            chunkSnapshots.add(chunk.getChunkSnapshot(true, false, false));
            BlocksProvider_WildStacker.cacheChunk(chunk);
        }

        blockCounts.clear();
        islandWorth = BigDecimalFormatted.ZERO;
        islandLevel = BigDecimalFormatted.ZERO;

        World world = Bukkit.getWorld(chunkSnapshots.get(0).getWorldName());

        Executor.async(() -> {
            Set<Pair<Location, Integer>> spawnersToCheck = new HashSet<>();

            ExecutorService scanService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("SuperiorSkyblock Blocks Scanner %d").build());

            for (ChunkSnapshot chunkSnapshot : chunkSnapshots) {
                scanService.execute(() -> {
                    if(LocationUtils.isChunkEmpty(chunkSnapshot))
                        return;

                    double islandWorth = 0;

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 0; y < 256; y++) {
                                Key blockKey = Key.of("AIR");

                                try{
                                    blockKey = plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y, z);
                                }catch(ArrayIndexOutOfBoundsException ignored){ }

                                if(blockKey.toString().contains("AIR"))
                                    continue;

                                Location location = new Location(world, (chunkSnapshot.getX() * 16) + x, y, (chunkSnapshot.getZ() * 16) + z);
                                int blockCount = plugin.getGrid().getBlockAmount(location);

                                if(blockKey.toString().contains("SPAWNER")){
                                    Pair<Integer, EntityType> entry = plugin.getProviders().getSpawner(location);
                                    blockCount = entry.getKey();
                                    if(entry.getValue() == null){
                                        spawnersToCheck.add(new Pair<>(location, blockCount));
                                        continue;
                                    }
                                    else{
                                        blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + ":" + entry.getValue());
                                    }
                                }

                                Pair<Integer, Material> blockPair = plugin.getProviders().getBlock(location);

                                if(blockPair != null){
                                    blockCount = blockPair.getKey();
                                    blockKey = Key.of(blockPair.getValue().name());
                                }

                                handleBlockPlace(blockKey, blockCount, false);
                            }
                        }
                    }
                });
            }

            try{
                scanService.shutdown();
                scanService.awaitTermination(1, TimeUnit.MINUTES);
            }catch(Exception ex){
                ex.printStackTrace();
            }

            Executor.sync(() -> {
                Key blockKey;
                int blockCount;

                for(Pair<Location, Integer> pair : spawnersToCheck){
                    try {
                        CreatureSpawner creatureSpawner = (CreatureSpawner) pair.getKey().getBlock().getState();
                        blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + ":" + creatureSpawner.getSpawnedType());
                        blockCount = pair.getValue();
                        if(blockCount <= 0)
                            blockCount = plugin.getProviders().getSpawner(pair.getKey()).getKey();
                        handleBlockPlace(blockKey, blockCount, false);
                    }catch(Throwable ignored){}
                }
                spawnersToCheck.clear();

                BigDecimal islandLevel = getIslandLevel();
                BigDecimal islandWorth = getWorth();

                Bukkit.getPluginManager().callEvent(new IslandWorthCalculatedEvent(this, asker, islandLevel, islandWorth));

                if(asker != null)
                    Locale.ISLAND_WORTH_RESULT.send(asker, islandWorth, islandLevel);

                if(callback != null)
                    callback.run();

                for(Chunk chunk : chunks)
                    BlocksProvider_WildStacker.uncacheChunk(chunk);

                saveBlockCounts();

                calcProcess = false;

                if(islandCalcsQueue.size() != 0){
                    CalcIslandData calcIslandData = islandCalcsQueue.pop();
                    calcIslandData.island.calcIslandWorth(calcIslandData.asker, calcIslandData.callback);
                }
            });
        });
    }

    @Override
    public void updateBorder() {
        getAllPlayersInside().forEach(superiorPlayer -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, this));
    }

    @Override
    public int getIslandSize() {
        return islandSize;
    }

    @Override
    public void setIslandSize(int islandSize) {
        this.islandSize = islandSize;
        Query.ISLAND_SET_SIZE.getStatementHolder()
                .setInt(islandSize)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getDiscord() {
        return discord;
    }

    @Override
    public void setDiscord(String discord) {
        this.discord = discord;
        Query.ISLAND_SET_DISCORD.getStatementHolder()
                .setString(discord)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getPaypal() {
        return paypal;
    }

    @Override
    public void setPaypal(String paypal) {
        this.paypal = paypal;
        Query.ISLAND_SET_PAYPAL.getStatementHolder()
                .setString(paypal)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public Biome getBiome() {
        return biome;
    }

    @Override
    public void setBiome(Biome biome){
        //We need to load all chunks so the biome will get changed.
        getAllChunks().forEach(Chunk::load);
        //We use the nms method as it's much more optimized and better in general.
        plugin.getNMSAdapter().setBiome(getMinimum(), getMaximum(), biome);
        this.biome = biome;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;

        if(locked){
            for(SuperiorPlayer victimPlayer : getAllPlayersInside()){
                if(!hasPermission(victimPlayer, IslandPermission.CLOSE_BYPASS)){
                    victimPlayer.teleport(plugin.getGrid().getSpawnIsland());
                    Locale.ISLAND_WAS_CLOSED.send(victimPlayer);
                }
            }
        }

        Query.ISLAND_SET_LOCKED.getStatementHolder()
                .setBoolean(locked)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;

        Query.ISLAND_SET_IGNORED.getStatementHolder()
                .setBoolean(ignored)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        if(superiorPlayer.equals(owner))
            return false;

        SuperiorPlayer previousOwner = getOwner();

        IslandTransferEvent islandTransferEvent = new IslandTransferEvent(this, previousOwner, superiorPlayer);
        Bukkit.getPluginManager().callEvent(islandTransferEvent);

        if(islandTransferEvent.isCancelled())
            return false;

        executeDeleteStatement(true);

        //Kick member without saving to database
        members.remove(superiorPlayer);
        superiorPlayer.setPlayerRole(SPlayerRole.lastRole());

        //Add member without saving to database
        members.add(previousOwner);
        PlayerRole previousRole = SPlayerRole.lastRole().getPreviousRole();
        previousOwner.setPlayerRole(previousRole == null ? SPlayerRole.lastRole() : previousRole);

        //Changing owner of the island and updating all players
        owner = superiorPlayer;
        getIslandMembers(true).forEach(islandMember -> islandMember.setIslandLeader(owner));

        executeInsertStatement(true);

        plugin.getGrid().transferIsland(previousOwner.getUniqueId(), owner.getUniqueId());

        return true;
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers){
        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> Locale.sendMessage(superiorPlayer, message));
    }

    /*
     *  Bank related methods
     */

    @Override
    @Deprecated
    public BigDecimal getMoneyInBankAsBigDecimal() {
        return getMoneyInBank();
    }

    @Override
    public BigDecimal getMoneyInBank() {
        if(islandBank.doubleValue() < 0)
            islandBank = BigDecimalFormatted.ZERO;
        return islandBank;
    }

    @Override
    public void depositMoney(double amount){
        islandBank = islandBank.add(BigDecimalFormatted.of(amount));
        Query.ISLAND_SET_BANK.getStatementHolder()
                .setString(islandBank.getAsString())
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void withdrawMoney(double amount){
        islandBank = islandBank.subtract(BigDecimalFormatted.of(amount));
        Query.ISLAND_SET_BANK.getStatementHolder()
                .setString(islandBank.getAsString())
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Worth related methods
     */

    @Override
    public void handleBlockPlace(Block block){
        handleBlockPlace(Key.of(block), 1);
    }

    @Override
    public void handleBlockPlace(Block block, int amount){
        handleBlockPlace(Key.of(block), amount, true);
    }

    @Override
    public void handleBlockPlace(Block block, int amount, boolean save) {
        handleBlockPlace(Key.of(block), amount, save);
    }

    @Override
    public void handleBlockPlace(Key key, int amount){
        handleBlockPlace(key, amount, true);
    }

    @Override
    public synchronized void handleBlockPlace(Key key, int amount, boolean save) {
        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean increaseAmount = false;

        if(blockValue.doubleValue() >= 0){
            islandWorth = islandWorth.add(blockValue.multiply(new BigDecimal(amount)));
            increaseAmount = true;
        }

        if(blockLevel.doubleValue() >= 0){
            islandLevel = islandLevel.add(blockLevel.multiply(new BigDecimal(amount)));
            increaseAmount = true;
        }

        if(increaseAmount || blockLimits.containsKey(key)) {
            key = plugin.getBlockValues().getBlockKey(key);
            int currentAmount = blockCounts.getRaw(key, 0);
            blockCounts.put(key, currentAmount + amount);

            if(!key.toString().equals(blockLimits.getKey(key).toString())){
                key = blockLimits.getKey(key);
                currentAmount = blockCounts.getRaw(key, 0);
                blockCounts.put(key, currentAmount + amount);
            }

            MenuValues.refreshMenus();

            if(save) saveBlockCounts();
        }
    }

    @Override
    public void handleBlockBreak(Block block){
        handleBlockBreak(Key.of(block), 1);
    }

    @Override
    public void handleBlockBreak(Block block, int amount){
        handleBlockBreak(block, amount, true);
    }

    @Override
    public void handleBlockBreak(Block block, int amount, boolean save) {
        handleBlockBreak(Key.of(block), amount, save);
    }

    @Override
    public void handleBlockBreak(Key key, int amount){
        handleBlockBreak(key, amount, true);
    }

    @Override
    public synchronized void handleBlockBreak(Key key, int amount, boolean save) {
        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean decreaseAmount = false;

        if(blockValue.doubleValue() > 0){
            islandWorth = islandWorth.subtract(blockValue.multiply(new BigDecimal(amount)));
            if(islandWorth.doubleValue() < 0)
                islandWorth = BigDecimalFormatted.ZERO;
            decreaseAmount = true;
        }

        if(blockLevel.doubleValue() > 0){
            islandLevel = islandLevel.subtract(blockLevel.multiply(new BigDecimal(amount)));
            if(islandLevel.doubleValue() < 0)
                islandLevel = BigDecimalFormatted.ZERO;
            decreaseAmount = true;
        }

        if(decreaseAmount || blockLimits.containsKey(key)){
            key = plugin.getBlockValues().getBlockKey(key);
            int currentAmount = blockCounts.getRaw(key, 0);
            if(currentAmount <= amount)
                blockCounts.removeRaw(key);
            else
                blockCounts.put(key, currentAmount - amount);

            if(!key.toString().equals(blockLimits.getKey(key).toString())){
                key = blockLimits.getKey(key);
                currentAmount = blockCounts.getRaw(key, 0);
                if(currentAmount <= amount)
                    blockCounts.removeRaw(key);
                else
                    blockCounts.put(key, currentAmount - amount);
            }

            MenuValues.refreshMenus();

            if(save) saveBlockCounts();
        }
    }

    @Override
    public int getBlockCount(Key key){
        return blockCounts.getOrDefault(key, 0);
    }

    @Override
    public int getExactBlockCount(Key key) {
        return blockCounts.getRaw(key, 0);
    }

    @Override
    @Deprecated
    public BigDecimal getWorthAsBigDecimal() {
        return getWorth();
    }

    @Override
    public BigDecimal getWorth() {
        int bankWorthRate = plugin.getSettings().bankWorthRate;
        //noinspection BigDecimalMethodWithoutRoundingCalled
        BigDecimal islandWorth = bankWorthRate <= 0 ? getRawWorth() : this.islandWorth.add(islandBank.divide(new BigDecimal(bankWorthRate)));
        return islandWorth.add(bonusWorth);
    }

    @Override
    @Deprecated
    public BigDecimal getRawWorthAsBigDecimal() {
        return getRawWorth();
    }

    @Override
    public BigDecimal getRawWorth() {
        return islandWorth;
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth){
        this.bonusWorth = bonusWorth instanceof BigDecimalFormatted ? (BigDecimalFormatted) bonusWorth : BigDecimalFormatted.of(bonusWorth);
        Query.ISLAND_SET_BONUS_WORTH.getStatementHolder()
                .setString(this.bonusWorth.getAsString())
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    @Deprecated
    public BigDecimal getIslandLevelAsBigDecimal() {
        return getIslandLevel();
    }

    @Override
    public BigDecimalFormatted getIslandLevel() {
        return plugin.getSettings().bonusAffectLevel ? islandLevel.add(new BigDecimal(plugin.getBlockValues().convertValueToLevel(bonusWorth))) : islandLevel;
    }

    private void saveBlockCounts(){
        Query.ISLAND_SET_BLOCK_COUNTS.getStatementHolder()
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Upgrade related methods
     */

    @Override
    public int getUpgradeLevel(String upgradeName){
        return upgrades.getOrDefault(upgradeName, 1);
    }

    @Override
    public void setUpgradeLevel(String upgradeName, int level){
        upgrades.put(upgradeName, Math.min(plugin.getUpgrades().getMaxUpgradeLevel(upgradeName), level));

        MenuUpgrades.refreshMenus();

        Query.ISLAND_SET_UPGRADES.getStatementHolder()
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getCropGrowthMultiplier() {
        return cropGrowth;
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        this.cropGrowth = cropGrowth;
        Query.ISLAND_SET_CROP_GROWTH.getStatementHolder()
                .setDouble(cropGrowth)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return spawnerRates;
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.spawnerRates = spawnerRates;
        Query.ISLAND_SET_SPAWNER_RATES.getStatementHolder()
                .setDouble(spawnerRates)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getMobDropsMultiplier() {
        return mobDrops;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        this.mobDrops = mobDrops;
        Query.ISLAND_SET_MOB_DROPS.getStatementHolder()
                .setDouble(mobDrops)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getBlockLimit(Key key) {
        return blockLimits.getOrDefault(key, NO_BLOCK_LIMIT);
    }

    @Override
    public int getExactBlockLimit(Key key) {
        return blockLimits.getRaw(key, NO_BLOCK_LIMIT);
    }

    @Override
    public Map<Key, Integer> getBlocksLimits() {
        return blockLimits.asKeyMap();
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        if(limit <= NO_BLOCK_LIMIT)
            this.blockLimits.removeRaw(key);
        else
            this.blockLimits.put(key, limit);

        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder()
                .setString(IslandSerializer.serializeBlockLimits(this.blockLimits))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key) {
        return hasReachedBlockLimit(key, 1);
    }

    @Override
    public boolean hasReachedBlockLimit(Key key, int amount) {
        int blockLimit = getExactBlockLimit(key);

        //Checking for the specific provided key.
        if(blockLimit > SIsland.NO_BLOCK_LIMIT)
            return getBlockCount(key) + amount > blockLimit;

        //Getting the global key values.
        key = Key.of(key.toString().split(":")[0]);
        blockLimit = getBlockLimit(key);

        return blockLimit > SIsland.NO_BLOCK_LIMIT && getBlockCount(key) + amount > blockLimit;
    }

    @Override
    public int getTeamLimit() {
        return teamLimit;
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder()
                .setInt(teamLimit)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit;
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        this.warpsLimit = warpsLimit;
        Query.ISLAND_SET_WARPS_LIMIT.getStatementHolder()
                .setInt(warpsLimit)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Warps related methods
     */

    @Override
    public Location getWarpLocation(String name){
        return warps.containsKey(name.toLowerCase()) ? warps.get(name.toLowerCase()).location.clone() : null;
    }

    @Override
    public boolean isWarpPrivate(String name) {
        return !warps.containsKey(name.toLowerCase()) || warps.get(name.toLowerCase()).privateFlag;
    }

    @Override
    public void setWarpLocation(String name, Location location, boolean privateFlag) {
        warps.put(name.toLowerCase(), new WarpData(location.clone(), privateFlag));

        MenuGlobalWarps.refreshMenus();
        MenuWarps.refreshMenus();

        Query.ISLAND_SET_WARPS.getStatementHolder()
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp){
        if(plugin.getSettings().warpsWarmup > 0) {
            Locale.TELEPORT_WARMUP.send(superiorPlayer, StringUtils.formatTime(plugin.getSettings().warpsWarmup));
            ((SSuperiorPlayer) superiorPlayer).setTeleportTask(Executor.sync(() ->
                    warpPlayerWithoutWarmup(superiorPlayer, warp), plugin.getSettings().warpsWarmup / 50));
        }
        else {
            warpPlayerWithoutWarmup(superiorPlayer, warp);
        }
    }

    private void warpPlayerWithoutWarmup(SuperiorPlayer superiorPlayer, String warp){
        Location location = warps.get(warp.toLowerCase()).location.clone();
        Block warpBlock = location.getBlock();
        ((SSuperiorPlayer) superiorPlayer).setTeleportTask(null);

        if(!isInsideRange(location)){
            Locale.UNSAFE_WARP.send(superiorPlayer);
            deleteWarp(warp);
            return;
        }

        if(!LocationUtils.isSafeBlock(warpBlock)){
            Locale.UNSAFE_WARP.send(superiorPlayer);
            return;
        }

        if(superiorPlayer.asPlayer().teleport(location.add(0.5, 0, 0.5))){
            Locale.TELEPORTED_TO_WARP.send(superiorPlayer);
        }
    }

    @Override
    public void deleteWarp(SuperiorPlayer superiorPlayer, Location location){
        for(String warpName : new ArrayList<>(warps.keySet())){
            if(LocationUtils.isSameBlock(location, warps.get(warpName).location)){
                deleteWarp(warpName);
                if(superiorPlayer != null)
                    Locale.DELETE_WARP.send(superiorPlayer, warpName);
            }
        }
    }

    @Override
    public void deleteWarp(String name){
        warps.remove(name);

        MenuGlobalWarps.refreshMenus();
        MenuWarps.refreshMenus();

        Query.ISLAND_SET_WARPS.getStatementHolder()
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public List<String> getAllWarps(){
        return new ArrayList<>(warps.keySet());
    }

    @Override
    public boolean hasMoreWarpSlots() {
        return warps.size() < warpsLimit;
    }

    /*
     *  Ratings related methods
     */

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return ratings.getOrDefault(superiorPlayer.getUniqueId(), Rating.UNKNOWN);
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        if(rating == Rating.UNKNOWN)
            ratings.remove(superiorPlayer.getUniqueId());
        else
            ratings.put(superiorPlayer.getUniqueId(), rating);

        MenuIslandRatings.refreshMenus();

        Query.ISLAND_SET_RATINGS.getStatementHolder()
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getTotalRating() {
        double avg = 0;

        for(Rating rating : ratings.values())
            avg += rating.getValue();

        return avg == 0 ? 0 : avg / getRatingAmount();
    }

    @Override
    public int getRatingAmount() {
        return ratings.size();
    }

    @Override
    public Map<UUID, Rating> getRatings() {
        return new HashMap<>(ratings);
    }

    /*
     *  Missions related methods
     */

    @Override
    public void completeMission(Mission mission) {
        completedMissions.add(mission.getName());

        MenuIslandMissions.refreshMenus();

        Query.ISLAND_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void resetMission(Mission mission) {
        completedMissions.remove(mission.getName());

        Query.ISLAND_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean hasCompletedMission(Mission mission) {
        return completedMissions.contains(mission.getName());
    }

    @Override
    public List<Mission> getCompletedMissions() {
        return completedMissions.stream().map(plugin.getMissions()::getMission).collect(Collectors.toList());
    }

    /*
     *  Settings related methods
     */

    @Override
    public boolean hasSettingsEnabled(IslandSettings settings) {
        return islandSettings.contains(settings);
    }

    @Override
    public void enableSettings(IslandSettings settings) {
        islandSettings.add(settings);
        boolean disableTime = false, disableWeather = false;

        //Updating times / weather if necessary
        switch (settings){
            case ALWAYS_DAY:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(0, false));
                disableTime = true;
                break;
            case ALWAYS_MIDDLE_DAY:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(6000, false));
                disableTime = true;
                break;
            case ALWAYS_NIGHT:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(14000, false));
                disableTime = true;
                break;
            case ALWAYS_MIDDLE_NIGHT:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(18000, false));
                disableTime = true;
                break;
            case ALWAYS_SHINY:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerWeather(WeatherType.CLEAR));
                disableWeather = true;
                break;
            case ALWAYS_RAIN:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerWeather(WeatherType.DOWNFALL));
                disableWeather = true;
                break;
            case PVP:
                if(plugin.getSettings().teleportOnPVPEnable)
                    getIslandVisitors().forEach(superiorPlayer -> {
                        superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                        Locale.ISLAND_GOT_PVP_ENABLED_WHILE_INSIDE.send(superiorPlayer);
                    });
                break;
        }

        if(disableTime){
            //Disabling settings without saving to database.
            if(settings != IslandSettings.ALWAYS_DAY)
                islandSettings.remove(IslandSettings.ALWAYS_DAY);
            if(settings != IslandSettings.ALWAYS_MIDDLE_DAY)
                islandSettings.remove(IslandSettings.ALWAYS_MIDDLE_DAY);
            if(settings != IslandSettings.ALWAYS_NIGHT)
                islandSettings.remove(IslandSettings.ALWAYS_NIGHT);
            if(settings != IslandSettings.ALWAYS_MIDDLE_NIGHT)
                islandSettings.remove(IslandSettings.ALWAYS_MIDDLE_NIGHT);
        }

        if(disableWeather){
            if(settings != IslandSettings.ALWAYS_RAIN)
                islandSettings.remove(IslandSettings.ALWAYS_RAIN);
            if(settings != IslandSettings.ALWAYS_SHINY)
                islandSettings.remove(IslandSettings.ALWAYS_SHINY);
        }

        MenuSettings.refreshMenus();

        Query.ISLAND_SET_SETTINGS.getStatementHolder()
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void disableSettings(IslandSettings settings) {
        islandSettings.remove(settings);

        switch (settings){
            case ALWAYS_DAY:
            case ALWAYS_MIDDLE_DAY:
            case ALWAYS_NIGHT:
            case ALWAYS_MIDDLE_NIGHT:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().resetPlayerTime());
                break;
            case ALWAYS_RAIN:
            case ALWAYS_SHINY:
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().resetPlayerWeather());
                break;
        }

        MenuSettings.refreshMenus();

        Query.ISLAND_SET_SETTINGS.getStatementHolder()
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Generator related methods
     */

    @Override
    public void setGeneratorPercentage(Key key, int percentage) {
        if(percentage < 0 || percentage > 100)
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");

        if(percentage == 0)
            cobbleGenerator.remove(key.toString());
        else
            cobbleGenerator.put(key.toString(), percentage);

        Query.ISLAND_SET_GENERATOR.getStatementHolder()
                .setString(IslandSerializer.serializeGenerator(cobbleGenerator))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getGeneratorPercentage(Key key) {
        String keyStr = key.toString();
        int percentage = cobbleGenerator.getOrDefault(keyStr, 0);

        if((percentage <= 0 || percentage > 100) && cobbleGenerator.containsKey(keyStr))
            setGeneratorPercentage(key, 0);

        return cobbleGenerator.getOrDefault(keyStr, 0);
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages() {
        return new HashMap<>(cobbleGenerator);
    }

    /*
     *  Schematic methods
     */

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        return generatedSchematics.contains(environment.name().toLowerCase());
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        generatedSchematics += environment.name().toLowerCase();
        Query.ISLAND_SET_GENERATED_SCHEMATICS.getStatementHolder()
                .setString(generatedSchematics)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getSchematicName() {
        return schemName == null ? "" : schemName;
    }

    /*
     *  Data related methods
     */

    @Override
    public void executeUpdateStatement(boolean async){
        Query.ISLAND_UPDATE.getStatementHolder()
                .setString(LocationUtils.getLocation(getTeleportLocation(World.Environment.NORMAL)))
                .setString(LocationUtils.getLocation(visitorsLocation))
                .setString(members.isEmpty() ? "" : getPlayerCollectionString(members))
                .setString(banned.isEmpty() ? "" : getPlayerCollectionString(banned))
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(islandBank.getAsString())
                .setInt(islandSize)
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setInt(teamLimit)
                .setFloat((float) cropGrowth)
                .setFloat((float) spawnerRates)
                .setFloat((float) mobDrops)
                .setString(discord)
                .setString(paypal)
                .setInt(warpsLimit)
                .setString(bonusWorth.getAsString())
                .setBoolean(false)
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(islandName)
                .setString(description)
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setBoolean(ignored)
                .setString(IslandSerializer.serializeGenerator(cobbleGenerator))
                .setString(generatedSchematics)
                .setString(schemName)
                .setString(owner.getUniqueId().toString())
                .execute(async);
    }

    @Override
    public void executeDeleteStatement(boolean async){
        Query.ISLAND_DELETE.getStatementHolder()
                .setString(owner.getUniqueId().toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async){
        Query.ISLAND_INSERT.getStatementHolder()
                .setString(owner.getUniqueId().toString())
                .setString(LocationUtils.getLocation(center.parse()))
                .setString(IslandSerializer.serializeLocations(teleportLocations))
                .setString(members.isEmpty() ? "" : getPlayerCollectionString(members))
                .setString(banned.isEmpty() ? "" : getPlayerCollectionString(banned))
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(islandBank.getAsString())
                .setInt(islandSize)
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setInt(teamLimit)
                .setFloat((float) cropGrowth)
                .setFloat((float) spawnerRates)
                .setFloat((float) mobDrops)
                .setString(discord)
                .setString(paypal)
                .setInt(warpsLimit)
                .setString(bonusWorth.getAsString())
                .setBoolean(false)
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(islandName)
                .setString(LocationUtils.getLocation(visitorsLocation))
                .setString(description)
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setBoolean(ignored)
                .setString(IslandSerializer.serializeGenerator(cobbleGenerator))
                .setString(generatedSchematics)
                .setString(schemName)
                .execute(async);
    }

    /*
     *  Object related methods
     */

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SIsland && owner.equals(((SIsland) obj).owner);
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(Island other) {
        if(other == null)
            return -1;

        if(plugin.getSettings().islandTopOrder.equals("WORTH")){
            int compare = getWorth().compareTo(other.getWorth());
            if(compare != 0) return compare;
        }
        else {
            int compare = getIslandLevel().compareTo(other.getIslandLevel());
            if(compare != 0) return compare;
        }

        return getOwner().getName().compareTo(other.getOwner().getName());
    }

    /*
     *  Private methods
     */

    private void assignPermissionNodes(){
        boolean save = false;

        for(PlayerRole playerRole : plugin.getPlayers().getRoles()) {
            if(!permissionNodes.containsKey(playerRole)) {
                PlayerRole previousRole = SPlayerRole.of(playerRole.getWeight() - 1);
                permissionNodes.put(playerRole, new SPermissionNode(((SPlayerRole) playerRole).getDefaultPermissions(), permissionNodes.get(previousRole)));
                save = true;
            }
        }

        if(save && owner != null){
            Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                    .setString(IslandSerializer.serializePermissions(permissionNodes))
                    .setString(owner.getUniqueId().toString())
                    .execute(true);
        }
    }

    private void assignSettings(){
        if(!islandSettings.isEmpty() || owner == null)
            return;

        plugin.getSettings().defaultSettings.forEach(setting -> {
            try{
                islandSettings.add(IslandSettings.valueOf(setting));
            }catch(Exception ignored){}
        });

        Query.ISLAND_SET_SETTINGS.getStatementHolder()
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    private void assignGenerator(){
        if(!cobbleGenerator.isEmpty() || owner == null)
            return;

        cobbleGenerator.putAll(plugin.getSettings().defaultGenerator);

        Query.ISLAND_SET_GENERATOR.getStatementHolder()
                .setString(IslandSerializer.serializeGenerator(cobbleGenerator))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    private void checkMembersDuplication(){
        Iterator<SuperiorPlayer> iterator = members.iterator();
        boolean removed = false;

        while (iterator.hasNext()){
            SuperiorPlayer superiorPlayer = iterator.next();
            if(!superiorPlayer.getIslandLeader().equals(owner)){
                iterator.remove();
                removed = true;
            }
        }

        if(removed){
            Query.ISLAND_SET_MEMBERS.getStatementHolder()
                    .setString(members.isEmpty() ? "" : getPlayerCollectionString(members))
                    .setString(owner.getUniqueId().toString())
                    .execute(true);
        }
    }

    private static String getPlayerCollectionString(Collection<SuperiorPlayer> collection) {
        StringBuilder builder = new StringBuilder();
        collection.forEach(superiorPlayer -> builder.append(",").append(superiorPlayer.getUniqueId().toString()));
        return builder.toString();
    }

    private static class CalcIslandData {

        private final Island island;
        private final SuperiorPlayer asker;
        private final Runnable callback;

        private CalcIslandData(Island island, SuperiorPlayer asker, Runnable callback){
            this.island = island;
            this.asker = asker;
            this.callback = callback;
        }

    }

    public static class WarpData{

        public Location location;
        public boolean privateFlag;

        public WarpData(Location location, boolean privateFlag){
            this.location = location;
            this.privateFlag = privateFlag;
        }

    }
}
