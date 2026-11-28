package com.bgsoftware.superiorskyblock.missions.common.requirements;

import java.util.Collection;
import java.util.LinkedHashSet;

public class Requirements extends RequirementsAbstract<String> {

    public Requirements(Collection<String> elements) {
        super(new LinkedHashSet<>());
        addAll(elements);
    }

    @Override
    protected boolean isAllElement(String element) {
        return element.equalsIgnoreCase("all");
    }
}
