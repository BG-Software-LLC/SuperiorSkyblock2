package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
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
        return isSafeBlockInternal(block) || isSafeBlockInternal(block.getRelative(BlockFace.DOWN));
    }

    private static boolean isSafeBlockInternal(Block block) {
        // Checks that the block in the parameter is safe for teleportation.
        // This means that the block below it is considered "safe", and the two blocks above the safe blocks
        // cannot suffocate the player.

        return !canPlayerSuffocateFromBlock(block) && !canPlayerSuffocateFromBlock(block.getRelative(BlockFace.UP)) &&
                plugin.getSettings().getSafeBlocks().contains(Keys.of(block.getRelative(BlockFace.DOWN)));
    }

    private static boolean canPlayerSuffocateFromBlock(Block block) {
        return canPlayerGetHurtFromBlock(block.getType()) || plugin.getNMSWorld().canPlayerSuffocate(block);
    }

    public static boolean isSafeBlock(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        return isSafeBlockInternal(chunkSnapshot, x, y, z) || isSafeBlockInternal(chunkSnapshot, x, y - 1, z);
    }

    private static boolean isSafeBlockInternal(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        // Checks that the block in the parameter is safe for teleportation.
        // This means that the block below it is considered "safe", and the two blocks above the safe blocks
        // cannot suffocate the player.

        return !canPlayerSuffocateFromBlock(chunkSnapshot, x, y, z) &&
                !canPlayerSuffocateFromBlock(chunkSnapshot, x, y + 1, z) &&
                plugin.getSettings().getSafeBlocks().contains(plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y - 1, z));
    }

    private static boolean canPlayerSuffocateFromBlock(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        if (plugin.getNMSWorld().canPlayerSuffocate(chunkSnapshot, x, y, z))
            return true;

        Key blockKey = plugin.getNMSWorld().getBlockKey(chunkSnapshot, x, y, z);
        return blockKey != null && canPlayerGetHurtFromBlock(((MaterialKey) blockKey).getMaterial());
    }

    private static boolean canPlayerGetHurtFromBlock(Material material) {
        return material == Material.LAVA || material == Material.FIRE;
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
