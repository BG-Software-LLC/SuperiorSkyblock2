package com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkSection;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class CropsTickingTileEntity extends TileEntity {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;
    private final int chunkX, chunkZ;

    private int currentTick = 0;

    private CropsTickingTileEntity(Island island, ChunkAccess chunk, BlockPosition blockPosition) {
        super(TileEntityTypes.v, blockPosition.getHandle(), chunk.getWorld().getType(blockPosition).getHandle());
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>((Chunk) chunk.getHandle());
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        this.chunkX = chunkCoords.getX();
        this.chunkZ = chunkCoords.getZ();
        a(chunk.getWorld().getHandle());
        chunk.getWorld().setTickingBlockEntity(new CropsTickingTileEntityTicker(this));
    }

    public static void create(Island island, ChunkAccess chunk) {
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        long chunkPair = chunkCoords.pair();
        if (!tickingChunks.containsKey(chunkPair)) {
            BlockPosition blockPosition = new BlockPosition(chunkCoords.getX() << 4, 1, chunkCoords.getZ() << 4);
            tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk, blockPosition));
        }
    }

    public static CropsTickingTileEntity remove(ChunkCoordIntPair chunkCoords) {
        return tickingChunks.remove(chunkCoords.pair());
    }

    public void remove() {
        this.p = true;
    }

    public boolean isRemoved() {
        return super.r();
    }

    public net.minecraft.core.BlockPosition getPosition() {
        return super.p();
    }

    public TileEntityTypes<?> getTileType() {
        return super.u();
    }

    public void tick() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        ChunkAccess chunk = ChunkAccess.ofNullable(this.chunk.get());
        Island island = this.island.get();
        WorldServer world = WorldServer.ofNullable(this.k());

        if (chunk == null || island == null || world == null) {
            remove();
            return;
        }

        currentTick = 0;

        int worldRandomTick = world.getGameRules().getInt(GameRules.n);
        double cropGrowth = island.getCropGrowthMultiplier() - 1;

        int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (net.minecraft.world.level.chunk.ChunkSection nmsSection : chunk.getSections()) {
                ChunkSection chunkSection = ChunkSection.ofNullable(nmsSection);
                if (chunkSection != null && chunkSection.isRandomlyTicking()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        BlockData blockData = chunkSection.getType(x, y, z);
                        Block block = blockData.getBlock();
                        if (block.isTicking(blockData) && plugin.getSettings().getCropsToGrow().contains(
                                CraftMagicNumbers.getMaterial(block.getHandle()).name())) {
                            BlockPosition blockPosition = new BlockPosition(x + (chunkX << 4),
                                    y + chunkSection.getYPosition(), z + (chunkZ << 4));
                            blockData.randomTick(world, blockPosition, ThreadLocalRandom.current());
                        }
                    }
                }
            }
        }
    }

    private final record CropsTickingTileEntityTicker(
            CropsTickingTileEntity cropsTickingTileEntity) implements TickingBlockEntity {

        @Override
        public void a() {
            cropsTickingTileEntity.tick();
        }

        @Override
        public boolean b() {
            return cropsTickingTileEntity.isRemoved();
        }

        @Override
        public net.minecraft.core.BlockPosition c() {
            return cropsTickingTileEntity.getPosition();
        }

        @Override
        public String d() {
            return TileEntityTypes.a(cropsTickingTileEntity.getTileType()) + "";
        }

    }

}
