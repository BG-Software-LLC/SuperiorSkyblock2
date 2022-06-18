package com.bgsoftware.superiorskyblock.nms.v1_16_R3.algorithms;

import org.bukkit.enchantments.Enchantment;

public class GlowEnchantmentFactory {

    public static Enchantment createEnchantment() {
        try {
            return new PaperGlowEnchantment("superior_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("superior_glowing_enchant");
        }
    }

}
