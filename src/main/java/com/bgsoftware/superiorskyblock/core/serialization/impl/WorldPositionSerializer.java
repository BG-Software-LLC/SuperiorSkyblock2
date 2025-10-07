package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.SWorldPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;

public class WorldPositionSerializer implements ISerializer<WorldPosition, String> {

    private static final WorldPositionSerializer INSTANCE = new WorldPositionSerializer();

    public static WorldPositionSerializer getInstance() {
        return INSTANCE;
    }

    private WorldPositionSerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable WorldPosition serializable) {
        return serializable == null ? "" :
                serializable.getX() + "," + serializable.getY() + "," + serializable.getZ() +
                        "," + serializable.getYaw() + "," + serializable.getPitch();
    }

    @Nullable
    @Override
    public WorldPosition deserialize(@Nullable String element) {
        if (Text.isBlank(element))
            return null;

        try {
            String[] sections = element.split(", ");

            int startIndex = 0;
            if (sections.length >= 6) {
                // In the past, WorldPositions were serialized with a world.
                // Nowadays, we ignore all of them.
                ++startIndex;
            }

            double x = Double.parseDouble(sections[startIndex++]);
            double y = Double.parseDouble(sections[startIndex++]);
            double z = Double.parseDouble(sections[startIndex++]);
            float yaw = Float.parseFloat(sections[startIndex++]);
            float pitch = Float.parseFloat(sections[startIndex]);

            return SWorldPosition.of(x, y, z, yaw, pitch);
        } catch (Exception error) {
            Log.entering("ENTER", element);
            Log.error(error, "An unexpected error occurred while deserializing position '" + element + "':");
            return null;
        }
    }

}
