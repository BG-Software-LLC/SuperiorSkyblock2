package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import com.songoda.ultimatestacker.UltimateStacker;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class EntityProvider_UltimateStacker implements EntityProvider {

    private final UltimateStacker instance = UltimateStacker.getInstance();

    @Override
    public int getEntityAmount(Entity entity) {
        return !(entity instanceof LivingEntity) ? 1 :
                instance.getEntityStackManager().getStack((LivingEntity) entity).getAmount();
    }

}
