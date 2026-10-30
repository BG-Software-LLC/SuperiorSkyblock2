package com.bgsoftware.superiorskyblock.api.platform;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

public interface TaskScheduler {

    boolean isFolia();

    boolean isTickThread();

    boolean isGlobalThread();

    boolean isOwned(Entity entity);

    boolean isOwned(Location location);

    BukkitTask global(Runnable task, long delay, long period);

    BukkitTask entity(Entity entity, Runnable task, Runnable retired, long delay, long period);

    BukkitTask region(Location location, Runnable task, long delay, long period);

    BukkitTask async(Runnable task, long delay, long period);

    void cancelTasks();

}
