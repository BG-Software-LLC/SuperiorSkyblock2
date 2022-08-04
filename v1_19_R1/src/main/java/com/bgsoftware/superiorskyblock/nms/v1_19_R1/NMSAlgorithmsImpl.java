package com.bgsoftware.superiorskyblock.nms.v1_19_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.algorithms.GlowEnchantmentFactory;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.RegistryBlocks;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.network.chat.ChatSerializer;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.item.ItemStack;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.menu.MenuTileEntityBrewing;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.menu.MenuTileEntityDispenser;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.menu.MenuTileEntityFurnace;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.menu.MenuTileEntityHopper;
import net.minecraft.world.IInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.EnumMap;
import java.util.function.BiFunction;

public final class NMSAlgorithmsImpl implements NMSAlgorithms {

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

    private static final String BUILT_AGAINST_MAPPING = "4cc0cc97cac491651bff3af8b124a214";

    private final SuperiorSkyblockPlugin plugin;

    public NMSAlgorithmsImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isMappingsSupported() {
        return ((CraftMagicNumbers) CraftMagicNumbers.INSTANCE).getMappingsVersion().equals(BUILT_AGAINST_MAPPING);
    }

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

    @Override
    public String parseSignLine(String original) {
        return ChatSerializer.toJson(CraftChatMessage.fromString(original)[0]);
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
        return KeyImpl.of(material, (byte) 0);
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        return KeyImpl.of(minecart.getDisplayBlockData().getMaterial(), (byte) 0);
    }

    @Override
    public Key getFallingBlockType(FallingBlock fallingBlock) {
        return KeyImpl.of(fallingBlock.getBlockData().getMaterial(), (byte) 0);
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
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        MenuCreator menuCreator = MENUS_HOLDER_CREATORS.get(inventoryType);
        return menuCreator == null ? null : menuCreator.apply(defaultHolder, title);
    }

    private interface MenuCreator extends BiFunction<InventoryHolder, String, IInventory> {

    }

}
