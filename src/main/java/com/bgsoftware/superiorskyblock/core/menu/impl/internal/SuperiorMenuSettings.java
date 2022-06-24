package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.SettingsManagerImpl;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SuperiorMenuSettings extends PagedSuperiorMenu<SuperiorMenuSettings, ItemStack> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<SuperiorMenuSettings, ItemStack> menuPattern;

    private static final Map<String, List<String>> pathSlots = new HashMap<>();
    private static final String[] ignorePaths = new String[]{"database", "max-island-size", "island-roles",
            "worlds.normal-world", "commands-cooldown", "starter-chest", "event-commands"};

    private static final Map<UUID, ISuperiorMenu> lastPage = new HashMap<>();

    private static final Set<UUID> pageMove = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    private static CommentedConfiguration config;

    private final String configurationPath;

    private SuperiorMenuSettings(SuperiorPlayer superiorPlayer, String configurationPath) {
        super(menuPattern, superiorPlayer, true);
        this.configurationPath = configurationPath;
        setPageMoveRunnable(_superiorPlayer -> pageMove.add(_superiorPlayer.getUniqueId()));
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, currentPage, configurationPath);
    }

    @Override
    protected String replaceTitle(String title) {
        return configurationPath.isEmpty() ? title : ChatColor.BOLD + "Section: " + configurationPath;
    }

    @Override
    protected List<ItemStack> requestObjects() {
        LinkedList<ItemStack> itemStacks = new LinkedList<>();
        buildFromSection(itemStacks, config.getConfigurationSection(this.configurationPath));
        return Collections.unmodifiableList(itemStacks);
    }

    @Override
    public void open(ISuperiorMenu previousMenu) {
        lastPage.put(inventoryViewer.getUniqueId(), this);
        super.open(previousMenu);
    }

    @Override
    public void closeInventory(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer) {
        super.closeInventory(plugin, superiorPlayer);

        if ((!previousMove || previousMenu == null) && !activePlayers.remove(superiorPlayer.getUniqueId()) &&
                !pageMove.remove(superiorPlayer.getUniqueId())) {
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
                .setPreviousMoveAllowed(true)
                .setInventoryType(InventoryType.CHEST)
                .setRowsSize(6)
                .setButton(47, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new TemplateItem(new ItemBuilder(Material.PAPER).withName("{0}Previous Page"))))
                .setPreviousPageSlots(Collections.singletonList(47))
                .setButton(49, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new TemplateItem(new ItemBuilder(Materials.SUNFLOWER.toBukkitType())
                                .withName("&aCurrent Page").withLore("&7Page {0}"))))
                .setCurrentPageSlots(Collections.singletonList(49))
                .setButton(51, new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new TemplateItem(new ItemBuilder(Material.PAPER).withName("{0}Next Page"))))
                .setNextPageSlots(Collections.singletonList(51))
                .setPagedObjectSlots(IntStream.range(0, 36).boxed().collect(Collectors.toList()),
                        new SuperiorSettingsPagedObjectButton.Builder())
                .setButtons(IntStream.range(36, 45).boxed().collect(Collectors.toList()), new DummyButton.Builder<SuperiorMenuSettings>()
                        .setButtonItem(new TemplateItem(new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE.toBukkitItem()).withName(" "))))
                .setButton(40, new SaveButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, 1, "");
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, int page, String path) {
        SuperiorMenuSettings superiorMenuSettings = new SuperiorMenuSettings(superiorPlayer, path);
        superiorMenuSettings.currentPage = page;
        superiorMenuSettings.open(previousMenu);
    }

    private static boolean onPlayerChat(Player player, Object message, String path) {
        if (!message.toString().equalsIgnoreCase("-cancel")) {
            if (config.isList(path)) {
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
        BukkitExecutor.sync(() -> {
            PlayerChat.remove(player);

            ISuperiorMenu previousMenu = lastPage.remove(player.getUniqueId());
            if (previousMenu != null) {
                previousMenu.cloneAndOpen(previousMenu.getPreviousMenu());
            } else {
                SuperiorMenuSettings.openInventory(plugin.getPlayers().getSuperiorPlayer(player), null);
            }


        });
    }

    private static void saveConfiguration() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.setSettings(new SettingsManagerImpl(plugin));
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    private static void reloadConfiguration() {
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    private static void buildFromSection(List<ItemStack> itemStacks, ConfigurationSection section) {
        List<String> pathSlots = new ArrayList<>();

        for (String path : section.getKeys(false)) {
            String fullPath = section.getCurrentPath().isEmpty() ? path : section.getCurrentPath() + "." + path;

            if (Arrays.stream(ignorePaths).anyMatch(fullPath::contains))
                continue;

            ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK.toBukkitItem()).withName("&6" +
                    Formatters.CAPITALIZED_FORMATTER.format(path.replace("-", "_")
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
                itemBuilder.withLore("&7Click to edit section.");

            pathSlots.add(path);
            itemStacks.add(itemBuilder.build());
        }

        SuperiorMenuSettings.pathSlots.put(section.getCurrentPath(), pathSlots);
    }

    private static class SaveButton extends SuperiorMenuButton<SuperiorMenuSettings> {

        private SaveButton() {
            super(new TemplateItem(new ItemBuilder(Material.EMERALD).withName("&aSave Changes")),
                    null, null, null, null);
        }

        @Override
        public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenuSettings superiorMenu, InventoryClickEvent clickEvent) {
            Player player = (Player) clickEvent.getWhoClicked();
            BukkitExecutor.async(() -> {
                saveConfiguration();
                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Saved configuration successfully.");
                BukkitExecutor.sync(player::closeInventory);
            });
        }

        private static class Builder extends AbstractBuilder<Builder, SaveButton, SuperiorMenuSettings> {

            @Override
            public SaveButton build() {
                return new SaveButton();
            }

        }

    }

    private static class SuperiorSettingsPagedObjectButton extends PagedObjectButton<SuperiorMenuSettings, ItemStack> {

        private SuperiorSettingsPagedObjectButton(int objectIndex) {
            super(null, null, null, null, null,
                    null, objectIndex);
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
                String sectionPath = pathSlots.get(superiorMenu.configurationPath).get((superiorMenu.currentPage - 1) * 36 + clickEvent.getRawSlot());

                if (sectionPath == null)
                    return;

                String fullPath = superiorMenu.configurationPath.isEmpty() ? sectionPath :
                        superiorMenu.configurationPath + "." + sectionPath;

                if (config.isConfigurationSection(fullPath)) {
                    prepareClose(player, superiorMenu);
                    openInventory(superiorMenu.inventoryViewer, superiorMenu, 1, fullPath);
                } else if (config.isBoolean(fullPath)) {
                    updateConfig(player, fullPath, !config.getBoolean(fullPath));

                    prepareClose(player, superiorMenu);
                    superiorMenu.closePage();

                    reopenMenu(player);
                } else {
                    PlayerChat.listen(player, message -> onPlayerChat(player, message, fullPath));

                    prepareClose(player, superiorMenu);
                    player.closeInventory();

                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

                    if (config.isList(fullPath)) {
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                    }
                }
            } catch (Exception error) {
                PluginDebugger.debug(error);
            }
        }

        private void prepareClose(Player player, SuperiorMenuSettings superiorMenu) {
            superiorMenu.previousMove = false;
            activePlayers.add(player.getUniqueId());
        }

        private static class Builder extends PagedObjectBuilder<Builder, SuperiorSettingsPagedObjectButton, SuperiorMenuSettings> {

            @Override
            public SuperiorSettingsPagedObjectButton build() {
                return new SuperiorSettingsPagedObjectButton(getObjectIndex());
            }

        }

    }

}
