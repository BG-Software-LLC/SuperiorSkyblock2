package com.bgsoftware.superiorskyblock.world.schematic.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.profiler.ProfileType;
import com.bgsoftware.superiorskyblock.core.profiler.Profiler;
import com.bgsoftware.superiorskyblock.core.schematic.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.world.WorldEditSession;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.world.schematic.BaseSchematic;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CachedSuperiorSchematic extends BaseSchematic implements Schematic {

    private final Int2ObjectMapView<WorldEditSessionCache> cachedSessions = CollectionsFactory.createInt2ObjectArrayMap();
    private final SuperiorSchematic baseSchematic;

    public CachedSuperiorSchematic(SuperiorSchematic baseSchematic) {
        super(baseSchematic.getName(), KeyMaps.createEmptyMap());
        this.baseSchematic = baseSchematic;
        populateCache();
    }

    @Override
    public Map<Key, Integer> getBlockCounts() {
        return this.baseSchematic.getBlockCounts();
    }

    @Override
    public List<ChunkPosition> getAffectedChunks() {
        return this.baseSchematic.getAffectedChunks();
    }

    @Override
    public Runnable onTeleportCallback() {
        return this.baseSchematic.onTeleportCallback();
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback, Consumer<Throwable> onFailure) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> pasteSchematic(island, location, callback, onFailure));
            return;
        }

        long profiler = Profiler.start(ProfileType.SCHEMATIC_PLACE, getName());

        Log.debug(Debug.PASTE_SCHEMATIC, this.name, island.getOwner().getName(), location);

        try {
            pasteSchematicAsyncInternal(island, location, profiler, callback, onFailure);
        } catch (Throwable error) {
            Log.debugResult(Debug.PASTE_SCHEMATIC, "Failed Schematic Placement", error);
            Profiler.end(profiler);
            if (onFailure != null)
                onFailure.accept(error);
        }
    }

    private void pasteSchematicAsyncInternal(Island island, Location location, long profiler, Runnable callback, Consumer<Throwable> onFailure) {
        int chunkX = location.getBlockX() & 0xF;
        int chunkZ = location.getBlockZ() & 0xF;

        long placeProfiler = Profiler.start(ProfileType.SCHEMATIC_BLOCKS_PLACE, getName());

        WorldEditSessionCache worldEditSessionCache = this.cachedSessions.get(posKey(chunkX, chunkZ));

        if (worldEditSessionCache == null)
            throw new IllegalStateException("Cannot find cache for (" + chunkX + "," + chunkZ + ")");

        WorldEditSession worldEditSession = worldEditSessionCache.session.buildFromCache(location);

        worldEditSessionCache.prePlaceTasks.forEach(schematicBlock -> {
            schematicBlock.doPrePlace(island);

            Location newBlockLoc = new Location(
                    location.getWorld(),
                    location.getBlockX() - worldEditSessionCache.baseLocation.getX() + schematicBlock.getLocation().getBlockX(),
                    schematicBlock.getLocation().getBlockY(),
                    location.getBlockZ() - worldEditSessionCache.baseLocation.getZ() + schematicBlock.getLocation().getBlockZ()
            );

            worldEditSession.setBlock(newBlockLoc, schematicBlock.getCombinedId(),
                    schematicBlock.getStatesTag(), schematicBlock.getTileEntityData());
        });

        Profiler.end(placeProfiler);

        List<SchematicBlock> postPlaceTasks = worldEditSessionCache.postPlaceTasks.isEmpty() ? Collections.emptyList() : new LinkedList<>();
        worldEditSessionCache.postPlaceTasks.forEach(schematicBlock -> {
            Location newBlockLoc = new Location(
                    location.getWorld(),
                    location.getBlockX() - worldEditSessionCache.baseLocation.getX() + schematicBlock.getLocation().getBlockX(),
                    schematicBlock.getLocation().getBlockY(),
                    location.getBlockZ() - worldEditSessionCache.baseLocation.getZ() + schematicBlock.getLocation().getBlockZ()
            );

            postPlaceTasks.add(schematicBlock.setLocation(newBlockLoc));
        });

        this.baseSchematic.finishPlaceSchematic(worldEditSession, postPlaceTasks,
                island, location, profiler, callback, onFailure);
    }

    @Override
    public Location adjustRotation(Location location) {
        return this.baseSchematic.adjustRotation(location);
    }

    private void populateCache() {
        Log.info("Caching schematics for ", this.name, "...");
        int cacheSize = calculateCacheSize();
        switch (cacheSize) {
            case 1:
                populateCache1Size();
                break;
            case 4:
                populateCache4Size();
                break;
            case 16:
                populateCache16Size();
                break;
        }
    }

    private void populateCache1Size() {
        // Possible chunk offsets: {(0, 0)}
        this.cachedSessions.put(posKey(0, 0), createSessionCache(48, 0));
    }

    private void populateCache4Size() {
        // Possible chunk offsets: {(8, 8), (0, 8), (8, 0), (0, 0)}
        this.cachedSessions.put(posKey(8, 8), createSessionCache(24, 24));
        this.cachedSessions.put(posKey(0, 8), createSessionCache(0, 24));
        this.cachedSessions.put(posKey(8, 0), createSessionCache(24, 0));
        this.cachedSessions.put(posKey(0, 0), createSessionCache(48, 0));
    }

    private void populateCache16Size() {
        // Possible chunk offsets: {(4, 4), (12, 4), (8, 8), (4, 0), (0, 4), (8, 4), (0, 0), (12, 0), (4, 12), (8, 0), (0, 12), (12, 12), (4, 8), (8, 12), (0, 8), (12, 8)}
        this.cachedSessions.put(posKey(4, 4), createSessionCache(-12, -12));
        this.cachedSessions.put(posKey(12, 4), createSessionCache(12, -12));
        this.cachedSessions.put(posKey(8, 8), createSessionCache(24, 24));
        this.cachedSessions.put(posKey(4, 0), createSessionCache(-12, 0));
        this.cachedSessions.put(posKey(0, 4), createSessionCache(0, -12));
        this.cachedSessions.put(posKey(8, 4), createSessionCache(24, -12));
        this.cachedSessions.put(posKey(0, 0), createSessionCache(48, 0));
        this.cachedSessions.put(posKey(12, 0), createSessionCache(12, 0));
        this.cachedSessions.put(posKey(4, 12), createSessionCache(-12, 12));
        this.cachedSessions.put(posKey(8, 0), createSessionCache(24, 0));
        this.cachedSessions.put(posKey(0, 12), createSessionCache(0, 12));
        this.cachedSessions.put(posKey(12, 12), createSessionCache(12, 12));
        this.cachedSessions.put(posKey(4, 8), createSessionCache(-12, 24));
        this.cachedSessions.put(posKey(8, 12), createSessionCache(-24, 12));
        this.cachedSessions.put(posKey(0, 8), createSessionCache(0, 24));
        this.cachedSessions.put(posKey(12, 8), createSessionCache(12, 24));
    }

    private WorldEditSessionCache createSessionCache(int x, int z) {
        World islandsWorld = null;
        for (World world : Bukkit.getWorlds()) {
            if (plugin.getProviders().getWorldsProvider().isIslandsWorld(world)) {
                islandsWorld = world;
                break;
            }
        }
        Preconditions.checkState(islandsWorld != null, "Cannot find valid islands world");
        Location location = new Location(islandsWorld, x, plugin.getSettings().getIslandHeight(), z);
        List<SchematicBlock> prePlaceTasks = new LinkedList<>();
        List<SchematicBlock> postPlaceTasks = new LinkedList<>();
        WorldEditSession session = this.baseSchematic.createSessionWithSchematicBlocks(location, prePlaceTasks, postPlaceTasks);
        session.markForCache(location);
        return new WorldEditSessionCache(session, prePlaceTasks, postPlaceTasks, location);
    }

    private static int posKey(int x, int z) {
        byte xByte = (byte) (x & 0xF);
        byte zByte = (byte) (z & 0xF);
        return xByte << 8 | zByte;
    }

    private static int calculateCacheSize() {
        switch ((plugin.getSettings().getMaxIslandSize() / 4) % 4) {
            case 0:
                return 1;
            case 2:
                return 4;
            // case 1:
            // case 3:
            default:
                return 16;
        }
    }

    private static class WorldEditSessionCache {

        private final WorldEditSession session;
        private final List<SchematicBlock> prePlaceTasks;
        private final List<SchematicBlock> postPlaceTasks;
        private final Location baseLocation;

        WorldEditSessionCache(WorldEditSession session, List<SchematicBlock> prePlaceTasks, List<SchematicBlock> postPlaceTasks, Location baseLocation) {
            this.session = session;
            this.prePlaceTasks = prePlaceTasks;
            this.postPlaceTasks = postPlaceTasks;
            this.baseLocation = baseLocation;
        }

    }

}
