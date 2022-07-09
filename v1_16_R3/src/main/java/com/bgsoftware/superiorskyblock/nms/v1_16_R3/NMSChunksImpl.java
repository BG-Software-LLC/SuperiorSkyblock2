package com.bgsoftware.superiorskyblock.nms.v1_16_R3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_16_R3.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.tuinity.tuinity.chunk.light.StarLightInterface;
import net.minecraft.server.v1_16_R3.BiomeBase;
import net.minecraft.server.v1_16_R3.BiomeStorage;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPropertySlabType;
import net.minecraft.server.v1_16_R3.BlockStepAbstract;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkProviderServer;
import net.minecraft.server.v1_16_R3.ChunkSection;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EnumSkyBlock;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.LightEngineBlock;
import net.minecraft.server.v1_16_R3.LightEngineGraph;
import net.minecraft.server.v1_16_R3.LightEngineThreaded;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.PacketPlayOutLightUpdate;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R3.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.ProtoChunk;
import net.minecraft.server.v1_16_R3.TagsBlock;
import net.minecraft.server.v1_16_R3.ThreadedMailbox;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R3.util.UnsafeList;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "h");
    private static final ReflectField<Collection<Entity>[]> ENTITY_SLICE_ARRAY = new ReflectField<>(
            Chunk.class, null, "entitySlices");
    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(
            LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);
    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(
            LightEngineThreaded.class, Object.class, "theLightEngine");
    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(
            LightEngineThreaded.class, ThreadedMailbox.class, "b");

    private final SuperiorSkyblockPlugin plugin;

    public NMSChunksImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = new SequentialListBuilder<ChunkCoordIntPair>()
                .build(chunkPositions, chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()));

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        IRegistry<BiomeBase> biomeBaseRegistry = worldServer.r().b(IRegistry.ay);
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if (biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.x + "," + chunkCoords.z + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) worldServer.e();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            //noinspection deprecation
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            PacketPlayOutLightUpdate lightUpdatePacket = new PacketPlayOutLightUpdate(chunkCoords, lightEngineThreaded, true);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(lightUpdatePacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        }, (chunkCoords, unloadedChunk) -> {
            int[] biomes = unloadedChunk.hasKeyOfType("Biomes", 11) ? unloadedChunk.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, biomeBaseRegistry.a(biomeBase));
            unloadedChunk.setIntArray("Biomes", biomes);
        });
    }

    @Override
    public void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = new SequentialListBuilder<ChunkCoordIntPair>()
                .build(chunkPositions, chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()));

        chunkPositions.forEach(chunkPosition -> ChunksTracker.markEmpty(island, chunkPosition, false));

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, onFinish, chunk -> {
            Arrays.fill(chunk.getSections(), Chunk.a);
            removeEntities(chunk);

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
            chunk.tileEntities.clear();

            removeBlocks(chunk);
        }, (chunkCoords, levelCompound) -> {
            NBTTagList sectionsList = new NBTTagList();
            NBTTagList tileEntities = new NBTTagList();

            levelCompound.set("Sections", sectionsList);
            levelCompound.set("TileEntities", tileEntities);
            levelCompound.set("Entities", new NBTTagList());

            if (!(worldServer.generator instanceof IslandsGenerator)) {
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);

                try {
                    CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer,
                            worldServer.getChunkProvider().chunkGenerator, worldServer.generator);
                    customChunkGenerator.buildBase(null, protoChunk);
                } catch (Exception ignored) {
                }

                ChunkSection[] chunkSections = protoChunk.getSections();

                for (int i = -1; i < 17; ++i) {
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

                for (BlockPosition tilePosition : protoChunk.c()) {
                    NBTTagCompound tileCompound = protoChunk.i(tilePosition);
                    if (tileCompound != null)
                        tileEntities.add(tileCompound);
                }
            }
        });
    }

    @Override
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                                    Map<ChunkPosition, CalculatedChunk> unloadedChunksCache) {
        List<CalculatedChunk> allCalculatedChunks = new LinkedList<>();
        List<ChunkCoordIntPair> chunksCoords = new LinkedList<>();

        Iterator<ChunkPosition> chunkPositionsIterator = chunkPositions.iterator();
        while (chunkPositionsIterator.hasNext()) {
            ChunkPosition chunkPosition = chunkPositionsIterator.next();
            CalculatedChunk cachedCalculatedChunk = unloadedChunksCache.get(chunkPosition);
            if (cachedCalculatedChunk != null) {
                allCalculatedChunks.add(cachedCalculatedChunk);
                chunkPositionsIterator.remove();
            } else {
                chunksCoords.add(new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()));
            }
        }

        if (chunkPositions.isEmpty())
            return CompletableFuture.completedFuture(allCalculatedChunks);

        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, false, () -> {
            completableFuture.complete(allCalculatedChunks);
        }, chunk -> {
            ChunkPosition chunkPosition = ChunkPosition.of(chunk.getWorld().getWorld(), chunk.getPos().x, chunk.getPos().z);
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunk.getSections()));
        }, (chunkCoords, unloadedChunk) -> {
            NBTTagList sectionsList = unloadedChunk.getList("Sections", 10);
            ChunkSection[] chunkSections = new ChunkSection[sectionsList.size()];

            for (int i = 0; i < sectionsList.size(); ++i) {
                NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                byte yPosition = sectionCompound.getByte("Y");
                if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                    //noinspection deprecation
                    chunkSections[i] = new ChunkSection(yPosition << 4);
                    chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                }
            }

            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunkCoords.x, chunkCoords.z);
            CalculatedChunk calculatedChunk = calculateChunk(chunkPosition, chunkSections);
            allCalculatedChunks.add(calculatedChunk);
            unloadedChunksCache.put(chunkPosition, calculatedChunk);
        });

        return completableFuture;
    }

    @Override
    public void injectChunkSections(org.bukkit.Chunk chunk) {
        // No implementation
    }

    @Override
    public boolean isChunkEmpty(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection -> chunkSection == null || chunkSection.c());
    }

    @Override
    public void refreshLights(org.bukkit.Chunk bukkitChunk, List<SchematicBlock> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        WorldServer world = (WorldServer) chunk.getWorld();

        if (plugin.getSettings().isLightsUpdate()) {
            // Update lights for the blocks.
            // We use a delayed task to avoid null nibbles
            BukkitExecutor.sync(() -> {
                if (STAR_LIGHT_INTERFACE.isValid()) {
                    LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) world.e();
                    StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
                    ChunkProviderServer chunkProviderServer = world.getChunkProvider();
                    LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).queue(() ->
                            starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
                                    chunkProviderServer.serverThreadQueue.execute(() ->
                                            NMSUtils.sendPacketToRelevantPlayers(world, chunkPos.x, chunkPos.z,
                                                    new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, true))
                                    ), null));
                } else {
                    for (SchematicBlock blockData : blockDataList) {
                        BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                        if (blockData.getBlockLightLevel() > 0) {
                            try {
                                ((LightEngineBlock) world.e().a(EnumSkyBlock.BLOCK)).a(blockPosition, blockData.getBlockLightLevel());
                            } catch (Exception ignored) {
                            }
                        }
                        if (blockData.getSkyLightLevel() > 0 && bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                            try {
                                SKY_LIGHT_UPDATE.invoke(world.e().a(EnumSkyBlock.SKY), 9223372036854775807L,
                                        blockPosition.asLong(), 15 - blockData.getSkyLightLevel(), true);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = ((CraftWorld) chunkPosition.getWorld()).getHandle().getChunkProvider()
                .getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(((CraftChunk) chunk).getHandle().getPos());
            World world = cropsTickingTileEntity == null ? null : cropsTickingTileEntity.getWorld();
            if (cropsTickingTileEntity != null && world != null)
                world.tileEntityListTick.remove(cropsTickingTileEntity);
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, ChunkSection[] chunkSections) {
        KeyMap<Integer> blockCounts = KeyMapImpl.createHashMap();
        Set<Location> spawnersLocations = new HashSet<>();

        for (ChunkSection chunkSection : chunkSections) {
            if (chunkSection != null) {
                for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                    IBlockData blockData = chunkSection.getType(bp.getX(), bp.getY(), bp.getZ());
                    if (blockData.getBlock() != Blocks.AIR) {
                        Location location = new Location(chunkPosition.getWorld(),
                                (chunkPosition.getX() << 4) + bp.getX(),
                                chunkSection.getYPosition() + bp.getY(),
                                (chunkPosition.getZ() << 4) + bp.getZ());

                        int blockAmount = 1;

                        if ((blockData.getBlock().a(TagsBlock.SLABS) || blockData.getBlock().a(TagsBlock.WOODEN_SLABS)) &&
                                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.DOUBLE) {
                            blockAmount = 2;
                            blockData = blockData.set(BlockStepAbstract.a, BlockPropertySlabType.BOTTOM);
                        }

                        Material type = CraftMagicNumbers.getMaterial(blockData.getBlock());
                        Key blockKey = KeyImpl.of(type.name() + "", "0", location);
                        blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + blockAmount);
                        if (type == Material.SPAWNER) {
                            spawnersLocations.add(location);
                        }
                    }
                }
            }
        }

        return new CalculatedChunk(chunkPosition, blockCounts, spawnersLocations);
    }

    private static void removeEntities(Chunk chunk) {
        Collection<Entity>[] entitySlices = null;
        Function<Void, Collection<Entity>> entitySliceCreationFunction = null;

        try {
            entitySlices = chunk.entitySlices;
            entitySliceCreationFunction = v -> new UnsafeList<>();
        } catch (Throwable ex) {
            try {
                entitySlices = ENTITY_SLICE_ARRAY.get(chunk);
                entitySliceCreationFunction = v -> new net.minecraft.server.v1_16_R3.EntitySlice<>(Entity.class);
            } catch (Exception ex2) {
                ex2.printStackTrace();
                PluginDebugger.debug(ex2);
            }
        }

        if (entitySlices != null) {
            for (int i = 0; i < entitySlices.length; i++) {
                entitySlices[i].forEach(entity -> {
                    if (!(entity instanceof EntityHuman))
                        entity.dead = true;
                });
                entitySlices[i] = entitySliceCreationFunction.apply(null);
            }
        }
    }

    private static void removeBlocks(Chunk chunk) {
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        WorldServer worldServer = chunk.world;

        if (worldServer.generator != null && !(worldServer.generator instanceof IslandsGenerator)) {
            CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer,
                    worldServer.getChunkProvider().chunkGenerator, worldServer.generator);
            ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);
            customChunkGenerator.buildBase(null, protoChunk);

            for (int i = 0; i < 16; i++)
                chunk.getSections()[i] = protoChunk.getSections()[i];

            for (Map.Entry<BlockPosition, TileEntity> entry : protoChunk.x().entrySet())
                worldServer.setTileEntity(entry.getKey(), entry.getValue());
        }
    }

}
