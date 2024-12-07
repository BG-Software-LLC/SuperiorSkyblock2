package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class NMSCachedBlock implements ICachedBlock {

    private static final ObjectsPool<NMSCachedBlock> POOL = new ObjectsPool<>(NMSCachedBlock::new);

    private Material blockType;
    private byte blockData;

    public static NMSCachedBlock obtain(Block block) {
        return POOL.obtain().initialize(block);
    }

    private NMSCachedBlock() {
    }

    private NMSCachedBlock initialize(Block block) {
        this.blockType = block.getType();
        this.blockData = block.getData();
        return this;
    }

    @Override
    public void setBlock(Location location) {
        Block block = location.getWorld().getBlockAt(location);
        block.setType(blockType);
        block.setData(blockData);
    }

    @Override
    public void release() {
        this.blockType = null;
        POOL.release(this);
    }

}
