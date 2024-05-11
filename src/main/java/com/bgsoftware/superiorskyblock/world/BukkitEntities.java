package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BukkitEntities {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, List<ItemStack>> entityContent = new HashMap<>();
    private static final Class<?> HOGLIN_CLASS = ((Supplier<Class<?>>) () -> {
        try {
            return Class.forName("org.bukkit.entity.Hoglin");
        } catch (ClassNotFoundException error) {
            return null;
        }
    }).get();

    private BukkitEntities() {

    }

    public static boolean isEquipment(LivingEntity livingEntity, ItemStack itemStack) {
        List<ItemStack> entityEquipment = entityContent.get(livingEntity.getUniqueId());

        if (entityEquipment == null) {
            cacheEntityEquipment(livingEntity);
            entityEquipment = entityContent.get(livingEntity.getUniqueId());
        }

        return entityEquipment.stream().anyMatch(equipmentItem -> equipmentItem != null &&
                equipmentItem.getType() == itemStack.getType());
    }

    public static void cacheEntityEquipment(LivingEntity livingEntity) {
        List<ItemStack> entityEquipment = new LinkedList<>(Arrays.asList(plugin.getNMSEntities().getEquipment(livingEntity.getEquipment())));

        if (livingEntity instanceof Pig) {
            if (((Pig) livingEntity).hasSaddle())
                entityEquipment.add(new ItemStack(Material.SADDLE));
        } else if (livingEntity instanceof Horse) {
            HorseInventory horseInventory = ((Horse) livingEntity).getInventory();
            horseInventory.forEach(itemStack -> {
                if (itemStack != null)
                    entityEquipment.add(itemStack);
            });

            entityEquipment.add(new ItemStack(Material.CHEST));

            if (horseInventory.getSaddle() != null)
                entityEquipment.add(horseInventory.getSaddle());
            if (horseInventory.getArmor() != null)
                entityEquipment.add(horseInventory.getArmor());
        }

        try {
            if (livingEntity instanceof AbstractHorse) {
                AbstractHorseInventory horseInventory = ((AbstractHorse) livingEntity).getInventory();
                horseInventory.forEach(itemStack -> {
                    if (itemStack != null)
                        entityEquipment.add(itemStack);
                });

                entityEquipment.add(new ItemStack(Material.CHEST));

                if (horseInventory.getSaddle() != null)
                    entityEquipment.add(horseInventory.getSaddle());
                if (horseInventory instanceof HorseInventory && ((HorseInventory) horseInventory).getArmor() != null)
                    entityEquipment.add(((HorseInventory) horseInventory).getArmor());
            }
        } catch (Throwable ignored) {
        }

        entityContent.put(livingEntity.getUniqueId(), entityEquipment);
    }

    public static void clearEntityEquipment(LivingEntity livingEntity) {
        entityContent.remove(livingEntity.getUniqueId());
    }

    public static Optional<Player> getPlayerSource(Entity damager) {
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player)
                return Optional.of((Player) shooter);
        } else if (damager instanceof Player) {
            return Optional.of((Player) damager);
        }

        return Optional.empty();
    }

    public static Key getLimitEntityType(Entity entity) {
        // TODO - Is this really necessary?
        return Keys.of(entity.getType());
    }

    public static boolean canHaveLimit(EntityType entityType) {
        Class<?> entityClass = entityType.getEntityClass();
        return (entityClass != null && (LivingEntity.class.isAssignableFrom(entityClass) ||
                Hanging.class.isAssignableFrom(entityClass) || Vehicle.class.isAssignableFrom(entityClass)));
    }

    public static boolean canBypassEntityLimit(Entity entity) {
        return canBypassEntityLimit(entity, true);
    }

    public static boolean canBypassEntityLimit(Entity entity, boolean checkProviders) {
        if (entity instanceof ArmorStand && !((ArmorStand) entity).isVisible())
            return true;

        if (checkProviders) {
            for (EntitiesProvider entitiesProvider : plugin.getProviders().getEntitiesProviders()) {
                if (!entitiesProvider.shouldTrackEntity(entity))
                    return true;
            }
        }

        return false;
    }

    public static EntityCategory getCategory(EntityType entityType) {
        for (EntityCategory entityCategory : EntityCategory.values()) {
            if (entityCategory.isFromCategory(entityType))
                return entityCategory;
        }

        return EntityCategory.UNKNOWN;
    }

    private static EnumSet<EntityType> createEntityTypesSet(Predicate<EntityType> predicate) {
        EnumSet<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);
        for (EntityType entityType : EntityType.values()) {
            if (predicate.test(entityType))
                entityTypes.add(entityType);
        }
        return entityTypes;
    }

    public enum EntityCategory {

        MONSTER(IslandPrivileges.MONSTER_DAMAGE, IslandPrivileges.MONSTER_SPAWN) {

            private final EnumSet<EntityType> MONSTER_TYPES = createEntityTypesSet(entityType -> {
                Class<? extends Entity> entityClass = entityType.getEntityClass();
                return entityClass != null && (Monster.class.isAssignableFrom(entityClass) ||
                        // Checks for slimes & magma cubes
                        Slime.class.isAssignableFrom(entityClass) ||
                        // Checks for ghasts and phantoms
                        Flying.class.isAssignableFrom(entityClass) ||
                        // In Bukkit, Hoglins are considered as Animals for no reason.
                        // We want to explicitly tell the plugin its monster type.
                        entityClass == HOGLIN_CLASS
                );
            });

            @Override
            boolean isFromCategory(EntityType entityType) {
                return MONSTER_TYPES.contains(entityType);
            }
        },
        // !!!Never put this above Monster category!!!
        ANIMAL(IslandPrivileges.ANIMAL_DAMAGE, IslandPrivileges.ANIMAL_SPAWN) {

            private final EnumSet<EntityType> ANIMAL_TYPES = createEntityTypesSet(entityType -> {
                Class<? extends Entity> entityClass = entityType.getEntityClass();
                // This call is done after Monster category is checked.
                // Therefore, the checks below are enough.
                return entityClass != null &&
                        // We check that the entity type is whether a Creature or an Ambient
                        (Creature.class.isAssignableFrom(entityClass) || Ambient.class.isAssignableFrom(entityClass)) &&
                        // We make sure this is not Hoglin; In Bukkit, they are considered Animals.
                        entityClass != HOGLIN_CLASS;
            });

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

    }

}
