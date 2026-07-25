package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.craftbukkit.entity.CraftMinecartFurnace;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.minecart.PoweredMinecart;

import java.lang.reflect.Modifier;

public class NMSEntitiesImpl extends com.bgsoftware.superiorskyblock.nms.v1_20_4.AbstractNMSEntities {

    private static final ReflectField<Integer> PORTAL_TICKS = new ReflectField<>(
            Entity.class, int.class, Modifier.PROTECTED, 2);
    private static final Ingredient MINECART_FURNACE_INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

    @Override
    public boolean isMinecartFuel(org.bukkit.inventory.ItemStack bukkitItem, PoweredMinecart minecart) {
        return ((CraftMinecartFurnace) minecart).getHandle().fuel + 3600 <= 32000 &&
                MINECART_FURNACE_INGREDIENT.test(CraftItemStack.asNMSCopy(bukkitItem));
    }

    @Override
    protected int getPortalTicks(Entity entity) {
        return PORTAL_TICKS.get(entity);
    }

}
