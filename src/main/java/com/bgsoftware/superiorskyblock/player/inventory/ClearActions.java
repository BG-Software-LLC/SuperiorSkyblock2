package com.bgsoftware.superiorskyblock.player.inventory;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.player.inventory.ClearAction;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.nms.player.OfflinePlayerData;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class ClearActions {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static final ClearAction EFFECTS = register(new ClearAction("EFFECTS") {

        @Override
        public void doClear(Player player) {
            for (PotionEffect potionEffect : player.getActivePotionEffects())
                player.removePotionEffect(potionEffect.getType());
        }

    });

    public static final ClearAction ENDER_CHEST = register(new ClearAction("ENDER_CHEST") {

        @Override
        public void doClear(Player player) {
            player.getEnderChest().clear();
        }

    });

    public static final ClearAction EXPERIENCE = register(new ClearAction("EXPERIENCE") {

        @Override
        public void doClear(Player player) {
            player.setExp(0);
            player.setLevel(0);
            player.setTotalExperience(0);
        }

    });

    public static final ClearAction HEALTH = register(new ClearAction("HEALTH") {

        @Override
        public void doClear(Player player) {
            player.setHealth(player.getMaxHealth());
        }

    });

    public static final ClearAction HUNGER = register(new ClearAction("HUNGER") {

        @Override
        public void doClear(Player player) {
            player.setFoodLevel(20);
        }

    });

    public static final ClearAction INVENTORY = register(new ClearAction("INVENTORY") {

        @Override
        public void doClear(Player player) {
            player.getInventory().clear();
        }

    });

    private ClearActions() {

    }

    public static void registerActions() {
        // Do nothing, only trigger all the register calls
    }

    public static void runClearActions(OfflinePlayer offlinePlayer, boolean teleportToSpawn, Collection<ClearAction> clearActions) {
        OfflinePlayerData offlinePlayerData;
        Player onlinePlayer;

        if (offlinePlayer.isOnline()) {
            offlinePlayerData = null;
            onlinePlayer = offlinePlayer.getPlayer();
        } else {
            offlinePlayerData = plugin.getNMSPlayers().createOfflinePlayerData(offlinePlayer);
            onlinePlayer = offlinePlayerData.getFakeOnlinePlayer();
        }

        try {
            clearActions.forEach(clearAction -> clearAction.doClear(onlinePlayer));

            Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(Dimensions.NORMAL);
            if (offlinePlayerData != null) {
                if (teleportToSpawn)
                    offlinePlayerData.setLocation(spawnLocation);
                offlinePlayerData.applyChanges();
            } else if (teleportToSpawn) {
                EntityTeleports.teleport(onlinePlayer, spawnLocation);
            }
        } finally {
            if (offlinePlayerData != null)
                offlinePlayerData.release();
        }
    }

    private static ClearAction register(ClearAction clearAction) {
        ClearAction.register(clearAction);
        return clearAction;
    }

}
