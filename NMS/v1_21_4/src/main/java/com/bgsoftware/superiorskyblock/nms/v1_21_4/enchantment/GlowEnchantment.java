package com.bgsoftware.superiorskyblock.nms.v1_21_4.enchantment;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import net.minecraft.core.Holder;
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

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class GlowEnchantment {

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
    public static final NamespacedKey GLOW_ENCHANTMENT_KEY = NamespacedKey.minecraft(GLOW_ENCHANTMENT_NAME);

    private static final HandleType HANDLE_TYPE = findHandleType();

    private static final Object HANDLE = initializeHandle();

    @Nullable
    private static Object initializeHandle() {
        Registry<Enchantment> registry = MinecraftServer.getServer().registryAccess()
                .lookup(Registries.ENCHANTMENT).orElse(null);

        if (registry == null)
            return null;

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

        Holder<Enchantment> holder;

        try {
            REGISTRY_FROZEN.set(registry, false);
            try {
                // This may throw IllegalStateException which we don't care about.
                registry.createIntrusiveHolder(handle);
            } catch (Throwable ignored) {
            }
            holder = Registry.registerForHolder(registry, ResourceLocation.withDefaultNamespace(GLOW_ENCHANTMENT_NAME), handle);
        } finally {
            freezeRegistry(registry);
        }

        return switch (HANDLE_TYPE) {
            case HOLDER -> holder;
            case RAW -> handle;
            default -> throw new IllegalStateException("Cannot find valid handle type for enchantments");
        };
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

    private static HandleType findHandleType() {
        Constructor<?> constructor = CraftEnchantment.class.getConstructors()[0];

        if (constructor.getParameterCount() == 1 && constructor.getParameterTypes()[0] == Holder.class) {
            return HandleType.HOLDER;
        } else if (constructor.getParameterCount() == 2 && constructor.getParameterTypes()[0] == NamespacedKey.class &&
                constructor.getParameterTypes()[1] == Enchantment.class) {
            return HandleType.RAW;
        } else {
            return HandleType.UNKNOWN;
        }
    }

    public static CraftEnchantment createEnchantment() {
        Constructor<?> constructor = CraftEnchantment.class.getConstructors()[0];

        try {
            return switch (HANDLE_TYPE) {
                case HOLDER -> (CraftEnchantment) constructor.newInstance(HANDLE);
                case RAW ->
                        (CraftEnchantment) constructor.newInstance(NamespacedKey.minecraft(GLOW_ENCHANTMENT_NAME), HANDLE);
                default -> throw new IllegalStateException("Cannot find valid handle type for enchantments");
            };
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    private GlowEnchantment() {

    }

    private enum HandleType {

        HOLDER,
        RAW,
        UNKNOWN

    }

}
