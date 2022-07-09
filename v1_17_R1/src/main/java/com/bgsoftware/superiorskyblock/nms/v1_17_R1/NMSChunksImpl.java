package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import ca.spottedleaf.starlight.light.StarLightInterface;
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
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_17_R1.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.SectionPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.BlockStepAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertySlabType;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.NibbleArray;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.LightEngineGraph;
import net.minecraft.world.level.lighting.LightEngineLayerEventListener;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Modifier;
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

public class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "f");
    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(
            LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);
    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(
            LightEngineThreaded.class, Object.class, "theLightEngine");
    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(
            LightEngineThreaded.class, ThreadedMailbox.class, "e");
    private static final ReflectField<BiomeStorage> CHUNK_BIOME_STORAGE = new ReflectField<>(
            Chunk.class, BiomeStorage.class, Modifier.PRIVATE, 1);

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
        IRegistry<BiomeBase> biomeBaseRegistry = worldServer.t().b(IRegistry.aO);
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomeBaseRegistry, biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            BiomeBase[] biomeBases = BIOME_BASE_ARRAY.get(chunk.getBiomeIndex());

            if (biomeBases == null)
                throw new RuntimeException("Error while receiving biome bases of chunk (" + chunkCoords.b + "," + chunkCoords.c + ").");

            Arrays.fill(biomeBases, biomeBase);
            chunk.markDirty();

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.b, chunkCoords.c);
            //noinspection deprecation
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk);
            PacketPlayOutLightUpdate lightUpdatePacket = new PacketPlayOutLightUpdate(chunkCoords, worldServer.C.getLightEngine(), null, null, true);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(lightUpdatePacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        }, (chunkCoords, unloadedChunk) -> {
            int[] biomes = unloadedChunk.hasKeyOfType("Biomes", 11) ? unloadedChunk.getIntArray("Biomes") : new int[256];
            Arrays.fill(biomes, biomeBaseRegistry.getId(biomeBase));
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

            new HashSet<>(chunk.l.keySet()).forEach(chunk.getWorld()::removeTileEntity);
            chunk.l.clear();

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
                            worldServer.getChunkProvider().d, worldServer.generator);
                    //noinspection ConstantConditions
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
                    NBTTagCompound tileCompound = protoChunk.f(tilePosition);
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
            ChunkPosition chunkPosition = ChunkPosition.of(chunk.getWorld().getWorld(), chunk.getPos().b, chunk.getPos().c);
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunk.getSections()));
        }, (chunkCoords, unloadedChunk) -> {
            NBTTagList sectionsList = unloadedChunk.getList("Sections", 10);
            ChunkSection[] chunkSections = new ChunkSection[sectionsList.size()];

            for (int i = 0; i < sectionsList.size(); ++i) {
                NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                byte yPosition = sectionCompound.getByte("Y");
                if (sectionCompound.hasKeyOfType("Palette", 9) && sectionCompound.hasKeyOfType("BlockStates", 12)) {
                    //noinspection deprecation
                    chunkSections[i] = new ChunkSection(yPosition);
                    chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                }
            }

            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunkCoords.b, chunkCoords.c);
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

        // Update lights for the blocks.
        // We use a delayed task to avoid null nibbles
        BukkitExecutor.sync(() -> {
            boolean canSkyLight = bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL;
            LightEngine lightEngine = world.k_();
            LightEngineLayerEventListener blocksLightLayer = lightEngine.a(EnumSkyBlock.b);
            LightEngineLayerEventListener skyLightLayer = lightEngine.a(EnumSkyBlock.a);

            if (plugin.getSettings().isLightsUpdate()) {
                if (STAR_LIGHT_INTERFACE.isValid()) {
                    LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) lightEngine;
                    StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
                    ChunkProviderServer chunkProviderServer = world.getChunkProvider();
                    LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).a(() ->
                            starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
                                    chunkProviderServer.h.execute(() -> NMSUtils.sendPacketToRelevantPlayers(world, chunkPos.b, chunkPos.c,
                                            new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, null, null, true))
                                    ), null));
                } else {
                    for (SchematicBlock blockData : blockDataList) {
                        BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                        if (blockData.getBlockLightLevel() > 0) {
                            try {
                                blocksLightLayer.a(blockPosition, blockData.getBlockLightLevel());
                            } catch (Exception ignored) {
                            }
                        }
                        if (blockData.getSkyLightLevel() > 0 && bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                            try {
                                SKY_LIGHT_UPDATE.invoke(skyLightLayer, 9223372036854775807L,
                                        blockPosition.asLong(), 15 - blockData.getSkyLightLevel(), true);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } else if (canSkyLight) {
                int sectionsAmount = chunk.getSections().length;
                ChunkCoordIntPair chunkCoords = chunk.getPos();

                for (int i = 0; i < sectionsAmount; ++i) {
                    byte[] skyLightArray = new byte[2048];
                    for (int j = 0; j < skyLightArray.length; j += 2)
                        skyLightArray[j] = 15;
                    lightEngine.a(EnumSkyBlock.a, SectionPosition.a(chunkCoords, i), new NibbleArray(skyLightArray), true);
                }
            }
        }, 10L);
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
            CropsTickingTileEntity cropsTickingTileEntity =
                    CropsTickingTileEntity.remove(((CraftChunk) chunk).getHandle().getPos());
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntity.remove();
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
                    if (blockData.getBlock() != Blocks.a) {
                        Location location = new Location(chunkPosition.getWorld(),
                                (chunkPosition.getX() << 4) + bp.getX(),
                                chunkSection.getYPosition() + bp.getY(),
                                (chunkPosition.getZ() << 4) + bp.getZ());

                        int blockAmount = 1;

                        if ((TagsBlock.E.isTagged(blockData.getBlock()) || TagsBlock.j.isTagged(blockData.getBlock())) &&
                                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.c) {
                            blockAmount = 2;
                            blockData = blockData.set(BlockStepAbstract.a, BlockPropertySlabType.b);
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
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        WorldServer worldServer = (WorldServer) chunk.getWorld();

        int chunkWorldCoordX = chunkCoords.b << 4;
        int chunkWorldCoordZ = chunkCoords.c << 4;

        AxisAlignedBB chunkBounds = new AxisAlignedBB(
                chunkWorldCoordX, 0, chunkWorldCoordZ,
                chunkWorldCoordX + 15, chunk.getWorld().getMaxBuildHeight(), chunkWorldCoordZ + 15);

        Iterator<Entity> chunkEntities;

        try {
            chunkEntities = chunk.entities.iterator();
        } catch (Throwable ex) {
            List<Entity> worldEntities = new LinkedList<>();
            worldServer.getEntities().a(chunkBounds, worldEntities::add);
            chunkEntities = worldEntities.iterator();
        }

        while (chunkEntities.hasNext()) {
            Entity entity = chunkEntities.next();
            if (!(entity instanceof EntityHuman))
                entity.setRemoved(Entity.RemovalReason.b);
        }
    }

    private static void removeBlocks(Chunk chunk) {
        WorldServer worldServer = (WorldServer) chunk.getWorld();

        ChunkGenerator bukkitGenerator = worldServer.getWorld().getGenerator();

        if (bukkitGenerator != null && !(bukkitGenerator instanceof IslandsGenerator)) {
            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(worldServer,
                    worldServer.getChunkProvider().getChunkGenerator(),
                    bukkitGenerator);

            RegionLimitedWorldAccess region = new RegionLimitedWorldAccess(worldServer,
                    Collections.singletonList(chunk), ChunkStatus.f, 0);

            try {
                chunkGenerator.buildBase(region, chunk);
            } catch (ClassCastException error) {
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunk.getPos(), worldServer);
                chunkGenerator.buildBase(region, protoChunk);

                // Load chunk sections from proto chunk to the actual chunk
                for (int i = 0; i < protoChunk.getSections().length && i < chunk.getSections().length; ++i) {
                    chunk.getSections()[i] = protoChunk.getSections()[i];
                }

                // Load biomes from proto chunk to the actual chunk
                if (protoChunk.getBiomeIndex() != null)
                    CHUNK_BIOME_STORAGE.set(chunk, protoChunk.getBiomeIndex());

                // Load tile entities from proto chunk to the actual chunk
                protoChunk.y().forEach(((blockPosition, tileEntity) -> chunk.setTileEntity(tileEntity)));
            }
        }
    }

}
