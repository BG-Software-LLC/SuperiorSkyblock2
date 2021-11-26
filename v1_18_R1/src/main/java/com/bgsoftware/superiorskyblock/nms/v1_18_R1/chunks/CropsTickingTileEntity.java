package com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks;

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
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

public final class CropsTickingTileEntity extends TileEntity {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;
    private final int chunkX, chunkZ;

    private int currentTick = 0;

    private CropsTickingTileEntity(Island island, Chunk chunk, BlockPosition blockPosition) {
        super(TileEntityTypes.v, blockPosition, getType(getWorld(chunk), blockPosition));
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(chunk);
        this.chunkX = getPos(chunk).c;
        this.chunkZ = getPos(chunk).d;
        a(getWorld(chunk));
        getWorld(chunk).a(new CropsTickingTileEntityTicker(this));
    }

    public static void create(Island island, Chunk chunk) {
        long chunkPair = pair(getPos(chunk));
        if (!tickingChunks.containsKey(chunkPair)) {
            BlockPosition blockPosition = new BlockPosition(getPos(chunk).c << 4, 1, getPos(chunk).d << 4);
            tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk, blockPosition));
        }
    }

    public static CropsTickingTileEntity remove(ChunkCoordIntPair chunkCoords) {
        return tickingChunks.remove(pair(chunkCoords));
    }

    public void remove() {
        this.p = true;
    }

    public void tick() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        Chunk chunk = this.chunk.get();
        Island island = this.island.get();
        World world = this.k();

        if (chunk == null || island == null || world == null) {
            remove();
            return;
        }

        currentTick = 0;

        int worldRandomTick = getGameRules(world).c(GameRules.n);
        double cropGrowth = island.getCropGrowthMultiplier() - 1;

        int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (ChunkSection chunkSection : getSections(chunk)) {
                if (chunkSection != null && chunkSection.d()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        IBlockData blockData = getType(chunkSection, x, y, z);
                        Block block = getBlock(blockData);
                        if (isTicking(block, blockData) && plugin.getSettings().getCropsToGrow().contains(CraftMagicNumbers.getMaterial(block).name())) {
                            BlockPosition blockPosition = new BlockPosition(x + (chunkX << 4), y + getYPosition(chunkSection), z + (chunkZ << 4));
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
            return isRemoved(cropsTickingTileEntity);
        }

        @Override
        public BlockPosition c() {
            return getPosition(cropsTickingTileEntity);
        }

        @Override
        public String d() {
            return TileEntityTypes.a(getTileType(cropsTickingTileEntity)) + "";
        }

    }

}
