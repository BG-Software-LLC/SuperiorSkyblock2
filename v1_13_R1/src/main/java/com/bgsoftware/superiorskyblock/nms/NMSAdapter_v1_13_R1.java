package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.DragonEggBreakEvent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.bgsoftware.superiorskyblock.api.key.Key;
import net.minecraft.server.v1_13_R1.BiomeBase;
import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.ChatMessage;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.EntityFallingBlock;
import net.minecraft.server.v1_13_R1.EntityItem;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.IMaterial;
import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_13_R1.Particles;
import net.minecraft.server.v1_13_R1.PlayerInteractManager;
import net.minecraft.server.v1_13_R1.SoundCategory;
import net.minecraft.server.v1_13_R1.SoundEffects;
import net.minecraft.server.v1_13_R1.TileEntityHopper;
import net.minecraft.server.v1_13_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_13_R1.World;

import net.minecraft.server.v1_13_R1.WorldBorder;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.v1_13_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_13_R1 implements NMSAdapter {

    private SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void registerCommand(BukkitCommand command) {
        ((CraftServer) plugin.getServer()).getCommandMap().register("superiorskyblock2", command);
    }

    @Override
    public Key getBlockKey(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
        IBlockData blockData = ((CraftBlockData) chunkSnapshot.getBlockData(x, y, z)).getState();
        Material type = chunkSnapshot.getBlockType(x, y, z);
        short data = (short) (Block.getCombinedId(blockData) >> 12 & 15);
        return Key.of(type, data);
    }

    @Override
    public int getSpawnerDelay(CreatureSpawner creatureSpawner) {
        Location location = creatureSpawner.getLocation();
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld())
                .getTileEntityAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return mobSpawner.getSpawner().spawnDelay;
    }

    @Override
    public void setWorldBorder(SuperiorPlayer superiorPlayer, Island island) {
        try {
            if(!plugin.getSettings().worldBordersEnabled)
                return;

            boolean disabled = !superiorPlayer.hasWorldBorderEnabled();

            WorldBorder worldBorder = new WorldBorder();

            worldBorder.world = ((CraftWorld) superiorPlayer.getWorld()).getHandle();
            worldBorder.setSize(disabled || island == null || (!plugin.getSettings().spawnWorldBorder && island.isSpawn()) ? Integer.MAX_VALUE : (island.getIslandSize() * 2) + 1);

            org.bukkit.World.Environment environment = superiorPlayer.getWorld().getEnvironment();

            Location center = island == null ? superiorPlayer.getLocation() : island.getCenter(environment);
            worldBorder.setCenter(center.getX(), center.getZ());

            switch (superiorPlayer.getBorderColor()){
                case GREEN:
                    worldBorder.transitionSizeBetween(worldBorder.getSize() - 0.1D, worldBorder.getSize(), Long.MAX_VALUE);
                    break;
                case RED:
                    worldBorder.transitionSizeBetween(worldBorder.getSize(), worldBorder.getSize() - 1.0D, Long.MAX_VALUE);
                    break;
            }

            PacketPlayOutWorldBorder packetPlayOutWorldBorder = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
            ((CraftPlayer) superiorPlayer.asPlayer()).getHandle().playerConnection.sendPacket(packetPlayOutWorldBorder);
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        EntityPlayer entityPlayer = ((CraftPlayer) superiorPlayer.asPlayer()).getHandle();
        Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
        optional.ifPresent(property -> superiorPlayer.setTextureValue(property.getValue()));
    }

    @Override
    public Object getCustomHolder(InventoryType inventoryType, InventoryHolder defaultHolder, String title) {
        return new CustomTileEntityHopper(defaultHolder, title);
    }

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if(offlinePlayer.isOnline() || offlinePlayer instanceof Player){
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);
        entity.world = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        entity.setPositionRotation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        targetPlayer.saveData();
    }

    @Override
    public void playGeneratorSound(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        double x = location.getX(), y = location.getY(), z = location.getZ();
        BlockPosition blockPosition = new BlockPosition(x, y, z);
        world.a(null, blockPosition, SoundEffects.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

        for(int i = 0; i < 8; i++)
            world.addParticle(Particles.F, x + Math.random(), y + 1.2D, z + Math.random(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void setBiome(org.bukkit.Chunk bukkitChunk, Biome biome) {
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        Arrays.fill(chunk.getBiomeIndex(), biomeBase);
        chunk.markDirty();
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public Enchantment getGlowEnchant() {
        return new Enchantment(NamespacedKey.minecraft("superior_glowing_enchant")) {
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

            @Override
            public boolean isTreasure() {
                return false;
            }

            @Override
            public boolean isCursed() {
                return false;
            }
        };
    }

    @Override
    public ItemStack[] getEquipment(EntityEquipment entityEquipment) {
        ItemStack[] itemStacks = new ItemStack[7];

        itemStacks[0] = new ItemStack(Material.ARMOR_STAND);
        itemStacks[1] = entityEquipment.getItemInMainHand();
        itemStacks[2] = entityEquipment.getItemInOffHand();
        itemStacks[3] = entityEquipment.getHelmet();
        itemStacks[4] = entityEquipment.getChestplate();
        itemStacks[5] = entityEquipment.getLeggings();
        itemStacks[6] = entityEquipment.getBoots();

        return itemStacks;
    }

    @Override
    public void spawnDragonEgg(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        world.addEntity(new CustomEntityFallingBlock(location, Material.DRAGON_EGG));
    }

    private static class CustomEntityFallingBlock extends EntityFallingBlock {

        public CustomEntityFallingBlock(Location location, Material material){
            super(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), CraftMagicNumbers.getBlock(material, (byte) 0));
        }

        @Nullable
        @Override
        public EntityItem a(IMaterial imaterial) {
            this.locY += 1;

            DragonEggBreakEvent dragonEggBreakEvent = new DragonEggBreakEvent((FallingBlock) getBukkitEntity());
            Bukkit.getPluginManager().callEvent(dragonEggBreakEvent);

            if(dragonEggBreakEvent.isCancelled()){
                world.setTypeAndData(new BlockPosition(locX, locY, locZ), getBlock(), 3);
                return null;
            }

            return super.a(imaterial);
        }

    }

    private static class CustomTileEntityHopper extends TileEntityHopper{

        private InventoryHolder holder;

        CustomTileEntityHopper(InventoryHolder holder, String title){
            this.holder = holder;
            this.setCustomName(new ChatMessage(title));
        }

        @Override
        public InventoryHolder getOwner() {
            return holder;
        }
    }

}
