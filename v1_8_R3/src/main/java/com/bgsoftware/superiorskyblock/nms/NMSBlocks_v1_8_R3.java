package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.objects.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumSkyBlock;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChunkLoader;
import net.minecraft.server.v1_8_R3.IUpdatePlayerListBox;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockChange;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PlayerChunkMap;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.TileEntitySign;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class NMSBlocks_v1_8_R3 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();
    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");

    private static final ReflectMethod<Boolean> WORLD_REFRESH_LIGHT = new ReflectMethod<>(World.class, boolean.class, "c", EnumSkyBlock.class, BlockPosition.class, Chunk.class, List.class);

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        World world = ((CraftWorld) bukkitChunk.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(bukkitChunk.getX(), bukkitChunk.getZ());

        for(com.bgsoftware.superiorskyblock.utils.blocks.BlockData blockData : blockDataList)
            setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getClonedTileEntity());

        // Update lights for the blocks.
        for (com.bgsoftware.superiorskyblock.utils.blocks.BlockData blockData : blockDataList) {
            BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
            if(plugin.getSettings().lightsUpdate && blockData.getBlockLightLevel() > 0)
                world.a(EnumSkyBlock.BLOCK, blockPosition, blockData.getBlockLightLevel());

            byte skyLight = plugin.getSettings().lightsUpdate ? blockData.getSkyLightLevel() : 15;

            if(skyLight > 0 && blockData.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL)
                world.a(EnumSkyBlock.SKY, blockPosition, skyLight);
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        //noinspection deprecation
        int combinedId = material.getId() + (data << 12);
        setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, combinedId, null, null);

        sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    private void setBlock(Chunk chunk, BlockPosition blockPosition, int combinedId, CompoundTag statesTag, CompoundTag tileEntity) {
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(blockData.getBlock().getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) {
            chunk.world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        int highestBlockLight = chunk.b(blockX, blockZ);
        boolean initLight = false;

        int indexY = blockY >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null) {
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, !chunk.world.worldProvider.o());
            initLight = blockY > highestBlockLight;
        }

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData);

        if(initLight)
            chunk.initLighting();

        if(tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            assert tileEntityCompound != null;
            tileEntityCompound.setInt("x", blockPosition.getX());
            tileEntityCompound.setInt("y", blockPosition.getY());
            tileEntityCompound.setInt("z", blockPosition.getZ());
            chunk.world.getTileEntity(blockPosition).a(tileEntityCompound);
        }
    }

    @Override
    public byte[] getLightLevels(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Chunk chunk = ((CraftWorld) location.getWorld()).getHandle().getChunkAtWorldCoords(blockPosition);
        return new byte[] {
                (byte) chunk.getBrightness(EnumSkyBlock.SKY, blockPosition),
                (byte) chunk.getBrightness(EnumSkyBlock.BLOCK, blockPosition),
        };
    }

    @Override
    public void refreshLights(org.bukkit.World bukkitWorld, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        Set<ChunkCoordIntPair> chunksToUpdate = new HashSet<>();
        World world = ((CraftWorld) bukkitWorld).getHandle();

        blockDataList.forEach(blockData -> {
            BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
            ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
            if(blockData.getSkyLightLevel() > 0 && blockData.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                recalculateLighting(world, blockPosition, EnumSkyBlock.SKY);
                chunksToUpdate.add(chunkCoords);
            }
            if(blockData.getBlockLightLevel() > 0) {
                recalculateLighting(world, blockPosition, EnumSkyBlock.BLOCK);
                chunksToUpdate.add(chunkCoords);
            }
        });

        chunksToUpdate.forEach(chunkCoords -> refreshChunk(world.getChunkAt(chunkCoords.x, chunkCoords.z).bukkitChunk));
    }

    private void recalculateLighting(World world, BlockPosition blockPosition, EnumSkyBlock enumSkyBlock){
        Chunk chunk = world.getChunkAtWorldCoords(blockPosition);
        List<Chunk> nearbyChunks = new ArrayList<>();

        if(WORLD_REFRESH_LIGHT.isValid()){
            addChunk(nearbyChunks, world, chunk.locX - 1, chunk.locZ - 1);
            addChunk(nearbyChunks, world, chunk.locX + 1, chunk.locZ - 1);
            addChunk(nearbyChunks, world, chunk.locX - 1, chunk.locZ + 1);
            addChunk(nearbyChunks, world, chunk.locX + 1, chunk.locZ + 1);

            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.south(), chunk, nearbyChunks);
            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.north(), chunk, nearbyChunks);
            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.up(), chunk, nearbyChunks);
            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.down(), chunk, nearbyChunks);
            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.east(), chunk, nearbyChunks);
            WORLD_REFRESH_LIGHT.invoke(world, enumSkyBlock, blockPosition.west(), chunk, nearbyChunks);
        }
        else{
            world.c(enumSkyBlock, blockPosition.south());
            world.c(enumSkyBlock, blockPosition.north());
            world.c(enumSkyBlock, blockPosition.up());
            world.c(enumSkyBlock, blockPosition.down());
            world.c(enumSkyBlock, blockPosition.east());
            world.c(enumSkyBlock, blockPosition.west());
        }
    }

    private void addChunk(List<Chunk> chunks, World world, int chunkX, int chunkZ){
        Chunk chunk = world.getChunkIfLoaded(chunkX, chunkZ);
        if(chunk != null)
            chunks.add(chunk);
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if(tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = new NBTTagCompound();
        tileEntity.b(tileEntityCompound);

        tileEntityCompound.remove("x");
        tileEntityCompound.remove("y");
        tileEntityCompound.remove("z");

        return CompoundTag.fromNBT(tileEntityCompound);
    }

    @Override
    public String parseSignLine(String original) {
        return IChatBaseComponent.ChatSerializer.a(CraftChatMessage.fromString(original)[0]);
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        sendPacketToRelevantPlayers((WorldServer) chunk.world, chunk.locX, chunk.locZ,
                new PacketPlayOutMapChunk(chunk, false, 65535));
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData =  world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().chunkProviderServer
                .getChunkIfLoaded(chunkPosition.getX(), chunkPosition.getZ());
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public CompletableFuture<CalculatedChunk> calculateChunk(ChunkPosition chunkPosition) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());

        CompletableFuture<CalculatedChunk> completableFuture = new CompletableFuture<>();

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, false, chunk -> {
            KeyMap<Integer> blockCounts = new KeyMap<>();
            Set<Location> spawnersLocations = new HashSet<>();

            for(ChunkSection chunkSection : chunk.getSections()){
                if(chunkSection != null){
                    for (BlockPosition bp : BlockPosition.b(new BlockPosition(0, 0, 0), new BlockPosition(15, 15, 15))) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(chunkPosition.getWorld(), (chunkCoords.x << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkCoords.z << 4) + bp.getZ());
                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            short data = (short) blockData.getBlock().toLegacyData(blockData);
                            Key blockKey = Key.of(type, data, location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (type == Material.MOB_SPAWNER) {
                                spawnersLocations.add(location);
                            }
                        }
                    }
                }
            }

            completableFuture.complete(new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations));
        });

        return completableFuture;
    }

    @Override
    public void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), null);

            for(int i = 0; i < chunk.entitySlices.length; i++) {
                chunk.entitySlices[i].forEach(entity -> {
                    if(!(entity instanceof EntityHuman))
                        entity.dead = true;
                });
                chunk.entitySlices[i] = new UnsafeList<>();
            }

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::t);
            chunk.tileEntities.clear();

            if(world.generator != null && !(world.generator instanceof WorldGenerator)){
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, 0L, world.generator);
                Chunk generatedChunk = customChunkGenerator.getOrCreateChunk(chunkCoords.x, chunkCoords.z);

                for (int i = 0; i < 16; i++)
                    chunk.getSections()[i] = generatedChunk.getSections()[i];

                for (Map.Entry<BlockPosition, TileEntity> entry : generatedChunk.getTileEntities().entrySet())
                    world.setTileEntity(entry.getKey(), entry.getValue());
            }

            ChunksTracker.markEmpty(island, chunkPosition, false);
        }, chunk -> refreshChunk(chunk.bukkitChunk));
    }

    @Override
    public void setChunkBiome(ChunkPosition chunkPosition, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, chunk -> {
            byte biomeBase = (byte) CraftBlock.biomeToBiomeBase(biome).id;
            Arrays.fill(chunk.getBiomeIndex(), biomeBase);
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer){
        runActionOnChunk(bukkitWorld, chunkCoords, saveChunk, null, chunkConsumer, null);
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Runnable onFinish, Consumer<Chunk> chunkConsumer, Consumer<Chunk> updateChunk){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(bukkitWorld.getUID(), uuid -> CHUNK_LOADER.get(world.chunkProviderServer));

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(updateChunk != null)
                updateChunk.accept(chunk);
            if(onFinish != null)
                onFinish.run();
        }

        else try {
            Chunk loadedChunk = chunkLoader.a(world, chunkCoords.x, chunkCoords.z);

            if(loadedChunk != null)
                chunkConsumer.accept(loadedChunk);

            if(loadedChunk != null) {
                if (saveChunk) {
                    try {
                        chunkLoader.a(world, loadedChunk);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            }

            if(onFinish != null)
                onFinish.run();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if(stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.tickingChunks
                    .remove(ChunkCoordIntPair.a(chunk.getX(), chunk.getZ()));
            if(cropsTickingTileEntity != null)
                cropsTickingTileEntity.getWorld().tileEntityList.remove(cropsTickingTileEntity);
        }
        else
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
    }

    @Override
    public void handleSignPlace(Island island, Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        TileEntity tileEntity = worldServer.getTileEntity(blockPosition);
        if(tileEntity instanceof TileEntitySign) {
            TileEntitySign tileEntitySign = (TileEntitySign) tileEntity;
            String[] lines = new String[4];
            System.arraycopy(CraftSign.revertComponents(tileEntitySign.lines), 0, lines, 0, lines.length);
            String[] strippedLines = new String[4];
            for (int i = 0; i < 4; i++)
                strippedLines[i] = StringUtils.stripColors(lines[i]);

            IChatBaseComponent[] newLines;

            if (BlocksListener.IMP.onSignPlace(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
        }
    }

    private void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet){
        PlayerChunkMap playerChunkMap = worldServer.getPlayerChunkMap();
        for(EntityHuman entityHuman : worldServer.players){
            if(entityHuman instanceof EntityPlayer && playerChunkMap.a((EntityPlayer) entityHuman, chunkX, chunkZ))
                ((EntityPlayer) entityHuman).playerConnection.sendPacket(packet);
        }
    }

    private static final class CropsTickingTileEntity extends TileEntity implements IUpdatePlayerListBox {

        private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
        private static int random = ThreadLocalRandom.current().nextInt();

        private final WeakReference<Island> island;
        private final WeakReference<Chunk> chunk;
        private final int chunkX, chunkZ;

        private int currentTick = 0;

        private CropsTickingTileEntity(Island island, Chunk chunk){
            this.island = new WeakReference<>(island);
            this.chunk = new WeakReference<>(chunk);
            this.chunkX = chunk.locX;
            this.chunkZ = chunk.locZ;
            a(chunk.getWorld());
            a(new BlockPosition(chunkX << 4, 1, chunkZ << 4));
            world.tileEntityList.add(this);
        }

        @Override
        public void c() {
            if(++currentTick <= plugin.getSettings().cropsInterval)
                return;

            Chunk chunk = this.chunk.get();
            Island island = this.island.get();

            if(chunk == null || island == null){
                world.tileEntityList.remove(this);
                return;
            }

            currentTick = 0;

            int worldRandomTick = world.getGameRules().c("randomTickSpeed");
            double cropGrowth = island.getCropGrowthMultiplier() - 1;

            int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().cropsInterval);

            if (chunkRandomTickSpeed > 0) {
                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != null && chunkSection.shouldTick()) {
                        for (int i = 0; i < chunkRandomTickSpeed; i++) {
                            random = random * 3 + 1013904223;
                            int factor = random >> 2;
                            int x = factor & 15;
                            int z = factor >> 8 & 15;
                            int y = factor >> 16 & 15;
                            IBlockData blockData = chunkSection.getType(x, y, z);
                            Block block = blockData.getBlock();
                            if (block.isTicking() && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(block).name())) {
                                block.a(world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)),
                                        blockData, ThreadLocalRandom.current());
                            }
                        }
                    }
                }
            }

        }

        static void create(Island island, Chunk chunk){
            long chunkKey = ChunkCoordIntPair.a(chunk.locX, chunk.locZ);
            if(!tickingChunks.containsKey(chunkKey)){
                tickingChunks.put(chunkKey, new CropsTickingTileEntity(island, chunk));
            }
        }

    }

}
