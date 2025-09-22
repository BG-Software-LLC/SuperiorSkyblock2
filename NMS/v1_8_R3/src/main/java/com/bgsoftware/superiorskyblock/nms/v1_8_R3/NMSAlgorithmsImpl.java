package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.world.BlockEntityCache;
import com.bgsoftware.superiorskyblock.nms.v1_8_R3.world.KeyBlocksCache;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityFallingBlock;
import net.minecraft.server.v1_8_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MinecraftKey;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftMinecart;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.Optional;

public class NMSAlgorithmsImpl implements NMSAlgorithms {

    private static final Enchantment GLOW_ENCHANT = initializeGlowEnchantment();

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
        IBlockData blockData;
        try (ObjectsPools.Wrapper<BlockPosition.MutableBlockPosition> wrapper = NMSUtils.BLOCK_POS_POOL.obtain()) {
            wrapper.getHandle().c(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            blockData = world.getType(wrapper.getHandle());
        }
        return Block.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        //noinspection deprecation
        return material.getId() + (data << 12);
    }

    @Override
    public Optional<String> getTileEntityIdFromCombinedId(int combinedId) {
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if (!blockData.getBlock().isTileEntity())
            return Optional.empty();

        String id = BlockEntityCache.getTileEntityId(blockData);

        return Text.isBlank(id) ? Optional.empty() : Optional.of(id);
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        return Integer.compare(o1.ordinal(), o2.ordinal());
    }

    @Override
    public short getBlockDataValue(BlockState blockState) {
        return blockState.getRawData();
    }

    @Override
    public short getBlockDataValue(org.bukkit.block.Block block) {
        return block.getData();
    }

    @Override
    public short getMaxBlockDataValue(Material material) {
        return 0;
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
        EntityFallingBlock fallingBlock = ((CraftFallingSand) bukkitFallingBlock).getHandle();
        return Optional.ofNullable(fallingBlock.getBlock()).map(KeyBlocksCache::getBlockKey).orElse(ConstantKeys.AIR);
    }

    @Override
    public void setCustomModel(ItemMeta itemMeta, int customModel) {
        // Doesn't exist
    }

    @Override
    public void setItemModel(ItemMeta itemMeta, String itemModel) {
        // Doesn't exist
    }

    @Override
    public void setRarity(ItemMeta itemMeta, String rarity) {
        // Doesn't exist
    }

    @Override
    public void setTrim(ItemMeta itemMeta, String trimMaterial, String trimPattern) {
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
    public void makeItemGlow(ItemMeta itemMeta) {
        itemMeta.addEnchant(GLOW_ENCHANT, 1, true);
    }

    @Override
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return defaultHolder;
    }

    @Override
    public int getMaxWorldSize() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return server.getPropertyManager().getInt("max-world-size", 29999984);
    }

    @Override
    public double getCurrentTps() {
        return MinecraftServer.getServer().recentTps[0];
    }

    @Override
    public int getDataVersion() {
        return -1;
    }

    private static Enchantment initializeGlowEnchantment() {
        int enchantId = 100;
        while (Enchantment.getById(enchantId) != null)
            ++enchantId;

        Enchantment glowEnchant = new Enchantment(enchantId) {
            @Override
            public String getName() {
                return "SuperiorSkyblockGlow";
            }

            @Override
            public int getMaxLevel() {
                return 1;
            }

            @Override
            public int getStartLevel() {
                return 0;
            }

            @Override
            public EnchantmentTarget getItemTarget() {
                return null;
            }

            @Override
            public boolean conflictsWith(Enchantment enchantment) {
                return false;
            }

            @Override
            public boolean canEnchantItem(org.bukkit.inventory.ItemStack itemStack) {
                return true;
            }
        };

        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
            field.setAccessible(false);
        } catch (Exception ignored) {
        }

        try {
            Enchantment.registerEnchantment(glowEnchant);
        } catch (Exception ignored) {
        }

        return glowEnchant;
    }

}
