package com.bgsoftware.superiorskyblock.nms.v1_19_R1.algorithms;

import org.bukkit.enchantments.Enchantment;

public final class GlowEnchantmentFactory {

    public static Enchantment createEnchantment() {
        try {
            return new PaperGlowEnchantment("superior_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("superior_glowing_enchant");
        }
    }

}
