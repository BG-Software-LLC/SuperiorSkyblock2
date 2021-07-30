package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.generator.WorldGenerator;
import com.bgsoftware.superiorskyblock.nms.NMSBlocks_v1_17_R1;
import com.bgsoftware.superiorskyblock.nms.NMSChunks;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class NMSChunksImpl implements NMSChunks {

    private static final ReflectField<BiomeBase[]> BIOME_BASE_ARRAY = new ReflectField<>(BiomeStorage.class, BiomeBase[].class, "f");

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

            new HashSet<>(chunk.l.keySet()).forEach(chunk.getWorld()::removeTileEntity);
            chunk.l.clear();

            if (!(worldServer.generator instanceof WorldGenerator)) {
                NMSBlocks_v1_17_R1.IslandsChunkGenerator chunkGenerator = new NMSBlocks_v1_17_R1.IslandsChunkGenerator(worldServer);
                ProtoChunk protoChunk = NMSUtils.createProtoChunk(chunkCoords, worldServer);
                chunkGenerator.buildBase(null, protoChunk);

                for (int i = 0; i < 16; i++)
                    chunk.getSections()[i] = protoChunk.getSections()[i];

                protoChunk.y().values().forEach(worldServer::setTileEntity);
            }

            //noinspection deprecation
            NMSUtils.sendPacketToRelevantPlayers(worldServer, chunkCoords.b, chunkCoords.c, new PacketPlayOutMapChunk(chunk));
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
                            worldServer.getChunkProvider().d, worldServer.generator);
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

}
