package com.bgsoftware.superiorskyblock.missions.common.requirements;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;

import java.util.Collection;
import java.util.LinkedHashSet;

public class KeyRequirements extends RequirementsAbstract<Key> {

    public KeyRequirements(Collection<Key> elements) {
        super(KeySet.createKeySet(LinkedHashSet::new));
        addAll(elements);
    }

    @Override
    protected boolean isAllElement(Key element) {
        return element.getGlobalKey().equalsIgnoreCase("ALL");
    }

    public Key getKey(Key original) {
        return ((KeySet) delegate()).getKey(original, original);
    }

}
