package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.core.BlockPosition;
import net.minecraft.world.level.levelgen.feature.WorldGenEndTrophy;

import java.lang.reflect.Modifier;

public final class DragonUtils {

    private static final ReflectField<net.minecraft.core.BlockPosition> END_PODIUM_LOCATION = new ReflectField<net.minecraft.core.BlockPosition>(
            WorldGenEndTrophy.class, net.minecraft.core.BlockPosition.class,
            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static BlockPosition currentPodiumPosition = BlockPosition.ZERO;

    private DragonUtils() {

    }

    public static void runWithPodiumPosition(BlockPosition podiumPosition, Runnable runnable) {
        try {
            END_PODIUM_LOCATION.set(null, podiumPosition.getHandle());
            currentPodiumPosition = podiumPosition;
            runnable.run();
        } finally {
            END_PODIUM_LOCATION.set(null, BlockPosition.ZERO.getHandle());
            currentPodiumPosition = BlockPosition.ZERO;
        }
    }

    public static BlockPosition getCurrentPodiumPosition() {
        return currentPodiumPosition;
    }

}
