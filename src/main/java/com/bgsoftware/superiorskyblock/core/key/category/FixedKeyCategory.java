package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;

public class FixedKeyCategory extends BaseKeyCategory {

    private final KeySet elements;

    public FixedKeyCategory(String name, KeySet elements) {
        super(name);
        this.elements = KeySets.unmodifiableKeySet(elements);
    }

    public KeySet getKeys() {
        return this.elements;
    }

    @Override
    public boolean isListed(Key key) {
        return this.elements.contains(key);
    }

}
