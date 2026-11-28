package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CraftingMissions extends BuiltinMission<CraftingMissions.CraftingsTracker> implements Listener {

    private final Map<ItemStack, Integer> itemsToCraft = new LinkedHashMap<>();

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection craftingsSection = section.getConfigurationSection("craftings");

        if (craftingsSection == null)
            throw new MissionLoadException("You must have the \"craftings\" section in the config.");

        for (String key : craftingsSection.getKeys(false)) {
            String type = section.getString("craftings." + key + ".type");
            short data = (short) section.getInt("craftings." + key + ".data", 0);
            int amount = section.getInt("craftings." + key + ".amount", 1);
            Material material;

            try {
                material = Material.valueOf(type);
            } catch (IllegalArgumentException ex) {
                throw new MissionLoadException("Invalid crafting result " + type + ".");
            }

            this.itemsToCraft.put(new ItemStack(material, 1, data), amount);
        }
    }

    @Override
    protected void registerListeners() {
        registerListener(this);
    }

    @Override
    protected void clearModuleData(CraftingsTracker data) {
        data.craftedItems.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        CraftingsTracker craftingsTracker = get(superiorPlayer);

        if (craftingsTracker == null)
            return 0.0;

        int requiredItems = 0;
        int interactions = 0;

        for (Map.Entry<ItemStack, Integer> entry : this.itemsToCraft.entrySet()) {
            requiredItems += entry.getValue();
            interactions += Math.min(craftingsTracker.getCrafts(entry.getKey()), entry.getValue());
        }

        return (double) interactions / requiredItems;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        CraftingsTracker craftingsTracker = get(superiorPlayer);

        if (craftingsTracker == null)
            return 0;

        int interactions = 0;

        for (Map.Entry<ItemStack, Integer> entry : this.itemsToCraft.entrySet())
            interactions += Math.min(craftingsTracker.getCrafts(entry.getKey()), entry.getValue());

        return interactions;
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, CraftingsTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            int index = 0;
            for (Map.Entry<ItemStack, Integer> craftedEntry : entry.getValue().craftedItems.entrySet()) {
                section.set(uuid + "." + index + ".item", craftedEntry.getKey());
                section.set(uuid + "." + index + ".amount", craftedEntry.getValue());
                index++;
            }
        }
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            if (uuid.equals("players"))
                continue;

            CraftingsTracker craftingsTracker = new CraftingsTracker();
            UUID playerUUID = UUID.fromString(uuid);
            SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

            insertData(superiorPlayer, craftingsTracker);

            for (String key : section.getConfigurationSection(uuid).getKeys(false)) {
                ItemStack itemStack = section.getItemStack(uuid + "." + key + ".item");
                int amount = section.getInt(uuid + "." + key + ".amount");
                craftingsTracker.craftedItems.put(itemStack, amount);
            }
        }
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        CraftingsTracker craftingsTracker = getOrCreate(superiorPlayer, s -> new CraftingsTracker());

        if (craftingsTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        Placeholders.PlaceholdersFunctions<ItemStack> placeholdersFunctions = new Placeholders.PlaceholdersFunctions<ItemStack>() {
            @Override
            public ItemStack getRequirementFromKey(String key) {
                return new ItemStack(Material.valueOf(key));
            }

            @Override
            public Optional<Integer> lookupRequirement(ItemStack requirement) {
                return itemsToCraft.entrySet().stream()
                        .filter(e -> e.getKey().isSimilar(requirement))
                        .findFirst()
                        .map(Map.Entry::getValue);
            }

            @Override
            public int getCountForRequirement(ItemStack requirement) {
                return craftingsTracker.getCrafts(requirement);
            }
        };

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parsePlaceholders(itemMeta.getDisplayName(), placeholdersFunctions));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parsePlaceholders(line, placeholdersFunctions));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || (e.getClickedInventory().getType() != InventoryType.WORKBENCH &&
                e.getClickedInventory().getType() != InventoryType.CRAFTING && e.getClickedInventory().getType() != InventoryType.FURNACE))
            return;

        int requiredSlot = e.getClickedInventory().getType() == InventoryType.FURNACE ? 2 : 0;

        ItemStack resultItem = e.getCurrentItem().clone();
        resultItem.setAmount(1);

        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer((Player) e.getWhoClicked());

        if (e.getRawSlot() == requiredSlot && itemsToCraft.containsKey(resultItem) &&
                this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this)) {
            int amountOfResult = countItems(e.getWhoClicked(), resultItem);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                int afterTickAmountOfResult = countItems(e.getWhoClicked(), resultItem);
                resultItem.setAmount(afterTickAmountOfResult - amountOfResult);
                trackItem(superiorPlayer, resultItem);
            }, 1L);
        }

    }

    private void trackItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        CraftingsTracker blocksTracker = getOrCreate(superiorPlayer, s -> new CraftingsTracker());

        if (blocksTracker == null)
            return;

        blocksTracker.trackItem(itemStack);

        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> superiorPlayer.runIfOnline(player -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    private static int countItems(HumanEntity humanEntity, ItemStack itemStack) {
        int amount = 0;

        if (itemStack == null)
            return amount;

        PlayerInventory playerInventory = humanEntity.getInventory();

        for (ItemStack invItem : playerInventory.getContents()) {
            if (invItem != null && itemStack.isSimilar(invItem))
                amount += invItem.getAmount();
        }

        if (humanEntity.getItemOnCursor() != null && itemStack.isSimilar(humanEntity.getItemOnCursor()))
            amount += humanEntity.getItemOnCursor().getAmount();

        return amount;
    }

    public static class CraftingsTracker {

        private final Map<ItemStack, Integer> craftedItems = new HashMap<>();

        void trackItem(ItemStack itemStack) {
            ItemStack keyItem = itemStack.clone();
            keyItem.setAmount(1);
            craftedItems.put(keyItem, craftedItems.getOrDefault(keyItem, 0) + itemStack.getAmount());
        }

        int getCrafts(ItemStack itemStack) {
            ItemStack keyItem = itemStack.clone();
            keyItem.setAmount(1);
            return craftedItems.getOrDefault(keyItem, 0);
        }

    }

}
