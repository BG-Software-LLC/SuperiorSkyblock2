package com.bgsoftware.superiorskyblock.core.key;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.collections.EntityTypeKeySet;
import com.bgsoftware.superiorskyblock.core.key.collections.LazyLoadedKeySet;
import com.bgsoftware.superiorskyblock.core.key.collections.MaterialKeySet;
import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class KeySets {

    private static final KeySet EMPTY_SET = new EmptyKeySet();

    private KeySets() {

    }

    public static KeySet createEmptySet() {
        return EMPTY_SET;
    }

    public static KeySet unmodifiableKeySet(KeySet delegate) {
        return delegate == EMPTY_SET ? createEmptySet() : new UnmodifiableKeySet(delegate);
    }

    public static KeySet createHashSet(KeyIndicator keyIndicator) {
        return createSet(keyIndicator, () -> new HashSet());
    }

    public static KeySet createSet(KeyIndicator keyIndicator, Supplier<Set> setSupplier) {
        switch (keyIndicator) {
            case MATERIAL:
                return MaterialKeySet.createSet(setSupplier);
            case ENTITY_TYPE:
                return EntityTypeKeySet.createSet(setSupplier);
        }

        return LazyLoadedKeySet.createSet(setSupplier);
    }

    public static KeySet createHashSet(KeyIndicator keyIndicator, Collection<String> rawKeys) {
        KeySet keySet = createHashSet(keyIndicator);

        Function<String, Key> keyCreator;
        switch (keyIndicator) {
            case MATERIAL:
                keyCreator = Keys::ofMaterialAndData;
                break;
            case ENTITY_TYPE:
                keyCreator = Keys::ofEntityType;
                break;
            default:
                keyCreator = Keys::ofCustom;
                break;
        }

        rawKeys.forEach(key -> keySet.add(keyCreator.apply(key)));

        return keySet;
    }

    private static class EmptyKeySet implements KeySet {

        @Nullable
        @Override
        public Key getKey(Key original) {
            return null;
        }

        @Override
        public Key getKey(Key original, Key def) {
            return def;
        }

        @Override
        public Set<Key> asSet() {
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return c.isEmpty();
        }

        @NotNull
        @Override
        public Iterator<Key> iterator() {
            return Iterators.emptyIterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            if (a.length > 0)
                a[0] = null;
            return a;
        }

        @Override
        public void forEach(Consumer<? super Key> action) {
            // Do nothing.
        }

        @Override
        public boolean removeIf(Predicate<? super Key> filter) {
            return false;
        }

        @Override
        public Spliterator<Key> spliterator() {
            return Spliterators.emptySpliterator();
        }

        @Override
        public boolean add(Key key) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Key> c) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Cannot modify EmptyKeySet");
        }

    }

    private static class UnmodifiableKeySet implements KeySet {

        private final KeySet delegate;

        UnmodifiableKeySet(KeySet delegate) {
            this.delegate = delegate;
        }

        @Nullable
        @Override
        public Key getKey(Key original) {
            return this.delegate.getKey(original);
        }

        @Override
        public Key getKey(Key original, Key def) {
            return this.delegate.getKey(original, def);
        }

        @Override
        public Set<Key> asSet() {
            return this;
        }

        @Override
        public int size() {
            return this.delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return this.delegate.contains(o);
        }

        @NotNull
        @Override
        public Iterator<Key> iterator() {
            return this.delegate.iterator();
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return this.delegate.toArray();
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            return this.delegate.toArray(a);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return this.delegate.containsAll(c);
        }

        @Override
        public boolean add(Key key) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends Key> c) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Cannot modify UnmodifiableKeySet");
        }

    }

}
