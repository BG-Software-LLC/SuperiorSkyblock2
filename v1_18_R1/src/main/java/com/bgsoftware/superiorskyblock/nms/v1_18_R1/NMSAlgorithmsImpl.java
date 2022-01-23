package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.algorithms.CustomTileEntityHopper;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.algorithms.GlowEnchantmentFactory;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.RegistryBlocks;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.network.chat.ChatBaseComponent;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
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
        return ChatBaseComponent.ChatSerializer.toJson(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public int getCombinedId(Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return 0;

        WorldServer world = new WorldServer(((CraftWorld) bukkitWorld).getHandle());
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockData blockData = world.getType(blockPosition);
        return Block.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        BlockData blockData;

        if (data == 0) {
            Block block = Block.ofNullable(CraftMagicNumbers.getBlock(material));
            if (block == null)
                return -1;
            blockData = block.getBlockData();
        } else {
            blockData = BlockData.ofNullable(CraftMagicNumbers.getBlock(material, data));
        }

        return blockData == null ? -1 : Block.getCombinedId(blockData);
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? Block.getCombinedId(new Block(CraftMagicNumbers.getBlock(o1)).getBlockData()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getCombinedId(new Block(CraftMagicNumbers.getBlock(o2)).getBlockData()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public Key getBlockKey(int combinedId) {
        Material material = CraftMagicNumbers.getMaterial(Block.getByCombinedId(combinedId).getBlock().getHandle());
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
    public String getMinecraftKey(org.bukkit.inventory.ItemStack itemStack) {
        return RegistryBlocks.getKey(RegistryBlocks.ITEM_REGISTRY,
                new ItemStack(CraftItemStack.asNMSCopy(itemStack)).getItem()).toString();
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
