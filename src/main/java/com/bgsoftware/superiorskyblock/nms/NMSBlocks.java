package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSBlocks {

    void setBlock(Chunk chunk, Location location, int combinedId, BlockType blockType, Object... args);

    void refreshChunk(Chunk chunk);

    ItemStack getFlowerPot(Location location);

    int getCombinedId(Location location);

    void setTileEntityBanner(Object tileEntityBanner, DyeColor dyeColor, List<Pattern> patterns);

    void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, ItemStack[] contents);

    void setTileEntityFlowerPot(Object tileEntityFlowerPot, ItemStack flower);

    void setTileEntitySkull(Object tileEntitySkull, SkullType skullType, BlockFace rotation, String owner);

    void setTileEntitySign(Object tileEntitySign, String[] lines);

}
