package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;

public class MaterialKeySet extends AbstractKeySet<MaterialKey> {

    public MaterialKeySet(KeySetStrategy strategy) {
        super(strategy, MaterialKey.class);
    }

    @Override
    public String toString() {
        return "MaterialKeySet" + super.toString();
    }

}
