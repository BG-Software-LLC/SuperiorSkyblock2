package com.bgsoftware.superiorskyblock.nms.v1_17_R1.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CropsTickingTileEntity extends TileEntity {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;
    private final int chunkX;
    private final int chunkZ;

    private int currentTick = 0;

    private CropsTickingTileEntity(Island island, Chunk chunk, BlockPosition blockPosition) {
        super(TileEntityTypes.v, blockPosition, chunk.getWorld().getType(blockPosition));
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(chunk);
        this.chunkX = chunk.getPos().b;
        this.chunkZ = chunk.getPos().c;
        setWorld(chunk.getWorld());
        chunk.getWorld().a(new CropsTickingTileEntityTicker(this));
    }

    public static void create(Island island, Chunk chunk) {
        long chunkPair = chunk.getPos().pair();
        if (!tickingChunks.containsKey(chunkPair)) {
            BlockPosition blockPosition = new BlockPosition(chunk.getPos().b << 4, 1, chunk.getPos().c << 4);
            tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk, blockPosition));
        }
    }

    public static CropsTickingTileEntity remove(ChunkCoordIntPair chunkCoords) {
        return tickingChunks.remove(chunkCoords.pair());
    }

    public void remove() {
        this.p = true;
    }

    public void tick() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        Chunk chunk = this.chunk.get();
        Island island = this.island.get();
        World world = this.getWorld();

        if (chunk == null || island == null || world == null) {
            remove();
            return;
        }

        currentTick = 0;

        int worldRandomTick = world.getGameRules().getInt(GameRules.n);
        double cropGrowth = island.getCropGrowthMultiplier() - 1;

        int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != Chunk.a && chunkSection.d()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        IBlockData blockData = chunkSection.getType(x, y, z);
                        Block block = blockData.getBlock();
                        if (block.isTicking(blockData) && plugin.getSettings().getCropsToGrow().contains(CraftMagicNumbers.getMaterial(block).name())) {
                            BlockPosition blockPosition = new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4));
                            blockData.b((WorldServer) world, blockPosition, ThreadLocalRandom.current());
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
        @SuppressWarnings("NullableProblems")
        public BlockPosition c() {
            return cropsTickingTileEntity.getPosition();
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public String d() {
            return TileEntityTypes.a(cropsTickingTileEntity.getTileType()) + "";
        }

    }

}
