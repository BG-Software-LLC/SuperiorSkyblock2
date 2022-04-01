package com.bgsoftware.superiorskyblock.api.enums;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Comparator;

public enum TopIslandMembersSorting {

    /**
     * Sort members in the top-islands menu by their names.
     */
    NAMES,

    /**
     * Sort members in the top-islands menu by their roles.
     */
    ROLES;

    private Comparator<SuperiorPlayer> comparator = null;

    TopIslandMembersSorting() {
    }

    public void setComparator(Comparator<SuperiorPlayer> comparator) {
        if (this.comparator != null)
            throw new IllegalArgumentException("You cannot set a comparator after it was already been set.");

        this.comparator = comparator;
    }

    public Comparator<SuperiorPlayer> getComparator() {
        if (this.comparator == null)
            throw new RuntimeException(this + " was not initialized.");

        return comparator;
    }

}
