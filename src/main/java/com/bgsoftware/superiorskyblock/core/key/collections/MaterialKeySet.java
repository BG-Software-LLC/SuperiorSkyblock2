package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.google.common.collect.Iterators;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

public class MaterialKeySet extends AbstractSet<Key> implements KeySet {

    private static final MaterialKeySet EMPTY_MAP = new MaterialKeySet(() -> Collections.emptySet());

    private final LazyReference<Set<MaterialKey>> innerSet;
    private final LazyReference<Set<CustomKey>> customInnerSet;

    public static MaterialKeySet createHashSet() {
        return createSet(() -> new HashSet<>());
    }

    public static MaterialKeySet createSet(Supplier<Set> setCreator) {
        return new MaterialKeySet(setCreator);
    }

    public static MaterialKeySet createEmptyMap() {
        return EMPTY_MAP;
    }

    private MaterialKeySet(Supplier<Set> setCreator) {
        this.innerSet = new LazyReference<Set<MaterialKey>>() {
            @Override
            protected Set<MaterialKey> create() {
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
    public boolean contains(Object obj) {
        if (size() == 0)
            return false;

        if (obj instanceof LazyKey) {
            return contains(((LazyKey<?>) obj).getBaseKey());
        }

        return containsInternal((Key) obj, true);
    }

    private boolean containsInternal(Key key, boolean tryGlobal) {
        boolean exists;

        if (key instanceof MaterialKey) {
            exists = this.innerSet.getIfPresent().map(s -> s.contains(key)).orElse(false);
        } else if (key instanceof CustomKey) {
            exists = this.customInnerSet.getIfPresent().map(s -> s.contains(key)).orElse(false);
        } else {
            return false;
        }

        if (!tryGlobal || exists)
            return exists;

        Key globalKey = ((BaseKey<?>) key).toGlobalKey();

        return globalKey != key && containsInternal(globalKey, false);
    }

    @Override
    public boolean add(Key key) {
        if (key instanceof LazyKey) {
            return add(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof MaterialKey) {
            return this.innerSet.get().add((MaterialKey) key);
        }

        if (key instanceof CustomKey) {
            return this.customInnerSet.get().add((CustomKey) key);
        }

        throw new IllegalArgumentException("obj is not MaterialKey");
    }

    @Override
    public boolean remove(Object key) {
        if (size() == 0)
            return false;

        if (key instanceof LazyKey) {
            return remove(((LazyKey<?>) key).getBaseKey());
        }

        if (key instanceof MaterialKey) {
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
        return this.getKey(original, null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        if (size() == 0)
            return def;

        if (original instanceof LazyKey) {
            return getKey(((LazyKey<?>) original).getBaseKey());
        }

        Set set;

        if (original instanceof MaterialKey) {
            set = this.innerSet.getIfPresent().orElse(null);
        } else if (original instanceof CustomKey) {
            set = this.customInnerSet.getIfPresent().orElse(null);
        } else {
            set = null;
        }

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
    public String toString() {
        return "MaterialKeySet" + super.toString();
    }

    @Override
    public Set<Key> asSet() {
        return this;
    }

}
