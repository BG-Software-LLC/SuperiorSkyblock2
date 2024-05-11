package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WorldBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private WorldBlocks() {

    }

    public static boolean isSafeBlock(Block block) {
        return _isSafeBlock(block) || _isSafeBlock(block.getRelative(BlockFace.DOWN));
    }

    private static boolean _isSafeBlock(Block block) {
        Block feetBlock = block.getRelative(BlockFace.UP);
        Block headBlock = feetBlock.getRelative(BlockFace.UP);

        if (feetBlock.getType().isSolid() || headBlock.getType().isSolid())
            return false;

        return plugin.getSettings().getSafeBlocks().contains(Keys.of(block));
    }

    public static boolean isSafeBlock(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        Key feetBlockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y + 1, z);
        Key headBlockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y + 2, z);

        try {
            if (Material.valueOf(feetBlockKey.getGlobalKey()).isSolid() ||
                    Material.valueOf(headBlockKey.getGlobalKey()).isSolid())
                return false;
        } catch (IllegalArgumentException error) {
            return false;
        }

        Key standingBlockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y, z);
        return plugin.getSettings().getSafeBlocks().contains(standingBlockKey);
    }

    public static boolean isChunkEmpty(Island island, ChunkSnapshot chunkSnapshot) {
        for (int i = 0; i < 16; i++) {
            if (!chunkSnapshot.isSectionEmpty(i)) {
                return false;
            }
        }

        island.markChunkEmpty(Bukkit.getWorld(chunkSnapshot.getWorldName()),
                chunkSnapshot.getX(), chunkSnapshot.getZ(), true);

        return true;
    }

    public static Location getChunkBlock(ChunkPosition chunkPosition, int x, int y, int z) {
        int realWorldChunkX = chunkPosition.getX() << 4;
        int realWorldChunkZ = chunkPosition.getZ() << 4;

        return new LazyWorldLocation(chunkPosition.getWorldName(),
                realWorldChunkX + x, y, realWorldChunkZ + z, 0, 0);
    }

}
