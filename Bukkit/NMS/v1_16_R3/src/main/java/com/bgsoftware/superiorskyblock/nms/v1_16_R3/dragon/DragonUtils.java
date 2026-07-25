package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.WorldGenEndTrophy;

import java.lang.reflect.Modifier;

public class DragonUtils {

    private static final ReflectField<BlockPosition> END_PODIUM_LOCATION = new ReflectField<BlockPosition>(
            WorldGenEndTrophy.class, BlockPosition.class,
            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static BlockPosition currentPodiumPosition = BlockPosition.ZERO;

    private DragonUtils() {

    }

    public static void runWithPodiumPosition(BlockPosition podiumPosition, Runnable runnable) {
        try {
            END_PODIUM_LOCATION.set(null, podiumPosition);
            currentPodiumPosition = podiumPosition;
            runnable.run();
        } finally {
            END_PODIUM_LOCATION.set(null, BlockPosition.ZERO);
            currentPodiumPosition = BlockPosition.ZERO;
        }
    }

    public static BlockPosition getCurrentPodiumPosition() {
        return currentPodiumPosition;
    }

}
