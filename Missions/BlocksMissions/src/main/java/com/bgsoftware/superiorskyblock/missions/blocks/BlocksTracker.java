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
        BlocksTrackingComponent component = getComponentIfExists(trackingType, block.getWorld());
        return component != null && component.remove(block.getX(), block.getY(), block.getZ());
    }

    public boolean isTracked(TrackingType trackingType, Block block) {
        BlocksTrackingComponent component = getComponentIfExists(trackingType, block.getWorld());
        return component != null && component.contains(block.getX(), block.getY(), block.getZ());
    }

    public boolean isTracked(TrackingType trackingType, Location blockLocation) {
        BlocksTrackingComponent component = getComponentIfExists(trackingType, blockLocation.getWorld());
        return component != null && component.contains(blockLocation.getBlockX(),
                blockLocation.getBlockY(), blockLocation.getBlockZ());
    }

    public void loadTrackedBlocks(TrackingType trackingType, String worldName, ConfigurationSection section) {
        if (this.rawData == null)
            this.rawData = new EnumMap<>(TrackingType.class);

        this.rawData.computeIfAbsent(trackingType, t -> new HashMap<>()).put(worldName, new TrackedBlocksData(section));
    }

    public void save(ConfigurationSection section) {
        if (!this.saved) {
            saveInternal(section, TrackingType.PLACED_BLOCKS);
            saveInternal(section, TrackingType.BROKEN_BLOCKS);
            this.saved = true;
        }
    }

    private void saveInternal(ConfigurationSection section, TrackingType trackingType) {
        Map<String, BlocksTrackingComponent> trackingComponentMap = this.trackingComponentMap.get(trackingType);

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

        if (this.rawData != null) {
            Map<String, TrackedBlocksData> rawData = this.rawData.get(trackingType);
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
        BlocksTrackingComponent trackingComponent = loadRawData(trackingType, world);
        if (trackingComponent != null)
            return trackingComponent;

        Map<String, BlocksTrackingComponent> trackingTypeComponents =
                trackingComponentMap.computeIfAbsent(trackingType, t -> new HashMap<>());

        String worldName = world.getName();
        trackingComponent = trackingTypeComponents.get(worldName);
        if (trackingComponent == null) {
            trackingComponent = new BlocksTrackingComponent(world);
            trackingTypeComponents.put(worldName, trackingComponent);
        }

        return trackingComponent;
    }

    private BlocksTrackingComponent getComponentIfExists(TrackingType trackingType, World world) {
        BlocksTrackingComponent trackingComponent = loadRawData(trackingType, world);
        if (trackingComponent != null)
            return trackingComponent;

        Map<String, BlocksTrackingComponent> trackingTypeComponents = trackingComponentMap.get(trackingType);
        return trackingTypeComponents == null ? null : trackingTypeComponents.get(world.getName());
    }

    private BlocksTrackingComponent loadRawData(TrackingType trackingType, World world) {
        String worldName = world.getName();

        if (this.rawData != null) {
            Map<String, TrackedBlocksData> rawData = this.rawData.get(trackingType);
            if (rawData != null) {
                TrackedBlocksData trackedBlocksData = rawData.remove(worldName);
                if (trackedBlocksData != null) {
                    BlocksTrackingComponent trackingComponent = new BlocksTrackingComponent(world);
                    trackingComponent.loadBlocks(trackedBlocksData);
                    this.trackingComponentMap.computeIfAbsent(trackingType, i -> new HashMap<>())
                            .put(worldName, trackingComponent);
                    if (rawData.isEmpty()) {
                        this.rawData.remove(trackingType);
                        if (this.rawData.isEmpty())
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
