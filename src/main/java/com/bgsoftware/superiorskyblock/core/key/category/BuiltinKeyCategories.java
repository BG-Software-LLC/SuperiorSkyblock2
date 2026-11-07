package com.bgsoftware.superiorskyblock.core.key.category;

import com.bgsoftware.common.annotations.Nullable;
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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class BuiltinKeyCategories {

    @Nullable
    private static final Class<?> HOGLIN_CLASS = getEntityTypeClass("org.bukkit.entity.Hoglin");
    @Nullable
    private static final Class<?> SKELETON_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.SkeletonHorse");
    @Nullable
    private static final Class<?> ZOMBIE_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieHorse");
    @Nullable
    private static final Class<?> ZOMBIE_NAUTILUS_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieNautilus");
    @Nullable
    private static final Class<?> SHULKER_CLASS = getEntityTypeClass("org.bukkit.entity.Shulker");

    public static final KeyCategoryId ALL = KeyCategoryId.register("ALL");
    public static final KeyCategoryId TAMED = KeyCategoryId.register("TAMED");
    public static final KeyCategoryId VEHICLE = KeyCategoryId.register("VEHICLE");
    public static final KeyCategoryId MONSTER = KeyCategoryId.register("MONSTER");
    public static final KeyCategoryId ANIMAL = KeyCategoryId.register("ANIMAL");

    public static final List<KeyCategoryId> ENTITY_CATEGORIES = Arrays.asList(TAMED, VEHICLE, MONSTER, ANIMAL);

    public static PredicateKeyCategory createAllCategory() {
        return new PredicateKeyCategory(ALL.getName(), key -> true);
    }

    @Nullable
    public static FixedKeyCategory createEntitiesCategory(KeyCategoryId key) {
        if (key == TAMED) {
            return createEntitiesCategory(TAMED, Tameable.class::isAssignableFrom);
        } else if (key == VEHICLE) {
            return createEntitiesCategory(VEHICLE, BuiltinKeyCategories::isVehicle);
        } else if (key == MONSTER) {
            return createEntitiesCategory(MONSTER, BuiltinKeyCategories::isMonster);
        } else if (key == ANIMAL) {
            return createEntitiesCategory(ANIMAL, BuiltinKeyCategories::isAnimal);
        } else {
            return null;
        }
    }

    private BuiltinKeyCategories() {

    }

    private static boolean isVehicle(Class<? extends Entity> entityClass) {
        return Vehicle.class.isAssignableFrom(entityClass) && !Creature.class.isAssignableFrom(entityClass);
    }

    private static boolean isMonster(Class<? extends Entity> entityClass) {
        return Monster.class.isAssignableFrom(entityClass) ||
                Slime.class.isAssignableFrom(entityClass) || Flying.class.isAssignableFrom(entityClass) ||
                entityClass == HOGLIN_CLASS || entityClass == SKELETON_HORSE_CLASS ||
                entityClass == ZOMBIE_HORSE_CLASS || entityClass == ZOMBIE_NAUTILUS_CLASS ||
                entityClass == SHULKER_CLASS;
    }

    private static boolean isAnimal(Class<? extends Entity> entityClass) {
        return !isMonster(entityClass) &&
                (Creature.class.isAssignableFrom(entityClass) || Ambient.class.isAssignableFrom(entityClass));
    }

    private static FixedKeyCategory createEntitiesCategory(KeyCategoryId key, Predicate<Class<? extends Entity>> predicate) {
        KeySet entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            if (entityClass != null && predicate.test(entityClass)) {
                entities.add(Keys.of(entityType));
            }
        }
        return entities.isEmpty() ? new FixedKeyCategory(key.getName(), KeySets.createEmptySet()) :
                new FixedKeyCategory(key.getName(), entities);
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
