package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.reflections.Fields;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_15_R1.BiomeBase;
import net.minecraft.server.v1_15_R1.BiomeStorage;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkSection;
import net.minecraft.server.v1_15_R1.ChunkStatus;
import net.minecraft.server.v1_15_R1.DimensionManager;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.HeightMap;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.TileEntityHopper;
import net.minecraft.server.v1_15_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldBorder;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.shorts.ShortList;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.util.UnsafeList;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "ConstantConditions"})
public final class NMSAdapter_v1_15_R1 implements NMSAdapter {

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
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)((CraftWorld) location.getWorld()).getHandle().getTileEntity(blockPosition);
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
        WorldServer worldServer = server.getWorldServer(DimensionManager.OVERWORLD);
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
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        world.triggerEffect(1501, blockPosition, 0);
    }

    @Override
    public void setBiome(org.bukkit.Chunk bukkitChunk, Biome biome) {
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        BiomeBase[] biomeBases = null;

        try{
            Field field = BiomeStorage.class.getDeclaredField("f");
            if(!field.getType().equals(BiomeBase[].class)){
                field = BiomeStorage.class.getDeclaredField("g");
            }
            field.setAccessible(true);
            biomeBases = (BiomeBase[]) field.get(chunk.getBiomeIndex());
        }catch(Exception ex){
            ex.printStackTrace();
        }

        if(biomeBases == null)
            throw new RuntimeException("Error while receiving biome bases of chunk (" + bukkitChunk.getX() + "," + bukkitChunk.getZ() + ").");

        Arrays.fill(biomeBases, biomeBase);
        chunk.markDirty();
    }

    @Override
    public Object getBlockData(org.bukkit.block.Block block) {
        return block.getBlockData();
    }

    @Override
    public Enchantment getGlowEnchant() {
        //noinspection NullableProblems
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

    @SuppressWarnings("rawtypes")
    @Override
    public void regenerateChunks(List<org.bukkit.Chunk> bukkitChunksList) {
        List<Chunk> chunksList = bukkitChunksList.stream()
                .map(bukkitChunk -> ((CraftWorld) bukkitChunk.getWorld()).getHandle().getChunkAt(bukkitChunk.getX(), bukkitChunk.getZ()))
                .collect(Collectors.toList());

        for(Chunk chunk : chunksList){
            Map<HeightMap.Type, HeightMap> heightMap = new HashMap<>();
            List[] entitySlices = new List[16];

            Fields.CHUNK_SECTIONS.set(chunk, new ChunkSection[16]);
            Fields.CHUNK_PENDING_BLOCK_ENTITIES.set(chunk, new HashMap<>());
            Fields.CHUNK_HEIGHT_MAP.set(chunk, heightMap);
            Fields.CHUNK_TILE_ENTITIES.set(chunk, new HashMap<>());
            Fields.CHUNK_STRUCTURE_STARTS.set(chunk, new HashMap<>());
            Fields.CHUNK_STRUCTURE_REFENCES.set(chunk, new HashMap<>());
            Fields.CHUNK_POST_PROCESSING.set(chunk, new ShortList[16]);
            Fields.CHUNK_ENTITY_SLICES.set(chunk, entitySlices);

            HeightMap.Type[] heightMapTypes = HeightMap.Type.values();

            for (HeightMap.Type heightMapType : heightMapTypes) {
                if (ChunkStatus.FULL.h().contains(heightMapType)) {
                    heightMap.put(heightMapType, new HeightMap(chunk, heightMapType));
                }
            }

            for(int i = 0; i < entitySlices.length; i++)
                entitySlices[i] = new UnsafeList();
        }
    }

    private static class CustomTileEntityHopper extends TileEntityHopper {

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
