package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.NotNull;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
                    elements.remove(removalNotification.getKey());
                })
                .build();
    }

    @Override
    public int size() {
        refreshLifeTime();
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public boolean contains(Object o) {
        refreshLifeTime(o);
        return elements.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        refreshLifeTime();
        return elements.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        refreshLifeTime();
        return elements.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        refreshLifeTime();
        return elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        refreshLifeTime(e);
        boolean result = elements.add(e);
        if (result)
            this.elementsLifeTime.put(e, DUMMY);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        this.elementsLifeTime.invalidate(o);
        return elements.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        c.forEach(this::refreshLifeTime);
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean result = false;
        for (E element : c)
            result |= add(element);
        return result;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.elementsLifeTime.invalidateAll((Iterable<E>) c.iterator());
        return elements.removeAll(c);
    }

    @Override
    public void clear() {
        elements.clear();
        this.elementsLifeTime.invalidateAll();
    }

    private void refreshLifeTime(Object o) {
        try {
            this.elementsLifeTime.get((E) o, () -> null);
        } catch (Throwable ignored) {

        }
    }

    private void refreshLifeTime() {
        this.elementsLifeTime.size();
    }

}
