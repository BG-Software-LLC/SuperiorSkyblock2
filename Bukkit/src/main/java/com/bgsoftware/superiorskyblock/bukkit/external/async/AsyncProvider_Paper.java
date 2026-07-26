package com.bgsoftware.superiorskyblock.bukkit.external.async;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.bukkit.external.async.AsyncProvider;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class AsyncProvider_Paper implements AsyncProvider {

    public AsyncProvider_Paper(SuperiorSkyblockPlugin plugin, JavaPlugin javaPlugin) {

    }

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        entity.teleportAsync(location).whenComplete((result, ex) -> {
            if (teleportResult != null)
                teleportResult.accept(result);
        });
    }

}
