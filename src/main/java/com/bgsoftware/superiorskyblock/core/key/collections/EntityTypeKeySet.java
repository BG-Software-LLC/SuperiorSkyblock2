package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.superiorskyblock.core.key.types.EntityTypeKey;

public class EntityTypeKeySet extends AbstractKeySet<EntityTypeKey> {

    public EntityTypeKeySet(KeySetStrategy strategy) {
        super(strategy, EntityTypeKey.class);
    }

    @Override
    public String toString() {
        return "EntityTypeKeySet" + super.toString();
    }

}
