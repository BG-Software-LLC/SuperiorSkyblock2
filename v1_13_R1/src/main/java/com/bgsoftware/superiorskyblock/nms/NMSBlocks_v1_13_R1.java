package com.bgsoftware.superiorskyblock.nms;

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
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectField;
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectMethod;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntArrayTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Maps;
import net.minecraft.server.v1_13_R1.AxisAlignedBB;
import net.minecraft.server.v1_13_R1.BiomeBase;
import net.minecraft.server.v1_13_R1.Block;
import net.minecraft.server.v1_13_R1.BlockBed;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.BlockProperties;
import net.minecraft.server.v1_13_R1.BlockStateBoolean;
import net.minecraft.server.v1_13_R1.BlockStateEnum;
import net.minecraft.server.v1_13_R1.BlockStateInteger;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.Chunk;
import net.minecraft.server.v1_13_R1.ChunkConverter;
import net.minecraft.server.v1_13_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R1.ChunkProviderServer;
import net.minecraft.server.v1_13_R1.ChunkSection;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumSkyBlock;
import net.minecraft.server.v1_13_R1.HeightMap;
import net.minecraft.server.v1_13_R1.IBlockData;
import net.minecraft.server.v1_13_R1.IBlockState;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.IChunkAccess;
import net.minecraft.server.v1_13_R1.IChunkLoader;
import net.minecraft.server.v1_13_R1.ITickable;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_13_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_13_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import net.minecraft.server.v1_13_R1.ProtoChunk;
import net.minecraft.server.v1_13_R1.ProtoChunkExtension;
import net.minecraft.server.v1_13_R1.TileEntity;
import net.minecraft.server.v1_13_R1.TileEntitySign;
import net.minecraft.server.v1_13_R1.TileEntityTypes;
import net.minecraft.server.v1_13_R1.World;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_13_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_13_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_13_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_13_R1.util.UnsafeList;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
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

@SuppressWarnings({"unused", "ConstantConditions", "rawtypes"})
public final class NMSBlocks_v1_13_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, IChunkLoader> chunkLoadersMap = Maps.newHashMap();
    private static final Map<String, IBlockState> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState, String> blockStateToName = new HashMap<>();
    private static final ReflectField<Boolean> RANDOM_TICK = new ReflectField<>(Block.class, Boolean.class, "randomTick");
    private static final ReflectField<IChunkLoader> CHUNK_LOADER = new ReflectField<>(ChunkProviderServer.class, IChunkLoader.class, "chunkLoader");
    private static final ReflectMethod<Void> SET_CURRENT_CHUNK = new ReflectMethod<>(TileEntity.class, "setCurrentChunk", Chunk.class);

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("z", "axis-empty");
        fieldNameToName.put("H", "facing-notup");
        fieldNameToName.put("I", "facing-horizontal");
        fieldNameToName.put("K", "redstone-east");
        fieldNameToName.put("L", "redstone-north");
        fieldNameToName.put("M", "redstone-south");
        fieldNameToName.put("N", "redstone-west");
        fieldNameToName.put("O", "double-half");
        fieldNameToName.put("Q", "track-shape-empty");
        fieldNameToName.put("R", "track-shape");
        fieldNameToName.put("S", "age2");
        fieldNameToName.put("T", "age3");
        fieldNameToName.put("U", "age5");
        fieldNameToName.put("V", "age7");
        fieldNameToName.put("W", "age15");
        fieldNameToName.put("X", "age25");
        fieldNameToName.put("ae", "level3");
        fieldNameToName.put("af", "level1-8");
        fieldNameToName.put("ag", "level15");
        fieldNameToName.put("aa", "distance1-7");
        fieldNameToName.put("ao", "chest-type");
        fieldNameToName.put("ap", "comparator-mode");
        fieldNameToName.put("as", "piston-type");
        fieldNameToName.put("at", "slab-type");

        try{
            for(Field field : BlockProperties.class.getFields()){
                Object value = field.get(null);
                if(value instanceof IBlockState) {
                    register(fieldNameToName.getOrDefault(field.getName(), ((IBlockState) value).a()),
                            field.getName(), (IBlockState) value);
                }
            }
        }catch (Exception ignored){}
    }

    private static void register(String key, String fieldName, IBlockState<?> blockState){
        if(nameToBlockState.containsKey(key)){
            SuperiorSkyblockPlugin.log("&cWarning: block state " + key + "(" + fieldName + ") already exists. Contact Ome_R!");
        }
        else {
            nameToBlockState.put(key, blockState);
            blockStateToName.put(blockState, key);
        }
    }

    @Override
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        World world = ((CraftWorld) bukkitChunk.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(bukkitChunk.getX(), bukkitChunk.getZ());

        for(com.bgsoftware.superiorskyblock.utils.blocks.BlockData blockData : blockDataList)
            setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getClonedTileEntity());

        if(plugin.getSettings().lightsUpdate) {
            // Update lights for the blocks.
            for (com.bgsoftware.superiorskyblock.utils.blocks.BlockData blockData : blockDataList) {
                BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                if(blockData.getBlockLightLevel() > 0)
                    world.a(EnumSkyBlock.BLOCK, blockPosition, blockData.getBlockLightLevel());
                if(blockData.getSkyLightLevel() > 0 && blockData.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL)
                    world.a(EnumSkyBlock.SKY, blockPosition, blockData.getSkyLightLevel());
            }
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, getCombinedId(material, data), null, null);

        AxisAlignedBB bb = new AxisAlignedBB(blockPosition.getX() - 60, 0, blockPosition.getZ() - 60,
                blockPosition.getX() + 60, 256, blockPosition.getZ() + 60);

        PacketPlayOutBlockChange packetPlayOutBlockChange = new PacketPlayOutBlockChange(world, blockPosition);

        for(Entity entity : world.getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutBlockChange);
        }
    }

    @SuppressWarnings("unchecked")
    private void setBlock(Chunk chunk, BlockPosition blockPosition, int combinedId, CompoundTag statesTag, CompoundTag tileEntity) {
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(statesTag != null){
            for(Map.Entry<String, Tag<?>> entry : statesTag.getValue().entrySet()){
                try {
                    IBlockState blockState = nameToBlockState.get(entry.getKey());
                    if(blockState != null) {
                        if (entry.getValue() instanceof ByteTag) {
                            blockData = blockData.set(blockState, ((ByteTag) entry.getValue()).getValue() == 1);
                        } else if (entry.getValue() instanceof IntArrayTag) {
                            int[] data = ((IntArrayTag) entry.getValue()).getValue();
                            blockData = blockData.set(blockState, data[0]);
                        } else if (entry.getValue() instanceof StringTag) {
                            String data = ((StringTag) entry.getValue()).getValue();
                            blockData = blockData.set(blockState, Enum.valueOf(blockState.b(), data));
                        }
                    }
                }catch (Exception ignored){}
            }
        }

        if((blockData.getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) || blockData.getBlock() instanceof BlockBed) {
            chunk.world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int blockX = blockPosition.getX() & 15;
        int blockY = blockPosition.getY();
        int blockZ = blockPosition.getZ() & 15;

        int highestBlockLight = chunk.heightMap.get(HeightMap.Type.LIGHT_BLOCKING).a(blockX, blockZ);
        boolean initLight = false;

        int indexY = blockY >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null) {
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, chunk.world.worldProvider.g());
            initLight = blockY > highestBlockLight;
        }

        chunkSection.setType(blockX, blockY & 15, blockZ, blockData);

        chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.OCEAN_FLOOR).a(blockX, blockY, blockZ, blockData);
        chunk.heightMap.get(HeightMap.Type.WORLD_SURFACE).a(blockX, blockY, blockZ, blockData);

        if(initLight)
            chunk.initLighting();

        if(tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            tileEntityCompound.setInt("x", blockPosition.getX());
            tileEntityCompound.setInt("y", blockPosition.getY());
            tileEntityCompound.setInt("z", blockPosition.getZ());
            chunk.world.getTileEntity(blockPosition).load(tileEntityCompound);
        }
    }

    @Override
    public CompoundTag readBlockStates(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = world.getType(blockPosition);
        CompoundTag compoundTag = null;

        for(Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.b().entrySet()){
            if(compoundTag == null)
                compoundTag = new CompoundTag();

            Tag<?> value;
            Class<?> keyClass = entry.getKey().getClass();
            String name = entry.getKey().a();

            if(keyClass.equals(BlockStateBoolean.class)) {
                value = new ByteTag((Boolean) entry.getValue() ? (byte) 1 : 0);
            }
            else if(keyClass.equals(BlockStateInteger.class)) {
                BlockStateInteger key = (BlockStateInteger) entry.getKey();
                value = new IntArrayTag(new int[] {(Integer) entry.getValue(), key.min, key.max});
            }
            else{
                BlockStateEnum<?> key = (BlockStateEnum<?>) entry.getKey();
                name = blockStateToName.get(key);
                value = new StringTag(((Enum<?>) entry.getValue()).name());
            }

            compoundTag.setTag(name, value);
        }

        return compoundTag;
    }

    @Override
    public byte[] getLightLevels(Location location) {
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        World world = ((CraftWorld) location.getWorld()).getHandle();
        return new byte[] {
                (byte) world.getBrightness(EnumSkyBlock.SKY, blockPosition),
                (byte) world.getBrightness(EnumSkyBlock.BLOCK, blockPosition),
        };
    }


    @Override
    public void refreshLights(org.bukkit.World bukkitWorld, List<com.bgsoftware.superiorskyblock.utils.blocks.BlockData> blockDataList) {
        Set<ChunkCoordIntPair> chunksToUpdate = new HashSet<>();
        World world = ((CraftWorld) bukkitWorld).getHandle();

        blockDataList.forEach(blockData -> {
            BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
            ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(blockPosition);
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
        world.c(enumSkyBlock, blockPosition.south());
        world.c(enumSkyBlock, blockPosition.north());
        world.c(enumSkyBlock, blockPosition.up());
        world.c(enumSkyBlock, blockPosition.down());
        world.c(enumSkyBlock, blockPosition.east());
        world.c(enumSkyBlock, blockPosition.west());
    }

    @Override
    public CompoundTag readTileEntity(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);

        if(tileEntity == null)
            return null;

        NBTTagCompound tileEntityCompound = tileEntity.save(new NBTTagCompound());

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

        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, 65535);

        AxisAlignedBB bb = new AxisAlignedBB((bukkitChunk.getX() << 4) - 120, 0, (bukkitChunk.getZ() << 4) - 120,
                (bukkitChunk.getX() << 4) + 120, 256, (bukkitChunk.getZ() << 4) + 120);

        Executor.ensureMain(() -> {
            for(Entity entity : chunk.getWorld().getEntities(null, bb)){
                if(entity instanceof EntityPlayer)
                    ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutMapChunk);
            }
        });
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData =  world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public int getCombinedId(Material material, byte data) {
        return Block.getCombinedId(data == 0 ? CraftMagicNumbers.getBlock(material).getBlockData() :
                CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public int compareMaterials(Material o1, Material o2) {
        if(o1.isBlock() && o2.isBlock()) {
            int firstMaterial = Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData());
            int secondMaterial = Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData());
            return Integer.compare(firstMaterial, secondMaterial);
        }
        else{
            return o1.name().compareTo(o2.name());
        }
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProviderServer()
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
                if(chunkSection != null && chunkSection != Chunk.a){
                    for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                        IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                        if (blockData.getBlock() != Blocks.AIR) {
                            Location location = new Location(chunkPosition.getWorld(), (chunkCoords.x << 4) + bp.getX(), chunkSection.getYPosition() + bp.getY(), (chunkCoords.z << 4) + bp.getZ());
                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            Key blockKey = Key.of(type.name(), location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
                            if (type == Material.SPAWNER) {
                                spawnersLocations.add(location);
                            }
                        }
                    }
                }
            }

            completableFuture.complete(new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations));
        }, null);

        return completableFuture;
    }

    @Override
    public void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);

            if(chunk instanceof Chunk) {
                for(int i = 0; i < ((Chunk) chunk).entitySlices.length; i++)
                    ((Chunk) chunk).entitySlices[i] = new UnsafeList<>();

                new HashSet<>(((Chunk) chunk).tileEntities.keySet()).forEach(((Chunk) chunk).world::n);
                ((Chunk) chunk).tileEntities.clear();
            }
            else{
                ((ProtoChunk) chunk).r().clear();
                ((ProtoChunk) chunk).s().clear();
            }

            if(world.generator != null && !(world.generator instanceof WorldGenerator)){
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, 0L, world.generator);
                ProtoChunk protoChunk = chunk instanceof ProtoChunk ? (ProtoChunk) chunk : new ProtoChunk(chunkCoords, ChunkConverter.a);
                customChunkGenerator.createChunk(protoChunk);

                if(chunk instanceof Chunk) {
                    for (int i = 0; i < 16; i++)
                        chunk.getSections()[i] = protoChunk.getSections()[i];

                    for (Map.Entry<BlockPosition, TileEntity> entry : protoChunk.r().entrySet())
                        world.setTileEntity(entry.getKey(), entry.getValue());
                }
            }

            ChunksTracker.markEmpty(island, chunkPosition, false);
        }, chunk -> refreshChunk(chunk.bukkitChunk));
    }

    @Override
    public void setChunkBiome(ChunkPosition chunkPosition, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, chunk -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
            BiomeBase[] biomeIndex = chunk.getBiomeIndex();

            if(chunk instanceof ProtoChunk && biomeIndex == null) {
                biomeIndex = new BiomeBase[256];
                chunk.a(biomeIndex);
            }

            Arrays.fill(biomeIndex, biomeBase);
            if(chunk instanceof Chunk)
                ((Chunk) chunk).markDirty();
        },
        chunk -> {
            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<IChunkAccess> chunkConsumer, Consumer<Chunk> updateChunk){
        runActionOnChunk(bukkitWorld, chunkCoords, saveChunk, null, chunkConsumer, updateChunk);
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Runnable onFinish, Consumer<IChunkAccess> chunkConsumer, Consumer<Chunk> updateChunk){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        IChunkLoader chunkLoader = chunkLoadersMap.computeIfAbsent(bukkitWorld.getUID(), uuid -> CHUNK_LOADER.get(world.getChunkProvider()));

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(updateChunk != null)
                updateChunk.accept(chunk);
            if(onFinish != null)
                onFinish.run();
        }

        else{
            Executor.createTask().runAsync(v -> {
                try{
                    ProtoChunk protoChunk = chunkLoader.b(world, chunkCoords.x, chunkCoords.z, chunkAccess -> {});

                    if(protoChunk == null || protoChunk instanceof ProtoChunkExtension)
                        protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);

                    chunkConsumer.accept(protoChunk);
                    if(saveChunk)
                        chunkLoader.saveChunk(world, protoChunk);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }).runSync(v -> {
                if(onFinish != null)
                    onFinish.run();
            });
        }
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if(stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.tickingChunks
                    .remove(((CraftChunk) chunk).getHandle().getPos().a());
            if(cropsTickingTileEntity != null)
                cropsTickingTileEntity.getWorld().tileEntityListTick.remove(cropsTickingTileEntity);
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

    @Override
    public Material getMaterial(int combinedId) {
        return CraftMagicNumbers.getMaterial(Block.getByCombinedId(combinedId).getBlock());
    }

    @Override
    public byte getData(int combinedId) {
        return 0;
    }

    @Override
    public Key getMinecartBlock(Minecart minecart) {
        return Key.of(minecart.getDisplayBlockData().getMaterial(), (byte) 0);
    }

    @Override
    public boolean isWaterLogged(org.bukkit.block.Block block) {
        if(block.getType().name().contains("WATER"))
            return true;

        BlockData blockData = block.getBlockData();

        return blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
    }

    private static final class CropsTickingTileEntity extends TileEntity implements ITickable {

        private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
        private static int random = ThreadLocalRandom.current().nextInt();

        private final Island island;
        private final Chunk chunk;
        private final int chunkX, chunkZ;

        private int currentTick = 0;

        private CropsTickingTileEntity(Island island, Chunk chunk){
            super(TileEntityTypes.w);
            this.island = island;
            this.chunk = chunk;
            this.chunkX = chunk.getPos().x;
            this.chunkZ = chunk.getPos().z;
            setWorld(chunk.getWorld());
            setPosition(new BlockPosition(chunkX << 4, 1, chunkZ << 4));
            SET_CURRENT_CHUNK.invoke(this, chunk);
            world.tileEntityListTick.add(this);
        }

        @Override
        public void Y_() {
            if(++currentTick <= plugin.getSettings().cropsInterval)
                return;

            currentTick = 0;

            int worldRandomTick = world.getGameRules().c("randomTickSpeed");
            double cropGrowth = island.getCropGrowthMultiplier() - 1;

            int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().cropsInterval);

            if (chunkRandomTickSpeed > 0) {
                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != Chunk.a && chunkSection.shouldTick()) {
                        for (int i = 0; i < chunkRandomTickSpeed; i++) {
                            random = random * 3 + 1013904223;
                            int factor = random >> 2;
                            int x = factor & 15;
                            int z = factor >> 8 & 15;
                            int y = factor >> 16 & 15;
                            IBlockData blockData = chunkSection.getType(x, y, z);
                            Block block = blockData.getBlock();
                            if (block.isTicking(blockData) && plugin.getSettings().cropsToGrow.contains(CraftMagicNumbers.getMaterial(block).name())) {
                                RANDOM_TICK.set(block, true);
                                blockData.b(world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)), ThreadLocalRandom.current());
                                RANDOM_TICK.set(block, false);
                            }
                        }
                    }
                }
            }

        }

        static void create(Island island, Chunk chunk){
            long chunkPair = chunk.getPos().a();
            if(!tickingChunks.containsKey(chunkPair)){
                tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk));
            }
        }

    }

}
