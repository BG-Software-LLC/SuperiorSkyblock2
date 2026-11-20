package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.objects.Enumerable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class UnparsedEnumerateSet<K extends Enumerable> extends EnumerateSet<K> {

    private final Set<String> unparsedNames = new HashSet<>();

    public UnparsedEnumerateSet(Collection<K> enumerables) {
        super(enumerables);
    }

    public boolean addName(String name) {
        if (name == null)
            return false;
        K key = parseName(name);

        // Names that are not registered yet are kept, so they can be matched once they are registered.
        return key == null ? unparsedNames.add(normalizeName(name)) : add(key);
    }

    @Override
    public boolean contains(K key) {
        if (super.contains(key))
            return true;

        if (unparsedNames.isEmpty())
            return false;

        if (unparsedNames.remove(normalizeName(getName(key)))) {
            add(key);
            return true;
        }

        return false;
    }

    private static String normalizeName(String name) {
        return name.toUpperCase(Locale.ENGLISH);
    }

    @Nullable
    protected abstract K parseName(String name);

    protected abstract String getName(K key);

}
