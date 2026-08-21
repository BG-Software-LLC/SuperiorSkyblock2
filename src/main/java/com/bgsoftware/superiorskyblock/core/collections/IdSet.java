package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Identified;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.collections.view.IntIterator;
import com.bgsoftware.superiorskyblock.core.collections.view.IntSetView;
import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

public abstract class IdSet<V extends Identified> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final boolean isLinked;
    protected final IntSetView ids;

    protected IdSet(boolean isLinked) {
        this.isLinked = isLinked;
        this.ids = isLinked ? CollectionsFactory.createIntLinkedHashSet() : CollectionsFactory.createIntHashSet();
    }

    public static IdSet<SuperiorPlayer> newPlayersLinkedSet() {
        return new SuperiorPlayerIdSet(true);
    }

    public static IdSet<Island> newIslandsLinkedSet() {
        return new IslandIdSet(true);
    }

    public int size() {
        return this.ids.size();
    }

    public boolean add(V value) {
        return add(value.getId());
    }

    public boolean add(int id) {
        return this.ids.add(id);
    }

    public boolean addAll(IdSet<V> other) {
        if (other.size() == 0)
            return false;

        boolean res = false;
        IntIterator iterator = other.ids.iterator();
        while (iterator.hasNext()) {
            res |= this.ids.add(iterator.next());
        }
        return res;
    }

    public boolean remove(V value) {
        return remove(value.getId());
    }

    public boolean remove(int id) {
        return this.ids.remove(id);
    }

    public boolean contains(V value) {
        return contains(value.getId());
    }

    public boolean contains(int id) {
        return this.ids.contains(id);
    }

    public Iterator<V> iterator() {
        return size() == 0 ? Iterators.emptyIterator() : new IteratorImpl();
    }

    public void forEach(Consumer<V> consumer) {
        if (size() == 0)
            return;

        Iterator<V> iterator = iterator();
        while (iterator.hasNext())
            consumer.accept(iterator.next());
    }

    public List<V> asListView() {
        return asListView(null, null);
    }

    public List<V> asListView(@Nullable IntPredicate predicate) {
        return asListView(predicate, null);
    }

    public List<V> asListView(@Nullable Consumer<IntSetView> beforeCreateCallback) {
        return asListView(null, beforeCreateCallback);
    }

    public List<V> asListView(@Nullable IntPredicate predicate, @Nullable Consumer<IntSetView> beforeCreateCallback) {
        if (size() == 0)
            return Collections.emptyList();

        IdSet<V> copy = newInstanceInternal();
        if (predicate == null && beforeCreateCallback == null) {
            copy.addAll(this);
        } else {
            IntIterator iterator = this.ids.iterator();
            while (iterator.hasNext()) {
                int value = iterator.next();
                if (predicate == null || predicate.test(value))
                    copy.ids.add(value);
            }
            if (beforeCreateCallback != null)
                beforeCreateCallback.accept(copy.ids);
            if (copy.size() == 0)
                return Collections.emptyList();
        }

        return new ListViewImpl<>(copy);
    }

    @Override
    public boolean equals(Object o) {
        return o == this || this.ids.equals(o);
    }

    @Override
    public int hashCode() {
        return this.ids.hashCode();
    }

    protected abstract IdSet<V> newInstanceInternal();

    protected abstract V getValueFromId(int id);

    private class IteratorImpl implements Iterator<V> {

        private final IntIterator delegate = ids.iterator();

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public V next() {
            return getValueFromId(this.delegate.next());
        }

        @Override
        public void remove() {
            this.delegate.remove();
        }

    }

    private static class ListViewImpl<V extends Identified> extends AbstractList<V> {

        private final IdSet<V> handle;

        ListViewImpl(IdSet<V> handle) {
            this.handle = handle;
        }

        @Override
        public int size() {
            return this.handle.size();
        }

        @Override
        public boolean equals(Object o) {
            return this == o || this.handle.equals(o);
        }

        @Override
        public int hashCode() {
            return this.handle.hashCode();
        }

        @Override
        public V get(int i) {
            return Iterators.get(this.handle.iterator(), i);
        }

        @Override
        public V set(int i, V v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int i, V v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            return o == null ? -1 : Iterators.indexOf(this.handle.iterator(), e -> e.equals(o));
        }

        @Override
        public int lastIndexOf(Object o) {
            if (o == null)
                return -1;

            Iterator<V> iterator = this.handle.iterator();
            int elementIdx = -1;
            int currIdx = 0;
            while (iterator.hasNext()) {
                if (o.equals(iterator.next())) {
                    elementIdx = currIdx;
                }
                ++currIdx;
            }
            return elementIdx;
        }

        @Override
        public boolean addAll(int i, Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(@NotNull UnaryOperator<V> unaryOperator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sort(@Nullable Comparator<? super V> comparator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<V> listIterator(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> subList(int i, int i1) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<V> iterator() {
            return this.handle.iterator();
        }
    }

    private static class SuperiorPlayerIdSet extends IdSet<SuperiorPlayer> {

        public SuperiorPlayerIdSet(boolean isLinked) {
            super(isLinked);
        }

        @Override
        protected SuperiorPlayer getValueFromId(int id) {
            return plugin.getPlayers().getPlayersContainer().getSuperiorPlayer(id);
        }

        @Override
        protected IdSet<SuperiorPlayer> newInstanceInternal() {
            return new SuperiorPlayerIdSet(this.isLinked);
        }
    }

    private static class IslandIdSet extends IdSet<Island> {

        IslandIdSet(boolean isLinked) {
            super(isLinked);
        }

        @Override
        protected Island getValueFromId(int id) {
            return plugin.getGrid().getIslandsContainer().getIslandById(id);
        }

        @Override
        public IdSet<Island> newInstanceInternal() {
            return new IslandIdSet(this.isLinked);
        }

    }

}
