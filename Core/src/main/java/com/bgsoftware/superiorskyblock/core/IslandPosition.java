package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

public class IslandPosition {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandPosition() {
    }

    public static long calculatePackedPosFromLocation(int locX, int locZ) {
        int radius = plugin.getSettings().getMaxIslandSize() * 3;
        int x = (Math.abs(locX) + (radius / 2)) / radius;
        int z = (Math.abs(locZ) + (radius / 2)) / radius;
        return calculatePackedPos(locX < 0 ? -x : x, locZ < 0 ? -z : z);
    }

    public static long calculatePackedPos(int posX, int posZ) {
        long posXLong = (long) posX & 0xFFFFFFFFL;
        long posZLong = (long) posZ & 0xFFFFFFFFL;
        return (posXLong) | (posZLong << 32);
    }

    public static int getXFromPacked(long packedPos) {
        return (int) (packedPos & 0xFFFFFFFFL);
    }

    public static int getZFromPacked(long packedPos) {
        return (int) ((packedPos >>> 32) & 0xFFFFFFFFL);
    }

}
