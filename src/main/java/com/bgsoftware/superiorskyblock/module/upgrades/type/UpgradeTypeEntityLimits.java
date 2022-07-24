package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UpgradeTypeEntityLimits implements IUpgradeType {

    private static final ReflectMethod<EquipmentSlot> INTERACT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEvent.class, "getHand");

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeEntityLimits(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Listener getListener() {
        return new EntityLimitsListener();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Collections.emptyList();
    }

    private class EntityLimitsListener implements Listener {

        private final Map<LocationKey, UUID> vehiclesOwners = AutoRemovalMap.newHashMap(2, TimeUnit.SECONDS);

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onEntitySpawn(CreatureSpawnEvent e) {
            if (BukkitEntities.canBypassEntityLimit(e.getEntity()))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            if (!BukkitEntities.canHaveLimit(e.getEntityType()))
                return;

            island.hasReachedEntityLimit(KeyImpl.of(e.getEntity())).whenComplete((result, ex) -> {
                if (result) {
                    e.setCancelled(true);
                }
            });
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onHangingPlace(HangingPlaceEvent e) {
            if (BukkitEntities.canBypassEntityLimit(e.getEntity()))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

            if (island == null)
                return;

            if (!BukkitEntities.canHaveLimit(e.getEntity().getType()))
                return;

            island.hasReachedEntityLimit(KeyImpl.of(e.getEntity())).whenComplete((result, ex) -> {
                if (result) {
                    e.setCancelled(true);
                }
            });
        }

        @EventHandler
        public void onVehicleSpawn(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null ||
                    e.getPlayer().getGameMode() == GameMode.CREATIVE ||
                    !Materials.isRail(e.getClickedBlock().getType()) ||
                    !Materials.isMinecart(e.getItem().getType()))
                return;

            if (INTERACT_GET_HAND.isValid() && INTERACT_GET_HAND.invoke(e) != EquipmentSlot.HAND)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

            if (island == null)
                return;

            Location blockLocation = e.getClickedBlock().getLocation();

            vehiclesOwners.put(new LocationKey(blockLocation), e.getPlayer().getUniqueId());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onVehicleSpawn(VehicleCreateEvent e) {
            if (!(e.getVehicle() instanceof Minecart) || BukkitEntities.canBypassEntityLimit(e.getVehicle()))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

            if (island == null)
                return;

            UUID placedVehicle = vehiclesOwners.remove(new LocationKey(e.getVehicle().getLocation()));

            if (!BukkitEntities.canHaveLimit(e.getVehicle().getType()))
                return;

            island.hasReachedEntityLimit(KeyImpl.of(e.getVehicle())).whenComplete((result, ex) -> {
                if (result) {
                    BukkitExecutor.sync(() -> {
                        removeEntity(e.getVehicle());
                        if (placedVehicle != null) {
                            Player player = Bukkit.getPlayer(placedVehicle);
                            if (player != null)
                                BukkitItems.addItem(asItemStack(e.getVehicle()), player.getInventory(), player.getLocation());
                        }
                    });
                }
            });
        }

        private ItemStack asItemStack(Entity entity) {
            if (entity instanceof Hanging) {
                switch (entity.getType()) {
                    case ITEM_FRAME:
                        return new ItemStack(Material.ITEM_FRAME);
                    case PAINTING:
                        return new ItemStack(Material.PAINTING);
                }
            } else if (entity instanceof Minecart) {
                Material material = Material.valueOf(plugin.getNMSAlgorithms().getMinecartBlock((Minecart) entity).getGlobalKey());
                switch (material.name()) {
                    case "HOPPER":
                        return new ItemStack(Material.HOPPER_MINECART);
                    case "COMMAND_BLOCK":
                        return new ItemStack(Material.valueOf("COMMAND_BLOCK_MINECART"));
                    case "COMMAND":
                        return new ItemStack(Material.COMMAND_MINECART);
                    case "TNT":
                        return new ItemStack(ServerVersion.isLegacy() ? Material.EXPLOSIVE_MINECART : Material.valueOf("TNT_MINECART"));
                    case "FURNACE":
                        return new ItemStack(ServerVersion.isLegacy() ? Material.POWERED_MINECART : Material.valueOf("FURNACE_MINECART"));
                    case "CHEST":
                        return new ItemStack(ServerVersion.isLegacy() ? Material.STORAGE_MINECART : Material.valueOf("CHEST_MINECART"));
                    default:
                        return new ItemStack(Material.MINECART);
                }
            }

            throw new IllegalArgumentException("Cannot find an item for " + entity.getType());
        }

        private void removeEntity(Entity entity) {
            if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                BukkitExecutor.ensureMain(entity::remove);
            } else {
                entity.remove();
            }
        }

    }

}
