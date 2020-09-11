package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.menu.MenuTopIslands;
import com.bgsoftware.superiorskyblock.schematics.BaseSchematic;
import com.bgsoftware.superiorskyblock.utils.database.StatementHolder;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.registry.IslandRegistry;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class GridHandler extends AbstractHandler implements GridManager {

    private final IslandRegistry islands = new IslandRegistry();
    private final StackedBlocksHandler stackedBlocks = new StackedBlocksHandler();
    private final Set<UUID> islandsToPurge = Sets.newConcurrentHashSet();
    private final Set<UUID> pendingCreationTasks = Sets.newHashSet();
    private final Set<UUID> customWorlds = Sets.newHashSet();

    private SpawnIsland spawnIsland;
    private SBlockPosition lastIsland;
    private boolean blockFailed = false;

    private BigDecimalFormatted totalWorth = BigDecimalFormatted.ZERO;
    private long lastTimeWorthUpdate = 0;
    private BigDecimalFormatted totalLevel = BigDecimalFormatted.ZERO;
    private long lastTimeLevelUpdate = 0;

    public GridHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData(){
        Executor.sync(() -> {
            lastIsland = SBlockPosition.of(plugin.getSettings().islandWorldName, 0, 100, 0);
            updateSpawn();
        });
    }

    public void updateSpawn(){
        spawnIsland = new SpawnIsland(plugin);
    }

    public void syncUpgrades(){
        getIslands().forEach(island -> ((SIsland) island).updateUpgrades());
    }

    public void createIsland(ResultSet resultSet) throws SQLException{
        UUID owner = UUID.fromString(resultSet.getString("owner"));
        islands.add(owner, new SIsland(this, resultSet));
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName) {
        createIsland(superiorPlayer, schemName, bonus, biome, islandName, false);
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName, boolean offset) {
        createIsland(superiorPlayer, schemName, bonus, BigDecimal.ZERO, biome, islandName, false);
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonusWorth, BigDecimal bonusLevel, Biome biome, String islandName, boolean offset) {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> createIsland(superiorPlayer, schemName, bonusWorth, bonusLevel, biome, islandName, offset));
            return;
        }

        SuperiorSkyblockPlugin.debug("Action: Create Island, Target: " + superiorPlayer.getName() + ", Schematic: " + schemName + ", Bonus Worth: " + bonusWorth + ", Bonus Level: " + bonusLevel + ", Biome: " + biome + ", Name: " + islandName + ", Offset: " + offset);

        if(!EventsCaller.callPreIslandCreateEvent(superiorPlayer, islandName))
            return;

        UUID islandUUID = generateIslandUUID();

        Location islandLocation = plugin.getProviders().getNextLocation(
                lastIsland.parse().clone(),
                plugin.getSettings().islandsHeight,
                plugin.getSettings().maxIslandSize,
                superiorPlayer.getUniqueId(),
                islandUUID
        );

        SuperiorSkyblockPlugin.debug("Action: Calculate Next Island, Location: " + LocationUtils.getLocation(islandLocation));

        SIsland island = new SIsland(superiorPlayer, islandUUID, islandLocation.add(0.5, 0, 0.5), islandName, schemName);
        EventResult<Boolean> event = EventsCaller.callIslandCreateEvent(superiorPlayer, island, schemName);

        if(!event.isCancelled()) {
            pendingCreationTasks.add(superiorPlayer.getUniqueId());

            Schematic schematic = plugin.getSchematics().getSchematic(schemName);
            long startTime = System.currentTimeMillis();
            schematic.pasteSchematic(island, islandLocation.getBlock().getRelative(BlockFace.DOWN).getLocation(), () -> {
                Set<ChunkPosition> loadedChunks = ((BaseSchematic) schematic).getLoadedChunks();

                islands.add(superiorPlayer.getUniqueId(), island);
                setLastIsland(SBlockPosition.of(islandLocation));
                plugin.getDataHandler().insertIsland(island);

                pendingCreationTasks.remove(superiorPlayer.getUniqueId());

                island.setBonusWorth(offset ? island.getRawWorth().negate() : bonusWorth);
                island.setBonusLevel(offset ? island.getRawLevel().negate() : bonusLevel);
                island.setBiome(biome);
                island.setTeleportLocation(((BaseSchematic) schematic).getTeleportLocation(islandLocation));

                if (superiorPlayer.isOnline()) {
                    Locale.CREATE_ISLAND.send(superiorPlayer, SBlockPosition.of(islandLocation), System.currentTimeMillis() - startTime);
                    if (event.getResult()) {
                        superiorPlayer.teleport(island);
                        Executor.sync(() -> island.resetChunksExcludedFromList(loadedChunks), 2L);
                    }
                }

                plugin.getProviders().finishIslandCreation(islandLocation, superiorPlayer.getUniqueId(), islandUUID);
            }, ex -> {
                pendingCreationTasks.remove(superiorPlayer.getUniqueId());
                plugin.getProviders().finishIslandCreation(islandLocation, superiorPlayer.getUniqueId(), islandUUID);
                ex.printStackTrace();
                Locale.CREATE_ISLAND_FAILURE.send(superiorPlayer);
            });
        }
    }

    public UUID generateIslandUUID(){
        UUID uuid;

        do{
            uuid = UUID.randomUUID();
        }while (getIslandByUUID(uuid) != null || getIsland(uuid) != null);

        return uuid;
    }

    @Override
    public boolean hasActiveCreateRequest(SuperiorPlayer superiorPlayer) {
        return pendingCreationTasks.contains(superiorPlayer.getUniqueId());
    }

    @Override
    public void deleteIsland(Island island){
        SuperiorSkyblockPlugin.debug("Action: Disband Island, Island: " + island.getOwner().getName());
        island.getAllPlayersInside().forEach(superiorPlayer -> {
            SuperiorMenu.killMenu(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            Locale.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
        });
        islands.remove(island.getOwner().getUniqueId());
        plugin.getDataHandler().deleteIsland(island);
        ChunksTracker.removeIsland(island);
    }

    @Override
    public Island getIsland(SuperiorPlayer superiorPlayer){
        return getIsland(superiorPlayer.getIslandLeader().getUniqueId());
    }

    @Override
    public Island getIsland(UUID uuid){
        return islands.get(uuid);
    }

    @Override
    public Island getIslandByUUID(UUID uuid) {
        return islands.getByUUID(uuid);
    }

    @Override
    public Island getIsland(String islandName) {
        String inputName = StringUtils.stripColors(islandName);
        return getIslands().stream().filter(island -> island.getRawName().equalsIgnoreCase(inputName)).findFirst().orElse(null);
    }

    @Override
    public Island getIsland(int index){
        return getIsland(index, SortingTypes.getDefaultSorting());
    }

    @Override
    public Island getIsland(int index, SortingType sortingType) {
        return index >= islands.size() ? null : islands.get(index, sortingType);
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        return islands.indexOf(island, sortingType);
    }

    @Override
    public Island getIslandAt(Location location){
        if(location == null)
            return null;

        if(spawnIsland != null && spawnIsland.isInside(location))
            return spawnIsland;

        return islands.get(location);
    }

    @Override
    public Island getIslandAt(Chunk chunk){
        if(chunk == null)
            return null;

        Island island;

        Location corner = chunk.getBlock(0, 100, 0).getLocation();
        if((island = getIslandAt(corner)) != null)
            return island;

        corner = chunk.getBlock(15, 100, 0).getLocation();
        if((island = getIslandAt(corner)) != null)
            return island;

        corner = chunk.getBlock(0, 100, 15).getLocation();
        if((island = getIslandAt(corner)) != null)
            return island;

        corner = chunk.getBlock(15, 100, 15).getLocation();
        if((island = getIslandAt(corner)) != null)
            return island;

        return null;
    }

    @Override
    public SpawnIsland getSpawnIsland(){
        if(spawnIsland == null)
            updateSpawn();

        return spawnIsland;
    }

    @Override
    public World getIslandsWorld() {
        return getIslandsWorld(World.Environment.NORMAL);
    }

    @Override
    public World getIslandsWorld(World.Environment environment) {
        return getIslandsWorld(null, environment);
    }

    @Override
    public World getIslandsWorld(Island island, World.Environment environment) {
        return plugin.getProviders().getIslandsWorld(island, environment);
    }

    @Override
    public boolean isIslandsWorld(World world) {
        return customWorlds.contains(world.getUID()) || plugin.getProviders().isIslandsWorld(world);
    }

    @Override
    public void registerIslandWorld(World world) {
        customWorlds.add(world.getUID());
    }

    @Override
    public List<World> getRegisteredWorlds() {
        return customWorlds.stream().map(Bukkit::getWorld).collect(Collectors.toList());
    }

    @Override
    public Location getNextLocation(){
        //lastIsland.parse().clone()
        return plugin.getProviders().getNextLocation(
                lastIsland.parse().clone(),
                plugin.getSettings().islandsHeight,
                plugin.getSettings().maxIslandSize,
                null, null
        );
    }

    @Override
    public void transferIsland(UUID oldOwner, UUID newOwner) {
        islands.transferIsland(oldOwner, newOwner);
    }

    @Override
    public int getSize() {
        return islands.size();
    }

    @Override
    public void sortIslands(SortingType sortingType) {
        sortIslands(sortingType, null);
    }

    public void sortIslands(SortingType sortingType, Runnable onFinish) {
        SuperiorSkyblockPlugin.debug("Action: Sort Islands, Sorting Type: " + sortingType.getName());
        islands.sort(sortingType, onFinish);
    }

    @Override
    public List<UUID> getAllIslands(){
        return getAllIslands(SortingTypes.getDefaultSorting());
    }

    @Override
    public List<UUID> getAllIslands(SortingType sortingType) {
        return Lists.newArrayList(islands.iterator(sortingType)).stream().map(island -> island.getOwner().getUniqueId()).collect(Collectors.toList());
    }

    @Override
    public List<Island> getIslands(){
        return Lists.newArrayList(islands.iterator());
    }

    @Override
    public List<Island> getIslands(SortingType sortingType) {
        return Lists.newArrayList(islands.iterator(sortingType));
    }

    @Override
    public void openTopIslands(SuperiorPlayer superiorPlayer){
        MenuTopIslands.openInventory(superiorPlayer, null, SortingTypes.getDefaultSorting());
    }

    @Override
    public int getBlockAmount(Block block){
        return getBlockAmount(block.getLocation());
    }

    @Override
    public int getBlockAmount(Location location){
        return stackedBlocks.getOrDefault(SBlockPosition.of(location), 1);
    }

    public Set<Pair<Integer, Key>> getBlockAmounts(ChunkPosition chunkPosition){
        return stackedBlocks.get(chunkPosition).values().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void setBlockAmount(Block block, int amount){
        SuperiorSkyblockPlugin.debug("Action: Set Block Amount, Block: " + block.getType() + ", Amount: " + amount);

        Key originalBlock = stackedBlocks.getOrDefault(SBlockPosition.of(block.getLocation()), null);
        Key currentBlock = Key.of(block);

        blockFailed = false;

        if(originalBlock != null && !currentBlock.equals(originalBlock)) {
            SuperiorSkyblockPlugin.log("Found a glitched stacked-block at " + SBlockPosition.of(block.getLocation()) + " - fixing it...");
            amount = 0;
            blockFailed = true;
        }

        stackedBlocks.put(SBlockPosition.of(block.getLocation()), amount, currentBlock);
        stackedBlocks.updateName(block);

        if(amount > 1) {
            if (originalBlock != null) {
                Query.STACKED_BLOCKS_INSERT.getStatementHolder(null)
                        .setString(block.getWorld().getName())
                        .setInt(block.getX())
                        .setInt(block.getY())
                        .setInt(block.getZ())
                        .setInt(amount)
                        .setString(Key.of(block).toString())
                        .execute(true);
            } else {
                Query.STACKED_BLOCKS_UPDATE.getStatementHolder(null)
                        .setInt(amount)
                        .setString(block.getWorld().getName())
                        .setInt(block.getX())
                        .setInt(block.getY())
                        .setInt(block.getZ())
                        .execute(true);
            }
        }else{
            Query.STACKED_BLOCKS_DELETE.getStatementHolder(null)
                    .setString(block.getWorld().getName())
                    .setInt(block.getX())
                    .setInt(block.getY())
                    .setInt(block.getZ())
                    .execute(true);
        }
    }

    public boolean hasBlockFailed(){
        return blockFailed;
    }

    public void removeStackedBlocks(Island island){
        StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_DELETE.getStatementHolder((SIsland) island);
        stackedBlocksHolder.prepareBatch();

        IslandUtils.getChunkCoords(island, true, false).forEach(chunkPosition -> {
            Map<SBlockPosition, Pair<Integer, Key>> stackedBlocks = this.stackedBlocks.stackedBlocks.remove(chunkPosition);
            if(stackedBlocks != null)
                stackedBlocks.keySet().forEach(blockPosition -> {
                    this.stackedBlocks.removeHologram(blockPosition.parse());
                    stackedBlocksHolder.setString(blockPosition.getWorldName()).setInt(blockPosition.getX())
                            .setInt(blockPosition.getY()).setInt(blockPosition.getZ()).addBatch();
                });
        });

        stackedBlocksHolder.execute(true);
    }

    @Override
    public void calcAllIslands(){
        calcAllIslands(null);
    }

    @Override
    public void calcAllIslands(Runnable callback) {
        SuperiorSkyblockPlugin.debug("Action: Calculate All Islands");
        List<Island> islands = new ArrayList<>();

        {
            for (Island island : this.islands) {
                if(!island.isBeingRecalculated())
                    islands.add(island);
            }
        }

        for(int i = 0; i < islands.size(); i++){
            islands.get(i).calcIslandWorth(null, i + 1 < islands.size() ? null : callback);
        }
    }

    @Override
    public boolean isSpawner(Material material) {
        return material == Materials.SPAWNER.toBukkitType();
    }

    @Override
    public void addIslandToPurge(Island island) {
        SuperiorSkyblockPlugin.debug("Action: Purge Island, Island: " + island.getOwner().getName());
        islandsToPurge.add(island.getOwner().getUniqueId());
    }

    @Override
    public void removeIslandFromPurge(Island island) {
        SuperiorSkyblockPlugin.debug("Action: Remove From Purge, Island: " + island.getOwner().getName());
        islandsToPurge.remove(island.getOwner().getUniqueId());
    }

    @Override
    public boolean isIslandPurge(Island island) {
        return islandsToPurge.contains(island.getOwner().getUniqueId());
    }

    @Override
    public List<Island> getIslandsToPurge() {
        return islandsToPurge.stream().map(this::getIsland).collect(Collectors.toList());
    }

    @Override
    public void registerSortingType(SortingType sortingType) {
        SuperiorSkyblockPlugin.debug("Action: Register Sorting Type, Sorting Type: " + sortingType.getName());
        islands.registerSortingType(sortingType, true);
    }

    @Override
    public BigDecimal getTotalWorth() {
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastTimeWorthUpdate > 60000){
            lastTimeWorthUpdate = currentTime;
            totalWorth = BigDecimalFormatted.ZERO;
            for(Island island : getIslands())
                totalWorth = totalWorth.add(island.getWorth());
        }

        return totalWorth;
    }

    @Override
    public BigDecimal getTotalLevel() {
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastTimeLevelUpdate > 60000){
            lastTimeLevelUpdate = currentTime;
            totalLevel = BigDecimalFormatted.ZERO;
            for(Island island : getIslands())
                totalLevel = totalLevel.add(island.getIslandLevel());
        }

        return totalLevel;
    }

    public void loadGrid(ResultSet resultSet) throws SQLException {
        lastIsland = SBlockPosition.of(resultSet.getString("lastIsland"));

        for(String entry : resultSet.getString("stackedBlocks").split(";")){
            if(!entry.isEmpty()) {
                String[] sections = entry.split("=");
                stackedBlocks.put(SBlockPosition.of(sections[0]), Integer.parseInt(sections[1]), Key.of("AIR"));
            }
        }

        int maxIslandSize = resultSet.getInt("maxIslandSize");
        String world = resultSet.getString("world");

        if(plugin.getSettings().maxIslandSize != maxIslandSize){
            SuperiorSkyblockPlugin.log("&cYou have changed the max-island-size value without deleting database.");
            SuperiorSkyblockPlugin.log("&cRestoring it to the old value...");
            plugin.getSettings().updateValue("max-island-size", maxIslandSize);
        }

        if(!plugin.getSettings().islandWorldName.equals(world)){
            SuperiorSkyblockPlugin.log("&cYou have changed the island-world value without deleting database.");
            SuperiorSkyblockPlugin.log("&cRestoring it to the old value...");
            plugin.getSettings().updateValue("worlds.normal-world", world);
        }

        ChunksTracker.deserialize(this, null, resultSet.getString("dirtyChunks"));
    }

    public void loadStackedBlocks(ResultSet resultSet) throws SQLException {
        String world = resultSet.getString("world");
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        int amount = resultSet.getInt("amount");

        if(world == null)
            return;

        String item = resultSet.getString("item");
        Key blockKey = item == null || item.isEmpty() ? null : Key.of(item);

        stackedBlocks.put(SBlockPosition.of(world, x, y, z), amount, blockKey);
    }

    public void updateStackedBlockKeys(){
        stackedBlocks.values().forEach(map ->
                map.forEach((blockPosition, pair) -> pair.setValue(Key.of(blockPosition.getBlock()))));
    }

    public void executeGridInsertStatement(boolean async) {
        Query.GRID_INSERT.getStatementHolder(null)
                .setString(this.lastIsland.toString())
                .setString("")
                .setInt(plugin.getSettings().maxIslandSize)
                .setString(plugin.getSettings().islandWorldName)
                .setString("")
                .execute(async);
    }

    public void saveIslands(){
        List<Island> onlineIslands = Bukkit.getOnlinePlayers().stream()
                .map(player -> SSuperiorPlayer.of(player).getIsland())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Island> modifiedIslands = getIslands().stream()
                .filter(island -> ((DatabaseObject) island).isModified())
                .collect(Collectors.toList());

        if(!onlineIslands.isEmpty()) {
            long lastTimeStatus = System.currentTimeMillis() / 1000;
            StatementHolder islandStatusHolder = Query.ISLAND_SET_LAST_TIME_UPDATE.getStatementHolder(null);
            islandStatusHolder.prepareBatch();
            onlineIslands.forEach(island -> islandStatusHolder.setLong(lastTimeStatus).setString(island.getOwner().getUniqueId() + "").addBatch());
            islandStatusHolder.execute(false);
        }

        if(!modifiedIslands.isEmpty()){
            StatementHolder islandUpdateHolder = Query.ISLAND_UPDATE.getStatementHolder(null);
            islandUpdateHolder.prepareBatch();
            modifiedIslands.forEach(island -> ((SIsland) island).setUpdateStatement(islandUpdateHolder).addBatch());
            islandUpdateHolder.execute(false);
        }

        getIslands().forEach(Island::removeEffects);
    }


    public void saveStackedBlocks(){
        Map<SBlockPosition, Pair<Integer, Key>> stackedBlocks = new HashMap<>();
        this.stackedBlocks.values().forEach(stackedBlocks::putAll);

        {
            StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_INSERT.getStatementHolder(null);
            stackedBlocksHolder.prepareBatch();
            stackedBlocks.forEach(((blockPosition, pair) -> {
                if(pair.getKey() > 1){
                    stackedBlocksHolder.setString(blockPosition.getWorldName())
                            .setInt(blockPosition.getX())
                            .setInt(blockPosition.getY())
                            .setInt(blockPosition.getZ())
                            .setInt(pair.getKey())
                            .setString(pair.getValue().toString())
                            .addBatch();
                }
            }));
            stackedBlocksHolder.execute(false);
        }

        {
            StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_DELETE.getStatementHolder(null);
            stackedBlocksHolder.prepareBatch();
            stackedBlocks.forEach(((blockPosition, pair) -> {
                if(pair.getKey() <= 1){
                    stackedBlocksHolder.setString(blockPosition.getWorldName())
                            .setInt(blockPosition.getX())
                            .setInt(blockPosition.getY())
                            .setInt(blockPosition.getZ())
                            .addBatch();
                }
            }));
            stackedBlocksHolder.execute(false);
        }
    }

    private void setLastIsland(SBlockPosition blockPosition){
        SuperiorSkyblockPlugin.debug("Action: Set Last Island, Location: " + blockPosition);
        this.lastIsland = blockPosition;
        Query.GRID_UPDATE_LAST_ISLAND.getStatementHolder(null)
                .setString(blockPosition.toString())
                .execute(true);
    }

    private class StackedBlocksHandler {

        private final Map<ChunkPosition, Map<SBlockPosition, Pair<Integer, Key>>> stackedBlocks = Maps.newHashMap();

        void put(SBlockPosition location, int amount, Key blockKey){
            stackedBlocks.computeIfAbsent(ChunkPosition.of(location), m -> new HashMap<>())
                    .put(location, new Pair<>(amount, blockKey));
        }

        @SuppressWarnings("SameParameterValue")
        int getOrDefault(SBlockPosition location, int def){
            Map<SBlockPosition, Pair<Integer, Key>> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(location));
            return chunkStackedBlocks == null ? def : chunkStackedBlocks.getOrDefault(location, new Pair<>(def, null)).getKey();
        }

        @SuppressWarnings("SameParameterValue")
        Key getOrDefault(SBlockPosition location, Key def){
            Map<SBlockPosition, Pair<Integer, Key>> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(location));
            return chunkStackedBlocks == null ? def : chunkStackedBlocks.getOrDefault(location, new Pair<>(null, def)).getValue();
        }

        Map<SBlockPosition, Pair<Integer, Key>> get(ChunkPosition chunkPosition){
            return stackedBlocks.getOrDefault(chunkPosition, new HashMap<>());
        }

        boolean containsKey(SBlockPosition blockPosition){
            Map<SBlockPosition, Pair<Integer, Key>> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(blockPosition));
            return chunkStackedBlocks != null && chunkStackedBlocks.containsKey(blockPosition);
        }

        Collection<Map<SBlockPosition, Pair<Integer, Key>>> values(){
            return stackedBlocks.values();
        }

        private void updateName(Block block){
            int amount = getBlockAmount(block);
            ArmorStand armorStand = getHologram(block.getLocation(), true);
            assert armorStand != null;

            if(amount <= 1){
                Map<SBlockPosition, Pair<Integer, Key>> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(block));
                if(chunkStackedBlocks != null)
                    chunkStackedBlocks.remove(SBlockPosition.of(block.getLocation()));
                armorStand.remove();
            }else{
                armorStand.setCustomName(plugin.getSettings().stackedBlocksName
                        .replace("{0}", String.valueOf(amount))
                        .replace("{1}", getFormattedType(block.getType().name()))
                );
            }

        }

        private void removeHologram(Location location){
            ArmorStand armorStand = getHologram(location, false);
            if(armorStand != null)
                armorStand.remove();
        }

        private ArmorStand getHologram(Location location, boolean createNew){
            Location hologramLocation = location.add(0.5, 1, 0.5);

            // Looking for an armorstand
            for(Entity entity : location.getChunk().getEntities()){
                if(entity instanceof ArmorStand && entity.getLocation().equals(hologramLocation))
                    return (ArmorStand) entity;
            }

            if(createNew) {
                // Couldn't find one, creating one...

                ArmorStand armorStand = location.getWorld().spawn(hologramLocation, ArmorStand.class);

                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setVisible(false);
                armorStand.setCustomNameVisible(true);
                armorStand.setMarker(true);

                return armorStand;
            }

            return null;
        }

        private String getFormattedType(String type){
            StringBuilder stringBuilder = new StringBuilder();

            for(String section : type.split("_")){
                stringBuilder.append(" ").append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase());
            }

            return stringBuilder.toString().substring(1);
        }

    }

}
