package com.bgsoftware.superiorskyblock.nms.v1_16_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.objects.CalculatedChunk;
import net.minecraft.server.v1_16_R2.BiomeBase;
import net.minecraft.server.v1_16_R2.BiomeStorage;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.BlockProperties;
import net.minecraft.server.v1_16_R2.BlockPropertySlabType;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.Chunk;
import net.minecraft.server.v1_16_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R2.ChunkSection;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.IRegistry;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagList;
import net.minecraft.server.v1_16_R2.PacketPlayOutMapChunk;
import net.minecraft.server.v1_16_R2.PacketPlayOutUnloadChunk;
import net.minecraft.server.v1_16_R2.PlayerConnection;
import net.minecraft.server.v1_16_R2.ProtoChunk;
import net.minecraft.server.v1_16_R2.TagsBlock;
import net.minecraft.server.v1_16_R2.TileEntity;
import net.minecraft.server.v1_16_R2.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_16_R2.util.UnsafeList;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "h");
    private static final ReflectField<Collection<Entity>[]> ENTITY_SLICE_ARRAY = new ReflectField<>(Chunk.class, null, "entitySlices");

    @Override
    public void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate) {
        if (chunkPositions.isEmpty())
            return;

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

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

            PacketPlayOutUnloadChunk unloadChunkPacket = new PacketPlayOutUnloadChunk(chunkCoords.x, chunkCoords.z);
            PacketPlayOutMapChunk mapChunkPacket = new PacketPlayOutMapChunk(chunk, 65535);

            playersToUpdate.forEach(player -> {
                PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
                playerConnection.sendPacket(unloadChunkPacket);
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

        List<ChunkCoordIntPair> chunksCoords = chunkPositions.stream()
                .map(chunkPosition -> new ChunkCoordIntPair(chunkPosition.getX(), chunkPosition.getZ()))
                .collect(Collectors.toList());

        chunkPositions.forEach(chunkPosition -> ChunksTracker.markEmpty(island, chunkPosition, false));

        WorldServer worldServer = ((CraftWorld) chunkPositions.get(0).getWorld()).getHandle();

        NMSUtils.runActionOnChunks(worldServer, chunksCoords, true, onFinish, chunk -> {
            ChunkCoordIntPair chunkCoords = chunk.getPos();

            Arrays.fill(chunk.getSections(), Chunk.a);
            removeEntities(chunk);

            new HashSet<>(chunk.tileEntities.keySet()).forEach(chunk.world::removeTileEntity);
            chunk.tileEntities.clear();

            removeBlocks(chunk);

            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.x, chunkCoords.z, new PacketPlayOutMapChunk(chunk, 65535));
        }, (chunkCoords, levelCompound) -> {
            NBTTagList sectionsList = new NBTTagList();
            NBTTagList tileEntities = new NBTTagList();

            levelCompound.set("Sections", sectionsList);
            levelCompound.set("TileEntities", tileEntities);
            levelCompound.set("Entities", new NBTTagList());

            if (!(worldServer.generator instanceof WorldGenerator)) {
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
            allCalculatedChunks.add(calculateChunk(chunkPosition, chunkSections));
        });

        return completableFuture;
    }

    private static CalculatedChunk calculateChunk(ChunkPosition chunkPosition, ChunkSection[] chunkSections) {
        KeyMap<Integer> blockCounts = new KeyMap<>();
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
                                blockData.get(BlockProperties.aK) == BlockPropertySlabType.DOUBLE) {
                            blockAmount = 2;
                            blockData = blockData.set(BlockProperties.aK, BlockPropertySlabType.BOTTOM);
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
        Collection<Entity>[] entitySlices = null;
        Function<Void, Collection<Entity>> entitySliceCreationFunction = null;

        try {
            entitySlices = chunk.entitySlices;
            entitySliceCreationFunction = v -> new UnsafeList<>();
        } catch (Throwable ex) {
            try {
                entitySlices = ENTITY_SLICE_ARRAY.get(chunk);
                entitySliceCreationFunction = v -> new net.minecraft.server.v1_16_R2.EntitySlice<>(Entity.class);
            } catch (Exception ex2) {
                ex2.printStackTrace();
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

        if (worldServer.generator != null && !(worldServer.generator instanceof WorldGenerator)) {
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
