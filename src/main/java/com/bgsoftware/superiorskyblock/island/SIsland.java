package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.database.Query;
import com.bgsoftware.superiorskyblock.hooks.WildStackerHook;
import com.bgsoftware.superiorskyblock.listeners.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.DoubleTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.IntTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.StringTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.queue.Queue;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SIsland extends DatabaseObject implements Island {

    protected static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static boolean calcProcess = false;
    private static Queue<CalcIslandData> islandCalcsQueue = new Queue<>();
    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
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
    private final Map<IslandRole, SPermissionNode> permissionNodes = new HashMap<>();
    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Set<UUID> invitedPlayers = new HashSet<>();
    private final KeyMap<Integer> blocksCalculations = new KeyMap<>();
    private final Map<String, Location> warps = new HashMap<>();
    private BigDecimalFormatted islandBank = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted islandWorth = BigDecimalFormatted.ZERO;
    private BigDecimalFormatted bonusWorth = BigDecimalFormatted.ZERO;
    private String discord = "None", paypal = "None";
    private int islandSize = plugin.getSettings().defaultIslandSize;
    protected Location teleportLocation;

    /*
     * SIsland multipliers & limits
     */

    private int warpsLimit = plugin.getSettings().defaultWarpsLimit;
    private int hoppersLimit = plugin.getSettings().defaultHoppersLimit;
    private int teamLimit = plugin.getSettings().defaultTeamLimit;
    private double cropGrowth = plugin.getSettings().defaultCropGrowth;
    private double spawnerRates = plugin.getSettings().defaultSpawnerRates;
    private double mobDrops = plugin.getSettings().defaultMobDrops;

    private boolean transferred = false;
    private UUID prevOwner;

    public SIsland(ResultSet resultSet) throws SQLException {
        this.owner = UUID.fromString(resultSet.getString("owner"));
        this.center = SBlockPosition.of(getLocation(resultSet.getString("center")));

        this.teleportLocation = getLocation(resultSet.getString("teleportLocation"));

        for(String uuid : resultSet.getString("members").split(",")) {
            try {
                this.members.add(UUID.fromString(uuid));
            }catch(Exception ignored){}
        }

        for(String uuid : resultSet.getString("banned").split(",")) {
            try {
                this.banned.add(UUID.fromString(uuid));
            }catch(Exception ignored){}
        }

        for(String entry : resultSet.getString("permissionNodes").split(",")) {
            try {
                String[] sections = entry.split("=");
                this.permissionNodes.put(IslandRole.valueOf(sections[0]), new SPermissionNode(sections.length == 1 ? "" : sections[1]));
            }catch(Exception ignored){}
        }

        for(String entry : resultSet.getString("upgrades").split(",")) {
            try {
                String[] sections = entry.split("=");
                this.upgrades.put(sections[0], Integer.valueOf(sections[1]));
            }catch(Exception ignored){}
        }

        for(String entry : resultSet.getString("warps").split(",")) {
            try {
                String[] sections = entry.split("=");
                this.warps.put(sections[0], FileUtil.toLocation(sections[1]));
            }catch(Exception ignored){}
        }

        this.islandBank = BigDecimalFormatted.of(resultSet.getString("islandBank"));
        this.bonusWorth = BigDecimalFormatted.of(resultSet.getString("bonusWorth"));
        this.islandSize = resultSet.getInt("islandSize");

        for(String limit : resultSet.getString("blockLimits").split(",")){
            try {
                this.hoppersLimit = Integer.valueOf(limit.split("=")[1]);
            }catch(Exception ignored){}
        }

        this.teamLimit = resultSet.getInt("teamLimit");
        this.warpsLimit =  resultSet.getInt("warpsLimit");
        this.cropGrowth = resultSet.getDouble("cropGrowth");
        this.spawnerRates = resultSet.getDouble("spawnerRates");
        this.mobDrops = resultSet.getDouble("mobDrops");
        this.discord = resultSet.getString("discord");
        this.paypal = resultSet.getString("paypal");

        calcIslandWorth(null);
    }

    public SIsland(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue();
        this.owner = UUID.fromString(((StringTag) compoundValues.get("owner")).getValue());
        this.center = SBlockPosition.of(((StringTag) compoundValues.get("center")).getValue());

        this.teleportLocation = compoundValues.containsKey("teleportLocation") ?
                getLocation(((StringTag) compoundValues.get("teleportLocation")).getValue()) : getCenter();

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

        this.islandBank = BigDecimalFormatted.of(compoundValues.get("islandBank"));
        this.islandSize = ((IntTag) compoundValues.getOrDefault("islandSize", new IntTag(plugin.getSettings().defaultIslandSize))).getValue();

        this.hoppersLimit = ((IntTag) compoundValues.get("hoppersLimit")).getValue();
        this.teamLimit = ((IntTag) compoundValues.get("teamLimit")).getValue();
        this.warpsLimit = compoundValues.containsKey("warpsLimit") ? ((IntTag) compoundValues.get("warpsLimit")).getValue() : plugin.getSettings().defaultWarpsLimit;
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
        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void kickMember(SuperiorPlayer superiorPlayer){
        members.remove(superiorPlayer.getUniqueId());
        superiorPlayer.setTeamLeader(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_MEMBERS.getStatementHolder()
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void banMember(SuperiorPlayer superiorPlayer){
        if(isMember(superiorPlayer)) kickMember(superiorPlayer);
        banned.add(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void unbanMember(SuperiorPlayer superiorPlayer) {
        banned.remove(superiorPlayer.getUniqueId());
        Query.ISLAND_SET_BANNED.getStatementHolder()
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(owner.toString())
                .execute();
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
    public Location getTeleportLocation() {
        if(teleportLocation == null)
            teleportLocation = getCenter();
        return teleportLocation.clone();
    }

    @Override
    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation.clone();
        Query.ISLAND_SET_TELEPORT_LOCATION.getStatementHolder()
                .setString(getLocation(getTeleportLocation()))
                .setString(owner.toString())
                .execute();
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

        StringBuilder permissionNodes = new StringBuilder();
        this.permissionNodes.keySet().forEach(_islandRole ->
                permissionNodes.append(",").append(_islandRole.name()).append("=").append(this.permissionNodes.get(_islandRole).getAsStatementString()));

        Query.ISLAND_SET_PERMISSION_NODES.getStatementHolder()
                .setString(permissionNodes.length() == 0 ? "" : permissionNodes.toString())
                .setString(owner.toString())
                .execute();
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
        new HashSet<>(members).forEach(member -> kickMember(SSuperiorPlayer.of(member)));
        plugin.getGrid().deleteIsland(this);
        for(Chunk chunk : getAllChunks(true))
            chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
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
                .execute();
    }

    @Override
    public void withdrawMoney(double amount){
        islandBank = islandBank.subtract(BigDecimalFormatted.of(amount));
        Query.ISLAND_SET_BANK.getStatementHolder()
                .setString(islandBank.getAsString())
                .setString(owner.toString())
                .execute();
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
        Map<Location, Map.Entry<Integer, EntityType>> spawners = new HashMap<>();
        Map<Location, Map.Entry<Integer, Material>> blocks = new HashMap<>();

        for (Chunk chunk : getAllChunks(true)) {
            chunkSnapshots.add(chunk.getChunkSnapshot(true, false, false));
            spawners.putAll(WildStackerHook.getAllSpawners(chunk));
            blocks.putAll(WildStackerHook.getAllBarrels(chunk));
        }

        blocksCalculations.clear();
        islandWorth = BigDecimalFormatted.ZERO;

        World world = Bukkit.getWorld(chunkSnapshots.get(0).getWorldName());

        new SuperiorThread(() -> {
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
                                Key blockKey = SKey.of("AIR");

                                try{
                                    blockKey = plugin.getNMSAdapter().getBlockKey(chunkSnapshot, x, y, z);
                                }catch(ArrayIndexOutOfBoundsException ignored){ }

                                if(blockKey.toString().contains("AIR"))
                                    continue;
                                Location location = new Location(world, (chunkSnapshot.getX() * 16) + x, y, (chunkSnapshot.getZ() * 16) + z);
                                int blockCount = 1;

                                if(spawners.containsKey(location)){
                                    Map.Entry<Integer, EntityType> entry = spawners.get(location);
                                    blockCount = entry.getKey();
                                    blockKey = SKey.of(Materials.SPAWNER.toBukkitType().name() + ":" + entry.getValue());
                                }

                                else if(blocks.containsKey(location)){
                                    Map.Entry<Integer, EntityType> entry = spawners.get(location);
                                    blockCount = entry.getKey();
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
                Bukkit.getPluginManager().callEvent(new IslandWorthCalculatedEvent(this, getIslandLevelAsBigDecimal(), asker));

                if(asker != null)
                    Locale.ISLAND_WORTH_RESULT.send(asker, getWorthAsBigDecimal(), getIslandLevelAsBigDecimal());
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
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || SKey.of("HOPPER").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);
            blocksCalculations.put(plugin.getGrid().getBlockValueKey(key), currentAmount + amount);
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
        if((blockValue = plugin.getGrid().getBlockValue(key)) > 0 || SKey.of("HOPPER").equals(key)){
            int currentAmount = blocksCalculations.getOrDefault(key, 0);

            key = plugin.getGrid().getBlockValueKey(key);

            if(currentAmount <= amount)
                blocksCalculations.remove(key);
            else
                blocksCalculations.put(key, currentAmount - amount);

            if((islandWorth = islandWorth.subtract(new BigDecimal(blockValue).multiply(new BigDecimal(amount)))).doubleValue() < 0)
                islandWorth = BigDecimalFormatted.ZERO;
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

    public void setBonusWorth(BigDecimal bonusWorth){
        this.bonusWorth = BigDecimalFormatted.of(bonusWorth);
        Query.ISLAND_SET_BONUS_WORTH.getStatementHolder()
                .setString(this.bonusWorth.getAsString())
                .setString(owner.toString())
                .execute();
    }

    @Override
    @Deprecated
    public int getIslandLevel(){
        return getIslandLevelAsBigDecimal().intValue();
    }

    @Override
    public BigDecimal getIslandLevelAsBigDecimal() {
        BigDecimalFormatted worth = (BigDecimalFormatted) getWorthAsBigDecimal();
        try {
            BigDecimal level = new BigDecimal(engine.eval(plugin.getSettings().islandLevelFormula.replace("{}", worth.getAsString())).toString());
            return BigDecimalFormatted.of(level.toBigInteger());
        }catch(Exception ex){
            ex.printStackTrace();
            return worth;
        }
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

        StringBuilder upgrades = new StringBuilder();
        this.upgrades.keySet().forEach(upgrade ->
                upgrades.append(",").append(upgrade).append("=").append(this.upgrades.get(upgrade)));

        Query.ISLAND_SET_UPGRADES.getStatementHolder()
                .setString(upgrades.length() == 0 ? "" : upgrades.toString())
                .setString(owner.toString())
                .execute();
    }

    @Override
    public int getIslandSize() {
        return islandSize;
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
        this.islandSize = islandSize;
        Query.ISLAND_SET_SIZE.getStatementHolder()
                .setInt(islandSize)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void updateBorder() {
        allPlayersInside().forEach(uuid -> plugin.getNMSAdapter().setWorldBorder(SSuperiorPlayer.of(uuid), this));
    }

    @Override
    public void setHoppersLimit(int hoppersLimit){
        this.hoppersLimit = hoppersLimit;
        Query.ISLAND_SET_BLOCK_LIMITS.getStatementHolder()
                .setString("HOPPER=" + this.hoppersLimit)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void setTeamLimit(int teamLimit) {
        this.teamLimit = teamLimit;
        Query.ISLAND_SET_TEAM_LIMIT.getStatementHolder()
                .setInt(teamLimit)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void setCropGrowthMultiplier(double cropGrowth) {
        this.cropGrowth = cropGrowth;
        Query.ISLAND_SET_CROP_GROWTH.getStatementHolder()
                .setDouble(cropGrowth)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void setSpawnerRatesMultiplier(double spawnerRates) {
        this.spawnerRates = spawnerRates;
        Query.ISLAND_SET_SPAWNER_RATES.getStatementHolder()
                .setDouble(spawnerRates)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void setMobDropsMultiplier(double mobDrops) {
        this.mobDrops = mobDrops;
        Query.ISLAND_SET_MOB_DROPS.getStatementHolder()
                .setDouble(mobDrops)
                .setString(owner.toString())
                .execute();
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
                .execute();
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
                .execute();
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

        StringBuilder warps = new StringBuilder();
        this.warps.keySet().forEach(warp ->
                warps.append(",").append(warp).append("=").append(FileUtil.fromLocation(this.warps.get(warp))));

        Query.ISLAND_SET_WARPS.getStatementHolder()
                .setString(warps.length() == 0 ? "" : warps.toString())
                .setString(owner.toString())
                .execute();
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

        if(superiorPlayer.asPlayer().teleport(location.add(0.5, 0, 0.5))){
            Locale.TELEPORTED_TO_WARP.send(superiorPlayer);
        }
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
                .execute();
    }

    @Override
    public int getWarpsLimit() {
        return warpsLimit;
    }

    @Override
    public void transfer(SuperiorPlayer player) {
        if (player.getUniqueId().equals(owner))
            return;

        if (prevOwner == null)
            prevOwner = owner;
        transferred = true;

        SuperiorPlayer previous = getOwner();

        members.remove(player.getUniqueId());
        members.add(previous.getUniqueId());

        owner = player.getUniqueId();

        for (UUID member : getAllMembers())
            Objects.requireNonNull(SSuperiorPlayer.of(member)).setTeamLeader(owner);

        previous.setTeamLeader(owner);
        player.setTeamLeader(owner);

        player.setIslandRole(IslandRole.LEADER);
        previous.setIslandRole(IslandRole.ADMIN);

        plugin.getGrid().getIslands().transfer(previous.getUniqueId(), owner);

        executeDeleteStatement();
        executeInsertStatement();
    }

    @Override
    public void executeUpdateStatement(){
        StringBuilder permissionNodes = new StringBuilder();
        this.permissionNodes.keySet().forEach(islandRole ->
                permissionNodes.append(",").append(islandRole.name()).append("=").append(this.permissionNodes.get(islandRole).getAsStatementString()));

        StringBuilder upgrades = new StringBuilder();
        this.upgrades.keySet().forEach(upgrade ->
                upgrades.append(",").append(upgrade).append("=").append(this.upgrades.get(upgrade)));

        StringBuilder warps = new StringBuilder();
        this.warps.keySet().forEach(warp ->
                warps.append(",").append(warp).append("=").append(FileUtil.fromLocation(this.warps.get(warp))));

        Query.ISLAND_UPDATE.getStatementHolder()
                .setString(getLocation(getTeleportLocation()))
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(permissionNodes.length() == 0 ? "" : permissionNodes.toString())
                .setString(upgrades.length() == 0 ? "" : upgrades.toString())
                .setString(warps.length() == 0 ? "" : warps.toString())
                .setString(islandBank.getAsString())
                .setInt(islandSize)
                .setString("HOPPER=" + this.hoppersLimit)
                .setInt(teamLimit)
                .setFloat((float) cropGrowth)
                .setFloat((float) spawnerRates)
                .setFloat((float) mobDrops)
                .setString(discord)
                .setString(paypal)
                .setString(bonusWorth.getAsString())
                .setInt(warpsLimit)
                .setString(owner.toString())
                .execute();
    }

    @Override
    public void executeDeleteStatement(){
        Query.ISLAND_DELETE.getStatementHolder()
                .setString(prevOwner.toString())
                .execute();
    }

    @Override
    public void executeInsertStatement(){
        StringBuilder permissionNodes = new StringBuilder();
        this.permissionNodes.keySet().forEach(islandRole ->
                permissionNodes.append(",").append(islandRole.name()).append("=").append(this.permissionNodes.get(islandRole).getAsStatementString()));

        StringBuilder upgrades = new StringBuilder();
        this.upgrades.keySet().forEach(upgrade ->
                upgrades.append(",").append(upgrade).append("=").append(this.upgrades.get(upgrade)));

        StringBuilder warps = new StringBuilder();
        this.warps.keySet().forEach(warp ->
                warps.append(",").append(warp).append("=").append(FileUtil.fromLocation(this.warps.get(warp))));

        Query.ISLAND_INSERT.getStatementHolder()
                .setString(owner.toString())
                .setString(getLocation(center.getBlock().getLocation()))
                .setString(getLocation(getTeleportLocation()))
                .setString(members.isEmpty() ? "" : getUuidCollectionString(members))
                .setString(banned.isEmpty() ? "" : getUuidCollectionString(banned))
                .setString(permissionNodes.length() == 0 ? "" : permissionNodes.toString())
                .setString(upgrades.length() == 0 ? "" : upgrades.toString())
                .setString(warps.length() == 0 ? "" : warps.toString())
                .setString(islandBank.getAsString())
                .setInt(islandSize)
                .setString("HOPPER=" + this.hoppersLimit)
                .setInt(teamLimit)
                .setFloat((float) cropGrowth)
                .setFloat((float) spawnerRates)
                .setFloat((float) mobDrops)
                .setString(discord)
                .setString(paypal)
                .setString(bonusWorth.getAsString())
                .setInt(warpsLimit)
                .execute();
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

    private class CalcIslandData{

        private UUID owner, asker;

        private CalcIslandData(UUID owner, UUID asker){
            this.owner = owner;
            this.asker = asker;
        }

    }

    private Location getLocation(String location){
        String[] sections = location.split(",");

        World world = Bukkit.getWorld(sections[0]);
        double x = Double.parseDouble(sections[1]);
        double y = Double.parseDouble(sections[2]);
        double z = Double.parseDouble(sections[3]);
        float yaw = sections.length > 5 ? Float.parseFloat(sections[4]) : 0;
        float pitch = sections.length > 4 ? Float.parseFloat(sections[5]) : 0;

        return new Location(world, x, y, z, yaw, pitch);
    }

    private String getLocation(Location location){
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }
}
