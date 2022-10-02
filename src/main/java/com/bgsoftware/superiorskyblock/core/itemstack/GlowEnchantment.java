package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.enchantments.Enchantment;

public class GlowEnchantment {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final ReflectField<Boolean> ACCEPTING_NEW = new ReflectField<>(Enchantment.class, boolean.class, "acceptingNew");

    private static Enchantment glowEnchant;

    private GlowEnchantment() {

    }

    public static void registerGlowEnchantment() {
        glowEnchant = plugin.getNMSAlgorithms().getGlowEnchant();
        ACCEPTING_NEW.set(null, true);
        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        } finally {
            ACCEPTING_NEW.set(null, false);
        }
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
