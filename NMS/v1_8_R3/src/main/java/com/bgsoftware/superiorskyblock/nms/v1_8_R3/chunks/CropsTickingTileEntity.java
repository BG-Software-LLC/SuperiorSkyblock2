package com.bgsoftware.superiorskyblock.nms.v1_8_R3.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IUpdatePlayerListBox;
import net.minecraft.server.v1_8_R3.TileEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class CropsTickingTileEntity extends TileEntity implements IUpdatePlayerListBox {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static Set<Block> CROPS_TO_GROW_CACHE;

    static {
        plugin.getPluginEventsDispatcher().registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CropsTickingTileEntity::onSettingsUpdate);
    }

    private static final Long2ObjectMapView<CropsTickingTileEntity> tickingChunks = CollectionsFactory.createLong2ObjectHashMap();
    private static int random = ThreadLocalRandom.current().nextInt();

    private final WeakReference<Island> island;
    private final WeakReference<Chunk> chunk;
    private final int chunkX;
    private final int chunkZ;

    private int currentTick = 0;

    private double cachedCropGrowthMultiplier;

    public static void register() {
        // Calls the static initializer which registers the callback.
    }

    public static CropsTickingTileEntity remove(long chunkCoords) {
        return tickingChunks.remove(chunkCoords);
    }

    public static void create(Island island, Chunk chunk) {
        long chunkKey = ChunkCoordIntPair.a(chunk.locX, chunk.locZ);
        tickingChunks.computeIfAbsent(chunkKey, i -> new CropsTickingTileEntity(island, chunk));
    }

    public static void forEachChunk(List<ChunkPosition> chunkPositions, Consumer<CropsTickingTileEntity> cropsTickingTileEntityConsumer) {
        if (tickingChunks.isEmpty())
            return;

        chunkPositions.forEach(chunkPosition -> {
            long chunkKey = chunkPosition.asPair();
            CropsTickingTileEntity cropsTickingTileEntity = tickingChunks.get(chunkKey);
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntityConsumer.accept(cropsTickingTileEntity);
        });
    }

    private CropsTickingTileEntity(Island island, Chunk chunk) {
        this.island = new WeakReference<>(island);
        this.chunk = new WeakReference<>(chunk);
        this.chunkX = chunk.locX;
        this.chunkZ = chunk.locZ;
        a(chunk.getWorld());
        a(new BlockPosition(chunkX << 4, 1, chunkZ << 4));
        try {
            world.tileEntityList.add(this);
        } catch (Throwable error) {
            world.a(this);
        }
        this.cachedCropGrowthMultiplier = island.getCropGrowthMultiplier() - 1;
    }

    @Override
    public void c() {
        if (++currentTick <= plugin.getSettings().getCropsInterval())
            return;

        Chunk chunk = this.chunk.get();
        Island island = this.island.get();

        if (chunk == null || island == null) {
            world.tileEntityList.remove(this);
            return;
        }

        currentTick = 0;

        int worldRandomTick = world.getGameRules().c("randomTickSpeed");

        int chunkRandomTickSpeed = (int) (worldRandomTick * this.cachedCropGrowthMultiplier * plugin.getSettings().getCropsInterval());

        if (chunkRandomTickSpeed > 0) {
            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != null && chunkSection.shouldTick()) {
                    for (int i = 0; i < chunkRandomTickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        IBlockData blockData = chunkSection.getType(x, y, z);
                        Block block = blockData.getBlock();
                        if (block.isTicking() && CROPS_TO_GROW_CACHE.contains(block)) {
                            block.a(world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)),
                                    blockData, ThreadLocalRandom.current());
                        }
                    }
                }
            }
        }

    }

    public void setCropGrowthMultiplier(double cropGrowthMultiplier) {
        this.cachedCropGrowthMultiplier = cropGrowthMultiplier;
    }

    private static void onSettingsUpdate() {
        CROPS_TO_GROW_CACHE = new HashSet<>();
        plugin.getSettings().getCropsToGrow().forEach(cropName -> {
            Key key = Keys.ofMaterialAndData(cropName);
            if (key instanceof MaterialKey) {
                Block block = CraftMagicNumbers.getBlock(((MaterialKey) key).getMaterial());
                if (block != null && block.isTicking())
                    CROPS_TO_GROW_CACHE.add(block);
            }
        });
    }

}
