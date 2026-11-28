package com.bgsoftware.superiorskyblock.missions.farming;

import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Material;

import java.util.EnumMap;

public enum PlantType {

    BAMBOO(0, Plants.of("BAMBOO"), Plants.of("BAMBOO_SAPLING")),
    BEETROOT(3, Plants.of("BEETROOT", "BEETROOTS", "BEETROOT_SEEDS")),
    CACTUS(0, Plants.of("CACTUS")),
    CARROT(7, Plants.of("CARROT", "CARROTS")),
    CHORUS_PLANT(0, Plants.of("CHORUS_PLANT", "CHORUS_FLOWER")),
    COCOA(2, Plants.of("COCOA", "COCOA_BEANS")),
    MELON(-1, Plants.of("MELON", "MELON_BLOCK"), Plants.of("MELON_STEM")),
    NETHER_WART(3, Plants.of("NETHER_WART", "NETHER_STALK")),
    POTATO(7, Plants.of("POTATO", "POTATOES")),
    PUMPKIN(-1, Plants.of("PUMPKIN"), Plants.of("PUMPKIN_STEM")),
    SUGAR_CANE(0, Plants.of("SUGAR_CANE", "SUGAR_CANE_BLOCK")),
    SWEET_BERRY_BUSH(3, Plants.of("SWEET_BERRY_BUSH")),
    WHEAT(7, Plants.of("WHEAT", "CROPS", "WHEAT_SEEDS")),

    UNKNOWN(-1, Plants.EMPTY);

    private static final EnumMap<Material, PlantType> MATERIALS_TO_PLANT_TYPE = new EnumMap<>(Material.class);
    private static final EnumMap<Material, PlantType> SAPLING_MATERIALS_TO_PLANT_TYPE = new EnumMap<>(Material.class);
    private static boolean hasRegisteredPlantTypes = false;

    private final String[] plantTypes;
    private final String[] saplingTypes;
    private final int maxAge;
    private final Key cachedKey;

    PlantType(int maxAge, Plants plantTypes) {
        this(maxAge, plantTypes, plantTypes);
    }

    PlantType(int maxAge, Plants plantTypes, Plants saplingTypes) {
        this.maxAge = maxAge;
        this.plantTypes = plantTypes.arr;
        this.saplingTypes = saplingTypes.arr;
        this.cachedKey = Key.ofMaterialAndData(name());
    }

    public int getMaxAge() {
        return maxAge;
    }

    public Key getCachedKey() {
        return cachedKey;
    }

    public static PlantType getByType(Material material) {
        registerPlantTypes();
        return MATERIALS_TO_PLANT_TYPE.getOrDefault(material, UNKNOWN);
    }

    public static PlantType getBySaplingType(Material material) {
        registerPlantTypes();
        return SAPLING_MATERIALS_TO_PLANT_TYPE.getOrDefault(material, UNKNOWN);
    }

    private static void registerPlantType(PlantType plantType) {
        for (String material : plantType.plantTypes) {
            try {
                MATERIALS_TO_PLANT_TYPE.put(Material.valueOf(material), plantType);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (String material : plantType.saplingTypes) {
            try {
                SAPLING_MATERIALS_TO_PLANT_TYPE.put(Material.valueOf(material), plantType);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static void registerPlantTypes() {
        if (!hasRegisteredPlantTypes) {
            for (PlantType plantType : PlantType.values())
                registerPlantType(plantType);
            hasRegisteredPlantTypes = true;
        }
    }

    private static class Plants {

        static Plants EMPTY = of();

        private final String[] arr;

        static Plants of(String... arr) {
            return new Plants(arr);
        }

        Plants(String[] arr) {
            this.arr = arr;
        }

    }

}
