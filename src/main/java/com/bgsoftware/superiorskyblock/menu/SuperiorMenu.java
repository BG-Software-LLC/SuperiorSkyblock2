package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
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
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SuperiorMenu implements ISuperiorMenu {

    protected static final String[] MENU_IGNORED_SECTIONS = new String[]{
            "items", "sounds", "commands", "back"
    };

    private static final Pattern COMMAND_PATTERN_ARGS = Pattern.compile("\\[(.+)](.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\[(.+)]");

    @Nullable
    private SuperiorMenuPattern menuPattern;

    private boolean completed;
    private int backSlot;

    protected final SuperiorPlayer superiorPlayer;
    protected SuperiorPlayer targetPlayer = null;

    protected ISuperiorMenu previousMenu;
    protected boolean previousMove = true, closeButton = false, nextMove = false;
    private boolean refreshing = false;

    public SuperiorMenu(@Nullable SuperiorMenuPattern menuPattern, SuperiorPlayer superiorPlayer) {
        this.superiorPlayer = superiorPlayer;
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
            if (inventoryHolder instanceof SuperiorMenu)
                ((SuperiorMenu) inventoryHolder).previousMove = false;

            player.closeInventory();
        });
    }

    protected static <T extends SuperiorMenu> void refreshMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, superiorMenu) -> {
            superiorMenu.previousMove = false;
            superiorMenu.open(superiorMenu.previousMenu);
        }));
    }

    protected static <T extends SuperiorMenu> void destroyMenus(Class<T> menuClazz) {
        destroyMenus(menuClazz, superiorMenu -> true);
    }

    protected static <T extends SuperiorMenu> void destroyMenus(Class<T> menuClazz, Predicate<T> predicate) {
        runActionOnMenus(menuClazz, predicate, ((player, superiorMenu) -> player.closeInventory()));
    }

    private static <T extends SuperiorMenu> void runActionOnMenus(Class<T> menuClazz, Predicate<T> predicate, BiConsumer<Player, SuperiorMenu> callback) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
                //noinspection unchecked
                if (menuClazz.isInstance(inventoryHolder) && predicate.test((T) inventoryHolder)) {
                    SuperiorMenu superiorMenu = (SuperiorMenu) inventoryHolder;
                    callback.accept(player, superiorMenu);
                }
            } catch (Exception error) {
                SuperiorSkyblockPlugin.debug(error);
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

    public void resetData(SuperiorMenuPattern menuPattern) {
        this.menuPattern = menuPattern;
        this.completed = false;
    }

    @Nullable
    public SuperiorMenuPattern getMenuPattern() {
        return this.menuPattern;
    }

    public int getRowsSize() {
        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");
        return this.menuPattern.getRowsSize();
    }

    @Override
    public Inventory getInventory() {
        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");
        return menuPattern.buildInventory(this, this::replaceTitle, superiorPlayer, targetPlayer);
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
        superiorPlayer.runIfOnline(player -> {
            previousMove = false;
            player.closeInventory();
        });
    }

    public final void onClick(SuperiorSkyblockPlugin plugin, InventoryClickEvent clickEvent) {
        if (refreshing)
            return;

        Preconditions.checkNotNull(this.menuPattern, "menu wasn't initialized properly.");

        SuperiorMenuButton menuButton = this.menuPattern.getButton(clickEvent.getRawSlot());

        String requiredPermission = menuButton.getRequiredPermission();
        if (requiredPermission != null && !superiorPlayer.hasPermission(requiredPermission)) {
            onButtonClickLackPermission(menuButton, clickEvent);
            return;
        }

        SoundWrapper clickSound = menuButton.getClickSound();
        if (clickSound != null)
            clickSound.playSound(clickEvent.getWhoClicked());

        menuButton.getCommands().forEach(command -> runCommand(plugin, command, clickEvent, Bukkit.getConsoleSender()));

        SuperiorSkyblockPlugin.debug("Action: Menu Click, Target: " + superiorPlayer.getName() + ", Item: " +
                (clickEvent.getCurrentItem() == null ? "AIR" : clickEvent.getCurrentItem().getType()) +
                ", Slot: " + clickEvent.getRawSlot());

        if (preButtonClick(menuButton, clickEvent))
            menuButton.onButtonClick(plugin, this, clickEvent);
    }

    public abstract void cloneAndOpen(ISuperiorMenu previousMenu);

    public boolean preButtonClick(SuperiorMenuButton menuButton, InventoryClickEvent clickEvent) {
        return true;
    }

    public void onButtonClickLackPermission(SuperiorMenuButton menuButton, InventoryClickEvent clickEvent) {
        SoundWrapper lackPermissionSound = menuButton.getLackPermissionSound();
        if (lackPermissionSound != null)
            lackPermissionSound.playSound(clickEvent.getWhoClicked());
    }

    private void runCommand(SuperiorSkyblockPlugin plugin, String command, InventoryClickEvent e, CommandSender sender) {
        Matcher matcher = COMMAND_PATTERN_ARGS.matcher(command);

        if (matcher.matches()) {
            String subCommand = matcher.group(1), args = matcher.group(2).trim();
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
            if (targetPlayer != null)
                command = PlaceholderHook.parse(targetPlayer, command);
            else if (sender instanceof Player)
                command = PlaceholderHook.parse(plugin.getPlayers().getSuperiorPlayer(sender), command);

            Bukkit.dispatchCommand(sender instanceof Player || command.startsWith("PLAYER:") ? e.getWhoClicked() : Bukkit.getConsoleSender(),
                    command.replace("PLAYER:", "").replace("%player%", e.getWhoClicked().getName()));
        }
    }

    private void handleSubCommand(SuperiorSkyblockPlugin plugin, String subCommand, String args, InventoryClickEvent e, CommandSender sender) {
        switch (subCommand.toLowerCase()) {
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
            Executor.async(() -> open(previousMenu));
            return;
        }

        Player player = superiorPlayer.asPlayer();

        if (player == null)
            return;

        if (player.isSleeping()) {
            Locale.OPEN_MENU_WHILE_SLEEPING.send(superiorPlayer);
            return;
        }

        SuperiorSkyblockPlugin.debug("Action: Open Menu, Target: " + superiorPlayer.getName() + ", Menu: " + getClass().getName());

        if (menuPattern == null) {
            if (!(this instanceof SuperiorMenuBlank))
                SuperiorMenuBlank.openInventory(superiorPlayer, previousMenu);
            return;
        }

        Inventory inventory;

        try {
            inventory = getInventory();
        } catch (Exception ex) {
            if (!(this instanceof SuperiorMenuBlank)) {
                completed = false;
                SuperiorMenuBlank.openInventory(superiorPlayer, previousMenu);
            }

            SuperiorSkyblockPlugin.debug(ex);
            ex.printStackTrace();
            return;
        }

        Executor.sync(() -> {
            if (!superiorPlayer.isOnline())
                return;

            SuperiorMenu currentMenu = null;
            InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();
            if (inventoryHolder instanceof SuperiorMenu) {
                currentMenu = (SuperiorMenu) inventoryHolder;
                currentMenu.nextMove = true;
            }

            if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
                return;

            if (previousMenu != null)
                previousMenu.setPreviousMove(false);

            player.openInventory(inventory);

            SoundWrapper openingSound = this.menuPattern.getOpeningSound();
            if (openingSound != null)
                openingSound.playSound(player);

            refreshing = false;

            this.previousMenu = previousMenu != null ? previousMenu : previousMove ? currentMenu : null;
        });
    }

    public void closeInventory(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer) {
        Executor.sync(() -> {
            if (!nextMove && !closeButton && plugin.getSettings().isOnlyBackButton()) {
                open(previousMenu);
            } else if (previousMenu != null && this.menuPattern != null && this.menuPattern.isPreviousMoveAllowed()) {
                if (previousMove)
                    previousMenu.cloneAndOpen(previousMenu.getPreviousMenu());
                else
                    previousMove = true;
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
