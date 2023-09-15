package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;

public class OffsetSerializer implements ISerializer<BlockOffset, String> {

    private final String separator;

    public OffsetSerializer(String separator) {
        this.separator = separator;
    }

    @NotNull
    @Override
    public String serialize(@Nullable BlockOffset serializable) {
        return serializable == null ? "" : serializable.getOffsetX() + separator + serializable.getOffsetY() + separator + serializable.getOffsetZ();
    }

    @Nullable
    @Override
    public BlockOffset deserialize(@Nullable String element) {
        if (Text.isBlank(element))
            return null;

        String[] stringSections = element.split(separator);

        if (stringSections.length != 3)
            return null;

        try {
            return SBlockOffset.fromOffsets(Integer.parseInt(stringSections[0]), Integer.parseInt(stringSections[1]), Integer.parseInt(stringSections[2]));
        } catch (NumberFormatException error) {
            return null;
        }
    }

}
