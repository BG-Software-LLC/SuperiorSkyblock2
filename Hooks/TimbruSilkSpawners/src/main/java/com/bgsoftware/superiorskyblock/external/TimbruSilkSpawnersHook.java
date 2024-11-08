package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Locale;

public class TimbruSilkSpawnersHook {

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new SpawnersListener(plugin), plugin);
    }

    private TimbruSilkSpawnersHook() {

    }

    private static class SpawnersListener implements Listener {

        private final SuperiorSkyblockPlugin plugin;

        SpawnersListener(SuperiorSkyblockPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerChange(SilkSpawnersSpawnerChangeEvent e) {
            EntityType oldEntity = EnumHelper.getEnum(EntityType.class, e.getOldEntityID().toUpperCase(Locale.ENGLISH));
            EntityType newEntity = EnumHelper.getEnum(EntityType.class, e.getEntityID().toUpperCase(Locale.ENGLISH));

            if (oldEntity == null || newEntity == null || oldEntity == newEntity)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if (island == null)
                return;

            island.handleBlockBreak(Keys.ofSpawner(oldEntity), 1);
            island.handleBlockPlace(Keys.ofSpawner(newEntity), 1);
        }

    }

}
