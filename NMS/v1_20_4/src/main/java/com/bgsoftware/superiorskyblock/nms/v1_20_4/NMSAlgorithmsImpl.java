package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v1_20_4.AbstractNMSAlgorithms {

    @Override
    public String parseSignLine(String original) {
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0], MinecraftServer.getServer().registryAccess());
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
