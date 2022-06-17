package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class NMSCachedBlock implements ICachedBlock {

    private final BlockData blockData;

    public NMSCachedBlock(Block block) {
        this.blockData = block.getBlockData();
    }

    @Override
    public void setBlock(Location location) {
        location.getWorld().getBlockAt(location).setBlockData(blockData);
    }

}
