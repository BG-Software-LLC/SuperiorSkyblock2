package com.bgsoftware.superiorskyblock.nms.v1_17_R1.chunks;

import com.bgsoftware.common.reflection.ReflectField;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.IRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.BiomeStorage;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.generator.CraftChunkData;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;
import java.util.Set;

public final class IslandsChunkGenerator extends CustomChunkGenerator {

    private static final ReflectField<ChunkSection[]> CHUNK_DATA_SECTIONS = new ReflectField<>(
            CraftChunkData.class, ChunkSection[].class, "sections");
    private static final ReflectField<Set<BlockPosition>> CHUNK_DATA_TILES = new ReflectField<>(
            CraftChunkData.class, Set.class, "tiles");

    private final Random random = new Random();
    private final WorldServer worldServer;

    public IslandsChunkGenerator(WorldServer worldServer) {
        super(worldServer, worldServer.getChunkProvider().d, worldServer.generator);
        this.worldServer = worldServer;
    }

    @Override
    public void buildBase(RegionLimitedWorldAccess region, IChunkAccess chunk) {
        int chunkX = chunk.getPos().b;
        int chunkZ = chunk.getPos().c;

        this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        Registry<BiomeBase> biomeBaseRegistry = worldServer.t().d(IRegistry.aO);
        IslandsBiomesStorage biomeStorage = new IslandsBiomesStorage(biomeBaseRegistry, worldServer);
        IslandsBiomeGrid biomeGrid = new IslandsBiomeGrid(worldServer, biomeStorage);

        ChunkGenerator.ChunkData data;
        if (worldServer.generator.isParallelCapable()) {
            data = worldServer.generator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
        } else {
            synchronized (this) {
                data = worldServer.generator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
            }
        }

        Preconditions.checkArgument(data instanceof CraftChunkData, "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);

        ChunkSection[] chunkDataSections = CHUNK_DATA_SECTIONS.get(data);
        ChunkSection[] chunkSections = chunk.getSections();
        int chunkSectionsLength = Math.min(chunkSections.length, chunkDataSections.length);

        for (int i = 0; i < chunkSectionsLength; i++) {
            if (chunkDataSections[i] != null)
                chunkSections[i] = chunkDataSections[i];
        }

        ((ProtoChunk) chunk).a(biomeGrid.biome);

        Set<BlockPosition> tiles = CHUNK_DATA_TILES.get(data);
        if (tiles != null) {
            for (BlockPosition tilePosition : tiles) {
                int tileX = tilePosition.getX(), tileY = tilePosition.getY(), tileZ = tilePosition.getZ();
                IBlockData tileBlock = ((CraftChunkData) data).getTypeId(tileX, tileY, tileZ);
                if (tileBlock.isTileEntity()) {
                    BlockPosition worldTilePosition = new BlockPosition((chunkX << 4) + tileX, tileY, (chunkZ << 4) + tileZ);
                    TileEntity tile = ((ITileEntity) tileBlock.getBlock()).createTile(worldTilePosition, tileBlock);
                    if(tile != null)
                        chunk.setTileEntity(tile);
                }
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    private record IslandsBiomeGrid(WorldServer worldServer,
                                    BiomeStorage biome) implements ChunkGenerator.BiomeGrid {

        @Override
        public Biome getBiome(int x, int z) {
            return this.getBiome(x, 0, z);
        }

        @Override
        public void setBiome(int x, int z, Biome biome) {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase((IRegistry<BiomeBase>) this.biome.e, biome);
            for (int y = worldServer.getMinBuildHeight(); y < worldServer.getMaxBuildHeight(); y += 4) {
                this.biome.setBiome(x >> 2, y >> 2, z >> 2, biomeBase);
            }
        }

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBlock.biomeBaseToBiome((IRegistry<BiomeBase>) this.biome.e, this.biome.getBiome(x >> 2, y >> 2, z >> 2));
        }

        @Override
        public void setBiome(int x, int y, int z, Biome biome) {
            Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot set the biome to %s", biome);
            this.biome.setBiome(x >> 2, y >> 2, z >> 2, CraftBlock.biomeToBiomeBase((IRegistry<BiomeBase>) this.biome.e, biome));
        }

    }

    private static class IslandsBiomesStorage extends BiomeStorage {

        private static final int c = MathHelper.e(16) - 2;

        IslandsBiomesStorage(Registry<BiomeBase> registry, WorldServer worldServer) {
            super(registry, worldServer, new BiomeBase[(1 << c + c) * a(worldServer.getHeight(), 4)]);
        }

        private static int a(int i, int j) {
            return (i + j - 1) / j;
        }

    }

}
