package com.bgsoftware.superiorskyblock.core.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import org.bukkit.Location;
import org.bukkit.World;

public class StackedBlock {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<HologramsService> hologramsService = new LazyReference<HologramsService>() {
        @Override
        protected HologramsService create() {
            return plugin.getServices().getService(HologramsService.class);
        }
    };

    private final LazyWorldLocation location;

    private int amount;
    private Key blockKey;
    private Hologram hologram;
    private boolean removed;

    public StackedBlock(Location location) {
        this.location = LazyWorldLocation.of(location);
    }

    public LazyWorldLocation getLocation() {
        return location;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Key getBlockKey() {
        return blockKey;
    }

    public void setBlockKey(Key blockKey) {
        this.blockKey = blockKey;
    }

    public void markAsRemoved() {
        removed = true;
        removeHologram();
    }

    public void updateName() {
        if (this.removed || this.amount <= 1) {
            removeHologram();
            return;
        }

        World world = this.location.getWorld();
        if (world == null)
            return;

        Key currentBlockKey = Keys.of(location.getBlock());

        if (blockKey == null || blockKey.equals(ConstantKeys.AIR)) {
            blockKey = currentBlockKey;
            if (blockKey.equals(ConstantKeys.AIR))
                return;
        }

        // Must be checked in order to fix issue #632
        if (!currentBlockKey.equals(blockKey)) {
            removeHologram();
            return;
        }

        if (hologram == null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location location = copyLocation(wrapper.getHandle()).add(0.5, 1, 0.5);
                hologram = hologramsService.get().createHologram(location);
            }
        }

        hologram.setHologramName(plugin.getSettings().getStackedBlocks().getCustomName()
                .replace("{0}", String.valueOf(amount))
                .replace("{1}", Formatters.CAPITALIZED_FORMATTER.format(blockKey.getGlobalKey()))
                .replace("{2}", Formatters.NUMBER_FORMATTER.format(amount))
        );
    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.removeHologram();
            hologram = null;
        }
    }

    private Location copyLocation(Location location) {
        location.setX(this.location.getX());
        location.setY(this.location.getY());
        location.setZ(this.location.getZ());
        location.setYaw(this.location.getYaw());
        location.setPitch(this.location.getPitch());
        location.setWorld(this.location.getWorld());
        return location;
    }

}
