package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.mojang.authlib.properties.Property;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public interface NMSAdapter {

    void registerCommand(BukkitCommand command);

    Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z);

    int getSpawnerDelay(CreatureSpawner creatureSpawner);

    void setSpawnerDelay(CreatureSpawner creatureSpawner, int spawnDelay);

    void setWorldBorder(SuperiorPlayer superiorPlayer, Island island);

    void setSkinTexture(SuperiorPlayer superiorPlayer);

    default void setSkinTexture(SuperiorPlayer superiorPlayer, Property property){
        superiorPlayer.setTextureValue(property.getValue());
    }

    default Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title){
        return defaultHolder;
    }

    void clearInventory(OfflinePlayer offlinePlayer);

    void playGeneratorSound(Location location);

    void playBreakAnimation(Block block);

    void playPlaceSound(Location location);

    default void setBiome(ChunkGenerator.BiomeGrid biomeGrid, Biome biome){
        for(int x = 0; x < 16; x++){
            for(int z = 0; z < 16; z++){
                biomeGrid.setBiome(x, z, biome);
            }
        }
    }

    default Object getBlockData(Block block){
        return null;
    }

    Enchantment getGlowEnchant();

    default void injectChunkSections(Chunk chunk){

    }

    boolean isChunkEmpty(Chunk chunk);

    ItemStack[] getEquipment(EntityEquipment entityEquipment);

    double[] getTPS();

    default void addPotion(PotionMeta potionMeta, PotionEffect potionEffect){
        potionMeta.addCustomEffect(potionEffect, true);
    }

    String getMinecraftKey(ItemStack itemStack);

    CompoundTag getNMSCompound(ItemStack itemStack);

    boolean isAnimalFood(ItemStack itemStack, Animals animals);

    void sendActionBar(Player player, String message);

    void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut);

    default void setCustomModel(ItemMeta itemMeta, int customModel){

    }

    int getPortalTicks(Entity entity);

}
