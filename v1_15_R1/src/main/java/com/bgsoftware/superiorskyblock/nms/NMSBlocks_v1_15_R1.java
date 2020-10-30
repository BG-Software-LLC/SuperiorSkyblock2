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
import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BiomeBase;
import net.minecraft.server.v1_15_R1.BiomeStorage;
import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.BlockBed;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.BlockProperties;
import net.minecraft.server.v1_15_R1.BlockStateBoolean;
import net.minecraft.server.v1_15_R1.BlockStateEnum;
import net.minecraft.server.v1_15_R1.BlockStateInteger;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkConverter;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkRegionLoader;
import net.minecraft.server.v1_15_R1.ChunkSection;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumSkyBlock;
import net.minecraft.server.v1_15_R1.GameRules;
import net.minecraft.server.v1_15_R1.HeightMap;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.IBlockState;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.ITickable;
import net.minecraft.server.v1_15_R1.LightEngine;
import net.minecraft.server.v1_15_R1.LightEngineBlock;
import net.minecraft.server.v1_15_R1.LightEngineGraph;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.PacketPlayOutBlockChange;
import net.minecraft.server.v1_15_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_15_R1.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_15_R1.PlayerChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.ProtoChunk;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.TileEntitySign;
import net.minecraft.server.v1_15_R1.TileEntityTypes;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_15_R1.util.UnsafeList;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ConstantConditions", "rawtypes"})
public final class NMSBlocks_v1_15_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<String, IBlockState> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState, String> blockStateToName = new HashMap<>();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "f", "g");
    private static final ReflectField<Boolean> RANDOM_TICK = new ReflectField<>(Block.class, Boolean.class, "randomTick");

    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("E", "axis-empty");
        fieldNameToName.put("M", "facing-notup");
        fieldNameToName.put("N", "facing-horizontal");
        fieldNameToName.put("Q", "redstone-east");
        fieldNameToName.put("R", "redstone-north");
        fieldNameToName.put("S", "redstone-south");
        fieldNameToName.put("T", "redstone-west");
        fieldNameToName.put("U", "double-half");
        fieldNameToName.put("W", "track-shape-empty");
        fieldNameToName.put("X", "track-shape");
        fieldNameToName.put("Y", "age1");
        fieldNameToName.put("Z", "age2");
        fieldNameToName.put("aa", "age3");
        fieldNameToName.put("ab", "age5");
        fieldNameToName.put("ac", "age7");
        fieldNameToName.put("ad", "age15");
        fieldNameToName.put("ae", "age25");
        fieldNameToName.put("al", "level3");
        fieldNameToName.put("am", "level8");
        fieldNameToName.put("an", "level1-8");
        fieldNameToName.put("ap", "level15");
        fieldNameToName.put("ah", "distance1-7");
        fieldNameToName.put("av", "distance7");
        fieldNameToName.put("ay", "chest-type");
        fieldNameToName.put("az", "comparator-mode");
        fieldNameToName.put("aC", "piston-type");
        fieldNameToName.put("aD", "slab-type");

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
                if (blockData.getBlockLightLevel() > 0) {
                    try {
                        ((LightEngineBlock) world.e().a(EnumSkyBlock.BLOCK)).a(blockPosition, blockData.getBlockLightLevel());
                    } catch (Exception ignored) {}
                }
                if(blockData.getSkyLightLevel() > 0 && bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL){
                    try {
                        SKY_LIGHT_UPDATE.invoke(world.e().a(EnumSkyBlock.SKY), 9223372036854775807L,
                                blockPosition.asLong(), 15 - blockData.getSkyLightLevel(), true);
                    } catch (Exception ignored) { }
                }
            }
        }

    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, getCombinedId(material, data), null, null);

        AxisAlignedBB bb = new AxisAlignedBB(blockPosition.getX() - 120, 0, blockPosition.getZ() - 120,
                blockPosition.getX() + 120, 256, blockPosition.getZ() + 120);

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

        if(plugin.getSettings().lightsUpdate) {
            chunk.setType(blockPosition, blockData, true, true);
        }
        else {
            int indexY = blockPosition.getY() >> 4;

            ChunkSection chunkSection = chunk.getSections()[indexY];

            if (chunkSection == null)
                chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4);

            int blockX = blockPosition.getX() & 15;
            int blockY = blockPosition.getY();
            int blockZ = blockPosition.getZ() & 15;

            chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

            chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING).a(blockX, blockY, blockZ, blockData);
            chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES).a(blockX, blockY, blockZ, blockData);
            chunk.heightMap.get(HeightMap.Type.OCEAN_FLOOR).a(blockX, blockY, blockZ, blockData);
            chunk.heightMap.get(HeightMap.Type.WORLD_SURFACE).a(blockX, blockY, blockZ, blockData);
        }

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

        for(Map.Entry<IBlockState<?>, Comparable<?>> entry : blockData.getStateMap().entrySet()){
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
        LightEngine lightEngine = ((CraftWorld) location.getWorld()).getHandle().e();
        return new byte[] {
                location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL ? 0 : (byte) lightEngine.a(EnumSkyBlock.SKY).b(blockPosition),
                (byte) lightEngine.a(EnumSkyBlock.BLOCK).b(blockPosition)
        };
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

        AxisAlignedBB bb = new AxisAlignedBB((bukkitChunk.getX() << 4) - 60, 0, (bukkitChunk.getZ() << 4) - 60,
                (bukkitChunk.getX() << 4) + 60, 256, (bukkitChunk.getZ() << 4) + 60);

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
        int firstMaterial = o1.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o1).getBlockData()) : o1.ordinal();
        int secondMaterial = o2.isBlock() ? Block.getCombinedId(CraftMagicNumbers.getBlock(o2).getBlockData()) : o2.ordinal();
        return Integer.compare(firstMaterial, secondMaterial);
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProvider()
                .getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public CompletableFuture<CalculatedChunk> calculateChunk(ChunkPosition chunkPosition) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());

        CompletableFuture<CalculatedChunk> completableFuture = new CompletableFuture<>();
        KeyMap<Integer> blockCounts = new KeyMap<>();
        Set<Location> spawnersLocations = new HashSet<>();

        Consumer<ChunkSection[]> calculateConsumer = chunkSections -> {
            for(ChunkSection chunkSection : chunkSections){
                if(chunkSection != null){
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
        };

        //noinspection all
        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, false, chunk -> {
                    calculateConsumer.accept(chunk.getSections());
                },
                levelCompound -> {
                    NBTTagList sectionsList = levelCompound.getList("Sections", 10);
                    ChunkSection[] chunkSections = new ChunkSection[sectionsList.size()];

                    for (int i = 0; i < sectionsList.size(); ++i) {
                        NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                        byte yPosition = sectionCompound.getByte("Y");
                        if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                            chunkSections[i] = new ChunkSection(yPosition << 4);
                            chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                        }
                    }

                    calculateConsumer.accept(chunkSections);
                });

        return completableFuture;
    }

    @Override
    public void deleteChunk(Island island, ChunkPosition chunkPosition, Runnable onFinish) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);

            for(int i = 0; i < chunk.entitySlices.length; i++)
                chunk.entitySlices[i] = new UnsafeList<>();

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
            chunk.tileEntities.clear();

            if(world.generator != null && !(world.generator instanceof WorldGenerator)){
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, world.generator);
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                customChunkGenerator.buildBase(null, protoChunk);

                for(int i = 0; i < 16; i++)
                    chunk.getSections()[i] = protoChunk.getSections()[i];

                for(Map.Entry<BlockPosition, TileEntity> entry : protoChunk.x().entrySet())
                    world.setTileEntity(entry.getKey(), entry.getValue());
            }

            refreshChunk(chunk.getBukkitChunk());
        },
        levelCompound -> {
            NBTTagList sectionsList = new NBTTagList();
            NBTTagList tileEntities = new NBTTagList();

            levelCompound.set("Sections", sectionsList);
            levelCompound.set("TileEntities", tileEntities);
            levelCompound.set("Entities", new NBTTagList());

            if(!(world.generator instanceof WorldGenerator)) {
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world, world.generator);
                customChunkGenerator.buildBase(null, protoChunk);
                ChunkSection[] chunkSections = protoChunk.getSections();

                for(int i = -1; i < 17; ++i) {
                    int chunkSectionIndex = i;
                    ChunkSection chunkSection = Arrays.stream(chunkSections).filter(_chunkPosition ->
                            _chunkPosition != null && _chunkPosition.getYPosition() >> 4 == chunkSectionIndex)
                            .findFirst().orElse(Chunk.a);

                    if (chunkSection != Chunk.a) {
                        NBTTagCompound sectionCompound = new NBTTagCompound();
                        sectionCompound.setByte("Y", (byte) (i & 255));
                        chunkSection.getBlocks().a(sectionCompound, "Palette", "BlockStates");
                        sectionsList.add(sectionCompound);
                    }
                }

                for(BlockPosition tilePosition : protoChunk.c()){
                    NBTTagCompound tileCompound = protoChunk.i(tilePosition);
                    if(tileCompound != null)
                        tileEntities.add(tileCompound);
                }
            }
        });

        ChunksTracker.markEmpty(island, chunkPosition, false);
    }

    @Override
    public void setChunkBiome(ChunkPosition chunkPosition, Biome biome, List<Player> playersToUpdate) {
        ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ());
        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, chunk -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);

            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if(biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkPosition.getX() + "," + chunkPosition.getZ() + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkPosition.getX(), chunkPosition.getZ());
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        },
        levelCompound -> {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biome);
            int[] biomes = levelCompound.hasKeyOfType("Biomes", 11) ? levelCompound.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, IRegistry.BIOME.a(biomeBase));
            levelCompound.setIntArray("Biomes", biomes);
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        runActionOnChunk(bukkitWorld, chunkCoords, saveChunk, null, chunkConsumer, compoundConsumer);
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Runnable onFinish, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        PlayerChunkMap playerChunkMap = world.getChunkProvider().playerChunkMap;

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.x, chunkCoords.z);

        if(chunk != null){
            chunkConsumer.accept(chunk);
            if(onFinish != null)
                onFinish.run();
        }

        else{
            Executor.createTask().runAsync(v -> {
                try{
                    NBTTagCompound chunkCompound = playerChunkMap.read(chunkCoords);

                    if(chunkCompound == null){
                        ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a);
                        chunkCompound = ChunkRegionLoader.saveChunk(world, protoChunk);
                    }

                    else{
                        chunkCompound = playerChunkMap.getChunkData(world.getWorldProvider().getDimensionManager(),
                                Suppliers.ofInstance(world.getWorldPersistentData()), chunkCompound, chunkCoords, world);
                    }

                    if(chunkCompound.hasKeyOfType("Level", 10)) {
                        compoundConsumer.accept(chunkCompound.getCompound("Level"));
                        if(saveChunk)
                            playerChunkMap.a(chunkCoords, chunkCompound);
                    }
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
                    .remove(((CraftChunk) chunk).getHandle().getPos().pair());
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

        private static final ReflectField<CompletableFuture<Either<Chunk, PlayerChunk.Failure>>> TICKING_FUTURE =
                new ReflectField<>(PlayerChunk.class, CompletableFuture.class, "tickingFuture");

        private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
        private static int random = ThreadLocalRandom.current().nextInt();

        private final Island island;
        private final Chunk chunk;
        private final int chunkX, chunkZ;

        private int currentTick = 0;

        private CropsTickingTileEntity(Island island, Chunk chunk){
            super(TileEntityTypes.COMMAND_BLOCK);
            this.island = island;
            this.chunk = chunk;
            this.chunkX = chunk.getPos().x;
            this.chunkZ = chunk.getPos().z;
            setLocation(chunk.getWorld(), new BlockPosition(chunkX << 4, 1, chunkZ << 4));
            world.tileEntityListTick.add(this);
        }

        @Override
        public void tick() {
            if(++currentTick <= plugin.getSettings().cropsInterval)
                return;

            currentTick = 0;

            int worldRandomTick = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            double cropGrowth = island.getCropGrowthMultiplier() - 1;

            int chunkRandomTickSpeed = (int) (worldRandomTick * cropGrowth * plugin.getSettings().cropsInterval);

            if (chunkRandomTickSpeed > 0) {
                for (ChunkSection chunkSection : chunk.getSections()) {
                    if (chunkSection != Chunk.a && chunkSection.d()) {
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
                                blockData.b((WorldServer) world, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)), ThreadLocalRandom.current());
                                RANDOM_TICK.set(block, false);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void v() {
            tick();
        }

        static void create(Island island, Chunk chunk){
            long chunkPair = chunk.getPos().pair();
            if(!tickingChunks.containsKey(chunkPair)){
                tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk));
            }
        }

    }

}
