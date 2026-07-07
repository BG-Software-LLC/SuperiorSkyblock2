package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import org.bukkit.Location;

public class LocationSerializer implements ISerializer<Location, String> {

    private final String separator;
    private final boolean center;

    public LocationSerializer(String separator) {
        this.separator = separator;
        this.center = false;
    }

    public LocationSerializer(String separator, boolean center) {
        this.separator = separator;
        this.center = center;
    }

    @NotNull
    @Override
    public String serialize(@Nullable Location serializable) {
        return serializable == null ? "" : LazyWorldLocation.getWorldName(serializable) + separator +
                serializable.getX() + separator +
                serializable.getY() + separator +
                serializable.getZ() + separator +
                serializable.getYaw() + separator +
                serializable.getPitch();
    }

    @Nullable
    @Override
    public Location deserialize(@Nullable String element) {
        if (Text.isBlank(element))
            return null;

        try {
            String[] sections = element.split(separator);

            double x = Double.parseDouble(sections[1]);
            if (center && !sections[1].contains(".")) x += 0.5;

            double y = Double.parseDouble(sections[2]);

            double z = Double.parseDouble(sections[3]);
            if (center && !sections[3].contains(".")) z += 0.5;

            float yaw = sections.length > 5 ? Float.parseFloat(sections[4]) : 0;
            float pitch = sections.length > 6 ? Float.parseFloat(sections[5]) : 0;

            return new LazyWorldLocation(sections[0], x, y, z, yaw, pitch);
        } catch (Exception error) {
            Log.entering("ENTER", element);
            Log.error(error, "An unexpected error occurred while deserializing location '" + element + "':");
            return null;
        }
    }

}
