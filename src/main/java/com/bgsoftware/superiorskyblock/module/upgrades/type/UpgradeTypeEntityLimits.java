package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Minecart;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UpgradeTypeEntityLimits implements IUpgradeType {

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

    private final class EntityLimitsListener implements Listener {

        private final Map<Location, UUID> vehiclesOwners = new HashMap<>();

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onEntitySpawn(CreatureSpawnEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getLocation());

            if (island == null)
                return;

            if (!EntityUtils.canHaveLimit(e.getEntityType()))
                return;

            island.hasReachedEntityLimit(Key.of(e.getEntity())).whenComplete((result, ex) -> {
                if (result) {
                    e.setCancelled(true);
                }
            });
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onHangingPlace(HangingPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

            if (island == null)
                return;

            if (!EntityUtils.canHaveLimit(e.getEntity().getType()))
                return;

            island.hasReachedEntityLimit(Key.of(e.getEntity())).whenComplete((result, ex) -> {
                if (result) {
                    e.setCancelled(true);
                    if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
                        e.getPlayer().getInventory().addItem(asItemStack(e.getEntity()));
                }
            });
        }

        @EventHandler
        public void onVehicleSpawn(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null ||
                    e.getPlayer().getGameMode() == GameMode.CREATIVE ||
                    !e.getClickedBlock().getType().name().contains("RAIL") ||
                    !e.getItem().getType().name().contains("MINECART"))
                return;

            if (INTERACT_GET_HAND.isValid() && INTERACT_GET_HAND.invoke(e) != EquipmentSlot.HAND)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

            if (island == null)
                return;

            Location blockLocation = e.getClickedBlock().getLocation();

            vehiclesOwners.put(blockLocation, e.getPlayer().getUniqueId());
            Executor.sync(() -> vehiclesOwners.remove(blockLocation), 40L);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onVehicleSpawn(VehicleCreateEvent e) {
            if (!(e.getVehicle() instanceof Minecart))
                return;

            Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

            if (island == null)
                return;

            UUID placedVehicle = vehiclesOwners.remove(LocationUtils.getBlockLocation(e.getVehicle().getLocation()));

            if (!EntityUtils.canHaveLimit(e.getVehicle().getType()))
                return;

            island.hasReachedEntityLimit(Key.of(e.getVehicle())).whenComplete((result, ex) -> {
                if (result) {
                    Executor.sync(() -> {
                        e.getVehicle().remove();
                        if (placedVehicle != null)
                            Bukkit.getPlayer(placedVehicle).getInventory().addItem(asItemStack(e.getVehicle()));
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

    }

}
