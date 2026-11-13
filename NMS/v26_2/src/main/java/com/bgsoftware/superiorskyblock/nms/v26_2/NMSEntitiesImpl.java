package com.bgsoftware.superiorskyblock.nms.v26_2;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import org.bukkit.craftbukkit.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.SulfurCube;
import org.bukkit.entity.minecart.PoweredMinecart;

public class NMSEntitiesImpl extends com.bgsoftware.superiorskyblock.nms.v26_2.AbstractNMSEntities {

    @Override
    public boolean isMinecartFuel(org.bukkit.inventory.ItemStack bukkitItem, PoweredMinecart minecart) {
        return ((CraftMinecartFurnace) minecart).getHandle().fuel + 3600 <= 32000 &&
                CraftItemStack.asNMSCopy(bukkitItem).is(ItemTags.FURNACE_MINECART_FUEL);
    }

    @Override
    protected int getPortalTicks(Entity entity) {
        PortalProcessor portalProcessor = entity.portalProcess;
        return portalProcessor == null ? 0 : portalProcessor.getPortalTime();
    }

    @Override
    public boolean hasSulfurCubeAbsorbedBlock(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof SulfurCube sulfurCube)) {
            return false;
        }

        return !sulfurCube.getEquipped().isEmpty();
    }

}
