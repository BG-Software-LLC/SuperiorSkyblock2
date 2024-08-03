package com.bgsoftware.superiorskyblock.core.key.collections;

import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;

public class MaterialKeyMap<V> extends AbstractKeyMap<MaterialKey, V> {

    public MaterialKeyMap(KeyMapStrategy strategy) {
        super(strategy, MaterialKey.class);
    }

    @Override
    public String toString() {
        return "MaterialKeyMap" + super.toString();
    }


}
