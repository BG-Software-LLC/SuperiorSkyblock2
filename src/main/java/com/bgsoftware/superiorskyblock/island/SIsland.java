package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.database.CachedResultSet;
import com.bgsoftware.superiorskyblock.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.database.Query;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.islands.IslandDeserializer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandSerializer;
import com.bgsoftware.superiorskyblock.utils.LocationUtil;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.DoubleTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
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
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SIsland extends DatabaseObject implements Island {

    public static final String VISITORS_WARP_NAME = "visit";
    public static final int NO_BLOCK_LIMIT = -1;

    protected static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static boolean calcProcess = false;
    private static Queue<CalcIslandData> islandCalcsQueue = new Queue<>();
    private static NumberFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");

    /*
     * SIsland identifiers
     */
    private UUID owner;
    private final BlockPosition center;

    /*
     * SIsland data
     */

    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> banned = new HashSet<>();
    private final Map<Object, SPermissionNode> permissionNodes = new HashMap<>();
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final KeyMap<Integer> blockCounts = new KeyMap<>();
    private final KeyMap<Integer> blockLimits = new KeyMap<>(plugin.getSettings().defaultBlockLimits);
    private final Map<String, WarpData> warps = new HashMap<>();
    private BigDecimalFormatted islandBank = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted islandWorth = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted islandLevel = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted bonusWorth = BigDecimalFormatted.ZERO;
    private String discord = "None", paypal = "None";
    private int islandSize = plugin.getSettings().defaultIslandSize;
    private Location teleportLocation, visitorsLocation;
    private boolean locked = false;
    private String islandName = "";
    private String description = "";
    private final Map<UUID, Rating> ratings = new HashMap<>();
    private final Set<String> completedMissions = new HashSet<>();

    /*
     * SIsland multipliers & limits
     */

    private int warpsLimit = plugin.getSettings().defaultWarpsLimit;
    private int teamLimit = plugin.getSettings().defaultTeamLimit;
    private double cropGrowth = plugin.getSettings().defaultCropGrowth;
    private double spawnerRates = plugin.getSettings().defaultSpawnerRates;
    private double mobDrops = plugin.getSettings().defaultMobDrops;

    public SIsland(CachedResultSet resultSet){
        this.owner = UUID.fromString(resultSet.getString("owner"));

        this.center = SBlockPosition.of(Objects.requireNonNull(LocationUtil.getLocation(resultSet.getString("center"))));
        this.teleportLocation = LocationUtil.getLocation(resultSet.getString("teleportLocation"));
        this.visitorsLocation = LocationUtil.getLocation(resultSet.getString("visitorsLocation"));

        IslandDeserializer.deserializeMembers(resultSet.getString("members"), this.members);
        IslandDeserializer.deserializeBanned(resultSet.getString("banned"), this.banned);
        IslandDeserializer.deserializePermissions(resultSet.getString("permissionNodes"), this.permissionNodes);
        IslandDeserializer.deserializeUpgrades(resultSet.getString("upgrades"), this.upgrades);
        IslandDeserializer.deserializeWarps(resultSet.getString("warps"), this.warps);
        IslandDeserializer.deserializeBlockCounts(resultSet.getString("blockCounts"), this);
        IslandDeserializer.deserializeBlockLimits(resultSet.getString("blockLimits"), this.blockLimits);
        IslandDeserializer.deserializeRatings(resultSet.getString("ratings"), this.ratings);
        IslandDeserializer.deserializeMissions(resultSet.getString("missions"), this.completedMissions);

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

        if(blockCounts.isEmpty())
            calcIslandWorth(null);
    }

    public SIsland(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();
        this.owner = UUID.fromString(((StringTag) compoundValues.get("owner")).getValue());
        this.center = SBlockPosition.of(((StringTag) compoundValues.get("center")).getValue());

        this.teleportLocation = compoundValues.containsKey("teleportLocation") ?
                LocationUtil.getLocation(((StringTag) compoundValues.get("teleportLocation")).getValue()) : getCenter();

        List<Tag> members = ((ListTag) compoundValues.get("members")).getValue();
        for(Tag _tag : members)
            this.members.add(UUID.fromString(((StringTag) _tag).getValue()));

        List<Tag> banned = ((ListTag) compoundValues.get("banned")).getValue();
        for(Tag _tag : banned)
            this.banned.add(UUID.fromString(((StringTag) _tag).getValue()));

        Map<String, Tag> permissionNodes = ((CompoundTag) compoundValues.get("permissionNodes")).getValue();
        for(String islandRole : permissionNodes.keySet())
            this.permissionNodes.put(IslandRole.valueOf(islandRole), new SPermissionNode((ListTag) permissionNodes.get(islandRole)));

        Map<String, Tag> upgrades = ((CompoundTag) compoundValues.get("upgrades")).getValue();
        for(String upgrade : upgrades.keySet())
            this.upgrades.put(upgrade, ((IntTag) upgrades.get(upgrade)).getValue());

        Map<String, Tag> warps = ((CompoundTag) compoundValues.get("warps")).getValue();
        for(String warp : warps.keySet())
            this.warps.put(warp, new WarpData(FileUtil.toLocation(((StringTag) warps.get(warp)).getValue()), false));

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
    }

    public SIsland(SuperiorPlayer superiorPlayer, Location location, String islandName){
        this(superiorPlayer, SBlockPosition.of(location), islandName);
    }

    public SIsland(SuperiorPlayer superiorPlayer, SBlockPosition wrappedLocation, String islandName){
        if(superiorPlayer != null){
            this.owner = superiorPlayer.getTeamLeader();
            superiorPlayer.setIslandRole(IslandRole.LEADER);
        }else{
            this.owner = null;
        }
        this.center = wrappedLocation;
        this.islandName = islandName;
        assignPermissionNodes();
    }

    @Override
    public SuperiorPlayer getOwner() {
        return SSuperiorPlayer.of(owner);
    }

    @Override
    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    @Override
    public boolean isSpawn() {
        return false;
    }

    @Override
    public void inviteMember(SuperiorPlayer superiorPlayer){
        if(invitedPlayers.contains(superiorPlayer.getUniqueId()))
            return;

        invitedPlayers.add(superiorPlayer.getUniqueId());
    }

    @Override
    public void revokeInvite(SuperiorPlayer superiorPlayer){
        invitedPlayers.remove(superiorPlayer.getUniqueId());
    }

    @Override
    public boolean isInvited(SuperiorPlayer superiorPlayer){
        return invitedPlayers.contains(superiorPlayer.getUniqueId());
    }

    @Override
    public void addMember(SuperiorPlayer superiorPlayer, IslandRole islandRole){
        members.add(superiorPlayer.getUniqueId());
        superiorPlayer.setTeamLeader(owner);
        superiorPlayer.setIslandRole(islandRole);
        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer){
        members.remove(superiorPlayer.getUniqueId());
        superiorPlayer.setTeamLeader(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer){
        if(isMember(superiorPlayer))
            kickMember(superiorPlayer);
        if(superiorPlayer.isOnline() && isInside(superiorPlayer.getLocation()))
            superiorPlayer.asPlayer().teleport(plugin.getGrid().getSpawnIsland().getCenter());
        banned.add(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        banned.remove(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer){
        return banned.contains(superiorPlayer.getUniqueId());
    }

    @Override
    public List<UUID> getAllBannedMembers() {
        return new ArrayList<>(banned);
    }

    @Override
    public List<UUID> getAllMembers() {
        List<UUID> members = new ArrayList<>();

        members.add(owner);
        members.addAll(getMembers());

        return members;
    }

    @Override
    public List<UUID> getVisitors(){
        List<UUID> visitors = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()){
            if(!isMember(SSuperiorPlayer.of(player)) && isInside(player.getLocation()))
                visitors.add(player.getUniqueId());
        }

        return visitors;
    }

    @Override
    public List<UUID> allPlayersInside(){
        List<UUID> visitors = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()){
            if(isInside(player.getLocation()))
                visitors.add(player.getUniqueId());
        }

        return visitors;
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer){
        return owner.equals(superiorPlayer.getTeamLeader());
    }

    @Override
    public Location getCenter(){
        return center.parse().add(0.5, 0, 0.5);
    }

    @Override
    public Location getVisitorsLocation() {
        return visitorsLocation == null ? null : visitorsLocation.clone();
    }

    @Override
    public Location getTeleportLocation() {
        if(teleportLocation == null)
            teleportLocation = getCenter();
        return teleportLocation.clone();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation.clone();
        Query.ISLAND_SET_TELEPORT_LOCATION.getStatementHolder()
                .setString(LocationUtil.getLocation(getTeleportLocation()))
                .setString(owner.toString())
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
                .setString(LocationUtil.getLocation(getVisitorsLocation()))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public Location getMinimum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter().subtract(islandDistance, 0, islandDistance);
    }

    @Override
    public Location getMaximum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter().add(islandDistance, 0, islandDistance);
    }

    @Override
    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission){
        return sender instanceof ConsoleCommandSender || hasPermission(SSuperiorPlayer.of(sender), islandPermission);
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission){
        boolean playerPermission = false;

        if(permissionNodes.containsKey(superiorPlayer.getUniqueId())){
            playerPermission = permissionNodes.get(superiorPlayer.getUniqueId()).hasPermission(islandPermission);
        }

        IslandRole islandRole = isMember(superiorPlayer) ? superiorPlayer.getIslandRole() : IslandRole.GUEST;
        return playerPermission || superiorPlayer.hasBypassModeEnabled() || permissionNodes.get(islandRole).hasPermission(islandPermission);
    }

    @Override
    public void setPermission(IslandRole islandRole, IslandPermission islandPermission, boolean value){
        permissionNodes.get(islandRole).setPermission(islandPermission, value);

        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission, boolean value) {
        SPermissionNode permissionNode = permissionNodes.getOrDefault(superiorPlayer.getUniqueId(), new SPermissionNode(""));

        permissionNode.setPermission(islandPermission, value);

        permissionNodes.put(superiorPlayer.getUniqueId(), permissionNode);

        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                .setString(IslandSerializer.serializePermissions(permissionNodes))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public SPermissionNode getPermisisonNode(IslandRole islandRole){
        return permissionNodes.get(islandRole).clone();
    }

    @Override
    public PermissionNode getPermisisonNode(SuperiorPlayer superiorPlayer) {
        IslandRole islandRole = isMember(superiorPlayer) ? superiorPlayer.getIslandRole() : IslandRole.GUEST;
        return permissionNodes.getOrDefault(superiorPlayer.getUniqueId(), getPermisisonNode(islandRole));
    }

    @Override
    public IslandRole getRequiredRole(IslandPermission islandPermission){
        IslandRole islandRole = IslandRole.LEADER;

        for(IslandRole _islandRole : IslandRole.values()){
            if(_islandRole.isLessThan(islandRole) && permissionNodes.get(_islandRole).hasPermission(islandPermission))
                islandRole = _islandRole;
        }

        return islandRole;
    }

    @Override
    public void disbandIsland(){
        getAllMembers().forEach(member -> {
            if(members.contains(member))
                kickMember(SSuperiorPlayer.of(member));
            if(plugin.getSettings().disbandInventoryClear)
                plugin.getNMSAdapter().clearInventory(Bukkit.getOfflinePlayer(member));
        });
        plugin.getGrid().deleteIsland(this);
        if(!Bukkit.getBukkitVersion().contains("1.14")) {
            for (Chunk chunk : getAllChunks(true))
                chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
        }
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
    @Deprecated
    public double getMoneyInBank(){
        return getMoneyInBankAsBigDecimal().doubleValue();
    }

    @Override
    public BigDecimal getMoneyInBankAsBigDecimal() {
        if(islandBank.doubleValue() < 0) islandBank = BigDecimalFormatted.ZERO;
        return islandBank;
    }

    @Override
    @Deprecated
    public String getMoneyAsString() {
        return getMoneyInBankAsBigDecimal().toString();
    }

    @Override
    public void depositMoney(double amount){
        islandBank = islandBank.add(BigDecimalFormatted.of(amount));
        Query.ISLAND_SET_BANK.getStatementHolder()
                .setString(islandBank.getAsString())
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void withdrawMoney(double amount){
        islandBank = islandBank.subtract(BigDecimalFormatted.of(amount));
        Query.ISLAND_SET_BANK.getStatementHolder()
                .setString(islandBank.getAsString())
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> calcIslandWorth(asker));
            return;
        }

        if(calcProcess) {
            islandCalcsQueue.push(new CalcIslandData(owner, asker == null ? null : asker.getUniqueId()));
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
                    boolean emptyChunk = true;

                    double islandWorth = 0;

                    for(int i = 0; i < 16 && emptyChunk; i++){
                        if(!chunkSnapshot.isSectionEmpty(i)){
                            emptyChunk = false;
                        }
                    }

                    if(emptyChunk)
                        return;

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

                Bukkit.getPluginManager().callEvent(new IslandWorthCalculatedEvent(this, getIslandLevelAsBigDecimal(), asker));

                if(asker != null)
                    Locale.ISLAND_WORTH_RESULT.send(asker, getWorthAsBigDecimal(), getIslandLevelAsBigDecimal());

                for(Chunk chunk : chunks)
                    BlocksProvider_WildStacker.uncacheChunk(chunk);

                saveBlockCounts();

                calcProcess = false;

                if(islandCalcsQueue.size() != 0){
                    CalcIslandData calcIslandData = islandCalcsQueue.pop();
                    plugin.getGrid().getIsland(SSuperiorPlayer.of(calcIslandData.owner))
                            .calcIslandWorth(calcIslandData.asker == null ? null : SSuperiorPlayer.of(calcIslandData.asker));
                }
            });
        });
    }

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

        if(blockValue.doubleValue() > 0){
            islandWorth = islandWorth.add(blockValue.multiply(new BigDecimal(amount)));
            increaseAmount = true;
        }

        if(blockLevel.doubleValue() > 0){
            islandLevel = islandLevel.add(blockLevel.multiply(new BigDecimal(amount)));
            increaseAmount = true;
        }

        if(increaseAmount || blockLimits.containsKey(key)) {
            int currentAmount = blockCounts.getOrDefault(key, 0);
            key = blockLimits.containsKey(key) ? blockLimits.getKey(key) : blockCounts.getKey(key);
            blockCounts.put(plugin.getBlockValues().getBlockKey(key), currentAmount + amount);
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
            int currentAmount = blockCounts.getOrDefault(key, 0);
            key = blockLimits.containsKey(key) ? blockLimits.getKey(key) : blockCounts.getKey(key);
            key = plugin.getBlockValues().getBlockKey(key);

            if(currentAmount <= amount)
                blockCounts.remove(key);
            else
                blockCounts.put(key, currentAmount - amount);

            if(save) saveBlockCounts();
        }
    }

    private void saveBlockCounts(){
        Query.ISLAND_SET_BLOCK_COUNTS.getStatementHolder()
                .setString(IslandSerializer.serializeBlockCounts(blockCounts))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public int getHoppersAmount(){
        return getBlockCount(Key.of("HOPPER"));
    }

    @Override
    public int getBlockCount(Key key){
        return blockCounts.getOrDefault(key, 0);
    }

    @Override
    @Deprecated
    public double getWorth(){
        return getWorthAsBigDecimal().doubleValue();
    }

    @Override
    public BigDecimal getWorthAsBigDecimal() {
        int bankWorthRate = plugin.getSettings().bankWorthRate;
        //noinspection BigDecimalMethodWithoutRoundingCalled
        BigDecimal islandWorth = bankWorthRate <= 0 ? getRawWorthAsBigDecimal() : this.islandWorth.add(islandBank.divide(new BigDecimal(bankWorthRate)));
        return islandWorth.add(bonusWorth);
    }

    @Override
    @Deprecated
    public double getRawWorth(){
        return getRawWorthAsBigDecimal().doubleValue();
    }

    @Override
    public BigDecimal getRawWorthAsBigDecimal() {
        return islandWorth;
    }

    @Override
    @Deprecated
    public String getWorthAsString(){
        return getWorthAsBigDecimal().toString();
    }

    @Override
    public void setBonusWorth(BigDecimal bonusWorth){
        this.bonusWorth = bonusWorth instanceof BigDecimalFormatted ? (BigDecimalFormatted) bonusWorth : BigDecimalFormatted.of(bonusWorth);
        Query.ISLAND_SET_BONUS_WORTH.getStatementHolder()
                .setString(this.bonusWorth.getAsString())
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    @Deprecated
    public int getIslandLevel(){
        return getIslandLevelAsBigDecimal().intValue();
    }

    @Override
    public BigDecimal getIslandLevelAsBigDecimal() {
        return plugin.getSettings().bonusAffectLevel ? islandLevel.add(new BigDecimal(plugin.getBlockValues().convertValueToLevel(bonusWorth))) : islandLevel;
    }

    @Override
    @Deprecated
    public String getLevelAsString() {
        return getIslandLevelAsBigDecimal().toString();
    }

    @Override
    public boolean isInside(Location location){
        Location min = getMinimum(), max = getMaximum();
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @Override
    public boolean isInsideRange(Location location){
        int islandSize = getIslandSize();
        Location min = center.parse().subtract(islandSize, 0, islandSize);
        Location max = center.parse().add(islandSize, 0, islandSize);
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @Override
    public int getUpgradeLevel(String upgradeName){
        return upgrades.getOrDefault(upgradeName, 1);
    }

    @Override
    public void setUpgradeLevel(String upgradeName, int level){
        upgrades.put(upgradeName, Math.min(plugin.getUpgrades().getMaxUpgradeLevel(upgradeName), level));

        Query.ISLAND_SET_UPGRADES.getStatementHolder()
                .setString(IslandSerializer.serializeUpgrades(upgrades))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public int getIslandSize() {
        return islandSize;
    }

    @Override
    public int getHoppersLimit(){
        return getBlockLimit(Key.of("HOPPER"));
    }

    @Override
    public int getBlockLimit(Key key) {
        return blockLimits.getOrDefault(key, NO_BLOCK_LIMIT);
    }

    @Override
    public int getTeamLimit() {
        return teamLimit;
    }

    @Override
    public double getCropGrowthMultiplier() {
        return cropGrowth;
    }

    @Override
    public double getSpawnerRatesMultiplier() {
        return spawnerRates;
    }

    @Override
    public double getMobDropsMultiplier() {
        return mobDrops;
    }

    @Override
    public void setIslandSize(int islandSize) {
        this.islandSize = islandSize;
        Query.ISLAND_SET_SIZE.getStatementHolder()
                .setInt(islandSize)
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void updateBorder() {
        allPlayersInside().forEach(uuid -> plugin.getNMSAdapter().setWorldBorder(SSuperiorPlayer.of(uuid), this));
    }

    @Override
    public void setHoppersLimit(int hoppersLimit){
        setBlockLimit(Key.of("HOPPER"), hoppersLimit);
    }

    @Override
    public void setBlockLimit(Key key, int limit) {
        this.blockLimits.put(key, limit);
        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder()
                .setString(IslandSerializer.serializeBlockLimits(this.blockLimits))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder()
                .setInt(teamLimit)
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        this.cropGrowth = cropGrowth;
        Query.ISLAND_SET_CROP_GROWTH.getStatementHolder()
                .setDouble(cropGrowth)
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.spawnerRates = spawnerRates;
        Query.ISLAND_SET_SPAWNER_RATES.getStatementHolder()
                .setDouble(spawnerRates)
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        this.mobDrops = mobDrops;
        Query.ISLAND_SET_MOB_DROPS.getStatementHolder()
                .setDouble(mobDrops)
                .setString(owner.toString())
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
                .setString(owner.toString())
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
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void setBiome(Biome biome){
        Location min = getMinimum(), max = getMaximum();
        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                center.getWorld().setBiome(x, z, biome);
            }
        }
    }

    @Override
    public void sendMessage(String message, UUID... ignoredMembers){
        List<UUID> ignoredList = Arrays.asList(ignoredMembers);
        SuperiorPlayer targetPlayer;
        for(UUID uuid : getAllMembers()){
            if(!ignoredList.contains(uuid)) {
                if ((targetPlayer = SSuperiorPlayer.of(uuid)).asOfflinePlayer().isOnline())
                    Locale.sendMessage(targetPlayer, message);
            }
        }
    }

    @Override
    public Location getWarpLocation(String name){
        return warps.containsKey(name.toLowerCase()) ? warps.get(name.toLowerCase()).location.clone() : null;
    }

    @Override
    public boolean isWarpPrivate(String name) {
        return !warps.containsKey(name.toLowerCase()) || warps.get(name.toLowerCase()).privateFlag;
    }

    @Override
    public void setWarpLocation(String name, Location location){
        setWarpLocation(name, location, false);
    }

    @Override
    public void setWarpLocation(String name, Location location, boolean privateFlag) {
        warps.put(name.toLowerCase(), new WarpData(location.clone(), privateFlag));

        Query.ISLAND_SET_WARPS.getStatementHolder()
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp){
        Location location = warps.get(warp.toLowerCase()).location.clone();
        Block warpBlock = location.getBlock();

        if(!isInsideRange(location)){
            Locale.UNSAFE_WARP.send(superiorPlayer);
            deleteWarp(warp);
            return;
        }

        if(!warpBlock.getRelative(BlockFace.DOWN).getType().isSolid() &&
                !warpBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType().isSolid()){
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
            if(LocationUtil.isSameBlock(location, warps.get(warpName).location)){
                deleteWarp(warpName);
                Locale.DELETE_WARP.send(superiorPlayer, warpName);
            }
        }
    }

    @Override
    public void deleteWarp(String name){
        warps.remove(name);

        Query.ISLAND_SET_WARPS.getStatementHolder()
                .setString(IslandSerializer.serializeWarps(warps))
                .setString(owner.toString())
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

    @Override
    public void setWarpsLimit(int warpsLimit) {
        this.warpsLimit = warpsLimit;
        Query.ISLAND_SET_WARPS_LIMIT.getStatementHolder()
                .setInt(warpsLimit)
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit;
    }

    @Override
    public boolean transferIsland(SuperiorPlayer superiorPlayer) {
        if(superiorPlayer.getUniqueId().equals(owner))
            return false;

        SuperiorPlayer previousOwner = getOwner();

        IslandTransferEvent islandTransferEvent = new IslandTransferEvent(this, previousOwner, superiorPlayer);
        Bukkit.getPluginManager().callEvent(islandTransferEvent);

        if(islandTransferEvent.isCancelled())
            return false;

        executeDeleteStatement(true);

        //Kick member without saving to database
        members.remove(superiorPlayer.getUniqueId());
        superiorPlayer.setIslandRole(IslandRole.LEADER);

        //Add member without saving to database
        members.add(previousOwner.getUniqueId());
        previousOwner.setIslandRole(IslandRole.ADMIN);

        //Changing owner of the island and updating all players
        owner = superiorPlayer.getUniqueId();
        for(UUID islandMember : getAllMembers())
            SSuperiorPlayer.of(islandMember).setTeamLeader(owner);

        executeInsertStatement(true);

        plugin.getGrid().getIslandRegistry().transferIsland(previousOwner.getUniqueId(), owner);

        return true;
    }

    @Override
    public void transfer(SuperiorPlayer player) {
        transferIsland(player);
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;

        if(locked){
            for(UUID uuid : allPlayersInside()){
                SuperiorPlayer victimPlayer = SSuperiorPlayer.of(uuid);
                if(!hasPermission(victimPlayer, IslandPermission.CLOSE_BYPASS)){
                    victimPlayer.asPlayer().teleport(plugin.getGrid().getSpawnIsland().getCenter());
                    Locale.ISLAND_WAS_CLOSED.send(victimPlayer);
                }
            }
        }

        Query.ISLAND_SET_LOCKED.getStatementHolder()
                .setBoolean(locked)
                .setString(owner.toString())
                .execute(true);
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
                .setString(owner.toString())
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
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public Rating getRating(UUID uuid) {
        return ratings.getOrDefault(uuid, Rating.UNKNOWN);
    }

    @Override
    public void setRating(UUID uuid, Rating rating) {
        if(rating == Rating.UNKNOWN)
            ratings.remove(uuid);
        else
            ratings.put(uuid, rating);

        Query.ISLAND_SET_RATINGS.getStatementHolder()
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(owner.toString())
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

    @Override
    public void completeMission(Mission mission) {
        completedMissions.add(mission.getName());

        Query.ISLAND_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public void resetMission(Mission mission) {
        completedMissions.remove(mission.getName());

        Query.ISLAND_SET_MISSIONS.getStatementHolder()
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .setString(owner.toString())
                .execute(true);
    }

    @Override
    public boolean hasCompletedMission(Mission mission) {
        return completedMissions.contains(mission.getName());
    }

    @Override
    public void executeUpdateStatement(boolean async){
        Query.ISLAND_UPDATE.getStatementHolder()
                .setString(LocationUtil.getLocation(getTeleportLocation()))
                .setString(LocationUtil.getLocation(visitorsLocation))
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
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
                .setString(owner.toString())
                .execute(async);
    }

    @Override
    public void executeDeleteStatement(boolean async){
        Query.ISLAND_DELETE.getStatementHolder()
                .setString(owner.toString())
                .execute(async);
    }

    @Override
    public void executeInsertStatement(boolean async){
        Query.ISLAND_INSERT.getStatementHolder()
                .setString(owner.toString())
                .setString(LocationUtil.getLocation(center.getBlock().getLocation()))
                .setString(LocationUtil.getLocation(getTeleportLocation()))
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
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
                .setString(LocationUtil.getLocation(visitorsLocation))
                .setString(description)
                .setString(IslandSerializer.serializeRatings(ratings))
                .setString(IslandSerializer.serializeMissions(completedMissions))
                .execute(async);
    }

    private static String getUuidCollectionString(Collection<UUID> collection) {
        StringBuilder builder = new StringBuilder();
        collection.forEach(uuid -> builder.append(",").append(uuid.toString()));
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SIsland ? owner.equals(((SIsland) obj).owner) : super.equals(obj);
    }

    @Override
    @SuppressWarnings("all")
    public int compareTo(Island other) {
        if(other == null)
            return -1;

        if(plugin.getSettings().islandTopOrder.equals("WORTH")){
            int compare = getWorthAsBigDecimal().compareTo(other.getWorthAsBigDecimal());
            if(compare != 0) return compare;
        }
        else {
            int compare = getIslandLevelAsBigDecimal().compareTo(other.getIslandLevelAsBigDecimal());
            if(compare != 0) return compare;
        }

        return getOwner().getName().compareTo(other.getOwner().getName());
    }

    private void assignPermissionNodes(){
        permissionNodes.put(IslandRole.GUEST, new SPermissionNode(null, plugin.getSettings().guestPermissions));
        permissionNodes.put(IslandRole.MEMBER,
                new SPermissionNode(permissionNodes.get(IslandRole.GUEST), plugin.getSettings().memberPermissions));
        permissionNodes.put(IslandRole.MODERATOR,
                new SPermissionNode(permissionNodes.get(IslandRole.MEMBER), plugin.getSettings().modPermissions));
        permissionNodes.put(IslandRole.ADMIN,
                new SPermissionNode(permissionNodes.get(IslandRole.MODERATOR), plugin.getSettings().adminPermission));
        permissionNodes.put(IslandRole.LEADER,
                new SPermissionNode(permissionNodes.get(IslandRole.ADMIN), plugin.getSettings().leaderPermissions));
    }

    private static class CalcIslandData{

        private UUID owner, asker;

        private CalcIslandData(UUID owner, UUID asker){
            this.owner = owner;
            this.asker = asker;
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
