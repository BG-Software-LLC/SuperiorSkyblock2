package com.bgsoftware.superiorskyblock.nms.v1_21_3.enchantment;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class GlowEnchantment extends CraftEnchantment {

    private static final ReflectField<Boolean> REGISTRY_FROZEN = new ReflectField<>(
            MappedRegistry.class, boolean.class, Modifier.PRIVATE, 1);

    private static final ReflectField<Object> REGISTRY_ALL_TAGS = new ReflectField<>(
            MappedRegistry.class, Object.class, "k");

    private static final ReflectField<Map<TagKey<?>, HolderSet.Named<?>>> REGISTRY_FROZEN_TAGS = new ReflectField<>(
            MappedRegistry.class, Map.class, Modifier.PRIVATE | Modifier.FINAL, 6);

    private static final ReflectMethod<Object> REGISTRY_TAG_SET_UNBOUND;

    static {
        Class<?> tagSetClass = MappedRegistry.class.getDeclaredClasses()[0];
        if (!tagSetClass.isInterface())
            throw new IllegalStateException("TagSet was not found, but " + tagSetClass);

        REGISTRY_TAG_SET_UNBOUND = new ReflectMethod<>(tagSetClass, tagSetClass, 1, new Class[0]);
    }

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

        MinecraftServer.getServer().registryAccess().lookup(Registries.ENCHANTMENT).ifPresent(registry -> {
            try {
                REGISTRY_FROZEN.set(registry, false);
                try {
                    // This may throw IllegalStateException which we don't care about.
                    registry.createIntrusiveHolder(handle);
                } catch (Throwable ignored) {
                }
                Registry.register(registry, ResourceLocation.withDefaultNamespace(GLOW_ENCHANTMENT_NAME), handle);
            } finally {
                freezeRegistry(registry);
            }
        });

        return handle;
    }

    private static void freezeRegistry(Registry<?> registry) {
        try {
            freezeRegistryUnsafe(registry);
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private static void freezeRegistryUnsafe(Registry<?> registry) throws Exception {
        Object allTags = REGISTRY_ALL_TAGS.get(registry);

        Field tagSetMapsField = allTags.getClass().getDeclaredFields()[0];
        tagSetMapsField.setAccessible(true);

        Map<TagKey<?>, HolderSet.Named<?>> tagsMap =
                (Map<TagKey<?>, HolderSet.Named<?>>) tagSetMapsField.get(allTags);
        Map<TagKey<?>, HolderSet.Named<?>> frozenTags = REGISTRY_FROZEN_TAGS.get(registry);

        tagsMap.forEach(frozenTags::putIfAbsent);

        Object unboundTagSet = REGISTRY_TAG_SET_UNBOUND.invoke(registry);
        REGISTRY_ALL_TAGS.set(registry, unboundTagSet);

        registry.freeze();

        frozenTags.forEach(tagsMap::putIfAbsent);

        tagSetMapsField.set(allTags, tagsMap);
        REGISTRY_ALL_TAGS.set(registry, allTags);
    }

    public GlowEnchantment() {
        super(NamespacedKey.minecraft(GLOW_ENCHANTMENT_NAME), HANDLE);
    }

}
