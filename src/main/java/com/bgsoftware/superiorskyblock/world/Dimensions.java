package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;

import java.util.Objects;

public class Dimensions {

    public static final Dimension NORMAL = register("NORMAL", World.Environment.NORMAL);
    public static final Dimension NETHER = register("NETHER", World.Environment.NETHER);
    public static final Dimension THE_END = register("THE_END", World.Environment.THE_END);

    public static void registerDimensions() {
        // Do nothing, only trigger all the register calls
    }

    @Nullable
    public static Dimension fromEnvironment(@Nullable World.Environment environment) {
        if (environment == null)
            return null;

        switch (environment) {
            case NORMAL:
                return NORMAL;
            case NETHER:
                return NETHER;
            case THE_END:
                return THE_END;
        }

        return Dimension.getByName(environment.name());
    }

    @NotNull
    private static Dimension register(String name, World.Environment environment) {
        Dimension.register(name, environment);
        return Objects.requireNonNull(Dimension.getByName(name));
    }

    private Dimensions() {

    }

}
