package com.bgsoftware.superiorskyblock.island.flag;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;

public class IslandFlags {

    public static final IslandFlag ALWAYS_DAY = register("ALWAYS_DAY");
    public static final IslandFlag ALWAYS_MIDDLE_DAY = register("ALWAYS_MIDDLE_DAY");
    public static final IslandFlag ALWAYS_NIGHT = register("ALWAYS_NIGHT");
    public static final IslandFlag ALWAYS_MIDDLE_NIGHT = register("ALWAYS_MIDDLE_NIGHT");
    public static final IslandFlag ALWAYS_RAIN = register("ALWAYS_RAIN");
    public static final IslandFlag ALWAYS_SHINY = register("ALWAYS_SHINY");
    public static final IslandFlag CREEPER_EXPLOSION = register("CREEPER_EXPLOSION");
    public static final IslandFlag CROPS_GROWTH = register("CROPS_GROWTH");
    public static final IslandFlag EGG_LAY = register("EGG_LAY");
    public static final IslandFlag ENDERMAN_GRIEF = register("ENDERMAN_GRIEF");
    public static final IslandFlag FIRE_SPREAD = register("FIRE_SPREAD");
    public static final IslandFlag GHAST_FIREBALL = register("GHAST_FIREBALL");
    public static final IslandFlag LAVA_FLOW = register("LAVA_FLOW");
    public static final IslandFlag NATURAL_ANIMALS_SPAWN = register("NATURAL_ANIMALS_SPAWN");
    public static final IslandFlag NATURAL_MONSTER_SPAWN = register("NATURAL_MONSTER_SPAWN");
    public static final IslandFlag PVP = register("PVP");
    public static final IslandFlag SPAWNER_ANIMALS_SPAWN = register("SPAWNER_ANIMALS_SPAWN");
    public static final IslandFlag SPAWNER_MONSTER_SPAWN = register("SPAWNER_MONSTER_SPAWN");
    public static final IslandFlag TNT_EXPLOSION = register("TNT_EXPLOSION");
    public static final IslandFlag TREE_GROWTH = register("TREE_GROWTH");
    public static final IslandFlag WATER_FLOW = register("WATER_FLOW");
    public static final IslandFlag WITHER_EXPLOSION = register("WITHER_EXPLOSION");

    private IslandFlags() {

    }

    public static void registerFlags() {
        // Do nothing, only trigger all the register calls
    }

    private static IslandFlag register(String name) {
        IslandFlag.register(name);
        return IslandFlag.getByName(name);
    }

}
