package com.bgsoftware.superiorskyblock.nms.v1_16_R3.crops;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class CropsTickingMethod {

    private static final ReflectField<Random> WORLD_SERVER_RANDOM_TICK_RANDOM = new ReflectField<>(
            WorldServer.class, Random.class, "randomTickRandom");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static Set<Block> CROPS_TO_GROW_CACHE;

    static {
        plugin.getPluginEventsDispatcher().registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CropsTickingMethod::onSettingsUpdate);
    }

    private static final BlockPosition.MutableBlockPosition chunkTickMutablePosition = new BlockPosition.MutableBlockPosition();

    private static final CropsTickingMethod INSTANCE = WORLD_SERVER_RANDOM_TICK_RANDOM.isValid() ?
            new PaperCropsTickingMethod() : new SpigotCropsTickingMethod();

    protected CropsTickingMethod() {

    }

    public static void register() {
        // Calls the static initializer which registers the callback.
    }

    public static void tick(Chunk chunk, int tickSpeed) {
        INSTANCE.doTick(chunk, tickSpeed);
    }

    protected abstract void doTick(Chunk chunk, int tickSpeed);

    private static void onSettingsUpdate() {
        CROPS_TO_GROW_CACHE = new HashSet<>();
        plugin.getSettings().getCropsToGrow().forEach(cropName -> {
            Key key = Keys.ofMaterialAndData(cropName);
            if (key instanceof MaterialKey) {
                Block block = CraftMagicNumbers.getBlock(((MaterialKey) key).getMaterial());
                if (block != null && block.getBlockData().isTicking())
                    CROPS_TO_GROW_CACHE.add(block);
            }
        });
    }

    private static class PaperCropsTickingMethod extends CropsTickingMethod {

        @Override
        protected void doTick(Chunk chunk, int tickSpeed) {
            WorldServer worldServer = chunk.world;

            ChunkCoordIntPair chunkCoord = chunk.getPos();
            int chunkOffsetX = chunkCoord.x << 4;
            int chunkOffsetZ = chunkCoord.z << 4;

            Random randomTickRandom = WORLD_SERVER_RANDOM_TICK_RANDOM.get(worldServer);

            ChunkSection[] sections = chunk.getSections();
            for (int sectionIndex = 0; sectionIndex < 16; ++sectionIndex) {
                ChunkSection chunkSection = sections[sectionIndex];
                if (chunkSection == null || chunkSection == Chunk.a)
                    continue;

                int tickingBlocks = chunkSection.tickingList.size();
                if (tickingBlocks <= 0)
                    continue;

                int offsetY = sectionIndex << 4;

                for (int i = 0; i < tickSpeed; ++i) {
                    int index = randomTickRandom.nextInt(4096);
                    if (index >= tickingBlocks)
                        continue;

                    long raw = chunkSection.tickingList.getRaw(index);
                    int location = com.destroystokyo.paper.util.maplist.IBlockDataList.getLocationFromRaw(raw);
                    IBlockData blockData = com.destroystokyo.paper.util.maplist.IBlockDataList.getBlockDataFromRaw(raw);
                    if (!CROPS_TO_GROW_CACHE.contains(blockData.getBlock()))
                        continue;

                    chunkTickMutablePosition.d((location & 15) | chunkOffsetX,
                            ((location >>> 8) & 255) | offsetY,
                            ((location >>> 4) & 15) | chunkOffsetZ);

                    blockData.b(worldServer, chunkTickMutablePosition, randomTickRandom);
                }

            }

        }

    }

    private static class SpigotCropsTickingMethod extends CropsTickingMethod {

        private static int random = ThreadLocalRandom.current().nextInt();

        @Override
        protected void doTick(Chunk chunk, int tickSpeed) {
            WorldServer worldServer = chunk.world;

            ChunkCoordIntPair chunkCoord = chunk.getPos();
            int chunkOffsetX = chunkCoord.x << 4;
            int chunkOffsetZ = chunkCoord.z << 4;

            for (ChunkSection chunkSection : chunk.getSections()) {
                if (chunkSection != Chunk.a && chunkSection.d()) {
                    for (int i = 0; i < tickSpeed; i++) {
                        random = random * 3 + 1013904223;
                        int factor = random >> 2;
                        int x = factor & 15;
                        int z = factor >> 8 & 15;
                        int y = factor >> 16 & 15;
                        IBlockData blockData = chunkSection.getType(x, y, z);
                        if (blockData.isTicking() && CROPS_TO_GROW_CACHE.contains(blockData.getBlock())) {
                            chunkTickMutablePosition.d(x + chunkOffsetX, y + chunkSection.getYPosition(), z + chunkOffsetZ);
                            blockData.b(worldServer, chunkTickMutablePosition, worldServer.random);
                        }
                    }
                }
            }
        }
    }

}
