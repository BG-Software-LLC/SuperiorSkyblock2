package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks.IslandsChunkGenerator;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockData;
import com.bgsoftware.superiorskyblock.utils.chunks.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.LightEngineThreaded;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockStepAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertySlabType;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LightEngineGraph;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

public final class NMSChunksImpl implements NMSChunks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(
            LightEngineGraph.class, "a", Long.class, Long.class, Integer.class, Boolean.class);

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, ChunkSection[] chunkSections) {
        KeyMap<Integer> blockCounts = new KeyMap<>();
        Set<Location> spawnersLocations = new HashSet<>();

        for (ChunkSection chunkSection : chunkSections) {
            if (chunkSection != null) {
                for (BlockPosition bp : BlockPosition.b(0, 0, 0, 15, 15, 15)) {
                    IBlockData blockData = getType(chunkSection, getX(bp), getY(bp), getZ(bp));
                    Block block = getBlock(blockData);

                    if (block != Blocks.a) {
                        Location location = new Location(chunkPosition.getWorld(),
                                (chunkPosition.getX() << 4) + getX(bp),
                                getYPosition(chunkSection) + getY(bp),
                                (chunkPosition.getZ() << 4) + getZ(bp));

                        int blockAmount = 1;

                        if ((isTagged(TagsBlock.E, block) || isTagged(TagsBlock.j, block)) &&
                                get(blockData, BlockStepAbstract.a) == BlockPropertySlabType.c) {
                            blockAmount = 2;
                            blockData = set(blockData, BlockStepAbstract.a, BlockPropertySlabType.b);
                        }

                        Material type = CraftMagicNumbers.getMaterial(getBlock(blockData));
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
        ChunkCoordIntPair chunkCoords = getPos(chunk);
        WorldServer worldServer = getWorld(chunk);

        AxisAlignedBB chunkBounds = new AxisAlignedBB(
                chunkCoords.c << 4, getMinBuildHeight(worldServer), chunkCoords.d << 4,
                chunkCoords.c << 4 + 15, getMaxBuildHeight(worldServer), chunkCoords.d << 4 + 15
        );

        List<Entity> worldEntities = new ArrayList<>();
        getEntities(worldServer).a().forEach(entity -> {
            if (getBoundingBox(entity).c(chunkBounds))
                worldEntities.add(entity);
        });

        for (Entity worldEntity : worldEntities) {
            setRemoved(worldEntity, Entity.RemovalReason.b);
        }
    }

    private static void removeBlocks(Chunk chunk) {
        ChunkCoordIntPair chunkCoords = getPos(chunk);
        WorldServer worldServer = getWorld(chunk);

        if (!(worldServer.generator instanceof IslandsGenerator)) {
            IslandsChunkGenerator chunkGenerator = new IslandsChunkGenerator(worldServer);
            ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);
            chunkGenerator.a(null, null, protoChunk);

            ChunkSection[] chunkSections = getSections(protoChunk);
            System.arraycopy(chunkSections, 0, getSections(chunk), 0, chunkSections.length);

            getTileEntities(protoChunk).values().forEach(tileEntity -> setTileEntity(worldServer, tileEntity));
        }
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();
        IRegistry<BiomeBase> biomesRegistry = getCustomRegistry(worldServer).d(IRegistry.aR);
        BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(biomesRegistry, biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            ChunkCoordIntPair chunkCoords = getPos(chunk);

            ChunkSection[] chunkSections = getSections(chunk);
            for (int i = 0; i < chunkSections.length; ++i) {
                ChunkSection currentSection = chunkSections[i];
                if (currentSection != null) {
                    DataPaletteBlock<IBlockData> dataPaletteBlock = getBlocks(currentSection);
                    chunkSections[i] = new ChunkSection(getYPosition(currentSection) >> 4, dataPaletteBlock,
                            new DataPaletteBlock<>(biomesRegistry, biomeBase, DataPaletteBlock.e.e));
                }
            }

            setNeedsSaving(chunk, true);

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.c, chunkCoords.d);
            //PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk);
            // TODO

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
                sendPacket(playerConnection, unloadChunkPacket);
            });
        }, unloadedChunkCompound -> {
            Codec<DataPaletteBlock<BiomeBase>> codec = DataPaletteBlock.a(biomesRegistry, biomesRegistry.i(),
                    DataPaletteBlock.e.e, biomesRegistry.d(Biomes.b));
            DataResult<NBTBase> dataResult = codec.encodeStart(DynamicOpsNBT.a,
                    new DataPaletteBlock<>(biomesRegistry, biomeBase, DataPaletteBlock.e.e));
            NBTBase biomesCompound = dataResult.getOrThrow(false, error -> {
            });

            NBTTagList sectionsList = unloadedChunkCompound.getSections();

            for (int i = 0; i < sectionsList.size(); ++i)
                set(getCompound(sectionsList, i), "biomes", biomesCompound);
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
            ChunkCoordIntPair chunkCoords = getPos(chunk);

            IRegistry<BiomeBase> biomesRegistry = getCustomRegistry(worldServer).d(IRegistry.aR);

            ChunkSection[] chunkSections = getSections(chunk);
            for (int i = 0; i < chunkSections.length; ++i)
                chunkSections[i] = new ChunkSection(i, biomesRegistry);

            removeEntities(chunk);

            WorldServer chunkWorld = getWorld(chunk);

            new HashSet<>(chunk.i.keySet()).forEach(blockPosition -> removeTileEntity(chunkWorld, blockPosition));
            chunk.i.clear();

            removeBlocks(chunk);

            //NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.c, chunkCoords.d, new PacketPlayOutMapChunk(chunk));
            // TODO
        }, unloadedChunkCompound -> {
            Codec<DataPaletteBlock<IBlockData>> blocksCodec = DataPaletteBlock.a(Block.p, IBlockData.b,
                    DataPaletteBlock.e.d, Blocks.a.n());

            NBTTagList tileEntities = new NBTTagList();

            unloadedChunkCompound.setEntities(new NBTTagList());
            unloadedChunkCompound.setBlockEntities(tileEntities);

            if (worldServer.generator instanceof IslandsGenerator) {
                DataResult<NBTBase> dataResult = blocksCodec.encodeStart(DynamicOpsNBT.a,
                        new DataPaletteBlock<>(Block.p, Blocks.a.n(), DataPaletteBlock.e.d));
                NBTBase blockStatesCompound = dataResult.getOrThrow(false, error -> {
                });

                NBTTagList sectionsList = unloadedChunkCompound.getSections();
                for (int i = 0; i < sectionsList.size(); ++i) {
                    NBTTagCompound sectionCompound = getCompound(sectionsList, i);
                    set(sectionCompound, "block_states", blockStatesCompound);
                }
            } else {
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.getChunkCoords(), worldServer);

                try {
                    CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer,
                            getChunkProvider(worldServer).g(), worldServer.generator);
                    buildBase(customChunkGenerator, null, protoChunk);
                } catch (Exception ignored) {
                }

                IRegistry<BiomeBase> biomesRegistry = getCustomRegistry(worldServer).d(IRegistry.aR);
                Codec<DataPaletteBlock<BiomeBase>> biomesCodec = DataPaletteBlock.a(biomesRegistry, biomesRegistry.i(),
                        DataPaletteBlock.e.e, biomesRegistry.d(Biomes.b));

                LightEngineThreaded lightEngineThreaded = getChunkProvider(worldServer).a();
                ChunkSection[] chunkSections = getSections(protoChunk);

                NBTTagList sectionsList = new NBTTagList();

                // Save blocks
                for (int i = lightEngineThreaded.c(); i < lightEngineThreaded.d(); ++i) {
                    int chunkSectionIndex = getSectionIndex(protoChunk, i);

                    NBTTagCompound sectionCompound = new NBTTagCompound();

                    if (chunkSectionIndex >= 0 && chunkSectionIndex < chunkSections.length) {
                        ChunkSection chunkSection = chunkSections[chunkSectionIndex];

                        {
                            DataResult<NBTBase> dataResult = blocksCodec.encodeStart(DynamicOpsNBT.a, getBlocks(chunkSection));
                            set(sectionCompound, "block_states", dataResult.getOrThrow(false, error -> {
                            }));
                        }

                        {
                            DataResult<NBTBase> dataResult = biomesCodec.encodeStart(DynamicOpsNBT.a, getBiomes(chunkSection));
                            set(sectionCompound, "biomes", dataResult.getOrThrow(false, error -> {
                            }));
                        }
                    }

                    if (!isEmpty(sectionCompound)) {
                        setByte(sectionCompound, "Y", (byte) i);
                        sectionsList.add(sectionCompound);
                    }
                }

                for (BlockPosition tilePosition : getTileEntities(protoChunk).keySet()) {
                    NBTTagCompound tileCompound = protoChunk.i(tilePosition);
                    if (tileCompound != null)
                        tileEntities.add(tileCompound);
                }

                unloadedChunkCompound.setSections(sectionsList);
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
            ChunkPosition chunkPosition = ChunkPosition.of(getWorld(chunk).getWorld(), getPos(chunk).c, getPos(chunk).d);
            allCalculatedChunks.add(calculateChunk(chunkPosition, getSections(chunk)));
        }, unloadedChunkCompound -> {
            IRegistry<BiomeBase> biomesRegistry = getCustomRegistry(worldServer).d(IRegistry.aR);

            Codec<DataPaletteBlock<IBlockData>> blocksCodec = DataPaletteBlock.a(Block.p, IBlockData.b,
                    DataPaletteBlock.e.d, Blocks.a.n());
            Codec<DataPaletteBlock<BiomeBase>> biomesCodec = DataPaletteBlock.a(biomesRegistry, biomesRegistry.i(),
                    DataPaletteBlock.e.e, biomesRegistry.d(Biomes.b));

            ChunkSection[] chunkSections = new ChunkSection[getSectionsAmount(worldServer)];

            NBTTagList sectionsList = unloadedChunkCompound.getSections();
            for (int i = 0; i < sectionsList.size(); ++i) {
                NBTTagCompound sectionCompound = getCompound(sectionsList, i);
                byte yPosition = getByte(sectionCompound, "Y");
                int sectionIndex = getSectionIndex(worldServer, yPosition);

                if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                    DataPaletteBlock<IBlockData> blocksDataPalette;
                    if (hasKeyOfType(sectionCompound, "block_states", 10)) {
                        DataResult<DataPaletteBlock<IBlockData>> dataResult = blocksCodec.parse(DynamicOpsNBT.a,
                                sectionCompound.p("block_states")).promotePartial((sx) -> {
                        });
                        blocksDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        blocksDataPalette = new DataPaletteBlock<>(Block.p, Blocks.a.n(), DataPaletteBlock.e.d);
                    }

                    DataPaletteBlock<BiomeBase> biomesDataPalette;
                    if (hasKeyOfType(sectionCompound, "biomes", 10)) {
                        DataResult<DataPaletteBlock<BiomeBase>> dataResult = biomesCodec.parse(DynamicOpsNBT.a,
                                sectionCompound.p("biomes")).promotePartial((sx) -> {
                        });
                        biomesDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        biomesDataPalette = new DataPaletteBlock<>(biomesRegistry, biomesRegistry.d(Biomes.b), DataPaletteBlock.e.e);
                    }

                    chunkSections[sectionIndex] = new ChunkSection(yPosition, blocksDataPalette, biomesDataPalette);
                }

            }

            ChunkCoordIntPair chunkCoords = unloadedChunkCompound.getChunkCoords();
            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunkCoords.c, chunkCoords.d);
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
        return Arrays.stream(getSections(chunk)).allMatch(chunkSection -> chunkSection == null || chunkSection.c());
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        ChunkCoordIntPair chunkCoords = getPos(chunk);
        //NMSUtils.sendPacketToRelevantPlayers(getWorld(chunk), chunkCoords.c, chunkCoords.d, packetPlayOutMapChunk);
        // TODO
    }

    @Override
    public void refreshLights(org.bukkit.Chunk bukkitChunk, List<BlockData> blockDataList) {
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
        WorldServer world = getWorld(chunk);

        if (plugin.getSettings().isLightsUpdate()) {
            // Update lights for the blocks.
            // We use a delayed task to avoid null nibbles
            Executor.sync(() -> {
                for (BlockData blockData : blockDataList) {
                    BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                    if (blockData.getBlockLightLevel() > 0) {
                        try {
                            getLightEngine(world).a(EnumSkyBlock.b).a(blockPosition, blockData.getBlockLightLevel());
                        } catch (Exception ignored) {
                        }
                    }
                    if (blockData.getSkyLightLevel() > 0 && bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                        try {
                            SKY_LIGHT_UPDATE.invoke(getLightEngine(world).a(EnumSkyBlock.a), 9223372036854775807L,
                                    asLong(blockPosition), 15 - blockData.getSkyLightLevel(), true);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }, 10L);
        }
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        Chunk chunk = getChunkAt(getChunkProvider(((CraftWorld) chunkPosition.getWorld()).getHandle()),
                chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.bukkitChunk;
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity =
                    CropsTickingTileEntity.remove(getPos(((CraftChunk) chunk).getHandle()));
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntity.remove();
        } else {
            CropsTickingTileEntity.create(island, ((CraftChunk) chunk).getHandle());
        }
    }

}
