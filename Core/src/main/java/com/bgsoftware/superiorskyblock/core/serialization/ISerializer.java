package com.bgsoftware.superiorskyblock.core.serialization;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;

public interface ISerializer<S, D> {

    @NotNull
    D serialize(@Nullable S serializable);

    @Nullable
    S deserialize(@Nullable D deserializable);

}
