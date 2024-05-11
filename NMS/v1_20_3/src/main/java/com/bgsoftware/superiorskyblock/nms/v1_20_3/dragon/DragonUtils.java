package com.bgsoftware.superiorskyblock.nms.v1_20_3.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;

import java.lang.reflect.Modifier;

public class DragonUtils {

    private static final ReflectField<BlockPos> END_PODIUM_LOCATION = new ReflectField<BlockPos>(
            EndPodiumFeature.class, BlockPos.class,
            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static BlockPos currentPodiumPosition = BlockPos.ZERO;

    private DragonUtils() {

    }

    public static void runWithPodiumPosition(BlockPos podiumPosition, Runnable runnable) {
        try {
            END_PODIUM_LOCATION.set(null, podiumPosition);
            currentPodiumPosition = podiumPosition;
            runnable.run();
        } finally {
            END_PODIUM_LOCATION.set(null, BlockPos.ZERO);
            currentPodiumPosition = BlockPos.ZERO;
        }
    }

    public static BlockPos getCurrentPodiumPosition() {
        return currentPodiumPosition;
    }

}
