package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public class UnparsedEnumerateSet<K extends Enumerable> extends EnumerateSet<K> {

    private final Set<String> unparsedNames = new HashSet<>();
    private final Function<String, K> parser;
    private final Function<K, String> nameProvider;

    public UnparsedEnumerateSet(Collection<K> enumerables, Function<String, K> parser, Function<K, String> nameProvider) {
        super(enumerables);
        this.parser = parser;
        this.nameProvider = nameProvider;
    }

    public boolean addName(String name) {
        try {
            return add(parser.apply(name));
        } catch (Throwable ignored) {
            if (name == null)
                return false;

            return unparsedNames.add(normalizeName(name));
        }
    }

    @Override
    public boolean contains(K key) {
        if (super.contains(key))
            return true;

        if (unparsedNames.remove(normalizeName(nameProvider.apply(key)))) {
            add(key);
            return true;
        }

        return false;
    }

    private static String normalizeName(String name) {
        return name.toUpperCase(Locale.ENGLISH);
    }

}
