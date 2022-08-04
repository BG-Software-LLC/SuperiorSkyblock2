package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;

public interface NMSAlgorithms {

    boolean isMappingsSupported();

    void registerCommand(BukkitCommand command);

    String parseSignLine(String original);

    int getCombinedId(Location location);

    int getCombinedId(Material material, byte data);

    int compareMaterials(Material o1, Material o2);

    Key getBlockKey(int combinedId);

    Key getMinecartBlock(Minecart minecart);

    Key getFallingBlockType(FallingBlock fallingBlock);

    void setCustomModel(ItemMeta itemMeta, int customModel);

    void addPotion(PotionMeta potionMeta, PotionEffect potionEffect);

    String getMinecraftKey(ItemStack itemStack);

    Enchantment getGlowEnchant();

    @Nullable
    Object createMenuInventoryHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title);

}
