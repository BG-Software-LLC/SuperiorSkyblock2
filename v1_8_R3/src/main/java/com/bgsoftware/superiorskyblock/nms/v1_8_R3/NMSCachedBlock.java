package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class NMSCachedBlock implements ICachedBlock {

    private final Material blockType;
    private final byte blockData;

    public NMSCachedBlock(Block block) {
        this.blockType = block.getType();
        this.blockData = block.getData();
    }

    @Override
    public void setBlock(Location location) {
        Block block = location.getWorld().getBlockAt(location);
        block.setType(blockType);
        block.setData(blockData);
    }

}
