package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.google.common.base.Preconditions;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class AbstractKeySet<K extends Key> extends AbstractSet<Key> implements KeySet {

    private final LazyReference<Set<K>> innerSet;
    private final LazyReference<Set<CustomKey>> customInnerSet;
    private final Class<K> keyType;

    private int size;

    protected AbstractKeySet(KeySetStrategy strategy, Class<K> keyType) {
        this.innerSet = new LazyReference<Set<K>>() {
            @Override
            protected Set<K> create() {
                return strategy.create(false);
            }
        };
        this.customInnerSet = new LazyReference<Set<CustomKey>>() {
            @Override
            protected Set<CustomKey> create() {
                return strategy.create(true);
            }
        };
        this.keyType = keyType;
    }

    @Override
    public int size() {
        return this.size;
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

        boolean contained;

        if (this.keyType.isInstance(key)) {
            contained = this.innerSet.getIfPresent().map(s -> s.contains(key)).orElse(false);
        } else if (key instanceof CustomKey) {
            contained = this.customInnerSet.getIfPresent().map(s -> s.contains(key)).orElse(false);
        } else {
            contained = false;
        }

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

        boolean modified;

        if (this.keyType.isInstance(key)) {
            modified = this.innerSet.get().add(this.keyType.cast(key));
        } else if (key instanceof CustomKey) {
            modified = this.customInnerSet.get().add((CustomKey) key);
        } else {
            throw new IllegalArgumentException("key " + key.getClass() + " is not of type " + this.keyType);
        }

        if (modified)
            ++this.size;

        return modified;
    }

    @Override
    public boolean remove(Object key) {
        return size() != 0 && removeNoSizeCheck(key);
    }

    private boolean removeNoSizeCheck(Object key) {
        if (key instanceof LazyKey) {
            return remove(((LazyKey<?>) key).getBaseKey());
        }

        boolean contained;

        if (this.keyType.isInstance(key)) {
            contained = this.innerSet.getIfPresent().map(s -> s.remove(key)).orElse(false);
        } else if (key instanceof CustomKey) {
            contained = this.customInnerSet.getIfPresent().map(s -> s.remove(key)).orElse(false);
        } else {
            return false;
        }

        if (contained)
            --this.size;

        return contained;
    }

    @Override
    public void clear() {
        this.innerSet.getIfPresent().ifPresent(Set::clear);
        this.customInnerSet.getIfPresent().ifPresent(Set::clear);
        this.size = 0;
    }

    @Override
    public Iterator<Key> iterator() {
        return new SetIterator();
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

        if (this.keyType.isInstance(original)) {
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
    public Set<Key> asSet() {
        return this;
    }

    private class SetIterator implements Iterator<Key> {

        private Iterator<Key> currIterator;
        private boolean isInnerSetIterator;
        private final boolean shouldRunCustomIterator;

        SetIterator() {
            Set innerSet = AbstractKeySet.this.innerSet.getIfPresent().orElse(null);
            Set customInnerSet = AbstractKeySet.this.customInnerSet.getIfPresent().orElse(null);
            this.shouldRunCustomIterator = customInnerSet != null && !customInnerSet.isEmpty();
            if (innerSet != null) {
                this.currIterator = innerSet.iterator();
                this.isInnerSetIterator = true;
            } else {
                this.currIterator = customInnerSet == null ? Collections.emptyIterator() : customInnerSet.iterator();
                this.isInnerSetIterator = false;
            }
        }

        @Override
        public boolean hasNext() {
            if (this.isInnerSetIterator) {
                return this.shouldRunCustomIterator || this.currIterator.hasNext();
            }

            return this.currIterator.hasNext();
        }

        @Override
        public Key next() {
            if (!this.currIterator.hasNext()) {
                if (!this.isInnerSetIterator || !this.shouldRunCustomIterator) {
                    throw new NoSuchElementException();
                }

                Set customInnerSet = AbstractKeySet.this.customInnerSet.getIfPresent().orElse(null);
                // Should never occur as `shouldRunCustomIterator` would be false
                if (customInnerSet == null || customInnerSet.isEmpty())
                    throw new NoSuchElementException();

                this.currIterator = customInnerSet.iterator();
                this.isInnerSetIterator = false;
            }

            return this.currIterator.next();
        }

        @Override
        public void remove() {
            this.currIterator.remove();
        }

    }

}
