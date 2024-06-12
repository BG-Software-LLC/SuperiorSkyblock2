package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;

public class EntityTypeKeyMap<V> extends AbstractKeyMap<EntityTypeKey, V> {

    public EntityTypeKeyMap(KeyMapStrategy strategy) {
        super(strategy, EntityTypeKey.class);
    }

    @Override
    public String toString() {
        return "EntityTypeKeyMap" + super.toString();
    }

}
