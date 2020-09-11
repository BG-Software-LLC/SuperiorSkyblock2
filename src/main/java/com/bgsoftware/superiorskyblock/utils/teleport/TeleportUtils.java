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

        Runnable teleportTask = () -> plugin.getProviders().teleport(entity, location,
                teleportResult == null ? r-> {} : teleportResult);

        if(island != null){
            plugin.getProviders().prepareTeleport(island, location.clone(), teleportTask);
        }
        else{
            teleportTask.run();
        }
    }
    
}
