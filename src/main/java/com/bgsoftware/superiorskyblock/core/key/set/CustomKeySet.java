package com.bgsoftware.superiorskyblock.core.key.set;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class CustomKeySet extends AbstractSet<Key> implements KeySet {

    private final LazyReference<Set<Key>> innerSet;

    public CustomKeySet(KeySetStrategy strategy) {
        this.innerSet = new LazyReference<Set<Key>>() {
            @Override
            protected Set<Key> create() {
                return strategy.create(true);
            }
        };
    }

    @Override
    public int size() {
        return this.innerSet.getIfPresent().map(Set::size).orElse(0);
    }

    @Override
    public boolean contains(Object o) {
        if (size() == 0)
            return false;

        if (o instanceof LazyKey) {
            return contains(((LazyKey<?>) o).getBaseKey());
        }

        return containsInternal((Key) o, true);
    }

    private boolean containsInternal(Key key, boolean tryGlobal) {
        Preconditions.checkArgument(!(key instanceof LazyKey), "Cannot call getInternal on LazyKey directly.");

        boolean contained = this.innerSet.getIfPresent().map(s -> s.contains(key)).orElse(false);

        if (!tryGlobal || contained)
            return contained;

        Key globalKey = ((BaseKey<?>) key).toGlobalKey();

        return globalKey != key && containsInternal(globalKey, false);
    }

    @Override
    public boolean add(Key key) {
        if (key instanceof LazyKey) {
            return add(((LazyKey<?>) key).getBaseKey());
        }

        return this.innerSet.get().add(key);
    }

    @Override
    public boolean remove(Object key) {
        return size() != 0 && removeNoSizeCheck(key);
    }

    private boolean removeNoSizeCheck(Object key) {
        if (key instanceof LazyKey) {
            return remove(((LazyKey<?>) key).getBaseKey());
        }

        return this.innerSet.getIfPresent().map(s -> s.remove(key)).orElse(false);
    }

    @Override
    public void clear() {
        this.innerSet.getIfPresent().ifPresent(Set::clear);
    }

    @Override
    public Iterator<Key> iterator() {
        return this.innerSet.getIfPresent().map(Set::iterator).orElse(Iterators.emptyIterator());
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return this.getKey(original, null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        if (size() == 0)
            return def;

        if (original instanceof LazyKey) {
            return getKey(((LazyKey<?>) original).getBaseKey());
        }

        Set<Key> set = this.innerSet.getIfPresent().orElse(null);

        if (set != null) {
            if (set.contains(original))
                return original;
            Key globalKey = ((BaseKey<?>) original).toGlobalKey();
            if (globalKey != original && set.contains(globalKey))
                return globalKey;
        }

        return def;
    }

    @Override
    public Set<Key> asSet() {
        return this;
    }

}
