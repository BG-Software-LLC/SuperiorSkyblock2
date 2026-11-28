package com.bgsoftware.superiorskyblock.missions.common.requirements;

import java.util.Set;

public interface IRequirements<K> extends Set<K> {

    boolean isContainsAll();

}
