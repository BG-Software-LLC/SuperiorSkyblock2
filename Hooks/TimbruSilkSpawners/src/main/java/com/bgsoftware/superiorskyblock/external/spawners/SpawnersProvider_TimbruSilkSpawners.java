package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import de.dustplanet.util.SilkUtil;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SpawnersProvider_TimbruSilkSpawners implements SpawnersProvider_AutoDetect {

    private static boolean registered = false;

    private final SilkUtil silkUtil;

    public SpawnersProvider_TimbruSilkSpawners() {
        this.silkUtil = SilkUtil.hookIntoSilkSpanwers();

        if (!registered) {
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

}
