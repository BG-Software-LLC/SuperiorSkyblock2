package com.bgsoftware.superiorskyblock.nms.v1_20_3;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_20_3.vibration.IslandVibrationUser;
import com.bgsoftware.superiorskyblock.world.SignType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.bukkit.Location;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.craftbukkit.v1_20_R3.generator.CustomChunkGenerator;

import java.lang.reflect.Modifier;

public class NMSWorldImpl extends com.bgsoftware.superiorskyblock.nms.v1_20_3.AbstractNMSWorld {

    private static final ReflectField<VibrationSystem.User> SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER = new ReflectField<VibrationSystem.User>(
            SculkSensorBlockEntity.class, VibrationSystem.User.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected Component[] getSignBlockEntityText(SignBlockEntity signBlockEntity) {
        return signBlockEntity.getFrontText().getMessages(false);
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
    protected ChunkGenerator getChunkGeneratorDelegate(CustomChunkGenerator chunkGenerator) {
        return chunkGenerator.getDelegate();
    }

    @Override
    protected NoiseGeneratorSettings getNoiseGeneratorSettings(NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
        return noiseBasedChunkGenerator.settings.value();
    }

    @Override
    public void replaceSculkSensorListener(Island island, Location location) {
        SculkSensorBlockEntity sculkSensorBlockEntity = NMSUtils.getBlockEntityAt(location, SculkSensorBlockEntity.class);
        if (sculkSensorBlockEntity == null || sculkSensorBlockEntity.getVibrationUser() instanceof IslandVibrationUser)
            return;

        SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER.set(sculkSensorBlockEntity, new IslandVibrationUser(island, sculkSensorBlockEntity));
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
