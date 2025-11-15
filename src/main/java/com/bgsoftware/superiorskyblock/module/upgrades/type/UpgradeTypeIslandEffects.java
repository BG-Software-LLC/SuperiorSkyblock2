package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddEffect;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpgradeTypeIslandEffects implements IUpgradeType {

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeIslandEffects(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<Listener> getListeners() {
        return ServerVersion.isAtLeast(ServerVersion.v1_15) ?
                Collections.singletonList(new IslandEffectsListener()) :
                Collections.emptyList();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Arrays.asList(new CmdAdminAddEffect(), new CmdAdminSetEffect());
    }

    private class IslandEffectsListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onPlayerEffect(EntityPotionEffectEvent e) {
            if (e.getAction() == EntityPotionEffectEvent.Action.ADDED || !(e.getEntity() instanceof Player) ||
                    e.getCause() == EntityPotionEffectEvent.Cause.PLUGIN || e.getCause() == EntityPotionEffectEvent.Cause.BEACON)
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getEntity().getLocation(wrapper.getHandle()));
            }

            if (island == null)
                return;

            int islandEffectLevel = island.getPotionEffectLevel(e.getModifiedType());

            if (islandEffectLevel > 0 && (e.getOldEffect() == null || e.getOldEffect().getAmplifier() == islandEffectLevel)) {
                e.setCancelled(true);
            }
        }

    }

}
