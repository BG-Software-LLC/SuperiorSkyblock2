package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityMinecartFurnace;
import net.minecraft.server.v1_12_R1.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class NMSEntitiesImpl implements NMSEntities {

    private static final ReflectField<Integer> PORTAL_TICKS = new ReflectField<>(Entity.class, int.class, "al");
    private static final ReflectField<Integer> MINECART_FURNACE_FUEL = new ReflectField<>(
            EntityMinecartFurnace.class, int.class, "d");

    @Override
    public ItemStack[] getEquipment(EntityEquipment entityEquipment) {
        ItemStack[] itemStacks = new ItemStack[7];

        itemStacks[0] = new ItemStack(Material.ARMOR_STAND);
        itemStacks[1] = entityEquipment.getItemInMainHand();
        itemStacks[2] = entityEquipment.getItemInOffHand();
        itemStacks[3] = entityEquipment.getHelmet();
        itemStacks[4] = entityEquipment.getChestplate();
        itemStacks[5] = entityEquipment.getLeggings();
        itemStacks[6] = entityEquipment.getBoots();

        return itemStacks;
    }

    @Override
    public boolean isAnimalFood(ItemStack itemStack, Animals animals) {
        return ((CraftAnimals) animals).getHandle().e(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isMinecartFuel(ItemStack bukkitItem, PoweredMinecart minecart) {
        EntityMinecartFurnace minecartFurnace = (EntityMinecartFurnace) ((CraftMinecartFurnace) minecart).getHandle();
        return MINECART_FURNACE_FUEL.get(minecartFurnace) + 3600 <= 32000 &&
                CraftItemStack.asNMSCopy(bukkitItem).getItem() == Items.COAL;
    }

    @Override
    public int getPortalTicks(org.bukkit.entity.Entity entity) {
        return PORTAL_TICKS.get(((CraftEntity) entity).getHandle());
    }

}
