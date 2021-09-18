package com.bgsoftware.superiorskyblock.world.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.bridge.StackedBlocksDatabaseBridge;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.world.blocks.container.StackedBlocksContainer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public final class StackedBlocksHandler extends AbstractHandler implements StackedBlocksManager {

    private final StackedBlocksContainer stackedBlocksContainer;
    private final DatabaseBridge databaseBridge;

    public StackedBlocksHandler(SuperiorSkyblockPlugin plugin, StackedBlocksContainer stackedBlocksContainer) {
        super(plugin);
        this.stackedBlocksContainer = stackedBlocksContainer;
        databaseBridge = plugin.getFactory().createDatabaseBridge(this);
        databaseBridge.startSavingData();
    }

    @Override
    public void loadData() {
        SuperiorSkyblockPlugin.log("Starting to load stacked blocks...");

        DatabaseBridge gridLoader = plugin.getFactory().createDatabaseBridge((GridManager) null);

        AtomicBoolean updateBlockKeys = new AtomicBoolean(false);

        gridLoader.loadAllObjects("stacked_blocks", _resultSet -> {
            DatabaseResult resultSet = new DatabaseResult(_resultSet);
            loadStackedBlock(resultSet);
            String item = resultSet.getString("block_type");
            if (item == null || item.isEmpty())
                updateBlockKeys.set(true);
        });

        if (updateBlockKeys.get()) {
            Executor.sync(this::updateStackedBlockKeys);
        }

        SuperiorSkyblockPlugin.log("Finished stacked blocks!");
    }

    @Override
    public int getStackedBlockAmount(Block block) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return getStackedBlockAmount(block.getLocation());
    }

    @Override
    public int getStackedBlockAmount(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        return stackedBlock == null ? 1 : stackedBlock.getAmount();
    }

    @Override
    public Key getStackedBlockKey(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world cannot be null.");
        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        return stackedBlock == null ? null : stackedBlock.getBlockKey();
    }

    @Override
    public boolean setStackedBlock(Block block, int amount) {
        Preconditions.checkNotNull(block, "block parameter cannot be null.");
        return setStackedBlock(block.getLocation(), Key.of(block), amount);
    }

    @Override
    public boolean setStackedBlock(Location location, com.bgsoftware.superiorskyblock.api.key.Key blockKey, int amount) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");
        Preconditions.checkNotNull(blockKey, "blockKey parameter cannot be null.");

        SuperiorSkyblockPlugin.debug("Action: Set Block Amount, Block: " + blockKey + ", Amount: " + amount);

        StackedBlock stackedBlock = this.stackedBlocksContainer.createStackedBlock(location);

        boolean succeed = true;

        if (stackedBlock.getBlockKey() != null && !blockKey.equals(stackedBlock.getBlockKey())) {
            SuperiorSkyblockPlugin.log("Found a glitched stacked-block at " + SBlockPosition.of(location) + " - fixing it...");
            amount = 0;
            succeed = false;
        }

        if (amount > 1) {
            stackedBlock.setBlockKey(blockKey);
            stackedBlock.setAmount(amount);
            stackedBlock.updateName(plugin);
            StackedBlocksDatabaseBridge.saveStackedBlock(this, stackedBlock);
        } else {
            stackedBlock.removeHologram();
            this.stackedBlocksContainer.removeStackedBlock(location);
            StackedBlocksDatabaseBridge.deleteStackedBlock(this, stackedBlock);
        }

        return succeed;
    }

    @Override
    public int removeStackedBlock(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock oldStackedBlock = this.stackedBlocksContainer.removeStackedBlock(location);

        if(oldStackedBlock != null) {
            oldStackedBlock.removeHologram();
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

        ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);
        Map<Location, StackedBlock> chunkStackedBlocks = this.stackedBlocksContainer.removeStackedBlocks(chunkPosition);

        if(!chunkStackedBlocks.isEmpty()) {
            try {
                databaseBridge.batchOperations(true);
                chunkStackedBlocks.values().forEach(stackedBlock -> {
                    stackedBlock.removeHologram();
                    StackedBlocksDatabaseBridge.deleteStackedBlock(this, stackedBlock);
                });
            } finally {
                databaseBridge.batchOperations(false);
            }
        }

        return Collections.unmodifiableMap(convertStackedBlocksMap(chunkStackedBlocks));
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
        Map<Location, StackedBlock> chunkStackedBlocks = this.stackedBlocksContainer.getStackedBlocks(chunkPosition);
        return Collections.unmodifiableMap(convertStackedBlocksMap(chunkStackedBlocks));
    }

    public Collection<StackedBlock> getRealStackedBlocks(ChunkPosition chunkPosition) {
        return Collections.unmodifiableCollection(this.stackedBlocksContainer.getStackedBlocks(chunkPosition).values());
    }

    @Override
    public Map<Location, Integer> getStackedBlocks() {
        Map<Location, StackedBlock> stackedBlocks = this.stackedBlocksContainer.getStackedBlocks();
        return Collections.unmodifiableMap(convertStackedBlocksMap(stackedBlocks));
    }

    @Override
    public void updateStackedBlockHologram(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        if (stackedBlock != null) {
            stackedBlock.updateName(plugin);
            if (stackedBlock.getAmount() <= 1)
                removeStackedBlock(location);
        }
    }

    @Override
    public void updateStackedBlockHolograms(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        this.stackedBlocksContainer.getStackedBlocks(ChunkPosition.of(chunk)).forEach((location, stackedBlock) -> {
            stackedBlock.updateName(plugin);
            if (stackedBlock.getAmount() <= 1)
                removeStackedBlock(location);
        });
    }

    @Override
    public void removeStackedBlockHologram(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        Preconditions.checkNotNull(location.getWorld(), "location's world parameter cannot be null.");

        StackedBlock stackedBlock = this.stackedBlocksContainer.getStackedBlock(location);
        if (stackedBlock != null) {
            stackedBlock.removeHologram();
        }
    }

    @Override
    public void removeStackedBlockHolograms(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");
        this.stackedBlocksContainer.getStackedBlocks(ChunkPosition.of(chunk))
                .values()
                .forEach(StackedBlock::removeHologram);
    }

    @Override
    public DatabaseBridge getDatabaseBridge() {
        return databaseBridge;
    }

    public void saveStackedBlocks() {
        Map<Location, StackedBlock> stackedBlocks = this.stackedBlocksContainer.getStackedBlocks();

        StackedBlocksDatabaseBridge.deleteStackedBlocks(this);

        try {
            databaseBridge.batchOperations(true);
            for (StackedBlock stackedBlock : stackedBlocks.values()) {
                if (stackedBlock.getAmount() > 1) {
                    StackedBlocksDatabaseBridge.saveStackedBlock(this, stackedBlock);
                }
            }
        } finally {
            databaseBridge.batchOperations(false);
        }
    }

    private void loadStackedBlock(DatabaseResult resultSet) {
        String location = resultSet.getString("location");

        int amount = resultSet.getInt("amount");

        String item = resultSet.getString("block_type");
        com.bgsoftware.superiorskyblock.key.Key blockKey = item == null || item.isEmpty() ? null : com.bgsoftware.superiorskyblock.key.Key.of(item);

        StackedBlock stackedBlock = this.stackedBlocksContainer.createStackedBlock(SBlockPosition.of(location).parse());
        stackedBlock.setAmount(amount);
        stackedBlock.setBlockKey(blockKey);
    }

    private void updateStackedBlockKeys() {
        this.stackedBlocksContainer.getStackedBlocks().values().forEach(stackedBlock -> {
            stackedBlock.setBlockKey(Key.of(stackedBlock.getLocation().getBlock()));
        });
    }

    private static Map<Location, Integer> convertStackedBlocksMap(Map<Location, StackedBlock> stackedBlocks) {
        return stackedBlocks.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                value -> value.getValue().getAmount()
        ));
    }

}
