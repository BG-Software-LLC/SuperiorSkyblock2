package com.bgsoftware.superiorskyblock.missions.blocks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlocksTracker {

    public static final BlocksTracker INSTANCE = new BlocksTracker();

    private final EnumMap<TrackingType, Map<String, BlocksTrackingComponent>> trackingComponentMap = new EnumMap<>(TrackingType.class);
    private EnumMap<TrackingType, Map<String, TrackedBlocksData>> rawData = null;

    private boolean saved = false;

    private BlocksTracker() {

    }

    public void trackBlock(TrackingType trackingType, Block block) {
        getComponent(trackingType, block.getWorld()).add(block.getX(), block.getY(), block.getZ());
    }

    public boolean untrackBlock(TrackingType trackingType, Block block) {
        return ifComponentExists(trackingType, block.getWorld(), false,
                component -> component.remove(block.getX(), block.getY(), block.getZ()));
    }

    public boolean isTracked(TrackingType trackingType, Block block) {
        return ifComponentExists(trackingType, block.getWorld(), false,
                component -> component.contains(block.getX(), block.getY(), block.getZ()));
    }

    public boolean isTracked(TrackingType trackingType, Location blockLocation) {
        return ifComponentExists(trackingType, blockLocation.getWorld(), false, component ->
                component.contains(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ()));
    }

    public void loadTrackedBlocks(TrackingType trackingType, String worldName, ConfigurationSection section) {
        TrackedBlocksData data = new TrackedBlocksData(section);
        synchronized (this.trackingComponentMap) {
            if (this.rawData == null)
                this.rawData = new EnumMap<>(TrackingType.class);
            this.rawData.computeIfAbsent(trackingType, type -> new HashMap<>()).put(worldName, data);
        }
    }

    public void save(ConfigurationSection section) {
        EnumMap<TrackingType, Map<String, BlocksTrackingComponent>> components = new EnumMap<>(TrackingType.class);
        EnumMap<TrackingType, Map<String, TrackedBlocksData>> raw = new EnumMap<>(TrackingType.class);
        synchronized (this.trackingComponentMap) {
            if (this.saved)
                return;
            this.saved = true;
            this.trackingComponentMap.forEach((type, data) -> components.put(type, new HashMap<>(data)));
            if (this.rawData != null)
                this.rawData.forEach((type, data) -> raw.put(type, new HashMap<>(data)));
        }
        try {
            saveInternal(section, TrackingType.PLACED_BLOCKS, components, raw);
            saveInternal(section, TrackingType.BROKEN_BLOCKS, components, raw);
        } catch (RuntimeException | Error error) {
            synchronized (this.trackingComponentMap) {
                this.saved = false;
            }
            throw error;
        }
    }

    private void saveInternal(ConfigurationSection section, TrackingType trackingType,
                              Map<TrackingType, Map<String, BlocksTrackingComponent>> components,
                              Map<TrackingType, Map<String, TrackedBlocksData>> raw) {
        Map<String, BlocksTrackingComponent> trackingComponentMap = components.get(trackingType);

        if (trackingComponentMap == null)
            return;

        trackingComponentMap.forEach((worldName, component) -> {
            component.getBlocks().forEach((chunkKey, blocksBitSet) -> {
                List<Integer> blocks = new LinkedList<>();
                blocksBitSet.forEach(blocks::add);
                if (!blocks.isEmpty())
                    section.set("tracked." + trackingType.path + "." + worldName + "." + chunkKey, blocks);
            });
        });

        if (!raw.isEmpty()) {
            Map<String, TrackedBlocksData> rawData = raw.get(trackingType);
            if (rawData != null) {
                rawData.forEach((worldName, trackedBlocksData) -> {
                    trackedBlocksData.getBlocks().forEach((chunkKey, blocksBitSet) -> {
                        List<Integer> blocks = new LinkedList<>();
                        blocksBitSet.forEach(blocks::add);
                        if (!blocks.isEmpty())
                            section.set("tracked." + trackingType.path + "." + worldName + "." + chunkKey, blocks);
                    });
                });
            }
        }
    }

    private BlocksTrackingComponent getComponent(TrackingType trackingType, World world) {
        return getComponent(trackingType, world, true);
    }

    private BlocksTrackingComponent getComponent(TrackingType trackingType, World world, boolean create) {
        String worldName = world.getName();
        synchronized (this.trackingComponentMap) {
            Map<String, TrackedBlocksData> raw = this.rawData == null ? null : this.rawData.get(trackingType);
            if (raw == null || !raw.containsKey(worldName)) {
                Map<String, BlocksTrackingComponent> components = this.trackingComponentMap.get(trackingType);
                BlocksTrackingComponent existing = components == null ? null : components.get(worldName);
                if (existing != null || !create)
                    return existing;
            }
        }
        BlocksTrackingComponent candidate = new BlocksTrackingComponent(world);
        synchronized (this.trackingComponentMap) {
            BlocksTrackingComponent loaded = loadRawData(trackingType, worldName, candidate);
            if (loaded != null)
                return loaded;
            Map<String, BlocksTrackingComponent> components =
                    this.trackingComponentMap.computeIfAbsent(trackingType, type -> new HashMap<>());
            return create ? components.computeIfAbsent(worldName, name -> candidate) : components.get(worldName);
        }
    }

    private <R> R ifComponentExists(TrackingType trackingType, World world, R def, Function<BlocksTrackingComponent, R> function) {
        BlocksTrackingComponent component = getComponent(trackingType, world, false);
        return component == null ? def : function.apply(component);
    }

    private BlocksTrackingComponent loadRawData(TrackingType trackingType, String worldName,
                                                BlocksTrackingComponent trackingComponent) {
        if (this.rawData != null) {
            Map<String, TrackedBlocksData> rawData = this.rawData.get(trackingType);
            if (rawData != null) {
                TrackedBlocksData trackedBlocksData = rawData.remove(worldName);
                if (trackedBlocksData != null) {
                    trackingComponent.loadBlocks(trackedBlocksData);
                    this.trackingComponentMap.computeIfAbsent(trackingType, i -> new HashMap<>())
                            .put(worldName, trackingComponent);
                    if (rawData.isEmpty()) {
                        this.rawData.remove(trackingType);
                        if(this.rawData.isEmpty())
                            this.rawData = null;
                    }
                    return trackingComponent;
                }
            }
        }

        return null;
    }

    public enum TrackingType {

        PLACED_BLOCKS("placed"),
        BROKEN_BLOCKS("broken");

        private final String path;

        TrackingType(String path) {
            this.path = path;
        }

    }

}
