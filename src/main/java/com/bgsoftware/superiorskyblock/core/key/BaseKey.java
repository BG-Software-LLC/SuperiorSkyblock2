package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;

public abstract class BaseKey<T extends Key> implements Key {

    protected final LazyReference<String> toStringCache = new LazyReference<String>() {
        @Override
        protected String create() {
            return toStringInternal();
        }
    };
    protected final LazyReference<Integer> hashCodeCache = new LazyReference<Integer>() {
        @Override
        protected Integer create() {
            return hashCodeInternal();
        }
    };

    private final Class<T> baseKeyClass;
    private boolean apiKey = false;

    protected BaseKey(Class<T> baseKeyClass) {
        this.baseKeyClass = baseKeyClass;
    }

    @Override
    public abstract String getGlobalKey();

    public abstract T toGlobalKey();

    @Override
    public abstract String getSubKey();

    protected abstract String toStringInternal();

    protected abstract int hashCodeInternal();

    protected abstract boolean equalsInternal(T other);

    protected abstract int compareToInternal(T other);

    public final T markAPIKey() {
        this.apiKey = true;
        return (T) this;
    }

    public boolean isAPIKey() {
        return this.apiKey;
    }

    @Override
    public final String toString() {
        return this.toStringCache.get();
    }

    @Override
    public final int hashCode() {
        return this.hashCodeCache.get();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (getClass() != o.getClass()) {
            if (o instanceof LazyKey) {
                LazyKey<T> lazyKey = (LazyKey<T>) o;
                return this.equalsInternal(lazyKey.getBaseKey());
            }

            if (!this.baseKeyClass.isAssignableFrom(o.getClass()))
                return false;
        }

        return this.equalsInternal((T) o);
    }

    @Override
    public final int compareTo(@NotNull Key o) {
        if (this == o)
            return 0;

        if (getClass() != o.getClass()) {
            if (o instanceof LazyKey) {
                LazyKey<T> lazyKey = (LazyKey<T>) o;
                return this.compareToInternal(lazyKey.getBaseKey());
            }

            if (!this.baseKeyClass.isAssignableFrom(o.getClass()))
                return toString().compareTo(o.toString());
        }

        return this.compareToInternal((T) o);
    }

    protected final void loadLazyCaches() {
        toStringCache.get();
        hashCodeCache.get();
    }

}
