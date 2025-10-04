package com.bgsoftware.superiorskyblock.nms.v1_18;

import com.bgsoftware.superiorskyblock.nms.algorithms.PaperGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.algorithms.SpigotGlowEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_18.AbstractNMSAlgorithms {

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    public String parseSignLine(String original) {
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return Registry.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    private static Enchantment initializeGlowEnchantment() {
        Enchantment glowEnchant;

        try {
            glowEnchant = new PaperGlowEnchantment("superiorskyblock_glowing_enchant");
        } catch (Throwable error) {
            glowEnchant = new SpigotGlowEnchantment("superiorskyblock_glowing_enchant");
        }

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }

        return glowEnchant;
    }

}
