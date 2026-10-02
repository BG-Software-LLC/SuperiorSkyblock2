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
        getComponent(world, true).track(x, y, z, placer);
    }

    public void untrack(Block block) {
        untrack(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public void untrack(World world, int x, int y, int z) {
        PlantsTrackingComponent trackingComponent = getComponent(world, false);
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
        PlantsTrackingComponent trackingComponent = getComponent(world, false);
        return trackingComponent == null ? null : trackingComponent.getPlacer(x, y, z);
    }

    public void load(String worldName, long chunkKey, List<Integer> blocks, UUID placer) {
        synchronized (this.plantsTracker) {
            if (this.rawData == null)
                this.rawData = new HashMap<>();
            TrackedPlantsData data = this.rawData.computeIfAbsent(worldName, world -> new TrackedPlantsData());
            blocks.forEach(block -> data.track(chunkKey, block, placer));
        }
    }

    public void loadLegacy(String worldName, BlockPosition plant, UUID placer) {
        synchronized (this.plantsTracker) {
            if (this.legacyRawData == null)
                this.legacyRawData = new HashMap<>();
            this.legacyRawData.computeIfAbsent(worldName, world -> new LinkedHashMap<>())
                    .computeIfAbsent(placer, player -> new LinkedList<>()).add(plant);
        }
    }

    public void save(ConfigurationSection section) {
        Map<String, PlantsTrackingComponent> components;
        Map<String, TrackedPlantsData> raw;
        Map<String, Map<UUID, List<BlockPosition>>> legacy = new HashMap<>();
        synchronized (this.plantsTracker) {
            if (this.saved)
                return;
            this.saved = true;
            components = new HashMap<>(this.plantsTracker);
            raw = this.rawData == null ? new HashMap<>() : new HashMap<>(this.rawData);
            if (this.legacyRawData != null) {
                this.legacyRawData.forEach((world, players) -> {
                    Map<UUID, List<BlockPosition>> copy = new LinkedHashMap<>();
                    players.forEach((player, plants) -> copy.put(player, new LinkedList<>(plants)));
                    legacy.put(world, copy);
                });
            }
        }
        try {
            saveInternal(section, components, raw, legacy);
        } catch (RuntimeException | Error error) {
            synchronized (this.plantsTracker) {
                this.saved = false;
            }
            throw error;
        }
    }

    private void saveInternal(ConfigurationSection section, Map<String, PlantsTrackingComponent> components,
                              Map<String, TrackedPlantsData> raw,
                              Map<String, Map<UUID, List<BlockPosition>>> legacy) {
        MutableBoolean savedData = new MutableBoolean(false);

        components.forEach((worldName, component) -> {
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

        if (!raw.isEmpty()) {
            raw.forEach((worldName, worldData) -> {
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

        if (!legacy.isEmpty()) {
            legacy.forEach((worldName, worldData) -> {
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

    private PlantsTrackingComponent getComponent(World world, boolean create) {
        String worldName = world.getName();
        synchronized (this.plantsTracker) {
            if ((this.rawData == null || !this.rawData.containsKey(worldName)) &&
                    (this.legacyRawData == null || !this.legacyRawData.containsKey(worldName))) {
                PlantsTrackingComponent existing = this.plantsTracker.get(worldName);
                if (existing != null || !create)
                    return existing;
            }
        }
        PlantsTrackingComponent candidate = new PlantsTrackingComponent(world);
        synchronized (this.plantsTracker) {
            loadRawData(worldName, candidate);
            return create ? this.plantsTracker.computeIfAbsent(worldName, name -> candidate) :
                    this.plantsTracker.get(worldName);
        }
    }

    private void loadRawData(String worldName, PlantsTrackingComponent candidate) {

        if (this.rawData != null) {
            TrackedPlantsData rawDataForWorld = this.rawData.remove(worldName);
            if (rawDataForWorld != null) {
                plantsTracker.put(worldName, new PlantsTrackingComponent(candidate.getWorldMinHeight(), rawDataForWorld));

                if (this.rawData.isEmpty())
                    this.rawData = null;
            }
        }

        if (this.legacyRawData != null) {
            Map<UUID, List<BlockPosition>> legacyRawDataForWorld = this.legacyRawData.remove(worldName);
            if (legacyRawDataForWorld != null) {
                PlantsTrackingComponent plantsTrackingComponent = candidate;
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
