package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.MenuCommands;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.layout.InventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.IPlayerMenuView;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuCommandsImpl implements MenuCommands {

    private static final MenuCommandsImpl INSTANCE = new MenuCommandsImpl();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private static final Pattern COMMAND_PATTERN_ARGS = Pattern.compile("\\[(.+)](.+)");
    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\[(.+)]");

    public static MenuCommandsImpl getInstance() {
        return INSTANCE;
    }

    private MenuCommandsImpl() {

    }

    @Override
    public void runCommand(MenuView<?, ?> menuView, String command, InventoryClickEvent clickEvent) {
        try (ButtonClickContextImpl<?> ctx = ButtonClickContextImpl.obtain((MenuView) menuView, clickEvent)) {
            runCommand(menuView, command, ctx);
        }
    }

    @Override
    public void runCommand(MenuView<?, ?> menuView, String command, ButtonClickContext<?> context) {
        MenuLayout<?> menuLayout = menuView.getMenu().getLayout();
        if (menuLayout instanceof InventoryMenuLayout) {
            Player player = context.getPlayer();
            runCommandInternal(menuView, command, player, Bukkit.getConsoleSender(),
                    menuView::closeView, player::closeInventory);
        } else {
            DialogWrapper<?> dialog = ((AbstractMenuView<?, ?>) menuView).getDialog();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());
            runCommandInternal(menuView, command, context.getPlayer(), Bukkit.getConsoleSender(),
                    menuView::closeView, () -> plugin.getNMSDialogs().ifPresent(nmsDialogs ->
                            nmsDialogs.closeDialog(superiorPlayer, dialog)));
        }
    }

    private void runCommandInternal(MenuView<?, ?> menuView, String command, Player player,
                                    CommandSender sender, Runnable onClose, Runnable onBack) {
        Matcher matcher = COMMAND_PATTERN_ARGS.matcher(command);
        if (matcher.matches()) {
            handleSubCommand(menuView, matcher.group(1), matcher.group(2).trim(), player, sender, onClose, onBack);
            return;
        }
        if ((matcher = COMMAND_PATTERN.matcher(command)).matches()) {
            handleSubCommand(menuView, matcher.group(1), "", player, sender, onClose, onBack);
            return;
        }
        if (command.equalsIgnoreCase("close")) {
            ((AbstractMenuView<?, ?>) menuView).setClickedCloseButton();
            onClose.run();
            return;
        }
        if (command.equalsIgnoreCase("back")) {
            ((AbstractMenuView<?, ?>) menuView).setClickedCloseButton();
            onBack.run();
            return;
        }

        SuperiorPlayer targetPlayer = menuView instanceof IPlayerMenuView ?
                ((IPlayerMenuView) menuView).getSuperiorPlayer() : null;

        if (targetPlayer != null)
            command = placeholdersService.get().parsePlaceholders(targetPlayer.asOfflinePlayer(), command);
        else if (sender instanceof Player)
            command = placeholdersService.get().parsePlaceholders((Player) sender, command);
        else
            command = placeholdersService.get().parsePlaceholders(player, command);

        Bukkit.dispatchCommand(
                sender instanceof Player || command.startsWith("PLAYER:") ? player : Bukkit.getConsoleSender(),
                command.replace("PLAYER:", "").replace("%player%", player.getName()));
    }

    private void handleSubCommand(MenuView<?, ?> menuView, String subCommand, String args,
                                  Player player, CommandSender sender, Runnable onClose, Runnable onBack) {
        switch (subCommand.toLowerCase(Locale.ENGLISH)) {
            case "player":
                runCommandInternal(menuView, args, player, player, onClose, onBack);
                break;
            case "admin":
                String commandLabel = plugin.getSettings().getIslandCommand().split(",")[0];
                runCommandInternal(menuView, commandLabel + " admin " + args, player, sender, onClose, onBack);
                break;
            case "close":
                ((AbstractMenuView<?, ?>) menuView).setClickedCloseButton();
                onClose.run();
                break;
            case "back":
                ((AbstractMenuView<?, ?>) menuView).setClickedCloseButton();
                onBack.run();
                break;
            default:
                plugin.getCommands().dispatchSubCommand(sender, subCommand, args);
                break;
        }
    }

}
