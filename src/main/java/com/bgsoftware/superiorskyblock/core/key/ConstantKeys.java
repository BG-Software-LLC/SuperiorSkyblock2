package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.Materials;

public class ConstantKeys {

    public static final Key HOPPER = KeyImpl.of("HOPPER");
    public static final Key WATER = KeyImpl.of("WATER");
    public static final Key LAVA = KeyImpl.of("LAVA");
    public static final Key DRAGON_EGG = KeyImpl.of("DRAGON_EGG");
    public static final Key OBSIDIAN = KeyImpl.of("OBSIDIAN");
    public static final Key COMMAND_BLOCK = KeyImpl.of("COMMAND_BLOCK");
    public static final Key COMMAND = KeyImpl.of("COMMAND");
    public static final Key TNT = KeyImpl.of("TNT");
    public static final Key FURNACE = KeyImpl.of("FURNACE");
    public static final Key CHEST = KeyImpl.of("CHEST");
    public static final Key AIR = KeyImpl.of("AIR");
    public static final Key CAULDRON = KeyImpl.of("CAULDRON");
    public static final Key EGG_MOB_SPAWNER = KeyImpl.of(Materials.SPAWNER.toBukkitType() + ":EGG");
    public static final Key MOB_SPAWNER = KeyImpl.of(Materials.SPAWNER.toBukkitType().name());
    public static final Key END_PORTAL_FRAME_WITH_EYE = KeyImpl.of(Materials.END_PORTAL_FRAME.toBukkitType(), (short) 7);
    public static final Key END_PORTAL_FRAME = KeyImpl.of(Materials.END_PORTAL_FRAME.toBukkitType().name());
    public static final Key WET_SPONGE = KeyImpl.of("WET_SPONGE");
    public static final Key COBBLESTONE = KeyImpl.of("COBBLESTONE");
    public static final Key CHORUS_FLOWER = KeyImpl.of("CHORUS_FLOWER");

    private ConstantKeys() {

    }

}
