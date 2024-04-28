package com.bgsoftware.superiorskyblock.nms.v1_20_4;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.nms.NMSAlgorithms;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms.PaperGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.algorithms.SpigotGlowEnchantment;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.menu.MenuBrewingStandBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.menu.MenuDispenserBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.menu.MenuFurnaceBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.menu.MenuHopperBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_20_4.world.KeyBlocksCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NMSAlgorithmsImpl implements NMSAlgorithms {

    private static final ReflectField<Map<NamespacedKey, Enchantment>> REGISTRY_CACHE =
            new ReflectField<>(CraftRegistry.class, Map.class, "cache");

    private static final EnumMap<InventoryType, MenuCreator> MENUS_HOLDER_CREATORS = new EnumMap<>(InventoryType.class);

    static {
        MENUS_HOLDER_CREATORS.put(InventoryType.DISPENSER, MenuDispenserBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.DROPPER, MenuDispenserBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.FURNACE, MenuFurnaceBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.BREWING, MenuBrewingStandBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.HOPPER, MenuHopperBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.BLAST_FURNACE, MenuFurnaceBlockEntity::new);
        MENUS_HOLDER_CREATORS.put(InventoryType.SMOKER, MenuFurnaceBlockEntity::new);
    }

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
        return Component.Serializer.toJson(CraftChatMessage.fromString(original)[0], MinecraftServer.getServer().registryAccess());
    }

    @Override
    public int getCombinedId(Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            return 0;

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockState blockState = serverLevel.getBlockState(blockPos);
        return Block.getId(blockState);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        BlockState blockState;

        if (data == 0) {
            Block block = CraftMagicNumbers.getBlock(material);
            if (block == null)
                return -1;
            blockState = block.defaultBlockState();
        } else {
            blockState = CraftMagicNumbers.getBlock(material, data);
        }

        return blockState == null ? -1 : Block.getId(blockState);
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        int firstMaterial = o1.isBlock() ? Block.getId(CraftMagicNumbers.getBlock(o1).defaultBlockState()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getId(CraftMagicNumbers.getBlock(o2).defaultBlockState()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public Key getBlockKey(int combinedId) {
        Block block = Block.stateById(combinedId).getBlock();
        return KeyBlocksCache.getBlockKey(block);
    }

    @Override
    public Key getMinecartBlock(org.bukkit.entity.Minecart bukkitMinecart) {
        AbstractMinecart minecart = ((CraftMinecart) bukkitMinecart).getHandle();
        Block block = minecart.getDisplayBlockState().getBlock();
        return KeyBlocksCache.getBlockKey(block);
    }

    @Override
    public Key getFallingBlockType(FallingBlock bukkitFallingBlock) {
        FallingBlockEntity fallingBlock = ((CraftFallingBlock) bukkitFallingBlock).getHandle();
        Block block = fallingBlock.getBlockState().getBlock();
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
        return BuiltInRegistries.ITEM.getKey(CraftItemStack.asNMSCopy(itemStack).getItem()).toString();
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
    public Enchantment createGlowEnchantment() {
        Enchantment enchantment = getGlowEnchant();

        Map<NamespacedKey, Enchantment> registryCache = REGISTRY_CACHE.get(Registry.ENCHANTMENT);

        registryCache.put(enchantment.getKey(), enchantment);

        return enchantment;
    }

    @Nullable
    @Override
    public Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        MenuCreator menuCreator = MENUS_HOLDER_CREATORS.get(inventoryType);
        return menuCreator == null ? null : menuCreator.apply(defaultHolder, title);
    }

    @Override
    public int getMaxWorldSize() {
        return Bukkit.getMaxWorldSize();
    }

    @Override
    public double getCurrentTps() {
        return MinecraftServer.getServer().tps1.getAverage();
    }

    private interface MenuCreator extends BiFunction<InventoryHolder, String, Container> {
    }

}
