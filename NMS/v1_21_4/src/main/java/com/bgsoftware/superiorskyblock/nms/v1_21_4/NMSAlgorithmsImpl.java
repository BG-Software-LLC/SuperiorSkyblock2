package com.bgsoftware.superiorskyblock.nms.v1_21_4;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.Locale;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_4.AbstractNMSAlgorithms {

    @Override
    public String parseSignLine(String original) {
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0], MinecraftServer.getServer().registryAccess());
    }

    @Override
    public void setItemModel(ItemMeta itemMeta, String itemModel) {
        itemMeta.setItemModel(NamespacedKey.fromString(itemModel));
    }

    @Override
    public void setRarity(ItemMeta itemMeta, String rarity) {
        itemMeta.setRarity(ItemRarity.valueOf(rarity));
    }

    @Override
    public void setTrim(ItemMeta itemMeta, String trimMaterial, String trimPattern) {
        if (itemMeta instanceof ArmorMeta armorMeta) {
            Registry<TrimMaterial> materialRegistry = Bukkit.getRegistry(TrimMaterial.class);
            Registry<TrimPattern> patternRegistry = Bukkit.getRegistry(TrimPattern.class);

            if (materialRegistry == null || patternRegistry == null) {
                return;
            }

            TrimMaterial material = materialRegistry.get(NamespacedKey.minecraft(trimMaterial));
            TrimPattern pattern = patternRegistry.get(NamespacedKey.minecraft(trimPattern));

            if (material == null) {
                throw new IllegalArgumentException("Couldn't convert '" + trimMaterial + "' into a trim material");
            }
            if (pattern == null) {
                throw new IllegalArgumentException("Couldn't convert '" + trimPattern + "' into a trim pattern");
            }

            ArmorTrim armorTrim = new ArmorTrim(material, pattern);
            armorMeta.setTrim(armorTrim);
        }
    }

    @Override
    public void setHideTooltip(ItemMeta itemMeta) {
        itemMeta.setHideTooltip(true);
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return BuiltInRegistries.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
    }

    @Override
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.setEnchantmentGlintOverride(true);
    }

    @Override
    public double getCurrentTps() {
        try {
            return MinecraftServer.getServer().tps1.getAverage();
        } catch (Throwable error) {
            //noinspection removal
            return MinecraftServer.getServer().recentTps[0];
        }
    }

    @Override
    public Biome getBiome(String biomeName) {
        NamespacedKey key = NamespacedKey.fromString(biomeName.toLowerCase(Locale.ENGLISH));
        if (key != null) {
            Registry<Biome> registry = Bukkit.getRegistry(Biome.class);
            if (registry != null) {
                Biome biome = registry.get(key);
                if (biome != null) {
                    return biome;
                }
            }
        }

        try {
            return Biome.valueOf(biomeName.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public boolean isSoftExplosion(EntityExplodeEvent e) {
        return e.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK;
    }

}
