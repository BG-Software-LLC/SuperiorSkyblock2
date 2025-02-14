package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityMinecartFurnace;
import net.minecraft.server.v1_16_R3.IMaterial;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.RecipeItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class NMSEntitiesImpl implements NMSEntities {

    private static final ReflectField<Integer> PORTAL_TICKS = new ReflectField<>(Entity.class, int.class, "portalTicks");
    private static final RecipeItemStack MINECART_FURNACE_FUEL_RECIPE = RecipeItemStack.a(Items.COAL, Items.CHARCOAL);

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
        return ((CraftAnimals) animals).getHandle().k(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public boolean isMinecartFuel(ItemStack bukkitItem, PoweredMinecart minecart) {
        return ((CraftMinecartFurnace) minecart).getHandle().fuel + 3600 <= 32000 &&
                MINECART_FURNACE_FUEL_RECIPE.test(CraftItemStack.asNMSCopy(bukkitItem));
    }

    @Override
    public int getPortalTicks(org.bukkit.entity.Entity entity) {
        return PORTAL_TICKS.get(((CraftEntity) entity).getHandle());
    }

}
