package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.google.common.base.Preconditions;

public class LazyKey<T extends Key> extends BaseKey<T> {

    private final LazyReference<T> baseKeyLoader;

    public LazyKey(Class<T> baseKeyClass, LazyReference<T> baseKeyLoader) {
        super(baseKeyClass);
        Preconditions.checkArgument(baseKeyClass != BaseKey.class);
        this.baseKeyLoader = baseKeyLoader;
    }

    public T getBaseKey() {
        return this.baseKeyLoader.get();
    }

    @Override
    public final String getGlobalKey() {
        return getBaseKey().getGlobalKey();
    }

    @Override
    public final T toGlobalKey() {
        return ((BaseKey<T>) getBaseKey()).toGlobalKey();
    }

    @Override
    public final String getSubKey() {
        return getBaseKey().getSubKey();
    }

    @Override
    protected final String toStringInternal() {
        return getBaseKey().toString();
    }

    @Override
    protected final int hashCodeInternal() {
        return getBaseKey().hashCode();
    }

    @Override
    protected final boolean equalsInternal(T other) {
        return getBaseKey().equals(other);
    }

    @Override
    protected final int compareToInternal(T other) {
        return getBaseKey().compareTo(other);
    }

}
