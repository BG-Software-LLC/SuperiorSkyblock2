package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.handlers.GridHandler;
import com.bgsoftware.superiorskyblock.island.permissions.PermissionNodeAbstract;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.island.permissions.RolePermissionNode;
import com.bgsoftware.superiorskyblock.menu.MenuCounts;
import com.bgsoftware.superiorskyblock.menu.MenuTopIslands;
import com.bgsoftware.superiorskyblock.menu.MenuUniqueVisitors;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.upgrades.DefaultUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.handlers.MissionsHandler;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
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
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.database.StatementHolder;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.pair.BiPair;
import com.bgsoftware.superiorskyblock.utils.queue.UniquePriorityQueue;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeKeyMap;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeMap;
import com.bgsoftware.superiorskyblock.utils.upgrades.UpgradeValue;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class SIsland extends DatabaseObject implements Island {

    public static final String VISITORS_WARP_NAME = "visit";
    public static final int NO_LIMIT = -1;
    private static int blocksUpdateCounter = 0;

    protected static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    /*
     * Island identifiers
     */
    private SuperiorPlayer owner;
    private final BlockPosition center;
    private final long creationTime;
    private String creationTimeDate;

    /*
     * Island flags
     */

    private final SyncedObject<Boolean> beingRecalculated = SyncedObject.of(false);
    private boolean rawKeyPlacements = false;

    /*
     * Island data
     */

    private final SyncedObject<UniquePriorityQueue<SuperiorPlayer>> members = SyncedObject.of(new UniquePriorityQueue<>(SortingComparators.ISLAND_MEMBERS_COMPARATOR));
    private final SyncedObject<UniquePriorityQueue<SuperiorPlayer>> playersInside = SyncedObject.of(new UniquePriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final SyncedObject<UniquePriorityQueue<SuperiorPlayer>> uniqueVisitors = SyncedObject.of(new UniquePriorityQueue<>(SortingComparators.PLAYER_NAMES_COMPARATOR));
    private final SyncedObject<Set<SuperiorPlayer>> banned = SyncedObject.of(new HashSet<>());
    private final SyncedObject<Set<SuperiorPlayer>> coop = SyncedObject.of(new HashSet<>());
    private final SyncedObject<Set<SuperiorPlayer>> invitedPlayers = SyncedObject.of(new HashSet<>());
    private final Registry<SuperiorPlayer, PlayerPermissionNode> playerPermissions = Registry.createRegistry();
    private final Registry<IslandPrivilege, PlayerRole> rolePermissions = Registry.createRegistry();
    private final Registry<IslandFlag, Byte> islandSettings = Registry.createRegistry();
    private final Registry<String, Integer> upgrades = Registry.createRegistry();
    private final SyncedObject<KeyMap<Integer>> blockCounts = SyncedObject.of(new KeyMap<>());
    private final Registry<String, WarpData> warps = Registry.createRegistry();
    private final SyncedObject<BigDecimalFormatted> islandBank = SyncedObject.of(BigDecimalFormatted.ZERO);
    private final SyncedObject<BigDecimalFormatted> islandWorth = SyncedObject.of(BigDecimalFormatted.ZERO);
    private final SyncedObject<BigDecimalFormatted> islandLevel = SyncedObject.of(BigDecimalFormatted.ZERO);
    private final SyncedObject<BigDecimalFormatted> bonusWorth = SyncedObject.of(BigDecimalFormatted.ZERO);
    private final SyncedObject<BigDecimalFormatted> bonusLevel = SyncedObject.of(BigDecimalFormatted.ZERO);
    private final SyncedObject<String> discord = SyncedObject.of("None");
    private final SyncedObject<String> paypal = SyncedObject.of("None");
    private final Registry<World.Environment, Location> teleportLocations = Registry.createRegistry();
    private final SyncedObject<Location> visitorsLocation = SyncedObject.of(null);
    private final SyncedObject<Boolean> locked = SyncedObject.of(false);
    private final SyncedObject<String> islandName = SyncedObject.of("");
    private final SyncedObject<String> islandRawName = SyncedObject.of("");
    private final SyncedObject<String> description = SyncedObject.of("");
    private final Registry<UUID, Rating> ratings = Registry.createRegistry();
    private final Registry<Mission<?>, Integer> completedMissions = Registry.createRegistry();
    private final SyncedObject<Biome> biome = SyncedObject.of(null);
    private final SyncedObject<Boolean> ignored = SyncedObject.of(false);
    private final SyncedObject<Integer> generatedSchematics = SyncedObject.of(8);
    private final SyncedObject<String> schemName = SyncedObject.of("");
    private final SyncedObject<Integer> unlockedWorlds = SyncedObject.of(0);
    private final SyncedObject<Long> lastTimeUpdate = SyncedObject.of(-1L);
    private final SyncedObject<IslandChest[]> islandChest = SyncedObject.of(new IslandChest[plugin.getSettings().islandChestsDefaultPage]);

    /*
     * Island multipliers & limits
     */

    private final UpgradeKeyMap blockLimits = UpgradeKeyMap.createMap();
    private final UpgradeKeyMap cobbleGeneratorValues = UpgradeKeyMap.createMap();
    private final UpgradeMap<EntityType> entityLimits = UpgradeMap.createMap();
    private final UpgradeMap<PotionEffectType> islandEffects = UpgradeMap.createMap();

    private final UpgradeValue<Integer> islandSize = UpgradeValue.createInteger();
    private final UpgradeValue<Integer> warpsLimit = UpgradeValue.createInteger();
    private final UpgradeValue<Integer> teamLimit = UpgradeValue.createInteger();
    private final UpgradeValue<Integer> coopLimit = UpgradeValue.createInteger();
    private final UpgradeValue<Double> cropGrowth = UpgradeValue.createDouble();
    private final UpgradeValue<Double> spawnerRates = UpgradeValue.createDouble();
    private final UpgradeValue<Double> mobDrops = UpgradeValue.createDouble();

    public SIsland(GridHandler grid, ResultSet resultSet) throws SQLException {
        this.owner = SSuperiorPlayer.of(UUID.fromString(resultSet.getString("owner")));
        this.center = SBlockPosition.of(Objects.requireNonNull(LocationUtils.getLocation(resultSet.getString("center"))));
        this.creationTime = resultSet.getLong("creationTime");
        updateCreationTimeDate();

        rawKeyPlacements = true;

        IslandDeserializer.deserializeLocations(resultSet.getString("teleportLocation"), this.teleportLocations);
        IslandDeserializer.deserializePlayers(resultSet.getString("members"), this.members);
        IslandDeserializer.deserializePlayers(resultSet.getString("banned"), this.banned);
        IslandDeserializer.deserializePermissions(resultSet.getString("permissionNodes"), this.playerPermissions, this.rolePermissions, this);
        IslandDeserializer.deserializeUpgrades(resultSet.getString("upgrades"), this.upgrades);
        IslandDeserializer.deserializeWarps(resultSet.getString("warps"), this.warps);
        IslandDeserializer.deserializeBlockCounts(resultSet.getString("blockCounts"), this);
        IslandDeserializer.deserializeBlockLimits(resultSet.getString("blockLimits"), this.blockLimits);
        IslandDeserializer.deserializeRatings(resultSet.getString("ratings"), this.ratings);
        IslandDeserializer.deserializeMissions(resultSet.getString("missions"), this.completedMissions);
        IslandDeserializer.deserializeSettings(resultSet.getString("settings"), this.islandSettings);
        IslandDeserializer.deserializeGenerators(resultSet.getString("generator"), this.cobbleGeneratorValues);
        IslandDeserializer.deserializePlayers(resultSet.getString("uniqueVisitors"), this.uniqueVisitors);
        IslandDeserializer.deserializeEntityLimits(resultSet.getString("entityLimits"), this.entityLimits);
        IslandDeserializer.deserializeEffects(resultSet.getString("islandEffects"), this.islandEffects);
        IslandDeserializer.deserializeIslandChest(this, resultSet.getString("islandChest"), this.islandChest);

        rawKeyPlacements = false;

        this.islandBank.set(BigDecimalFormatted.of(resultSet.getString("islandBank")));
        this.bonusWorth.set(BigDecimalFormatted.of(resultSet.getString("bonusWorth")));
        this.islandSize.set(resultSet.getInt("islandSize"));
        this.teamLimit.set(resultSet.getInt("teamLimit"));
        this.warpsLimit.set(resultSet.getInt("warpsLimit"));
        this.cropGrowth.set(resultSet.getDouble("cropGrowth"));
        this.spawnerRates.set(resultSet.getDouble("spawnerRates"));
        this.mobDrops.set(resultSet.getDouble("mobDrops"));
        this.discord.set(resultSet.getString("discord"));
        this.paypal.set(resultSet.getString("paypal"));
        this.visitorsLocation.set(LocationUtils.getLocation(resultSet.getString("visitorsLocation")));
        this.locked.set(resultSet.getBoolean("locked"));
        this.islandName.set(resultSet.getString("name"));
        this.islandRawName.set(StringUtils.stripColors(resultSet.getString("name")));
        this.description.set(resultSet.getString("description"));
        this.ignored.set(resultSet.getBoolean("ignored"));
        this.bonusLevel.set(BigDecimalFormatted.of(resultSet.getString("bonusLevel")));

        String generatedSchematics = resultSet.getString("generatedSchematics");
        try{
            this.generatedSchematics.set(Integer.parseInt(generatedSchematics));
        }catch(Exception ex){
            int n = 0;

            if(generatedSchematics.contains("normal"))
                n |= 8;
            if(generatedSchematics.contains("nether"))
                n |= 4;
            if(generatedSchematics.contains("the_end"))
                n |= 3;

            this.generatedSchematics.set(n);
        }

        this.schemName.set(resultSet.getString("schemName"));

        String unlockedWorlds = resultSet.getString("unlockedWorlds");
        try{
            this.unlockedWorlds.set(Integer.parseInt(unlockedWorlds));
        }catch(Exception ex){
            int n = 0;

            if(unlockedWorlds.contains("nether"))
                n |= 1;
            if(unlockedWorlds.contains("the_end"))
                n |= 2;

            this.unlockedWorlds.set(n);
        }

        this.lastTimeUpdate.set(resultSet.getLong("lastTimeUpdate"));
        this.coopLimit.set(resultSet.getInt("coopLimit"));

        if(blockCounts.readAndGet(Map::isEmpty))
            calcIslandWorth(null);

        ChunksTracker.deserialize(grid, this, resultSet.getString("dirtyChunks"));

        //assignPermissionNodes();
        checkMembersDuplication();
        updateOldUpgradeValues();
        updateUpgrades();
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
        this.creationTime = System.currentTimeMillis() / 1000;
        updateCreationTimeDate();
        this.islandName.set(islandName);
        this.islandRawName.set(StringUtils.stripColors(islandName));
        this.schemName.set(schemName);
        //assignPermissionNodes();
        assignGenerator();
        assignIslandChest();
        updateUpgrades();
    }

    /*
     *  General methods
     */

    @Override
    public SuperiorPlayer getOwner() {
        return owner;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getCreationTimeDate() {
        return creationTimeDate;
    }

    public void updateCreationTimeDate(){
        this.creationTimeDate = StringUtils.formatDate(creationTime * 1000);
    }

    /*
     *  Player related methods
     */

    @Override
    public List<SuperiorPlayer> getIslandMembers(boolean includeOwner) {
        List<SuperiorPlayer> members = new ArrayList<>();

        if(includeOwner)
            members.add(owner);

        this.members.read(members::addAll);

        return members;
    }

    @Override
    public List<SuperiorPlayer> getBannedPlayers() {
        return banned.readAndGet(ArrayList::new);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors() {
        return getIslandVisitors(true);
    }

    @Override
    public List<SuperiorPlayer> getIslandVisitors(boolean vanishPlayers) {
        return playersInside.readAndGet(playersInside -> playersInside.stream()
                .filter(superiorPlayer -> !isMember(superiorPlayer) &&
                        (vanishPlayers || !plugin.getProviders().isVanished(superiorPlayer.asPlayer())))
                .collect(Collectors.toList()));
    }

    @Override
    public List<SuperiorPlayer> getAllPlayersInside() {
        return playersInside.readAndGet(playersInside -> playersInside.stream().filter(SuperiorPlayer::isOnline).collect(Collectors.toList()));
    }

    @Override
    public List<SuperiorPlayer> getUniqueVisitors() {
        return uniqueVisitors.readAndGet(ArrayList::new);
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer){
        SuperiorSkyblockPlugin.debug("Action: Invite, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        invitedPlayers.write(invitedPlayers -> invitedPlayers.add(superiorPlayer));
        //Revoke the invite after 5 minutes
        Executor.sync(() -> revokeInvite(superiorPlayer), 6000L);
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer){
        SuperiorSkyblockPlugin.debug("Action: Invite Revoke, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        invitedPlayers.write(invitedPlayers -> invitedPlayers.remove(superiorPlayer));
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer){
        return invitedPlayers.readAndGet(invitedPlayers -> invitedPlayers.contains(superiorPlayer));
    }

    @Override
    public List<SuperiorPlayer> getInvitedPlayers() {
        return invitedPlayers.readAndGet(ArrayList::new);
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        SuperiorSkyblockPlugin.debug("Action: Add Member, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Role: " + playerRole);
        members.write(members -> members.add(superiorPlayer));

        superiorPlayer.setIslandLeader(owner);
        superiorPlayer.setPlayerRole(playerRole);

        MenuMembers.refreshMenus();

        Query.ISLAND_SET_MEMBERS.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(members))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer){
        SuperiorSkyblockPlugin.debug("Action: Kick Member, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        members.write(members -> members.remove(superiorPlayer));

        superiorPlayer.setIslandLeader(superiorPlayer);

        if (superiorPlayer.isOnline()) {
            SuperiorMenu.killMenu(superiorPlayer);
            if(plugin.getSettings().teleportOnKick && getAllPlayersInside().contains(superiorPlayer))
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }

        MenuMemberManage.destroyMenus(superiorPlayer);
        MenuMemberRole.destroyMenus(superiorPlayer);
        MenuMembers.refreshMenus();

        Query.ISLAND_SET_MEMBERS.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(members))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer){
        return owner.equals(superiorPlayer.getIslandLeader());
    }

    public boolean checkMember(SuperiorPlayer superiorPlayer){
        return superiorPlayer == owner || members.readAndGet(members -> members.contains(superiorPlayer));
    }

    public void addMemberRaw(SuperiorPlayer superiorPlayer){
        members.write(members -> members.add(superiorPlayer));
        Query.ISLAND_SET_MEMBERS.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(members))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer){
        SuperiorSkyblockPlugin.debug("Action: Ban Player, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        banned.write(banned -> banned.add(superiorPlayer));

        if (isMember(superiorPlayer))
            kickMember(superiorPlayer);

        if (superiorPlayer.isOnline() && isInside(superiorPlayer.getLocation()))
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());

        Query.ISLAND_SET_BANNED.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(banned))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        SuperiorSkyblockPlugin.debug("Action: Unban Player, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        banned.write(banned -> banned.remove(superiorPlayer));
        Query.ISLAND_SET_BANNED.getStatementHolder(this)
                .setString(IslandSerializer.serializePlayers(banned))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer){
        return banned.readAndGet(banned -> banned.contains(superiorPlayer));
    }

    @Override
    public void addCoop(SuperiorPlayer superiorPlayer) {
        SuperiorSkyblockPlugin.debug("Action: Coop, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        coop.write(coop -> coop.add(superiorPlayer));
    }

    @Override
    public void removeCoop(SuperiorPlayer superiorPlayer) {
        SuperiorSkyblockPlugin.debug("Action: Uncoop, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        coop.write(coop -> coop.remove(superiorPlayer));

        if (isLocked() && superiorPlayer.isOnline() && isInside(superiorPlayer.getLocation())) {
            SuperiorMenu.killMenu(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }
    }

    @Override
    public boolean isCoop(SuperiorPlayer superiorPlayer) {
        return coop.readAndGet(coop -> coop.contains(superiorPlayer));
    }

    @Override
    public List<SuperiorPlayer> getCoopPlayers() {
        return coop.readAndGet(ArrayList::new);
    }

    @Override
    public int getCoopLimit() {
        return this.coopLimit.get();
    }

    @Override
    public void setCoopLimit(int coopLimit) {
        SuperiorSkyblockPlugin.debug("Action: Set Coop Limit, Island: " + owner.getName() + ", Coop Limit: " + coopLimit);
        this.coopLimit.set(coopLimit);
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder(this)
                .setInt(coopLimit)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setPlayerInside(SuperiorPlayer superiorPlayer, boolean inside) {
        playersInside.write(playersInside -> {
            if (inside)
                playersInside.add(superiorPlayer);
            else
                playersInside.remove(superiorPlayer);
        });

        if(inside){
            SuperiorSkyblockPlugin.debug("Action: Entered Island, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        }
        else{
            SuperiorSkyblockPlugin.debug("Action: Left Island, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName());
        }

        if(inside && !isMember(superiorPlayer)){
            boolean newVisitor = uniqueVisitors.writeAndGet(uniqueVisitors ->
                    !uniqueVisitors.contains(superiorPlayer) && uniqueVisitors.add(superiorPlayer));

            if(newVisitor){
                Query.ISLAND_SET_VISITORS.getStatementHolder(this)
                        .setString(IslandSerializer.serializePlayers(uniqueVisitors))
                        .setString(owner.getUniqueId().toString())
                        .execute(true);

                MenuUniqueVisitors.refreshMenus();
            }
        }

        updateLastTime();

        MenuVisitors.refreshMenus();
    }

    @Override
    public boolean isVisitor(SuperiorPlayer superiorPlayer, boolean includeCoopStatus) {
        return !isMember(superiorPlayer) && (!includeCoopStatus || !isCoop(superiorPlayer));
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

        Preconditions.checkNotNull(world, "Couldn't find world for environment " + environment + ".");

        return center.parse(world).add(0.5, 0, 0.5);
    }

    @Override
    public Location getVisitorsLocation() {
        return visitorsLocation.readAndGet(location -> location == null ? null : location.clone());
    }

    @Override
    @Deprecated
    public Location getTeleportLocation() {
        return getTeleportLocation(World.Environment.NORMAL);
    }

    @Override
    public Location getTeleportLocation(World.Environment environment) {
        Location teleportLocation = teleportLocations.get(environment);

        if (teleportLocation == null)
            teleportLocation = getCenter(environment);

        return teleportLocation == null ? null : teleportLocation.clone();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        SuperiorSkyblockPlugin.debug("Action: Change Teleport Location, Island: " + owner.getName() + ", Location: " + LocationUtils.getLocation(teleportLocation));
        teleportLocations.add(teleportLocation.getWorld().getEnvironment(), teleportLocation.clone());
        Query.ISLAND_SET_TELEPORT_LOCATION.getStatementHolder(this)
                .setString(IslandSerializer.serializeLocations(teleportLocations))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setVisitorsLocation(Location visitorsLocation) {
        this.visitorsLocation.set(visitorsLocation);

        if(visitorsLocation == null){
            deleteWarp(VISITORS_WARP_NAME);
            SuperiorSkyblockPlugin.debug("Action: Delete Visitors Location, Island: " + owner.getName());
        }
        else{
            setWarpLocation(VISITORS_WARP_NAME, visitorsLocation, false);
            SuperiorSkyblockPlugin.debug("Action: Change Visitors Location, Island: " + owner.getName() + ", Location: " + LocationUtils.getLocation(visitorsLocation));
        }

        Query.ISLAND_SET_VISITORS_LOCATION.getStatementHolder(this)
                .setString(LocationUtils.getLocation(getVisitorsLocation()))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public Location getMinimum(){
        int islandDistance = (int) Math.round(plugin.getSettings().maxIslandSize * (plugin.getSettings().buildOutsideIsland ? 1.5 : 1D));
        return getCenter(World.Environment.NORMAL).subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMinimumProtected() {
        int islandSize = getIslandSize();
        return getCenter(World.Environment.NORMAL).subtract(islandSize, 0, islandSize);
    }

    @Override
    public Location getMaximum(){
        int islandDistance = (int) Math.round(plugin.getSettings().maxIslandSize * (plugin.getSettings().buildOutsideIsland ? 1.5 : 1D));
        return getCenter(World.Environment.NORMAL).add(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximumProtected() {
        int islandSize = getIslandSize();
        return getCenter(World.Environment.NORMAL).add(islandSize, 0, islandSize);
    }

    @Override
    public List<Chunk> getAllChunks() {
        return getAllChunks(false);
    }

    @Override
    public List<Chunk> getAllChunks(boolean onlyProtected){
        List<Chunk> chunks = new ArrayList<>();

        for(World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getAllChunks(environment, onlyProtected));
            }catch(NullPointerException ignored){}
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

        for(World.Environment environment : World.Environment.values()) {
            try {
                chunks.addAll(getLoadedChunks(environment, onlyProtected, noEmptyChunks));
            }catch(NullPointerException ignored){}
        }

        return chunks;
    }

    @Override
    public List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getChunkCoords(this, world, onlyProtected, noEmptyChunks).stream()
                .map(plugin.getNMSBlocks()::getChunkIfLoaded).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, BiConsumer<Chunk, Throwable> whenComplete) {
        return getAllChunksAsync(environment, onlyProtected, false, whenComplete);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, Consumer<Chunk> onChunkLoad) {
        return getAllChunksAsync(environment, onlyProtected, false, onChunkLoad);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, BiConsumer<Chunk, Throwable> whenComplete) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getAllChunksAsync(this, world, onlyProtected, noEmptyChunks, whenComplete);
    }

    @Override
    public List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, Consumer<Chunk> onChunkLoad) {
        World world = getCenter(environment).getWorld();
        return IslandUtils.getAllChunksAsync(this, world, onlyProtected, noEmptyChunks, onChunkLoad);
    }

    @Override
    public void resetChunks(World.Environment environment, boolean onlyProtected) {
        World world = getCenter(environment).getWorld();
        IslandUtils.getChunkCoords(this,world, onlyProtected, true)
                .forEach(chunkPosition -> plugin.getNMSBlocks().deleteChunk(this, chunkPosition));
    }

    @Override
    public void resetChunks(boolean onlyProtected) {
        IslandUtils.getChunkCoords(this, onlyProtected, true)
                .forEach(chunkPosition -> plugin.getNMSBlocks().deleteChunk(this, chunkPosition));
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
        return isInsideRange(location, 0);
    }

    public boolean isInsideRange(Location location, int extra){
        if(!plugin.getGrid().isIslandsWorld(location.getWorld()))
            return false;

        Location min = getMinimumProtected(), max = getMaximumProtected();
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @Override
    public boolean isInsideRange(Chunk chunk) {
        if(!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return false;

        Location min = getMinimumProtected(), max = getMaximumProtected();
        return (min.getBlockX() >> 4) <= chunk.getX() && (min.getBlockZ() >> 4) <= chunk.getZ() &&
                (max.getBlockX() >> 4) >= chunk.getX() &&(max.getBlockZ() >> 4) >= chunk.getZ();
    }

    @Override
    public boolean isNetherEnabled() {
        return plugin.getSettings().netherWorldUnlocked || (unlockedWorlds.get() & 1) == 1;
    }

    @Override
    public void setNetherEnabled(boolean enabled) {
        int unlockedWorlds = this.unlockedWorlds.get();

        if(enabled){
            SuperiorSkyblockPlugin.debug("Action: Enable Nether, Island: " + owner.getName());
            unlockedWorlds |= 1;
        }
        else {
            SuperiorSkyblockPlugin.debug("Action: Disable Nether, Island: " + owner.getName());
            unlockedWorlds &= 2;
        }

        this.unlockedWorlds.set(unlockedWorlds);

        Query.ISLAND_SET_UNLOCK_WORLDS.getStatementHolder(this)
                .setString(unlockedWorlds + "")
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isEndEnabled() {
        return plugin.getSettings().endWorldUnlocked || (unlockedWorlds.get() & 2) == 2;
    }

    @Override
    public void setEndEnabled(boolean enabled) {
        int unlockedWorlds = this.unlockedWorlds.get();

        if(enabled){
            SuperiorSkyblockPlugin.debug("Action: Enable End, Island: " + owner.getName());
            unlockedWorlds |= 2;
        }
        else {
            SuperiorSkyblockPlugin.debug("Action: Disable End, Island: " + owner.getName());
            unlockedWorlds &= 1;
        }

        this.unlockedWorlds.set(unlockedWorlds);

        Query.ISLAND_SET_UNLOCK_WORLDS.getStatementHolder(this)
                .setString(unlockedWorlds + "")
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Permissions related methods
     */

    @Override
    @Deprecated
    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission) {
        return hasPermission(sender, IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    @Deprecated
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return hasPermission(superiorPlayer, IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPrivilege islandPrivilege){
        return sender instanceof ConsoleCommandSender || hasPermission(SSuperiorPlayer.of(sender), islandPrivilege);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege){
        PermissionNode playerNode = getPermissionNode(superiorPlayer);
        return superiorPlayer.hasBypassModeEnabled() || superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass.*") ||
                superiorPlayer.hasPermissionWithoutOP("superior.admin.bypass." + islandPrivilege.getName()) ||
                (playerNode != null && playerNode.hasPermission(islandPrivilege));
    }

    @Override
    public boolean hasPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege) {
        return getRequiredPlayerRole(islandPrivilege).getWeight() <= playerRole.getWeight();
    }

    @Override
    @Deprecated
    public void setPermission(PlayerRole playerRole, IslandPermission islandPermission, boolean value) {
        setPermission(playerRole, IslandPrivilege.getByName(islandPermission.name()), value);
    }

    @Override
    @Deprecated
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {
        setPermission(superiorPlayer, IslandPrivilege.getByName(islandPermission.name()), value);
    }

    @Override
    public void setPermission(PlayerRole playerRole, IslandPrivilege islandPrivilege, boolean value) {
        SuperiorSkyblockPlugin.debug("Action: Set Permission, Island: " + owner.getName() + ", Role: " + playerRole + ", Permission: " + islandPrivilege.getName() + ", Value: " + value);
        if(value) {
            rolePermissions.add(islandPrivilege, playerRole);

            if(islandPrivilege == IslandPrivileges.FLY){
                for(SuperiorPlayer targetPlayer : getAllPlayersInside()){
                    Player player = targetPlayer.asPlayer();
                    if(!player.isFlying() && targetPlayer.hasIslandFlyEnabled() && hasPermission(targetPlayer, IslandPrivileges.FLY)){
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        Locale.ISLAND_FLY_ENABLED.send(player);
                    }
                    else if(player.isFlying() && !hasPermission(targetPlayer, IslandPrivileges.FLY)){
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        Locale.ISLAND_FLY_DISABLED.send(player);
                    }
                }
            }

            savePermissionNodes();
        }
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPrivilege islandPrivilege, boolean value) {
        SuperiorSkyblockPlugin.debug("Action: Set Permission, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Permission: " + islandPrivilege.getName() + ", Value: " + value);

        if(!playerPermissions.containsKey(superiorPlayer))
            playerPermissions.add(superiorPlayer, new PlayerPermissionNode(superiorPlayer, this));

        playerPermissions.get(superiorPlayer).setPermission(islandPrivilege, value);

        if(islandPrivilege == IslandPrivileges.FLY){
            Player player = superiorPlayer.asPlayer();
            if(!player.isFlying() && value){
                player.setAllowFlight(true);
                player.setFlying(true);
                Locale.ISLAND_FLY_ENABLED.send(player);
            }
            else if(player.isFlying() && !value){
                player.setAllowFlight(false);
                player.setFlying(false);
                Locale.ISLAND_FLY_DISABLED.send(player);
            }
        }

        savePermissionNodes();

        MenuPermissions.refreshMenus();
    }

    @Override
    public PermissionNodeAbstract getPermissionNode(PlayerRole playerRole) {
        SuperiorSkyblockPlugin.log("&cIt seems like a plugin developer is using a deprecated method. Please inform him about it.");
        new Throwable().printStackTrace();
        return RolePermissionNode.EmptyRolePermissionNode.INSTANCE;
    }

    @Override
    public PermissionNodeAbstract getPermissionNode(SuperiorPlayer superiorPlayer) {
        return playerPermissions.get(superiorPlayer, new PlayerPermissionNode(superiorPlayer, this));
    }

    @Override
    @Deprecated
    public PlayerRole getRequiredPlayerRole(IslandPermission islandPermission) {
        return getRequiredPlayerRole(IslandPrivilege.getByName(islandPermission.name()));
    }

    @Override
    public PlayerRole getRequiredPlayerRole(IslandPrivilege islandPrivilege) {
        PlayerRole playerRole = rolePermissions.get(islandPrivilege);

        if(playerRole != null)
            return playerRole;

        return plugin.getPlayers().getRoles().stream()
                .filter(_playerRole -> ((SPlayerRole) _playerRole).getDefaultPermissions().hasPermission(islandPrivilege))
                .min(Comparator.comparingInt(PlayerRole::getWeight)).orElse(SPlayerRole.lastRole());
    }

    public void savePermissionNodes(){
        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder(this)
                .setString(IslandSerializer.serializePermissions(playerPermissions, rolePermissions))
                .setString(owner.getUniqueId().toString())
                .execute(true);
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
        return plugin.getSettings().islandNamesColorSupport ? islandName.get() : islandRawName.get();
    }

    @Override
    public String getRawName(){
        return islandRawName.get();
    }

    @Override
    public void setName(String islandName) {
        SuperiorSkyblockPlugin.debug("Action: Set Name, Island: " + owner.getName() + ", Name: " + islandName);
        this.islandName.set(islandName);
        this.islandRawName.set(StringUtils.stripColors(islandName));
        Query.ISLAND_SET_NAME.getStatementHolder(this)
                .setString(islandName)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public void setDescription(String description) {
        SuperiorSkyblockPlugin.debug("Action: Set Desrciption, Island: " + owner.getName() + ", Description: " + description);
        this.description.set(description);
        Query.ISLAND_SET_DESCRIPTION.getStatementHolder(this)
                .setString(description)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void disbandIsland(){
        getIslandMembers(true).forEach(superiorPlayer -> {
            if (isMember(superiorPlayer))
                kickMember(superiorPlayer);

            if (plugin.getSettings().disbandInventoryClear)
                plugin.getNMSAdapter().clearInventory(superiorPlayer.asOfflinePlayer());

            plugin.getMissions().getAllMissions().stream().filter(mission -> {
                MissionsHandler.MissionData missionData = plugin.getMissions().getMissionData(mission).orElse(null);
                return missionData != null && missionData.disbandReset;
            }).forEach(superiorPlayer::resetMission);
        });

        plugin.getMissions().getAllMissions().forEach(this::resetMission);

        resetChunks(true);

        plugin.getGrid().deleteIsland(this);
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

        long lastUpdateTime =  getLastTimeUpdate();

        if(lastUpdateTime != -1 && (System.currentTimeMillis() / 1000) - lastUpdateTime >= 600){
            finishCalcIsland(asker, callback, getIslandLevel(), getWorth());
            return;
        }

        beingRecalculated.set(true);

        SuperiorSkyblockPlugin.debug("Action: Calculate Island, Island: " + owner.getName() + ", Target: " + (asker == null ? "Null" : asker.getName()));

        List<CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>>> chunksToLoad;
        BlocksProvider_WildStacker.WildStackerSnapshot snapshot = plugin.getProviders().isWildStacker() ?
                new BlocksProvider_WildStacker.WildStackerSnapshot() : null;

        if(snapshot == null) {
            chunksToLoad = IslandUtils.getChunkCoords(this, true, true).stream()
                    .map(chunkPosition -> plugin.getNMSBlocks().calculateChunk(chunkPosition))
                    .collect(Collectors.toList());
        }
        else{
            chunksToLoad = new ArrayList<>();
            IslandUtils.getAllChunksAsync(this, true, true, snapshot::cacheChunk).forEach(completableFuture -> {
                CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> calculateCompletable = new CompletableFuture<>();
                completableFuture.whenComplete((chunk, ex) -> plugin.getNMSBlocks().calculateChunk(ChunkPosition.of(chunk)).whenComplete(
                        (pair, ex2) -> calculateCompletable.complete(pair)));
                chunksToLoad.add(calculateCompletable);
            });
        }

        BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();
        SyncedObject<KeyMap<Integer>> blockCounts = SyncedObject.of(new KeyMap<>());
        SyncedObject<BigDecimalFormatted> islandWorth = SyncedObject.of(BigDecimalFormatted.ZERO);
        SyncedObject<BigDecimalFormatted> islandLevel = SyncedObject.of(BigDecimalFormatted.ZERO);

        Executor.async(() -> {
            Set<Pair<Location, Integer>> spawnersToCheck = new HashSet<>();
            Map<Location, Pair<Integer, ItemStack>> blocksToCheck = new HashMap<>();

            for(CompletableFuture<BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>>> chunkInfoFuture : chunksToLoad){
                BiPair<ChunkPosition, KeyMap<Integer>, Set<Location>> chunkInfo;

                try{
                    chunkInfo = chunkInfoFuture.get();
                }catch (Exception ex){
                    SuperiorSkyblockPlugin.log("&cCouldn't load chunk!");
                    ex.printStackTrace();
                    continue;
                }

                // Load block counts
                handleBlocksPlace(chunkInfo.getY(), false, blockCounts, islandWorth, islandLevel);

                // Load spawners
                for(Location location : chunkInfo.getZ()){
                    Pair<Integer, String> spawnerInfo = snapshot != null ? snapshot.getSpawner(location) : plugin.getProviders().getSpawner(location);
                    if(spawnerInfo.getValue() == null){
                        spawnersToCheck.add(new Pair<>(location, spawnerInfo.getKey()));
                    }
                    else{
                        Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType().name() + ":" + spawnerInfo.getValue(), location);
                        handleBlockPlace(spawnerKey, spawnerInfo.getKey(), false, blockCounts, islandWorth, islandLevel);
                    }
                }

                // Load stacked blocks
                if(snapshot == null){
                    for(Pair<Integer, com.bgsoftware.superiorskyblock.api.key.Key> pair : plugin.getProviders().getBlocks(chunkInfo.getX()))
                        handleBlockPlace(pair.getValue(), pair.getKey() - 1, false, blockCounts, islandWorth, islandLevel);
                }
                else for(Pair<Integer, ItemStack> stackedBlock : snapshot.getBlocks(chunkInfo.getX())){
                    handleBlockPlace(Key.of(stackedBlock.getValue()), stackedBlock.getKey() - 1, false,
                            blockCounts, islandWorth, islandLevel);
                }

                for(Pair<Integer, Key> pair : plugin.getGrid().getBlockAmounts(chunkInfo.getX()))
                    handleBlockPlace(pair.getValue(), pair.getKey() - 1, false, blockCounts, islandWorth, islandLevel);
            }

            Executor.sync(() -> {
                Key blockKey;
                int blockCount;

                for(Pair<Location, Integer> pair : spawnersToCheck){
                    try {
                        CreatureSpawner creatureSpawner = (CreatureSpawner) pair.getKey().getBlock().getState();
                        blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + ":" + creatureSpawner.getSpawnedType(), pair.getKey());
                        blockCount = pair.getValue();
                        if(blockCount <= 0) {
                            Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawner(pair.getKey());
                            blockCount = spawnerInfo.getKey();
                            blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + ":" + spawnerInfo.getValue(), pair.getKey());
                        }
                        handleBlockPlace(blockKey, blockCount, false, blockCounts, islandWorth, islandLevel);
                    }catch(Throwable ignored){}
                }
                spawnersToCheck.clear();

                this.blockCounts.write(_blockCounts -> {
                    _blockCounts.clear();
                    blockCounts.read(_blockCounts::putAll);
                });

                this.islandWorth.set(islandWorth.get());
                this.islandLevel.set(islandLevel.get());

                BigDecimal newIslandLevel = getIslandLevel();
                BigDecimal newIslandWorth = getWorth();

                finishCalcIsland(asker, callback, newIslandLevel, newIslandWorth);

                if(snapshot != null)
                    snapshot.delete();

                MenuValues.refreshMenus();
                MenuCounts.refreshMenus();

                saveBlockCounts(oldWorth, oldLevel);

                beingRecalculated.set(false);
            });
        });
    }

    @Override
    public void updateBorder() {
        SuperiorSkyblockPlugin.debug("Action: Update Border, Island: " + owner.getName());
        getAllPlayersInside().forEach(superiorPlayer -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, this));
    }

    @Override
    public int getIslandSize() {
        if(plugin.getSettings().buildOutsideIsland)
            return (int) Math.round(plugin.getSettings().maxIslandSize * 1.5);

        return this.islandSize.get();
    }

    @Override
    public void setIslandSize(int islandSize) {
        SuperiorSkyblockPlugin.debug("Action: Set Size, Island: " + owner.getName() + ", Size: " + islandSize);

        // First, we want to remove all the current crop tile entities
        getLoadedChunks(true, false).forEach(chunk ->
                plugin.getNMSBlocks().startTickingChunk(this, chunk, true));

        this.islandSize.set(islandSize);

        // Now, we want to update the tile entities again
        getLoadedChunks(true, false).forEach(chunk ->
                plugin.getNMSBlocks().startTickingChunk(this, chunk, false));

        Query.ISLAND_SET_SIZE.getStatementHolder(this)
                .setInt(islandSize)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getDiscord() {
        return discord.get();
    }

    @Override
    public void setDiscord(String discord) {
        SuperiorSkyblockPlugin.debug("Action: Set Discord, Island: " + owner.getName() + ", Discord: " + discord);
        this.discord.set(discord);
        Query.ISLAND_SET_DISCORD.getStatementHolder(this)
                .setString(discord)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getPaypal() {
        return paypal.get();
    }

    @Override
    public void setPaypal(String paypal) {
        SuperiorSkyblockPlugin.debug("Action: Set Paypal, Island: " + owner.getName() + ", Paypal: " + paypal);
        this.paypal.set(paypal);
        Query.ISLAND_SET_PAYPAL.getStatementHolder(this)
                .setString(paypal)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public Biome getBiome() {
        return biome.get();
    }

    @Override
    public void setBiome(Biome biome){
        SuperiorSkyblockPlugin.debug("Action: Set Biome, Island: " + owner.getName() + ", Biome: " + biome.name());
        List<Player> playersToUpdate = getAllPlayersInside().stream().map(SuperiorPlayer::asPlayer).collect(Collectors.toList());

        {
            World normalWorld = getCenter(World.Environment.NORMAL).getWorld();
            IslandUtils.getChunkCoords(this, normalWorld, false, false).forEach(chunkPosition ->
                    plugin.getNMSBlocks().setChunkBiome(chunkPosition, biome, playersToUpdate));
        }

        if(plugin.getSettings().netherWorldEnabled && wasSchematicGenerated(World.Environment.NETHER)){
            World netherWorld = getCenter(World.Environment.NETHER).getWorld();
            Biome netherBiome = ServerVersion.isLegacy() ? Biome.HELL :
                    ServerVersion.isEquals(ServerVersion.v1_16) ? Biome.valueOf("NETHER_WASTES") : Biome.valueOf("NETHER");
            IslandUtils.getChunkCoords(this, netherWorld, false, false).forEach(chunkPosition ->
                    plugin.getNMSBlocks().setChunkBiome(chunkPosition, netherBiome, playersToUpdate));
        }

        if(plugin.getSettings().endWorldEnabled && wasSchematicGenerated(World.Environment.THE_END)){
            World endWorld = getCenter(World.Environment.THE_END).getWorld();
            Biome endBiome = ServerVersion.isLegacy() ? Biome.SKY : Biome.valueOf("THE_END");
            IslandUtils.getChunkCoords(this, endWorld, false, false).forEach(chunkPosition ->
                    plugin.getNMSBlocks().setChunkBiome(chunkPosition, endBiome, playersToUpdate));
        }

        for(World registeredWorld : plugin.getGrid().getRegisteredWorlds()){
            IslandUtils.getChunkCoords(this, registeredWorld, false, false).forEach(chunkPosition ->
                    plugin.getNMSBlocks().setChunkBiome(chunkPosition, biome, playersToUpdate));
        }

        setBiomeRaw(biome);
    }

    public void setBiomeRaw(Biome biome){
        this.biome.set(biome);
    }

    @Override
    public boolean isLocked() {
        return locked.get();
    }

    @Override
    public void setLocked(boolean locked) {
        SuperiorSkyblockPlugin.debug("Action: Set Locked, Island: " + owner.getName() + ", Locked: " + locked);
        this.locked.set(locked);
        if(locked){
            for(SuperiorPlayer victimPlayer : getAllPlayersInside()){
                if(!hasPermission(victimPlayer, IslandPrivileges.CLOSE_BYPASS)){
                    victimPlayer.teleport(plugin.getGrid().getSpawnIsland());
                    Locale.ISLAND_WAS_CLOSED.send(victimPlayer);
                }
            }
        }

        Query.ISLAND_SET_LOCKED.getStatementHolder(this)
                .setBoolean(locked)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean isIgnored() {
        return ignored.get();
    }

    @Override
    public void setIgnored(boolean ignored) {
        SuperiorSkyblockPlugin.debug("Action: Set Ignored, Island: " + owner.getName() + ", Ignored: " + ignored);
        this.ignored.set(ignored);

        Query.ISLAND_SET_IGNORED.getStatementHolder(this)
                .setBoolean(ignored)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        if(superiorPlayer.equals(owner))
            return false;

        SuperiorPlayer previousOwner = getOwner();

        if(!EventsCaller.callIslandTransferEvent(this, previousOwner, superiorPlayer))
            return false;

        SuperiorSkyblockPlugin.debug("Action: Transfer Owner, Island: " + owner.getName() + ", New Owner: " + superiorPlayer.getName());

        executeDeleteStatement(true);

        //Kick member without saving to database
        members.write(members -> members.remove(superiorPlayer));

        superiorPlayer.setPlayerRole(SPlayerRole.lastRole());

        //Add member without saving to database
        members.write(members -> members.add(previousOwner));

        PlayerRole previousRole = SPlayerRole.lastRole().getPreviousRole();
        previousOwner.setPlayerRole(previousRole == null ? SPlayerRole.lastRole() : previousRole);

        //Changing owner of the island and updating all players
        owner = superiorPlayer;
        getIslandMembers(true).forEach(islandMember -> islandMember.setIslandLeader(owner));

        executeInsertStatement(true);

        plugin.getGrid().transferIsland(previousOwner.getUniqueId(), owner.getUniqueId());

        plugin.getMissions().getAllMissions().forEach(mission -> mission.transferData(previousOwner, owner));

        return true;
    }

    public void replacePlayers(SuperiorPlayer originalPlayer, SuperiorPlayer newPlayer){
        boolean executeUpdate = false;

        if(owner == originalPlayer) {
            executeDeleteStatement(true);
            owner = newPlayer;
            getIslandMembers(true).forEach(islandMember -> islandMember.setIslandLeader(owner));
            executeInsertStatement(true);
            plugin.getGrid().transferIsland(originalPlayer.getUniqueId(), owner.getUniqueId());
        }
        else if(isMember(originalPlayer)){
            members.write(members -> {
                members.remove(originalPlayer);
                members.add(newPlayer);
            });
            executeUpdate = true;
        }

        PlayerPermissionNode playerPermissionNode = playerPermissions.remove(originalPlayer);
        if(playerPermissionNode != null){
            playerPermissions.add(newPlayer, playerPermissionNode);
            executeUpdate = true;
        }

        if(executeUpdate)
            executeUpdateStatement(true);
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers){
        SuperiorSkyblockPlugin.debug("Action: Send Message, Island: " + owner.getName() + ", Ignored Members: " + Arrays.asList(ignoredMembers) + ", Message: " + message);

        List<UUID> ignoredList = Arrays.asList(ignoredMembers);

        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredList.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> Locale.sendMessage(superiorPlayer, message, false));
    }

    public void sendMessage(Locale message, List<UUID> ignoredMembers, Object... args){
        getIslandMembers(true).stream()
                .filter(superiorPlayer -> !ignoredMembers.contains(superiorPlayer.getUniqueId()) && superiorPlayer.isOnline())
                .forEach(superiorPlayer -> message.send(superiorPlayer, args));
    }

    @Override
    public boolean isBeingRecalculated() {
        return beingRecalculated.get();
    }

    @Override
    public void updateLastTime() {
        this.lastTimeUpdate.write(lastTimeUpdate -> {
            if(lastTimeUpdate != -1) {
                setLastTimeUpdate(System.currentTimeMillis() / 1000);
            }
        });
    }

    @Override
    public long getLastTimeUpdate() {
        return lastTimeUpdate.get();
    }

    public void setLastTimeUpdate(long lastTimeUpdate){
        SuperiorSkyblockPlugin.debug("Action: Update Last Time, Island: " + owner.getName() + ", Last Time: " + lastTimeUpdate);
        this.lastTimeUpdate.set(lastTimeUpdate);
        if(lastTimeUpdate != -1){
            Query.ISLAND_SET_LAST_TIME_UPDATE.getStatementHolder(this)
                    .setLong(lastTimeUpdate)
                    .setString(owner.getUniqueId() + "")
                    .execute(true);
        }
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
        return islandBank.writeAndGet(islandBank -> {
            if(islandBank.doubleValue() < 0) {
                islandBank = BigDecimalFormatted.ZERO;
                this.islandBank.set(islandBank);
            }

            return islandBank;
        });
    }

    @Override
    public void depositMoney(double amount){
        depositMoney(BigDecimal.valueOf(amount));
    }

    @Override
    public void depositMoney(BigDecimal amount) {
        SuperiorSkyblockPlugin.debug("Action: Deposit Money, Island: " + owner.getName() + ", Money: " + amount);

        String islandBankString = this.islandBank.writeAndGet(islandBank -> {
            islandBank = islandBank.add(amount);
            this.islandBank.set(islandBank);
            return islandBank.getAsString();
        });

        Query.ISLAND_SET_BANK.getStatementHolder(this)
                .setString(islandBankString)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void withdrawMoney(double amount){
        withdrawMoney(BigDecimal.valueOf(amount));
    }

    @Override
    public void withdrawMoney(BigDecimal amount) {
        SuperiorSkyblockPlugin.debug("Action: Withdraw Money, Island: " + owner.getName() + ", Money: " + amount);

        String islandBankString = islandBank.writeAndGet(islandBank -> {
            islandBank = islandBank.subtract(amount);
            this.islandBank.set(islandBank);
            return islandBank.getAsString();
        });

        Query.ISLAND_SET_BANK.getStatementHolder(this)
                .setString(islandBankString)
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
    public void handleBlockPlace(com.bgsoftware.superiorskyblock.api.key.Key key, int amount){
        handleBlockPlace(key, amount, true);
    }

    @Override
    public void handleBlockPlace(com.bgsoftware.superiorskyblock.api.key.Key key, int amount, boolean save) {
        handleBlockPlace(key, amount, save, blockCounts, islandWorth, islandLevel);
    }

    private void handleBlockPlace(com.bgsoftware.superiorskyblock.api.key.Key key, int amount, boolean save, SyncedObject<KeyMap<Integer>> syncedBlockCounts, SyncedObject<BigDecimalFormatted> syncedIslandWorth, SyncedObject<BigDecimalFormatted> syncedIslandLevel){
        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean increaseAmount = false;

        BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();

        if(blockValue.doubleValue() >= 0){
            syncedIslandWorth.set(islandWorth -> islandWorth.add(blockValue.multiply(new BigDecimal(amount))));
            increaseAmount = true;
        }

        if(blockLevel.doubleValue() >= 0){
            syncedIslandLevel.set(islandLevel -> islandLevel.add(blockLevel.multiply(new BigDecimal(amount))));
            increaseAmount = true;
        }

        boolean hasBlockLimit = blockLimits.containsKey(key);

        if(increaseAmount || hasBlockLimit) {
            SuperiorSkyblockPlugin.debug("Action: Block Place, Island: " + owner.getName() + ", Block: " + key + ", Amount: " + amount);

            syncedBlockCounts.write(blockCounts -> addCounts(blockCounts, blockLimits, key, amount));

            updateLastTime();

            if(save){
                saveBlockCounts(oldWorth, oldLevel);
                if(++blocksUpdateCounter >= Bukkit.getOnlinePlayers().size() * 10){
                    blocksUpdateCounter = 0;
                    plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
                    plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
                    MenuTopIslands.refreshMenus();
                    MenuValues.refreshMenus();
                    MenuCounts.refreshMenus();
                }
            }
        }
    }

    public void handleBlocksPlace(KeyMap<Integer> blocks){
        handleBlocksPlace(blocks, false, this.blockCounts, this.islandWorth, this.islandLevel);
        saveBlockCounts();
        saveDirtyChunks();
    }

    public void handleBlocksPlace(KeyMap<Integer> blocks, boolean save, SyncedObject<KeyMap<Integer>> syncedBlockCounts, SyncedObject<BigDecimalFormatted> syncedIslandWorth, SyncedObject<BigDecimalFormatted> syncedIslandLevel){
        KeyMap<Integer> blockCounts = new KeyMap<>();
        BigDecimal blocksValues = BigDecimal.ZERO, blocksLevels = BigDecimal.ZERO;

        syncedBlockCounts.read(blockCounts::putAll);

        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, Integer> entry : blocks.entrySet()){
            BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(entry.getKey());
            BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(entry.getKey());

            boolean increaseAmount = false;

            BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();

            if(blockValue.doubleValue() >= 0){
                blocksValues = blocksValues.add(blockValue.multiply(new BigDecimal(entry.getValue())));
                increaseAmount = true;
            }

            if(blockLevel.doubleValue() >= 0){
                blocksLevels = blocksLevels.add(blockLevel.multiply(new BigDecimal(entry.getValue())));
                increaseAmount = true;
            }

            boolean hasBlockLimit = blockLimits.containsKey(entry.getKey());

            if(increaseAmount || hasBlockLimit) {
                SuperiorSkyblockPlugin.debug("Action: Block Place, Island: " + owner.getName() + ", Block: " + entry.getKey() + ", Amount: " + entry.getValue());
                addCounts(blockCounts, blockLimits, entry.getKey(), entry.getValue());
            }
        }

        BigDecimal BLOCKS_VALUES = blocksValues, BLOCKS_LEVELS = blocksLevels;

        syncedIslandWorth.set(islandWorth -> islandWorth.add(BLOCKS_VALUES));
        syncedIslandLevel.set(islandLevel -> islandLevel.add(BLOCKS_LEVELS));
        syncedBlockCounts.write(_blockCounts -> _blockCounts.putAll(blockCounts));

        if(save)
            saveBlockCounts(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private void addCounts(KeyMap<Integer> blockCounts, UpgradeKeyMap blockLimits, com.bgsoftware.superiorskyblock.api.key.Key key, int amount){
        Key valueKey = plugin.getBlockValues().getBlockKey(key);

        int currentAmount = blockCounts.getRaw(valueKey, 0);
        blockCounts.put(valueKey, currentAmount + amount);

        if(!rawKeyPlacements) {
            Key limitKey = blockLimits.getKey(valueKey);
            Key globalKey = Key.of(valueKey.getGlobalKey());
            boolean limitCount = false;

            if (!limitKey.equals(valueKey)) {
                currentAmount = blockCounts.getRaw(limitKey, 0);
                blockCounts.put(limitKey, currentAmount + amount);
                limitCount = true;
            }

            if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey)) &&
                    (plugin.getBlockValues().getBlockWorth(globalKey).doubleValue() >= 0 ||
                            plugin.getBlockValues().getBlockLevel(globalKey).doubleValue() >= 0)) {
                currentAmount = blockCounts.getRaw(globalKey, 0);
                blockCounts.put(globalKey, currentAmount + amount);
            }
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
    public void handleBlockBreak(com.bgsoftware.superiorskyblock.api.key.Key key, int amount){
        handleBlockBreak(key, amount, true);
    }

    @Override
    public void handleBlockBreak(com.bgsoftware.superiorskyblock.api.key.Key key, int amount, boolean save) {
        BigDecimal blockValue = plugin.getBlockValues().getBlockWorth(key);
        BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

        boolean decreaseAmount = false;

        BigDecimal oldWorth = getWorth(), oldLevel = getIslandLevel();

        if(blockValue.doubleValue() >= 0){
            BigDecimalFormatted islandWorth = this.islandWorth.get().subtract(blockValue.multiply(new BigDecimal(amount)));
            this.islandWorth.set(islandWorth);
            if(islandWorth.doubleValue() < 0)
                this.islandWorth.set(BigDecimalFormatted.ZERO);
            decreaseAmount = true;
        }

        if(blockLevel.doubleValue() >= 0){
            BigDecimalFormatted islandLevel = this.islandLevel.get().subtract(blockLevel.multiply(new BigDecimal(amount)));
            this.islandLevel.set(islandLevel);
            if(islandLevel.doubleValue() < 0)
                this.islandLevel.set(BigDecimalFormatted.ZERO);
            decreaseAmount = true;
        }

        boolean hasBlockLimit = blockLimits.containsKey(key);

        if(decreaseAmount || hasBlockLimit){
            SuperiorSkyblockPlugin.debug("Action: Block Break, Island: " + owner.getName() + ", Block: " + key);

            blockCounts.write(blockCounts -> {
                Key valueKey = plugin.getBlockValues().getBlockKey(key);
                removeCounts(blockCounts, valueKey, amount);

                com.bgsoftware.superiorskyblock.api.key.Key limitKey = blockLimits.getKey(valueKey);
                Key globalKey = Key.of(valueKey.getGlobalKey());
                boolean limitCount = false;

                if (!limitKey.equals(valueKey)) {
                    removeCounts(blockCounts, limitKey, amount);
                    limitCount = true;
                }

                if (!globalKey.equals(valueKey) && (!limitCount || !globalKey.equals(limitKey)) &&
                        (plugin.getBlockValues().getBlockWorth(globalKey).doubleValue() >= 0 ||
                                plugin.getBlockValues().getBlockLevel(globalKey).doubleValue() >= 0)) {
                    removeCounts(blockCounts, globalKey, amount);
                }
            });

            updateLastTime();

            if(save) {
                saveBlockCounts(oldWorth, oldLevel);
                if(++blocksUpdateCounter >= Bukkit.getOnlinePlayers().size() * 10){
                    blocksUpdateCounter = 0;
                    plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
                    plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
                    MenuTopIslands.refreshMenus();
                    MenuValues.refreshMenus();
                    MenuCounts.refreshMenus();
                }
            }
        }
    }

    private void removeCounts(KeyMap<Integer> blockCounts, com.bgsoftware.superiorskyblock.api.key.Key key, int amount){
        int currentAmount = blockCounts.getRaw(key, 0);
        if(currentAmount <= amount)
            blockCounts.remove(key);
        else
            blockCounts.put(key, currentAmount - amount);
    }

    @Override
    public int getBlockCount(com.bgsoftware.superiorskyblock.api.key.Key key){
        return blockCounts.readAndGet(blockCounts -> blockCounts.getOrDefault(key, 0));
    }

    @Override
    public Map<com.bgsoftware.superiorskyblock.api.key.Key, Integer> getBlockCounts() {
        return blockCounts.readAndGet(blockCounts -> new HashMap<>(blockCounts.asKeyMap()));
    }

    public KeyMap<Integer> getRawBlockCounts(){
        return blockCounts.readAndGet(KeyMap::new);
    }

    @Override
    public int getExactBlockCount(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return blockCounts.readAndGet(blockCounts -> blockCounts.getRaw(key, 0));
    }

    @Override
    @Deprecated
    public BigDecimal getWorthAsBigDecimal() {
        return getWorth();
    }

    @Override
    public BigDecimal getWorth() {
        int bankWorthRate = plugin.getSettings().bankWorthRate;
        BigDecimalFormatted islandWorth = this.islandWorth.get(), islandBank = this.islandBank.get(), bonusWorth = this.bonusWorth.get();
        //noinspection BigDecimalMethodWithoutRoundingCalled
        BigDecimal finalIslandWorth = bankWorthRate <= 0 ? getRawWorth() : islandWorth.add(islandBank.divide(new BigDecimal(bankWorthRate)));

        finalIslandWorth = finalIslandWorth.add(bonusWorth);

        if(!plugin.getSettings().negativeWorth && finalIslandWorth.compareTo(BigDecimal.ZERO) < 0)
            finalIslandWorth = BigDecimalFormatted.of(0);

        return finalIslandWorth;
    }

    @Override
    @Deprecated
    public BigDecimal getRawWorthAsBigDecimal() {
        return getRawWorth();
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
    public void setBonusWorth(BigDecimal bonusWorth){
        SuperiorSkyblockPlugin.debug("Action: Set Bonus Worth, Island: " + owner.getName() + ", Bonus: " + bonusWorth);

        BigDecimalFormatted newBonusWorth = bonusWorth instanceof BigDecimalFormatted ? (BigDecimalFormatted) bonusWorth : BigDecimalFormatted.of(bonusWorth);
        this.bonusWorth.set(newBonusWorth);

        plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
        plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
        MenuTopIslands.refreshMenus();

        Query.ISLAND_SET_BONUS_WORTH.getStatementHolder(this)
                .setString(newBonusWorth.getAsString())
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public BigDecimal getBonusLevel() {
        return bonusLevel.get();
    }

    @Override
    public void setBonusLevel(BigDecimal bonusLevel) {
        SuperiorSkyblockPlugin.debug("Action: Set Bonus Level, Island: " + owner.getName() + ", Bonus: " + bonusLevel);

        BigDecimalFormatted newBonusLevel = bonusLevel instanceof BigDecimalFormatted ? (BigDecimalFormatted) bonusLevel : BigDecimalFormatted.of(bonusLevel);
        this.bonusLevel.set(newBonusLevel);

        plugin.getGrid().sortIslands(SortingTypes.BY_WORTH);
        plugin.getGrid().sortIslands(SortingTypes.BY_LEVEL);
        MenuTopIslands.refreshMenus();

        Query.ISLAND_SET_BONUS_LEVEL.getStatementHolder(this)
                .setString(newBonusLevel.getAsString())
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
        BigDecimalFormatted bonusLevel = this.bonusLevel.get(), islandLevel = this.islandLevel.get().add(bonusLevel);

        if(plugin.getSettings().roundedIslandLevel) {
            islandLevel = islandLevel.setScale(0, RoundingMode.HALF_UP);
        }

        if(!plugin.getSettings().negativeLevel && islandLevel.compareTo(BigDecimal.ZERO) < 0)
            islandLevel = BigDecimalFormatted.of(0);

        return islandLevel;
    }

    @Override
    public BigDecimal getRawLevel() {
        BigDecimalFormatted islandLevel = this.islandLevel.get();

        if(plugin.getSettings().roundedIslandLevel) {
            islandLevel = islandLevel.setScale(0, RoundingMode.HALF_UP);
        }

        if(!plugin.getSettings().negativeLevel && islandLevel.compareTo(BigDecimal.ZERO) < 0)
            islandLevel = BigDecimalFormatted.of(0);

        return islandLevel;
    }

    private void saveBlockCounts(BigDecimal oldWorth, BigDecimal oldLevel){
        BigDecimal newWorth = getWorth(), newLevel = getIslandLevel();

        if(oldLevel.compareTo(newLevel) != 0 || oldWorth.compareTo(newWorth) != 0) {
            Executor.async(() ->
                    EventsCaller.callIslandWorthUpdateEvent(this, oldWorth, oldLevel, newWorth, newLevel), 0L);
        }

        saveBlockCounts();
    }

    /*
     *  Upgrade related methods
     */

    @Override
    @Deprecated
    public int getUpgradeLevel(String upgradeName){
        return getUpgradeLevel(plugin.getUpgrades().getUpgrade(upgradeName)).getLevel();
    }

    @Override
    public UpgradeLevel getUpgradeLevel(Upgrade upgrade) {
        return upgrade.getUpgradeLevel(getUpgrades().getOrDefault(upgrade.getName(), 1));
    }

    @Override
    @Deprecated
    public void setUpgradeLevel(String upgradeName, int level){
        setUpgradeLevel(plugin.getUpgrades().getUpgrade(upgradeName), level);
    }

    @Override
    public void setUpgradeLevel(Upgrade upgrade, int level) {
        SuperiorSkyblockPlugin.debug("Action: Set Upgrade, Island: " + owner.getName() + ", Upgrade: " + upgrade.getName() + ", Level: " + level);

        upgrades.add(upgrade.getName(), Math.min(upgrade.getMaxUpgradeLevel(), level));
        Query.ISLAND_SET_UPGRADES.getStatementHolder(this)
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        UpgradeLevel upgradeLevel = getUpgradeLevel(upgrade);

        syncUpgrade(upgradeLevel);

        if(upgradeLevel.getBorderSize() != -1)
            updateBorder();

        MenuUpgrades.refreshMenus();
    }

    @Override
    public void syncUpgrades() {
        clearGeneratorAmounts();
        clearEffects();
        clearBlockLimits();
        clearEntitiesLimits();

        setCropGrowthMultiplier(-1D);
        setSpawnerRatesMultiplier(-1D);
        setMobDropsMultiplier(-1D);
        setTeamLimit(-1);
        setWarpsLimit(-1);
        setCoopLimit(-1);
        setIslandSize(-1);

        // We want to sync the default upgrade first, then the actual upgrades
        syncUpgrade(DefaultUpgradeLevel.getInstance());
        // Syncing all real upgrades
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> syncUpgrade(getUpgradeLevel(upgrade)));

        if(getIslandSize() != -1)
            updateBorder();
    }

    public Map<String, Integer> getUpgrades(){
        if(!upgrades.isEmpty())
            return upgrades.toMap();

        return plugin.getUpgrades().getUpgrades().stream().collect(Collectors.toMap(Upgrade::getName, upgrade -> 1));
    }

    public void updateUpgrades(){
        islandSize.clearUpgrade();
        blockLimits.clearUpgrades();
        entityLimits.clearUpgrades();
        warpsLimit.clearUpgrade();
        teamLimit.clearUpgrade();
        coopLimit.clearUpgrade();
        cropGrowth.clearUpgrade();
        spawnerRates.clearUpgrade();
        mobDrops.clearUpgrade();
        cobbleGeneratorValues.clearUpgrades();
        islandEffects.clearUpgrades();
        // We want to sync the default upgrade first, then the actual upgrades
        syncUpgrade(DefaultUpgradeLevel.getInstance());
        // Syncing all real upgrades
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> syncUpgrade(getUpgradeLevel(upgrade)));
    }

    @Override
    public double getCropGrowthMultiplier() {
        return cropGrowth.get();
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        SuperiorSkyblockPlugin.debug("Action: Set Crop Growth, Island: " + owner.getName() + ", Crop Growth: " + cropGrowth);
        this.cropGrowth.set(cropGrowth);
        Query.ISLAND_SET_CROP_GROWTH.getStatementHolder(this)
                .setDouble(cropGrowth)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return this.spawnerRates.get();
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        SuperiorSkyblockPlugin.debug("Action: Set Spawner Rates, Island: " + owner.getName() + ", Spawner Rates: " + spawnerRates);
        this.spawnerRates.set(spawnerRates);
        Query.ISLAND_SET_SPAWNER_RATES.getStatementHolder(this)
                .setDouble(spawnerRates)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public double getMobDropsMultiplier() {
        return this.mobDrops.get();
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        SuperiorSkyblockPlugin.debug("Action: Set Mob Drops, Island: " + owner.getName() + ", Mob Drops: " + mobDrops);
        this.mobDrops.set(mobDrops);
        Query.ISLAND_SET_MOB_DROPS.getStatementHolder(this)
                .setDouble(mobDrops)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getBlockLimit(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return blockLimits.get(key, NO_LIMIT);
    }

    @Override
    public int getExactBlockLimit(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return blockLimits.getRaw(key, NO_LIMIT);
    }

    @Override
    public Map<com.bgsoftware.superiorskyblock.api.key.Key, Integer> getBlocksLimits() {
        return this.blockLimits.copy();
    }

    @Override
    public void clearBlockLimits() {
        SuperiorSkyblockPlugin.debug("Action: Clear Block Limits, Island: " + owner.getName());
        blockLimits.clear();
        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setBlockLimit(com.bgsoftware.superiorskyblock.api.key.Key key, int limit) {
        SuperiorSkyblockPlugin.debug("Action: Set Block Limit, Island: " + owner.getName() + ", Block: " + key + ", Limit: " + limit);
        blockLimits.set(key ,limit);
        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public boolean hasReachedBlockLimit(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return hasReachedBlockLimit(key, 1);
    }

    @Override
    public boolean hasReachedBlockLimit(com.bgsoftware.superiorskyblock.api.key.Key key, int amount) {
        int blockLimit = getExactBlockLimit(key);

        //Checking for the specific provided key.
        if(blockLimit > SIsland.NO_LIMIT)
            return getBlockCount(key) + amount > blockLimit;

        //Getting the global key values.
        key = Key.of(key.getGlobalKey());
        blockLimit = getBlockLimit(key);

        return blockLimit > SIsland.NO_LIMIT && getBlockCount(key) + amount > blockLimit;
    }

    @Override
    public int getEntityLimit(EntityType entityType) {
        return this.entityLimits.get(entityType, NO_LIMIT);
    }

    @Override
    public Map<EntityType, Integer> getEntitiesLimits() {
        return this.entityLimits.copy();
    }

    @Override
    public void clearEntitiesLimits() {
        SuperiorSkyblockPlugin.debug("Action: Clear Entity Limit, Island: " + owner.getName());
        entityLimits.clear();
        Query.ISLAND_SET_ENTITY_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEntityLimits(entityLimits))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setEntityLimit(EntityType entityType, int limit) {
        SuperiorSkyblockPlugin.debug("Action: Set Entity Limit, Island: " + owner.getName() + ", Entity: " + entityType + ", Limit: " + limit);
        entityLimits.set(entityType, limit);
        Query.ISLAND_SET_ENTITY_LIMITS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEntityLimits(entityLimits))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType) {
        return hasReachedEntityLimit(entityType, 1);
    }

    @Override
    public CompletableFuture<Boolean> hasReachedEntityLimit(EntityType entityType, int amount) {
        List<CompletableFuture<Chunk>> chunks = new ArrayList<>();
        int entityLimit = getEntityLimit(entityType);

        for(World.Environment environment : World.Environment.values()){
            try{
                chunks.addAll(getAllChunksAsync(environment, true, true, (Consumer<Chunk>) null));
            }catch(Exception ignored){}
        }

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Executor.async(() -> {
            int amountOfEntities = 0;

            if(entityLimit <= SIsland.NO_LIMIT){
                completableFuture.complete(false);
                return;
            }

            for(CompletableFuture<Chunk> completableChunk : chunks){
                try{
                    Chunk chunk = completableChunk.get();
                    amountOfEntities += Arrays.stream(chunk.getEntities())
                            .filter(entity -> entityType == EntityUtils.getLimitEntityType(entity.getType()) &&
                                    !EntityUtils.canBypassEntityLimit(entity)).count();
                }catch(Exception ignored){}
            }

            completableFuture.complete(amountOfEntities + amount - 1 > entityLimit);
        });

        return completableFuture;
    }

    @Override
    public int getTeamLimit() {
        return this.teamLimit.get();
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        SuperiorSkyblockPlugin.debug("Action: Set Team Limit, Island: " + owner.getName() + ", Team Limit: " + teamLimit);
        this.teamLimit.set(teamLimit);
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder(this)
                .setInt(teamLimit)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getWarpsLimit() {
        return this.warpsLimit.get();
    }

    @Override
    public void setWarpsLimit(int warpsLimit) {
        SuperiorSkyblockPlugin.debug("Action: Set Warps Limit, Island: " + owner.getName() + ", Warps Limit: " + warpsLimit);

        this.warpsLimit.set(warpsLimit);
        Query.ISLAND_SET_WARPS_LIMIT.getStatementHolder(this)
                .setInt(warpsLimit)
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public void setPotionEffect(PotionEffectType type, int level) {
        SuperiorSkyblockPlugin.debug("Action: Set Island Effect, Island: " + owner.getName() + ", Effect: " + type.getName() + ", Level: " + level);

        if(level <= 0) {
            islandEffects.remove(type);
            Executor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().removePotionEffect(type)));
        }
        else {
            PotionEffect potionEffect = new PotionEffect(type, Integer.MAX_VALUE, level - 1);
            islandEffects.set(type, level - 1);
            Executor.ensureMain(() -> getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().addPotionEffect(potionEffect, true)));
        }


        Query.ISLAND_SET_ISLAND_EFFECTS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEffects(islandEffects))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getPotionEffectLevel(PotionEffectType type) {
        return islandEffects.get(type, -1) + 1;
    }

    @Override
    public Map<PotionEffectType, Integer> getPotionEffects() {
        Map<PotionEffectType, Integer> islandEffects = new HashMap<>();

        for(PotionEffectType potionEffectType : PotionEffectType.values()){
            int level = getPotionEffectLevel(potionEffectType);
            if(level > 0)
                islandEffects.put(potionEffectType, level);
        }

        return islandEffects;
    }

    @Override
    public void applyEffects(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        getPotionEffects().forEach((potionEffectType, level) -> player.addPotionEffect(new PotionEffect(potionEffectType, Integer.MAX_VALUE, level), true));
    }

    @Override
    public void removeEffects(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        getPotionEffects().keySet().forEach(player::removePotionEffect);
    }

    @Override
    public void removeEffects() {
        getAllPlayersInside().forEach(this::removeEffects);
    }

    @Override
    public void clearEffects() {
        SuperiorSkyblockPlugin.debug("Action: Clear Island Effects, Island: " + owner.getName());
        islandEffects.clear();
        removeEffects();
        Query.ISLAND_SET_ISLAND_EFFECTS.getStatementHolder(this)
                .setString(IslandSerializer.serializeEffects(islandEffects))
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
        SuperiorSkyblockPlugin.debug("Action: Set Warp, Island: " + owner.getName() + ", Name: " + name + ", Location: " + LocationUtils.getLocation(location) + ", Private: " + privateFlag);

        warps.add(name.toLowerCase(), new WarpData(location.clone(), privateFlag));

        Query.ISLAND_SET_WARPS.getStatementHolder(this)
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        MenuGlobalWarps.refreshMenus();
        MenuWarps.refreshMenus();
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp){
        if(plugin.getSettings().warpsWarmup > 0 && !superiorPlayer.hasBypassModeEnabled()) {
            Locale.TELEPORT_WARMUP.send(superiorPlayer, StringUtils.formatTime(superiorPlayer.getUserLocale(), plugin.getSettings().warpsWarmup));
            ((SSuperiorPlayer) superiorPlayer).setTeleportTask(Executor.sync(() ->
                    warpPlayerWithoutWarmup(superiorPlayer, warp), plugin.getSettings().warpsWarmup / 50));
        }
        else {
            warpPlayerWithoutWarmup(superiorPlayer, warp);
        }
    }

    private void warpPlayerWithoutWarmup(SuperiorPlayer superiorPlayer, String warp){
        Location location = warps.get(warp.toLowerCase()).location.clone();
        ((SSuperiorPlayer) superiorPlayer).setTeleportTask(null);

        if(!isInsideRange(location)){
            Locale.UNSAFE_WARP.send(superiorPlayer);
            deleteWarp(warp);
            return;
        }

        if(!LocationUtils.isSafeBlock(location.getBlock())){
            Locale.UNSAFE_WARP.send(superiorPlayer);
            return;
        }

        superiorPlayer.teleport(location, success -> {
            if(success)
                Locale.TELEPORTED_TO_WARP.send(superiorPlayer);
        });
    }

    @Override
    public void deleteWarp(SuperiorPlayer superiorPlayer, Location location){
        for(String warpName : warps.keys()){
            if(LocationUtils.isSameBlock(location, warps.get(warpName).location)){
                deleteWarp(warpName);
                if(superiorPlayer != null)
                    Locale.DELETE_WARP.send(superiorPlayer, warpName);
            }
        }
    }

    @Override
    public void deleteWarp(String name){
        SuperiorSkyblockPlugin.debug("Action: Delete Warp, Island: " + owner.getName() + ", Warp: " + name);

        warps.remove(name.toLowerCase());

        Query.ISLAND_SET_WARPS.getStatementHolder(this)
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        MenuGlobalWarps.refreshMenus();
        MenuWarps.refreshMenus();
    }

    @Override
    public List<String> getAllWarps(){
        return new ArrayList<>(warps.keys());
    }

    @Override
    public boolean hasMoreWarpSlots() {
        return warps.size() < getWarpsLimit();
    }

    /*
     *  Ratings related methods
     */

    @Override
    public Rating getRating(SuperiorPlayer superiorPlayer) {
        return ratings.get(superiorPlayer.getUniqueId(), Rating.UNKNOWN);
    }

    @Override
    public void setRating(SuperiorPlayer superiorPlayer, Rating rating) {
        SuperiorSkyblockPlugin.debug("Action: Set Rating, Island: " + owner.getName() + ", Target: " + superiorPlayer.getName() + ", Rating: " + rating);

        if(rating == Rating.UNKNOWN)
            ratings.remove(superiorPlayer.getUniqueId());
        else
            ratings.add(superiorPlayer.getUniqueId(), rating);

        Query.ISLAND_SET_RATINGS.getStatementHolder(this)
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        MenuIslandRatings.refreshMenus();
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
        return ratings.toMap();
    }

    /*
     *  Missions related methods
     */

    @Override
    public void completeMission(Mission<?> mission) {
        SuperiorSkyblockPlugin.debug("Action: Complete Mission, Island: " + owner.getName() + ", Mission: " + mission.getName());

        completedMissions.add(mission, completedMissions.get(mission, 0) + 1);

        Query.ISLAND_SET_MISSIONS.getStatementHolder(this)
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        MenuIslandMissions.refreshMenus();
    }

    @Override
    public void resetMission(Mission<?> mission) {
        SuperiorSkyblockPlugin.debug("Action: Reset Mission, Island: " + owner.getName() + ", Mission: " + mission.getName());

        if(completedMissions.get(mission, 0) > 0) {
            completedMissions.add(mission, completedMissions.get(mission) - 1);
        }

        else {
            completedMissions.remove(mission);
        }

        Query.ISLAND_SET_MISSIONS.getStatementHolder(this)
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        mission.clearData(getOwner());

        MenuIslandMissions.refreshMenus();
    }

    @Override
    public boolean hasCompletedMission(Mission<?> mission) {
        return completedMissions.get(mission, 0) > 0;
    }

    @Override
    public boolean canCompleteMissionAgain(Mission<?> mission) {
        Optional<MissionsHandler.MissionData> missionDataOptional = plugin.getMissions().getMissionData(mission);
        return missionDataOptional.isPresent() && getAmountMissionCompleted(mission) < missionDataOptional.get().resetAmount;
    }

    @Override
    public int getAmountMissionCompleted(Mission<?> mission) {
        return completedMissions.get(mission, 0);
    }

    @Override
    public List<Mission<?>> getCompletedMissions() {
        return new ArrayList<>(completedMissions.keys());
    }

    /*
     *  Settings related methods
     */

    @Override
    @Deprecated
    public boolean hasSettingsEnabled(IslandSettings islandSettings) {
        return hasSettingsEnabled(IslandFlag.getByName(islandSettings.name()));
    }

    @Override
    @Deprecated
    public void enableSettings(IslandSettings islandSettings) {
        enableSettings(IslandFlag.getByName(islandSettings.name()));
    }

    @Override
    @Deprecated
    public void disableSettings(IslandSettings islandSettings) {
        disableSettings(IslandFlag.getByName(islandSettings.name()));
    }

    @Override
    public boolean hasSettingsEnabled(IslandFlag settings) {
        return islandSettings.get(settings, (byte) (plugin.getSettings().defaultSettings.contains(settings.getName()) ? 1 : 0)) == 1;
    }

    @Override
    public void enableSettings(IslandFlag settings) {
        SuperiorSkyblockPlugin.debug("Action: Enable Settings, Island: " + owner.getName() + ", Settings: " + settings.getName());

        islandSettings.add(settings, (byte) 1);

        boolean disableTime = false, disableWeather = false;

        //Updating times / weather if necessary
        switch (settings.getName()){
            case "ALWAYS_DAY":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(0, false));
                disableTime = true;
                break;
            case "ALWAYS_MIDDLE_DAY":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(6000, false));
                disableTime = true;
                break;
            case "ALWAYS_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(14000, false));
                disableTime = true;
                break;
            case "ALWAYS_MIDDLE_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerTime(18000, false));
                disableTime = true;
                break;
            case "ALWAYS_SHINY":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerWeather(WeatherType.CLEAR));
                disableWeather = true;
                break;
            case "ALWAYS_RAIN":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().setPlayerWeather(WeatherType.DOWNFALL));
                disableWeather = true;
                break;
            case "PVP":
                if(plugin.getSettings().teleportOnPVPEnable)
                    getIslandVisitors().forEach(superiorPlayer -> {
                        superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                        Locale.ISLAND_GOT_PVP_ENABLED_WHILE_INSIDE.send(superiorPlayer);
                    });
                break;
        }

        if(disableTime){
            //Disabling settings without saving to database.
            if(settings != IslandFlags.ALWAYS_DAY)
                islandSettings.remove(IslandFlags.ALWAYS_DAY);
            if(settings != IslandFlags.ALWAYS_MIDDLE_DAY)
                islandSettings.remove(IslandFlags.ALWAYS_MIDDLE_DAY);
            if(settings != IslandFlags.ALWAYS_NIGHT)
                islandSettings.remove(IslandFlags.ALWAYS_NIGHT);
            if(settings != IslandFlags.ALWAYS_MIDDLE_NIGHT)
                islandSettings.remove(IslandFlags.ALWAYS_MIDDLE_NIGHT);
        }

        if(disableWeather){
            if(settings != IslandFlags.ALWAYS_RAIN)
                islandSettings.remove(IslandFlags.ALWAYS_RAIN);
            if(settings != IslandFlags.ALWAYS_SHINY)
                islandSettings.remove(IslandFlags.ALWAYS_SHINY);
        }

        Query.ISLAND_SET_SETTINGS.getStatementHolder(this)
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        MenuSettings.refreshMenus();
    }

    @Override
    public void disableSettings(IslandFlag settings) {
        SuperiorSkyblockPlugin.debug("Action: Disable Settings, Island: " + owner.getName() + ", Settings: " + settings.getName());

        islandSettings.add(settings, (byte) 0);

        Query.ISLAND_SET_SETTINGS.getStatementHolder(this)
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setString(owner.getUniqueId().toString())
                .execute(true);

        switch (settings.getName()){
            case "ALWAYS_DAY":
            case "ALWAYS_MIDDLE_DAY":
            case "ALWAYS_NIGHT":
            case "ALWAYS_MIDDLE_NIGHT":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().resetPlayerTime());
                break;
            case "ALWAYS_RAIN":
            case "ALWAYS_SHINY":
                getAllPlayersInside().forEach(superiorPlayer -> superiorPlayer.asPlayer().resetPlayerWeather());
                break;
        }

        MenuSettings.refreshMenus();
    }

    /*
     *  Generator related methods
     */

    @Override
    public void setGeneratorPercentage(com.bgsoftware.superiorskyblock.api.key.Key key, int percentage) {
        SuperiorSkyblockPlugin.debug("Action: Set Generator, Island: " + owner.getName() + ", Block: " + key + ", Percentage: " + percentage);

        Preconditions.checkArgument(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100 - got " + percentage + ".");

        if(percentage == 0){
            setGeneratorAmount(key, 0);
        }
        else if(percentage == 100){
            cobbleGeneratorValues.clear();
            setGeneratorAmount(key, 1);
        }
        else {
            //Removing the key from the generator
            setGeneratorAmount(key, 0);
            int totalAmount = getGeneratorTotalAmount();
            double realPercentage = percentage / 100D;
            double amount = (realPercentage * totalAmount) / (1 - realPercentage);
            if(amount < 1){
                cobbleGeneratorValues.map(v -> v * 10);
                amount *= 10;
            }
            setGeneratorAmount(key, (int) Math.round(amount));
        }
    }

    @Override
    public int getGeneratorPercentage(com.bgsoftware.superiorskyblock.api.key.Key key) {
        int totalAmount = getGeneratorTotalAmount();
        return totalAmount == 0 ? 0 : (getGeneratorAmount(key) * 100) / totalAmount;
    }

    public double getGeneratorPercentageDecimal(com.bgsoftware.superiorskyblock.api.key.Key key){
        int totalAmount = getGeneratorTotalAmount();
        return totalAmount == 0 ? 0 : (getGeneratorAmount(key) * 100D) / totalAmount;
    }

    @Override
    public Map<String, Integer> getGeneratorPercentages() {
        return getGeneratorAmounts().keySet().stream().collect(Collectors.toMap(key -> key, key -> getGeneratorAmount(Key.of(key))));
    }

    @Override
    public void setGeneratorAmount(com.bgsoftware.superiorskyblock.api.key.Key key, int amount) {
        SuperiorSkyblockPlugin.debug("Action: Set Generator, Island: " + owner.getName() + ", Block: " + key + ", Amount: " + amount);
        cobbleGeneratorValues.set(key, amount);
        Query.ISLAND_SET_GENERATOR.getStatementHolder(this)
                .setString(IslandSerializer.serializeGenerator(cobbleGeneratorValues))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public int getGeneratorAmount(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return cobbleGeneratorValues.get(key, 0);
    }

    @Override
    public int getGeneratorTotalAmount() {
        int totalAmount = 0;
        for(int amt : getGeneratorAmounts().values())
            totalAmount += amt;
        return totalAmount;
    }


    @Override
    public Map<String, Integer> getGeneratorAmounts() {
        return cobbleGeneratorValues.copy().asMap();
    }

    @Override
    public String[] getGeneratorArray() {
        String[] newCobbleGenerator = new String[getGeneratorTotalAmount()];
        int index = 0;
        for(Map.Entry<String, Integer> entry : getGeneratorAmounts().entrySet()){
            for(int i = 0; i < entry.getValue() && index < newCobbleGenerator.length; i++, index++){
                newCobbleGenerator[index] = entry.getKey();
            }
        }
        return newCobbleGenerator;
    }

    @Override
    public void clearGeneratorAmounts() {
        SuperiorSkyblockPlugin.debug("Action: Clear Generator, Island: " + owner.getName());
        cobbleGeneratorValues.clear();
        Query.ISLAND_SET_GENERATOR.getStatementHolder(this)
                .setString(IslandSerializer.serializeGenerator(cobbleGeneratorValues))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    /*
     *  Schematic methods
     */

    @Override
    public boolean wasSchematicGenerated(World.Environment environment) {
        int n = environment == World.Environment.NORMAL ? 8 : environment == World.Environment.NETHER ? 4 : 3;
        return (generatedSchematics.get() & n) == n;
    }

    @Override
    public void setSchematicGenerate(World.Environment environment) {
        SuperiorSkyblockPlugin.debug("Action: Set Schematic, Island: " + owner.getName() + ", Environment: " + environment);
        int n = environment == World.Environment.NORMAL ? 8 : environment == World.Environment.NETHER ? 4 : 3;
        int generatedSchematics = this.generatedSchematics.get() | n;
        this.generatedSchematics.set(generatedSchematics);
        Query.ISLAND_SET_GENERATED_SCHEMATICS.getStatementHolder(this)
                .setString(generatedSchematics + "")
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    @Override
    public String getSchematicName() {
        String schemName = this.schemName.get();
        return schemName == null ? "" : schemName;
    }

    /*
     *  Island top methods
     */

    @Override
    public int getPosition(SortingType sortingType) {
        return plugin.getGrid().getIslandPosition(this, sortingType);
    }

    /*
     *  Vault related methods
     */

    @Override
    public IslandChest[] getChest() {
        return islandChest.readAndGet(islandChests -> Arrays.copyOf(islandChests, islandChests.length));
    }

    @Override
    public int getChestSize() {
        return islandChest.readAndGet(islandChests -> islandChests.length);
    }

    @Override
    public void setChestRows(int index, int rows) {
        IslandChest[] islandChests = islandChest.get();
        int oldSize = islandChests.length;

        if(index >= oldSize) {
            islandChests = Arrays.copyOf(islandChests, index + 1);
            islandChest.set(islandChests);
            for(int i = oldSize; i <= index; i++) {
                (islandChests[i] = new SIslandChest(this, i)).setRows(plugin.getSettings().islandChestsDefaultSize);
            }
        }

        islandChests[index].setRows(rows);

        setIslandChestsModified();
    }

    /*
     *  Data related methods
     */

    @Override
    public StatementHolder setUpdateStatement(StatementHolder statementHolder) {
        return statementHolder.setString(LocationUtils.getLocation(getTeleportLocation(World.Environment.NORMAL)))
                .setString(LocationUtils.getLocation(visitorsLocation.get()))
                .setString(IslandSerializer.serializePlayers(members))
                .setString(IslandSerializer.serializePlayers(banned))
                .setString(IslandSerializer.serializePermissions(playerPermissions, rolePermissions))
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(islandBank.get().getAsString())
                .setInt(islandSize.getValue())
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setInt(teamLimit.getValue())
                .setFloat((float) (double) cropGrowth.getValue())
                .setFloat((float) (double) spawnerRates.getValue())
                .setFloat((float) (double) mobDrops.getValue())
                .setString(discord.get())
                .setString(paypal.get())
                .setInt(warpsLimit.getValue())
                .setString(bonusWorth.get().getAsString())
                .setBoolean(locked.get())
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(islandName.get())
                .setString(description.get())
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setBoolean(ignored.get())
                .setString(IslandSerializer.serializeGenerator(cobbleGeneratorValues))
                .setString(generatedSchematics.get() + "")
                .setString(schemName.get())
                .setString(IslandSerializer.serializePlayers(uniqueVisitors))
                .setString(unlockedWorlds.get() + "")
                .setLong(lastTimeUpdate.get())
                .setString(ChunksTracker.serialize(this))
                .setString(IslandSerializer.serializeEntityLimits(entityLimits))
                .setString(bonusLevel.get().getAsString())
                .setLong(creationTime)
                .setInt(coopLimit.getValue())
                .setString(IslandSerializer.serializeEffects(islandEffects))
                .setString(IslandSerializer.serializeIslandChest(islandChest))
                .setString(owner.getUniqueId().toString());
    }

    @Override
    public void executeUpdateStatement(boolean async) {
        setUpdateStatement(Query.ISLAND_UPDATE.getStatementHolder(this)).execute(async);
    }

    @Override
    public void executeDeleteStatement(boolean async){
        Query.ISLAND_DELETE.getStatementHolder(this)
                .setString(owner.getUniqueId().toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async){
        Query.ISLAND_INSERT.getStatementHolder(this)
                .setString(owner.getUniqueId().toString())
                .setString(LocationUtils.getLocation(center.parse()))
                .setString(IslandSerializer.serializeLocations(teleportLocations))
                .setString(IslandSerializer.serializePlayers(members))
                .setString(IslandSerializer.serializePlayers(banned))
                .setString(IslandSerializer.serializePermissions(playerPermissions, rolePermissions))
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(islandBank.get().getAsString())
                .setInt(islandSize.getValue())
                .setString(IslandSerializer.serializeBlockLimits(blockLimits))
                .setInt(teamLimit.getValue())
                .setFloat((float) (double) cropGrowth.getValue())
                .setFloat((float) (double) spawnerRates.getValue())
                .setFloat((float) (double) mobDrops.getValue())
                .setString(discord.get())
                .setString(paypal.get())
                .setInt(warpsLimit.getValue())
                .setString(bonusWorth.get().getAsString())
                .setBoolean(locked.get())
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(islandName.get())
                .setString(LocationUtils.getLocation(visitorsLocation.get()))
                .setString(description.get())
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(IslandSerializer.serializeSettings(islandSettings))
                .setBoolean(ignored.get())
                .setString(IslandSerializer.serializeGenerator(cobbleGeneratorValues))
                .setString(generatedSchematics.get() + "")
                .setString(schemName.get())
                .setString(IslandSerializer.serializePlayers(uniqueVisitors))
                .setString(unlockedWorlds.get() + "")
                .setLong(lastTimeUpdate.get())
                .setString(ChunksTracker.serialize(this))
                .setString(IslandSerializer.serializeEntityLimits(entityLimits))
                .setString(bonusLevel.get().getAsString())
                .setLong(creationTime)
                .setInt(coopLimit.getValue())
                .setString(IslandSerializer.serializeEffects(islandEffects))
                .setString(IslandSerializer.serializeIslandChest(islandChest))
                .execute(async);
    }

    public void saveDirtyChunks(){
        Query.ISLAND_SET_DIRTY_CHUNKS.getStatementHolder(this)
                .setString(ChunksTracker.serialize(this))
                .setString(owner.getUniqueId().toString())
                .execute(true);
    }

    public void saveBlockCounts(){
        blockCounts.read(blockCounts -> Query.ISLAND_SET_BLOCK_COUNTS.getStatementHolder(this)
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(owner.getUniqueId().toString())
                .execute(true));
    }

    public void setIslandChestsModified(){
        setModified(Query.ISLAND_SET_ISLAND_CHEST);
    }

    public void saveIslandChests(){
        Query.ISLAND_SET_ISLAND_CHEST.getStatementHolder(this)
                .setString(IslandSerializer.serializeIslandChest(islandChest))
                .setString(owner.getUniqueId().toString())
                .execute(true);
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

    private void assignGenerator(){
        if(getGeneratorAmounts().isEmpty() && owner != null) {
            plugin.getSettings().defaultGenerator.forEach(cobbleGeneratorValues::set);

            Query.ISLAND_SET_GENERATOR.getStatementHolder(this)
                    .setString(IslandSerializer.serializeGenerator(cobbleGeneratorValues))
                    .setString(owner.getUniqueId().toString())
                    .execute(true);
        }
    }

    private void assignIslandChest(){
        islandChest.write(islandChests -> {
            for(int i = 0; i < islandChests.length; i++) {
                islandChests[i] = new SIslandChest(this, i);
                islandChests[i].setRows(plugin.getSettings().islandChestsDefaultSize);
            }
        });
    }

    private void checkMembersDuplication(){
        boolean toSave = members.writeAndGet(members -> {
            Iterator<SuperiorPlayer> iterator = members.iterator();
            boolean removed = false;

            while (iterator.hasNext()){
                SuperiorPlayer superiorPlayer = iterator.next();
                if(!superiorPlayer.getIslandLeader().equals(owner)){
                    iterator.remove();
                    removed = true;
                }
                else if(superiorPlayer.equals(owner)){
                    iterator.remove();
                    removed = true;
                }
            }

            return removed;
        });

        if(toSave) {
            Query.ISLAND_SET_MEMBERS.getStatementHolder(this)
                    .setString(IslandSerializer.serializePlayers(members))
                    .setString(owner.getUniqueId().toString())
                    .execute(true);
        }
    }

    private void updateOldUpgradeValues(){
        for(com.bgsoftware.superiorskyblock.api.key.Key key : blockLimits.keySet()){
            if(blockLimits.getValue(key, -1) == plugin.getSettings().defaultBlockLimits.getOrDefault(key, -2))
                blockLimits.set(key, -1);
        }

        for(EntityType entityType : entityLimits.keySet()){
            if(entityLimits.getValue(entityType).equals(plugin.getSettings().defaultEntityLimits.get(entityType)))
                entityLimits.set(entityType, -1);
        }

        for(com.bgsoftware.superiorskyblock.api.key.Key key : cobbleGeneratorValues.keySet()){
            if(cobbleGeneratorValues.getValue(key, -1) == plugin.getSettings().defaultGenerator.getOrDefault(key, -2))
                cobbleGeneratorValues.set(key, -1);
        }

        if(getIslandSize() == plugin.getSettings().defaultIslandSize)
            islandSize.set(-1);

        if(getWarpsLimit() == plugin.getSettings().defaultWarpsLimit)
            warpsLimit.set(-1);

        if(getTeamLimit() == plugin.getSettings().defaultTeamLimit)
            teamLimit.set(-1);

        if(getCoopLimit() == plugin.getSettings().defaultCoopLimit)
            coopLimit.set(-1);

        if(getCropGrowthMultiplier() == plugin.getSettings().defaultCropGrowth)
            cropGrowth.set(-1D);

        if(getSpawnerRatesMultiplier() == plugin.getSettings().defaultSpawnerRates)
            spawnerRates.set(-1D);

        if(getMobDropsMultiplier() == plugin.getSettings().defaultMobDrops)
            mobDrops.set(-1D);
    }

    private void syncUpgrade(UpgradeLevel upgradeLevel){
        cropGrowth.setUpgrade(upgradeLevel.getCropGrowth());
        spawnerRates.setUpgrade(upgradeLevel.getSpawnerRates());
        mobDrops.setUpgrade(upgradeLevel.getMobDrops());
        blockLimits.setUpgrade(upgradeLevel.getBlockLimits(), true);
        entityLimits.setUpgrade(upgradeLevel.getEntityLimits());
        teamLimit.setUpgrade(upgradeLevel.getTeamLimit());
        warpsLimit.setUpgrade(upgradeLevel.getWarpsLimit());
        coopLimit.setUpgrade(upgradeLevel.getCoopLimit());
        islandSize.setUpgrade(upgradeLevel.getBorderSize());
        cobbleGeneratorValues.setUpgradeString(upgradeLevel.getGeneratorAmounts(), false);
        islandEffects.setUpgrade(upgradeLevel.getPotionEffects());
    }

    private void finishCalcIsland(SuperiorPlayer asker, Runnable callback, BigDecimal islandLevel, BigDecimal islandWorth){
        EventsCaller.callIslandWorthCalculatedEvent(this, asker, islandLevel, islandWorth);

        if(asker != null)
            Locale.ISLAND_WORTH_RESULT.send(asker, islandWorth, islandLevel);

        if(callback != null)
            callback.run();
    }

    public static class WarpData{

        public Location location;
        public boolean privateFlag;

        public WarpData(Location location, boolean privateFlag){
            this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(),
                    location.getBlockZ() + 0.5, location.getYaw(), location.getPitch());
            this.privateFlag = privateFlag;
        }

    }
}
