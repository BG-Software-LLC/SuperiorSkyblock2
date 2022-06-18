package com.bgsoftware.superiorskyblock.external.async;

import com.bgsoftware.superiorskyblock.external.async.AsyncProvider;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public class AsyncProvider_Paper implements AsyncProvider {

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        entity.teleportAsync(location).whenComplete((result, ex) -> {
            if (teleportResult != null)
                teleportResult.accept(result);
        });
    }

}
