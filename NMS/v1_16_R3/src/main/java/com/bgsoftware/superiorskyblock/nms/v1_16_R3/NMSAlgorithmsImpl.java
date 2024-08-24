package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.io.ClassProcessor;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.algorithms.PaperGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.algorithms.SpigotGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.menu.MenuTileEntityBrewing;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.menu.MenuTileEntityDispenser;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.menu.MenuTileEntityFurnace;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.menu.MenuTileEntityHopper;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.world.KeyBlocksCache;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityFallingBlock;
import net.minecraft.server.v1_16_R3.EntityMinecartAbstract;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IInventory;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecart;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.EnumMap;
import java.util.function.BiFunction;

public class NMSAlgorithmsImpl implements NMSAlgorithms {

    private static final EnumMap<InventoryType, MenuCreator> MENUS_HOLDER_CREATORS = new EnumMap<>(InventoryType.class);

    static {
        MENUS_HOLDER_CREATORS.put(InventoryType.DISPENSER, MenuTileEntityDispenser::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.DROPPER, MenuTileEntityDispenser::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.FURNACE, MenuTileEntityFurnace::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.BREWING, MenuTileEntityBrewing::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.HOPPER, MenuTileEntityHopper::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.BLAST_FURNACE, MenuTileEntityFurnace::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.SMOKER, MenuTileEntityFurnace::new);
    }

    private final ClassProcessor CLASS_PROCESSOR = new ClassProcessor() {
        @Override
        public byte[] processClass(byte[] classBytes, String path) {
            return Bukkit.getUnsafe().processClass(plugin.getDescription(), path, classBytes);
        }
    };

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
        Block block = Block.getByCombinedId(combinedId).getBlock();
        return KeyBlocksCache.getBlockKey(block);
    }

    @Override
    public Key getMinecartBlock(org.bukkit.entity.Minecart bukkitMinecart) {
        EntityMinecartAbstract minecart = ((CraftMinecart) bukkitMinecart).getHandle();
        Block block = minecart.getDisplayBlock().getBlock();
        return KeyBlocksCache.getBlockKey(block);
    }

    @Override
    public Key getFallingBlockType(FallingBlock bukkitFallingBlock) {
        EntityFallingBlock fallingBlock = ((CraftFallingBlock) bukkitFallingBlock).getHandle();
        Block block = fallingBlock.getBlock().getBlock();
        return KeyBlocksCache.getBlockKey(block);
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
        try {
            return new PaperGlowEnchantment("superior_glowing_enchant");
        } catch (Throwable error) {
            return new SpigotGlowEnchantment("superior_glowing_enchant");
        }
    }

    @Override
    public int getMaxWorldSize() {
        return Bukkit.getMaxWorldSize();
    }

    @Override
    public double getCurrentTps() {
        return Bukkit.getTPS()[0];
    }

    @Override
    public int getDataVersion() {
        return CraftMagicNumbers.INSTANCE.getDataVersion();
    }

    @Override
    public ClassProcessor getClassProcessor() {
        return CLASS_PROCESSOR;
    }

    @Override
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        MenuCreator menuCreator = MENUS_HOLDER_CREATORS.get(inventoryType);
        return menuCreator == null ? null : menuCreator.apply(defaultHolder, title);
    }

    private interface MenuCreator extends BiFunction<InventoryHolder, String, IInventory> {

    }

}
