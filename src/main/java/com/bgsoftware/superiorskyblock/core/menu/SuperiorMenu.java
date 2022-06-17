package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.SuperiorMenuBlank;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SuperiorMenu<M extends ISuperiorMenu> implements ISuperiorMenu {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected static final String[] MENU_IGNORED_SECTIONS = new String[]{
            "items", "sounds", "commands", "back"
    };

    private static final Pattern COMMAND_PATTERN_ARGS = Pattern.compile("\\[(.+)](.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\[(.+)]");

    @Nullable
    private SuperiorMenuPattern<M> menuPattern;

    private boolean completed;
    private int backSlot;

    protected final SuperiorPlayer inventoryViewer;
    protected SuperiorPlayer targetPlayer = null;

    protected ISuperiorMenu previousMenu;
    protected boolean previousMove = true;
    protected boolean closeButton = false;
    protected boolean nextMove = false;
    private boolean refreshing = false;

    public SuperiorMenu(@Nullable SuperiorMenuPattern<M> menuPattern, SuperiorPlayer inventoryViewer) {
        this.inventoryViewer = inventoryViewer;
        this.resetData(menuPattern);
    }

    protected static String[] additionalMenuSections(String... ignoredSections) {
        String[] sections = Arrays.copyOf(MENU_IGNORED_SECTIONS, MENU_IGNORED_SECTIONS.length + ignoredSections.length);
        System.arraycopy(ignoredSections, 0, sections, MENU_IGNORED_SECTIONS.length, ignoredSections.length);
        return sections;
    }

    public static void killMenu(SuperiorPlayer superiorPlayer) {
        superiorPlayer.runIfOnline(player -> {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            InventoryHolder inventoryHolder = inventory == null ? null : inventory.getHolder();
            if (inventoryHolder instanceof ISuperiorMenu)
                ((ISuperiorMenu) inventoryHolder).setPreviousMove(false);

            player.closeInventory();
        });
    }

    protected static <T extends SuperiorMenu<?>> void refreshMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, superiorMenu) -> {
            superiorMenu.setPreviousMove(false);
            superiorMenu.open(superiorMenu.previousMenu);
        }));
    }

    protected static <T extends ISuperiorMenu> void destroyMenus(Class<T> menuClazz) {
        destroyMenus(menuClazz, superiorMenu -> true);
    }

    protected static <T extends ISuperiorMenu> void destroyMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, superiorMenu) -> player.closeInventory()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ISuperiorMenu> void runActionOnMenus(Class<T> menuClazz, Predicate<T> predicate,
                                                                   BiConsumer<Player, T> callback) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
                if (menuClazz.isInstance(inventoryHolder) && predicate.test((T) inventoryHolder)) {
                    T superiorMenu = (T) inventoryHolder;
                    callback.accept(player, superiorMenu);
                }
            } catch (Exception error) {
                PluginDebugger.debug(error);
            }
        }
    }

    protected static List<Integer> getSlots(ConfigurationSection section, String key, MenuPatternSlots menuPatternSlots) {
        return !section.contains(key) ? Collections.singletonList(-1) :
                menuPatternSlots.getSlots(section.getString(key), -1);
    }

    protected void updateTargetPlayer(SuperiorPlayer targetPlayer) {
        this.targetPlayer = targetPlayer;
    }

    public SuperiorPlayer getTargetPlayer() {
        return targetPlayer;
    }

    public SuperiorPlayer getInventoryViewer() {
        return inventoryViewer;
    }

    public void resetData(SuperiorMenuPattern<M> menuPattern) {
        this.menuPattern = menuPattern;
        this.completed = false;
    }

    @Nullable
    public SuperiorMenuPattern<M> getMenuPattern() {
        return this.menuPattern;
    }

    public int getRowsSize() {
        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");
        return this.menuPattern.getRowsSize();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Inventory getInventory() {
        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");
        return menuPattern.buildInventory((M) this, this::replaceTitle);
    }

    protected String replaceTitle(String title) {
        return title;
    }

    @Override
    public void setPreviousMove(boolean previousMove) {
        this.previousMove = previousMove;
    }

    @Nullable
    @Override
    public ISuperiorMenu getPreviousMenu() {
        return this.previousMenu;
    }

    public void refreshPage() {
        previousMove = false;
        open(previousMenu);
    }

    public void closePage() {
        inventoryViewer.runIfOnline(player -> {
            previousMove = false;
            player.closeInventory();
        });
    }

    public final void onClick(SuperiorSkyblockPlugin plugin, InventoryClickEvent clickEvent) {
        if (refreshing || clickEvent.getCurrentItem() == null)
            return;

        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");

        SuperiorMenuButton<M> menuButton = this.menuPattern.getButton(clickEvent.getRawSlot());

        String requiredPermission = menuButton.getRequiredPermission();
        if (requiredPermission != null && !inventoryViewer.hasPermission(requiredPermission)) {
            onButtonClickLackPermission(menuButton, clickEvent);
            return;
        }

        GameSound clickSound = menuButton.getClickSound();
        if (clickSound != null)
            clickSound.playSound(clickEvent.getWhoClicked());

        menuButton.getCommands().forEach(command -> runCommand(plugin, command, clickEvent, Bukkit.getConsoleSender()));

        PluginDebugger.debug("Action: Menu Click, Target: " + inventoryViewer.getName() + ", Item: " +
                (clickEvent.getCurrentItem() == null ? "AIR" : clickEvent.getCurrentItem().getType()) +
                ", Slot: " + clickEvent.getRawSlot());

        if (preButtonClick(menuButton, clickEvent))
            // noinspection unchecked
            menuButton.onButtonClick(plugin, (M) this, clickEvent);
    }

    public abstract void cloneAndOpen(ISuperiorMenu previousMenu);

    public boolean preButtonClick(SuperiorMenuButton<M> menuButton, InventoryClickEvent clickEvent) {
        return true;
    }

    public void onButtonClickLackPermission(SuperiorMenuButton<M> menuButton, InventoryClickEvent clickEvent) {
        GameSound lackPermissionSound = menuButton.getLackPermissionSound();
        if (lackPermissionSound != null)
            lackPermissionSound.playSound(clickEvent.getWhoClicked());
    }

    private void runCommand(SuperiorSkyblockPlugin plugin, String command, InventoryClickEvent e, CommandSender sender) {
        Matcher matcher = COMMAND_PATTERN_ARGS.matcher(command);

        if (matcher.matches()) {
            String subCommand = matcher.group(1);
            String args = matcher.group(2).trim();
            handleSubCommand(plugin, subCommand, args, e, sender);
        } else if ((matcher = COMMAND_PATTERN.matcher(command)).matches()) {
            String subCommand = matcher.group(1);
            handleSubCommand(plugin, subCommand, "", e, sender);
        } else if (command.equalsIgnoreCase("close")) {
            closeButton = true;
            previousMove = false;
            e.getWhoClicked().closeInventory();
        } else if (command.equalsIgnoreCase("back")) {
            closeButton = true;
            e.getWhoClicked().closeInventory();
        } else {
            PlaceholdersService placeholdersService = plugin.getServices().getPlaceholdersService();

            if (targetPlayer != null)
                command = placeholdersService.parsePlaceholders(targetPlayer.asOfflinePlayer(), command);
            else if (sender instanceof Player)
                command = placeholdersService.parsePlaceholders((Player) sender, command);

            Bukkit.dispatchCommand(sender instanceof Player || command.startsWith("PLAYER:") ? e.getWhoClicked() : Bukkit.getConsoleSender(),
                    command.replace("PLAYER:", "").replace("%player%", e.getWhoClicked().getName()));
        }
    }

    private void handleSubCommand(SuperiorSkyblockPlugin plugin, String subCommand, String args, InventoryClickEvent e, CommandSender sender) {
        switch (subCommand.toLowerCase(Locale.ENGLISH)) {
            case "player":
                runCommand(plugin, args, e, e.getWhoClicked());
                break;
            case "admin":
                String commandLabel = plugin.getSettings().getIslandCommand().split(",")[0];
                runCommand(plugin, commandLabel + " admin " + args, e, sender);
                break;
            case "close":
                closeButton = true;
                previousMove = false;
                e.getWhoClicked().closeInventory();
                break;
            case "back":
                closeButton = true;
                e.getWhoClicked().closeInventory();
                break;
            default:
                plugin.getCommands().dispatchSubCommand(sender, subCommand, args);
                break;
        }
    }

    public void open(ISuperiorMenu previousMenu) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> open(previousMenu));
            return;
        }

        Player player = inventoryViewer.asPlayer();

        if (player == null)
            return;

        if (player.isSleeping()) {
            Message.OPEN_MENU_WHILE_SLEEPING.send(inventoryViewer);
            return;
        }

        if (!plugin.getEventsBus().callPlayerOpenMenuEvent(inventoryViewer, this))
            return;

        PluginDebugger.debug("Action: Open Menu, Target: " + inventoryViewer.getName() + ", Menu: " + getClass().getName());

        if (menuPattern == null) {
            if (!(this instanceof SuperiorMenuBlank))
                SuperiorMenuBlank.openInventory(inventoryViewer, previousMenu);
            return;
        }

        Inventory inventory;

        try {
            inventory = getInventory();
        } catch (Exception ex) {
            if (!(this instanceof SuperiorMenuBlank)) {
                completed = false;
                SuperiorMenuBlank.openInventory(inventoryViewer, previousMenu);
            }

            PluginDebugger.debug(ex);
            ex.printStackTrace();
            return;
        }

        BukkitExecutor.sync(() -> {
            if (!inventoryViewer.isOnline())
                return;

            SuperiorMenu<M> currentMenu = null;
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
            if (inventoryHolder instanceof SuperiorMenu) {
                // noinspection unchecked
                currentMenu = (SuperiorMenu<M>) inventoryHolder;
                currentMenu.nextMove = true;
            }

            if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
                return;

            if (previousMenu != null)
                previousMenu.setPreviousMove(false);

            if (currentMenu != null && previousMenu != currentMenu)
                currentMenu.setPreviousMove(false);

            player.openInventory(inventory);

            GameSound openingSound = this.menuPattern.getOpeningSound();
            if (openingSound != null)
                openingSound.playSound(player);

            refreshing = false;

            this.previousMenu = previousMenu != null ? previousMenu : previousMove ? currentMenu : null;
        });
    }

    public void closeInventory(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer) {
        BukkitExecutor.sync(() -> {
            if (!nextMove && !closeButton && plugin.getSettings().isOnlyBackButton()) {
                open(previousMenu);
            } else if (previousMenu != null && this.menuPattern != null && this.menuPattern.isPreviousMoveAllowed()) {
                EventResult<ISuperiorMenu> eventResult = plugin.getEventsBus().callPlayerCloseMenuEvent(superiorPlayer,
                        this, previousMove ? previousMenu : null);

                if (previousMove) {
                    if (!eventResult.isCancelled()) {
                        ISuperiorMenu newMenu = eventResult.getResult();
                        if (newMenu != null)
                            newMenu.cloneAndOpen(newMenu.getPreviousMenu());
                    }
                } else if (eventResult.isCancelled()) {
                    open(previousMenu);
                } else {
                    previousMove = true;
                }
            }

            closeButton = false;
            nextMove = false;
        });
    }

    private int getBackSlot() {
        return backSlot;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperiorMenu && obj.getClass().equals(getClass());
    }

}
