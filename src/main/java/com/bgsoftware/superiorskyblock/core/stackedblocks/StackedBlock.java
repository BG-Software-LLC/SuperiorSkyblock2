package com.bgsoftware.superiorskyblock.core.stackedblocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import org.bukkit.Location;

public class StackedBlock {

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
    }

    public void updateName(SuperiorSkyblockPlugin plugin) {
        if (removed) {
            removeHologram();
            return;
        }

        if (amount <= 1) {
            removeHologram();
        } else {
            Key currentBlockKey = KeyImpl.of(location.getBlock());

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
                hologram = plugin.getServices().getHologramsService().createHologram(getLocation().add(0.5, 1, 0.5));

            hologram.setHologramName(plugin.getSettings().getStackedBlocks().getCustomName()
                    .replace("{0}", String.valueOf(amount))
                    .replace("{1}", Formatters.CAPITALIZED_FORMATTER.format(blockKey.getGlobalKey()))
                    .replace("{2}", Formatters.NUMBER_FORMATTER.format(amount))
            );
        }

    }

    public void removeHologram() {
        if (hologram != null) {
            hologram.removeHologram();
            hologram = null;
        }
    }

}
