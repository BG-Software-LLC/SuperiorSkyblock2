package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.key.types.LazyKey;
import com.google.common.base.Preconditions;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class AbstractKeyMap<K extends Key, V> extends AbstractMap<Key, V> implements KeyMap<V> {

    private final LazyReference<Map<K, V>> innerMap;
    private final LazyReference<Map<CustomKey, V>> customInnerMap;
    private final Class<K> keyType;

    private EntrySet entrySet;
    private int size;

    protected AbstractKeyMap(KeyMapStrategy strategy, Class<K> keyType) {
        this.innerMap = new LazyReference<Map<K, V>>() {
            @Override
            protected Map<K, V> create() {
                return strategy.create(false);
            }
        };
        this.customInnerMap = new LazyReference<Map<CustomKey, V>>() {
            @Override
            protected Map<CustomKey, V> create() {
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
    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    @Override
    public V get(Object obj) {
        if (size() == 0)
            return null;

        if (obj instanceof LazyKey) {
            return get(((LazyKey<?>) obj).getBaseKey());
        }

        return getInternal((Key) obj, true);
    }

    private V getInternal(Key key, boolean tryGlobal) {
        Preconditions.checkArgument(!(key instanceof LazyKey), "Cannot call getInternal on LazyKey directly.");

        V value;

        if (this.keyType.isInstance(key)) {
            value = this.innerMap.getIfPresent().map(m -> m.get(key)).orElse(null);
        } else if (key instanceof CustomKey) {
            value = this.customInnerMap.getIfPresent().map(m -> m.get(key)).orElse(null);
        } else {
            value = null;
        }

        if (!tryGlobal || value != null)
            return value;

        Key globalKey = ((BaseKey<?>) key).toGlobalKey();

        return globalKey == key ? null : getInternal(globalKey, false);
    }

    @Override
    public V put(Key key, V value) {
        if (key instanceof LazyKey) {
            return put(((LazyKey<?>) key).getBaseKey(), value);
        }

        V oldValue;

        if (this.keyType.isInstance(key)) {
            oldValue = this.innerMap.get().put(this.keyType.cast(key), value);
        } else if (key instanceof CustomKey) {
            oldValue = this.customInnerMap.get().put((CustomKey) key, value);
        } else {
            throw new IllegalArgumentException("key " + key.getClass() + " is not of type " + this.keyType);
        }

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return size() == 0 ? null : removeNoSizeCheck(key);
    }

    private V removeNoSizeCheck(Object key) {
        if (key instanceof LazyKey) {
            return removeNoSizeCheck(((LazyKey<?>) key).getBaseKey());
        }

        V oldValue;

        if (this.keyType.isInstance(key)) {
            oldValue = this.innerMap.getIfPresent().map(m -> m.remove(key)).orElse(null);
        } else if (key instanceof CustomKey) {
            oldValue = this.customInnerMap.getIfPresent().map(m -> m.remove(key)).orElse(null);
        } else {
            return null;
        }

        if (oldValue != null)
            --this.size;

        return oldValue;
    }

    @Override
    public void clear() {
        this.innerMap.getIfPresent().ifPresent(Map::clear);
        this.customInnerMap.getIfPresent().ifPresent(Map::clear);
        this.size = 0;
    }

    @NotNull
    @Override
    public Set<Entry<Key, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
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

        Map<? extends Key, V> map;

        if (this.keyType.isInstance(original)) {
            map = this.innerMap.getIfPresent().orElse(null);
        } else if (original instanceof CustomKey) {
            map = this.customInnerMap.getIfPresent().orElse(null);
        } else {
            map = null;
        }

        if (map != null) {
            if (map.containsKey(original))
                return original;
            Key globalKey = ((BaseKey<?>) original).toGlobalKey();
            if (globalKey != original && map.containsKey(globalKey))
                return globalKey;
        }

        return def;
    }

    @Override
    public boolean removeIf(Predicate<Key> predicate) {
        if (isEmpty())
            return false;

        boolean removed = false;

        {
            Map<K, V> innerMap = this.innerMap.getIfPresent().orElse(null);
            if (innerMap != null) {
                int originalSize = innerMap.size();
                removed |= innerMap.entrySet().removeIf(entry -> predicate.test(entry.getKey()));
                int delta = innerMap.size() - originalSize;
                this.size -= delta;
            }
        }

        {
            Map<CustomKey, V> customInnerMap = this.customInnerMap.getIfPresent().orElse(null);
            if (customInnerMap != null) {
                int originalSize = customInnerMap.size();
                removed |= customInnerMap.entrySet().removeIf(entry -> predicate.test(entry.getKey()));
                int delta = customInnerMap.size() - originalSize;
                this.size -= delta;
            }
        }

        return removed;
    }

    @Override
    public V getRaw(Key key, V def) {
        if (size() == 0)
            return def;

        return getRawNoSizeCheck(key, def);
    }

    private V getRawNoSizeCheck(Key key, V def) {
        if (key instanceof LazyKey) {
            return getRawNoSizeCheck(((LazyKey<?>) key).getBaseKey(), def);
        }

        return Optional.ofNullable(getInternal(key, false)).orElse(def);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V value = get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public Map<Key, V> asMap() {
        return this;
    }

    private class EntrySet extends AbstractSet<Entry<Key, V>> {

        @Override
        public int size() {
            return AbstractKeyMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractKeyMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            Object key;
            Object value;

            if (o instanceof Entry) {
                Entry<?, ?> entry = (Entry<?, ?>) o;
                key = entry.getKey();
                value = entry.getValue();
            } else {
                key = o;
                value = null;
            }

            V realValue = AbstractKeyMap.this.get(key);
            return realValue != null && (value == null || Objects.equals(realValue, value));
        }

        @NotNull
        @Override
        public Iterator<Entry<Key, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public void clear() {
            AbstractKeyMap.this.clear();
        }

    }

    private class EntryIterator implements Iterator<Entry<Key, V>> {

        private Iterator<Entry> currIterator;
        private boolean isInnerMapIterator;
        private final boolean shouldRunCustomIterator;

        EntryIterator() {
            Map innerMap = AbstractKeyMap.this.innerMap.getIfPresent().orElse(null);
            Map customInnerMap = AbstractKeyMap.this.customInnerMap.getIfPresent().orElse(null);
            this.shouldRunCustomIterator = customInnerMap != null && !customInnerMap.isEmpty();
            if (innerMap != null) {
                this.currIterator = innerMap.entrySet().iterator();
                this.isInnerMapIterator = true;
            } else {
                this.currIterator = customInnerMap == null ? Collections.emptyIterator() : customInnerMap.entrySet().iterator();
                this.isInnerMapIterator = false;
            }
        }

        @Override
        public boolean hasNext() {
            if (this.isInnerMapIterator) {
                return this.shouldRunCustomIterator || this.currIterator.hasNext();
            }

            return this.currIterator.hasNext();
        }

        @Override
        public Entry<Key, V> next() {
            if (!this.currIterator.hasNext()) {
                if (!this.isInnerMapIterator || !this.shouldRunCustomIterator) {
                    throw new NoSuchElementException();
                }

                Map customInnerMap = AbstractKeyMap.this.customInnerMap.getIfPresent().orElse(null);
                // Should never occur as `shouldRunCustomIterator` would be false
                if (customInnerMap == null || customInnerMap.isEmpty())
                    throw new NoSuchElementException();

                this.currIterator = customInnerMap.entrySet().iterator();
                this.isInnerMapIterator = false;
            }

            return this.currIterator.next();
        }

        @Override
        public void remove() {
            this.currIterator.remove();
        }

    }

}
