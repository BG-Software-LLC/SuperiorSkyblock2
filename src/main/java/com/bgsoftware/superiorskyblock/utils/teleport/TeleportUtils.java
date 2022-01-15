package com.bgsoftware.superiorskyblock.utils.teleport;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public final class TeleportUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private TeleportUtils() {
    }

    public static void teleport(Entity entity, Location location) {
        teleport(entity, location, null);
    }

    public static void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island != null) {
            plugin.getProviders().getWorldsProvider().prepareTeleport(island, location.clone(),
                    () -> teleportEntity(entity, location, teleportResult));
        } else {
            teleportEntity(entity, location, teleportResult);
        }
    }

    private static void teleportEntity(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        plugin.getProviders().getAsyncProvider().teleport(entity, location, teleportResult);
    }

}
