package com.bgsoftware.superiorskyblock.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms.PaperGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms.SpigotGlowEnchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_20_3.AbstractNMSAlgorithms {

    private static final ReflectField<Map<NamespacedKey, Enchantment>> REGISTRY_CACHE =
            new ReflectField<>(CraftRegistry.class, Map.class, "cache");

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

    @Override
    public String parseSignLine(String original) {
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
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

        Map<NamespacedKey, Enchantment> registryCache = REGISTRY_CACHE.get(Registry.ENCHANTMENT);

        registryCache.put(glowEnchant.getKey(), glowEnchant);

        return glowEnchant;
    }

}
