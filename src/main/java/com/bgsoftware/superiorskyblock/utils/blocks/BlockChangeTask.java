package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.ListTag;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition, List<BlockData>> blocksCache = Maps.newConcurrentMap();
    private final Island island;

    private boolean submitted = false;

    public BlockChangeTask(Island island){
        this.island = island;
    }

    public void setBlock(Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");
        blocksCache.computeIfAbsent(ChunkPosition.of(location), pairs -> new ArrayList<>())
                .add(new BlockData(location, combinedId, statesTag, tileEntity));
    }

    public void submitUpdate(Runnable onFinish){
        try {
            Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");

            submitted = true;
            int index = 0, size = blocksCache.size();

            for (Map.Entry<ChunkPosition, List<BlockData>> entry : blocksCache.entrySet()) {
                int entryIndex = ++index;
                ChunksProvider.loadChunk(entry.getKey(), chunk -> {
                    plugin.getNMSBlocks().startTickingChunk(island, chunk, false);

                    plugin.getNMSBlocks().refreshLight(chunk);
                    ChunksTracker.markDirty(island, chunk, false);

                    for (BlockData blockData : entry.getValue()) {
                        CompoundTag tileEntity = blockData.tileEntity;
                        if(tileEntity != null) {
                            tileEntity = new CompoundTag(tileEntity);
                            for (int i = 1; i <= 4; i++) {
                                String line = getSignLine(i - 1, tileEntity.getString("Text" + i));
                                if (line != null)
                                    tileEntity.setString("Text" + i, line
                                            .replace("{player}", island.getOwner().getName())
                                            .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                                    );
                            }

                            if(plugin.getSettings().defaultContainersEnabled) {
                                String inventoryType = tileEntity.getString("inventoryType");
                                if (inventoryType != null) {
                                    try {
                                        InventoryType containerType = InventoryType.valueOf(inventoryType);
                                        Registry<Integer, ItemStack> containerContents = plugin.getSettings().defaultContainersContents.get(containerType);
                                        if(containerContents != null) {
                                            ListTag items = new ListTag(CompoundTag.class, new ArrayList<>());
                                            containerContents.entries().forEach(itemEntry -> {
                                                CompoundTag itemCompound = new CompoundTag();
                                                itemCompound.setString("id", plugin.getNMSAdapter().getMinecraftKey(itemEntry.getValue()));
                                                itemCompound.setByte("Count", (byte) itemEntry.getValue().getAmount());
                                                itemCompound.setByte("Slot", (byte) (int) itemEntry.getKey());
                                                items.addTag(itemCompound);
                                            });
                                            tileEntity.setTag("Items", items);
                                        }
                                    }catch (Exception ignored){}
                                }
                            }
                        }

                        plugin.getNMSBlocks().setBlock(chunk, blockData.location, blockData.combinedId,
                                blockData.statesTag, tileEntity);

                        if(tileEntity != null && (tileEntity.containsKey("Text1") || tileEntity.containsKey("Text2") ||
                                tileEntity.containsKey("Text3") || tileEntity.containsKey("Text4")))
                            plugin.getNMSBlocks().handleSignPlace(island, blockData.location);
                    }

                    plugin.getNMSBlocks().refreshChunk(chunk);

                    if(entryIndex == size && onFinish != null)
                        onFinish.run();
                });
            }
        }finally {
            blocksCache.clear();
        }
    }

    private static String getSignLine(int index, String def){
        return index >= plugin.getSettings().defaultSignLines.size() ? def :
                plugin.getSettings().defaultSignLines.get(index);
    }

    private static class BlockData {

        private final Location location;
        private final int combinedId;
        private final CompoundTag statesTag, tileEntity;

        BlockData(Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
            this.location = location;
            this.combinedId = combinedId;
            this.statesTag = statesTag;
            this.tileEntity = tileEntity;
        }

    }

}
