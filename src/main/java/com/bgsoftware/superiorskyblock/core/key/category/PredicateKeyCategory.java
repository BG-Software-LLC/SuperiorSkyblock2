package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.util.function.Predicate;

public class PredicateKeyCategory extends BaseKeyCategory {

    private final Predicate<Key> predicate;

    public PredicateKeyCategory(String name, Predicate<Key> predicate) {
        super(name);
        this.predicate = predicate;
    }

    @Override
    public boolean isListed(Key key) {
        return this.predicate.test(key);
    }

}
