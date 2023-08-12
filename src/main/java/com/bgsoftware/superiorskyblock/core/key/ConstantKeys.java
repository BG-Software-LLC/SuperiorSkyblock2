package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.Materials;
import org.bukkit.Material;

import java.util.Optional;

public class ConstantKeys {

    public static final Key HOPPER = Keys.of(Material.HOPPER);
    public static final Key WATER = Keys.of(Material.WATER);
    public static final Key LAVA = Keys.of(Material.LAVA);
    public static final Key DRAGON_EGG = Keys.of(Material.DRAGON_EGG);
    public static final Key OBSIDIAN = Keys.of(Material.OBSIDIAN);
    public static final Key COMMAND_BLOCK = Keys.of(Materials.getMaterialSafe("COMMAND_BLOCK", "COMMAND"));
    public static final Key TNT = Keys.of(Material.TNT);
    public static final Key FURNACE = Keys.of(Material.FURNACE);
    public static final Key CHEST = Keys.of(Material.CHEST);
    public static final Key AIR = Keys.of(Material.AIR);
    public static final Key CAULDRON = Keys.of(Material.CAULDRON);
    public static final Key EGG_MOB_SPAWNER = Keys.ofSpawner("EGG");
    public static final Key MOB_SPAWNER = Keys.of(Materials.SPAWNER.toBukkitType());
    public static final Key END_PORTAL_FRAME_WITH_EYE = Keys.of(Materials.END_PORTAL_FRAME.toBukkitType(), (short) 7);
    public static final Key END_PORTAL_FRAME = Keys.of(Materials.END_PORTAL_FRAME.toBukkitType());
    public static final Key WET_SPONGE = Keys.of(Materials.getMaterialSafe("WET_SPONGE", "SPONGE"));
    public static final Key COBBLESTONE = Keys.of(Material.COBBLESTONE);
    public static final Key CHORUS_FLOWER = Optional.ofNullable(Materials.getMaterialSafe("CHORUS_FLOWER"))
            .map(Keys::of).orElse(Keys.EMPTY);

    private ConstantKeys() {

    }

}
