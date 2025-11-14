package com.bgsoftware.superiorskyblock.world.entity;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Vehicle;

import java.util.Map;
import java.util.function.Predicate;

public class EntityCategories {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Class<?> HOGLIN_CLASS = getEntityTypeClass("org.bukkit.entity.Hoglin");
    private static final Class<?> SKELETON_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.SkeletonHorse");
    private static final Class<?> ZOMBIE_HORSE_CLASS = getEntityTypeClass("org.bukkit.entity.ZombieHorse");
    @Nullable
    private static final EntityType GLOW_ITEM_FRAME = EnumHelper.getEnum(EntityType.class, "GLOW_ITEM_FRAME");

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, EntityCategories::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        EntityCategory.clear();

        // Load built-in categories with low priority
        new Builder("ANIMAL", createEntityTypesSet(EntityCategories::isAnimalType))
                .spawnerSpawnFlag(IslandFlags.SPAWNER_ANIMALS_SPAWN).naturalSpawnFlag(IslandFlags.NATURAL_ANIMALS_SPAWN).build();
        new Builder("MONSTER", createEntityTypesSet(EntityCategories::isMonsterType)).build();

        // Load config categories, which can override the categories above
        Map<String, KeySet> entityCategories = plugin.getSettings().getEntityCategories();
        entityCategories.forEach((name, entityTypes) -> {
            new Builder(name, entityTypes).build();
        });

        // Register built-in categories with high priority
        new Builder("PAINTING", createEntityTypesSet(EntityType.PAINTING))
                .damagePrivilege(IslandPrivileges.PAINTING).spawnPrivilege(IslandPrivileges.BUILD)
                .naturalSpawnFlag(null).spawnerSpawnFlag(null).build();
        new Builder("ITEM_FRAME", createEntityTypesSet(EntityType.ITEM_FRAME, GLOW_ITEM_FRAME))
                .damagePrivilege(IslandPrivileges.ITEM_FRAME).spawnPrivilege(IslandPrivileges.BUILD)
                .naturalSpawnFlag(null).spawnerSpawnFlag(null).build();
        new Builder("ARMOR_STAND", createEntityTypesSet(EntityType.ARMOR_STAND))
                .damagePrivilege(IslandPrivileges.BREAK).spawnPrivilege(IslandPrivileges.BUILD)
                .naturalSpawnFlag(null).spawnerSpawnFlag(null).build();
        new Builder("VEHICLE", createEntityTypesSet(EntityCategories::isVehicleType))
                .damagePrivilege(IslandPrivileges.MINECART_DAMAGE).spawnPrivilege(IslandPrivileges.MINECART_PLACE)
                .naturalSpawnFlag(null).spawnerSpawnFlag(null).build();
    }

    private EntityCategories() {

    }

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

    private static boolean isVehicleType(EntityType entityType) {
        Class<? extends Entity> entityClass = entityType.getEntityClass();
        return entityClass != null && Vehicle.class.isAssignableFrom(entityClass) && !Creature.class.isAssignableFrom(entityClass);
    }

    private static KeySet createEntityTypesSet(Predicate<EntityType> predicate) {
        KeySet entityTypes = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : EntityType.values()) {
            if (predicate.test(entityType))
                entityTypes.add(Keys.of(entityType));
        }
        return entityTypes;
    }

    private static KeySet createEntityTypesSet(EntityType... entityTypes) {
        KeySet entities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        for (EntityType entityType : entityTypes) {
            if (entityType != null)
                entities.add(Keys.of(entityType));
        }
        return entities;
    }

    private static Class<?> getEntityTypeClass(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException error) {
            return null;
        }
    }

    private static IslandPrivilege getOrRegisterPrivilege(@Nullable String name) {
        if (name == null)
            return null;

        try {
            return IslandPrivilege.getByName(name);
        } catch (NullPointerException error) {
            IslandPrivilege.register(name);
            return IslandPrivilege.getByName(name);
        }
    }

    private static IslandFlag getOrRegisterFlag(@Nullable String name) {
        if (name == null)
            return null;

        try {
            return IslandFlag.getByName(name);
        } catch (NullPointerException error) {
            IslandFlag.register(name);
            return IslandFlag.getByName(name);
        }
    }

    private static class Builder {

        private final KeySet entityTypes;

        private String damagePrivilege;
        private String spawnPrivilege;
        private String spawnerSpawnFlag;
        private String naturalSpawnFlag;

        public Builder(String name, KeySet entityTypes) {
            this.entityTypes = entityTypes;
            this.damagePrivilege = name + "_DAMAGE";
            this.spawnPrivilege = name + "_SPAWN";
            this.spawnerSpawnFlag = "SPAWNER_" + name + "_SPAWN";
            this.naturalSpawnFlag = "NATURAL_" + name + "_SPAWN";
        }

        public Builder damagePrivilege(@Nullable IslandPrivilege damagePrivilege) {
            this.damagePrivilege = damagePrivilege == null ? null : damagePrivilege.getName();
            return this;
        }

        public Builder spawnPrivilege(@Nullable IslandPrivilege spawnPrivilege) {
            this.spawnPrivilege = spawnPrivilege == null ? null : spawnPrivilege.getName();
            return this;
        }

        public Builder spawnerSpawnFlag(@Nullable IslandFlag spawnerSpawnFlag) {
            this.spawnerSpawnFlag = spawnerSpawnFlag == null ? null : spawnerSpawnFlag.getName();
            return this;
        }

        public Builder naturalSpawnFlag(@Nullable IslandFlag naturalSpawnFlag) {
            this.naturalSpawnFlag = naturalSpawnFlag == null ? null : naturalSpawnFlag.getName();
            return this;
        }

        public EntityCategory build() {
            return EntityCategory.register(this.entityTypes,
                    getOrRegisterPrivilege(this.damagePrivilege), getOrRegisterPrivilege(this.spawnPrivilege),
                    getOrRegisterFlag(this.spawnerSpawnFlag), getOrRegisterFlag(this.naturalSpawnFlag));
        }

    }

}
