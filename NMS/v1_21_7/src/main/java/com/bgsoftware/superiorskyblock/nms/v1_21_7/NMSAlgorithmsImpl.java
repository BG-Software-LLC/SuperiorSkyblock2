package com.bgsoftware.superiorskyblock.nms.v1_21_7;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.Locale;
import java.util.Objects;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_7.AbstractNMSAlgorithms {

    private static final Gson COMPONENT_GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    public String parseSignLine(String original) {
        RegistryOps<JsonElement> context = MinecraftServer.getServer().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        JsonElement jsonElement = ComponentSerialization.CODEC.encodeStart(context, CraftChatMessage.fromString(original)[0])
                .getOrThrow(JsonParseException::new);
        return COMPONENT_GSON.toJson(jsonElement);
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
            TrimMaterial material = Objects.requireNonNull(Bukkit.getRegistry(TrimMaterial.class)).get(NamespacedKey.minecraft(trimMaterial));
            TrimPattern pattern = Objects.requireNonNull(Bukkit.getRegistry(TrimPattern.class)).get(NamespacedKey.minecraft(trimPattern));

            if (material == null)
                throw new IllegalArgumentException("Couldn't convert " + trimMaterial.toUpperCase(Locale.ENGLISH) +
                        " into trim material, skipping...");
            if (pattern == null)
                throw new IllegalArgumentException("Couldn't convert " + trimPattern.toUpperCase(Locale.ENGLISH) +
                        " into trim pattern, skipping...");

            ArmorTrim armorTrim = new ArmorTrim(material, pattern);
            armorMeta.setTrim(armorTrim);
        }
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

}
