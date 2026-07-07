package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.Requirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.RawDataTracker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnchantingMissions extends BuiltinMission<RawDataTracker> implements Listener {

    private static final Pattern ENCHANTED_PATTERN = Pattern.compile("\\{enchanted_(.+?)}");

    private final Map<Requirements, RequiredEnchantment> requiredEnchantments = new LinkedHashMap<>();

    private String enchantedPlaceholder, notEnchantedPlaceholder;

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection requiredEnchantsSection = section.getConfigurationSection("required-enchants");

        if (requiredEnchantsSection == null)
            throw new MissionLoadException("You must have the \"required-enchants\" section in the config.");

        for (String key : requiredEnchantsSection.getKeys(false)) {
            List<String> itemTypes = section.getStringList("required-enchants." + key + ".types");
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            ConfigurationSection enchantsSection = section.getConfigurationSection("required-enchants." + key + ".enchants");

            if (enchantsSection == null)
                throw new MissionLoadException("You must have the \"required-enchants." + key + ".enchants\" section in the config.");

            for (String enchantment : enchantsSection.getKeys(false)) {
                Enchantment _enchantment = Enchantment.getByName(enchantment.toUpperCase(Locale.ENGLISH));

                if (_enchantment == null)
                    throw new MissionLoadException("Enchantment " + enchantment + " is not valid.");

                enchantments.put(_enchantment, section.getInt("required-enchants." + key + ".enchants." + enchantment));
            }

            if (!enchantments.isEmpty()) {
                this.requiredEnchantments.put(new Requirements(itemTypes), new RequiredEnchantment(key, enchantments,
                        section.getInt("required-enchants." + key + ".amount", 1)));
            }
        }

        this.enchantedPlaceholder = section.getString("enchanted-placeholder", "Yes");
        this.notEnchantedPlaceholder = section.getString("not-enchanted-placeholder", "No");
    }

    @Override
    protected void registerListeners() {
        registerListener(this);

        try {
            Class.forName("org.bukkit.event.inventory.PrepareAnvilEvent");
            registerListener(new PrepareAnvilListener());
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void clearModuleData(RawDataTracker data) {
        data.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        RawDataTracker enchantsTracker = get(superiorPlayer);

        if (enchantsTracker == null)
            return 0.0;

        int requiredItems = 0;
        int enchants = 0;

        for (RequiredEnchantment requiredEnchantment : this.requiredEnchantments.values()) {
            requiredItems += requiredEnchantment.amount;
            enchants += Math.min(enchantsTracker.getCount(requiredEnchantment.key), requiredEnchantment.amount);
        }

        return (double) enchants / requiredItems;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        RawDataTracker enchantsTracker = get(superiorPlayer);

        if (enchantsTracker == null)
            return 0;

        int enchants = 0;

        for (RequiredEnchantment requiredEnchantment : this.requiredEnchantments.values()) {
            enchants += Math.min(enchantsTracker.getCount(requiredEnchantment.key), requiredEnchantment.amount);
        }

        return enchants;
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        RawDataTracker enchantsTracker = getOrCreate(superiorPlayer, s -> new RawDataTracker());

        if (enchantsTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        Placeholders.PlaceholdersFunctions<Requirements> placeholdersFunctions = new Placeholders.PlaceholdersFunctions<Requirements>() {
            @Override
            public Requirements getRequirementFromKey(String key) {
                for (Requirements requirements : requiredEnchantments.keySet()) {
                    if (requirements.contains(key))
                        return requirements;
                }

                return null;
            }

            @Override
            public Optional<Integer> lookupRequirement(Requirements requirement) {
                return Optional.ofNullable(requiredEnchantments.get(requirement))
                        .map(requiredEnchantment -> requiredEnchantment.amount);
            }

            @Override
            public int getCountForRequirement(Requirements requirement) {
                return Optional.ofNullable(requiredEnchantments.get(requirement))
                        .map(requiredEnchantment -> enchantsTracker.getCount(requiredEnchantment.key))
                        .orElse(0);
            }
        };

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(parsePlaceholders(placeholdersFunctions, enchantsTracker, itemMeta.getDisplayName()));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(parsePlaceholders(placeholdersFunctions, enchantsTracker, line));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemEnchant(EnchantItemEvent e) {
        ItemStack simulateEnchanted = e.getItem().clone();
        ItemMeta itemMeta = simulateEnchanted.getItemMeta();

        for (Map.Entry<Enchantment, Integer> entry : e.getEnchantsToAdd().entrySet()) {
            if (simulateEnchanted.getType() == Material.BOOK) {
                simulateEnchanted = new ItemStack(Material.ENCHANTED_BOOK);
                itemMeta = simulateEnchanted.getItemMeta();
            }

            if (itemMeta == null)
                continue;

            if (simulateEnchanted.getType() == Material.ENCHANTED_BOOK) {
                ((EnchantmentStorageMeta) itemMeta).addStoredEnchant(entry.getKey(), entry.getValue(), true);
            } else if (entry.getKey().canEnchantItem(simulateEnchanted)) {
                itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        simulateEnchanted.setItemMeta(itemMeta);

        getMissionRequirement(simulateEnchanted).ifPresent(requirement -> handleEnchanting(e.getEnchanter(), requirement));
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, RawDataTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            List<String> data = new ArrayList<>();
            entry.getValue().getCounts().forEach((enchant, amount) -> data.add(enchant + ";" + amount.get()));
            section.set(uuid, data);
        }
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            RawDataTracker enchantsTracker = new RawDataTracker();
            UUID playerUUID = UUID.fromString(uuid);
            SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

            insertData(superiorPlayer, enchantsTracker);

            section.getStringList(uuid).forEach(line -> {
                String[] sections = line.split(";");
                int amount = sections.length == 2 ? Integer.parseInt(sections[1]) : 1;
                String enchantment = sections[0];
                enchantsTracker.load(enchantment, amount);
            });
        }
    }

    private Optional<RequiredEnchantment> getMissionRequirement(ItemStack itemStack) {
        outerLoop:
        for (Map.Entry<Requirements, RequiredEnchantment> requirement : this.requiredEnchantments.entrySet()) {
            if (!requirement.getKey().contains(itemStack.getType().name()))
                continue;

            for (Map.Entry<Enchantment, Integer> requiredEnchantment : requirement.getValue().enchantments.entrySet()) {
                Enchantment enchantment = requiredEnchantment.getKey();
                int requiredLevel = requiredEnchantment.getValue();

                if (itemStack.getType() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                    if (storageMeta == null || storageMeta.getStoredEnchantLevel(enchantment) < requiredLevel)
                        continue outerLoop;
                } else if (itemStack.getEnchantmentLevel(enchantment) < requiredLevel) {
                    continue outerLoop;
                }
            }

            return Optional.of(requirement.getValue());
        }

        return Optional.empty();
    }

    private void handleEnchanting(Player player, RequiredEnchantment requirement) {
        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(player);

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        RawDataTracker enchantsTracker = getOrCreate(superiorPlayer, s -> new RawDataTracker());

        if (enchantsTracker == null)
            return;

        enchantsTracker.track(requirement.key, 1);

        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, () -> superiorPlayer.runIfOnline(_player -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    private String parsePlaceholders(Placeholders.PlaceholdersFunctions<Requirements> placeholdersFunctions,
                                     RawDataTracker enchantsTracker, String line) {
        Matcher matcher = ENCHANTED_PATTERN.matcher(line);

        if (matcher.find()) {
            String requiredBlock = matcher.group(1).toUpperCase(Locale.ENGLISH);

            Optional<Map.Entry<Requirements, RequiredEnchantment>> entry = requiredEnchantments.entrySet().stream()
                    .filter(e -> e.getKey().contains(requiredBlock)).findAny();

            if (entry.isPresent()) {
                RequiredEnchantment requiredEnchantment = entry.get().getValue();
                line = matcher.replaceAll(enchantsTracker.getCount(requiredEnchantment.key) > 0 ? enchantedPlaceholder : notEnchantedPlaceholder);
            }
        }

        return Placeholders.parsePlaceholders(line, placeholdersFunctions);
    }

    private static class RequiredEnchantment {

        private final String key;
        private final Map<Enchantment, Integer> enchantments;
        private final Integer amount;

        RequiredEnchantment(String key, Map<Enchantment, Integer> enchantments, Integer amount) {
            this.key = key;
            this.enchantments = enchantments;
            this.amount = amount;
        }

    }

    private class PrepareAnvilListener implements Listener {

        private final Map<UUID, RequiredEnchantment> addingEnchantments = new HashMap<>();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemAnvil(org.bukkit.event.inventory.PrepareAnvilEvent e) {
            if (e.getResult() == null || !isAddingEnchantment(e))
                return;

            getMissionRequirement(e.getResult()).ifPresent(requirement ->
                    addingEnchantments.put(e.getView().getPlayer().getUniqueId(), requirement));
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onItemAnvil(InventoryClickEvent e) {
            if (e.getRawSlot() != 2)
                return;

            RequiredEnchantment requiredEnchantment = addingEnchantments.remove(e.getWhoClicked().getUniqueId());
            if (requiredEnchantment == null)
                return;

            handleEnchanting((Player) e.getWhoClicked(), requiredEnchantment);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent e) {
            addingEnchantments.remove(e.getPlayer().getUniqueId());
        }

        private boolean isAddingEnchantment(org.bukkit.event.inventory.PrepareAnvilEvent e) {
            ItemStack result = e.getResult();
            if (result == null)
                return false;

            ItemStack firstSlot = e.getInventory().getItem(0);
            if (firstSlot == null)
                return false;

            ItemStack secondSlot = e.getInventory().getItem(1);
            if (secondSlot == null)
                return false;

            return firstSlot.getType() == secondSlot.getType() || secondSlot.getType().name().contains("BOOK");
        }

    }

}
