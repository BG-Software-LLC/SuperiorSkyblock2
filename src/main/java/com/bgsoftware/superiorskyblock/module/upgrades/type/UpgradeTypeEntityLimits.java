package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
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
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UpgradeTypeEntityLimits implements IUpgradeType {

    private final Map<UUID, SpawnOrigin> breedingOrigins = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, SpawnOrigin> vehiclesOwners = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<EntityType, SpawnOrigin> spawnEggPlayers = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

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
            EntityType type = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(type)) return;

            Island island = plugin.getGrid().getIslandAt(e.getLocation());
            if (island == null) return;

            SpawnOrigin origin = getSpawnOriginFromSpawnEvent(e);

            boolean limit = island.hasReachedEntityLimit(Keys.of(entity)).join();
            if (!limit) return;

            e.setCancelled(true);

            if (origin != null && origin.getPlayerId() != null) {
                Player p = plugin.getServer().getPlayer(origin.getPlayerId());
                if (p != null && p.isOnline()) {
                    Message.REACHED_ENTITY_LIMIT.send(p, Formatters.CAPITALIZED_FORMATTER.format(type.toString()));
                    try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                        Location loc = p.getLocation(w.getHandle());
                        PlayerInventory inv = p.getInventory();
                        for (ItemStack it : origin.getRefunds())
                            BukkitItems.addItem(it, inv, loc);
                    }
                }
            }
        }




        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onHangingPlace(HangingPlaceEvent e) {
            Entity entity = e.getEntity();
            EntityType entityType = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(entityType))
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()));
            }

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
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            ItemStack handItem = e.getItem();
            if (handItem == null) return;

            Material handType = handItem.getType();
            boolean isMinecart = Materials.isRail(e.getClickedBlock().getType()) && Materials.isMinecart(handType);
            boolean isBoat = Materials.isBoat(handType);
            if (!isMinecart && !isBoat) return;

            LocationKey futureEntitySpawnLocation;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location blockLocation = e.getClickedBlock().getLocation(wrapper.getHandle());
                Island island = plugin.getGrid().getIslandAt(blockLocation);

                if (island == null)
                    return;

                futureEntitySpawnLocation = isMinecart ? LocationKey.of(blockLocation, false) :
                        LocationKey.of(blockLocation.getWorld().getName(), blockLocation.getX(),
                                blockLocation.getY() + 1, blockLocation.getZ(), false);
            }

            vehiclesOwners.put(futureEntitySpawnLocation, new SpawnOrigin(e.getPlayer().getUniqueId()));
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onVehicleSpawn(VehicleCreateEvent e) {
            Entity entity = e.getVehicle();
            EntityType type = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(type)) return;

            Island island;
            SpawnOrigin origin;

            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                Location loc = entity.getLocation(w.getHandle());
                island = plugin.getGrid().getIslandAt(loc);
                if (island == null) return;

                LocationKey key = LocationKey.of(
                        loc.getWorld().getName(),
                        loc.getBlockX(),
                        loc.getBlockY(),
                        loc.getBlockZ()
                );

                origin = vehiclesOwners.remove(key);
            }

            boolean limit = island.hasReachedEntityLimit(Keys.of(entity)).join();
            if (!limit) return;

            entity.remove();

            if (origin != null && origin.getPlayerId() != null) {
                Player p = plugin.getServer().getPlayer(origin.getPlayerId());
                if (p != null && p.isOnline()) {
                    Message.REACHED_ENTITY_LIMIT.send(p, Formatters.CAPITALIZED_FORMATTER.format(type.toString()));
                }
            }
        }


        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnEggUse(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            ItemStack usedItem = e.getItem();
            if (usedItem == null || usedItem.getType() == Material.AIR)
                return;

            Player player = e.getPlayer();
            EntityType eggType;

            if (usedItem.getType() == Material.ARMOR_STAND) {
                eggType = EntityType.ARMOR_STAND;
            } else {
                eggType = BukkitItems.getEntityType(usedItem);
            }

            if (eggType == EntityType.UNKNOWN || !BukkitEntities.canHaveLimit(eggType))
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            spawnEggPlayers.put(eggType, new SpawnOrigin(player.getUniqueId()));
        }

        @Nullable
        private SpawnOrigin getSpawnOriginFromSpawnEvent(CreatureSpawnEvent event) {
            EntityType type = event.getEntityType();

            if (type == EntityType.ARMOR_STAND) return spawnEggPlayers.remove(type);

            switch (event.getSpawnReason()) {
                case SPAWNER_EGG:
                    return spawnEggPlayers.remove(type);
                case BREEDING:
                    return breedingOrigins.remove(event.getEntity().getUniqueId());
                default:
                    return null;
            }
        }


    }

    private class EntityLimitsBreedListener implements Listener {

        private final Int2ObjectMapView<ItemStack> trackedBreedItems = CollectionsFactory.createInt2ObjectArrayMap();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityBreed(EntityBreedEvent e) {
            Entity child = e.getEntity();
            EntityType type = child.getType();

            if (!BukkitEntities.canHaveLimit(type))
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(child.getLocation(w.getHandle()));
            }
            if (island == null)
                return;

            UUID breederId = null;
            if (e.getBreeder() instanceof Player)
                breederId = e.getBreeder().getUniqueId();

            ItemStack fatherItem = trackedBreedItems.remove(e.getFather().getEntityId());
            ItemStack motherItem = e.getFather().equals(e.getMother()) ? null :
                    trackedBreedItems.remove(e.getMother().getEntityId());

            List<ItemStack> refunds = new LinkedList<>();
            if (fatherItem != null && fatherItem.getType() != Material.AIR && fatherItem.getAmount() > 0)
                refunds.add(fatherItem);
            if (motherItem != null && motherItem.getType() != Material.AIR && motherItem.getAmount() > 0)
                refunds.add(motherItem);

            breedingOrigins.put(child.getUniqueId(), new SpawnOrigin(breederId, refunds));
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityFeed(PlayerInteractAtEntityEvent e) {
            if (!(e.getRightClicked() instanceof Animals))
                return;

            Player player = e.getPlayer();
            ItemStack usedItem = BukkitItems.getHandItem(player, BukkitItems.getHand(e));
            if (usedItem == null || usedItem.getType() == Material.AIR)
                return;

            if (!plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) e.getRightClicked()))
                return;

            // We want to calculate the amount of items consumed by breeding this animal.
            // We do that by checking the held item one tick later, and subtracting the
            // amount after 1 tick of the item from the original amount.
            int originalAmount = usedItem.getAmount();
            ItemStack breedItem = usedItem.clone();

            BukkitExecutor.sync(() -> {
                ItemStack currentItem = BukkitItems.getHandItem(player, BukkitItems.getHand(e));

                boolean isEmpty = currentItem == null || currentItem.getType() == Material.AIR;
                if (!isEmpty && !currentItem.isSimilar(usedItem))
                    return;

                int currentAmount = isEmpty ? 0 : currentItem.getAmount();
                int consumed = originalAmount - currentAmount;
                if (consumed <= 0)
                    return;

                breedItem.setAmount(consumed);
                trackedBreedItems.put(e.getRightClicked().getEntityId(), breedItem);
            }, 5L);
        }


    }

    private static final class SpawnOrigin {
        private final @Nullable UUID playerId;
        private final List<ItemStack> refunds;

        SpawnOrigin(@Nullable UUID playerId, @Nullable List<ItemStack> refunds) {
            this.playerId = playerId;
            if (refunds == null || refunds.isEmpty()) {
                this.refunds = Collections.emptyList();
            } else {
                this.refunds = Collections.unmodifiableList(new ArrayList<>(refunds));
            }
        }

        SpawnOrigin(@Nullable UUID playerId) {
            this.playerId = playerId;
            this.refunds = Collections.emptyList();
        }

        @Nullable
        UUID getPlayerId() {
            return playerId;
        }

        List<ItemStack> getRefunds() {
            return refunds;
        }
    }

}
