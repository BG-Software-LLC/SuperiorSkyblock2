package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping;

import org.jetbrains.annotations.Nullable;

public final class Holder<T> extends MappedObject<net.minecraft.core.Holder<T>> {

    public Holder(net.minecraft.core.Holder<T> handle) {
        super(handle);
    }

    @Nullable
    public static <T> Holder<T> ofNullable(net.minecraft.core.Holder<T> handle) {
        return handle == null ? null : new Holder<>(handle);
    }

}
