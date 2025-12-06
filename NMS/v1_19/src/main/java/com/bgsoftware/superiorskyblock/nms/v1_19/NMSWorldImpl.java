package com.bgsoftware.superiorskyblock.nms.v1_19;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.v1_19.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_19.vibration.IslandSculkSensorBlockEntity;
import com.bgsoftware.superiorskyblock.world.SignType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.bukkit.Location;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.generator.CustomChunkGenerator;

public class NMSWorldImpl extends com.bgsoftware.superiorskyblock.nms.v1_19.AbstractNMSWorld {

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected Component[] getSignBlockEntityText(SignBlockEntity signBlockEntity) {
        return signBlockEntity.messages;
    }

    @Override
    protected ChunkGenerator getChunkGeneratorDelegate(CustomChunkGenerator chunkGenerator) {
        return chunkGenerator.getDelegate();
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
        return noiseBasedChunkGenerator.settings.value();
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
    public SignType getSignType(Object sign) {
        if (sign instanceof HangingSign)
            return SignType.HANGING_SIGN;
        else if (sign instanceof WallHangingSign)
            return SignType.HANGING_WALL_SIGN;
        else
            return super.getSignType(sign);
    }

}
