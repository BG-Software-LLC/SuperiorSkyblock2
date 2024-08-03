package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.enchantments.Enchantment;

public class GlowEnchantment {

    private static Enchantment glowEnchant;

    private GlowEnchantment() {

    }

    public static void registerGlowEnchantment(SuperiorSkyblockPlugin plugin) {
        try {
            glowEnchant = plugin.getNMSAlgorithms().createGlowEnchantment();
        } catch (Throwable error) {
            Log.error(error, "Failed to create glow enchantment");
        }
    }

    @Nullable
    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
