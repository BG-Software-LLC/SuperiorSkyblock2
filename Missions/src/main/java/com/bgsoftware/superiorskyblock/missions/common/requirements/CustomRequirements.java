package com.bgsoftware.superiorskyblock.missions.common.requirements;

import java.util.Collection;
import java.util.LinkedHashSet;

public class CustomRequirements<E> extends RequirementsAbstract<E> {

    public CustomRequirements() {
        super(new LinkedHashSet<>());
    }

    public CustomRequirements(Collection<E> elements) {
        this();
        addAll(elements);
    }

    @Override
    protected boolean isAllElement(E element) {
        return false;
    }

}
