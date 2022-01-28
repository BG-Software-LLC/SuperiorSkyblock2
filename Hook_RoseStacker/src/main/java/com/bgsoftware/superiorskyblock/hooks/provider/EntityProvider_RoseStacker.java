package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.EntityProvider;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public final class EntityProvider_RoseStacker implements EntityProvider {

    @Override
    public int getEntityAmount(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return 1;

        StackedEntity stackedEntity = RoseStackerAPI.getInstance().getStackedEntity((LivingEntity) entity);

        return stackedEntity == null ? 1 : stackedEntity.getStackSize();
    }

}
