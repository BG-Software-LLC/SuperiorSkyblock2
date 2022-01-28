package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class EntityProvider_WildStacker implements EntityProvider {

    @Override
    public int getEntityAmount(Entity entity) {
        return entity instanceof LivingEntity ? WildStackerAPI.getEntityAmount((LivingEntity) entity) : 1;
    }

}
