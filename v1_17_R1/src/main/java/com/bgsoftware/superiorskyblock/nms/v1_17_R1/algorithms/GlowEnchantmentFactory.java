package com.bgsoftware.superiorskyblock.nms.v1_17_R1.algorithms;

import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("NullableProblems")
public final class GlowEnchantmentFactory {

    public static Enchantment createEnchantment() {
        try {
            return new PaperGlowEnchantment("superior_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("superior_glowing_enchant");
        }
    }

    static class SpigotGlowEnchantment extends EnchantmentWrapper {

        protected SpigotGlowEnchantment(String name) {
            super(name);
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

    static class PaperGlowEnchantment extends SpigotGlowEnchantment {

        PaperGlowEnchantment(String name) {
            super(name);
        }

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

}
