package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.google.common.collect.Iterators;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class LazyLoadedKeySet extends AbstractSet<Key> implements KeySet {

    private final KeySetStrategy strategy;
    @Nullable
    private KeySet delegate;

    public LazyLoadedKeySet(KeySetStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<Key> iterator() {
        return this.delegate == null ? Iterators.emptyIterator() : this.delegate.iterator();
    }

    @Override
    public int size() {
        return this.delegate == null ? 0 : this.delegate.size();
    }

    @Override
    public boolean contains(Object o) {
        return this.delegate != null && this.delegate.contains(o);
    }

    @Override
    public boolean add(Key key) {
        if (this.delegate != null)
            return this.delegate.add(key);

        return addNoDelegate(key);
    }

    public boolean addNoDelegate(Key key) {
        if (key instanceof LazyKey) {
            return addNoDelegate(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof EntityTypeKey) {
            this.delegate = new EntityTypeKeySet(this.strategy);
        } else if (key instanceof MaterialKey) {
            this.delegate = new MaterialKeySet(this.strategy);
        } else {
            throw new IllegalArgumentException("Cannot insert key of type " + key.getClass());
        }

        return this.delegate.add(key);
    }

    @Override
    public boolean remove(Object key) {
        return this.delegate != null && this.delegate.remove(key);
    }

    @Override
    public void clear() {
        if (this.delegate != null)
            this.delegate.clear();
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return this.delegate == null ? null : this.delegate.getKey(original);
    }

    @Override
    public Key getKey(Key original, Key def) {
        return this.delegate == null ? def : this.delegate.getKey(original, def);
    }

    @Override
    public String toString() {
        return this.delegate == null ? "LazyLoadedKeySet[]" : this.delegate.toString();
    }

    @Override
    public Set<Key> asSet() {
        return this.delegate == null ? this : this.delegate.asSet();
    }

}
