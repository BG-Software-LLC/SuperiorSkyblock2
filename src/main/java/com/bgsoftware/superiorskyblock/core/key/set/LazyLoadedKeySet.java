package com.bgsoftware.superiorskyblock.core.key.set;

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
import java.util.function.Consumer;
import java.util.function.Function;

public class LazyLoadedKeySet extends AbstractSet<Key> implements KeySet {

    private final KeySetStrategy strategy;
    @Nullable
    private KeySet delegate;
    @Nullable
    private KeySet pendingCustomKeys;

    public LazyLoadedKeySet(KeySetStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public int size() {
        return runOnSet(KeySet::size, 0);
    }

    @Override
    public boolean contains(Object o) {
        return runOnSet(s -> s.contains(o), false);
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
            addPendingCustomKeys();
        } else if (key instanceof MaterialKey) {
            this.delegate = new MaterialKeySet(this.strategy);
            addPendingCustomKeys();
        } else {
            if (this.pendingCustomKeys == null)
                this.pendingCustomKeys = new CustomKeySet(this.strategy);

            return this.pendingCustomKeys.add(key);
        }

        return this.delegate.add(key);
    }

    @Override
    public boolean remove(Object key) {
        return runOnSet(s -> s.remove(key), false);
    }

    @Override
    public void clear() {
        runOnSet(Set::clear);
    }

    @Override
    public Iterator<Key> iterator() {
        return runOnSet(Set::iterator, Iterators.emptyIterator());
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return runOnSet(s -> s.getKey(original), null);
    }

    @Override
    public Key getKey(Key original, Key def) {
        return runOnSet(s -> s.getKey(original, def), def);
    }

    @Override
    public String toString() {
        return runOnSet(Object::toString, "LazyLoadedKeySet[]");
    }

    @Override
    public Set<Key> asSet() {
        return runOnSet(KeySet::asSet, this);
    }

    private <T> T runOnSet(Function<KeySet, T> function, T def) {
        if (this.delegate != null)
            return function.apply(this.delegate);

        else if (this.pendingCustomKeys != null)
            return function.apply(this.pendingCustomKeys);

        else
            return def;
    }

    private void runOnSet(Consumer<KeySet> consumer) {
        if (this.delegate != null)
            consumer.accept(this.delegate);

        else if (this.pendingCustomKeys != null)
            consumer.accept(this.pendingCustomKeys);
    }

    private void addPendingCustomKeys() {
        if (this.pendingCustomKeys == null)
            return;

        this.delegate.addAll(this.pendingCustomKeys);
        this.pendingCustomKeys = null;
    }

}
