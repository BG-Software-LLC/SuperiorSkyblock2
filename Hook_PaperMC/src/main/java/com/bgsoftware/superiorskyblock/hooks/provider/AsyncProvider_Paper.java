package com.bgsoftware.superiorskyblock.hooks.provider;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public final class AsyncProvider_Paper implements AsyncProvider {

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        entity.teleportAsync(location).whenComplete((result, ex) -> {
            if (teleportResult != null)
                teleportResult.accept(result);
        });
    }

}
