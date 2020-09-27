package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.holograms.Hologram;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.collect.Maps;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class StackedBlocksHandler {

    private final Map<ChunkPosition, Map<SBlockPosition, StackedBlock>> stackedBlocks = Maps.newHashMap();
    private final SuperiorSkyblockPlugin plugin;

    public StackedBlocksHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    public StackedBlock setStackedBlock(Location location, int amount, Key blockKey){
        return setStackedBlock(SBlockPosition.of(location), amount, blockKey);
    }

    public StackedBlock setStackedBlock(SBlockPosition blockPosition, int amount, Key blockKey){
        StackedBlock stackedBlock = stackedBlocks.computeIfAbsent(ChunkPosition.of(blockPosition), m -> new HashMap<>())
                .computeIfAbsent(blockPosition, b -> new StackedBlock(blockPosition, amount, blockKey));
        stackedBlock.setBlockKey(blockKey);
        stackedBlock.setAmount(amount);
        return stackedBlock;
    }

    public int getBlockAmount(SBlockPosition blockPosition, int def){
        Map<SBlockPosition, StackedBlock> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(blockPosition));
        StackedBlock stackedBlock = chunkStackedBlocks == null ? null : chunkStackedBlocks.get(blockPosition);
        return stackedBlock == null ? def : stackedBlock.getAmount();
    }

    public Key getBlockKey(SBlockPosition blockPosition, Key def){
        Map<SBlockPosition, StackedBlock> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(blockPosition));
        StackedBlock stackedBlock = chunkStackedBlocks == null ? null : chunkStackedBlocks.get(blockPosition);
        return stackedBlock == null ? def : stackedBlock.getBlockKey();
    }

    public Map<SBlockPosition, StackedBlock> getStackedBlocks(ChunkPosition chunkPosition){
        return stackedBlocks.getOrDefault(chunkPosition, new HashMap<>());
    }

    public Collection<Map<SBlockPosition, StackedBlock>> getStackedBlocks(){
        return stackedBlocks.values();
    }

    public Map<SBlockPosition, StackedBlock> removeStackedBlocks(ChunkPosition chunkPosition){
        return stackedBlocks.remove(chunkPosition);
    }

    public void removeStackedBlock(SBlockPosition blockPosition){
        Map<SBlockPosition, StackedBlock> chunkStackedBlocks = stackedBlocks.get(ChunkPosition.of(blockPosition));
        if(chunkStackedBlocks != null)
            chunkStackedBlocks.remove(blockPosition);
    }

    public final class StackedBlock{

        private final SBlockPosition blockPosition;

        private int amount;
        private Key blockKey;
        private Hologram hologram;

        StackedBlock(SBlockPosition blockPosition, int amount, Key blockKey){
            this.blockPosition = blockPosition;
            this.amount = amount;
            this.blockKey = blockKey;
        }

        public SBlockPosition getBlockPosition() {
            return blockPosition;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public Key getBlockKey() {
            return blockKey;
        }

        public void setBlockKey(Key blockKey){
            this.blockKey = blockKey;
        }

        public void updateName(){
            if(amount <= 1){
                removeStackedBlock(blockPosition);
                removeHologram();
            }
            else{
                if(hologram == null)
                    hologram = plugin.getNMSHolograms().createHologram(blockPosition.parse().add(0.5, 1, 0.5));

                if(blockKey == null)
                    blockKey = Key.of(blockPosition.getBlock());

                hologram.setHologramName(plugin.getSettings().stackedBlocksName
                        .replace("{0}", String.valueOf(amount))
                        .replace("{1}", StringUtils.format(blockKey.getGlobalKey()))
                        .replace("{2}", StringUtils.format(amount))
                );
            }

        }

        public void removeHologram(){
            if(hologram != null) {
                hologram.removeHologram();
                hologram = null;
            }
        }

    }

}
