package com.bgsoftware.superiorskyblock.nms.v26_2_folia;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon.IslandDragonFight;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon.DragonNavigation;
import com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon.WorldDragonFight;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public final class NMSDragonFightImpl implements NMSDragonFight, Listener {
    private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Map<BattleKey, IslandDragonFight> fights = new ConcurrentHashMap<>();
    private final Map<UUID, BattleKey> playerIslands = new ConcurrentHashMap<>();
    private final Map<BattleKey, Object> requests = new ConcurrentHashMap<>();
    private final AtomicBoolean registered = new AtomicBoolean();

    @Override
    public void prepareEndWorld(World world) {
        if (registered.compareAndSet(false, true))
            Bukkit.getPluginManager().registerEvents(this, plugin);
        ServerLevel level = ((CraftWorld) world).getHandle();
        if (!(level.getDragonFight() instanceof WorldDragonFight))
            level.setDragonFight(new WorldDragonFight(level, this));
    }

    @Override
    public EnderDragon getEnderDragon(Island island, Dimension dimension) {
        World world = island.getCenter(dimension).getWorld();
        IslandDragonFight fight = world == null ? null : fights.get(new BattleKey(world.getUID(), island.getUniqueId()));
        return fight == null ? null : fight.getDragon();
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        World world = location.getWorld();
        if (world == null)
            return;
        BattleKey key = new BattleKey(world.getUID(), island.getUniqueId());
        IslandDragonFight previous = fights.remove(key);
        if (previous != null)
            previous.close(true);
        request(island, location.clone(), key, true, null, false);
    }

    @Override
    public void removeDragonBattle(Island island, Dimension dimension) {
        World world = island.getCenter(dimension).getWorld();
        if (world == null)
            return;
        BattleKey key = new BattleKey(world.getUID(), island.getUniqueId());
        requests.remove(key);
        IslandDragonFight fight = fights.remove(key);
        if (fight != null)
            fight.close(true);
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        BukkitExecutor.ensureMain(player, () -> {
            var advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
            if (advancement != null) {
                var progress = player.getAdvancementProgress(advancement);
                for (String criterion : progress.getRemainingCriteria())
                    progress.awardCriteria(criterion);
            }
        });
    }

    public IslandDragonFight find(World world, BlockPos position) {
        Island island = plugin.getGrid().getIslandAt(new Location(world, position.getX(), position.getY(), position.getZ()));
        return island == null ? null : fights.get(new BattleKey(world.getUID(), island.getUniqueId()));
    }

    public void restoreForCrystal(World world, BlockPos position) {
        Island island = plugin.getGrid().getIslandAt(new Location(world, position.getX(), position.getY(), position.getZ()));
        if (island == null)
            return;
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
        Location portal = portal(island, dimension);
        if (portal == null)
            return;
        BattleKey key = new BattleKey(world.getUID(), island.getUniqueId());
        if (fights.containsKey(key) || requests.containsKey(key))
            return;
        request(island, portal, key, false, null, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to != null && (event.getFrom().getBlockX() >> 4 != to.getBlockX() >> 4
                || event.getFrom().getBlockZ() >> 4 != to.getBlockZ() >> 4))
            playerEntered(event.getPlayer(), to);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        BukkitExecutor.sync(event.getPlayer(), () -> playerEntered(event.getPlayer(), event.getPlayer().getLocation()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        BukkitExecutor.sync(event.getPlayer(), () -> playerEntered(event.getPlayer(), event.getPlayer().getLocation()), 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerIslands.remove(event.getPlayer().getUniqueId());
    }

    private void playerEntered(Player player, Location location) {
        if (location.getWorld().getEnvironment() != World.Environment.THE_END) {
            playerIslands.remove(player.getUniqueId());
            return;
        }
        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null) {
            playerIslands.remove(player.getUniqueId());
            return;
        }
        BattleKey key = new BattleKey(location.getWorld().getUID(), island.getUniqueId());
        if (key.equals(playerIslands.put(player.getUniqueId(), key)))
            return;
        IslandDragonFight active = fights.get(key);
        if (active != null) {
            active.wake();
            return;
        }
        if (requests.containsKey(key))
            return;
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(location.getWorld());
        Location portal = portal(island, dimension);
        if (portal != null && island.getPersistentDataContainer().get("folia-dragon:" + dimension.getName(),
                com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType.STRING) != null)
            request(island, portal, key, false, null, false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAdded(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof EnderDragon dragon) {
            if (((CraftWorld) dragon.getWorld()).getHandle().getDragonFight() instanceof WorldDragonFight)
                DragonNavigation.freeze(dragon, plugin);
            BukkitExecutor.sync(dragon, () -> restoreDragon(dragon), 1L);
        } else if (event.getEntity() instanceof EnderCrystal crystal && crystal.getPersistentDataContainer()
                .has(IslandDragonFight.crystalKey(plugin), PersistentDataType.STRING)) {
            BukkitExecutor.sync(crystal, () -> restoreCrystal(crystal), 1L);
        }
    }

    private void restoreCrystal(EnderCrystal crystal) {
        if (!crystal.isValid())
            return;
        Location location = crystal.getLocation();
        Island island = plugin.getGrid().getIslandAt(location);
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(location.getWorld());
        if (island == null || portal(island, dimension) == null) {
            crystal.setInvulnerable(false);
            crystal.setBeamTarget(null);
            crystal.getPersistentDataContainer().remove(IslandDragonFight.crystalKey(plugin));
            return;
        }
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IslandDragonFight fight = find(location.getWorld(), position);
        if (fight != null) {
            if (!fight.isRespawning()) {
                crystal.setInvulnerable(false);
                crystal.setBeamTarget(null);
                crystal.getPersistentDataContainer().remove(IslandDragonFight.crystalKey(plugin));
            }
        } else {
            restoreForCrystal(location.getWorld(), position);
            BukkitExecutor.sync(crystal, () -> restoreCrystal(crystal), 20L);
        }
    }

    private void restoreDragon(EnderDragon dragon) {
        World world = dragon.getWorld();
        if (!(((CraftWorld) world).getHandle().getDragonFight() instanceof WorldDragonFight))
            return;
        Island island = plugin.getGrid().getIslandAt(dragon.getLocation());
        if (island == null) {
            DragonNavigation.restore(dragon, plugin);
            return;
        }
        DragonNavigation.freeze(dragon, plugin);
        BattleKey key = new BattleKey(world.getUID(), island.getUniqueId());
        IslandDragonFight existing = fights.get(key);
        if (existing != null) {
            existing.attach(((CraftEnderDragon) dragon).getHandle());
            return;
        }
        if (requests.containsKey(key))
            return;
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
        Location portal = portal(island, dimension);
        if (portal != null)
            request(island, portal, key, false, dragon, false);
        else
            DragonNavigation.restore(dragon, plugin);
    }

    private Location portal(Island island, Dimension dimension) {
        if (dimension == null || dimension.getEnvironment() != World.Environment.THE_END)
            return null;
        SettingsManager.Worlds.DimensionConfig config = plugin.getSettings().getWorlds().getDimensionConfig(dimension);
        if (!(config instanceof SettingsManager.Worlds.End end) || !end.isEnabled() || !end.isDragonFight())
            return null;
        return end.getPortalOffset().applyToLocation(island.getCenter(dimension));
    }

    private void request(Island island, Location portal, BattleKey key, boolean fresh, EnderDragon existing, boolean respawn) {
        Object request = new Object();
        requests.put(key, request);
        Dimension dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(portal.getWorld());
        BukkitExecutor.sync(portal, () -> prepareArena(island, portal, key, fresh, existing, respawn, request, dimension), 1L);
    }

    private void prepareArena(Island island, Location portal, BattleKey key, boolean fresh, EnderDragon existing,
                              boolean respawn, Object request, Dimension dimension) {
        if (requests.get(key) != request)
            return;
        IslandDragonFight.loadArena(plugin, portal, () -> requests.get(key) == request).whenComplete((arena, error) -> {
            if (error != null) {
                if (requests.remove(key, request))
                    plugin.getLogger().log(Level.SEVERE, "Failed to prepare an island dragon arena", error);
                return;
            }
            BukkitExecutor.sync(portal, () -> {
                if (!requests.remove(key, request)) {
                    arena.close();
                    return;
                }
                try {
                    IslandDragonFight fight = new IslandDragonFight(plugin, island, dimension, portal, arena, fresh,
                            existing == null ? null : existing.getUniqueId(), idle -> {
                        if (fights.remove(key, idle))
                            idle.close(false);
                    });
                    IslandDragonFight previous = fights.put(key, fight);
                    if (previous != null)
                        previous.close(false);
                    fight.start(fresh, existing, respawn);
                } catch (RuntimeException | Error failure) {
                    arena.close();
                    plugin.getLogger().log(Level.SEVERE, "Failed to start an island dragon battle", failure);
                }
            });
        });
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin() != plugin)
            return;
        requests.clear();
        playerIslands.clear();
        fights.values().forEach(fight -> fight.close(false));
        fights.clear();
    }

    private record BattleKey(UUID world, UUID island) {}
}
