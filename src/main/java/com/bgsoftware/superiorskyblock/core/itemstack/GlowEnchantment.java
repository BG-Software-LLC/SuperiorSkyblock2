package com.bgsoftware.superiorskyblock.core.itemstack;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
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
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
