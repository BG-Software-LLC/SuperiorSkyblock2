package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.CachedResult;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BukkitEntities {

    public static final CachedResult<SuperiorPlayer, Entity> CACHED_SUPERIOR_PLAYER_SOURCE = CachedResult.create();

    @Nullable
    private static final EntityType CUSHION_TYPE = EnumHelper.getEnum(EntityType.class, "CUSHION");
    @Nullable
    private static final EntityType NAUTILUS_TYPE = EnumHelper.getEnum(EntityType.class, "NAUTILUS");
    @Nullable
    private static final EntityType ZOMBIE_NAUTILUS_TYPE = EnumHelper.getEnum(EntityType.class, "ZOMBIE_NAUTILUS");

    private static final boolean ABSTRACT_HORSE_SUPPORT = isAbstractHorseSupported();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Synchronized<Int2ObjectMapView<List<ItemStack>>> entityContent =
            Synchronized.of(CollectionsFactory.createInt2ObjectArrayMap());

    private BukkitEntities() {

    }

    public static List<ItemStack> getEntityEquipment(LivingEntity livingEntity) {
        return entityContent.writeAndGet(entityContent ->
                entityContent.computeIfAbsent(livingEntity.getEntityId(), u -> cacheEntityEquipmentInternal(livingEntity)));
    }

    public static boolean isEquipment(List<ItemStack> entityEquipment, ItemStack itemStack) {
        Material itemType = itemStack.getType();
        for (ItemStack equipmentItem : entityEquipment) {
            if (equipmentItem != null && equipmentItem.getType() == itemType)
                return true;
        }

        return false;
    }

    public static void cacheEntityEquipment(LivingEntity livingEntity) {
        List<ItemStack> entityEquipment = cacheEntityEquipmentInternal(livingEntity);
        entityContent.write(entityContent ->
                entityContent.put(livingEntity.getEntityId(), entityEquipment));
    }

    private static List<ItemStack> cacheEntityEquipmentInternal(LivingEntity livingEntity) {
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

        if (ABSTRACT_HORSE_SUPPORT && livingEntity instanceof AbstractHorse) {
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

        return entityEquipment.isEmpty() ? Collections.emptyList() : entityEquipment;
    }

    public static void clearEntityEquipment(LivingEntity livingEntity) {
        entityContent.write(entityContent -> entityContent.remove(livingEntity.getEntityId()));
    }

    @Nullable
    public static Player getPlayerSource(@Nullable Entity damager) {
        if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            if (shooter instanceof Player)
                return (Player) shooter;
        } else if (damager instanceof Player) {
            return (Player) damager;
        }

        return null;
    }

    @Nullable
    public static SuperiorPlayer getSuperiorPlayerSource(@Nullable Entity entity) {
        return CACHED_SUPERIOR_PLAYER_SOURCE.getOrCompute(entity, BukkitEntities::lookupSuperiorPlayerSourceInternal);
    }

    private static SuperiorPlayer lookupSuperiorPlayerSourceInternal(@Nullable Entity entity) {
        Player player = getPlayerSource(entity);
        return player == null ? null : plugin.getPlayers().getSuperiorPlayer(player);
    }

    public static boolean canHaveLimit(EntityType entityType) {
        Class<?> entityClass = entityType.getEntityClass();
        return (entityClass != null && (LivingEntity.class.isAssignableFrom(entityClass) || entityType == CUSHION_TYPE ||
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

    public static boolean isTamed(Entity entity) {
        return entity instanceof Tameable && ((Tameable) entity).isTamed();
    }

    public static boolean isHorse(Entity entity) {
        return entity instanceof Horse || (ServerVersion.isAtLeast(ServerVersion.v1_11) && entity instanceof org.bukkit.entity.AbstractHorse);
    }

    public static boolean isNautilus(EntityType entityType) {
        return entityType == NAUTILUS_TYPE || entityType == ZOMBIE_NAUTILUS_TYPE;
    }

    private static boolean isAbstractHorseSupported() {
        try {
            Class.forName("org.bukkit.entity.AbstractHorse");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }

}
