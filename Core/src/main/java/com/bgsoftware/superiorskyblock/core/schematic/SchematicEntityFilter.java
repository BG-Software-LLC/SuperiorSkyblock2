package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;

public class SchematicEntityFilter {

    private SchematicEntityFilter() {

    }

    public static CompoundTag filterNBTTag(@Nullable CompoundTag entityTag) {
        if (entityTag != null) {
            entityTag.remove("UUID");
            entityTag.remove("UUIDMost");
            entityTag.remove("UUIDLeast");
        }

        return entityTag;
    }

}
