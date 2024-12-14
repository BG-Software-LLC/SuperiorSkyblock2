package com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class NMSCachedBlock implements ICachedBlock {

    private static final ObjectsPool<NMSCachedBlock> POOL = new ObjectsPool<>(NMSCachedBlock::new);

    private BlockData blockData;

    public static NMSCachedBlock obtain(Block block) {
        return POOL.obtain().initialize(block);
    }

    private NMSCachedBlock() {
    }

    private NMSCachedBlock initialize(Block block) {
        this.blockData = block.getBlockData();
        return this;
    }

    @Override
    public void setBlock(Location location) {
        World world = location.getWorld();
        if (world != null)
            world.getBlockAt(location).setBlockData(blockData);
    }

    @Override
    public void release() {
        this.blockData = null;
        POOL.release(this);
    }

}
