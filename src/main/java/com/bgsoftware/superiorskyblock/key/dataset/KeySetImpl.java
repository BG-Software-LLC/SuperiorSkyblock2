package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class KeySetImpl extends AbstractSet<Key> implements KeySet {

    private final Set<String> set;

    private Set<Key> innerReflectedSet;

    public static KeySetImpl create(Supplier<Set<String>> setCreator, Collection<Key> keys) {
        return new KeySetImpl(setCreator, keys.stream().map(Object::toString).collect(Collectors.toSet()));
    }

    public static KeySetImpl create(Supplier<Set<String>> setCreator) {
        return new KeySetImpl(setCreator);
    }

    public static KeySetImpl createHashSet() {
        return create(() -> new HashSet<>());
    }

    public static KeySetImpl createHashSet(Collection<Key> keys) {
        return create(() -> new HashSet<>(), keys);
    }

    private KeySetImpl(Supplier<Set<String>> setCreator, Collection<String> keys) {
        this(setCreator);
        this.set.addAll(keys);
    }

    private KeySetImpl(Supplier<Set<String>> setCreator) {
        this.set = setCreator.get();
    }

    @Override
    @NotNull
    public Iterator<Key> iterator() {
        return asSet().iterator();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Key && (set.contains(o.toString()) || (!((Key) o).getSubKey().isEmpty() && set.contains(((Key) o).getGlobalKey())));
    }

    @Override
    public boolean add(Key key) {
        return set.add(key.toString());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Key ? set.remove(o.toString()) : set.remove(o);
    }

    @Override
    public Key getKey(Key original) {
        return getKey(original, null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        if (set.contains(original.toString()))
            return original;
        else if (set.contains(original.getGlobalKey()))
            return KeyImpl.of(original.getGlobalKey(), "");
        else
            return def;
    }

    @Override
    public Set<Key> asSet() {
        return Collections.unmodifiableSet(innerReflectedSet == null ? (innerReflectedSet = new InnerReflectedSet()) : innerReflectedSet);
    }

    private final class InnerReflectedSet implements Set<Key> {

        @Override
        public int size() {
            return KeySetImpl.this.set.size();
        }

        @Override
        public boolean isEmpty() {
            return KeySetImpl.this.set.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return KeySetImpl.this.set.contains(o);
        }

        @NotNull
        @Override
        public Iterator<Key> iterator() {
            return new SetIterator();
        }

        @Override
        public Object @NotNull [] toArray() {
            return KeySetImpl.this.set.toArray();
        }

        @Override
        public <T> T @NotNull [] toArray(T @NotNull [] a) {
            return KeySetImpl.this.set.toArray(a);
        }

        @Override
        public boolean add(Key key) {
            // No implementation
            return false;
        }

        @Override
        public boolean remove(Object o) {
            // No implementation
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return KeySetImpl.this.set.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Key> c) {
            // No implementation
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            // No implementation
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            // No implementation
            return false;
        }

        @Override
        public void clear() {
            // No implementation
        }
    }

    private final class SetIterator implements Iterator<Key> {

        final Iterator<String> innerSetIterator = KeySetImpl.this.set.iterator();

        @Override
        public boolean hasNext() {
            return innerSetIterator.hasNext();
        }

        @Override
        public Key next() {
            return KeyImpl.of(innerSetIterator.next());
        }

        @Override
        public void remove() {
            innerSetIterator.remove();
        }
    }

}
