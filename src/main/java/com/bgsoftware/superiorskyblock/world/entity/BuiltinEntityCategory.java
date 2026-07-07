package com.bgsoftware.superiorskyblock.world.entity;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;

import java.lang.ref.WeakReference;
import java.util.Objects;

public enum BuiltinEntityCategory {

    TAMEABLE {
        @Override
        protected boolean isEntityListedInternal(Class<? extends Entity> entityClass) {
            return Tameable.class.isAssignableFrom(entityClass);
        }
    },
    VEHICLE {
        @Override
        protected boolean isEntityListedInternal(Class<? extends Entity> entityClass) {
            return Vehicle.class.isAssignableFrom(entityClass) && !Creature.class.isAssignableFrom(entityClass);
        }
    },
    MONSTER {
        @Override
        protected boolean isEntityListedInternal(Class<? extends Entity> entityClass) {
            return Monster.class.isAssignableFrom(entityClass) ||
                    Slime.class.isAssignableFrom(entityClass) || Flying.class.isAssignableFrom(entityClass) ||
                    entityClass == HOGLIN_CLASS || entityClass == SKELETON_HORSE_CLASS ||
                    entityClass == ZOMBIE_HORSE_CLASS || entityClass == ZOMBIE_NAUTILUS_CLASS;
        }
    },
    ANIMAL {
        @Override
        protected boolean isEntityListedInternal(Class<? extends Entity> entityClass) {
            return !BuiltinEntityCategory.MONSTER.isEntityListedInternal(entityClass) &&
                    (Creature.class.isAssignableFrom(entityClass) || Ambient.class.isAssignableFrom(entityClass));
        }
    };

    @Nullable
    private static final Class<?> HOGLIN_CLASS = getEntityTypeClass("org.bukkit.entity.Hoglin");
    @Nullable
    private static final Class<?> SKELETON_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.SkeletonHorse");
    @Nullable
    private static final Class<?> ZOMBIE_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieHorse");
    @Nullable
    private static final Class<?> ZOMBIE_NAUTILUS_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieNautilus");

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeySet entities = getEntitiesInternal();
    private WeakReference<EntityCategory> entityCategoryReference;

    public KeySet getEntities() {
        return entities;
    }

    @NotNull
    public EntityCategory getEntityCategory() {
        if (this.entityCategoryReference == null) {
            EntityCategory entityCategory = plugin.getSettings().getEntityCategoriesMap().getCategoryByName(name());
            this.entityCategoryReference = new WeakReference<>(entityCategory);
            return Objects.requireNonNull(entityCategory);
        }

        EntityCategory entityCategory = this.entityCategoryReference.get();
        if (entityCategory == null) {
            entityCategory = plugin.getSettings().getEntityCategoriesMap().getCategoryByName(name());
            this.entityCategoryReference = new WeakReference<>(entityCategory);
        }

        return Objects.requireNonNull(entityCategory);
    }

    private KeySet getEntitiesInternal() {
        KeySet entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null && isEntityListedInternal(entityClass)) {
                entities.add(Keys.of(entityType));
            }
        }
        return entities.isEmpty() ? KeySets.createEmptySet() : KeySets.unmodifiableKeySet(entities);
    }

    protected boolean isEntityListedInternal(Class<? extends Entity> entityClass) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    private static Class<?> getEntityTypeClass(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException error) {
            return null;
        }
    }

}
