package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.nms.NMSEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.EntityAnimal;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Animals;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class NMSEntitiesImpl implements NMSEntities {

    private static final ReflectField<Integer> PORTAL_TICKS = new ReflectField<>(
            Entity.class, int.class, "ah");
    private static final ReflectMethod<Boolean> ANIMAL_BREED_ITEM = new ReflectMethod<>(
            EntityAnimal.class, "n", net.minecraft.world.item.ItemStack.class);

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
        EntityAnimal entityAnimal = ((CraftAnimals) animals).getHandle();
        if (ANIMAL_BREED_ITEM.isValid()) {
            return ANIMAL_BREED_ITEM.invoke(entityAnimal, CraftItemStack.asNMSCopy(itemStack));
        } else {
            return entityAnimal.isBreedItem(CraftItemStack.asNMSCopy(itemStack));
        }
    }

    @Override
    public int getPortalTicks(org.bukkit.entity.Entity entity) {
        return PORTAL_TICKS.get(((CraftEntity) entity).getHandle());
    }

}
