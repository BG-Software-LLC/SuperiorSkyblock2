package com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;

public class SpigotGlowEnchantment extends Enchantment {

    private final NamespacedKey key;

    public SpigotGlowEnchantment(String name) {
        this.key = NamespacedKey.minecraft(name);
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

    public boolean isTreasure() {
        return false;
    }

    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
        return true;
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

}
