package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.MutableBlockPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

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
        return isSafeStandingSpot(chunkSnapshot, x, y, z) || isSafeStandingSpot(chunkSnapshot, x, y - 1, z);
    }

    public static boolean isSafeStandingSpot(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
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
        World world = Bukkit.getWorld(chunkSnapshot.getWorldName());
        int sectionsCount = (world.getMaxHeight() - plugin.getNMSWorld().getMinHeight(world)) >> 4;

        for (int i = 0; i < sectionsCount; i++) {
            if (!chunkSnapshot.isSectionEmpty(i)) {
                return false;
            }
        }

        island.markChunkEmpty(world, chunkSnapshot.getX(), chunkSnapshot.getZ(), true);

        return true;
    }

    public static Location getChunkBlock(ChunkPosition chunkPosition, int x, int y, int z) {
        int realWorldChunkX = chunkPosition.getX() << 4;
        int realWorldChunkZ = chunkPosition.getZ() << 4;

        return new LazyWorldLocation(chunkPosition.getWorldName(),
                realWorldChunkX + x, y, realWorldChunkZ + z, 0, 0);
    }

    public static Island findIslandWithHint(Block block, @Nullable Island hintIsland) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return findIslandWithHint(block.getLocation(wrapper.getHandle()), hintIsland);
        }
    }

    public static Island findIslandWithHint(Entity entity, @Nullable Island hintIsland) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return findIslandWithHint(entity.getLocation(wrapper.getHandle()), hintIsland);
        }
    }

    public static Island findIslandWithHint(Location location, @Nullable Island hintIsland) {
        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
            return findIslandWithHint(location, blockPosition.set(location), hintIsland);
        }
    }

    /**
     * This method is used when we have a hint for the island that the block might be in.
     * This way we optimize an Island call by just checking if {@param blockPosition} is inside the island.
     */
    public static Island findIslandWithHint(Location location, BlockPosition blockPosition, @Nullable Island hintIsland) {
        if (hintIsland != null && hintIsland.isInside(blockPosition)) {
            return hintIsland;
        }

        return plugin.getGrid().getIslandAt(location);
    }

}
