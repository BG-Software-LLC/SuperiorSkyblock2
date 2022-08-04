package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.algorithms.GlowEnchantment;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;

public class NMSAlgorithmsImpl implements NMSAlgorithms {

    private final SuperiorSkyblockPlugin plugin;

    public NMSAlgorithmsImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isMappingsSupported() {
        return true;
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
        // noinspection deprecation
        Material material = Material.getMaterial(combinedId & 4095);
        byte data = (byte) (combinedId >> 12 & 15);
        return KeyImpl.of(material, data);
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        MaterialData materialData = minecart.getDisplayBlock();
        //noinspection deprecation
        return KeyImpl.of(materialData.getItemType(), materialData.getData());
    }

    @Override
    public Key getFallingBlockType(FallingBlock fallingBlock) {
        //noinspection deprecation
        return KeyImpl.of(fallingBlock.getMaterial(), fallingBlock.getBlockData());
    }

    @Override
    public void setCustomModel(ItemMeta itemMeta, int customModel) {
        // Doesn't exist
    }

    @Override
    public void addPotion(PotionMeta potionMeta, PotionEffect potionEffect) {
        potionMeta.addCustomEffect(potionEffect, true);
    }

    @Override
    public String getMinecraftKey(org.bukkit.inventory.ItemStack itemStack) {
        MinecraftKey minecraftKey = Item.REGISTRY.c(CraftItemStack.asNMSCopy(itemStack).getItem());
        return minecraftKey == null ? "minecraft:air" : minecraftKey.toString();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return GlowEnchantment.createEnchantment();
    }

    @Override
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return defaultHolder;
    }

}
