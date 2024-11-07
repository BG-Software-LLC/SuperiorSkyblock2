package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import de.dustplanet.silkspawners.events.SilkSpawnersSpawnerChangeEvent;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SpawnersProvider_TimbruSilkSpawners implements SpawnersProvider_AutoDetect {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;
    private final SilkUtil silkUtil;

    public SpawnersProvider_TimbruSilkSpawners(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        this.silkUtil = SilkUtil.hookIntoSilkSpanwers();

        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new SpawnersListener(), plugin);
            registered = true;
            Log.info("Using SilkSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return new Pair<>(1, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        String entityId = this.silkUtil.getStoredSpawnerItemEntityID(itemStack);

        if (entityId != null) {
            EntityType entityType = EnumHelper.getEnum(EntityType.class, entityId.toUpperCase(Locale.ENGLISH));
            if (entityType != null)
                return entityType.name();
        }

        return "PIG";
    }

    @SuppressWarnings("unused")
    private class SpawnersListener implements Listener {

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
