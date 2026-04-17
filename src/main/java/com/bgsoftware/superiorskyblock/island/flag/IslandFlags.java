package com.bgsoftware.superiorskyblock.island.flag;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;

import java.util.Comparator;
import java.util.Locale;

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

    private static String ALL_FLAG_NAMES;
    private static int KNOWN_FLAGS_COUNT;

    private IslandFlags() {

    }

    public static void registerFlags() {
        // Do nothing, only trigger all the register calls
    }

    public static String getFlagsNames() {
        if (ALL_FLAG_NAMES == null || KNOWN_FLAGS_COUNT != IslandFlag.values().size()) {
            ALL_FLAG_NAMES = Formatters.COMMA_FORMATTER.format(IslandFlag.values().stream()
                    .sorted(Comparator.comparing(IslandFlag::getName))
                    .map(islandFlag -> islandFlag.getName().toLowerCase(Locale.ENGLISH)));
            KNOWN_FLAGS_COUNT = IslandFlag.values().size();
        }

        return ALL_FLAG_NAMES;
    }

    private static IslandFlag register(String name) {
        IslandFlag.register(name);
        return IslandFlag.getByName(name);
    }

}
