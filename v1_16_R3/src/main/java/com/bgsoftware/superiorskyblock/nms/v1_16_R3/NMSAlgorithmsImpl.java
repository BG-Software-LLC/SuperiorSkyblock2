package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.algorithms.CustomTileEntityHopper;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.algorithms.GlowEnchantmentFactory;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public final class NMSAlgorithmsImpl implements NMSAlgorithms {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
        IBlockData blockData;

        if (data == 0) {
            Block block = CraftMagicNumbers.getBlock(material);
            if (block == null)
                return -1;
            blockData = block.getBlockData();
        } else {
            blockData = CraftMagicNumbers.getBlock(material, data);
        }

        return blockData == null ? -1 : Block.getCombinedId(blockData);
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public Key getBlockKey(int combinedId) {
        Material material = CraftMagicNumbers.getMaterial(Block.getByCombinedId(combinedId).getBlock());
        return Key.of(material, (byte) 0);
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        return Key.of(minecart.getDisplayBlockData().getMaterial(), (byte) 0);
    }

    @Override
    public Key getFallingBlockType(FallingBlock fallingBlock) {
        return Key.of(fallingBlock.getBlockData().getMaterial(), (byte) 0);
    }

    @Override
    public void setCustomModel(ItemMeta itemMeta, int customModel) {
        itemMeta.setCustomModelData(customModel);
    }

    @Override
    public void addPotion(PotionMeta potionMeta, PotionEffect potionEffect) {
        if (!potionMeta.hasCustomEffects())
            potionMeta.setColor(potionEffect.getType().getColor());
        potionMeta.addCustomEffect(potionEffect, true);
    }

    @Override
    public String getMinecraftKey(ItemStack itemStack) {
        return IRegistry.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return GlowEnchantmentFactory.createEnchantment();
    }

    @Override
    public Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return new CustomTileEntityHopper(defaultHolder, title);
    }

}
