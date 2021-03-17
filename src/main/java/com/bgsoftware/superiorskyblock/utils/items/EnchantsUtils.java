package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.enchantments.Enchantment;

public final class EnchantsUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final ReflectField<Boolean> ACCEPTING_NEW = new ReflectField<>(Enchantment.class, boolean.class, "acceptingNew");

    private static Enchantment glowEnchant;

    private EnchantsUtils(){

    }

    public static void registerGlowEnchantment(){
        glowEnchant = plugin.getNMSAdapter().getGlowEnchant();
        ACCEPTING_NEW.set(null, true);
        try{
            Enchantment.registerEnchantment(glowEnchant);
        }catch(Exception ignored){}
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
