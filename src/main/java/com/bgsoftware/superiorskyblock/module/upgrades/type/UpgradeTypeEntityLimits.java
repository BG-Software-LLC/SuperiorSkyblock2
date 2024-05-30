package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class UpgradeTypeEntityLimits implements IUpgradeType {

    private final Map<EntityType, Player> entityBreederPlayers = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, Player> vehiclesOwners = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<EntityType, Player> spawnEggPlayers = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeEntityLimits(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Listener> getListeners() {
        List<Listener> listeners = new LinkedList<>();

        listeners.add(new EntityLimitsListener());

        checkEntityBreedListener().ifPresent(listeners::add);

        return listeners;
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Collections.emptyList();
    }

    private Optional<Listener> checkEntityBreedListener() {
        try {
            Class.forName("org.bukkit.event.entity.EntityBreedEvent");
            return Optional.of(new EntityLimitsBreedListener());
        } catch (ClassNotFoundException error) {
            return Optional.empty();
        }
    }

    private class EntityLimitsListener implements Listener {


        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onEntitySpawn(CreatureSpawnEvent e) {
            Entity entity = e.getEntity();
            EntityType entityType = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(entityType))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            Player spawningPlayer = getSpawningPlayerFromSpawnEvent(e);

            boolean hasReachedLimit = island.hasReachedEntityLimit(Keys.of(entity)).join();

            if (hasReachedLimit) {
                e.setCancelled(true);
                if (spawningPlayer != null && spawningPlayer.isOnline()) {
                    Message.REACHED_ENTITY_LIMIT.send(spawningPlayer, Formatters.CAPITALIZED_FORMATTER.format(entityType.toString()));
                }
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onHangingPlace(HangingPlaceEvent e) {
            Entity entity = e.getEntity();
            EntityType entityType = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(entityType))
                return;

            Island island = plugin.getGrid().getIslandAt(entity.getLocation());

            if (island == null)
                return;

            boolean hasReachedLimit = island.hasReachedEntityLimit(Keys.of(entity)).join();

            if (hasReachedLimit) {
                e.setCancelled(true);
                Message.REACHED_ENTITY_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(entityType.toString()));
            }
        }

        @EventHandler
        public void onVehicleSpawn(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            PlayerHand playerHand = BukkitItems.getHand(e);
            if (playerHand != PlayerHand.MAIN_HAND)
                return;

            ItemStack handItem = BukkitItems.getHandItem(e.getPlayer(), playerHand);
            if (handItem == null)
                return;

            Material handType = handItem.getType();

            // Check if minecart or boat
            boolean isMinecart = Materials.isRail(e.getClickedBlock().getType()) && Materials.isMinecart(handType);
            boolean isBoat = Materials.isBoat(handType);
            if (!isMinecart && !isBoat)
                return;

            Location blockLocation = e.getClickedBlock().getLocation();
            Island island = plugin.getGrid().getIslandAt(blockLocation);

            if (island == null)
                return;

            LocationKey futureEntitySpawnLocation = isMinecart ? new LocationKey(blockLocation) : new LocationKey(
                    blockLocation.getWorld().getName(),
                    blockLocation.getX(),
                    blockLocation.getY() + 1,
                    blockLocation.getZ()
            );

            vehiclesOwners.put(futureEntitySpawnLocation, e.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onVehicleSpawn(VehicleCreateEvent e) {
            Entity entity = e.getVehicle();
            EntityType entityType = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(entityType))
                return;

            Location entityLocation = entity.getLocation();

            Island island = plugin.getGrid().getIslandAt(entityLocation);

            if (island == null)
                return;

            LocationKey entityBlockLocation = new LocationKey(
                    entityLocation.getWorld().getName(),
                    entityLocation.getBlockX(),
                    entityLocation.getBlockY(),
                    entityLocation.getBlockZ()
            );

            Player vehicleOwner = vehiclesOwners.remove(entityBlockLocation);

            boolean hasReachedLimit = island.hasReachedEntityLimit(Keys.of(entity)).join();

            if (hasReachedLimit) {
                entity.remove();
                if (vehicleOwner != null && vehicleOwner.isOnline()) {
                    Message.REACHED_ENTITY_LIMIT.send(vehicleOwner, Formatters.CAPITALIZED_FORMATTER.format(entityType.toString()));
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnEggUse(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null)
                return;

            PlayerHand usedHand = BukkitItems.getHand(e);
            ItemStack usedItem = BukkitItems.getHandItem(e.getPlayer(), usedHand);
            EntityType spawnEggEntityType = usedItem == null ? EntityType.UNKNOWN :
                    usedItem.getType() == Material.ARMOR_STAND ? EntityType.ARMOR_STAND :
                            BukkitItems.getEntityType(usedItem);

            if (spawnEggEntityType == EntityType.UNKNOWN || !BukkitEntities.canHaveLimit(spawnEggEntityType))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

            if (island == null)
                return;

            spawnEggPlayers.put(spawnEggEntityType, e.getPlayer());
        }

        @Nullable
        private Player getSpawningPlayerFromSpawnEvent(CreatureSpawnEvent event) {
            EntityType entityType = event.getEntityType();

            if (entityType == EntityType.ARMOR_STAND) {
                return spawnEggPlayers.remove(entityType);
            }

            switch (event.getSpawnReason()) {
                case SPAWNER_EGG:
                    return spawnEggPlayers.remove(entityType);
                case BREEDING:
                    return entityBreederPlayers.remove(entityType);
            }

            return null;
        }

    }

    private class EntityLimitsBreedListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityBreed(EntityBreedEvent e) {
            Entity child = e.getEntity();
            EntityType childEntityType = child.getType();

            if (!(e.getBreeder() instanceof Player) || !BukkitEntities.canHaveLimit(childEntityType))
                return;

            Island island = plugin.getGrid().getIslandAt(child.getLocation());

            if (island == null)
                return;

            entityBreederPlayers.put(childEntityType, (Player) e.getBreeder());
        }

    }

}
