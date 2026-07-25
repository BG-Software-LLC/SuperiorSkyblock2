package com.bgsoftware.superiorskyblock.nms.v1_21;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.craftbukkit.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.minecart.PoweredMinecart;

public class NMSEntitiesImpl extends com.bgsoftware.superiorskyblock.nms.v1_21.AbstractNMSEntities {

    private static final Ingredient MINECART_FURNACE_INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

    @Override
    public boolean isMinecartFuel(org.bukkit.inventory.ItemStack bukkitItem, PoweredMinecart minecart) {
        return ((CraftMinecartFurnace) minecart).getHandle().fuel + 3600 <= 32000 &&
                MINECART_FURNACE_INGREDIENT.test(CraftItemStack.asNMSCopy(bukkitItem));
    }

    @Override
    protected int getPortalTicks(Entity entity) {
        PortalProcessor portalProcessor = entity.portalProcess;
        return portalProcessor == null ? 0 : portalProcessor.getPortalTime();
    }

}
