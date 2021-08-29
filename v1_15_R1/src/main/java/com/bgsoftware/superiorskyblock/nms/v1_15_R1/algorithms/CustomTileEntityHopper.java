package com.bgsoftware.superiorskyblock.nms.v1_15_R1.algorithms;

import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.TileEntityHopper;
import org.bukkit.inventory.InventoryHolder;

public final class CustomTileEntityHopper extends TileEntityHopper {

    private final InventoryHolder holder;

    public CustomTileEntityHopper(InventoryHolder holder, String title) {
        this.holder = holder;
        this.setCustomName(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
