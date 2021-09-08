package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import ca.spottedleaf.starlight.light.StarLightInterface;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_17_R1.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_17_R1.chunks.IslandsChunkGenerator;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.chunks.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.util.thread.ThreadedMailbox;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LightEngineGraph;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class NMSChunksImpl implements NMSChunks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(
            BiomeStorage.class, BiomeBase[].class, "f");
    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(
            LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);
    private static final ReflectField<Object> STAR_LIGHT_INTERFACE = new ReflectField<>(
            LightEngineThreaded.class, Object.class, "theLightEngine");
    private static final ReflectField<ThreadedMailbox<Runnable>> LIGHT_ENGINE_EXECUTOR = new ReflectField<>(
            LightEngineThreaded.class, ThreadedMailbox.class, "e");

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

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

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
                playerConnection.sendPacket(unloadChunkPacket);
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

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        chunkPositions.forEach(chunkPosition -> ChunksTracker.markEmpty(island, chunkPosition, false));

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, onFinish, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();

            Arrays.fill(chunk.getSections(), Chunk.a);

            removeEntities(chunk);

            new HashSet<>(chunk.l.keySet()).forEach(chunk.getWorld()::removeTileEntity);
            chunk.l.clear();

            removeBlocks(chunk);

            //noinspection deprecation
            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.b, chunkCoords.c, new PacketPlayOutMapChunk(chunk));
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
    public CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions) {
        CompletableFuture<List<CalculatedChunk>> completableFuture = new CompletableFuture<>();
        List<CalculatedChunk> allCalculatedChunks = new ArrayList<>();

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());
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
                    chunkSections[i] = new ChunkSection(yPosition << 4);
                    chunkSections[i].getBlocks().a(sectionCompound.getList("Palette", 10), sectionCompound.getLongArray("BlockStates"));
                }
            }

            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunkCoords.b, chunkCoords.c);
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunkSections));
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
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        ChunkCoordIntPair chunkCoords = chunk.getPos();

        PacketPlayOutMapChunk packetPlayOutMapChunk;

        try {
            packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, true);
        } catch (Throwable ex) {
            // noinspection deprecation
            packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk);
        }

        NMSUtils.sendPacketToRelevantPlayers((WorldServer) chunk.getWorld(), chunkCoords.b, chunkCoords.c, packetPlayOutMapChunk);
    }

    @Override
    public void refreshLights(org.bukkit.Chunk bukkitChunk, List<BlockData> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        WorldServer world = (WorldServer) chunk.getWorld();

        if (plugin.getSettings().isLightsUpdate()) {
            // Update lights for the blocks.
            // We use a delayed task to avoid null nibbles
            Executor.sync(() -> {
                if (STAR_LIGHT_INTERFACE.isValid()) {
                    LightEngineThreaded lightEngineThreaded = (LightEngineThreaded) world.k_();
                    StarLightInterface starLightInterface = (StarLightInterface) STAR_LIGHT_INTERFACE.get(lightEngineThreaded);
                    ChunkProviderServer chunkProviderServer = world.getChunkProvider();
                    LIGHT_ENGINE_EXECUTOR.get(lightEngineThreaded).a(() ->
                            starLightInterface.relightChunks(Collections.singleton(chunk.getPos()), chunkPos ->
                                    chunkProviderServer.h.execute(() -> NMSUtils.sendPacketToRelevantPlayers(world, chunkPos.b, chunkPos.c,
                                            new PacketPlayOutLightUpdate(chunkPos, lightEngineThreaded, null, null, true))
                                    ), null));
                } else {
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
        KeyMap<Integer> blockCounts = new KeyMap<>();
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
                        Key blockKey = Key.of(type.name() + "", "", location);
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

        AxisAlignedBB chunkBounds = new AxisAlignedBB(
                chunkCoords.b << 4, 0, chunkCoords.c << 4,
                chunkCoords.b << 4 + 15, chunk.getWorld().getMaxBuildHeight(), chunkCoords.c << 4 + 15
        );

        Iterator<Entity> chunkEntities;

        try {
            chunkEntities = chunk.entities.iterator();
        } catch (Throwable ex) {
            List<Entity> worldEntities = new ArrayList<>();
            worldServer.getEntities().a().forEach(entity -> {
                if (entity.getBoundingBox().c(chunkBounds))
                    worldEntities.add(entity);
            });
            chunkEntities = worldEntities.iterator();
        }

        while (chunkEntities.hasNext()) {
            chunkEntities.next().setRemoved(Entity.RemovalReason.b);
        }
    }

    private static void removeBlocks(Chunk chunk) {
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        WorldServer worldServer = (WorldServer) chunk.getWorld();

        if (!(worldServer.generator instanceof IslandsGenerator)) {
            IslandsChunkGenerator chunkGenerator = new IslandsChunkGenerator(worldServer);
            ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);
            //noinspection ConstantConditions
            chunkGenerator.buildBase(null, protoChunk);

            for (int i = 0; i < 16; i++)
                chunk.getSections()[i] = protoChunk.getSections()[i];

            protoChunk.y().values().forEach(worldServer::setTileEntity);
        }
    }

}
