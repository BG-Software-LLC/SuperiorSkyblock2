package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.google.common.collect.Iterators;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

public class EntityTypeKeySet extends AbstractSet<Key> implements KeySet {

    private final LazyReference<Set<EntityTypeKey>> innerSet;
    private final LazyReference<Set<CustomKey>> customInnerSet;

    public static EntityTypeKeySet createSet(Supplier<Set> setCreator) {
        return new EntityTypeKeySet(setCreator);
    }

    private EntityTypeKeySet(Supplier<Set> setCreator) {
        this.innerSet = new LazyReference<Set<EntityTypeKey>>() {
            @Override
            protected Set<EntityTypeKey> create() {
                return setCreator.get();
            }
        };
        this.customInnerSet = new LazyReference<Set<CustomKey>>() {
            @Override
            protected Set<CustomKey> create() {
                return setCreator.get();
            }
        };
    }

    @Override
    public Iterator<Key> iterator() {
        Iterator innerSetIterator = this.innerSet.getIfPresent().map(Set::iterator).orElse(null);
        Iterator customInnerSetIterator = this.customInnerSet.getIfPresent().map(Set::iterator).orElse(null);

        if (innerSetIterator == null && customInnerSetIterator == null)
            return Iterators.emptyIterator();
        if (innerSetIterator == null)
            return customInnerSetIterator;
        if (customInnerSetIterator == null)
            return innerSetIterator;

        return Iterators.concat(innerSetIterator, customInnerSetIterator);
    }

    @Override
    public int size() {
        int innerSetSize = this.innerSet.getIfPresent().map(Set::size).orElse(0);
        int customInnerSetSize = this.customInnerSet.getIfPresent().map(Set::size).orElse(0);
        return innerSetSize + customInnerSetSize;
    }

    @Override
    public boolean contains(Object o) {
        if (size() == 0)
            return false;

        if (o instanceof LazyKey) {
            return contains(((LazyKey<?>) o).getBaseKey());
        }

        if (o instanceof EntityTypeKey) {
            return this.innerSet.getIfPresent().map(s -> s.contains(o)).orElse(false);
        }

        if (o instanceof CustomKey) {
            return this.customInnerSet.getIfPresent().map(s -> s.contains(o)).orElse(false);
        }

        return false;
    }

    @Override
    public boolean add(Key key) {
        if (key instanceof LazyKey) {
            return add(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof EntityTypeKey) {
            return this.innerSet.get().add((EntityTypeKey) key);
        }

        if (key instanceof CustomKey) {
            return this.customInnerSet.get().add((CustomKey) key);
        }

        throw new IllegalArgumentException("obj is not EntityTypeKey");
    }

    @Override
    public boolean remove(Object key) {
        if (size() == 0)
            return false;

        if (key instanceof LazyKey) {
            return remove(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof EntityTypeKey) {
            return this.innerSet.getIfPresent().map(s -> s.remove(key)).orElse(false);
        }

        if (key instanceof CustomKey) {
            return this.customInnerSet.getIfPresent().map(s -> s.remove(key)).orElse(false);
        }

        return false;
    }

    @Override
    public void clear() {
        this.innerSet.getIfPresent().ifPresent(Set::clear);
        this.customInnerSet.getIfPresent().ifPresent(Set::clear);
    }

    @Nullable
    @Override
    public Key getKey(Key original) {
        return getKey(original, null);
    }

    @Override
    public Key getKey(Key original, Key def) {
        if (size() == 0)
            return def;

        if (original instanceof LazyKey) {
            return getKey(((LazyKey<?>) original).getBaseKey());
        }

        Set set;

        if (original instanceof EntityTypeKey) {
            set = this.innerSet.getIfPresent().orElse(null);
        } else if (original instanceof CustomKey) {
            set = this.customInnerSet.getIfPresent().orElse(null);
        } else {
            set = null;
        }

        return set == null || !set.contains(original) ? def : original;
    }

    @Override
    public String toString() {
        return "EntityTypeKeySet" + super.toString();
    }

    @Override
    public Set<Key> asSet() {
        return this;
    }

}
