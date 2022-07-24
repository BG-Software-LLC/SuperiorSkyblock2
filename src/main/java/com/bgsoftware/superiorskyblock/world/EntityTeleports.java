package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class EntityTeleports {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private EntityTeleports() {
    }

    public static void teleport(Entity entity, Location location) {
        teleport(entity, location, null);
    }

    public static void teleport(Entity entity, Location location, @Nullable Consumer<Boolean> teleportResult) {
        Island island = plugin.getGrid().getIslandAt(location);

        if (island != null) {
            plugin.getProviders().getWorldsProvider().prepareTeleport(island, location.clone(),
                    () -> teleportEntity(entity, location, teleportResult));
        } else {
            teleportEntity(entity, location, teleportResult);
        }
    }

    public static void teleportUntilSuccess(Entity entity, Location location, long cooldown, @Nullable Runnable onFinish) {
        teleport(entity, location, succeed -> {
            if (!succeed) {
                if (cooldown > 0) {
                    BukkitExecutor.sync(() -> teleportUntilSuccess(entity, location, cooldown, onFinish), cooldown);
                } else {
                    teleportUntilSuccess(entity, location, cooldown, onFinish);
                }
            } else if (onFinish != null) {
                onFinish.run();
            }
        });
    }

    private static void teleportEntity(Entity entity, Location location, @Nullable Consumer<Boolean> teleportResult) {
        entity.eject();
        plugin.getProviders().getAsyncProvider().teleport(entity, location, teleportResult);
    }

}
