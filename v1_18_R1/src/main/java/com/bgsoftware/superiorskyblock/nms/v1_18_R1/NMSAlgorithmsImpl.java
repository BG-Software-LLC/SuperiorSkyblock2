package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.algorithms.CustomTileEntityHopper;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.algorithms.GlowEnchantmentFactory;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

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
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return 0;

        World world = ((CraftWorld) bukkitWorld).getHandle();
        IBlockData blockData = getType(world, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return NMSMappings.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        return NMSMappings.getCombinedId(data == 0 ? getBlockData(CraftMagicNumbers.getBlock(material)) :
                CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? NMSMappings.getCombinedId(getBlockData(CraftMagicNumbers.getBlock(o1))) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? NMSMappings.getCombinedId(getBlockData(CraftMagicNumbers.getBlock(o2))) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public Key getBlockKey(int combinedId) {
        Material material = CraftMagicNumbers.getMaterial(getBlock(getByCombinedId(combinedId)));
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
        return getKey(IRegistry.aa, getItem(CraftItemStack.asNMSCopy(itemStack))).toString();
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
