package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import org.bukkit.World;

import java.util.EnumMap;

public class Dimensions {

    private static final EnumMap<World.Environment, Dimension> ENVIRONMENT_TO_DIMENSION = new EnumMap<>(World.Environment.class);

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, Dimensions::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        ENVIRONMENT_TO_DIMENSION.clear();

        for (Dimension dimension : Dimension.values()) {
            Dimension currentDimension = ENVIRONMENT_TO_DIMENSION.get(dimension.getEnvironment());
            if (currentDimension == null || dimension.ordinal() < currentDimension.ordinal())
                ENVIRONMENT_TO_DIMENSION.put(dimension.getEnvironment(), dimension);
        }
    }

    @Nullable
    public static Dimension fromEnvironment(World.Environment environment) {
        return ENVIRONMENT_TO_DIMENSION.get(environment);
    }

    private Dimensions() {

    }

}
