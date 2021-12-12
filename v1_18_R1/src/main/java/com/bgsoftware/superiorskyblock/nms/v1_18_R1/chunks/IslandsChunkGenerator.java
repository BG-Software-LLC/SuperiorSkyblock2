package com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.block.ITileEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.generator.CraftChunkData;
import org.bukkit.craftbukkit.v1_18_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

public final class IslandsChunkGenerator extends CustomChunkGenerator {

    private final Random random = new Random();
    private final WorldServer worldServer;

    public IslandsChunkGenerator(WorldServer worldServer) {
        super(worldServer, getChunkProvider(worldServer).g(), worldServer.generator);
        this.worldServer = worldServer;
    }

    @Override
    public void a(RegionLimitedWorldAccess region, StructureManager structureManager, IChunkAccess chunk) {
        int chunkX = getPos(chunk).c;
        int chunkZ = getPos(chunk).d;

        this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        IslandsBiomeGrid biomeGrid = new IslandsBiomeGrid(worldServer, chunk);

        ChunkGenerator.ChunkData data;
        //noinspection deprecation
        if (worldServer.generator.isParallelCapable()) {
            //noinspection deprecation
            data = worldServer.generator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
        } else {
            synchronized (this) {
                //noinspection deprecation
                data = worldServer.generator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
            }
        }

        Preconditions.checkArgument(data instanceof CraftChunkData || data.getClass().getName().equals("OldCraftChunkData"),
                "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);

        assert data instanceof CraftChunkData;

        IChunkAccess chunkAccess = ((CraftChunkData) data).getHandle();

        ChunkSection[] chunkDataSections = getSections(chunkAccess);
        ChunkSection[] chunkSections = getSections(chunk);
        int chunkSectionsLength = Math.min(chunkSections.length, chunkDataSections.length);

        for (int i = 0; i < chunkSectionsLength; i++) {
            if (chunkDataSections[i] != null)
                chunkSections[i] = chunkDataSections[i];
        }

        for (BlockPosition tilePosition : getTileEntities(chunkAccess).keySet()) {
            int tileX = getX(tilePosition), tileY = getY(tilePosition), tileZ = getZ(tilePosition);
            IBlockData tileBlock = ((CraftChunkData) data).getTypeId(tileX, tileY, tileZ);
            if (isTileEntity(tileBlock)) {
                BlockPosition worldTilePosition = new BlockPosition((chunkX << 4) + tileX, tileY, (chunkZ << 4) + tileZ);
                TileEntity tile = createTile(((ITileEntity) getBlock(tileBlock)), worldTilePosition, tileBlock);
                if (tile != null)
                    setTileEntity(chunk, tile);
            }
        }
    }

    @SuppressWarnings({"NullableProblems", "deprecation"})
    private record IslandsBiomeGrid(WorldServer worldServer,
                                    IChunkAccess chunkAccess) implements ChunkGenerator.BiomeGrid {

        @Override
        public Biome getBiome(int x, int z) {
            return this.getBiome(x, 0, z);
        }

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBlock.biomeBaseToBiome(chunkAccess.biomeRegistry, chunkAccess.getNoiseBiome(x, y, z));
        }

        @Override
        public void setBiome(int x, int z, Biome biome) {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(chunkAccess.biomeRegistry, biome);
            for (int y = getMinBuildHeight(worldServer); y < getMaxBuildHeight(worldServer); y++) {
                chunkAccess.setBiome(x, y, z, biomeBase);
            }
        }

        @Override
        public void setBiome(int x, int y, int z, Biome biome) {
            Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot set the biome to %s", biome);
            chunkAccess.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(chunkAccess.biomeRegistry, biome));
        }

    }

}
