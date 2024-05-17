package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.world.Dimensions;

import java.util.Collection;
import java.util.function.Predicate;

public class LegacyMasks {

    public static EnumerateSet<Dimension> convertGeneratedSchematicsMask(int generatedSchematicsMask) {
        EnumerateSet<Dimension> generatedSchematics = new EnumerateSet<>(Dimension.values());

        if ((generatedSchematicsMask & 8) == 8)
            generatedSchematics.add(Dimensions.NORMAL);

        if ((generatedSchematicsMask & 4) == 4)
            generatedSchematics.add(Dimensions.NETHER);

        if ((generatedSchematicsMask & 3) == 3)
            generatedSchematics.add(Dimensions.THE_END);

        return generatedSchematics;
    }

    public static int convertGeneratedSchematicsMask(EnumerateSet<Dimension> generatedSchematics) {
        return convertGeneratedSchematicsMaskInternal(generatedSchematics::contains);
    }

    public static int convertGeneratedSchematicsMask(Collection<Dimension> generatedSchematics) {
        return convertGeneratedSchematicsMaskInternal(generatedSchematics::contains);
    }

    public static int convertGeneratedSchematicsMaskInternal(Predicate<Dimension> containsMethod) {
        int generatedSchematicsMask = 0;

        // generatedSchematics cannot have dimension that is not default
        for (Dimension dimension : Dimension.values()) {
            if (dimension != Dimensions.NORMAL && dimension != Dimensions.NETHER && dimension != Dimensions.THE_END &&
                    containsMethod.test(dimension))
                throw new IllegalStateException("Cannot build mask for generated schematics of custom dimensions.");
        }

        if (containsMethod.test(Dimensions.NORMAL))
            generatedSchematicsMask |= 8;
        if (containsMethod.test(Dimensions.NETHER))
            generatedSchematicsMask |= 4;
        if (containsMethod.test(Dimensions.THE_END))
            generatedSchematicsMask |= 3;

        return generatedSchematicsMask;
    }

    public static EnumerateSet<Dimension> convertUnlockedWorldsMask(int unlockedWorldsMask) {
        EnumerateSet<Dimension> unlockedWorlds = new EnumerateSet<>(Dimension.values());

        if ((unlockedWorldsMask & 4) == 4)
            unlockedWorlds.add(Dimensions.NORMAL);

        if ((unlockedWorldsMask & 1) == 1)
            unlockedWorlds.add(Dimensions.NETHER);

        if ((unlockedWorldsMask & 2) == 2)
            unlockedWorlds.add(Dimensions.THE_END);

        return unlockedWorlds;
    }

    public static int convertUnlockedWorldsMask(EnumerateSet<Dimension> unlockedWorlds) {
        return convertUnlockedWorldsMaskInternal(unlockedWorlds::contains);
    }

    public static int convertUnlockedWorldsMask(Collection<Dimension> unlockedWorlds) {
        return convertUnlockedWorldsMaskInternal(unlockedWorlds::contains);
    }

    private static int convertUnlockedWorldsMaskInternal(Predicate<Dimension> containsMethod) {
        int generatedSchematicsMask = 0;

        // generatedSchematics cannot have dimension that is not default
        for (Dimension dimension : Dimension.values()) {
            if (dimension != Dimensions.NORMAL && dimension != Dimensions.NETHER && dimension != Dimensions.THE_END &&
                    containsMethod.test(dimension))
                throw new IllegalStateException("Cannot build mask for unlocked worlds of custom dimensions.");
        }

        if (containsMethod.test(Dimensions.NORMAL))
            generatedSchematicsMask |= 4;
        if (containsMethod.test(Dimensions.NETHER))
            generatedSchematicsMask |= 1;
        if (containsMethod.test(Dimensions.THE_END))
            generatedSchematicsMask |= 2;

        return generatedSchematicsMask;
    }

    private LegacyMasks() {

    }

}
