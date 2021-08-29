package com.bgsoftware.superiorskyblock.nms.v1_15_R1.algorithms;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

@SuppressWarnings("NullableProblems")
public final class GlowEnchantment extends Enchantment {

    public static GlowEnchantment createEnchantment(){
        return new GlowEnchantment(NamespacedKey.minecraft("superior_glowing_enchant"));
    }

    private GlowEnchantment(NamespacedKey namespacedKey){
        super(namespacedKey);
    }

    @Override
    public String getName() {
        return "SuperiorSkyblockGlow";
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
        return true;
    }

    public boolean isTreasure() {
        return false;
    }

    public boolean isCursed() {
        return false;
    }

}
