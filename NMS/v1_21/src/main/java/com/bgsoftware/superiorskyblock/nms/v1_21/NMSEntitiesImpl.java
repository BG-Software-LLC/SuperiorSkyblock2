package com.bgsoftware.superiorskyblock.nms.v1_21;

import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftAnimals;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class NMSEntitiesImpl implements NMSEntities {

    private static final Ingredient MINECART_FURNACE_INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

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
        return ((CraftAnimals) animals).getHandle().isFood(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isMinecartFuel(ItemStack bukkitItem, PoweredMinecart minecart) {
        return ((CraftMinecartFurnace) minecart).getHandle().fuel + 3600 <= 32000 &&
                MINECART_FURNACE_INGREDIENT.test(CraftItemStack.asNMSCopy(bukkitItem));
    }

    @Override
    public int getPortalTicks(org.bukkit.entity.Entity bukkitEntity) {
        Entity entity = ((CraftEntity) bukkitEntity).getHandle();
        PortalProcessor portalProcessor = entity.portalProcess;
        return portalProcessor == null ? 0 : portalProcessor.getPortalTime();
    }

}
