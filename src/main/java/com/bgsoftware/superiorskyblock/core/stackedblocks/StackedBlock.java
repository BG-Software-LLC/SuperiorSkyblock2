package com.bgsoftware.superiorskyblock.core.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.api.service.hologram.HologramsService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import org.bukkit.Location;

public class StackedBlock {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<HologramsService> hologramsService = new LazyReference<HologramsService>() {
        @Override
        protected HologramsService create() {
            return plugin.getServices().getService(HologramsService.class);
        }
    };

    private final Location location;

    private int amount;
    private Key blockKey;
    private Hologram hologram;
    private boolean removed;

    public StackedBlock(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location.clone();
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

        if (hologram == null)
            hologram = hologramsService.get().createHologram(getLocation().add(0.5, 1, 0.5));

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

}
