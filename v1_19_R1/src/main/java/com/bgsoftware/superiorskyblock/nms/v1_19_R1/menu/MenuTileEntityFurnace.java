package com.bgsoftware.superiorskyblock.nms.v1_19_R1.menu;

import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.block.entity.TileEntityFurnaceFurnace;
import org.bukkit.inventory.InventoryHolder;

public final class MenuTileEntityFurnace extends TileEntityFurnaceFurnace {

    private final InventoryHolder holder;

    public MenuTileEntityFurnace(InventoryHolder holder, String title) {
        super(BlockPosition.b, Block.AIR.getBlockData().getHandle());
        this.holder = holder;
        this.a(IChatBaseComponent.b(title));
    }

    @Override
    public InventoryHolder getOwner() {
        return holder;
    }

}
