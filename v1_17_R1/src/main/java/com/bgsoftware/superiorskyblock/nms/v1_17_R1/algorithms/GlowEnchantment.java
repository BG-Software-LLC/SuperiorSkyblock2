package com.bgsoftware.superiorskyblock.nms.v1_17_R1.algorithms;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

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

    @Override
    public Component displayName(int i) {
        return Component.empty();
    }

    public boolean isTradeable() {
        return false;
    }

    public boolean isDiscoverable() {
        return false;
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.COMMON;
    }

    public float getDamageIncrease(int i, EntityCategory entityCategory) {
        return 0;
    }

    public Set<EquipmentSlot> getActiveSlots() {
        return new HashSet<>();
    }

}
