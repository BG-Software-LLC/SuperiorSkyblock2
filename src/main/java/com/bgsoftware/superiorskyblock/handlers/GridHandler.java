package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIslandPreview;
import com.bgsoftware.superiorskyblock.island.data.SIslandDataHandler;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.database.DatabaseObject;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.menu.MenuTopIslands;
import com.bgsoftware.superiorskyblock.schematics.BaseSchematic;
import com.bgsoftware.superiorskyblock.utils.database.SQLHelper;
import com.bgsoftware.superiorskyblock.utils.database.StatementHolder;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.registry.IslandRegistry;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;

import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class GridHandler extends AbstractHandler implements GridManager {

    private final IslandRegistry islands = new IslandRegistry();
    private final Set<UUID> islandsToPurge = Sets.newConcurrentHashSet();
    private final Set<UUID> pendingCreationTasks = Sets.newHashSet();
    private final Registry<UUID, IslandPreview> islandPreviews = Registry.createRegistry();
    private final Set<UUID> customWorlds = Sets.newHashSet();
    private final StackedBlocksHandler stackedBlocks;

    private SpawnIsland spawnIsland;
    private SBlockPosition lastIsland;
    private boolean blockFailed = false;

    private BigDecimal totalWorth = BigDecimal.ZERO;
    private long lastTimeWorthUpdate = 0;
    private BigDecimal totalLevel = BigDecimal.ZERO;
    private long lastTimeLevelUpdate = 0;

    private boolean pluginDisable = false;

    public GridHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
        stackedBlocks = new StackedBlocksHandler(plugin);
    }

    @Override
    public void loadData(){
        lastIsland = SBlockPosition.of(plugin.getSettings().islandWorldName, 0, 100, 0);
        Executor.sync(this::updateSpawn);
        Executor.timer(plugin.getNMSDragonFight()::tickBattles, 1L);
    }

    public void updateSpawn(){
        spawnIsland = new SpawnIsland(plugin);
    }

    public void syncUpgrades(){
        getIslands().forEach(Island::updateUpgrades);
    }

    public void createIsland(ResultSet resultSet) throws SQLException{
        UUID owner = UUID.fromString(resultSet.getString("owner"));
        islands.add(owner, plugin.getFactory().createIsland(this, resultSet));
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(schemName, "schemName parameter cannot be null.");
        Preconditions.checkNotNull(bonus, "bonus parameter cannot be null.");
        Preconditions.checkNotNull(biome, "biome parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        createIsland(superiorPlayer, schemName, bonus, biome, islandName, false);
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName, boolean offset) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(schemName, "schemName parameter cannot be null.");
        Preconditions.checkNotNull(bonus, "bonus parameter cannot be null.");
        Preconditions.checkNotNull(biome, "biome parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        createIsland(superiorPlayer, schemName, bonus, BigDecimal.ZERO, biome, islandName, false);
    }

    @Override
    public void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonusWorth, BigDecimal bonusLevel, Biome biome, String islandName, boolean offset) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(schemName, "schemName parameter cannot be null.");
        Preconditions.checkNotNull(bonusWorth, "bonusWorth parameter cannot be null.");
        Preconditions.checkNotNull(bonusLevel, "bonusLevel parameter cannot be null.");
        Preconditions.checkNotNull(biome, "biome parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");

        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> createIsland(superiorPlayer, schemName, bonusWorth, bonusLevel, biome, islandName, offset));
            return;
        }

        SuperiorSkyblockPlugin.debug("Action: Create Island, Target: " + superiorPlayer.getName() + ", Schematic: " + schemName + ", Bonus Worth: " + bonusWorth + ", Bonus Level: " + bonusLevel + ", Biome: " + biome + ", Name: " + islandName + ", Offset: " + offset);

        // Removing any active previews for the player.
        boolean updateGamemode = islandPreviews.remove(superiorPlayer.getUniqueId()) != null;

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

        Island island = plugin.getFactory().createIsland(superiorPlayer, islandUUID, islandLocation.add(0.5, 0, 0.5), islandName, schemName);
        EventResult<Boolean> event = EventsCaller.callIslandCreateEvent(superiorPlayer, island, schemName);

        if(!event.isCancelled()) {
            pendingCreationTasks.add(superiorPlayer.getUniqueId());

            Schematic schematic = plugin.getSchematics().getSchematic(schemName);
            long startTime = System.currentTimeMillis();
            assert schematic != null;
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

                superiorPlayer.runIfOnline(player -> {
                    Locale.CREATE_ISLAND.send(superiorPlayer, SBlockPosition.of(islandLocation), System.currentTimeMillis() - startTime);
                    if (event.getResult()) {
                        if(updateGamemode)
                            player.setGameMode(GameMode.SURVIVAL);
                        superiorPlayer.teleport(island, result -> {
                            if(result)
                                Executor.sync(() -> IslandUtils.resetChunksExcludedFromList(island, loadedChunks), 10L);
                        });
                    }
                });

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
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return pendingCreationTasks.contains(superiorPlayer.getUniqueId());
    }

    @Override
    public void startIslandPreview(SuperiorPlayer superiorPlayer, String schemName, String islandName) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(schemName, "schemName parameter cannot be null.");
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");

        Location previewLocation = plugin.getSettings().islandPreviewLocations.get(schemName.toLowerCase());
        if(previewLocation != null && previewLocation.getWorld() != null) {
            superiorPlayer.teleport(previewLocation, result -> {
                if(result){
                    islandPreviews.add(superiorPlayer.getUniqueId(), new SIslandPreview(superiorPlayer, previewLocation, schemName, islandName));
                    Executor.ensureMain(() -> superiorPlayer.runIfOnline(player -> player.setGameMode(GameMode.SPECTATOR)));
                    Locale.ISLAND_PREVIEW_START.send(superiorPlayer, schemName);
                }
            });
        }
    }

    @Override
    public void cancelIslandPreview(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        IslandPreview islandPreview = islandPreviews.remove(superiorPlayer.getUniqueId());
        if(islandPreview != null){
            superiorPlayer.runIfOnline(player -> {
                Executor.ensureMain(() -> superiorPlayer.teleport(plugin.getGrid().getSpawnIsland(), teleportResult -> {
                    if (teleportResult && superiorPlayer.isOnline())
                        player.setGameMode(GameMode.SURVIVAL);
                }));
                PlayerChat.remove(player);
            });
        }
    }

    @Override
    public void cancelAllIslandPreviews() {
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::cancelAllIslandPreviews);
            return;
        }

        islandPreviews.values().forEach(islandPreview -> {
            SuperiorPlayer superiorPlayer = islandPreview.getPlayer();
            superiorPlayer.runIfOnline(player -> {
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                // We don't wait for the teleport to happen, as this method is called when the server is disabled.
                // Therefore, we can't wait for the async task to occur.
                player.setGameMode(GameMode.SURVIVAL);
                PlayerChat.remove(player);
            });
        });
    }

    @Override
    public IslandPreview getIslandPreview(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return islandPreviews.get(superiorPlayer.getUniqueId());
    }

    @Override
    public void deleteIsland(Island island){
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Disband Island, Island: " + island.getOwner().getName());

        island.getAllPlayersInside().forEach(superiorPlayer -> {
            SuperiorMenu.killMenu(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            Locale.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
        });

        islands.remove(island.getOwner().getUniqueId());
        plugin.getDataHandler().deleteIsland(island, !pluginDisable);
        plugin.getNMSDragonFight().removeDragonBattle(island);

        ChunksTracker.removeIsland(island);
    }

    @Override
    public Island getIsland(SuperiorPlayer superiorPlayer){
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        return getIsland(superiorPlayer.getIslandLeader().getUniqueId());
    }

    @Override
    public Island getIsland(UUID uuid){
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        return islands.get(uuid);
    }

    @Override
    public Island getIslandByUUID(UUID uuid) {
        return islands.getByUUID(uuid);
    }

    @Override
    public Island getIsland(String islandName) {
        Preconditions.checkNotNull(islandName, "islandName parameter cannot be null.");
        String inputName = StringUtils.stripColors(islandName);
        return getIslands().stream().filter(island -> island.getRawName().equalsIgnoreCase(inputName)).findFirst().orElse(null);
    }

    @Override
    public Island getIsland(int index, SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        return index >= islands.size() ? null : islands.get(index, sortingType);
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
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
    public World getIslandsWorld(Island island, World.Environment environment) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        return plugin.getProviders().getIslandsWorld(island, environment);
    }

    @Override
    public boolean isIslandsWorld(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        return customWorlds.contains(world.getUID()) || plugin.getProviders().isIslandsWorld(world);
    }

    @Override
    public void registerIslandWorld(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        customWorlds.add(world.getUID());
    }

    @Override
    public List<World> getRegisteredWorlds() {
        return customWorlds.stream().map(Bukkit::getWorld).collect(Collectors.toList());
    }

    @Override
    public void transferIsland(UUID oldOwner, UUID newOwner) {
        Preconditions.checkNotNull(oldOwner, "oldOwner parameter cannot be null.");
        Preconditions.checkNotNull(newOwner, "newOwner parameter cannot be null.");
        islands.transferIsland(oldOwner, newOwner);
    }

    @Override
    public int getSize() {
        return islands.size();
    }

    @Override
    public void sortIslands(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        sortIslands(sortingType, null);
    }

    public void sortIslands(SortingType sortingType, Runnable onFinish) {
        SuperiorSkyblockPlugin.debug("Action: Sort Islands, Sorting Type: " + sortingType.getName());
        islands.sort(sortingType, () -> {
            MenuTopIslands.refreshMenus(sortingType);
            if(onFinish != null)
                onFinish.run();
        });
    }

    @Override
    public List<UUID> getAllIslands(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        return Lists.newArrayList(islands.iterator(sortingType)).stream().map(island -> {
            SuperiorPlayer superiorPlayer = island.getOwner();
            return superiorPlayer == null ? null : superiorPlayer.getUniqueId();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public List<Island> getIslands(){
        return Lists.newArrayList(islands.iterator());
    }

    @Override
    public List<Island> getIslands(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        return Lists.newArrayList(islands.iterator(sortingType));
    }

    @Override
    public int getBlockAmount(Block block){
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return getBlockAmount(block.getLocation());
    }

    @Override
    public int getBlockAmount(Location location){
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        return stackedBlocks.getBlockAmount(SBlockPosition.of(location), 1);
    }

    public Key getBlockKey(Location location){
        return stackedBlocks.getBlockKey(SBlockPosition.of(location), Key.of(location.getBlock()));
    }

    public Set<StackedBlocksHandler.StackedBlock> getStackedBlocks(ChunkPosition chunkPosition){
        return new HashSet<>(stackedBlocks.getStackedBlocks(chunkPosition).values());
    }

    @Override
    public void setBlockAmount(Block block, int amount){
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Set Block Amount, Block: " + block.getType() + ", Amount: " + amount);

        Key originalBlock = stackedBlocks.getBlockKey(SBlockPosition.of(block.getLocation()), null);
        Key currentBlock = Key.of(block);

        blockFailed = false;

        if(originalBlock != null && !currentBlock.equals(originalBlock)) {
            SuperiorSkyblockPlugin.log("Found a glitched stacked-block at " + SBlockPosition.of(block.getLocation()) + " - fixing it...");
            amount = 0;
            blockFailed = true;
        }

        if(amount > 1) {
            StackedBlocksHandler.StackedBlock stackedBlock = stackedBlocks.setStackedBlock(block.getLocation(), amount, currentBlock);
            Executor.sync(stackedBlock::updateName, 5L);

            if (originalBlock == null) {
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
        }
        else{
            stackedBlocks.removeStackedBlock(SBlockPosition.of(block.getLocation()));

            Query.STACKED_BLOCKS_DELETE.getStatementHolder(null)
                    .setString(block.getWorld().getName())
                    .setInt(block.getX())
                    .setInt(block.getY())
                    .setInt(block.getZ())
                    .execute(true);
        }
    }

    @Override
    public List<Location> getStackedBlocks() {
        List<Location> stackedBlocks = new ArrayList<>();
        this.stackedBlocks.getStackedBlocks().forEach(map -> stackedBlocks.addAll(map.keySet().stream().map(SBlockPosition::parse).collect(Collectors.toList())));
        return stackedBlocks;
    }

    public boolean hasBlockFailed(){
        return blockFailed;
    }

    public void removeStackedBlocks(Island island, ChunkPosition chunkPosition){
        StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_DELETE.getStatementHolder(
                (SIslandDataHandler) island.getDataHandler());
        stackedBlocksHolder.prepareBatch();

        Map<SBlockPosition, StackedBlocksHandler.StackedBlock> stackedBlocks =
                this.stackedBlocks.removeStackedBlocks(chunkPosition);

        if(stackedBlocks != null) {
            stackedBlocks.values().forEach(stackedBlock -> {
                stackedBlock.removeHologram();
                SBlockPosition blockPosition = stackedBlock.getBlockPosition();
                stackedBlocksHolder.setString(blockPosition.getWorldName()).setInt(blockPosition.getX())
                        .setInt(blockPosition.getY()).setInt(blockPosition.getZ()).addBatch();
            });
        }

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
    public void addIslandToPurge(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(island.getOwner(), "island's owner cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Purge Island, Island: " + island.getOwner().getName());
        islandsToPurge.add(island.getOwner().getUniqueId());
    }

    @Override
    public void removeIslandFromPurge(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(island.getOwner(), "island's owner cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Remove From Purge, Island: " + island.getOwner().getName());
        islandsToPurge.remove(island.getOwner().getUniqueId());
    }

    @Override
    public boolean isIslandPurge(Island island) {
        Preconditions.checkNotNull(island, "island parameter cannot be null.");
        Preconditions.checkNotNull(island.getOwner(), "island's owner cannot be null.");
        return islandsToPurge.contains(island.getOwner().getUniqueId());
    }

    @Override
    public List<Island> getIslandsToPurge() {
        return islandsToPurge.stream().map(this::getIsland).collect(Collectors.toList());
    }

    @Override
    public void registerSortingType(SortingType sortingType) {
        Preconditions.checkNotNull(sortingType, "sortingType parameter cannot be null.");
        SuperiorSkyblockPlugin.debug("Action: Register Sorting Type, Sorting Type: " + sortingType.getName());
        islands.registerSortingType(sortingType, true);
    }

    @Override
    public BigDecimal getTotalWorth() {
        long currentTime = System.currentTimeMillis();

        if(currentTime - lastTimeWorthUpdate > 60000){
            lastTimeWorthUpdate = currentTime;
            totalWorth = BigDecimal.ZERO;
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
            totalLevel = BigDecimal.ZERO;
            for(Island island : getIslands())
                totalLevel = totalLevel.add(island.getIslandLevel());
        }

        return totalLevel;
    }

    public void disablePlugin(){
        this.pluginDisable = true;
        cancelAllIslandPreviews();
    }

    public void loadGrid(ResultSet resultSet) throws SQLException {
        lastIsland = SBlockPosition.of(resultSet.getString("lastIsland"));

        for(String entry : resultSet.getString("stackedBlocks").split(";")){
            if(!entry.isEmpty()) {
                String[] sections = entry.split("=");
                stackedBlocks.setStackedBlock(SBlockPosition.of(sections[0]), Integer.parseInt(sections[1]), ConstantKeys.AIR);
            }
        }

        int maxIslandSize = resultSet.getInt("maxIslandSize");
        String world = resultSet.getString("world");

        try {
            if (plugin.getSettings().maxIslandSize != maxIslandSize) {
                SuperiorSkyblockPlugin.log("&cYou have changed the max-island-size value without deleting database.");
                SuperiorSkyblockPlugin.log("&cRestoring it to the old value...");
                plugin.getSettings().updateValue("max-island-size", maxIslandSize);
            }

            if (!plugin.getSettings().islandWorldName.equals(world)) {
                SuperiorSkyblockPlugin.log("&cYou have changed the island-world value without deleting database.");
                SuperiorSkyblockPlugin.log("&cRestoring it to the old value...");
                plugin.getSettings().updateValue("worlds.normal-world", world);
            }
        }catch (IOException ex){
            ex.printStackTrace();
            Bukkit.shutdown();
            return;
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

        stackedBlocks.setStackedBlock(SBlockPosition.of(world, x, y, z), amount, blockKey);
    }

    public void updateStackedBlockKeys(){
        stackedBlocks.getStackedBlocks().forEach(map -> map.forEach((blockPosition, stackedBlock) -> {
            try{
                stackedBlock.setBlockKey(Key.of(blockPosition.getBlock()));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }));
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
                .map(player -> plugin.getPlayers().getSuperiorPlayer(player).getIsland())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Island> modifiedIslands = getIslands().stream()
                .filter(island -> ((DatabaseObject) island.getDataHandler()).isModified())
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
            modifiedIslands.forEach(island -> ((SIslandDataHandler) island.getDataHandler())
                    .setUpdateStatement(islandUpdateHolder).addBatch());
            islandUpdateHolder.execute(false);
        }

        getIslands().forEach(Island::removeEffects);
    }


    public void saveStackedBlocks(){
        Map<SBlockPosition, StackedBlocksHandler.StackedBlock> stackedBlocks = new HashMap<>();
        this.stackedBlocks.getStackedBlocks().forEach(stackedBlocks::putAll);

        SQLHelper.executeUpdate("DELETE FROM {prefix}stackedBlocks;");

        {
            StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_INSERT.getStatementHolder(null);
            stackedBlocksHolder.prepareBatch();
            stackedBlocks.forEach(((blockPosition, pair) -> {
                if(pair.getAmount() > 1){
                    stackedBlocksHolder.setString(blockPosition.getWorldName())
                            .setInt(blockPosition.getX())
                            .setInt(blockPosition.getY())
                            .setInt(blockPosition.getZ())
                            .setInt(pair.getAmount())
                            .setString(pair.getBlockKey().toString())
                            .addBatch();
                }
            }));
            stackedBlocksHolder.execute(false);
        }

        {
            StatementHolder stackedBlocksHolder = Query.STACKED_BLOCKS_DELETE.getStatementHolder(null);
            stackedBlocksHolder.prepareBatch();
            stackedBlocks.forEach(((blockPosition, pair) -> {
                if(pair.getAmount() <= 1){
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

}
