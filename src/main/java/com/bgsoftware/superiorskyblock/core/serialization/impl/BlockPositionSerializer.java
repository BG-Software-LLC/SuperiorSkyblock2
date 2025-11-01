package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;

public class BlockPositionSerializer implements ISerializer<BlockPosition, String> {

    private static final BlockPositionSerializer INSTANCE = new BlockPositionSerializer();

    public static BlockPositionSerializer getInstance() {
        return INSTANCE;
    }

    private BlockPositionSerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable BlockPosition serializable) {
        return serializable == null ? "" :
                serializable.getX() + ", " + serializable.getY() + ", " + serializable.getZ();
    }

    @Nullable
    @Override
    public BlockPosition deserialize(@Nullable String element) {
        if (Text.isBlank(element))
            return null;

        try {
            String[] sections = element.split(", ");

            int startIndex = 0;
            if (sections.length >= 4) {
                // In the past, BlockPositions were serialized with a world, yaw and pitch.
                // Nowadays, we ignore all of them.
                ++startIndex;
            }

            int x = (int) Double.parseDouble(sections[startIndex++]);
            int y = (int) Double.parseDouble(sections[startIndex++]);
            int z = (int) Double.parseDouble(sections[startIndex]);

            return SBlockPosition.of(x, y, z);
        } catch (Exception error) {
            Log.entering("ENTER", element);
            Log.error(error, "An unexpected error occurred while deserializing position '" + element + "':");
            return null;
        }
    }

}
