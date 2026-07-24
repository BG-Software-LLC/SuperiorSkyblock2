package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.bgsoftware.superiorskyblock.core.ObjectsPools.Wrapper;

@SuppressWarnings("unused")
public final class ItemsMissions extends BuiltinMission<ItemsMissions.ItemsTracker> implements Listener {

    private static final long COUNT_INVENTORY_THRESHOLD = TimeUnit.SECONDS.toMillis(1);
    private final ObjectsPool<Wrapper<ItemProgress[]>> ITEM_PROGRESS_POOL = ObjectsPools.createNewPool(this::createProgressLookup);

    private final Map<KeyRequirements, Integer> requiredItems = new LinkedHashMap<>();
    private int totalRequiredAmount;

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection requiredItemsSection = section.getConfigurationSection("required-items");

        if (requiredItemsSection == null)
            throw new MissionLoadException("You must have the \"required-items\" section in the config.");

        for (String key : requiredItemsSection.getKeys(false)) {
            KeySet itemStacks = KeySet.createKeySet();
            section.getStringList("required-items." + key + ".types").forEach(itemStackName ->
                    itemStacks.add(Key.ofMaterialAndData(itemStackName.toUpperCase(Locale.ENGLISH))));
            int requiredAmount = section.getInt("required-items." + key + ".amount");
            requiredItems.put(new KeyRequirements(itemStacks), requiredAmount);
            this.totalRequiredAmount += requiredAmount;
        }
    }

    @Override
    protected void registerListeners() {
        registerListener(this);
    }

    @Override
    protected void clearModuleData(ItemsTracker data) {
        data.itemsTracker.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        int totalItemAmount = getProgressValue(superiorPlayer);
        if (totalItemAmount <= 0)
            return 0D;

        double progress = (double) totalItemAmount / this.totalRequiredAmount;

        return Math.max(0, progress);
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();

        if (player == null)
            return 0;

        ItemsTracker itemsTracker = getOrCreate(superiorPlayer, s -> new ItemsTracker());

        if (itemsTracker == null)
            return 0;

        return countItemsForPlayerInternal(player, itemsTracker);
    }

    private int countItemsForPlayerInternal(Player player, ItemsTracker itemsTracker) {
        long currTime = System.currentTimeMillis();

        if (itemsTracker.lastCountTime > 0 && currTime < itemsTracker.lastCountTime)
            return itemsTracker.totalItemAmount;

        try (Wrapper<ItemProgress[]> progressLookupWrapper = ITEM_PROGRESS_POOL.obtain()) {
            ItemProgress[] progressLookup = progressLookupWrapper.getHandle();
            for (ItemProgress itemProgress : progressLookup) {
                itemProgress.count = 0;
            }

            itemsTracker.clear(currTime);

            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    Pair<Key, Integer> missionItemData = getMissionItemData(itemStack);
                    if (missionItemData != null) {
                        ItemProgress itemProgress = progressLookup[missionItemData.getValue()];
                        int counted = itemProgress.track(itemStack.getAmount());
                        itemsTracker.track(itemStack, missionItemData.getKey(), counted);
                    }
                }
            }
        }

        return itemsTracker.totalItemAmount;
    }

    @Override
    public void onComplete(SuperiorPlayer superiorPlayer) {
        getProgress(superiorPlayer);
        ItemsTracker itemsTracker = get(superiorPlayer);

        if (itemsTracker == null)
            return;

        Player player = superiorPlayer.asPlayer();
        assert player != null;

        removeItems(player.getInventory(), itemsTracker);
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        ItemsTracker itemsTracker = getOrCreate(superiorPlayer, s -> new ItemsTracker());

        if (itemsTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parseKeyPlaceholders(this.requiredItems, itemsTracker::getCount, itemMeta.getDisplayName(), true));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parseKeyPlaceholders(this.requiredItems, itemsTracker::getCount, line, true));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent e) {
        ItemStack itemStack = e.getItem().getItemStack();
        handleItemPickup(e.getPlayer(), e.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getClickedInventory() == null ||
                e.getClickedInventory().getType() != InventoryType.CHEST || e.getClick() != ClickType.SHIFT_LEFT)
            return;

        handleItemPickup((Player) e.getWhoClicked(), e.getCurrentItem());
    }

    private void handleItemPickup(Player player, @Nullable ItemStack itemStack) {
        if (getMissionItemData(itemStack) == null)
            return;

        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(player);

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> superiorPlayer.runIfOnline(unused -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    @Nullable
    private Pair<Key, Integer> getMissionItemData(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;

        Key itemKey = Key.of(itemStack);

        int index = 0;
        for (KeyRequirements requirements : requiredItems.keySet()) {
            if (requirements.contains(itemKey))
                return new Pair<>(requirements.getKey(itemKey), index);
            ++index;
        }

        return null;
    }

    private static void removeItems(PlayerInventory inventory, ItemsTracker itemsTracker) {
        if (itemsTracker.itemsTracker.isEmpty())
            return;

        for (Pair<ItemStack, Integer> itemToRemove : itemsTracker.itemsTracker) {
            ItemStack itemStack = itemToRemove.getKey();
            int removeCount = itemToRemove.getValue();
            int itemAmount = itemStack.getAmount();
            int newCount = Math.max(0, itemAmount - removeCount);
            itemStack.setAmount(newCount);
        }
    }

    private ItemProgress[] createProgressLookup() {
        ItemProgress[] lookupArray = new ItemProgress[this.requiredItems.size()];

        int index = 0;
        for (Integer requiredAmount : this.requiredItems.values())
            lookupArray[index++] = new ItemProgress(requiredAmount);

        return lookupArray;
    }

    private static class ItemProgress {

        private final int requiredCount;
        private int count;

        ItemProgress(int requiredCount) {
            this.requiredCount = requiredCount;
        }

        int track(int delta) {
            int newCount = this.count + delta;

            if (newCount >= this.requiredCount) {
                int oldCount = this.count;
                this.count = this.requiredCount;
                return this.requiredCount - oldCount;
            } else {
                this.count = newCount;
                return delta;
            }
        }

    }

    public class ItemsTracker {

        private final Set<Pair<ItemStack, Integer>> itemsTracker = new HashSet<>();
        private final KeyMap<Counter> countedItems = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        private int totalItemAmount = 0;
        private long lastCountTime = -1;

        void track(ItemStack itemStack, Key itemKey, int count) {
            if (count > 0) {
                itemsTracker.add(new Pair<>(itemStack, count));
                this.totalItemAmount += count;
                this.countedItems.computeIfAbsent(itemKey, i -> new Counter(0)).inc(count);
            }
        }

        int getCount(Key key) {
            Counter counter = this.countedItems.get(key);
            return counter == null ? 0 : counter.get();
        }

        void clear(long currTime) {
            itemsTracker.clear();
            countedItems.clear();
            totalItemAmount = 0;
            lastCountTime = currTime + COUNT_INVENTORY_THRESHOLD;
        }

        @Nullable
        private Key getMissionItemKey(@Nullable ItemStack itemStack) {
            if (itemStack == null || itemStack.getType() == Material.AIR)
                return null;

            Key itemKey = Key.of(itemStack);

            for (KeyRequirements requirements : requiredItems.keySet()) {
                if (requirements.contains(itemKey))
                    return requirements.getKey(itemKey);
            }

            return null;
        }

    }

}
