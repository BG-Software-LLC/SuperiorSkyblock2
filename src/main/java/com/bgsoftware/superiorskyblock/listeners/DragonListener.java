package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class DragonListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public DragonListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderDragonChangePhase(EnderDragonChangePhaseEvent e) {
        Executor.sync(() -> plugin.getNMSDragonFight().setDragonPhase(e.getEntity(), e.getNewPhase()), 1L);
    }

    private static final ReflectField<Object> A = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.entity.CraftEntity", Object.class, "entity");

    @EventHandler
    public void g(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType().name().equals("GUNPOWDER")) {
            for (Entity entity : event.getPlayer().getNearbyEntities(20, 20, 20)) {
                if (entity instanceof EnderDragon) {
                    Bukkit.broadcastMessage(A.get(entity) + "");
                }
            }
        }
    }

}
