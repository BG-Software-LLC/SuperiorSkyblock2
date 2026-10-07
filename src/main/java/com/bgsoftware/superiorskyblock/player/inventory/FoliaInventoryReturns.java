package com.bgsoftware.superiorskyblock.player.inventory;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FoliaInventoryReturns {

    private static final String KEY_PREFIX = "superiorskyblock:inventory-return:";
    private static final Set<String> active = ConcurrentHashMap.newKeySet();

    private FoliaInventoryReturns() {
    }

    public static Reservation reserve(SuperiorPlayer player, ItemStack[] contents) {
        Reservation reservation = new Reservation(player, KEY_PREFIX + UUID.randomUUID(), copy(contents));
        active.add(reservation.key);
        try {
            reservation.save();
            return reservation;
        } catch (Throwable error) {
            active.remove(reservation.key);
            throw error;
        }
    }

    public static void restoreOnJoin(SuperiorPlayer player, Player onlinePlayer) {
        if (!BukkitExecutor.isFolia() || player.isPersistentDataContainerEmpty())
            return;
        player.getPersistentDataContainer().forEach((key, value) -> {
            if (!key.startsWith(KEY_PREFIX) || !(value instanceof String) || !active.add(key))
                return;
            try {
                ItemStack[] contents = Serializers.INVENTORY_SERIALIZER.deserialize(Base64.getDecoder().decode((String) value));
                new Reservation(player, key, contents).returnItems(onlinePlayer);
            } catch (Throwable error) {
                active.remove(key);
                Log.error(error, "Unable to restore reserved inventory for " + player.getUniqueId());
            }
        });
    }

    private static ItemStack[] copy(ItemStack[] contents) {
        ItemStack[] result = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++)
            result[slot] = contents[slot] == null ? null : contents[slot].clone();
        return result;
    }

    public static final class Reservation {

        private final SuperiorPlayer player;
        private final String key;
        private final ItemStack[] remaining;
        private final AtomicBoolean finished = new AtomicBoolean();

        private Reservation(SuperiorPlayer player, String key, ItemStack[] remaining) {
            this.player = player;
            this.key = key;
            this.remaining = remaining;
        }

        public synchronized void consume(int amount, boolean[] eligible) {
            int available = 0;
            for (int slot = 0; slot < remaining.length; slot++) {
                if (eligible[slot] && remaining[slot] != null)
                    available += remaining[slot].getAmount();
            }
            if (amount < 0 || amount > available)
                throw new IllegalArgumentException("Invalid reserved item amount: " + amount);
            for (int slot = 0; slot < remaining.length && amount > 0; slot++) {
                ItemStack item = remaining[slot];
                if (eligible[slot] && item != null) {
                    int removed = Math.min(amount, item.getAmount());
                    amount -= removed;
                    if (removed == item.getAmount())
                        remaining[slot] = null;
                    else
                        item.setAmount(item.getAmount() - removed);
                }
            }
            save();
        }

        public void finish() {
            if (!finished.compareAndSet(false, true))
                return;
            Player onlinePlayer = player.asPlayer();
            if (onlinePlayer == null) {
                active.remove(key);
                return;
            }
            try {
                SuperiorSkyblockPlugin.getPlugin().getTaskScheduler().entity(onlinePlayer,
                        () -> returnItems(onlinePlayer), () -> active.remove(key), 1L, 0L);
            } catch (Throwable error) {
                active.remove(key);
                Log.error(error, "Unable to schedule reserved inventory return for " + player.getUniqueId());
            }
        }

        private void returnItems(Player onlinePlayer) {
            try {
                if (!onlinePlayer.isOnline() || player.asPlayer() != onlinePlayer)
                    return;
                for (int slot = 0; slot < remaining.length; slot++) {
                    ItemStack item = remaining[slot];
                    if (item == null || item.getType() == Material.AIR)
                        continue;
                    Map<Integer, ItemStack> leftovers = onlinePlayer.getInventory().addItem(item.clone());
                    remaining[slot] = leftovers.isEmpty() ? null : leftovers.values().iterator().next().clone();
                    save();
                    if (remaining[slot] != null) {
                        onlinePlayer.getWorld().dropItemNaturally(onlinePlayer.getLocation(), remaining[slot].clone());
                        remaining[slot] = null;
                        save();
                    }
                }
                player.getPersistentDataContainer().remove(key);
                player.savePersistentDataContainer();
            } finally {
                active.remove(key);
            }
        }

        private void save() {
            byte[] serialized = Serializers.INVENTORY_SERIALIZER.serialize(remaining);
            if (remaining.length != 0 && serialized.length == 0)
                throw new IllegalStateException("Unable to serialize reserved inventory");
            PersistentDataContainer data = player.getPersistentDataContainer();
            data.put(key, PersistentDataType.STRING, Base64.getEncoder().encodeToString(serialized));
            player.savePersistentDataContainer();
        }
    }
}