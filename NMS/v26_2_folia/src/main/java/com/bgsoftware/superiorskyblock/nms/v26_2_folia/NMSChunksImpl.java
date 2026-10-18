package com.bgsoftware.superiorskyblock.nms.v26_2_folia;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v26_2.crops.CropsTickingMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMSChunksImpl extends com.bgsoftware.superiorskyblock.nms.v26_2.NMSChunksImpl implements Listener {

    private final Map<ChunkKey, CropsTicker> tickingChunks = new ConcurrentHashMap<>();
    private final AtomicBoolean registered = new AtomicBoolean();

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        CropsTicker ticker = this.tickingChunks.remove(new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ()));
        if (ticker != null)
            ticker.removed = true;
    }

    @Override
    public void injectChunkSections(Chunk chunk) {
        ((NMSWorldImpl) plugin.getNMSWorld()).installTrackers(((CraftWorld) chunk.getWorld()).getHandle());
    }

    @Override
    public void startTickingChunk(Island island, Chunk chunk, boolean stop) {
        if (this.registered.compareAndSet(false, true))
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        Location location = new Location(chunk.getWorld(), chunk.getX() << 4, 0, chunk.getZ() << 4);
        BukkitExecutor.ensureMain(location, () -> {
            ChunkKey key = new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
            if (stop || plugin.getSettings().getCropsInterval() <= 0) {
                CropsTicker ticker = this.tickingChunks.remove(key);
                if (ticker != null)
                    ticker.removed = true;
                return;
            }
            this.tickingChunks.computeIfAbsent(key, ignored -> {
                ServerLevel level = ((CraftWorld) chunk.getWorld()).getHandle();
                CropsTicker ticker = new CropsTicker(key, island, level.getChunk(chunk.getX(), chunk.getZ()));
                level.addBlockEntityTicker(ticker);
                return ticker;
            });
        });
    }

    @Override
    public void updateCropsTicker(List<ChunkPosition> positions, double multiplier) {
        positions.forEach(position -> {
            CropsTicker ticker = this.tickingChunks.get(new ChunkKey(position.getWorld().getUID(), position.getX(), position.getZ()));
            if (ticker != null)
                ticker.multiplier = multiplier;
        });
    }

    private record ChunkKey(UUID world, int x, int z) {
    }

    private class CropsTicker implements TickingBlockEntity {

        private final ChunkKey key;
        private final WeakReference<Island> island;
        private final WeakReference<LevelChunk> chunk;
        private final BlockPos position;
        private volatile double multiplier;
        private volatile boolean removed;
        private int ticks;

        CropsTicker(ChunkKey key, Island island, LevelChunk chunk) {
            this.key = key;
            this.island = new WeakReference<>(island);
            this.chunk = new WeakReference<>(chunk);
            this.position = new BlockPos(key.x << 4, 1, key.z << 4);
            this.multiplier = island.getCropGrowthMultiplier() - 1;
        }

        @Override
        public void tick() {
            if (!plugin.isEnabled()) {
                this.removed = true;
                tickingChunks.remove(this.key, this);
                return;
            }
            int interval = plugin.getSettings().getCropsInterval();
            if (++this.ticks <= interval)
                return;
            this.ticks = 0;
            LevelChunk chunk = this.chunk.get();
            if (chunk == null || this.island.get() == null || !plugin.isEnabled()) {
                this.removed = true;
                tickingChunks.remove(this.key, this);
                return;
            }
            ServerLevel level = (ServerLevel) chunk.getLevel();
            int speed = (int) (level.getGameRules().get(GameRules.RANDOM_TICK_SPEED) * this.multiplier * interval);
            if (speed > 0)
                CropsTickingMethod.tick(chunk, speed);
        }

        @Override
        public boolean isRemoved() {
            return this.removed;
        }

        @Override
        public BlockPos getPos() {
            return this.position;
        }

        @Override
        public BlockEntity getTileEntity() {
            return null;
        }

        @Override
        public String getType() {
            return "superiorskyblock:crops";
        }
    }
}
