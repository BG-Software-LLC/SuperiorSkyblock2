package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.SettingsHandler;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SuperiorMenuSettings extends PagedSuperiorMenu<SuperiorMenuSettings, ItemStack> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<SuperiorMenuSettings, ItemStack> menuPattern;

    private static final List<String> pathSlots = new ArrayList<>();
    private static final String[] ignorePaths = new String[]{"database", "max-island-size", "island-roles",
            "worlds.normal-world", "commands-cooldown", "starter-chest", "event-commands"};
    private static final Map<UUID, Integer> lastPage = new HashMap<>();
    private static final Set<UUID> pageMove = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    private static CommentedConfiguration config;

    private SuperiorMenuSettings(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer, true);
        setPageMoveRunnable(_superiorPlayer -> pageMove.add(_superiorPlayer.getUniqueId()));
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    @Override
    protected List<ItemStack> requestObjects() {
        List<ItemStack> itemStacks = new ArrayList<>();
        buildFromSection(itemStacks, config.getConfigurationSection(""));
        return itemStacks;
    }

    @Override
    public void open(ISuperiorMenu previousMenu) {
        lastPage.put(inventoryViewer.getUniqueId(), currentPage);
        super.open(previousMenu);
    }

    @Override
    public void closeInventory(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer) {
        super.closeInventory(plugin, superiorPlayer);

        if (!activePlayers.remove(superiorPlayer.getUniqueId()) && !pageMove.remove(superiorPlayer.getUniqueId())) {
            reloadConfiguration();
            lastPage.remove(superiorPlayer.getUniqueId());
        }
    }

    public static void init() {
        menuPattern = null;

        File file = new File(plugin.getDataFolder(), "config.yml");
        config = CommentedConfiguration.loadConfiguration(file);

        PagedMenuPattern.Builder<SuperiorMenuSettings, ItemStack> patternBuilder = new PagedMenuPattern.Builder<>();

        menuPattern = patternBuilder
                .setTitle(ChatColor.BOLD + "Settings Editor")
                .setInventoryType(InventoryType.CHEST)
                .setRowsSize(6)
                .setButton(47, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new ItemBuilder(Material.PAPER).withName("{0}Previous Page")))
                .setPreviousPageSlots(Collections.singletonList(47))
                .setButton(49, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new ItemBuilder(Materials.SUNFLOWER.toBukkitType()).withName("&aCurrent Page").withLore("&7Page {0}")))
                .setCurrentPageSlots(Collections.singletonList(49))
                .setButton(51, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new ItemBuilder(Material.PAPER).withName("{0}Next Page")))
                .setNextPageSlots(Collections.singletonList(51))
                .setPagedObjectSlots(IntStream.range(0, 36).boxed().collect(Collectors.toList()),
                        new SuperiorSettingsPagedObjectButton.Builder())
                .setButtons(IntStream.range(36, 45).boxed().collect(Collectors.toList()), new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE.toBukkitItem()).withName(" ")))
                .setButton(40, new SaveButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, 1);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, int page) {
        SuperiorMenuSettings superiorMenuSettings = new SuperiorMenuSettings(superiorPlayer);
        superiorMenuSettings.currentPage = page;
        superiorMenuSettings.open(previousMenu);
    }

    private static boolean onPlayerChat(Player player, Object message, String path) {
        if (!message.toString().equalsIgnoreCase("-cancel")) {
            if (config.isConfigurationSection(path)) {
                Matcher matcher;
                if (!(matcher = Pattern.compile("(.*):(.*)").matcher(message.toString())).matches()) {
                    player.sendMessage(ChatColor.RED + "Please follow the <sub-section>:<value> format");
                } else {
                    path = path + "." + matcher.group(1);
                    message = matcher.group(2);

                    if (config.get(path) != null && config.get(path).toString().equals(message)) {
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + matcher.group(1) + " from " + path);
                        message = null;
                    } else {
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + message.toString() + " to " + path);

                        try {
                            message = Integer.valueOf(message.toString());
                        } catch (IllegalArgumentException ex) {
                            if (message.toString().equalsIgnoreCase("true") || message.toString().equalsIgnoreCase("false")) {
                                message = Boolean.valueOf(message.toString());
                            }
                        }

                    }

                    config.set(path, message);
                }
            } else if (config.isList(path)) {
                List<String> list = config.getStringList(path);

                if (list.contains(message.toString())) {
                    list.remove(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + message + " from " + path);
                } else {
                    list.add(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + message + " to " + path);
                }

                config.set(path, list);
            } else {
                boolean valid = true;
                if (config.isInt(path)) {
                    try {
                        message = Integer.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                } else if (config.isDouble(path)) {
                    try {
                        message = Double.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                if (valid) {
                    updateConfig(player, path, message);
                }
            }
        }

        reopenMenu(player);

        return true;
    }

    private static void updateConfig(Player player, String path, Object value) {
        config.set(path, value);
        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY +
                " Changed value of " + path + " to " + value);
    }

    private static void reopenMenu(Player player) {
        Executor.sync(() -> {
            Integer page = lastPage.remove(player.getUniqueId());

            if (page == null)
                page = 1;

            PlayerChat.remove(player);
            SuperiorMenuSettings.openInventory(plugin.getPlayers().getSuperiorPlayer(player), null, Math.max(1, page));
        });
    }

    private static void saveConfiguration() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.setSettings(new SettingsHandler(plugin));
        } catch (Exception ex) {
            ex.printStackTrace();
            SuperiorSkyblockPlugin.debug(ex);
        }
    }

    private static void reloadConfiguration() {
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            SuperiorSkyblockPlugin.debug(ex);
        }
    }

    private static void buildFromSection(List<ItemStack> itemStacks, ConfigurationSection section) {
        for (String path : section.getKeys(false)) {
            String fullPath = section.getCurrentPath().isEmpty() ? path : section.getCurrentPath() + "." + path;

            if (Arrays.stream(ignorePaths).anyMatch(fullPath::contains))
                continue;

            if (section.isConfigurationSection(path)) {
                buildFromSection(itemStacks, section.getConfigurationSection(path));
            } else {
                ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK.toBukkitItem()).withName("&6" +
                        StringUtils.format(fullPath.replace("-", "_")
                                .replace(".", "_").replace(" ", "_"))
                );

                if (section.isBoolean(path))
                    itemBuilder.withLore("&7Value: " + section.getBoolean(path));
                else if (section.isInt(path))
                    itemBuilder.withLore("&7Value: " + section.getInt(path));
                else if (section.isDouble(path))
                    itemBuilder.withLore("&7Value: " + section.getDouble(path));
                else if (section.isString(path))
                    itemBuilder.withLore("&7Value: " + section.getString(path));
                else if (section.isList(path))
                    itemBuilder.withLore("&7Value:", section.getStringList(path));
                else if (section.isConfigurationSection(path))
                    itemBuilder.withLore("&7Value:", section.getConfigurationSection(path));

                pathSlots.add(fullPath);
                itemStacks.add(itemBuilder.build());
            }
        }
    }

    private static final class SaveButton extends SuperiorMenuButton<SuperiorMenuSettings> {

        private SaveButton() {
            super(new ItemBuilder(Material.EMERALD).withName("&aSave Changes"),
                    null, null, null, null);
        }

        @Override
        public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenuSettings superiorMenu, InventoryClickEvent clickEvent) {
            Player player = (Player) clickEvent.getWhoClicked();
            Executor.async(() -> {
                saveConfiguration();
                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Saved configuration successfully.");
                Executor.sync(player::closeInventory);
            });
        }

        private static class Builder extends AbstractBuilder<Builder, SaveButton, SuperiorMenuSettings> {

            @Override
            public SaveButton build() {
                return new SaveButton();
            }

        }

    }

    private static final class SuperiorSettingsPagedObjectButton extends PagedObjectButton<SuperiorMenuSettings, ItemStack> {

        private static final SuperiorSettingsPagedObjectButton INSTANCE = new SuperiorSettingsPagedObjectButton();

        private SuperiorSettingsPagedObjectButton() {
            super(null, null, null, null, null, null);
        }

        @Override
        public ItemStack modifyButtonItem(ItemStack buttonItem, SuperiorMenuSettings superiorMenu, ItemStack itemStack) {
            return itemStack;
        }

        @Override
        public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenuSettings superiorMenu,
                                  InventoryClickEvent clickEvent) {
            try {
                Player player = (Player) clickEvent.getWhoClicked();
                String path = pathSlots.get((superiorMenu.currentPage - 1) * 36 + clickEvent.getRawSlot());

                if (path == null)
                    return;

                if (config.isBoolean(path)) {
                    updateConfig(player, path, !config.getBoolean(path));
                    activePlayers.add(player.getUniqueId());

                    superiorMenu.closePage();
                    reopenMenu(player);
                } else {
                    activePlayers.add(player.getUniqueId());
                    PlayerChat.listen(player, message -> onPlayerChat(player, message, path));

                    player.closeInventory();
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

                    if (config.isList(path) || config.isConfigurationSection(path)) {
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                    }
                }
            } catch (Exception error) {
                SuperiorSkyblockPlugin.debug(error);
            }
        }

        private static class Builder extends PagedObjectBuilder<Builder, SuperiorSettingsPagedObjectButton, SuperiorMenuSettings> {

            @Override
            public SuperiorSettingsPagedObjectButton build() {
                return INSTANCE;
            }

        }

    }

}
