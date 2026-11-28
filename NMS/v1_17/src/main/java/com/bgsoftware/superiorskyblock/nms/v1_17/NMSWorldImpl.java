package com.bgsoftware.superiorskyblock.nms.v1_17;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.nms.v1_17.vibration.IslandSculkSensorBlockEntity;
import com.bgsoftware.superiorskyblock.nms.v1_17.world.BlockServerTickListTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.generator.CustomChunkGenerator;

public class NMSWorldImpl extends com.bgsoftware.superiorskyblock.nms.v1_17.AbstractNMSWorld {

    private static boolean alreadyWarned = false;

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void lerpSizeBetween(WorldBorder worldBorder, double oldSize, double newSize) {
        worldBorder.lerpSizeBetween(oldSize, newSize, Long.MAX_VALUE);
    }

    @Override
    protected Component[] getSignBlockEntityText(SignBlockEntity signBlockEntity) {
        return signBlockEntity.messages;
    }

    @Override
    protected ChunkGenerator getChunkGeneratorDelegate(CustomChunkGenerator chunkGenerator) {
        return chunkGenerator.delegate;
    }

    @Override
    protected FlatLevelSource createFlatLevelSource(FlatLevelSource original, int seaLevel) {
        return new FlatLevelSource(original.settings()) {
            @Override
            public int getSeaLevel() {
                return seaLevel;
            }
        };
    }

    @Override
    protected NoiseGeneratorSettings getNoiseGeneratorSettings(NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
        return noiseBasedChunkGenerator.settings.get();
    }

    @Override
    public void replaceSculkSensorListener(Island island, Location location) {
        SculkSensorBlockEntity sculkSensorBlockEntity = NMSUtils.getBlockEntityAt(location, SculkSensorBlockEntity.class);
        if (sculkSensorBlockEntity == null || sculkSensorBlockEntity instanceof IslandSculkSensorBlockEntity)
            return;

        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        serverLevel.removeBlockEntity(sculkSensorBlockEntity.getBlockPos());

        serverLevel.setBlockEntity(new IslandSculkSensorBlockEntity(island, sculkSensorBlockEntity));
    }

    @Override
    public void listenBlockStateChanges(World world) {
        ServerLevel serverLevel = ((CraftWorld) world).getHandle();
        BlockServerTickListTracker.listenForTicks(serverLevel);
        if (!alreadyWarned) {
            Log.warn("This version is old and you may experience issues with block changes detection");
            alreadyWarned = true;
        }
    }

}
