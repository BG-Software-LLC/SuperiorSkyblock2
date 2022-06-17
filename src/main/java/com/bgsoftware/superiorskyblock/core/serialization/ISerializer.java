package com.bgsoftware.superiorskyblock.core.serialization;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface ISerializer<S, D> {

    @NotNull
    D serialize(@Nullable S serializable);

    @Nullable
    S deserialize(@Nullable D deserializable);

}
