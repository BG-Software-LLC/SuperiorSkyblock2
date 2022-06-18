package com.bgsoftware.superiorskyblock.external.async;

import com.bgsoftware.superiorskyblock.external.async.AsyncProvider;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public class AsyncProvider_Default implements AsyncProvider {

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        boolean result = entity.teleport(location);
        if (teleportResult != null)
            teleportResult.accept(result);
    }

}
