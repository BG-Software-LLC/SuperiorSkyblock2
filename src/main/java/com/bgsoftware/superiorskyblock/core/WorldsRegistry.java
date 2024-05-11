package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorldsRegistry {

    private static final Map<String, SyncedWorld> trackedWorlds = new HashMap<>();

    private WorldsRegistry() {

    }

    public static SyncedWorld getWorld(String name) {
        return trackedWorlds.computeIfAbsent(name, n -> new SyncedWorld(n, Bukkit.getWorld(n)));
    }

    public static void onWorldLoad(World world) {
        SyncedWorld syncedWorld = trackedWorlds.get(world.getName());
        if (syncedWorld != null)
            syncedWorld.bukkitWorld = world;
    }

    public static void onWorldUnload(World world) {
        SyncedWorld syncedWorld = trackedWorlds.get(world.getName());
        if (syncedWorld != null)
            syncedWorld.bukkitWorld = null;
    }

    public static class SyncedWorld {

        private final String worldName;
        @Nullable
        private World bukkitWorld;

        SyncedWorld(String worldName, @Nullable World bukkitWorld) {
            this.worldName = worldName;
            this.bukkitWorld = bukkitWorld;
        }

        @Nullable
        public World getBukkitWorld() {
            return bukkitWorld;
        }

        public String getWorldName() {
            return worldName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SyncedWorld that = (SyncedWorld) o;
            return Objects.equals(worldName, that.worldName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(worldName);
        }

    }

}
