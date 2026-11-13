package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class AutoRemovalCollection<E> implements Collection<E> {

    private static final Object DUMMY = new Object();

    private final Collection<E> elements;
    private final Cache<E, Object> elementsLifeTime;

    public static <E> AutoRemovalCollection<E> newHashSet(long removalDelay, TimeUnit timeUnit) {
        return new AutoRemovalCollection<>(removalDelay, timeUnit, HashSet::new);
    }

    public static <E> AutoRemovalCollection<E> newArrayList(long removalDelay, TimeUnit timeUnit) {
        return new AutoRemovalCollection<>(removalDelay, timeUnit, ArrayList::new);
    }

    private AutoRemovalCollection(long removalDelay, TimeUnit timeUnit, Supplier<Collection<E>> collectionSupplier) {
        this.elements = collectionSupplier.get();
        this.elementsLifeTime = CacheBuilder.newBuilder()
                .expireAfterWrite(removalDelay, timeUnit)
                .removalListener(removalNotification -> {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED) {
                        synchronized (AutoRemovalCollection.this) {
                            elements.removeIf(element -> Objects.equals(element, removalNotification.getKey()));
                        }
                    }
                })
                .build();
    }

    @Override
    public synchronized int size() {
        refreshLifeTime();
        return elements.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public synchronized boolean contains(Object o) {
        refreshLifeTime(o);
        return elements.contains(o);
    }

    @NotNull
    @Override
    public synchronized Iterator<E> iterator() {
        refreshLifeTime();
        Iterator<E> iterator = new ArrayList<>(elements).iterator();
        return new Iterator<E>() {
            private E current;
            private boolean removable;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                current = iterator.next();
                removable = true;
                return current;
            }

            @Override
            public void remove() {
                if (!removable)
                    throw new IllegalStateException();
                AutoRemovalCollection.this.remove(current);
                removable = false;
            }
        };
    }

    @NotNull
    @Override
    public synchronized Object[] toArray() {
        refreshLifeTime();
        return elements.toArray();
    }

    @NotNull
    @Override
    public synchronized <T> T[] toArray(@NotNull T[] a) {
        refreshLifeTime();
        return elements.toArray(a);
    }

    @Override
    public synchronized boolean add(E e) {
        refreshLifeTime(e);
        boolean result = elements.add(e);
        if (result)
            this.elementsLifeTime.put(e, DUMMY);
        return result;
    }

    @Override
    public synchronized boolean remove(Object o) {
        boolean removed = elements.remove(o);
        if (!elements.contains(o))
            this.elementsLifeTime.invalidate(o);
        return removed;
    }

    @Override
    public synchronized boolean containsAll(@NotNull Collection<?> c) {
        c.forEach(this::refreshLifeTime);
        return elements.containsAll(c);
    }

    @Override
    public synchronized boolean addAll(@NotNull Collection<? extends E> c) {
        boolean result = false;
        for (E element : c)
            result |= add(element);
        return result;
    }

    @Override
    public synchronized boolean retainAll(@NotNull Collection<?> c) {
        refreshLifeTime();
        Collection<E> removed = new ArrayList<>(elements);
        removed.removeAll(c);
        boolean changed = elements.retainAll(c);
        this.elementsLifeTime.invalidateAll(removed);
        return changed;
    }

    @Override
    public synchronized boolean removeAll(@NotNull Collection<?> c) {
        Collection<?> removed = new ArrayList<>(c);
        boolean changed = elements.removeAll(removed);
        this.elementsLifeTime.invalidateAll(removed);
        return changed;
    }

    @Override
    public synchronized void clear() {
        elements.clear();
        this.elementsLifeTime.invalidateAll();
    }

    private void refreshLifeTime(Object o) {
        this.elementsLifeTime.getIfPresent(o);
        refreshLifeTime();
    }

    private void refreshLifeTime() {
        this.elementsLifeTime.cleanUp();
    }

}
