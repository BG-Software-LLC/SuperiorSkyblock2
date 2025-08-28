package com.bgsoftware.superiorskyblock.nms;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public interface NMSEntities {

    ItemStack[] getEquipment(EntityEquipment entityEquipment);

    boolean isAnimalFood(ItemStack itemStack, Animals animals);

    boolean isMinecartFuel(ItemStack itemStack, PoweredMinecart minecart);

    int getPortalTicks(Entity entity);

    default boolean canShearSaddleFromEntity(Entity entity) {
        // Not implemented for most versions
        return false;
    }

}
