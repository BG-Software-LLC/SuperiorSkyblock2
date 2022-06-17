package com.bgsoftware.superiorskyblock.dependencies.provider;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public interface AsyncProvider {

    default void teleport(Entity entity, Location location) {
        teleport(entity, location, r -> {
        });
    }

    void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult);


}
