package com.bgsoftware.superiorskyblock.nms.v1_19;

import com.bgsoftware.superiorskyblock.nms.algorithms.PaperGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.algorithms.SpigotGlowEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_19.AbstractNMSAlgorithms {

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    public String parseSignLine(String original) {
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public void setTrim(ItemMeta itemMeta, String trimMaterial, String trimPattern) {
        if (itemMeta instanceof ArmorMeta armorMeta) {
            TrimMaterial material = Objects.requireNonNull(Bukkit.getRegistry(TrimMaterial.class)).get(NamespacedKey.minecraft(trimMaterial));
            TrimPattern pattern = Objects.requireNonNull(Bukkit.getRegistry(TrimPattern.class)).get(NamespacedKey.minecraft(trimPattern));

            if (material == null)
                throw new IllegalArgumentException("Couldn't convert " + trimMaterial.toUpperCase(Locale.ENGLISH) +
                        " into trim material, skipping...");
            if (pattern == null)
                throw new IllegalArgumentException("Couldn't convert " + trimPattern.toUpperCase(Locale.ENGLISH) +
                        " into trim pattern, skipping...");

            ArmorTrim armorTrim = new ArmorTrim(material, pattern);
            armorMeta.setTrim(armorTrim);
        }
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    @Override
    public double getCurrentTps() {
        try {
            return MinecraftServer.getServer().tps1.getAverage();
        } catch (Throwable error) {
            //noinspection removal
            return MinecraftServer.getServer().recentTps[0];
        }
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
