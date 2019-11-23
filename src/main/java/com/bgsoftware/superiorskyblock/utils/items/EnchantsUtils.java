package com.bgsoftware.superiorskyblock.utils.items;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

public final class EnchantsUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static Enchantment glowEnchant;

    public static void registerGlowEnchantment(){
        glowEnchant = plugin.getNMSAdapter().getGlowEnchant();

        try{
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        }catch(Exception ignored){}

        try{
            Enchantment.registerEnchantment(glowEnchant);
        }catch(Exception ignored){}
    }

    public static Enchantment getGlowEnchant() {
        return glowEnchant;
    }
}
