package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import org.bukkit.entity.Entity;

public final class EntityProvider_Default implements EntityProvider {

    @Override
    public int getEntityAmount(Entity entity) {
        return 1;
    }

}
