package com.bgsoftware.superiorskyblock.nms;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public interface NMSEntities {

    ItemStack[] getEquipment(EntityEquipment entityEquipment);

    boolean isAnimalFood(ItemStack itemStack, Animals animals);

    int getPortalTicks(Entity entity);

}
