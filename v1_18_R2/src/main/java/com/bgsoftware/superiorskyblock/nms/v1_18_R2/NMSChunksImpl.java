package com.bgsoftware.superiorskyblock.nms.v1_18_R2;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.chunks.CropsTickingTileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.SectionPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.chunk.ChunkSection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.lighting.LightEngine;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.lighting.LightEngineLayerEventListener;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.nbt.NBTTagList;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.network.PlayerConnection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.tags.TagsBlock;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.Entity;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.world.chunks.CalculatedChunk;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.world.generator.IslandsGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.DynamicOpsNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BlockStepAbstract;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.properties.BlockPropertySlabType;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataPaletteBlock;
import net.minecraft.world.level.chunk.NibbleArray;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings({"ConstantConditions", "deprecation"})
public final class NMSChunksImpl implements NMSChunks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition,
                                                  net.minecraft.world.level.chunk.ChunkSection[] chunkSections) {
        KeyMap<Integer> blockCounts = new KeyMap<>();
        Set<Location> spawnersLocations = new HashSet<>();

        for (net.minecraft.world.level.chunk.ChunkSection nmsSection : chunkSections) {
            ChunkSection chunkSection = ChunkSection.ofNullable(nmsSection);
            if (chunkSection != null) {
                for (BlockPosition blockPosition : BlockPosition.allBlocksBetween(0, 0, 0, 15, 15, 15)) {
                    BlockData blockData = chunkSection.getType(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                    Block block = blockData.getBlock();

                    if (block.getHandle() != Blocks.a) {
                        Location location = new Location(chunkPosition.getWorld(),
                                (chunkPosition.getX() << 4) + blockPosition.getX(),
                                chunkSection.getYPosition() + blockPosition.getY(),
                                (chunkPosition.getZ() << 4) + blockPosition.getZ());

                        int blockAmount = 1;

                        if ((TagsBlock.isTagged(TagsBlock.SLABS, block) || TagsBlock.isTagged(TagsBlock.WOODEN_SLABS, block)) &&
                                blockData.get(BlockStepAbstract.a) == BlockPropertySlabType.c) {
                            blockAmount = 2;
                            blockData = blockData.set(BlockStepAbstract.a, BlockPropertySlabType.b);
                        }

                        Material type = CraftMagicNumbers.getMaterial(blockData.getBlock().getHandle());
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

    private static void removeEntities(ChunkAccess chunk) {
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        WorldServer worldServer = chunk.getWorld();

        int minBuildHeight = worldServer.getWorld().getMinHeight();
        int maxBuildHeight = worldServer.getWorld().getMaxHeight();

        net.minecraft.world.phys.AxisAlignedBB chunkBounds = new net.minecraft.world.phys.AxisAlignedBB(
                chunkCoords.getX() << 4, minBuildHeight, chunkCoords.getZ() << 4,
                chunkCoords.getX() << 4 + 15, maxBuildHeight, chunkCoords.getZ() << 4 + 15
        );

        List<Entity> worldEntities = new ArrayList<>();
        worldServer.getEntities().getAll().forEach(nmsEntity -> {
            Entity entity = new Entity(nmsEntity);
            if (entity.getBoundingBox().intercepts(chunkBounds))
                worldEntities.add(entity);
        });

        for (Entity worldEntity : worldEntities) {
            if (!(worldEntity.getHandle() instanceof EntityHuman))
                worldEntity.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.b);
        }
    }

    private static void removeBlocks(ChunkAccess chunk) {
        WorldServer worldServer = chunk.getWorld();

        ChunkGenerator bukkitGenerator = worldServer.getWorld().getGenerator();

        if (bukkitGenerator != null && !(bukkitGenerator instanceof IslandsGenerator)) {
            CustomChunkGenerator chunkGenerator = new CustomChunkGenerator(worldServer.getHandle(),
                    worldServer.getChunkProvider().getGenerator(),
                    bukkitGenerator);

            RegionLimitedWorldAccess region = new RegionLimitedWorldAccess(worldServer.getHandle(),
                    Collections.singletonList(chunk.getHandle()), ChunkStatus.h, 0);

            chunkGenerator.a(region,
                    worldServer.getStructureManager().getStructureManager(region).getHandle(),
                    chunk.getHandle());
        }
    }

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        WorldServer worldServer = new WorldServer(((CraftWorld) chunkPositions.get(0).getWorld()).getHandle());
        IRegistry<BiomeBase> biomesRegistry = worldServer.getBiomeRegistry();
        Registry<Holder<BiomeBase>> biomesRegistryHolder = worldServer.getBiomeRegistryHolder();
        Holder<BiomeBase> biomeBase = CraftBlock.biomeToBiomeBase(biomesRegistry, biome);

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, null, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();

            net.minecraft.world.level.chunk.ChunkSection[] chunkSections = chunk.getSections();
            for (int i = 0; i < chunkSections.length; ++i) {
                ChunkSection currentSection = new ChunkSection(chunkSections[i]);
                if (currentSection != null) {
                    DataPaletteBlock<IBlockData> dataPaletteBlock = currentSection.getBlocks();
                    chunkSections[i] = new net.minecraft.world.level.chunk.ChunkSection(
                            currentSection.getYPosition() >> 4, dataPaletteBlock,
                            new DataPaletteBlock<>(biomesRegistryHolder, biomeBase, DataPaletteBlock.e.e));
                }
            }

            chunk.setNeedsSaving(true);

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.getX(), chunkCoords.getZ());
            ClientboundLevelChunkWithLightPacket mapChunkPacket = new ClientboundLevelChunkWithLightPacket(
                    (Chunk) chunk.getHandle(), worldServer.getLightEngine().getHandle(), null, null, true);

            playersToUpdate.forEach(player -> {
                Entity playerEntity = new Entity(((CraftPlayer) player).getHandle());
                PlayerConnection playerConnection = playerEntity.getPlayerConnection();
                playerConnection.sendPacket(unloadChunkPacket);
                playerConnection.sendPacket(mapChunkPacket);
            });
        }, unloadedChunkCompound -> {
            Codec<DataPaletteBlock<Holder<BiomeBase>>> codec = DataPaletteBlock.a(biomesRegistryHolder,
                    biomesRegistry.p(), DataPaletteBlock.e.e, biomesRegistry.g(Biomes.b));
            DataResult<NBTBase> dataResult = codec.encodeStart(DynamicOpsNBT.a,
                    new DataPaletteBlock<>(biomesRegistryHolder, biomeBase, DataPaletteBlock.e.e));
            NBTBase biomesCompound = dataResult.getOrThrow(false, error -> {
            });

            NBTTagList sectionsList = unloadedChunkCompound.getSections();

            for (int i = 0; i < sectionsList.size(); ++i)
                sectionsList.getCompound(i).set("biomes", biomesCompound);
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

        WorldServer worldServer = new WorldServer(((CraftWorld) chunkPositions.get(0).getWorld()).getHandle());

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, onFinish, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();

            IRegistry<BiomeBase> biomesRegistry = worldServer.getBiomeRegistry();

            net.minecraft.world.level.chunk.ChunkSection[] chunkSections = chunk.getSections();
            for (int i = 0; i < chunkSections.length; ++i)
                chunkSections[i] = new net.minecraft.world.level.chunk.ChunkSection(i, biomesRegistry);

            removeEntities(chunk);

            chunk.getTileEntities().keySet().forEach(worldServer::removeTileEntity);
            chunk.getTilePositions().clear();

            removeBlocks(chunk);

            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.getX(), chunkCoords.getZ(),
                    new ClientboundLevelChunkWithLightPacket((Chunk) chunk.getHandle(),
                            worldServer.getLightEngine().getHandle(), null, null, true));
        }, unloadedChunkCompound -> {
            Codec<DataPaletteBlock<IBlockData>> blocksCodec = DataPaletteBlock.a(Block.CODEC, IBlockData.b,
                    DataPaletteBlock.e.d, Block.AIR.getBlockData().getHandle());

            NBTTagList tileEntities = new NBTTagList();

            unloadedChunkCompound.setEntities(new NBTTagList());
            unloadedChunkCompound.setBlockEntities(tileEntities);

            if (worldServer.getBukkitGenerator() instanceof IslandsGenerator) {
                DataResult<NBTBase> dataResult = blocksCodec.encodeStart(DynamicOpsNBT.a,
                        new DataPaletteBlock<>(Block.CODEC, Block.AIR.getBlockData().getHandle(), DataPaletteBlock.e.d));
                NBTBase blockStatesCompound = dataResult.getOrThrow(false, error -> {
                });

                NBTTagList sectionsList = unloadedChunkCompound.getSections();
                for (int i = 0; i < sectionsList.size(); ++i) {
                    NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                    sectionCompound.set("block_states", blockStatesCompound);
                }
            } else {
                ChunkAccess protoChunk = NMSUtils.createProtoChunk(unloadedChunkCompound.getChunkCoords(), worldServer);

                try {
                    CustomChunkGenerator customChunkGenerator = new CustomChunkGenerator(worldServer.getHandle(),
                            worldServer.getChunkProvider().getGenerator(), worldServer.getBukkitGenerator());
                    customChunkGenerator.a(null, null, protoChunk.getHandle());
                } catch (Exception ignored) {
                }

                IRegistry<BiomeBase> biomesRegistry = worldServer.getBiomeRegistry();
                Registry<Holder<BiomeBase>> biomesRegistryHolder = worldServer.getBiomeRegistryHolder();
                Codec<DataPaletteBlock<Holder<BiomeBase>>> biomesCodec = DataPaletteBlock.a(biomesRegistryHolder,
                        biomesRegistry.p(), DataPaletteBlock.e.e, biomesRegistry.g(Biomes.b));

                LightEngine lightEngine = worldServer.getLightEngine();
                net.minecraft.world.level.chunk.ChunkSection[] chunkSections = protoChunk.getSections();

                NBTTagList sectionsList = new NBTTagList();

                // Save blocks
                for (int i = lightEngine.getMinSection(); i < lightEngine.getMaxSection(); ++i) {
                    int chunkSectionIndex = worldServer.getSectionIndex(i);

                    NBTTagCompound sectionCompound = new NBTTagCompound();

                    if (chunkSectionIndex >= 0 && chunkSectionIndex < chunkSections.length) {
                        ChunkSection chunkSection = new ChunkSection(chunkSections[chunkSectionIndex]);

                        {
                            DataResult<NBTBase> dataResult = blocksCodec.encodeStart(DynamicOpsNBT.a, chunkSection.getBlocks());
                            sectionCompound.set("block_states", dataResult.getOrThrow(false, error -> {
                            }));
                        }

                        {
                            DataResult<NBTBase> dataResult = biomesCodec.encodeStart(DynamicOpsNBT.a, chunkSection.getBiomes());
                            sectionCompound.set("biomes", dataResult.getOrThrow(false, error -> {
                            }));
                        }
                    }

                    if (!sectionCompound.isEmpty()) {
                        sectionCompound.setByte("Y", (byte) i);
                        sectionsList.add(sectionCompound.getHandle());
                    }
                }

                for (BlockPosition tilePosition : protoChunk.getTileEntities().keySet()) {
                    NBTTagCompound tileCompound = protoChunk.getTileEntityNBT(tilePosition);
                    if (tileCompound != null)
                        tileEntities.add(tileCompound.getHandle());
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
        WorldServer worldServer = new WorldServer(((CraftWorld) chunkPositions.get(0).getWorld()).getHandle());

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, false, () -> {
            completableFuture.complete(allCalculatedChunks);
        }, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();
            ChunkPosition chunkPosition = ChunkPosition.of(chunk.getWorld().getWorld(), chunkCoords.getX(), chunkCoords.getZ());
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunk.getSections()));
        }, unloadedChunkCompound -> {
            IRegistry<BiomeBase> biomesRegistry = worldServer.getBiomeRegistry();
            Registry<Holder<BiomeBase>> biomesRegistryHolder = worldServer.getBiomeRegistryHolder();

            Codec<DataPaletteBlock<IBlockData>> blocksCodec = DataPaletteBlock.a(Block.CODEC, IBlockData.b,
                    DataPaletteBlock.e.d, Blocks.a.n());
            Codec<DataPaletteBlock<Holder<BiomeBase>>> biomesCodec = DataPaletteBlock.a(biomesRegistryHolder,
                    biomesRegistry.p(), DataPaletteBlock.e.e, biomesRegistry.g(Biomes.b));

            net.minecraft.world.level.chunk.ChunkSection[] chunkSections =
                    new net.minecraft.world.level.chunk.ChunkSection[worldServer.getSectionsAmount()];

            NBTTagList sectionsList = unloadedChunkCompound.getSections();
            for (int i = 0; i < sectionsList.size(); ++i) {
                NBTTagCompound sectionCompound = sectionsList.getCompound(i);
                byte yPosition = sectionCompound.getByte("Y");
                int sectionIndex = worldServer.getSectionIndexFromSectionY(yPosition);

                if (sectionIndex >= 0 && sectionIndex < chunkSections.length) {
                    DataPaletteBlock<IBlockData> blocksDataPalette;
                    if (sectionCompound.hasKeyOfType("block_states", 10)) {
                        DataResult<DataPaletteBlock<IBlockData>> dataResult = blocksCodec.parse(DynamicOpsNBT.a,
                                sectionCompound.getCompound("block_states").getHandle()).promotePartial((sx) -> {
                        });
                        blocksDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        blocksDataPalette = new DataPaletteBlock<>(Block.CODEC, Blocks.a.n(), DataPaletteBlock.e.d);
                    }

                    DataPaletteBlock<Holder<BiomeBase>> biomesDataPalette;
                    if (sectionCompound.hasKeyOfType("biomes", 10)) {
                        DataResult<DataPaletteBlock<Holder<BiomeBase>>> dataResult = biomesCodec.parse(DynamicOpsNBT.a,
                                sectionCompound.getCompound("biomes").getHandle()).promotePartial((sx) -> {
                        });
                        biomesDataPalette = dataResult.getOrThrow(false, error -> {
                        });
                    } else {
                        biomesDataPalette = new DataPaletteBlock<>(biomesRegistryHolder, biomesRegistry.g(Biomes.b),
                                DataPaletteBlock.e.e);
                    }

                    chunkSections[sectionIndex] = new net.minecraft.world.level.chunk.ChunkSection(
                            yPosition, blocksDataPalette, biomesDataPalette);
                }

            }

            ChunkCoordIntPair chunkCoords = unloadedChunkCompound.getChunkCoords();
            ChunkPosition chunkPosition = ChunkPosition.of(worldServer.getWorld(), chunkCoords.getX(), chunkCoords.getZ());
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
        ChunkAccess chunk = new ChunkAccess(((CraftChunk) bukkitChunk).getHandle());
        return Arrays.stream(chunk.getSections()).allMatch(chunkSection ->
                chunkSection == null || new ChunkSection(chunkSection).isEmpty());
    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        ChunkAccess chunk = new ChunkAccess(((CraftChunk) bukkitChunk).getHandle());
        WorldServer worldServer = chunk.getWorld();
        ChunkCoordIntPair chunkCoords = chunk.getPos();
        NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.getX(), chunkCoords.getZ(),
                new ClientboundLevelChunkWithLightPacket((Chunk) chunk.getHandle(), worldServer.getLightEngine().getHandle(),
                        null, null, true));
    }

    @Override
    public void refreshLights(org.bukkit.Chunk bukkitChunk,
                              List<com.bgsoftware.superiorskyblock.world.blocks.BlockData> blockDataList) {
        ChunkAccess chunk = new ChunkAccess(((CraftChunk) bukkitChunk).getHandle());
        WorldServer world = chunk.getWorld();

        // Update lights for the blocks.
        // We use a delayed task to avoid null nibbles
        Executor.sync(() -> {
            boolean canSkyLight = bukkitChunk.getWorld().getEnvironment() == org.bukkit.World.Environment.NORMAL;
            LightEngine lightEngine = world.getLightEngine();
            LightEngineLayerEventListener blocksLightLayer = lightEngine.getLayer(EnumSkyBlock.b);
            LightEngineLayerEventListener skyLightLayer = lightEngine.getLayer(EnumSkyBlock.a);

            if (plugin.getSettings().isLightsUpdate()) {
                for (com.bgsoftware.superiorskyblock.world.blocks.BlockData blockData : blockDataList) {
                    BlockPosition blockPosition = new BlockPosition(blockData.getX(), blockData.getY(), blockData.getZ());
                    if (blockData.getBlockLightLevel() > 0) {
                        blocksLightLayer.flagDirty(blockPosition, blockData.getBlockLightLevel());
                    }
                    if (canSkyLight && blockData.getSkyLightLevel() > 0) {
                        skyLightLayer.flagDirty(blockPosition, blockData.getSkyLightLevel());
                    }
                }
            } else if (canSkyLight) {
                int sectionsAmount = chunk.getSections().length;
                ChunkCoordIntPair chunkCoords = chunk.getPos();

                for (int i = 0; i < sectionsAmount; ++i) {
                    byte[] skyLightArray = new byte[2048];
                    for (int j = 0; j < skyLightArray.length; j += 2)
                        skyLightArray[j] = 15;
                    lightEngine.queueData(EnumSkyBlock.a, SectionPosition.getByIndex(chunkCoords, i),
                            new NibbleArray(skyLightArray), true);
                }
            }
        }, 10L);
    }

    @Override
    public org.bukkit.Chunk getChunkIfLoaded(ChunkPosition chunkPosition) {
        WorldServer worldServer = new WorldServer(((CraftWorld) chunkPosition.getWorld()).getHandle());
        ChunkAccess chunk = worldServer.getChunkProvider().getChunkAt(chunkPosition.getX(), chunkPosition.getZ(), false);
        return chunk == null ? null : chunk.getBukkitChunk();
    }

    @Override
    public void startTickingChunk(Island island, org.bukkit.Chunk chunk, boolean stop) {
        if (plugin.getSettings().getCropsInterval() <= 0)
            return;

        ChunkAccess chunkAccess = new ChunkAccess(((CraftChunk) chunk).getHandle());

        if (stop) {
            CropsTickingTileEntity cropsTickingTileEntity = CropsTickingTileEntity.remove(chunkAccess.getPos());
            if (cropsTickingTileEntity != null)
                cropsTickingTileEntity.remove();
        } else {
            CropsTickingTileEntity.create(island, chunkAccess);
        }
    }

}
