package com.bgsoftware.superiorskyblock.nms.v1_8_R3.algorithms;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

public class GlowEnchantment extends Enchantment {

    private GlowEnchantment(int id) {
        super(id);
    }

    public static GlowEnchantment createEnchantment() {
        int id = 100;

        //noinspection deprecation, StatementWithEmptyBody
        while (Enchantment.getById(++id) != null) {
        }

        return new GlowEnchantment(id);
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

}
