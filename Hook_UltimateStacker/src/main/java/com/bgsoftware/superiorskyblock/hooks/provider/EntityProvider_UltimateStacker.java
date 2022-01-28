package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.stackable.entity.EntityStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class EntityProvider_UltimateStacker implements EntityProvider {

    private final UltimateStacker instance = UltimateStacker.getInstance();

    @Override
    public int getEntityAmount(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return 1;

        EntityStack entityStack = instance.getEntityStackManager().getStack((LivingEntity) entity);

        return entityStack == null ? 1 : entityStack.getAmount();
    }

}
