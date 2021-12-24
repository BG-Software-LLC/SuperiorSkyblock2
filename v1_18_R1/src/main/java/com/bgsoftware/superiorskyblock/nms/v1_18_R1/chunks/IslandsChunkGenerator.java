package com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.chunk.ChunkAccess;
import com.google.common.base.Preconditions;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.chunk.IChunkAccess;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_18_R1.generator.CraftChunkData;
import org.bukkit.craftbukkit.v1_18_R1.generator.CustomChunkGenerator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public final class IslandsChunkGenerator extends CustomChunkGenerator {

    private final Random random = new Random();
    private final WorldServer worldServer;

    public IslandsChunkGenerator(WorldServer worldServer) {
        super(worldServer.getHandle(), worldServer.getChunkProvider().getGenerator(), worldServer.getBukkitGenerator());
        this.worldServer = worldServer;
    }

    @Override
    public void a(RegionLimitedWorldAccess region, StructureManager structureManager, IChunkAccess nmsChunkAccess) {
        ChunkAccess chunk = new ChunkAccess(nmsChunkAccess);

        ChunkCoordIntPair chunkCoords = chunk.getPos();
        int chunkX = chunkCoords.getX();
        int chunkZ = chunkCoords.getZ();

        this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        IslandsBiomeGrid biomeGrid = new IslandsBiomeGrid(worldServer, chunk);

        ChunkGenerator chunkGenerator = worldServer.getBukkitGenerator();
        ChunkGenerator.ChunkData data;
        //noinspection deprecation
        if (chunkGenerator.isParallelCapable()) {
            //noinspection deprecation
            data = chunkGenerator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
        } else {
            synchronized (this) {
                //noinspection deprecation
                data = chunkGenerator.generateChunkData(worldServer.getWorld(), this.random, chunkX, chunkZ, biomeGrid);
            }
        }

        Preconditions.checkArgument(data instanceof CraftChunkData || data.getClass().getName().equals("OldCraftChunkData"),
                "Plugins must use createChunkData(World) rather than implementing ChunkData: %s", data);

        assert data instanceof CraftChunkData;

        ChunkAccess chunkAccess = new ChunkAccess(((CraftChunkData) data).getHandle());

        net.minecraft.world.level.chunk.ChunkSection[] chunkDataSections = chunkAccess.getSections();
        ChunkSection[] chunkSections = chunk.getSections();
        int chunkSectionsLength = Math.min(chunkSections.length, chunkDataSections.length);

        for (int i = 0; i < chunkSectionsLength; i++) {
            if (chunkDataSections[i] != null)
                chunkSections[i] = chunkDataSections[i];
        }

        for (BlockPosition tilePosition : chunkAccess.getTileEntities().keySet()) {
            int tileX = tilePosition.getX(), tileY = tilePosition.getY(), tileZ = tilePosition.getZ();
            BlockData tileBlock = new BlockData(((CraftChunkData) data).getTypeId(tileX, tileY, tileZ));
            if (tileBlock.isTileEntity()) {
                BlockPosition worldTilePosition = new BlockPosition((chunkX << 4) + tileX, tileY, (chunkZ << 4) + tileZ);
                TileEntity tile = tileBlock.getBlock().createTile(worldTilePosition, tileBlock);
                if (tile != null)
                    chunk.setTileEntity(tile);
            }
        }
    }

    @SuppressWarnings({"NullableProblems", "deprecation"})
    private record IslandsBiomeGrid(WorldServer worldServer,
                                    ChunkAccess chunkAccess) implements ChunkGenerator.BiomeGrid {

        @Override
        public Biome getBiome(int x, int z) {
            return this.getBiome(x, 0, z);
        }

        @Override
        public Biome getBiome(int x, int y, int z) {
            return CraftBlock.biomeBaseToBiome(chunkAccess.getBiomeRegistry(), chunkAccess.getNoiseBiome(x, y, z));
        }

        @Override
        public void setBiome(int x, int z, Biome biome) {
            BiomeBase biomeBase = CraftBlock.biomeToBiomeBase(chunkAccess.getBiomeRegistry(), biome);
            int minBuildHeight = worldServer.getWorld().getMinHeight();
            int maxBuildHeight = worldServer.getWorld().getMaxHeight();
            for (int y = minBuildHeight; y < maxBuildHeight; ++y) {
                chunkAccess.setBiome(x, y, z, biomeBase);
            }
        }

        @Override
        public void setBiome(int x, int y, int z, Biome biome) {
            Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot set the biome to %s", biome);
            chunkAccess.setBiome(x, y, z, CraftBlock.biomeToBiomeBase(chunkAccess.getBiomeRegistry(), biome));
        }

    }

}
