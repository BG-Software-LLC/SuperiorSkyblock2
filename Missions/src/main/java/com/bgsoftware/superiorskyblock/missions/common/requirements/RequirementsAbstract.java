package com.bgsoftware.superiorskyblock.missions.common.requirements;

import com.google.common.collect.ForwardingSet;

import java.util.Set;

public abstract class RequirementsAbstract<E> extends ForwardingSet<E> implements IRequirements<E> {

    private final Set<E> handle;
    private boolean containsAll = false;

    public RequirementsAbstract(Set<E> handle) {
        this.handle = handle;
    }

    @Override
    public boolean add(E element) {
        if (containsAll)
            return true;

        if (isAllElement(element)) {
            this.containsAll = true;
            return true;
        }

        return super.add(element);
    }

    @Override
    public boolean contains(Object object) {
        return isContainsAll() || super.contains(object);
    }

    public boolean isContainsAll() {
        return this.containsAll;
    }

    @Override
    protected Set<E> delegate() {
        return this.handle;
    }

    protected abstract boolean isAllElement(E element);

}
