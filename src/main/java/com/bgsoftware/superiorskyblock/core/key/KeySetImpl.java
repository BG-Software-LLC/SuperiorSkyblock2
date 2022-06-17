package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KeySetImpl extends AbstractSet<Key> implements KeySet {

    private final Set<String> innerSet;

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
        this.innerSet.addAll(keys);
    }

    private KeySetImpl(Supplier<Set<String>> setCreator) {
        this.innerSet = setCreator.get();
    }

    @Override
    @NotNull
    public Iterator<Key> iterator() {
        return asSet().iterator();
    }

    @Override
    public int size() {
        return innerSet.size();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Key && (innerSet.contains(o.toString()) || (!((Key) o).getSubKey().isEmpty() && innerSet.contains(((Key) o).getGlobalKey())));
    }

    @Override
    public boolean add(Key key) {
        return innerSet.add(key.toString());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Key ? innerSet.remove(o.toString()) : innerSet.remove(o);
    }

    @Override
    public Key getKey(Key original) {
        return getKey(original, null);
    }

    @Override
    public Key getKey(Key original, @Nullable Key def) {
        if (innerSet.contains(original.toString()))
            return original;
        else if (innerSet.contains(original.getGlobalKey()))
            return KeyImpl.of(original.getGlobalKey(), "");
        else
            return def;
    }

    @Override
    public Set<Key> asSet() {
        return innerReflectedSet == null ? (innerReflectedSet = new InnerReflectedSet()) : innerReflectedSet;
    }

    private class InnerReflectedSet implements Set<Key> {

        @Override
        public int size() {
            return KeySetImpl.this.innerSet.size();
        }

        @Override
        public boolean isEmpty() {
            return KeySetImpl.this.innerSet.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return KeySetImpl.this.innerSet.contains(o);
        }

        @NotNull
        @Override
        public Iterator<Key> iterator() {
            return new SetIterator();
        }

        @Override
        public Object @NotNull [] toArray() {
            return KeySetImpl.this.innerSet.toArray();
        }

        @Override
        public <T> T @NotNull [] toArray(T @NotNull [] a) {
            return KeySetImpl.this.innerSet.toArray(a);
        }

        @Override
        public boolean add(Key key) {
            return KeySetImpl.this.innerSet.add(key.toString());
        }

        @Override
        public boolean remove(Object o) {
            return KeySetImpl.this.innerSet.remove(o);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return KeySetImpl.this.innerSet.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Key> c) {
            boolean added = false;
            for (Key key : c) {
                added |= add(key);
            }
            return added;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return KeySetImpl.this.innerSet.retainAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return KeySetImpl.this.innerSet.removeAll(c);
        }

        @Override
        public void clear() {
            KeySetImpl.this.innerSet.clear();
        }
    }

    private class SetIterator implements Iterator<Key> {

        final Iterator<String> innerSetIterator = KeySetImpl.this.innerSet.iterator();

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
