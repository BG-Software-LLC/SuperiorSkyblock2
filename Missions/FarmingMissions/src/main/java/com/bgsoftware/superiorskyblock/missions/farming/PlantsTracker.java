package com.bgsoftware.superiorskyblock.missions.farming;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlantsTracker {

    public static final PlantsTracker INSTANCE = new PlantsTracker();

    private final Map<String, PlantsTrackingComponent> plantsTracker = new HashMap<>();
    private Map<String, TrackedPlantsData> rawData = null;
    private Map<String, Map<UUID, List<BlockPosition>>> legacyRawData = null;
    private boolean saved = false;

    private PlantsTracker() {

    }

    public void track(Block block, UUID placer) {
        track(block.getWorld(), block.getX(), block.getY(), block.getZ(), placer);
    }

    public void track(World world, int x, int y, int z, UUID placer) {
        loadRawData(world);
        plantsTracker.computeIfAbsent(world.getName(), s -> new PlantsTrackingComponent(world))
                .track(x, y, z, placer);
    }

    public void untrack(Block block) {
        untrack(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public void untrack(World world, int x, int y, int z) {
        loadRawData(world);
        PlantsTrackingComponent trackingComponent = this.plantsTracker.get(world.getName());
        if (trackingComponent != null)
            trackingComponent.untrack(x, y, z);
    }

    @Nullable
    public UUID getPlacer(Location location) {
        World world = location.getWorld();
        return world == null ? null : getPlacer(world, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Nullable
    public UUID getPlacer(World world, int x, int y, int z) {
        loadRawData(world);
        PlantsTrackingComponent trackingComponent = this.plantsTracker.get(world.getName());
        return trackingComponent == null ? null : trackingComponent.getPlacer(x, y, z);
    }

    public void load(String worldName, long chunkKey, List<Integer> blocks, UUID placer) {
        if (this.rawData == null)
            this.rawData = new HashMap<>();
        TrackedPlantsData trackedPlantsData = this.rawData.computeIfAbsent(worldName, w -> new TrackedPlantsData());
        blocks.forEach(block -> trackedPlantsData.track(chunkKey, block, placer));
    }

    public void loadLegacy(String worldName, BlockPosition plant, UUID placer) {
        if (this.legacyRawData == null)
            this.legacyRawData = new HashMap<>();
        this.legacyRawData.computeIfAbsent(worldName, w -> new LinkedHashMap<>())
                .computeIfAbsent(placer, i -> new LinkedList<>()).add(plant);
    }

    public void save(ConfigurationSection section) {
        if (!this.saved) {
            saveInternal(section);
            this.saved = true;
        }
    }

    private void saveInternal(ConfigurationSection section) {
        MutableBoolean savedData = new MutableBoolean(false);

        this.plantsTracker.forEach((worldName, component) -> {
            component.getPlants().forEach((chunkKey, blocks) -> {
                blocks.forEach((block, placer) -> {
                    String path = "placed-plants." + placer + "." + worldName + "." + chunkKey;
                    List<Integer> blocksList = section.getIntegerList(path);
                    blocksList.add(block);
                    section.set(path, blocksList);
                    savedData.set(true);
                });
            });
        });

        if (this.rawData != null) {
            this.rawData.forEach((worldName, worldData) -> {
                worldData.getPlants().forEach((chunkKey, blocks) -> {
                    blocks.forEach((block, placer) -> {
                        String path = "placed-plants." + placer + "." + worldName + "." + chunkKey;
                        List<Integer> blocksList = section.getIntegerList(path);
                        blocksList.add(block);
                        section.set(path, blocksList);
                        savedData.set(true);
                    });
                });
            });
        }

        if (this.legacyRawData != null) {
            this.legacyRawData.forEach((worldName, worldData) -> {
                worldData.forEach((placer, plants) -> {
                    plants.forEach(plant -> {
                        String plantKey = worldName + ";" + plant.getX() + ";" + plant.getY() + ";" + plant.getZ();
                        section.set("placed-plants-legacy." + plantKey, placer.toString());
                        savedData.set(true);
                    });
                });
            });
        }

        if (savedData.get()) {
            section.set("data-version", 1);
        }
    }

    private void loadRawData(World world) {
        String worldName = world.getName();

        if (this.rawData != null) {
            TrackedPlantsData rawDataForWorld = this.rawData.remove(worldName);
            if (rawDataForWorld != null) {
                plantsTracker.put(worldName, new PlantsTrackingComponent(world, rawDataForWorld));

                if (this.rawData.isEmpty())
                    this.rawData = null;
            }
        }

        if (this.legacyRawData != null) {
            Map<UUID, List<BlockPosition>> legacyRawDataForWorld = this.legacyRawData.remove(worldName);
            if (legacyRawDataForWorld != null) {
                PlantsTrackingComponent plantsTrackingComponent = new PlantsTrackingComponent(world);
                legacyRawDataForWorld.forEach((placer, plants) -> {
                    plants.forEach(plant -> plantsTrackingComponent.track(plant.getX(), plant.getY(), plant.getZ(), placer));
                });
                this.plantsTracker.put(worldName, plantsTrackingComponent);

                if (this.legacyRawData.isEmpty())
                    this.legacyRawData = null;
            }
        }
    }

}
