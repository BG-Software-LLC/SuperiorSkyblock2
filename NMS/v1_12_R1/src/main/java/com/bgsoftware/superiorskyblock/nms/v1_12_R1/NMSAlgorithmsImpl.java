package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.algorithms.GlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.v1_12_R1.world.KeyBlocksCache;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityFallingBlock;
import net.minecraft.server.v1_12_R1.EntityMinecartAbstract;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftMinecart;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Optional;

public class NMSAlgorithmsImpl implements NMSAlgorithms {

    private final SuperiorSkyblockPlugin plugin;

    public NMSAlgorithmsImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

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
        //noinspection deprecation
        return material.getId() + (data << 12);
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    @Override
    public Key getBlockKey(int combinedId) {
        IBlockData blockData = Block.getByCombinedId(combinedId);
        return KeyBlocksCache.getBlockKey(blockData);
    }

    @Override
    public Key getMinecartBlock(org.bukkit.entity.Minecart bukkitMinecart) {
        EntityMinecartAbstract minecart = ((CraftMinecart) bukkitMinecart).getHandle();
        return KeyBlocksCache.getBlockKey(minecart.getDisplayBlock());
    }

    @Override
    public Key getFallingBlockType(FallingBlock bukkitFallingBlock) {
        EntityFallingBlock fallingBlock = ((CraftFallingBlock) bukkitFallingBlock).getHandle();
        return Optional.ofNullable(fallingBlock.getBlock()).map(KeyBlocksCache::getBlockKey).orElse(ConstantKeys.AIR);
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
    public int getMaxWorldSize() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return server.getPropertyManager().getInt("max-world-size", 29999984);
    }

    @Override
    public double getCurrentTps() {
        return Bukkit.getTPS()[0];
    }

    @Override
    public int getDataVersion() {
        return -1;
    }

    @Override
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return defaultHolder;
    }

}
