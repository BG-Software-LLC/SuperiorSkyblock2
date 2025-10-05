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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Dispenser;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UpgradeTypeEntityLimits implements IUpgradeType {

    private final Map<UUID, SpawnOrigin> breedingOrigins = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, SpawnOrigin> vehiclesOwners = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<EntityType, SpawnOrigin> spawnEggPlayers = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, SpawnOrigin> dispenserOrigins = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, SpawnOrigin> bucketOrigins    = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);
    private final Map<LocationKey, SpawnOrigin> structureOrigins = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

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

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onDispenseEgg(BlockDispenseEvent e) {
            ItemStack it = e.getItem();
            if (it == null) return;

            EntityType eggType = BukkitItems.getEntityType(it);
            if (eggType == null || eggType == EntityType.UNKNOWN || !BukkitEntities.canHaveLimit(eggType)) return;

            Block b = e.getBlock();
            MaterialData data = b.getState().getData();
            BlockFace face = BlockFace.SELF;
            if (data instanceof Dispenser) face = ((Dispenser) data).getFacing();

            Block target = b.getRelative(face);
            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                Location tl = target.getLocation(w.getHandle());
                dispenserOrigins.put(LocationKey.of(tl.getWorld().getName(), tl.getBlockX(), tl.getBlockY(), tl.getBlockZ(), false),
                        new SpawnOrigin(null));
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFishBucket(PlayerBucketEmptyEvent e) {
            Material bucket = e.getBucket();
            if (bucket == null || !isFishBucket(bucket)) return;

            Player p = e.getPlayer();
            Block clicked = e.getBlockClicked();
            if (clicked == null) return;
            Block target = clicked.getRelative(e.getBlockFace());
            ItemStack refund = new ItemStack(bucket, 1);

            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                Location tl = target.getLocation(w.getHandle());
                bucketOrigins.put(LocationKey.of(tl.getWorld().getName(), tl.getBlockX(), tl.getBlockY(), tl.getBlockZ(), false),
                        new SpawnOrigin(p.getUniqueId(), Collections.singletonList(refund)));
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onPumpkinPlace(BlockPlaceEvent e) {
            Material t = e.getBlockPlaced().getType();
            String n = t.name();
            if (!(n.contains("PUMPKIN"))) return;

            Player p = e.getPlayer();
            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                Location base = e.getBlockPlaced().getLocation(w.getHandle());
                String world = base.getWorld().getName();
                for (int dx=-1; dx<=1; dx++) for (int dy=-2; dy<=1; dy++) for (int dz=-1; dz<=1; dz++) {
                    structureOrigins.put(LocationKey.of(world, base.getBlockX()+dx, base.getBlockY()+dy, base.getBlockZ()+dz, false),
                            new SpawnOrigin(p.getUniqueId(), Collections.singletonList(new ItemStack(t, 1))));
                }
            }
        }



        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onEntitySpawn(CreatureSpawnEvent e) {
            Entity entity = e.getEntity();
            EntityType type = entity.getType();

            if (BukkitEntities.canBypassEntityLimit(entity) || !BukkitEntities.canHaveLimit(type)) return;

            Island island;
            SpawnOrigin origin;

            try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                Location loc = entity.getLocation(w.getHandle());
                island = plugin.getGrid().getIslandAt(loc);
                if (island == null) return;

                CreatureSpawnEvent.SpawnReason reason = e.getSpawnReason();
                String rn = reason.name();

                if (hasSpawnReason("DISPENSE_EGG") && "DISPENSE_EGG".equals(rn)) {
                    origin = popNearby(dispenserOrigins, loc, 1);
                } else if (hasSpawnReason("BUCKET") && "BUCKET".equals(rn)) {
                    origin = popNearby(bucketOrigins, loc, 1);
                } else if ("BUILD_IRONGOLEM".equals(rn) || "BUILD_SNOWMAN".equals(rn) || "BUILD_SNOW_GOLEM".equals(rn)) {
                    origin = popNearby(structureOrigins, loc, 2);
                } else {
                    origin = getSpawnOriginFromSpawnEvent(e);
                }
            }

            boolean limit = island.hasReachedEntityLimit(Keys.of(entity)).join();
            if (!limit) return;

            e.setCancelled(true);

            if (origin != null && origin.getPlayerId() != null) {
                Player p = plugin.getServer().getPlayer(origin.getPlayerId());
                if (p != null && p.isOnline()) {
                    Message.REACHED_ENTITY_LIMIT.send(p, Formatters.CAPITALIZED_FORMATTER.format(type.toString()));
                    try (ObjectsPools.Wrapper<Location> w = ObjectsPools.LOCATION.obtain()) {
                        Location ploc = p.getLocation(w.getHandle());
                        PlayerInventory inv = p.getInventory();
                        for (ItemStack it : origin.getRefunds()) {
                            BukkitItems.addItem(it, inv, ploc);
                        }
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

    private static boolean hasSpawnReason(String name) {
        try { Enum.valueOf(CreatureSpawnEvent.SpawnReason.class, name); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }

    private static boolean isFishBucket(Material m) {
        String n = m.name();
        return n.endsWith("_BUCKET") && (
                n.contains("COD") || n.contains("SALMON") || n.contains("PUFFERFISH")
                        || n.contains("TROPICAL_FISH") || n.contains("AXOLOTL")
        );
    }

    @Nullable
    private static SpawnOrigin popNearby(Map<LocationKey, SpawnOrigin> map, Location loc, int r) {
        String w = loc.getWorld().getName();
        for (int dx=-r; dx<=r; dx++) for (int dy=-r; dy<=r; dy++) for (int dz=-r; dz<=r; dz++) {
            LocationKey k = LocationKey.of(w, loc.getBlockX()+dx, loc.getBlockY()+dy, loc.getBlockZ()+dz, false);
            SpawnOrigin so = map.remove(k);
            if (so != null) return so;
        }
        return null;
    }

}
