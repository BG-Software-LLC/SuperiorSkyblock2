package com.bgsoftware.superiorskyblock.utils.teleport;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public final class TeleportUtils {
    
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    
    private TeleportUtils() { }

    public static void teleport(Entity entity, Location location){
        teleport(entity, location, null);
    }
    
    public static void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult){
        Island island = plugin.getGrid().getIslandAt(location);

        if(island != null){
            plugin.getProviders().prepareTeleport(island, location.clone(),
                    () -> _teleport(entity, location, teleportResult));
        }
        else{
            _teleport(entity, location, teleportResult);
        }
    }

    private static void _teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        plugin.getProviders().teleport(entity, location, teleportResult);
    }
    
}
