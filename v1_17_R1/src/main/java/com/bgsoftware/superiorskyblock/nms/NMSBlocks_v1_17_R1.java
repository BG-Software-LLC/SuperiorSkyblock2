package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.listeners.BlocksListener;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.objects.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.tags.ByteTag;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.tags.IntArrayTag;
import com.bgsoftware.superiorskyblock.utils.tags.StringTag;
import com.bgsoftware.superiorskyblock.utils.tags.Tag;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.World;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockBed;
import net.minecraft.world.level.block.BlockStepAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntitySign;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertySlabType;
import net.minecraft.world.level.block.state.properties.BlockStateBoolean;
import net.minecraft.world.level.block.state.properties.BlockStateEnum;
import net.minecraft.world.level.block.state.properties.BlockStateInteger;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkConverter;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkRegionLoader;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.LightEngineGraph;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "ConstantConditions", "rawtypes"})
public final class NMSBlocks_v1_17_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<String, IBlockState> nameToBlockState = new HashMap<>();
    private static final Map<IBlockState, String> blockStateToName = new HashMap<>();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "f");
    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);

//    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(LightEngineThreaded.class, Object.class, "theLightEngine");
//    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(LightEngineThreaded.class, ThreadedMailbox.class, "b");

    static {
        Map<String, String> fieldNameToName = new HashMap<>();
        fieldNameToName.put("G", "axis-empty");
        fieldNameToName.put("O", "facing-notup");
        fieldNameToName.put("P", "facing-horizontal");
        fieldNameToName.put("T", "wall-east");
        fieldNameToName.put("U", "wall-north");
        fieldNameToName.put("V", "wall-south");
        fieldNameToName.put("W", "wall-west");
        fieldNameToName.put("X", "redstone-east");
        fieldNameToName.put("Y", "redstone-north");
        fieldNameToName.put("Z", "redstone-south");
        fieldNameToName.put("aa", "redstone-west");
        fieldNameToName.put("ab", "double-half");
        fieldNameToName.put("ad", "track-shape-empty");
        fieldNameToName.put("ae", "track-shape");
        fieldNameToName.put("am", "age1");
        fieldNameToName.put("an", "age2");
        fieldNameToName.put("ao", "age3");
        fieldNameToName.put("ap", "age5");
        fieldNameToName.put("aq", "age7");
        fieldNameToName.put("ar", "age15");
        fieldNameToName.put("as", "age25");
        fieldNameToName.put("aF", "level3");
        fieldNameToName.put("aG", "level8");
        fieldNameToName.put("aH", "level1-8");
        fieldNameToName.put("aK", "level15");
        fieldNameToName.put("ax", "distance1-7");
        fieldNameToName.put("aR", "distance7");
        fieldNameToName.put("aY", "chest-type");
        fieldNameToName.put("aZ", "comparator-mode");
        fieldNameToName.put("bc", "piston-type");
        fieldNameToName.put("bd", "slab-type");

        try{
            // Fixes BlockProperties being private-class in some versions of Yatopia causing illegal access errors.
            Class<?> blockPropertiesClass = Class.forName("net.minecraft.world.level.block.state.properties.BlockProperties");

            for(Field field : blockPropertiesClass.getFields()){
                field.setAccessible(true);
                Object value = field.get(null);
                if(value instanceof IBlockState) {
                    register(fieldNameToName.getOrDefault(field.getName(), ((IBlockState) value).getName()),
                            field.getName(), (IBlockState) value);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

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
    public void setBlocks(org.bukkit.Chunk bukkitChunk, List<BlockData> blockDataList) {
        WorldServer world = ((CraftWorld) bukkitChunk.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(bukkitChunk.getX(), bukkitChunk.getZ());

        for(BlockData blockData : blockDataList)
            setBlock(chunk, new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ()),
                    blockData.getCombinedId(), blockData.getStatesTag(), blockData.getClonedTileEntity());

        if(plugin.getSettings().lightsUpdate) {
            // Update lights for the blocks.
            // We use a delayed task to avoid null nibbles
            Executor.sync(() -> {
//                if(STAR_LIGHT_INTERFACE.isValid()){
//                    LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) world.e();
//                    StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
//                    ChunkProviderServer chunkProviderServer = world.getChunkProvider();
//                    LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).queue(() ->
//                        starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
//                                chunkProviderServer.serverThreadQueue.execute(() ->
//                                        chunkProviderServer.playerChunkMap.getUpdatingChunk(chunkPos.pair())
//                                                .sendPacketToTrackedPlayers(new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, true), false)
//                                ), null));
//                }
//                else {
//                TODO: Paper
                    for (BlockData blockData : blockDataList) {
                        BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                        if (blockData.getBlockLightLevel() > 0) {
                            try {
                                world.k_().a(EnumSkyBlock.b).a(blockPosition, blockData.getBlockLightLevel());
                            } catch (Exception ignored) {
                            }
                        }
                        if (blockData.getSkyLightLevel() > 0 && bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                            try {
                                SKY_LIGHT_UPDATE.invoke(world.k_().a(EnumSkyBlock.a), 9223372036854775807L,
                                        blockPosition.asLong(), 15 - blockData.getSkyLightLevel(), true);
                            } catch (Exception ignored) {
                            }
                        }
                    }
//                }
            }, 10L);
        }
    }

    @Override
    public void setBlock(Location location, Material material, byte data) {
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        setBlock(world.getChunkAtWorldCoords(blockPosition), blockPosition, getCombinedId(material, data), null, null);
        sendPacketToRelevantPlayers(world, blockPosition.getX() >> 4, blockPosition.getZ() >> 4,
                new PacketPlayOutBlockChange(world, blockPosition));
    }

    @SuppressWarnings("unchecked")
    private void setBlock(Chunk chunk, BlockPosition blockPosition, int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
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
                            blockData = blockData.set(blockState, Enum.valueOf(blockState.getType(), data));
                        }
                    }
                }catch (Exception ignored){}
            }
        }

        if((blockData.getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) ||
                blockData.getBlock() instanceof BlockBed) {
            chunk.getWorld().setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        if(plugin.getSettings().lightsUpdate) {
            chunk.setType(blockPosition, blockData, true, true);
        }
        else {
            int indexY = chunk.getSectionIndex(blockPosition.getY());

            ChunkSection chunkSection = chunk.getSections()[indexY];

            if (chunkSection == null) {
                int yOffset = SectionPosition.a(blockPosition.getY());
                try {
                    // Paper's constructor for ChunkSection for more optimized chunk sections.
                    chunkSection = chunk.getSections()[indexY] = new ChunkSection(yOffset, chunk, chunk.getWorld(), true);
                }catch (Throwable ex){
                    // Spigot's constructor for ChunkSection
                    // noinspection deprecation
                    chunkSection = chunk.getSections()[indexY] = new ChunkSection(yOffset);
                }
                chunkSection = chunk.getSections()[indexY] = chunkSection;
            }

            int blockX = blockPosition.getX() & 15;
            int blockY = blockPosition.getY();
            int blockZ = blockPosition.getZ() & 15;

            chunkSection.setType(blockX, blockY & 15, blockZ, blockData, false);

            chunk.j.get(HeightMap.Type.e).a(blockX, blockY, blockZ, blockData);
            chunk.j.get(HeightMap.Type.f).a(blockX, blockY, blockZ, blockData);
            chunk.j.get(HeightMap.Type.d).a(blockX, blockY, blockZ, blockData);
            chunk.j.get(HeightMap.Type.b).a(blockX, blockY, blockZ, blockData);

            chunk.markDirty();
        }

        if(tileEntity != null) {
            NBTTagCompound tileEntityCompound = (NBTTagCompound) tileEntity.toNBT();
            tileEntityCompound.setInt("x", blockPosition.getX());
            tileEntityCompound.setInt("y", blockPosition.getY());
            tileEntityCompound.setInt("z", blockPosition.getZ());
            chunk.getWorld().getTileEntity(blockPosition).load(tileEntityCompound);
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
            String name = entry.getKey().getName();

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
        LightEngine lightEngine = ((CraftWorld) location.getWorld()).getHandle().k_();
        return new byte[] {
            location.getWorld().getEnvironment() != org.bukkit.World.Environment.NORMAL ? 0 :
                    (byte) lightEngine.a(EnumSkyBlock.a).b(blockPosition),
            (byte) lightEngine.a(EnumSkyBlock.b).b(blockPosition)
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
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        sendPacketToRelevantPlayers((WorldServer) chunk.getWorld(), chunkCoords.b, chunkCoords.c, new PacketPlayOutMapChunk(chunk));
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
                        if (blockData.getBlock() != Blocks.a) {
                            Location location = new Location(chunkPosition.getWorld(),
                                    (chunkCoords.b << 4) + bp.getX(),
                                    chunkSection.getYPosition() + bp.getY(),
                                    (chunkCoords.c << 4) + bp.getZ());

                            int blockAmount = 1;

                            if((TagsBlock.E.isTagged(blockData.getBlock()) || TagsBlock.j.isTagged(blockData.getBlock())) &&
                                    blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.c) {
                                blockAmount = 2;
                                blockData = blockData.set(BlockStepAbstract.a, BlockPropertySlabType.b);
                            }

                            Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                            Key blockKey = Key.of(type.name() + "", "", location);
                            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + blockAmount);
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

            AxisAlignedBB chunkBounds = new AxisAlignedBB(
                    chunkCoords.b << 4, 0, chunkCoords.c << 4,
                    chunkCoords.b << 4 + 15, chunk.getWorld().getMaxBuildHeight(), chunkCoords.c << 4 + 15
            );

            Iterator<Entity> chunkEntities;

            try {
                chunkEntities = chunk.entities.iterator();
            } catch(Throwable ex) {
                List<Entity> worldEntities = new ArrayList<>();
                world.getEntities().a(chunkBounds, worldEntities::add);
                chunkEntities = worldEntities.iterator();
            }

            while(chunkEntities.hasNext()){
                chunkEntities.next().setRemoved(Entity.RemovalReason.b);
            }

            new HashSet<>(chunk.l.keySet()).forEach(chunk.getWorld()::removeTileEntity);
            chunk.l.clear();

            if(world.generator != null && !(world.generator instanceof WorldGenerator)){
                CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world,
                        world.getChunkProvider().d, world.generator);
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a, world);
                customChunkGenerator.buildBase(null, protoChunk);

                for(int i = 0; i < 16; i++)
                    chunk.getSections()[i] = protoChunk.getSections()[i];

                protoChunk.y().values().forEach(world::setTileEntity);
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
                ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a, world);

                try {
                    CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(world,
                            world.getChunkProvider().d, world.generator);
                    customChunkGenerator.buildBase(null, protoChunk);
                }catch (Exception ignored){}

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
                    NBTTagCompound tileCompound = protoChunk.f(tilePosition);
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
        WorldServer world = ((CraftWorld) chunkPosition.getWorld()).getHandle();
        IRegistry<BiomeBase> biomeBaseRegistry = world.t().b(IRegistry.aO);
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

        runActionOnChunk(chunkPosition.getWorld(), chunkCoords, true, chunk -> {
            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if(biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.b + "," + chunkCoords.c + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.b, chunkCoords.c);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        },
        levelCompound -> {
            int[] biomes = levelCompound.hasKeyOfType("Biomes", 11) ? levelCompound.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, biomeBaseRegistry.getId(biomeBase));
            levelCompound.setIntArray("Biomes", biomes);
        });
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        runActionOnChunk(bukkitWorld, chunkCoords, saveChunk, null, chunkConsumer, compoundConsumer);
    }

    private void runActionOnChunk(org.bukkit.World bukkitWorld, ChunkCoordIntPair chunkCoords, boolean saveChunk, Runnable onFinish, Consumer<Chunk> chunkConsumer, Consumer<NBTTagCompound> compoundConsumer){
        WorldServer world = ((CraftWorld) bukkitWorld).getHandle();
        PlayerChunkMap playerChunkMap = world.getChunkProvider().a;

        Chunk chunk = world.getChunkIfLoaded(chunkCoords.b, chunkCoords.c);

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
                        ProtoChunk protoChunk = new ProtoChunk(chunkCoords, ChunkConverter.a, world);
                        chunkCompound = ChunkRegionLoader.saveChunk(world, protoChunk);
                    }

                    else{
                        chunkCompound = playerChunkMap.getChunkData(world.getTypeKey(),
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
                cropsTickingTileEntity.remove();
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
            System.arraycopy(CraftSign.revertComponents(tileEntitySign.d), 0, lines, 0, lines.length);
            String[] strippedLines = new String[4];
            for (int i = 0; i < 4; i++)
                strippedLines[i] = StringUtils.stripColors(lines[i]);

            IChatBaseComponent[] newLines;

            if (BlocksListener.IMP.onSignPlace(island.getOwner(), island, location, strippedLines, false))
                newLines = CraftSign.sanitizeLines(strippedLines);
            else
                newLines = CraftSign.sanitizeLines(lines);

            System.arraycopy(newLines, 0, tileEntitySign.d, 0, 4);
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

        org.bukkit.block.data.BlockData blockData = block.getBlockData();

        return blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
    }

    @Override
    public int getDefaultAmount(org.bukkit.block.Block block) {
        IBlockData blockData = ((CraftBlock) block).getNMS();
        Block nmsBlock =  blockData.getBlock();

        // Checks for double slabs
        if((TagsBlock.E.isTagged(nmsBlock) || TagsBlock.j.isTagged(nmsBlock)) &&
            blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.c) {
            return 2;
        }

        return 1;
    }

    private void sendPacketToRelevantPlayers(WorldServer worldServer, int chunkX, int chunkZ, Packet<?> packet){
        PlayerChunkMap playerChunkMap = worldServer.getChunkProvider().a;
        ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(chunkX, chunkZ);
        playerChunkMap.l.get(chunkCoordIntPair.pair()).y.a(chunkCoordIntPair, false)
                .forEach(entityPlayer -> entityPlayer.b.sendPacket(packet));
    }

    private static final class CropsTickingTileEntity extends TileEntity {

        private static final Map<Long, CropsTickingTileEntity> tickingChunks = new HashMap<>();
        private static int random = ThreadLocalRandom.current().nextInt();

        private final WeakReference<Island> island;
        private final WeakReference<Chunk> chunk;
        private final int chunkX, chunkZ;

        private int currentTick = 0;

        private CropsTickingTileEntity(Island island, Chunk chunk, BlockPosition blockPosition){
            super(TileEntityTypes.v, blockPosition, chunk.getWorld().getType(blockPosition));
            this.island = new WeakReference<>(island);
            this.chunk = new WeakReference<>(chunk);
            this.chunkX = chunk.getPos().b;
            this.chunkZ = chunk.getPos().c;
            setWorld(chunk.getWorld());

//            try {
//                // Not a method of Spigot - fixes https://github.com/OmerBenGera/SuperiorSkyblock2/issues/5
//                setCurrentChunk(chunk);
//            }catch (Throwable ignored){}

            this.n.a(new CropsTickingTileEntityTicker(this));
        }

        public void remove(){
            this.p = true;
        }

        public void tick(){
            if(++currentTick <= plugin.getSettings().cropsInterval)
                return;

            Chunk chunk = this.chunk.get();
            Island island = this.island.get();

            if(chunk == null || island == null){
                remove();
                return;
            }

            currentTick = 0;

            int worldRandomTick = this.n.getGameRules().getInt(GameRules.n);
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
                                blockData.b((WorldServer) this.n, new BlockPosition(x + (chunkX << 4), y + chunkSection.getYPosition(), z + (chunkZ << 4)), ThreadLocalRandom.current());
                            }
                        }
                    }
                }
            }
        }

        static void create(Island island, Chunk chunk){
            long chunkPair = chunk.getPos().pair();
            if(!tickingChunks.containsKey(chunkPair)) {
                BlockPosition blockPosition = new BlockPosition(chunk.getPos().b << 4, 1, chunk.getPos().c << 4);
                tickingChunks.put(chunkPair, new CropsTickingTileEntity(island, chunk, blockPosition));
            }
        }

    }

    private static class CropsTickingTileEntityTicker implements TickingBlockEntity {

        private final CropsTickingTileEntity cropsTickingTileEntity;

        CropsTickingTileEntityTicker(CropsTickingTileEntity cropsTickingTileEntity){
            this.cropsTickingTileEntity = cropsTickingTileEntity;
        }

        @Override
        public void a() {
            cropsTickingTileEntity.tick();
        }

        @Override
        public boolean b() {
            return cropsTickingTileEntity.isRemoved();
        }

        @Override
        public BlockPosition c() {
            return cropsTickingTileEntity.getPosition();
        }

        @Override
        public String d() {
            return TileEntityTypes.a(cropsTickingTileEntity.getTileType()) + "";
        }
    }

}
