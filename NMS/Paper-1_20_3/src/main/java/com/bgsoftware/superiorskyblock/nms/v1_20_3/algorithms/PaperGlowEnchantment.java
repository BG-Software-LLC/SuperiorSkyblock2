package com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collections;
import java.util.Set;

public class PaperGlowEnchantment extends SpigotGlowEnchantment {

    public PaperGlowEnchantment(String name) {
        super(name);
    }

    @Override
    public Component displayName(int i) {
        return Component.empty();
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.COMMON;
    }

    @Override
    public float getDamageIncrease(int i, EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public Set<EquipmentSlot> getActiveSlots() {
        return Collections.emptySet();
    }

}
