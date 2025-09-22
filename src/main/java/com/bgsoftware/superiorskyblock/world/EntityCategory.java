package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Vehicle;

import java.util.EnumSet;
import java.util.function.Predicate;

public enum EntityCategory {

    MONSTER(IslandPrivileges.MONSTER_DAMAGE, IslandPrivileges.MONSTER_SPAWN) {

        private final EnumSet<EntityType> MONSTER_TYPES = createEntityTypesSet(EntityCategory::isMonsterType);

        @Override
        boolean isFromCategory(EntityType entityType) {
            return MONSTER_TYPES.contains(entityType);
        }
    },
    // !!!Never put this above Monster category!!!
    ANIMAL(IslandPrivileges.ANIMAL_DAMAGE, IslandPrivileges.ANIMAL_SPAWN) {

        private final EnumSet<EntityType> ANIMAL_TYPES = createEntityTypesSet(EntityCategory::isAnimalType);

        @Override
        boolean isFromCategory(EntityType entityType) {
            return ANIMAL_TYPES.contains(entityType);
        }
    },
    PAINTING(IslandPrivileges.PAINTING, IslandPrivileges.BUILD) {
        @Override
        boolean isFromCategory(EntityType entityType) {
            return entityType == EntityType.PAINTING;
        }
    },
    ITEM_FRAME(IslandPrivileges.ITEM_FRAME, IslandPrivileges.BUILD) {
        @Override
        boolean isFromCategory(EntityType entityType) {
            return entityType == EntityType.ITEM_FRAME;
        }
    },
    VEHICLE(IslandPrivileges.MINECART_DAMAGE, IslandPrivileges.MINECART_PLACE) {

        private final EnumSet<EntityType> VEHICLE_TYPES = createEntityTypesSet(entityType -> {
            Class<? extends Entity> entityClass = entityType.getEntityClass();
            return entityClass != null && Vehicle.class.isAssignableFrom(entityClass);
        });

        @Override
        boolean isFromCategory(EntityType entityType) {
            return VEHICLE_TYPES.contains(entityType);
        }
    },
    UNKNOWN(IslandPrivileges.BREAK, IslandPrivileges.BUILD) {
        @Override
        boolean isFromCategory(EntityType entityType) {
            return true;
        }
    };

    private static final Class<?> HOGLIN_CLASS = getEntityTypeClass("org.bukkit.entity.Hoglin");
    private static final Class<?> SKELETON_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.SkeletonHorse");
    private static final Class<?> ZOMBIE_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieHorse");

    private final IslandPrivilege damagePrivilege;
    private final IslandPrivilege spawnPrivilege;

    EntityCategory(IslandPrivilege damagePrivilege, IslandPrivilege spawnPrivilege) {
        this.damagePrivilege = damagePrivilege;
        this.spawnPrivilege = spawnPrivilege;
    }

    public IslandPrivilege getDamagePrivilege() {
        return damagePrivilege;
    }

    public IslandPrivilege getSpawnPrivilege() {
        return spawnPrivilege;
    }

    abstract boolean isFromCategory(EntityType entityType);

    private static boolean isAnimalType(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        // This call is done after Monster category is checked.
        // Therefore, the checks below are enough.
        return entityClass != null &&
                // We check that the entity type is whether a Creature or an Ambient
                (Creature.class.isAssignableFrom(entityClass) || Ambient.class.isAssignableFrom(entityClass)) &&
                // We make sure this is not Hoglin; In Bukkit, they are considered Animals.
                entityClass != HOGLIN_CLASS &&
                // We make sure this is not Skeleton/Zombie horses; In Bukkit, they are considered Animals.
                entityClass != SKELETON_HORSE_CLASS && entityClass != ZOMBIE_HORSE_CLASS;
    }

    private static boolean isMonsterType(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && (Monster.class.isAssignableFrom(entityClass) ||
                // Checks for slimes & magma cubes
                Slime.class.isAssignableFrom(entityClass) ||
                // Checks for ghasts and phantoms
                Flying.class.isAssignableFrom(entityClass) ||
                // In Bukkit, Hoglins are considered as Animals for no reason.
                // We want to explicitly tell the plugin its monster type.
                entityClass == HOGLIN_CLASS ||
                // In Bukkit, Skeleton and Zombie horses are considered Horses,
                // and therefore they are animals. We want to explicit tell the plugin
                // they are monsters.
                entityClass == SKELETON_HORSE_CLASS || entityClass == ZOMBIE_HORSE_CLASS
        );
    }

    private static EnumSet<EntityType> createEntityTypesSet(Predicate<EntityType> predicate) {
        EnumSet<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);
        for (EntityType entityType : EntityType.values()) {
            if (predicate.test(entityType))
                entityTypes.add(entityType);
        }
        return entityTypes;
    }

    private static Class<?> getEntityTypeClass(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException error) {
            return null;
        }
    }

}
