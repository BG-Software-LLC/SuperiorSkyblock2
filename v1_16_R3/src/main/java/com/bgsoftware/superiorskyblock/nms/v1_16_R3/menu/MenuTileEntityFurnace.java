package com.bgsoftware.superiorskyblock.nms.v1_16_R3.menu;

import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.TileEntityFurnaceFurnace;
import org.bukkit.inventory.InventoryHolder;

public class MenuTileEntityFurnace extends TileEntityFurnaceFurnace {

    private final InventoryHolder holder;

    public MenuTileEntityFurnace(InventoryHolder holder, String title) {
        this.holder = holder;
        this.setCustomName(new ChatMessage(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
