package com.bgsoftware.superiorskyblock.serialization.impl;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SBlockOffset;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class OffsetSerializer implements ISerializer<BlockOffset, String> {

    private final String separator;

    public OffsetSerializer(String separator) {
        this.separator = separator;
    }

    @NotNull
    @Override
    public String serialize(@Nullable BlockOffset serializable) {
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    @Nullable
    @Override
    public BlockOffset deserialize(@Nullable String element) {
        if (StringUtils.isBlank(element))
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
