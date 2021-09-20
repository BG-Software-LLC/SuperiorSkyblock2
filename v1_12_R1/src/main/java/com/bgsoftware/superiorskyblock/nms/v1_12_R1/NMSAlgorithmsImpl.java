package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.algorithms.GlowEnchantment;
import com.bgsoftware.superiorskyblock.key.Key;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;

public final class NMSAlgorithmsImpl implements NMSAlgorithms {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public String parseSignLine(String original) {
        return IChatBaseComponent.ChatSerializer.a(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData = world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        return 0;
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    @Override
    public Key getBlockKey(int combinedId) {
        //noinspection deprecation
        Material material = Material.getMaterial(combinedId & 4095);
        byte data = (byte) (combinedId >> 12 & 15);
        return Key.of(material, data);
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        MaterialData materialData = minecart.getDisplayBlock();
        //noinspection deprecation
        return Key.of(materialData.getItemType(), materialData.getData());
    }

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

    @Override
    public void setCustomModel(ItemMeta itemMeta, int customModel) {
        // Doesn't exist
    }

    @Override
    public void addPotion(PotionMeta potionMeta, PotionEffect potionEffect) {
        if (!potionMeta.hasCustomEffects())
            potionMeta.setColor(potionEffect.getType().getColor());
        potionMeta.addCustomEffect(potionEffect, true);
    }

    @Override
    public String getMinecraftKey(org.bukkit.inventory.ItemStack itemStack) {
        MinecraftKey minecraftKey = Item.REGISTRY.b(CraftItemStack.asNMSCopy(itemStack).getItem());
        return minecraftKey == null ? "minecraft:air" : minecraftKey.toString();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return GlowEnchantment.createEnchantment();
    }

    @Override
    public Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return defaultHolder;
    }

}
