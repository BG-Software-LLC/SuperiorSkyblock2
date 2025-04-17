package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.io.ClassProcessor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Optional;

public interface NMSAlgorithms {

    void registerCommand(BukkitCommand command);

    String parseSignLine(String original);

    int getCombinedId(Location location);

    int getCombinedId(Material material, byte data);

    Optional<String> getTileEntityIdFromCombinedId(int combinedId);

    int compareMaterials(Material o1, Material o2);

    short getBlockDataValue(BlockState blockState);

    short getBlockDataValue(Block block);

    short getMaxBlockDataValue(Material material);

    Key getBlockKey(int combinedId);

    Key getMinecartBlock(Minecart minecart);

    Key getFallingBlockType(FallingBlock fallingBlock);

    void setCustomModel(ItemMeta itemMeta, int customModel);

    void addPotion(PotionMeta potionMeta, PotionEffect potionEffect);

    String getMinecraftKey(ItemStack itemStack);

    void makeItemGlow(ItemMeta itemMeta);

    @Nullable
    Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title);

    int getMaxWorldSize();

    double getCurrentTps();

    int getDataVersion();

    default ClassProcessor getClassProcessor() {
        return null;
    }

    default void handlePaperChatRenderer(Object event) {
        throw new UnsupportedOperationException();
    }

    default void hideAttributes(ItemMeta itemMeta) {

    }
}
