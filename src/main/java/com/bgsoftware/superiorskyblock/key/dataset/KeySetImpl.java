package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class KeySetImpl extends AbstractSet<Key> implements KeySet {

    private final Set<String> set;

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
        return this.set.stream().map(KeyImpl::of).collect(Collectors.toSet());
    }

}
