package com.bgsoftware.superiorskyblock.core.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.hooks.LazyWorldsProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.database.bridge.StackedBlocksDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.stackedblocks.container.StackedBlocksContainer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class StackedBlocksManagerImpl extends Manager implements StackedBlocksManager {

    private final StackedBlocksContainer stackedBlocksContainer;
    private DatabaseBridge databaseBridge;

    public StackedBlocksManagerImpl(SuperiorSkyblockPlugin plugin, StackedBlocksContainer stackedBlocksContainer) {
        super(plugin);
        this.stackedBlocksContainer = stackedBlocksContainer;
    }

    @Override
    public void loadData() {
        initializeDatabaseBridge();

        Log.info("Starting to load stacked blocks...");

        AtomicBoolean updateBlockKeys = new AtomicBoolean(false);

        databaseBridge.loadAllObjects("stacked_blocks", _resultSet -> {
            DatabaseResult resultSet = new DatabaseResult(_resultSet);
            loadStackedBlock(resultSet);
            Optional<String> item = resultSet.getString("block_type");
            if (!item.isPresent() || item.get().isEmpty())
                updateBlockKeys.set(true);
        });

        if (updateBlockKeys.get()) {
            BukkitExecutor.sync(this::updateStackedBlockKeys);
        }

        Log.info("Finished stacked blocks!");
    }

    @Override
    public int getStackedBlockAmount(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return getStackedBlockAmount(block.getLocation());
    }

    @Override
    public int getStackedBlockAmount(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        return stackedBlock == null ? 1 : stackedBlock.getAmount();
    }

    @Override
    public Key getStackedBlockKey(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        return stackedBlock == null ? null : stackedBlock.getBlockKey();
    }

    @Override
    public boolean setStackedBlock(Block block, int amount) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return setStackedBlock(block.getLocation(), Keys.of(block), amount);
    }

    @Override
    public boolean setStackedBlock(Location location, Key blockKey, int amount) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");
        Preconditions.checkNotNull(blockKey, "blockKey parameter cannot be null.");

        Log.debug(Debug.SET_BLOCK_AMOUNT, location, blockKey, amount);

        StackedBlock stackedBlock = this.stackedBlocksContainer.createStackedBlock(location);

        boolean succeed = true;

        if (stackedBlock.getBlockKey() != null && !blockKey.equals(stackedBlock.getBlockKey())) {
            Log.warn("Found a glitched stacked-block at ", Formatters.LOCATION_FORMATTER.format(location), " - fixing it...");
            amount = 0;
            succeed = false;
        }

        if (amount > 1) {
            stackedBlock.setBlockKey(blockKey);
            stackedBlock.setAmount(amount);
            // Must be called with delay in order to fix issue #632
            BukkitExecutor.sync(stackedBlock::updateName, 2L);
            StackedBlocksDatabaseBridge.saveStackedBlock(this, stackedBlock);
        } else {
            this.stackedBlocksContainer.removeStackedBlock(location);
            StackedBlocksDatabaseBridge.deleteStackedBlock(this, stackedBlock);
        }

        return succeed;
    }

    @Override
    public int removeStackedBlock(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock oldStackedBlock = this.stackedBlocksContainer.removeStackedBlock(location);

        if (oldStackedBlock != null) {
            StackedBlocksDatabaseBridge.deleteStackedBlock(this, oldStackedBlock);
        }

        return oldStackedBlock == null ? 1 : oldStackedBlock.getAmount();
    }

    @Override
    public Map<Location, Integer> removeStackedBlocks(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        return removeStackedBlocks(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public Map<Location, Integer> removeStackedBlocks(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        return removeStackedBlocks(ChunkPosition.of(WorldInfo.of(world), chunkX, chunkZ));
    }

    public Map<Location, Integer> removeStackedBlocks(ChunkPosition chunkPosition) {
        Preconditions.checkNotNull(chunkPosition, "chunkPosition parameter cannot be null.");

        Map<Location, Integer> removedStackedBlocks = new LinkedHashMap<>();

        try {
            databaseBridge.batchOperations(true);
            this.stackedBlocksContainer.removeStackedBlocks(chunkPosition, stackedBlock -> {
                removedStackedBlocks.put(stackedBlock.getLocation(), stackedBlock.getAmount());
                stackedBlock.removeHologram();
                StackedBlocksDatabaseBridge.deleteStackedBlock(this, stackedBlock);
            });
        } finally {
            databaseBridge.batchOperations(false);
        }

        return Collections.unmodifiableMap(removedStackedBlocks);
    }

    @Override
    public Map<Location, Integer> getStackedBlocks(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        return getStackedBlocks(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public Map<Location, Integer> getStackedBlocks(World world, int chunkX, int chunkZ) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);

        Map<Location, Integer> chunkStackedBlocks = new LinkedHashMap<>();

        this.stackedBlocksContainer.forEach(chunkPosition, stackedBlock ->
                chunkStackedBlocks.put(stackedBlock.getLocation(), stackedBlock.getAmount()));

        return Collections.unmodifiableMap(chunkStackedBlocks);
    }

    @Override
    public Map<Location, Integer> getStackedBlocks() {
        Map<Location, Integer> allStackedBlocks = new LinkedHashMap<>();
        this.stackedBlocksContainer.forEach(stackedBlock ->
                allStackedBlocks.put(stackedBlock.getLocation(), stackedBlock.getAmount()));
        return Collections.unmodifiableMap(allStackedBlocks);
    }

    @Override
    public void updateStackedBlockHologram(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        if (stackedBlock != null) {
            stackedBlock.updateName();
            if (stackedBlock.getAmount() <= 1)
                removeStackedBlock(location);
        }
    }

    @Override
    public void updateStackedBlockHolograms(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        this.stackedBlocksContainer.forEach(ChunkPosition.of(chunk), stackedBlock -> {
            stackedBlock.updateName();
            if (stackedBlock.getAmount() <= 1)
                removeStackedBlock(stackedBlock.getLocation());
        });
    }

    @Override
    public void removeStackedBlockHologram(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        if (!(location instanceof LazyWorldLocation))
            Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        if (stackedBlock != null) {
            stackedBlock.removeHologram();
        }
    }

    @Override
    public void removeStackedBlockHolograms(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        this.stackedBlocksContainer.forEach(ChunkPosition.of(chunk), StackedBlock::removeHologram);
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    public void forEach(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer) {
        this.stackedBlocksContainer.forEach(chunkPosition, consumer);
    }

    public void saveStackedBlocks() {
        StackedBlocksDatabaseBridge.deleteStackedBlocks(this);

        try {
            databaseBridge.batchOperations(true);
            this.stackedBlocksContainer.forEach(stackedBlock -> {
                if (stackedBlock.getAmount() > 1) {
                    StackedBlocksDatabaseBridge.saveStackedBlock(this, stackedBlock);
                }
            });
        } finally {
            databaseBridge.batchOperations(false);
        }
    }

    private void loadStackedBlock(DatabaseResult resultSet) {
        Optional<Location> location = resultSet.getString("location").map(Serializers.LOCATION_SPACED_SERIALIZER::deserialize);
        if (!location.isPresent()) {
            Log.warn("Cannot load stacked block from null location, skipping...");
            return;
        }

        if (!(plugin.getProviders().getWorldsProvider() instanceof LazyWorldsProvider)) {
            if (location.get().getWorld() == null) {
                Log.warn("Cannot load stacked block with invalid world ",
                        LazyWorldLocation.getWorldName(location.get()), ", skipping...");
                return;
            }
        }

        Optional<Integer> amount = resultSet.getInt("amount");
        if (!amount.isPresent()) {
            Log.warn("Cannot load stacked block from null amount, skipping...");
            return;
        }

        Optional<String> item = resultSet.getString("block_type");

        Key blockKey;
        if (!item.isPresent() || item.get().isEmpty()) {
            blockKey = null;
        } else {
            String itemValue = item.get();
            Location blockLocation = location.get();
            blockKey = Keys.of(Key.class, new LazyReference<Key>() {

                private final Key baseKey = Keys.ofMaterialAndData(itemValue);
                private final Location keyLocation = blockLocation;

                @Override
                protected Key create() {
                    return Keys.of(this.baseKey, this.keyLocation);
                }
            });
        }

        try {
            StackedBlock stackedBlock = this.stackedBlocksContainer.createStackedBlock(location.get());
            stackedBlock.setAmount(amount.get());
            stackedBlock.setBlockKey(blockKey);
        } catch (IllegalArgumentException error) {
            Log.error(error);
        }
    }

    private void updateStackedBlockKeys() {
        this.stackedBlocksContainer.forEach(stackedBlock ->
                stackedBlock.setBlockKey(Keys.of(stackedBlock.getLocation().getBlock())));
    }

    private void initializeDatabaseBridge() {
        databaseBridge = plugin.getFactory().createDatabaseBridge(this);
        databaseBridge.setDatabaseBridgeMode(DatabaseBridgeMode.SAVE_DATA);
    }

}
