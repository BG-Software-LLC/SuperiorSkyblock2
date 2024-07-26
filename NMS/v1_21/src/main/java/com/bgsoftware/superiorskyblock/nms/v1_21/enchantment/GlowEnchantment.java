package com.bgsoftware.superiorskyblock.nms.v1_21.enchantment;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;

import java.lang.reflect.Modifier;

public class GlowEnchantment extends CraftEnchantment {

    private static final ReflectField<Boolean> REGISTRY_FROZEN = new ReflectField<>(
            MappedRegistry.class, boolean.class, Modifier.PRIVATE, 1);

    private static final String GLOW_ENCHANTMENT_NAME = "superiorskyblock_glowing_enchant";

    private static final Enchantment HANDLE = initializeHandle();

    private static Enchantment initializeHandle() {
        Enchantment handle = new Enchantment(
                Component.empty(),
                Enchantment.definition(
                        HolderSet.empty(),
                        1,
                        1,
                        Enchantment.constantCost(0),
                        Enchantment.constantCost(0),
                        0),
                HolderSet.empty(),
                DataComponentMap.EMPTY
        );

        MinecraftServer.getServer().registryAccess().registry(Registries.ENCHANTMENT).ifPresent(registry -> {
            try {
                REGISTRY_FROZEN.set(registry, false);
                try {
                    // This may throw IllegalStateException which we don't care about.
                    registry.createIntrusiveHolder(handle);
                } catch (Throwable ignored) {
                }
                Registry.register(registry, ResourceLocation.withDefaultNamespace(GLOW_ENCHANTMENT_NAME), handle);
            } finally {
                registry.freeze();
            }
        });

        return handle;
    }

    public GlowEnchantment() {
        super(NamespacedKey.minecraft(GLOW_ENCHANTMENT_NAME), HANDLE);
    }

}
