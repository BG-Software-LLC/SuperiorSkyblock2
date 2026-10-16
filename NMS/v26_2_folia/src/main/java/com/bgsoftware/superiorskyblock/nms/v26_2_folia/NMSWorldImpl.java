package com.bgsoftware.superiorskyblock.nms.v26_2_folia;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v26_2.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.spawners.TickingSpawnerBlockEntityNotifier;
import com.bgsoftware.superiorskyblock.nms.v26_2.world.CollectingNeighborUpdaterTracker;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.world.BlockLevelTicksTracker;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.threadedregions.RegionizedWorldData;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import java.util.concurrent.CompletableFuture;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.world.FoliaWorldLoader;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

public class NMSWorldImpl extends com.bgsoftware.superiorskyblock.nms.v26_2.NMSWorldImpl implements Listener {

    private static final Field BLOCK_TICKS = field("blockLevelTicks");
    private static final Field NEIGHBOR_UPDATER = field("neighborUpdater");

    private final Set<UUID> watchedWorlds = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean registered = new AtomicBoolean();

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<World> createWorldAsync(String name, World.Environment environment, ChunkGenerator generator) {
        return FoliaWorldLoader.create(name, environment, generator);
    }

    @Override
    public void listenBlockStateChanges(World world) {
        if (this.registered.compareAndSet(false, true))
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.watchedWorlds.add(world.getUID());
    }

    @EventHandler
    public void onRegionTick(ServerTickStartEvent event) {
        RegionizedWorldData data = TickRegionScheduler.getCurrentRegionizedWorldData();
        if (data != null && this.watchedWorlds.contains(data.regionData.world.getWorld().getUID()))
            installTrackers(data.regionData.world);
    }

    public void installTrackers(ServerLevel level) {
        if (!this.watchedWorlds.contains(level.getWorld().getUID()))
            return;
        RegionizedWorldData data = level.getCurrentWorldData();
        try {
            if (!(data.getBlockLevelTicks() instanceof BlockLevelTicksTracker)) {
                BlockLevelTicksTracker tracker = new BlockLevelTicksTracker(level);
                data.getBlockLevelTicks().merge(tracker, 0L);
                BLOCK_TICKS.set(data, tracker);
            }
            if (!(data.neighborUpdater instanceof CollectingNeighborUpdaterTracker))
                NEIGHBOR_UPDATER.set(data, new CollectingNeighborUpdaterTracker(level));
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Unable to install regional block trackers", error);
        }
    }

    @Override
    public void listenSpawner(Location location, IntFunction<Integer> delayChangeCallback) {
        if (!BukkitExecutor.isOwned(location)) {
            BukkitExecutor.ensureMain(location, () -> listenSpawner(location, delayChangeCallback));
            return;
        }
        SpawnerBlockEntity spawner = NMSUtils.getBlockEntityAt(location, SpawnerBlockEntity.class);
        if (spawner == null)
            return;
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        ListIterator<TickingBlockEntity> iterator = level.getCurrentWorldData().getBlockEntityTickers().listIterator();
        while (iterator.hasNext()) {
            TickingBlockEntity ticker = iterator.next();
            if (ticker.getPos().equals(spawner.getBlockPos()) && !(ticker instanceof TickingSpawnerBlockEntityNotifier))
                iterator.set(new TickingSpawnerBlockEntityNotifier(spawner, ticker, delayChangeCallback));
        }
    }

    private static Field field(String name) {
        try {
            Field field = RegionizedWorldData.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException error) {
            throw new ExceptionInInitializerError(error);
        }
    }
}
