package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.queue.Queue;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.DoubleTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;

import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SIsland implements Island{

    protected static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static boolean calcProcess = false;
    private static Queue<CalcIslandData> islandCalcsQueue = new Queue<>();
    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    private static NumberFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");

    /*
     * SIsland identifiers
     */

    private final UUID owner;
    private final BlockPosition center;

    /*
     * SIsland data
     */

    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> banned = new HashSet<>();
    private final Map<IslandRole, SPermissionNode> permissionNodes = new HashMap<>();
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final KeyMap<Integer> blocksCalculations = new KeyMap<>();
    private final Map<String, Location> warps = new HashMap<>();
    private BigDecimal islandBank = new BigDecimal(0);
    private BigDecimal islandWorth = new BigDecimal(0);
    private String discord = "None", paypal = "None";

    /*
     * SIsland multipliers & limits
     */

    private int hoppersLimit = plugin.getSettings().defaultHoppersLimit;
    private int teamLimit = plugin.getSettings().defaultTeamLimit;
    private double cropGrowth = plugin.getSettings().defaultCropGrowth;
    private double spawnerRates = plugin.getSettings().defaultSpawnerRates;
    private double mobDrops = plugin.getSettings().defaultMobDrops;

    public SIsland(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();
        this.owner = UUID.fromString(((StringTag) compoundValues.get("owner")).getValue());
        this.center = SBlockPosition.of(((StringTag) compoundValues.get("center")).getValue());

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
            this.warps.put(warp, FileUtil.toLocation(((StringTag) warps.get(warp)).getValue()));

        this.islandBank = new BigDecimal(((DoubleTag) compoundValues.get("islandBank")).getValue());

        this.hoppersLimit = ((IntTag) compoundValues.get("hoppersLimit")).getValue();
        this.teamLimit = ((IntTag) compoundValues.get("teamLimit")).getValue();
        this.cropGrowth = ((DoubleTag) compoundValues.get("cropGrowth")).getValue();
        this.spawnerRates = ((DoubleTag) compoundValues.get("spawnerRates")).getValue();
        this.mobDrops = ((DoubleTag) compoundValues.get("mobDrops")).getValue();
        this.discord = ((StringTag) compoundValues.get("discord")).getValue();
        this.paypal = ((StringTag) compoundValues.get("paypal")).getValue();

        calcIslandWorth(null);
    }

    public SIsland(SuperiorPlayer superiorPlayer, Location location){
        this(superiorPlayer, SBlockPosition.of(location));
    }

    public SIsland(SuperiorPlayer superiorPlayer, SBlockPosition wrappedLocation){
        if(superiorPlayer != null){
            this.owner = superiorPlayer.getTeamLeader();
            superiorPlayer.setIslandRole(IslandRole.LEADER);
        }else{
            this.owner = null;
        }
        this.center = wrappedLocation;
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
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer){
        members.remove(superiorPlayer.getUniqueId());
        superiorPlayer.setTeamLeader(superiorPlayer.getUniqueId());
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer){
        if(isMember(superiorPlayer)) kickMember(superiorPlayer);
        banned.add(superiorPlayer.getUniqueId());
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        banned.remove(superiorPlayer.getUniqueId());
    }

    @Override
    public boolean isBanned(SuperiorPlayer superiorPlayer){
        return banned.contains(superiorPlayer.getUniqueId());
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
        IslandRole islandRole = isMember(superiorPlayer) ? superiorPlayer.getIslandRole() : IslandRole.GUEST;
        return superiorPlayer.hasBypassModeEnabled() || permissionNodes.get(islandRole).hasPermission(islandPermission);
    }

    @Override
    public void setPermission(IslandRole islandRole, IslandPermission islandPermission, boolean value){
        permissionNodes.get(islandRole).setPermission(islandPermission, value);
    }

    @Override
    public SPermissionNode getPermisisonNode(IslandRole islandRole){
        return permissionNodes.get(islandRole).clone();
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
        members.forEach(member -> kickMember(SSuperiorPlayer.of(member)));
        plugin.getGrid().deleteIsland(this);
        for(Chunk chunk : getAllChunks(false))
            chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
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
    public double getMoneyInBank(){
        if(islandBank.doubleValue() < 0) islandBank = new BigDecimal(0);
        return islandBank.doubleValue();
    }

    @Override
    public String getMoneyAsString() {
        return numberFormatter.format(getMoneyInBank());
    }

    @Override
    public void depositMoney(double amount){
        islandBank = islandBank.add(new BigDecimal(amount));
    }

    @Override
    public void withdrawMoney(double amount){
        islandBank = islandBank.subtract(new BigDecimal(amount));
    }

    @Override
    public void calcIslandWorth(SuperiorPlayer asker) {
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, () -> calcIslandWorth(asker));
            return;
        }

        if(calcProcess) {
            islandCalcsQueue.push(new CalcIslandData(owner, asker == null ? null : asker.getUniqueId()));
            return;
        }

        calcProcess = true;

        List<ChunkSnapshot> chunkSnapshots = new ArrayList<>();

        for (Chunk chunk : getAllChunks(true))
            chunkSnapshots.add(chunk.getChunkSnapshot(true, false, false));

        blocksCalculations.clear();
        islandWorth = new BigDecimal(0);

        World world = Bukkit.getWorld(chunkSnapshots.get(0).getWorldName());

        new SuperiorThread(() -> {
            Map<Location, Integer> spawnersToCheck = new HashMap<>();
            Set<Thread> threads = new HashSet<>();

            for (ChunkSnapshot chunkSnapshot : chunkSnapshots) {
                Thread thread = new Thread(() -> {
                    boolean emptyChunk = true;

                    double islandWorth = 0;

                    for(int i = 0; i < 16 && emptyChunk; i++){
                        if(!chunkSnapshot.isSectionEmpty(i)){
                            emptyChunk = false;
                        }
                    }

                    if(emptyChunk)
                        return;

                    int highestBlock;

                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            highestBlock = chunkSnapshot.getHighestBlockYAt(x, z);
                            for (int y = 0; y <= highestBlock; y++) {
                                Key blockKey = plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y, z);
                                if(blockKey.toString().contains("AIR"))
                                    continue;
                                Location location = new Location(world, (chunkSnapshot.getX() * 16) + x, y, (chunkSnapshot.getZ() * 16) + z);
                                int blockCount = 1;

                                for(BlocksProvider blocksProvider : plugin.getBlocksProviders()) {
                                    blockCount = Math.max(blockCount, blocksProvider.getBlockCount(location));
                                    blockKey = blocksProvider.getBlockKey(location, blockKey);
                                }

                                if(blockKey.toString().contains("SPAWNER")){
                                    spawnersToCheck.put(location, blockCount);
                                    continue;
                                }

                                handleBlockPlace(blockKey, blockCount);
                                //islandWorth += plugin.getGrid().getBlockValue(blockKey) * blockCount;
                            }
                        }
                    }
                });
                thread.start();
                threads.add(thread);
            }

            for(Thread th : threads){
                try{
                    th.join();
                }catch(Exception ignored){}
            }

            calcProcess = false;
            Bukkit.getScheduler().runTask(plugin, () -> {
                for(Location location : spawnersToCheck.keySet()){
                    BlockState blockState = location.getBlock().getState();
                    if(blockState instanceof CreatureSpawner){
                        CreatureSpawner creatureSpawner = (CreatureSpawner) blockState;
                        Key key = SKey.of(creatureSpawner.getType() + ":" + creatureSpawner.getSpawnedType());
                        int blockCount = spawnersToCheck.get(location);
                        handleBlockPlace(key, blockCount);
                        islandWorth = islandWorth.add(new BigDecimal(plugin.getGrid().getBlockValue(key) * blockCount));
                    }
                }
                if(asker != null)
                    Locale.ISLAND_WORTH_RESULT.send(asker, getWorthAsString(), getLevelAsString());
            });

            if(islandCalcsQueue.size() != 0){
                CalcIslandData calcIslandData = islandCalcsQueue.pop();
                plugin.getGrid().getIsland(SSuperiorPlayer.of(calcIslandData.owner))
                        .calcIslandWorth(calcIslandData.asker == null ? null : SSuperiorPlayer.of(calcIslandData.asker));
            }
        }).start();
    }

    @Override
    public void handleBlockPlace(Block block){
        handleBlockPlace(SKey.of(block), 1);
    }

    @Override
    public void handleBlockPlace(Block block, int amount){
        handleBlockPlace(SKey.of(block), amount);
    }

    @Override
    public synchronized void handleBlockPlace(Key key, int amount){
        int blockValue;
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || SKey.of("HOPPER:0").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);
            blocksCalculations.put(key, currentAmount + amount);
            islandWorth = islandWorth.add(new BigDecimal(blockValue).multiply(new BigDecimal(amount)));
        }
    }

    @Override
    public void handleBlockBreak(Block block){
        handleBlockBreak(SKey.of(block), 1);
    }

    @Override
    public void handleBlockBreak(Block block, int amount){
        handleBlockBreak(SKey.of(block), amount);
    }

    @Override
    public void handleBlockBreak(Key key, int amount){
        int blockValue;
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || SKey.of("HOPPER:0").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);

            if(currentAmount <= amount)
                blocksCalculations.remove(key);
            else
                blocksCalculations.put(key, currentAmount - amount);

            if((islandWorth = islandWorth.subtract(new BigDecimal(blockValue).multiply(new BigDecimal(amount)))).doubleValue() < 0)
                islandWorth = new BigDecimal(0);
        }
    }

    @Override
    public int getHoppersAmount(){
        return getBlockCount(SKey.of("HOPPER"));
    }

    @Override
    public int getBlockCount(Key key){
        return blocksCalculations.getOrDefault(key, 0);
    }

    @Override
    public double getWorth(){
        int bankWorthRate = plugin.getSettings().bankWorthRate;
        //noinspection BigDecimalMethodWithoutRoundingCalled
        return bankWorthRate <= 0 ? islandWorth.doubleValue() : islandWorth.add(islandBank.divide(new BigDecimal(bankWorthRate))).doubleValue();
    }

    @Override
    public double getRawWorth(){
        return islandWorth.doubleValue();
    }

    public String getWorthAsString(){
        return numberFormatter.format(getWorth());
    }

    @Override
    public long getIslandLevel(){
        double worth = getWorth();
        try {
            return new BigDecimal(engine.eval(plugin.getSettings().islandLevelFormula.
                    replace("{}", String.valueOf(worth))).toString()).longValue();
        }catch(Exception ex){
            return (long) worth;
        }
    }

    @Override
    public String getLevelAsString() {
        return numberFormatter.format(getIslandLevel());
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
    }

    @Override
    public int getIslandSize() {
        return getOwner().getIslandSize();
    }

    @Override
    public int getHoppersLimit(){
        return hoppersLimit;
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
        getOwner().setIslandSize(islandSize);
    }

    @Override
    public void setHoppersLimit(int hoppersLimit){
        this.hoppersLimit = hoppersLimit;
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        this.cropGrowth = cropGrowth;
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.spawnerRates = spawnerRates;
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        this.mobDrops = mobDrops;
    }

    @Override
    public String getDiscord() {
        return discord;
    }

    @Override
    public void setDiscord(String discord) {
        this.discord = discord;
    }

    @Override
    public String getPaypal() {
        return paypal;
    }

    @Override
    public void setPaypal(String paypal) {
        this.paypal = paypal;
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
        return warps.containsKey(name.toLowerCase()) ? warps.get(name.toLowerCase()).clone() : null;
    }

    @Override
    public void setWarpLocation(String name, Location location){
        warps.put(name.toLowerCase(), location.clone());
    }

    @Override
    public void warpPlayer(SuperiorPlayer superiorPlayer, String warp){
        Location location = warps.get(warp.toLowerCase()).clone();
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

        superiorPlayer.asPlayer().teleport(location.add(0.5, 0, 0.5));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(superiorPlayer.getLocation().distanceSquared(location) < 25)
                Locale.TELEPORTED_TO_WARP.send(superiorPlayer);
        }, 2L);
    }

    @Override
    public void deleteWarp(SuperiorPlayer superiorPlayer, Location location){
        for(String warpName : warps.keySet()){
            if(warps.get(warpName).distanceSquared(location) < 2){
                warps.remove(warpName);
                Locale.DELETE_WARP.send(superiorPlayer, warpName);
            }
        }
    }

    @Override
    public void deleteWarp(String name){
        warps.remove(name);
    }

    @Override
    public List<String> getAllWarps(){
        return new ArrayList<>(warps.keySet());
    }

    public CompoundTag getAsTag(){
        Map<String, Tag> compoundValues = new HashMap<>();

        compoundValues.put("owner", new StringTag(owner.toString()));
        compoundValues.put("center", new StringTag(center.toString()));

        List<Tag> members = new ArrayList<>();
        this.members.forEach(uuid -> members.add(new StringTag(uuid.toString())));
        compoundValues.put("members", new ListTag(StringTag.class, members));

        List<Tag> banned = new ArrayList<>();
        this.banned.forEach(uuid -> banned.add(new StringTag(uuid.toString())));
        compoundValues.put("banned", new ListTag(StringTag.class, banned));

        Map<String, Tag> permissionNodes = new HashMap<>();
        this.permissionNodes.keySet()
                .forEach(islandRole -> permissionNodes.put(islandRole.name(), this.permissionNodes.get(islandRole).getAsTag()));
        compoundValues.put("permissionNodes", new CompoundTag(permissionNodes));

        Map<String, Tag> upgrades = new HashMap<>();
        this.upgrades.keySet()
                .forEach(upgrade -> upgrades.put(upgrade, new IntTag(this.upgrades.get(upgrade))));
        compoundValues.put("upgrades", new CompoundTag(upgrades));

        Map<String, Tag> warps = new HashMap<>();
        this.warps.keySet()
                .forEach(warp -> warps.put(warp, new StringTag(FileUtil.fromLocation(this.warps.get(warp)))));
        compoundValues.put("warps", new CompoundTag(warps));

        compoundValues.put("islandBank", new DoubleTag(this.islandBank.doubleValue()));

        compoundValues.put("hoppersLimit", new IntTag(this.hoppersLimit));
        compoundValues.put("teamLimit", new IntTag(this.teamLimit));
        compoundValues.put("cropGrowth", new DoubleTag(this.cropGrowth));
        compoundValues.put("spawnerRates", new DoubleTag(this.spawnerRates));
        compoundValues.put("mobDrops", new DoubleTag(this.mobDrops));
        compoundValues.put("discord", new StringTag(this.discord));
        compoundValues.put("paypal", new StringTag(this.paypal));

        return new CompoundTag(compoundValues);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SIsland ? owner.equals(((SIsland) obj).owner) : super.equals(obj);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Island other) {
        if(other == null)
            return -1;
        if(plugin.getSettings().islandTopOrder.equals("WORTH")){
            if (getWorth() > other.getWorth())
                return 1;
            else if (getWorth() < other.getWorth())
                return -1;
        }
        else {
            if (getIslandLevel() > other.getIslandLevel())
                return 1;
            else if (getIslandLevel() < other.getIslandLevel())
                return -1;
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

    private class CalcIslandData{

        private UUID owner, asker;

        private CalcIslandData(UUID owner, UUID asker){
            this.owner = owner;
            this.asker = asker;
        }

    }


}
