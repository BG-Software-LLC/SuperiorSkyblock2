package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.enchantments.Enchantment;

public class GlowEnchantment {

    private static Enchantment glowEnchant;

    private GlowEnchantment() {

    }

    public static void registerGlowEnchantment(SuperiorSkyblockPlugin plugin) {
        glowEnchant = plugin.getNMSAlgorithms().createGlowEnchantment();
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
