package com.ome_r.superiorskyblock.island;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.utils.FileUtil;
import com.ome_r.superiorskyblock.utils.queue.Queue;
import com.ome_r.superiorskyblock.utils.key.Key;
import com.ome_r.superiorskyblock.utils.key.KeyMap;
import com.ome_r.superiorskyblock.utils.jnbt.CompoundTag;
import com.ome_r.superiorskyblock.utils.jnbt.DoubleTag;
import com.ome_r.superiorskyblock.utils.jnbt.IntTag;
import com.ome_r.superiorskyblock.utils.jnbt.ListTag;
import com.ome_r.superiorskyblock.utils.jnbt.StringTag;
import com.ome_r.superiorskyblock.utils.jnbt.Tag;
import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Island implements Comparable<Island> {

    protected static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

    private static boolean calcProcess = false;
    private static Queue<CalcIslandData> islandCalcsQueue = new Queue<>();
    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    /*
     * Island identifiers
     */

    private final UUID owner;
    private final WrappedLocation center;

    /*
     * Island data
     */

    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> banned = new HashSet<>();
    private final Map<IslandRole, PermissionNode> permissionNodes = new HashMap<>();
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final KeyMap<Integer> blocksCalculations = new KeyMap<>();
    private final Map<String, Location> warps = new HashMap<>();
    private double islandBank = 0;
    private double islandWorth = 0;
    private String discord = "None", paypal = "None";

    /*
     * Island multipliers & limits
     */

    private int hoppersLimit = plugin.getSettings().defaultHoppersLimit;
    private int teamLimit = plugin.getSettings().defaultTeamLimit;
    private double cropGrowth = plugin.getSettings().defaultCropGrowth;
    private double spawnerRates = plugin.getSettings().defaultSpawnerRates;
    private double mobDrops = plugin.getSettings().defaultMobDrops;

    public Island(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();
        this.owner = UUID.fromString(((StringTag) compoundValues.get("owner")).getValue());
        this.center = WrappedLocation.of(((StringTag) compoundValues.get("center")).getValue());

        List<Tag> members = ((ListTag) compoundValues.get("members")).getValue();
        for(Tag _tag : members)
            this.members.add(UUID.fromString(((StringTag) _tag).getValue()));

        List<Tag> banned = ((ListTag) compoundValues.get("banned")).getValue();
        for(Tag _tag : banned)
            this.banned.add(UUID.fromString(((StringTag) _tag).getValue()));

        Map<String, Tag> permissionNodes = ((CompoundTag) compoundValues.get("permissionNodes")).getValue();
        for(String islandRole : permissionNodes.keySet())
            this.permissionNodes.put(IslandRole.valueOf(islandRole), new PermissionNode((ListTag) permissionNodes.get(islandRole)));

        Map<String, Tag> upgrades = ((CompoundTag) compoundValues.get("upgrades")).getValue();
        for(String upgrade : upgrades.keySet())
            this.upgrades.put(upgrade, ((IntTag) upgrades.get(upgrade)).getValue());

        Map<String, Tag> warps = ((CompoundTag) compoundValues.get("warps")).getValue();
        for(String warp : warps.keySet())
            this.warps.put(warp, FileUtil.toLocation(((StringTag) warps.get(warp)).getValue()));

        this.islandBank = ((DoubleTag) compoundValues.get("islandBank")).getValue();

        this.hoppersLimit = ((IntTag) compoundValues.get("hoppersLimit")).getValue();
        this.teamLimit = ((IntTag) compoundValues.get("teamLimit")).getValue();
        this.cropGrowth = ((DoubleTag) compoundValues.get("cropGrowth")).getValue();
        this.spawnerRates = ((DoubleTag) compoundValues.get("spawnerRates")).getValue();
        this.mobDrops = ((DoubleTag) compoundValues.get("mobDrops")).getValue();
        this.discord = ((StringTag) compoundValues.get("discord")).getValue();
        this.paypal = ((StringTag) compoundValues.get("paypal")).getValue();

        calcIslandWorth(null);
    }

    public Island(WrappedPlayer wrappedPlayer, Location location){
        this(wrappedPlayer, WrappedLocation.of(location));
    }

    public Island(WrappedPlayer wrappedPlayer, WrappedLocation wrappedLocation){
        if(wrappedPlayer != null){
            this.owner = wrappedPlayer.getTeamLeader();
            wrappedPlayer.setIslandRole(IslandRole.LEADER);
        }else{
            this.owner = null;
        }
        this.center = wrappedLocation;
        assignPermissionNodes();
    }

    public WrappedPlayer getOwner() {
        return WrappedPlayer.of(owner);
    }

    public List<UUID> getMembers() {
        return new ArrayList<>(members);
    }

    public void inviteMember(WrappedPlayer wrappedPlayer){
        if(invitedPlayers.contains(wrappedPlayer.getUniqueId()))
            return;

        invitedPlayers.add(wrappedPlayer.getUniqueId());
    }

    public void revokeInvite(WrappedPlayer wrappedPlayer){
        invitedPlayers.remove(wrappedPlayer.getUniqueId());
    }

    public boolean isInvited(WrappedPlayer wrappedPlayer){
        return invitedPlayers.contains(wrappedPlayer.getUniqueId());
    }

    public void addMember(WrappedPlayer wrappedPlayer, IslandRole islandRole){
        members.add(wrappedPlayer.getUniqueId());
        wrappedPlayer.setTeamLeader(owner);
        wrappedPlayer.setIslandRole(islandRole);
    }

    public void kickMember(WrappedPlayer wrappedPlayer){
        members.remove(wrappedPlayer.getUniqueId());
        wrappedPlayer.setTeamLeader(wrappedPlayer.getUniqueId());
    }

    public void banMember(WrappedPlayer wrappedPlayer){
        if(isMember(wrappedPlayer)) kickMember(wrappedPlayer);
        banned.add(wrappedPlayer.getUniqueId());
    }

    public boolean isBanned(WrappedPlayer wrappedPlayer){
        return banned.contains(wrappedPlayer.getUniqueId());
    }

    public List<UUID> getAllMembers() {
        List<UUID> members = new ArrayList<>();

        members.add(owner);
        members.addAll(getMembers());

        return members;
    }

    public List<UUID> getVisitors(){
        List<UUID> visitors = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()){
            if(!isMember(WrappedPlayer.of(player)) && isInside(player.getLocation()))
                visitors.add(player.getUniqueId());
        }

        return visitors;
    }

    public List<UUID> allPlayersInside(){
        List<UUID> visitors = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()){
            if(isInside(player.getLocation()))
                visitors.add(player.getUniqueId());
        }

        return visitors;
    }

    public boolean isMember(WrappedPlayer wrappedPlayer){
        return owner.equals(wrappedPlayer.getTeamLeader());
    }

    public Location getCenter(){
        return center.parse().add(0.5, 0, 0.5);
    }

    public Location getMinimum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter().subtract(islandDistance, 0, islandDistance);
    }

    public Location getMaximum(){
        int islandDistance = plugin.getSettings().maxIslandSize;
        return getCenter().add(islandDistance, 0, islandDistance);
    }

    public boolean hasPermission(CommandSender sender, IslandPermission islandPermission){
        return sender instanceof ConsoleCommandSender || hasPermission(WrappedPlayer.of(sender), islandPermission);
    }

    public boolean hasPermission(WrappedPlayer wrappedPlayer, IslandPermission islandPermission){
        IslandRole islandRole = isMember(wrappedPlayer) ? wrappedPlayer.getIslandRole() : IslandRole.GUEST;
        return wrappedPlayer.hasBypassModeEnabled() || permissionNodes.get(islandRole).hasPermission(islandPermission);
    }

    public void setPermission(IslandRole islandRole, IslandPermission islandPermission, boolean value){
        permissionNodes.get(islandRole).setPermission(islandPermission, value);
    }

    public PermissionNode getPermisisonNode(IslandRole islandRole){
        return permissionNodes.get(islandRole).clone();
    }

    public IslandRole getRequiredRole(IslandPermission islandPermission){
        IslandRole islandRole = IslandRole.LEADER;

        for(IslandRole _islandRole : IslandRole.values()){
            if(_islandRole.isLessThan(islandRole) && permissionNodes.get(_islandRole).hasPermission(islandPermission))
                islandRole = _islandRole;
        }

        return islandRole;
    }

    public void disbandIsland(){
        members.forEach(member -> kickMember(WrappedPlayer.of(member)));
        plugin.getGrid().deleteIsland(this);
    }

    public List<Chunk> getAllChunks(){
        Set<Chunk> chunks = new HashSet<>();
        Chunk minChunk = getMinimum().getChunk(), maxChunk = getMaximum().getChunk();

        for(int x = minChunk.getX(); x <= maxChunk.getX(); x++){
            for(int z = minChunk.getZ(); z <= maxChunk.getZ(); z++){
                chunks.add(minChunk.getWorld().getChunkAt(x, z));
            }
        }

        return new ArrayList<>(chunks);
    }

    public double getMoneyInBank(){
        if(islandBank < 0) islandBank = 0;
        return islandBank;
    }

    public void depositMoney(double amount){
        islandBank += amount;
    }

    public void withdrawMoney(double amount){
        islandBank -= amount;
    }

    public void calcIslandWorth(WrappedPlayer asker) {
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

        for (Chunk chunk : getAllChunks())
            chunkSnapshots.add(chunk.getChunkSnapshot(true, false, false));

        islandWorth = 0;

        new Thread(() -> {
            for (ChunkSnapshot chunkSnapshot : chunkSnapshots) {
                int highestBlock;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        highestBlock = chunkSnapshot.getHighestBlockYAt(x, z);
                        for (int y = 0; y <= highestBlock; y++) {
                            islandWorth += plugin.getGrid().getBlockValue(plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y, z));
                        }
                    }
                }
            }

            calcProcess = false;
            if(asker != null)
                Locale.ISLAND_WORTH_RESULT.send(asker, getWorth(), getIslandLevel());

            if(islandCalcsQueue.size() != 0){
                CalcIslandData calcIslandData = islandCalcsQueue.pop();
                plugin.getGrid().getIsland(WrappedPlayer.of(calcIslandData.owner))
                        .calcIslandWorth(calcIslandData.asker == null ? null : WrappedPlayer.of(calcIslandData.asker));
            }
        }).start();
    }

    public void handleBlockPlace(Block block){
        handleBlockPlace(Key.of(block));
    }

    public void handleBlockPlace(Key key){
        int blockValue;
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || Key.of("HOPPER").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);
            blocksCalculations.put(key, currentAmount + 1);
            islandWorth += blockValue;
        }
    }

    public void handleBlockBreak(Block block){
        handleBlockBreak(Key.of(block));
    }

    public void handleBlockBreak(Key key){
        int blockValue;
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || Key.of("HOPPER").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);
            if(currentAmount <= 1)
                blocksCalculations.remove(key);
            else
                blocksCalculations.put(key, currentAmount - 1);
            if((islandWorth -= blockValue) < 0)
                islandWorth = 0;
        }
    }

    public int getHoppersAmount(){
        return getBlockCount(Key.of("HOPPER"));
    }

    public int getBlockCount(Key key){
        return blocksCalculations.getOrDefault(key, 0);
    }

    public double getWorth(){
        int bankWorthRate = plugin.getSettings().bankWorthRate;
        return bankWorthRate <= 0 ? islandWorth : islandWorth + (islandBank / bankWorthRate);
    }

    public double getRawWorth(){
        return islandWorth;
    }

    public int getIslandLevel(){
        double worth = getWorth();
        try {
            return (int) ((double) engine.eval(plugin.getSettings().islandLevelFormula.
                    replace("{}", String.valueOf(worth))));
        }catch(Exception ex){
            return (int) worth;
        }
    }

    public boolean isInside(Location location){
        Location min = getMinimum(), max = getMaximum();
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isInsideRange(Location location){
        int islandSize = getIslandSize();
        Location min = center.parse().subtract(islandSize, 0, islandSize);
        Location max = center.parse().add(islandSize, 0, islandSize);
        return min.getBlockX() <= location.getBlockX() && min.getBlockZ() <= location.getBlockZ() &&
                max.getBlockX() >= location.getBlockX() && max.getBlockZ() >= location.getBlockZ();
    }

    public int getUpgradeLevel(String upgradeName){
        return upgrades.getOrDefault(upgradeName, 1);
    }

    public void setUpgradeLevel(String upgradeName, int level){
        upgrades.put(upgradeName, Math.min(plugin.getUpgrades().getMaxUpgradeLevel(upgradeName), level));
    }

    public int getIslandSize() {
        return getOwner().getIslandSize();
    }

    public int getHoppersLimit(){
        return hoppersLimit;
    }

    public int getTeamLimit() {
        return teamLimit;
    }

    public double getCropGrowthMultiplier() {
        return cropGrowth;
    }

    public double getSpawnerRatesMultiplier() {
        return spawnerRates;
    }

    public double getMobDropsMultiplier() {
        return mobDrops;
    }

    public void setIslandSize(int islandSize) {
        getOwner().setIslandSize(islandSize);
    }

    public void setHoppersLimit(int hoppersLimit){
        this.hoppersLimit = hoppersLimit;
    }

    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
    }

    public void setCropGrowthMultiplier(double cropGrowth) {
        this.cropGrowth = cropGrowth;
    }

    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.spawnerRates = spawnerRates;
    }

    public void setMobDropsMultiplier(double mobDrops) {
        this.mobDrops = mobDrops;
    }

    public String getDiscord() {
        return discord;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public String getPaypal() {
        return paypal;
    }

    public void setPaypal(String paypal) {
        this.paypal = paypal;
    }

    public void setBiome(Biome biome){
        Location min = getMinimum(), max = getMaximum();
        for(int x = min.getBlockX(); x <= max.getBlockX(); x++){
            for(int z = min.getBlockZ(); z <= max.getBlockZ(); z++){
                center.getWorld().setBiome(x, z, biome);
            }
        }
    }

    public void sendMessage(String message, UUID... ignoredMembers){
        List<UUID> ignoredList = Arrays.asList(ignoredMembers);
        WrappedPlayer targetPlayer;
        for(UUID uuid : getAllMembers()){
            if(!ignoredList.contains(uuid)) {
                if ((targetPlayer = WrappedPlayer.of(uuid)).asOfflinePlayer().isOnline())
                    Locale.sendMessage(targetPlayer, message);
            }
        }
    }

    public Location getWarpLocation(String name){
        return warps.containsKey(name.toLowerCase()) ? warps.get(name.toLowerCase()).clone() : null;
    }

    public void setWarpLocation(String name, Location location){
        warps.put(name.toLowerCase(), location.clone());
    }

    public void warpPlayer(WrappedPlayer wrappedPlayer, String warp){
        Location location = warps.get(warp.toLowerCase()).clone();
        Block warpBlock = location.getBlock();

        if(!warpBlock.getRelative(BlockFace.DOWN).getType().isSolid() &&
                !warpBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType().isSolid()){
            Locale.UNSAFE_WARP.send(wrappedPlayer);
            return;
        }

        wrappedPlayer.asPlayer().teleport(location.add(0.5, 0, 0.5));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(wrappedPlayer.getLocation().distanceSquared(location) < 25)
                Locale.TELEPORTED_TO_WARP.send(wrappedPlayer);
        }, 2L);
    }

    public void deleteWarp(WrappedPlayer wrappedPlayer, Location location){
        for(String warpName : warps.keySet()){
            if(warps.get(warpName).distanceSquared(location) < 2){
                warps.remove(warpName);
                Locale.DELETE_WARP.send(wrappedPlayer, warpName);
            }
        }
    }

    public void deleteWarp(String name){
        warps.remove(name);
    }

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

        compoundValues.put("islandBank", new DoubleTag(this.islandBank));

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
        return obj instanceof Island ? owner.equals(((Island) obj).owner) : super.equals(obj);
    }

    @Override
    @SuppressWarnings("NullableProblems")
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
        permissionNodes.put(IslandRole.GUEST, new PermissionNode(null, plugin.getSettings().guestPermissions));
        permissionNodes.put(IslandRole.MEMBER,
                new PermissionNode(permissionNodes.get(IslandRole.GUEST), plugin.getSettings().memberPermissions));
        permissionNodes.put(IslandRole.MODERATOR,
                new PermissionNode(permissionNodes.get(IslandRole.MEMBER), plugin.getSettings().modPermissions));
        permissionNodes.put(IslandRole.ADMIN,
                new PermissionNode(permissionNodes.get(IslandRole.MODERATOR), plugin.getSettings().adminPermission));
        permissionNodes.put(IslandRole.LEADER,
                new PermissionNode(permissionNodes.get(IslandRole.ADMIN), plugin.getSettings().leaderPermissions));
    }

    private class CalcIslandData{

        private UUID owner, asker;

        private CalcIslandData(UUID owner, UUID asker){
            this.owner = owner;
            this.asker = asker;
        }

    }


}
